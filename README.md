# 🛡️ Real-Time Compliance Monitoring & Risk Detection System

A production-grade, event-driven backend system that processes high-volume
financial transaction data in real time, detects suspicious activities using
a rule engine, and provides fast search capabilities via ElasticSearch.

---

## 🏗️ Architecture
```
Transaction Generator
        ↓
   Apache Kafka
        ↓
  Consumer Service
        ↓
Compliance Rule Engine
   ↙           ↘
PostgreSQL   ElasticSearch
   ↘           ↙
    REST APIs
```

---

## ⚙️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| Event Streaming | Apache Kafka |
| Primary Database | PostgreSQL 15 |
| Search Engine | ElasticSearch 8.11 |
| Build Tool | Maven |
| Containerization | Docker + Docker Compose |

---

## 🚀 Features

- **Real-Time Transaction Processing** via Kafka event streaming
- **Compliance Rule Engine** with 3 configurable rules:
    - High Amount Detection (configurable threshold)
    - Velocity Check (too many transactions in short window)
    - Suspicious Location Flagging
- **Risk Scoring System** — assigns scores 0-100, classifies as LOW/MEDIUM/HIGH/CRITICAL
- **Dual Storage** — PostgreSQL for reliability, ElasticSearch for fast search
- **Auto Transaction Generator** — simulates real financial activity
- **REST APIs** — full CRUD + advanced search + dashboard endpoints
- **Docker Support** — entire system runs with one command

---

## 📦 Project Structure
```
src/main/java/com/compliance/riskmonitor/
├── controller/          # REST endpoints
├── service/             # Business logic
│   └── impl/
├── repository/          # JPA + ES repositories
├── entity/              # PostgreSQL JPA entities
├── document/            # ElasticSearch documents
├── dto/                 # Request/Response objects
├── kafka/
│   ├── producer/        # Kafka producers + generator
│   └── consumer/        # Kafka consumers
├── engine/              # Compliance rule engine
│   └── rules/           # Individual rules
├── config/              # App configuration
└── exception/           # Global exception handling
```

---

## 🐳 Quick Start (Docker)

### Prerequisites
- Docker Desktop installed and running
- Java 17+ (for local development)
- Maven 3.8+

### Run with Docker (Recommended)
```bash
# Clone the repository
git clone https://github.com/YOUR_USERNAME/real-time-compliance-monitor.git
cd real-time-compliance-monitor

# Start all services
docker-compose up --build
```

All 6 services start automatically:
- Spring Boot App → `http://localhost:8080`
- ElasticSearch → `http://localhost:9200`
- Kibana → `http://localhost:5601`
- PostgreSQL → `localhost:5433`
- Kafka → `localhost:9092`

### Run Locally (Without Docker App)
```bash
# Start infrastructure only
docker-compose up -d postgres kafka elasticsearch zookeeper

# Run Spring Boot app
mvn spring-boot:run
```

---

## 📡 API Reference

### Transactions

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/transactions` | Create a transaction |
| GET | `/api/v1/transactions` | Get all transactions |
| GET | `/api/v1/transactions/{id}` | Get by ID |
| GET | `/api/v1/transactions/flagged` | Get flagged transactions |
| GET | `/api/v1/transactions/high-risk?minScore=60` | Get high risk transactions |
| GET | `/api/v1/transactions/user/{userId}` | Get user transactions |
| GET | `/api/v1/transactions/user/{userId}/summary` | Get user activity summary |

### Search (ElasticSearch powered)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/search/transactions` | Advanced search with filters |
| GET | `/api/v1/search/transactions/flagged` | Search flagged |
| GET | `/api/v1/search/transactions/risk/{level}` | Search by risk level |

### Dashboard

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/dashboard/summary` | System-wide statistics |
| GET | `/api/v1/dashboard/risk-breakdown` | Count by risk level |

---

## 📊 Sample API Calls

### Create Transaction
```bash
curl -X POST http://localhost:8080/api/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user-001",
    "amount": 15000.00,
    "currency": "USD",
    "merchant": "Unknown Vendor",
    "location": "Iran"
  }'
```

### Advanced Search
```bash
curl -X POST http://localhost:8080/api/v1/search/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "flagged": true,
    "minAmount": 5000,
    "riskLevel": "HIGH"
  }'
```

### Dashboard Summary
```bash
curl http://localhost:8080/api/v1/dashboard/summary
```

---

## ⚙️ Configuration

Key settings in `application.yml`:
```yaml
app:
  compliance:
    rules:
      high-amount-threshold: 10000.00   # Flag transactions above this
      velocity-max-transactions: 5       # Max transactions per window
      velocity-window-minutes: 10        # Time window in minutes
      suspicious-locations:
        - "North Korea"
        - "Iran"
        - "Syria"
  generator:
    rate-ms: 5000                        # Auto-generate every 5 seconds
```

---

## 🧪 Risk Scoring

| Rule | Score Weight | Trigger |
|------|-------------|---------|
| High Amount | +40 | amount > $10,000 |
| Velocity | +35 | 5+ transactions in 10 mins |
| Suspicious Location | +25 | Known high-risk country |

| Total Score | Risk Level |
|-------------|-----------|
| 0 – 30 | LOW |
| 31 – 60 | MEDIUM |
| 61 – 85 | HIGH |
| 86 – 100 | CRITICAL |

---

## 🏥 Health Check
```bash
curl http://localhost:8080/actuator/health
```

---

## 👨‍💻 Author

**Sanjeev**  
[GitHub](https://github.com/SJ1975) •
[LinkedIn](https://www.linkedin.com/in/sanjeevk1964/)