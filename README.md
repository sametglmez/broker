# Broker Project

## About the Project

The Broker project is a system where users can manage their assets and perform order operations. Users can view their assets, create new orders, cancel existing orders, and users with the **ADMIN** role can match orders.

The project is built using **Java 21** and **Spring Boot 3.x**. Repository-based CRUD operations are handled with **Spring Data JPA**, entity-to-DTO conversions are handled using a **Converter pattern**, and order logic is abstracted using the **Strategy pattern**.

## Technologies and Structures Used

* **Spring Boot 3.x:** Backend framework
* **Spring Data JPA:** Repository and entity management
* **H2/PostgreSQL:** Database options
* **Lombok:** Getter, Setter, Builder, Constructor
* **JWT:** Authentication and authorization
* **Strategy Pattern:** Separate logic for BUY and SELL orders
* **Transactional + PESSIMISTIC_WRITE:** Data consistency and concurrent access control
* **Aspect (AOP):**

    * Role-based access control
    * Method entry-exit logging
* **Interceptor + MDC:** CorrelationId for request tracking and log monitoring
* **DTO + Converter:** Separation of entity and API models
* **Unit Tests:** Service and strategy classes are tested

## Running the Project

### Prerequisites

1. Install **Java 21 JDK**
2. Install **Maven**

### Database Configuration

* Edit the database information in `application.properties`
* H2 database can be used for development

The project uses an in-memory H2 database for simplicity and fast startup.
Each time the application starts, the following SQL scripts are automatically executed by Spring Boot:

schema.sql → creates database tables

data.sql → inserts initial sample data (roles, customers, assets, and orders)

This means the database is reset and recreated every time the project restarts.
All data is stored in memory and will be lost once the application stops.

To view the in-memory database:

Open the H2 Console at: http://localhost:8081/h2-console

Use the following credentials (as configured in application.properties):

### Build and Run

```bash
mvn clean install
mvn spring-boot:run
```

### Running in IntelliJ IDEA

1. Open the project: File → Open → Select project folder
2. Maven dependencies: Open Maven panel → Right-click project → Reimport
3. Set Java SDK: File → Project Structure → Project → Project SDK: Java 21
4. Verify database configuration in `application.properties`
5. Run the project: Right-click `BrokerApplication` → Run 'BrokerApplication.main()'

### Running Without IntelliJ

1. Open terminal / PowerShell
2. Navigate to project directory
3. Run `mvn clean install`
4. Run `mvn spring-boot:run`
5. Access endpoints at `http://localhost:8081`

## API Endpoints

* `/auth/register` → User registration
* `/auth/login` → User login and JWT token
* `/broker/assets` → List user assets (**CUSTOMER**)
* `/broker/orders` → Create, list, and cancel orders
* `/orders/match-order/**` → Only **ADMIN** can match orders
* JWT token is required for authentication
* Role-based access control is handled via Aspect

## Transaction and Lock Management

* Method-level transaction management is handled with `@Transactional`
* `PESSIMISTIC_WRITE` lock ensures that multiple operations cannot modify the same data simultaneously
* One operation waits while another works on the same data
* Prevents race conditions and data inconsistency
* Works in multi-pod or cluster environments at database level

## Strategy Pattern Usage

* Order operations are abstracted with `OrderStrategy` interface:

    * **BuyOrderStrategy**

        * `createOrder(OrderDto, Customer)`
        * `cancelOrder(Order, Customer)`
        * `matchOrder(Order, Customer)`
    * **SellOrderStrategy**

        * `createOrder(OrderDto, Customer)`
        * `cancelOrder(Order, Customer)`
        * `matchOrder(Order, Customer)`
* Advantages:

    * Adding new order types without breaking existing code
    * Separate logic for BUY and SELL operations

## Role-Based Access Control

* Aspect-based control checks roles before method execution
* Example:

```java
@CheckRoleAccess(customerIdParam = "customerId")
public OrderDto matchOrder(Long orderId) { ... }
```

* Users without the required role are blocked with a `CustomException`

## Logging and Monitoring

* Method entry-exit logs are automatically handled via Aspect
* Logs include:

    * Method name
    * Parameters
    * Return value
    * Errors
* Interceptor + MDC adds `correlationId` to each request for tracking

### Example Log

```
[correlationId=abc123] Entering createOrder(OrderDto{...})
[correlationId=abc123] Exiting createOrder with result OrderDto{id=1,...}
```

Sensitive Data Handling

Some fields such as passwords, tokens, or other confidential information are annotated with @SensitiveData.

The aspect now masks these fields in logs while keeping the original service response intact.

Example:

public class UserDto {
private String username;
@SensitiveData
private String password;
}


Log Output:

> REQUEST -> UserService.createUser() | Parameters: [UserDto{username='admin', password='***MASKED***'}]
<<< RESPONSE <- UserService.createUser() | Returned: UserDto{id=1, username='admin', password='***MASKED***'}


Key Points:

Original objects are not modified; only the log output is masked.

Works for single DTOs, arrays, collections, and can be extended to nested objects.

Ensures sensitive information never appears in logs while allowing monitoring and debugging.

## Example Flow Scenarios

1. **User Registration and Login**

```
[USER] --POST /auth/register--> [Broker API] --> Create User + Role
[USER] --POST /auth/login--> [Broker API] --> JWT Token
```

2. **List Assets**

```
[USER] --GET /broker/assets--> [AssetServiceImpl.getAssetsByCustomerId]
[AssetRepository.findByCustomer] --> DB --> List<AssetDto>
```

3. **Create Order (BUY / SELL)**

```
[USER] --POST /broker/orders--> [OrderServiceImpl.createOrder]
--> RoleValidator / OrderStrategy
--> AssetRepository (PESSIMISTIC_WRITE lock)
--> OrderRepository.save
--> Return OrderDto
```

4. **Cancel Order**

```
[USER] --POST /broker/orders/cancel/{orderId}--> [OrderServiceImpl.cancelOrder]
--> Check OrderStatus == PENDING
--> Buy/Sell Strategy -> Refund / Restore usableSize
--> OrderRepository.save
```

5. **Match Order (ADMIN)**

```
[ADMIN] --POST /orders/match-order/{orderId}--> [OrderServiceImpl.matchOrder]
--> Buy/Sell Strategy -> Asset and TRY update
--> OrderRepository.save(status=MATCHED)
--> Return OrderDto
```

## Entity Relationships

* Customer 1 <---> * Asset
* Customer 1 <---> * Order
* User * <---> 1 Role
* User * <---> 0..1 Customer
* Order -> Customer (ManyToOne)
* Asset -> Customer (ManyToOne)
* Customers own assets and orders
* Users represent logged-in individuals and are linked to roles
* Orders and Assets are managed via Customer

## Unit Tests

* Service and Strategy classes are tested
* Mocked repositories validate different scenarios

## Notes

* `PESSIMISTIC_WRITE` lock prevents race conditions and data inconsistency
* Aspect + MDC + Interceptor enables role control, logging, and monitoring
* Adding new order types or asset operations is simple due to Strategy and DTO design
