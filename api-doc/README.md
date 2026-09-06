# API Documentation

## How to Import in Postman

1. Open Postman
2. Click **Import** button
3. Select `compliance-monitor.postman_collection.json`
4. All APIs load automatically

## APIs Overview

### Transactions (10 APIs)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/transactions` | Create transaction |
| GET | `/api/v1/transactions` | Get all |
| GET | `/api/v1/transactions/{id}` | Get by ID |
| GET | `/api/v1/transactions/flagged` | Flagged only |
| GET | `/api/v1/transactions/high-risk` | High risk |
| GET | `/api/v1/transactions/user/{id}` | By user |
| GET | `/api/v1/transactions/user/{id}/summary` | User summary |

### Search APIs - ElasticSearch Powered (5 APIs)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/search/transactions` | Advanced search |
| GET | `/api/v1/search/transactions/flagged` | Flagged |
| GET | `/api/v1/search/transactions/risk/{level}` | By risk |
| GET | `/api/v1/search/transactions/user/{id}` | By user |

### Dashboard & Analytics APIs (5 APIs)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/dashboard/summary` | Full stats |
| GET | `/api/v1/dashboard/risk-breakdown` | Risk counts |
| GET | `/api/v1/dashboard/trends/daily` | Daily trends |
| GET | `/api/v1/dashboard/trends/hourly` | Hourly patterns |
| GET | `/api/v1/dashboard/locations` | Location analytics |

### Health (1 API)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/actuator/health` | System health |

## Total: 21 APIs