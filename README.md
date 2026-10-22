# Microservices Project (Java 21 + Spring Boot + Spring Cloud)

## 1. Architecture

```text
Client / Postman
      |
      v
API Gateway (8080) --- asks ---> Eureka Server (8761)  <--- services register here
      |
      |-- /api/users/**  --> lb://USER-SERVICE  --> User Service  (8081) --> PostgreSQL  user_db
      |-- /api/orders/** --> lb://ORDER-SERVICE --> Order Service (8082) --> PostgreSQL  order_db
```

| Project         | Port | Eureka name     | Database   |
|-----------------|------|-----------------|------------|
| eureka-server   | 8761 | (the registry)  | -          |
| api-gateway     | 8080 | API-GATEWAY     | -          |
| user-service    | 8081 | USER-SERVICE    | `user_db`  |
| order-service   | 8082 | ORDER-SERVICE   | `order_db` |

Versions: Java 21, Spring Boot 3.5.6, Spring Cloud 2025.0.0 (the release train that matches Boot 3.5.x).
The Gateway uses `spring-cloud-starter-gateway-server-webflux` and the `spring.cloud.gateway.server.webflux.routes` property, which are the names used by Spring Cloud 2025.0.x.

## 2. Folder structure

```text
microservices-project/
├── eureka-server/   (pom.xml, EurekaServerApplication, application.yml)
├── api-gateway/     (pom.xml, ApiGatewayApplication, application.yml)
├── user-service/    (entity, repository, service, service.impl, controller, exception)
├── order-service/   (entity, repository, service, service.impl, controller, exception)
└── README.md
```

Each folder is a separate Maven/Spring Boot project with its own `pom.xml`.

## 3. Prerequisites

- JDK 21
- Maven 3.9+ (or use IntelliJ's bundled Maven)
- PostgreSQL running on `localhost:5432`
- IntelliJ IDEA and Postman

## 4. Create the databases

Using `psql`:

```bash
psql -U postgres -c "CREATE DATABASE user_db;"
psql -U postgres -c "CREATE DATABASE order_db;"
```

Tables (`users`, `orders`) are created automatically on first start (`ddl-auto: update`).

## 5. Configure the database password

The password is NOT stored in the code. Both services read these environment variables:

| Variable      | Default     | Required |
|---------------|-------------|----------|
| `DB_PASSWORD` | none        | **yes**  |
| `DB_USERNAME` | `postgres`  | no       |
| `DB_HOST`     | `localhost` | no       |
| `DB_PORT`     | `5432`      | no       |

**In IntelliJ (recommended):** Run | Edit Configurations → pick `UserServiceApplication` → *Modify options* → *Environment variables* → add `DB_PASSWORD=your_password`. Repeat for `OrderServiceApplication`.

**In a terminal:**

- Windows PowerShell: `$env:DB_PASSWORD="your_password"`
- Windows CMD: `set DB_PASSWORD=your_password`
- macOS/Linux: `export DB_PASSWORD=your_password`

## 6. Open in IntelliJ IDEA

1. File → Open → select the `microservices-project` folder.
2. IntelliJ may show "Maven projects need to be imported". Click **Load Maven Project** / **Load All**.
3. If a project is not detected, open the **Maven** tool window (right side) → **+** (Add Maven Projects) → choose each `pom.xml` (all four).
4. File → Project Structure → Project SDK = Java 21.
5. Run each main class using the green arrow next to `main(...)`.

## 7. Build all four projects

Run in each folder (the first build downloads dependencies, so internet is needed):

```bash
cd eureka-server && mvn clean package && cd ..
cd api-gateway   && mvn clean package && cd ..
cd user-service  && mvn clean package && cd ..
cd order-service && mvn clean package && cd ..
```

## 8. Start order (important)

1. **eureka-server** (wait until it is up) → http://localhost:8761
2. **user-service**
3. **order-service**
4. **api-gateway**

Terminal commands (one terminal each, from the project folder):

```bash
mvn spring-boot:run
```

or, after building: `java -jar target/<project-name>-1.0.0.jar`

Wait ~30 seconds after the last service starts. Open http://localhost:8761 and check that `API-GATEWAY`, `USER-SERVICE` and `ORDER-SERVICE` are listed as **UP**.

## 9. Postman requests (always call the Gateway on port 8080)

### Users

**Create user** — `POST http://localhost:8080/api/users`, Body → raw → JSON

```json
{
  "name": "Alice Johnson",
  "email": "alice@example.com"
}
```
Expected: `201 Created` with the saved user (including `id`).

**Get all users** — `GET http://localhost:8080/api/users` → `200 OK`

**Get user by id** — `GET http://localhost:8080/api/users/1` → `200 OK`
Unknown id (e.g. `/api/users/999`) → `404 Not Found` with a JSON message.

### Orders

**Create order** — `POST http://localhost:8080/api/orders`, Body → raw → JSON

```json
{
  "userId": 1,
  "productName": "Laptop",
  "amount": 1299.99
}
```
Expected: `201 Created`.

**Get all orders** — `GET http://localhost:8080/api/orders` → `200 OK`

**Get order by id** — `GET http://localhost:8080/api/orders/1` → `200 OK`
Unknown id → `404 Not Found`.

You can also hit each service directly while debugging: `http://localhost:8081/api/users` and `http://localhost:8082/api/orders`.

## 10. URLs summary

| What               | URL                                   |
|--------------------|---------------------------------------|
| Eureka dashboard   | http://localhost:8761                 |
| API Gateway        | http://localhost:8080                 |
| User Service       | http://localhost:8081/api/users       |
| Order Service      | http://localhost:8082/api/orders      |

## 11. Troubleshooting

- **`Could not resolve placeholder 'DB_PASSWORD'`** → the environment variable is not set for that run configuration/terminal (section 5).
- **`password authentication failed for user "postgres"`** → wrong `DB_PASSWORD` or `DB_USERNAME`.
- **`database "user_db" does not exist`** → create the databases (section 4).
- **Gateway returns 503 "Unable to find instance for USER-SERVICE"** → the service has not registered yet. Check the Eureka dashboard, wait ~30 s, make sure Eureka started first.
- **Gateway returns 404** → check the URL starts with `/api/users` or `/api/orders`, and that you call port 8080.
- **`Port 8080/8081/8082/8761 already in use`** → stop the other process using that port.
- **Eureka shows a red "EMERGENCY! EUREKA MAY BE INCORRECTLY CLAIMING INSTANCES ARE UP" banner** → harmless in local development.
- **Services keep logging "Connection refused: localhost:8761"** → Eureka is not running yet; start it first.
- **IntelliJ shows red imports** → reload Maven (Maven tool window → refresh icon) and confirm the Project SDK is Java 21.
- **`release version 21 not supported`** → Maven is running on an older JDK. Set `JAVA_HOME` to JDK 21 (or choose JDK 21 in Settings → Build Tools → Maven → Runner).
- **Gateway fails to start with unknown property/bean errors** → make sure you have not mixed older Gateway starter names with the 2025.0.x versions used here.
