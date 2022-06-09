# Lab for Learning Kotlin

- Event Driven Design
- Functional Kotlin with Arrow
- Kafka + Mongo DB
- Observability with Prometheus + Grafana

## How to Run

```bash
docker-compose up
```

Go to http://localhost:3000 for Grafana. User: admin - Password: admin.

Generate load
```bash
cd loadtest

# start generating a low load
yarn run low

# start generating a medium load
yarn run medium

# start generating a high load
yarn run high
```
