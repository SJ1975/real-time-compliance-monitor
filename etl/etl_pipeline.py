import os
import json
import pandas as pd
from datetime import datetime
from dotenv import load_dotenv
from sqlalchemy import create_engine


load_dotenv()

def get_engine():
    host     = os.getenv("DB_HOST", "localhost")
    port     = os.getenv("DB_PORT", "5433")
    database = os.getenv("DB_NAME", "compliance_db")
    user     = os.getenv("DB_USER", "postgres")
    password = os.getenv("DB_PASSWORD", "postgres")

    url = f"postgresql+pg8000://{user}:{password}@{host}:{port}/{database}"
    print(f"  Connecting to: {host}:{port}/{database}")
    return create_engine(url)


# ══════════════════════════════════════════════════
# EXTRACT — Pull raw data from PostgreSQL
# ══════════════════════════════════════════════════
def extract():
    print("\n" + "="*50)
    print("  EXTRACT PHASE")
    print("="*50)

    try:
        engine = get_engine()

        query = """
            SELECT transaction_id, user_id, amount, currency,
                   merchant, location, timestamp, flagged,
                   risk_score, risk_level, flag_reasons, processed_at
            FROM transactions
            ORDER BY timestamp DESC
        """

        df = pd.read_sql_query(query, engine)
        print(f"  ✅ Connected successfully!")
        print(f"  ✅ Extracted {len(df)} transactions")
        return df

    except Exception as e:
        print(f"  ❌ Extract failed: {e}")
        raise

# ══════════════════════════════════════════════════
# TRANSFORM — Clean and enrich the data
# ══════════════════════════════════════════════════
def transform(df):
    print("\n" + "="*50)
    print("  TRANSFORM PHASE")
    print("="*50)

    if df.empty:
        print("  ⚠️  No data to transform")
        return df

    # 1. Convert data types
    df['amount']    = pd.to_numeric(df['amount'])
    df['timestamp'] = pd.to_datetime(df['timestamp'])
    df['flagged']   = df['flagged'].astype(bool)
    print("  ✅ Data types converted")

    # 2. Add derived columns
    df['date']        = df['timestamp'].dt.date
    df['hour']        = df['timestamp'].dt.hour
    df['day_of_week'] = df['timestamp'].dt.day_name()
    df['month']       = df['timestamp'].dt.month_name()
    print("  ✅ Date columns derived")

    # 3. Add amount classification
    df['amount_band'] = pd.cut(
        df['amount'],
        bins=[0, 1000, 5000, 10000, float('inf')],
        labels=['LOW (<1K)', 'MEDIUM (1K-5K)',
                'HIGH (5K-10K)', 'VERY HIGH (>10K)']
    )
    print("  ✅ Amount bands classified")

    # 4. Fill nulls
    df['flag_reasons'] = df['flag_reasons'].fillna('NONE')
    df['risk_level']   = df['risk_level'].fillna('LOW')
    print("  ✅ Null values handled")

    print(f"  ✅ Transform complete — {len(df)} records ready")
    return df

