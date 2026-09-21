# Market Management — Orders & Payments API

![Java 17](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0-green)
![SQLite](https://img.shields.io/badge/SQLite-embedded-lightgrey)
![License MIT](https://img.shields.io/badge/License-MIT-yellow)

> REST API for a small store: products, customers, orders with items, and payments — built with Java 17 + Spring Boot + JPA/Hibernate on an embedded SQLite database, with a minimal static web UI included.

## What it does

- Product catalog with price in cents and active flag
- Order creation with items, quantities and snapshot unit prices
- Order status flow (`NEW` → …) with detailed and summary views
- Payment registration per order (e.g. PIX) with validation — including a negative-path example
- Global error responses via a dedicated exception handler
- Static HTML/JS UI served by the API itself for manual testing

## Tech Stack

Java 17, Spring Boot 4 (Web MVC, Data JPA, Validation), Hibernate with SQLite dialect (`sqlite-jdbc`), H2 console (dev), Maven. Tests with JUnit 5 + Spring Boot Test.

## Run locally

```sh
git clone https://github.com/TadeuN1/marketManagement-project.git
cd marketManagement-project
./mvnw spring-boot:run        # Windows: .\mvnw.cmd spring-boot:run
```

- API at `http://localhost:8080`
- Minimal web UI at `http://localhost:8080/`
- Uses the SQLite file at `db/orders.db` (no server needed). Seed data in `db/seed-data.sql`.

## Try it

```sh
# list products
curl http://localhost:8080/products

# create an order (see examples/order.json)
curl -X POST http://localhost:8080/orders \
  -H 'Content-Type: application/json' \
  -d @examples/order.json

# pay for order 3 with PIX (see examples/payment2.json)
curl -X POST http://localhost:8080/payments \
  -H 'Content-Type: application/json' \
  -d @examples/payment2.json

# invalid payment example (non-existent order)
curl -X POST http://localhost:8080/payments \
  -H 'Content-Type: application/json' \
  -d @examples/payment-bad.json
```

| Method & path       | Description                  |
| ------------------- | ---------------------------- |
| `GET /products`     | List products                |
| `GET /orders`       | List orders (summary)        |
| `GET /orders/{id}`  | Order detail with items      |
| `POST /orders`      | Create order with items      |
| `POST /payments`    | Register payment for an order |

## Project Structure

```
.
├── src/                  # API (controller / dto / entity / repository / service)
│   └── main/resources/static/  # minimal web UI
├── db/
│   ├── orders.db         # SQLite demo database
│   └── seed-data.sql     # seed data (customers, products, order)
├── examples/             # curl-ready request payloads
```

## Roadmap

- [x] Orders, payments and catalog with validation
- [ ] Dockerfile + Compose for one-command run
- [ ] More tests beyond context load

## License

MIT — see [LICENSE](LICENSE).
