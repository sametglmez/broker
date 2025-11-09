# Broker Project – Usage Guide

## 1. Admin Register & Login

1. **Register Admin**

```http
POST /auth/register
Content-Type: application/json

{
  "username": "adminUser",
  "password": "AdminPass123",
  "role": "ADMIN"
}
```

2. **Login Admin**

```http
POST /auth/login
Content-Type: application/json

{
  "username": "adminUser",
  "password": "AdminPass123"
}
```

Response:

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

## 2. Customer Register & Login

1. **Register Customer**

```http
POST /auth/register
Content-Type: application/json

{
  "username": "customerA",
  "password": "CustomerPass123",
  "role": "CUSTOMER",
  "customerId": 101
}
```

2. **Login Customer**

```http
POST /auth/login
Content-Type: application/json

{
  "username": "customerA",
  "password": "CustomerPass123"
}
```

Response:

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

## 3. Customer – List Assets

```http
GET /broker/assets
Authorization: Bearer <JWT_TOKEN>
```

Response:

```json
[
  {
    "assetName": "TRY",
    "size": 10000.00,
    "usableSize": 10000.00
  },
  {
    "assetName": "ASELS",
    "size": 200.00,
    "usableSize": 200.00
  }
]
```

---

## 4. Admin – Create Orders (BUY / SELL)

```http
POST /broker/orders
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "assetName": "ASELS",
  "orderSide": "BUY",
  "size": 10.00,
  "price": 50.00
}
```

Response:

```json
{
  "id": 1,
  "assetName": "ASELS",
  "orderSide": "BUY",
  "size": 10.00,
  "price": 50.00,
  "status": "PENDING"
}
```

---

## 5. Admin – Cancel Order

```http
POST /broker/orders/cancel/1
Authorization: Bearer <JWT_TOKEN>
```

Response:

```json
{
  "id": 1,
  "status": "CANCELED"
}
```

---

## 6. Admin – Match Order

```http
POST /orders/match-order/1
Authorization: Bearer <JWT_TOKEN>
```

Response:

```json
{
  "id": 1,
  "status": "MATCHED",
  "assetName": "ASELS",
  "size": 10.00,
  "price": 50.00
}
```

---

## 7. Example Database Inserts

### Roles

| id | name     |
| -- | -------- |
| 1  | ADMIN    |
| 2  | CUSTOMER |

### Customers

| id  | name       |
| --- | ---------- |
| 101 | Customer A |
| 102 | Customer B |

### Assets

| customer_id | asset_name | size    | usable_size |
| ----------- | ---------- | ------- | ----------- |
| 101         | TRY        | 10000.0 | 10000.0     |
| 101         | ASELS      | 200.0   | 200.0       |
| 102         | TRY        | 5000.0  | 5000.0      |
| 102         | THYAO      | 150.0   | 150.0       |

### Orders

| customer_id | asset_name | order_side | size | price | status  | create_date       |
| ----------- | ---------- | ---------- | ---- | ----- | ------- | ----------------- |
| 101         | ASELS      | BUY        | 10.0 | 50.0  | PENDING | CURRENT_TIMESTAMP |
| 101         | TRY        | SELL       | 1000 | 1.0   | PENDING | CURRENT_TIMESTAMP |
| 102         | THYAO      | BUY        | 5.0  | 100.0 | PENDING | CURRENT_TIMESTAMP |

---

## 8. Important Notes

* **Strategy Pattern**: BUY and SELL order logics are implemented separately. `OrderStrategy` interface allows adding new order types without breaking existing code.
* **Transactional + PESSIMISTIC_WRITE Lock**: Prevents race conditions and ensures data consistency when multiple operations access the same asset.
* **Aspect-Oriented Programming (AOP)**:

    * Role-based access control for methods.
    * Method entry/exit logging automatically.
* **Interceptor + MDC**:

    * Each request gets a `correlationId`.
    * Logs include correlationId for easier monitoring.
* **DTO + Converter**: Separates API layer from entity layer.
* **Unit Tests**: Service and strategy classes are fully tested using mocked repositories.