# ══════════════════════════════════════════════════
# LOAD — Generate analytics reports
# ══════════════════════════════════════════════════
def load(df):
    print("\n" + "="*50)
    print("  LOAD PHASE")
    print("="*50)

    os.makedirs("reports", exist_ok=True)
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    reports = {}

    # ── Report 1: Overall Summary ──
    total         = len(df)
    flagged_count = df['flagged'].sum()
    reports['summary'] = {
        "generated_at":           datetime.now().isoformat(),
        "total_transactions":     int(total),
        "flagged_transactions":   int(flagged_count),
        "clean_transactions":     int(total - flagged_count),
        "flagged_percentage":     round(flagged_count / total * 100, 2) if total > 0 else 0,
        "total_volume_usd":       round(float(df['amount'].sum()), 2),
        "average_amount":         round(float(df['amount'].mean()), 2),
        "max_transaction":        round(float(df['amount'].max()), 2),
        "min_transaction":        round(float(df['amount'].min()), 2),
    }
    print(f"  ✅ Summary report generated")

    # ── Report 2: Daily Volume Trends ──
    daily = df.groupby('date').agg(
        total_transactions=('transaction_id', 'count'),
        total_amount=('amount', 'sum'),
        avg_amount=('amount', 'mean'),
        flagged_count=('flagged', 'sum')
    ).reset_index()
    daily['date'] = daily['date'].astype(str)
    daily['total_amount'] = daily['total_amount'].round(2)
    daily['avg_amount']   = daily['avg_amount'].round(2)
    reports['daily_trends'] = daily.to_dict(orient='records')
    print(f"  ✅ Daily trends report: {len(daily)} days")

    # ── Report 3: Hourly Patterns ──
    hourly = df.groupby('hour').agg(
        transaction_count=('transaction_id', 'count'),
        avg_amount=('amount', 'mean'),
        flagged_count=('flagged', 'sum')
    ).reset_index()
    hourly['avg_amount']  = hourly['avg_amount'].round(2)
    hourly['time_label']  = hourly['hour'].apply(
        lambda h: f"{h:02d}:00 - {h+1:02d}:00"
    )
    reports['hourly_patterns'] = hourly.to_dict(orient='records')
    print(f"  ✅ Hourly patterns report: {len(hourly)} hours")

    # ── Report 4: Risk Level Breakdown ──
    risk = df.groupby('risk_level').agg(
        count=('transaction_id', 'count'),
        total_amount=('amount', 'sum'),
        avg_risk_score=('risk_score', 'mean')
    ).reset_index()
    risk['total_amount']   = risk['total_amount'].round(2)
    risk['avg_risk_score'] = risk['avg_risk_score'].round(2)
    reports['risk_breakdown'] = risk.to_dict(orient='records')
    print(f"  ✅ Risk breakdown report: {len(risk)} levels")

    # ── Report 5: Location Analytics ──
    location = df.groupby('location').agg(
        total_transactions=('transaction_id', 'count'),
        flagged_count=('flagged', 'sum'),
        avg_risk_score=('risk_score', 'mean'),
        total_amount=('amount', 'sum')
    ).reset_index()
    location['flagged_pct'] = (
        location['flagged_count'] / location['total_transactions'] * 100
    ).round(2)
    location['avg_risk_score'] = location['avg_risk_score'].round(2)
    location['total_amount']   = location['total_amount'].round(2)
    location['risk_label'] = location['flagged_pct'].apply(
        lambda x: 'DANGEROUS' if x >= 50 else ('MODERATE' if x >= 20 else 'SAFE')
    )
    location = location.sort_values('flagged_count', ascending=False)
    reports['location_analytics'] = location.to_dict(orient='records')
    print(f"  ✅ Location analytics report: {len(location)} locations")

    # ── Report 6: Top Flagged Users ──
    top_users = df[df['flagged']].groupby('user_id').agg(
        flagged_count=('transaction_id', 'count'),
        total_flagged_amount=('amount', 'sum'),
        avg_risk_score=('risk_score', 'mean')
    ).reset_index()
    top_users = top_users.sort_values('flagged_count', ascending=False).head(10)
    top_users['total_flagged_amount'] = top_users['total_flagged_amount'].round(2)
    top_users['avg_risk_score']       = top_users['avg_risk_score'].round(2)
    reports['top_flagged_users'] = top_users.to_dict(orient='records')
    print(f"  ✅ Top flagged users report: {len(top_users)} users")

    # ── Save all reports to JSON ──
    output_file = f"reports/etl_report_{timestamp}.json"
    with open(output_file, 'w') as f:
        json.dump(reports, f, indent=2, default=str)

    print(f"\n  ✅ All reports saved to: {output_file}")
    return output_file

# ══════════════════════════════════════════════════
# MAIN — Run the full ETL pipeline
# ══════════════════════════════════════════════════
def run_pipeline():
    print("\n" + "="*50)
    print("  Compliance ETL Pipeline")
    print(f"  Started at: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print("="*50)

    try:
        # Step 1: Extract
        raw_data = extract()

        if raw_data.empty:
            print("\n  ⚠️  No transactions found in database.")
            print("  Run the Spring Boot app first to generate data.")
            return

        # Step 2: Transform
        transformed_data = transform(raw_data)

        # Step 3: Load
        output_file = load(transformed_data)

        # Final summary
        print("\n" + "="*50)
        print("  ✅ ETL Pipeline Completed Successfully!")
        print(f"  Total records processed: {len(transformed_data)}")
        print(f"  Report saved to: {output_file}")
        print("="*50 + "\n")

    except Exception as e:
        print(f"\n  ❌ Pipeline failed: {e}")
        raise

if __name__ == "__main__":
    run_pipeline()