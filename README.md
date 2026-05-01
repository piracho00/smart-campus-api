# Smart Campus Sensor & Room Management API

**Module:** 5COSC022W — Client-Server Architectures  
**University of Westminster | 2025–26**

A RESTful web service built with JAX-RS (Jersey) and deployed on Apache Tomcat, providing a backend for the university's Smart Campus initiative. The API manages campus rooms, IoT sensors, and sensor reading history through a clean resource hierarchy with proper HTTP semantics, structured error handling, and request/response logging.

---

## Contents

- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [How to Build and Run](#how-to-build-and-run)
- [API Base URL](#api-base-url)
- [Endpoints](#endpoints)
- [curl Examples](#curl-examples)
- [Error Handling](#error-handling)
- [Logging](#logging)
- [Report: Question Answers](#report-question-answers)

---

## Technology Stack

- **Java 11**
- **JAX-RS 2.1.1** (`javax.ws.rs`) — REST framework specification
- **Jersey 2.39.1** — JAX-RS reference implementation
- **Jackson 2.15.2** — JSON serialisation via `jersey-media-json-jackson`
- **Apache Maven** — build and dependency management
- **Apache Tomcat 9.x** — servlet container (WAR deployment)
- **`ConcurrentHashMap` / `ArrayList`** — all state held in memory; no database

> Spring Boot is **not used**. No SQL database or ORM is used. This submission complies fully with the technology constraints in the specification.

---

## Project Structure

```
smart-campus-api/
├── pom.xml
└── src/
    └── main/
        ├── java/com/westminster/smartcampus/
        │   │
        │   ├── config/
        │   │   └── ApplicationConfig.java          # extends Application, @ApplicationPath("/api/v1")
        │   │
        │   ├── resource/
        │   │   ├── DiscoveryResource.java           # GET /api/v1/  →  HATEOAS metadata
        │   │   ├── RoomResource.java                # /rooms  →  CRUD + deletion safety
        │   │   ├── SensorResource.java              # /sensors  →  CRUD, filtering, sub-resource locator
        │   │   └── SensorReadingResource.java       # /sensors/{id}/readings  →  history + side-effect
        │   │
        │   ├── service/
        │   │   ├── RoomService.java                 # room validation, orphan-prevention logic
        │   │   ├── SensorService.java               # sensor validation, roomId integrity check
        │   │   └── SensorReadingService.java        # reading logic, currentValue sync
        │   │
        │   ├── datastore/
        │   │   ├── RoomStore.java                   # ConcurrentHashMap<String, Room>
        │   │   ├── SensorStore.java                 # ConcurrentHashMap<String, Sensor>
        │   │   └── SensorReadingStore.java          # ConcurrentHashMap<String, List<SensorReading>>
        │   │
        │   ├── model/
        │   │   ├── Room.java                        # id, name, capacity, sensorIds
        │   │   ├── Sensor.java                      # id, type, status, currentValue, roomId
        │   │   └── SensorReading.java               # id, sensorId, timestamp, value
        │   │
        │   ├── dto/
        │   │   ├── ApiInfoResponse.java             # shape of the discovery response
        │   │   └── ErrorResponse.java               # unified error shape: status, error, message, timestamp
        │   │
        │   ├── exception/
        │   │   ├── RoomNotFoundException.java
        │   │   ├── RoomNotEmptyException.java       # → 409 when deleting a room that has sensors
        │   │   ├── SensorNotFoundException.java
        │   │   ├── SensorUnavailableException.java  # → 403 when posting to MAINTENANCE/OFFLINE sensor
        │   │   ├── LinkedResourceNotFoundException.java  # → 422 when sensor's roomId does not exist
        │   │   └── ValidationException.java         # → 400 on missing/invalid fields
        │   │
        │   ├── mapper/
        │   │   ├── RoomNotEmptyExceptionMapper.java
        │   │   ├── LinkedResourceNotFoundExceptionMapper.java
        │   │   ├── SensorUnavailableExceptionMapper.java
        │   │   ├── RoomNotFoundExceptionMapper.java
        │   │   ├── SensorNotFoundExceptionMapper.java
        │   │   ├── ValidationExceptionMapper.java
        │   │   ├── ProcessingExceptionMapper.java   # handles malformed JSON body → 400
        │   │   └── GlobalExceptionMapper.java       # catches Throwable → 500, no stack trace leaked
        │   │
        │   ├── filter/
        │   │   └── LoggingFilter.java               # ContainerRequestFilter + ContainerResponseFilter
        │   │
        │   └── util/
        │       ├── IdGenerator.java                 # UUID.randomUUID()
        │       ├── StatusConstants.java             # ACTIVE / MAINTENANCE / OFFLINE
        │       └── TimeUtil.java                    # System.currentTimeMillis() helper
        │
        ├── resources/
        │   └── META-INF/persistence.xml
        └── webapp/
            └── WEB-INF/
                ├── web.xml                          # Jersey ServletContainer mapping
                └── beans.xml
```

**Why this structure:**  
Resources handle HTTP routing only — no business logic sits there. Services own all validation, constraint enforcement, and cross-entity updates. Datastores hold the `ConcurrentHashMap` instances that serve as in-memory persistence. DTOs (`ErrorResponse`, `ApiInfoResponse`) decouple the API contract from domain models and guarantee a consistent JSON shape across all responses. Mappers and filters are isolated cross-cutting concerns that never touch business logic.

---

## How to Build and Run

**Requirements:** JDK 11+, Maven 3.6+, Apache Tomcat 9.x

**1. Clone the repository**
```bash
git clone https://github.com/<your-username>/smart-campus-api.git
cd smart-campus-api
```

**2. Build**
```bash
mvn clean package
```
This produces `target/smartcampus.war`.

**3. Deploy to Tomcat**
```bash
cp target/smartcampus.war /path/to/tomcat/webapps/
/path/to/tomcat/bin/startup.sh        # Linux / macOS
/path/to/tomcat/bin/startup.bat       # Windows
```

**4. Verify**
```bash
curl http://localhost:8080/smartcampus/api/v1/
```
You should receive a JSON object with version info and HATEOAS navigation links.

**5. Stop the server**
```bash
/path/to/tomcat/bin/shutdown.sh
```

> **NetBeans:** Right-click project → Run. Tomcat is started and the WAR is deployed automatically.

---

## API Base URL

```
http://localhost:8080/smartcampus/api/v1
```

---

## Endpoints

### Discovery

| Method | Path | Description | Status |
|--------|------|-------------|--------|
| GET | `/` | API metadata + HATEOAS navigation links | 200 |

### Rooms

| Method | Path | Description | Status |
|--------|------|-------------|--------|
| GET | `/rooms` | List all rooms | 200 |
| POST | `/rooms` | Create a room | 201 + Location header |
| GET | `/rooms/{roomId}` | Get a single room | 200 |
| DELETE | `/rooms/{roomId}` | Delete room (blocked if sensors assigned) | 204 |

### Sensors

| Method | Path | Description | Status |
|--------|------|-------------|--------|
| GET | `/sensors` | List all sensors; optional `?type=` filter | 200 |
| POST | `/sensors` | Register a sensor (validates roomId exists) | 201 + Location header |
| GET | `/sensors/{sensorId}` | Get a single sensor | 200 |

### Readings (sub-resource)

| Method | Path | Description | Status |
|--------|------|-------------|--------|
| GET | `/sensors/{sensorId}/readings` | Reading history for a sensor | 200 |
| POST | `/sensors/{sensorId}/readings` | Add a reading; updates sensor's currentValue | 201 + Location header |

### Error codes

| Code | When it is returned |
|------|---------------------|
| 400 | Missing or invalid fields in the request body |
| 403 | Reading posted to a MAINTENANCE or OFFLINE sensor |
| 404 | Requested resource does not exist |
| 409 | DELETE attempted on a room that still has sensors |
| 422 | Sensor registered with a roomId that does not exist |
| 500 | Any unexpected runtime error (no stack trace in response) |

---

## curl Examples

Replace `<ROOM_ID>` and `<SENSOR_ID>` with the IDs returned by the create calls.

**1 — Discovery**
```bash
curl -s http://localhost:8080/smartcampus/api/v1/
```

**2 — Create a room**
```bash
curl -s -X POST http://localhost:8080/smartcampus/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"name": "Library Quiet Study", "capacity": 50}'
```

**3 — Register a CO2 sensor**
```bash
curl -s -X POST http://localhost:8080/smartcampus/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"type": "CO2", "status": "ACTIVE", "currentValue": 0.0, "roomId": "<ROOM_ID>"}'
```

**4 — Filter sensors by type**
```bash
curl -s "http://localhost:8080/smartcampus/api/v1/sensors?type=CO2"
```

**5 — Post a sensor reading**
```bash
curl -s -X POST http://localhost:8080/smartcampus/api/v1/sensors/<SENSOR_ID>/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 850.5}'
```

**6 — Attempt to delete a room that has sensors (expect 409)**
```bash
curl -s -X DELETE http://localhost:8080/smartcampus/api/v1/rooms/<ROOM_ID>
```

**7 — Register a sensor with a non-existent roomId (expect 422)**
```bash
curl -s -X POST http://localhost:8080/smartcampus/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"type": "Temperature", "roomId": "DOES-NOT-EXIST"}'
```

**8 — Post a reading to a MAINTENANCE sensor (expect 403)**
```bash
curl -s -X POST http://localhost:8080/smartcampus/api/v1/sensors/<MAINTENANCE_SENSOR_ID>/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 22.5}'
```

---

## Error Handling

Every error — regardless of where it originates — returns the same JSON shape:

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Cannot delete room '...'. It still has 1 sensor(s) assigned.",
  "timestamp": 1714123456789
}
```

This is enforced by the `ErrorResponse` DTO, which every `ExceptionMapper` uses to build its response. No raw Java stack trace, no default Tomcat HTML error page, and no inconsistent response body will reach a consumer.

| Exception | Mapper | HTTP status |
|-----------|--------|-------------|
| `RoomNotEmptyException` | `RoomNotEmptyExceptionMapper` | 409 Conflict |
| `LinkedResourceNotFoundException` | `LinkedResourceNotFoundExceptionMapper` | 422 Unprocessable Entity |
| `SensorUnavailableException` | `SensorUnavailableExceptionMapper` | 403 Forbidden |
| `RoomNotFoundException` | `RoomNotFoundExceptionMapper` | 404 Not Found |
| `SensorNotFoundException` | `SensorNotFoundExceptionMapper` | 404 Not Found |
| `ValidationException` | `ValidationExceptionMapper` | 400 Bad Request |
| `ProcessingException` | `ProcessingExceptionMapper` | 400 Bad Request |
| `Throwable` | `GlobalExceptionMapper` | 500 Internal Server Error |

`GlobalExceptionMapper` maps `Throwable` — the widest possible net. It contains a guard: if the exception is already a `WebApplicationException`, the existing JAX-RS response is passed through unchanged so intentional HTTP codes are never overridden. Any other unhandled exception is logged with its full stack trace server-side (for debugging), while only the generic 500 message is returned to the caller.

---

## Logging

`LoggingFilter` implements both `ContainerRequestFilter` and `ContainerResponseFilter` and is registered automatically via `@Provider`. It logs every request on the way in and every response on the way out, using `java.util.logging.Logger`:

```
[REQUEST]  Method=POST  URI=http://localhost:8080/smartcampus/api/v1/sensors
[RESPONSE] Method=POST  URI=http://localhost:8080/smartcampus/api/v1/sensors  Status=201
```

---

## Report: Question Answers

---

### Part 1.1 — JAX-RS Resource Lifecycle and Thread Safety

By default, the JAX-RS specification mandates that **a new instance of each resource class is created for every incoming HTTP request** (per-request scope). This design exists because resource instances carry request-specific context injected by the container — `@Context UriInfo`, `@PathParam`, `@QueryParam` — that is only valid for the duration of one request. If a single shared instance were used across concurrent requests, these fields would be overwritten by each thread, causing data corruption and unpredictable behaviour.

The direct consequence for in-memory state management is that **resource instances must never hold application data**. If the rooms collection were stored as an instance field on `RoomResource`, each new request would get a fresh object with an empty map, and every write would be silently discarded at the end of the request. This implementation solves the problem through two complementary decisions.

First, all state is delegated to **singleton service and datastore objects** managed explicitly with the static factory pattern (`RoomService.getInstance()`, `RoomStore.getInstance()`). These objects are created once when the class is first loaded and live for the entire lifetime of the application. All requests — regardless of how many `RoomResource` instances JAX-RS creates — share the same service and store instances, and therefore the same underlying data.

Second, all three datastores back their state with **`ConcurrentHashMap`** rather than a plain `HashMap` or a `Collections.synchronizedMap` wrapper. `ConcurrentHashMap` uses internal lock striping, which means individual buckets are locked independently rather than the whole map. This allows multiple threads to read and write concurrently without contention, while still providing atomic guarantees on individual operations (`get`, `put`, `remove`, `computeIfAbsent`). The `SensorReadingStore` in particular uses `computeIfAbsent` to initialise a sensor's reading list atomically on first write, eliminating the classic check-then-act race condition that would occur with a separate `containsKey` + `put` sequence.

---

### Part 1.2 — Why HATEOAS is a Hallmark of Advanced RESTful Design

HATEOAS (Hypermedia as the Engine of Application State) is the constraint, articulated by Roy Fielding in his REST dissertation, that API responses should embed links describing what actions and resources are reachable from the current state. The server drives navigation; the client does not need to know URLs in advance.

The discovery endpoint at `GET /api/v1/` demonstrates this directly. `DiscoveryResource` builds a `links` map using the live base URI from `UriInfo.getBaseUri()` and returns it as part of every response. A client that has only the root URL can discover `/rooms` and `/sensors` from the response itself without consulting any external document.

The practical benefits compared to static documentation are significant. When an API is versioned, restructured, or deployed to a different host, clients that follow embedded links automatically receive the correct URLs — they are never hard-coded against a fixed document. Static documentation, by contrast, becomes stale the moment the deployment changes and every consumer must be notified and must update. HATEOAS also enables runtime context-awareness: a room that cannot be deleted because it has sensors could include a `canDelete: false` field or omit the delete link entirely, giving the client actionable state information before it attempts an operation. This kind of dynamic signalling is impossible with a static API reference.

---

### Part 2.1 — Returning IDs vs. Full Objects from a Collection Endpoint

`GET /api/v1/rooms` returns the full `Room` object for every room in the collection. The alternative — returning only IDs — creates the N+1 problem: a client that wants to display a list of rooms must first call `GET /rooms` to obtain IDs, then issue a separate `GET /rooms/{id}` for every room it wants to display. Under network latency, N sequential round trips are far more expensive than one slightly larger payload, regardless of the individual object size.

For a campus room registry, the payload concern does not apply. A room object is small (four fields: `id`, `name`, `capacity`, `sensorIds`), and the number of rooms on a university campus is bounded. The bandwidth difference between an array of IDs and an array of full objects is negligible. Returning full objects means clients — whether a facilities dashboard or an automated building system — can render a complete room list from a single request.

Returning only IDs is the right trade-off when the collection is very large (millions of items) and most clients only need a small subset at a time, in which case pagination combined with a summary projection (`?fields=id,name`) gives better control. For this use case, full objects are the correct default.

---

### Part 2.2 — DELETE Idempotency

The HTTP specification (RFC 9110) defines an idempotent method as one where the **server state** resulting from multiple identical requests is the same as from a single request. The key word is server state, not the HTTP response code.

In this implementation, the first `DELETE /rooms/{id}` on an existing empty room removes the resource and returns `204 No Content`. The second identical request finds no resource, throws `RoomNotFoundException`, and returns `404 Not Found`. The response codes differ, but the server state is identical after both calls: the room does not exist. DELETE is therefore idempotent by the specification's definition.

Returning `404` rather than a second `204` is intentional and correct. Returning `204` on a repeat deletion would actively mislead the client into believing it had deleted something. `404` is an honest signal: "the resource is not here, which is consistent with your delete having succeeded." Clients implementing retry logic — for example after a network timeout — should treat `404` on a DELETE as confirmation of success rather than a failure condition.

---

### Part 3.1 — Consequences of a Content-Type Mismatch with @Consumes

The `POST /sensors` method is annotated `@Consumes(MediaType.APPLICATION_JSON)`. This annotation participates in JAX-RS content negotiation at the routing stage.

When a request arrives, the Jersey runtime reads the `Content-Type` header and compares it against the `@Consumes` declarations of all candidate resource methods for the matched path. If no method accepts the provided content type, Jersey rejects the request with **`415 Unsupported Media Type`** before the method body is ever reached. This is a framework-level decision, not application code. The resource method is never invoked.

If the `Content-Type` header is correctly set to `application/json` but the body is syntactically invalid JSON, the Jackson deserialiser throws a `JsonMappingException` wrapped in a JAX-RS `ProcessingException`. The `ProcessingExceptionMapper` in this project intercepts that and returns a structured `400 Bad Request` with an explanatory message, rather than letting it bubble up as an unformatted 500 error.

This two-layer defence — `@Consumes` rejecting wrong content types at the routing stage, and `ProcessingExceptionMapper` handling malformed bodies — means the API never processes garbage input and always responds with a structured, informative error.

---

### Part 3.2 — @QueryParam vs. Path Parameter for Collection Filtering

`GET /sensors?type=CO2` uses a query parameter. The alternative path design would be something like `GET /sensors/type/CO2`.

Path parameters denote **identity** — they locate a specific resource within a hierarchy. `/sensors/{sensorId}` works because `sensorId` uniquely identifies a resource. `/sensors/type/CO2` is a category query masquerading as a resource address. It implies that `CO2` is a child resource of `/sensors/type`, which does not reflect the actual data model and pollutes the URL namespace with non-resource segments.

Query parameters express **intent to modify the processing of a request** — filtering, sorting, pagination. `?type=CO2` correctly signals "give me the sensors collection, but filtered to CO2 type." The parameter is optional by definition: omitting it (`GET /sensors`) returns the full collection, handled by the same method with a null check. With a path parameter, that use case requires a separate route.

Query parameters also compose cleanly. A future requirement to filter by both type and status — `?type=CO2&status=ACTIVE` — requires no routing changes, just an additional `@QueryParam` in the method signature. The equivalent path design (`/sensors/type/CO2/status/ACTIVE`) is non-standard, fragile, and incompatible with HTTP caching conventions, which treat the path as the resource identity and query strings as the modifiable part.

---

### Part 4.1 — Sub-Resource Locator Pattern: Benefits for API Complexity Management

The sub-resource locator in `SensorResource` is the method:

```java
@Path("/{sensorId}/readings")
public SensorReadingResource getReadingsResource(@PathParam("sensorId") String sensorId) {
    return new SensorReadingResource(sensorId, uriInfo);
}
```

There is no HTTP verb annotation on this method. JAX-RS recognises it as a locator: when a request arrives for any path under `/{sensorId}/readings`, the runtime calls this method, receives the returned object, and continues dispatching against that object's annotated methods. `SensorReadingResource` then handles `GET` (reading history) and `POST` (new reading) within its own class.

The architectural benefit over placing all handlers in one class is separation of concerns at the class level. Everything related to reading management — validating that the parent sensor exists, checking whether the sensor is in an acceptable state to receive a reading, persisting the reading, and updating the parent sensor's `currentValue` — lives inside `SensorReadingResource` and `SensorReadingService`. Changes to that logic require touching only those two classes. `SensorResource` does not know or care how readings are stored or validated.

In a large API, the alternative — a single resource class with methods for sensors, readings, and all nested resources — grows into an unmanageable file. Every developer working on readings touches the same class as every developer working on sensor registration, causing merge conflicts and increasing the risk of regressions. The locator pattern gives each sub-domain its own class, making the codebase proportional to the domain size and far easier to navigate, extend, and test.

`SensorReadingResource` also benefits from receiving `sensorId` as a constructor argument rather than extracting it via `@PathParam` in every method. The class is constructed with its context already set, which is also why `UriInfo` is passed from the parent resource: objects instantiated with `new` outside the JAX-RS container do not receive `@Context` injections automatically.

---

### Part 5.1 — Why HTTP 422 is More Semantically Correct Than 404 for a Missing Referenced Resource

When `POST /sensors` is called with a valid JSON body whose `roomId` field references a room that does not exist, the request reached the right endpoint, the JSON was valid, and the server fully understood the payload. The problem is a **semantic integrity violation inside the payload** — a referential dependency that cannot be satisfied.

`404 Not Found` means the server cannot locate the target of the HTTP request — the URL itself was not found. Returning 404 here would imply that `/sensors` does not exist, which is wrong and actively misleads the client.

`422 Unprocessable Entity` (defined in RFC 4918) means the server received and understood the request but was unable to fulfil it because the instructions within the body are semantically erroneous. The JSON is well-formed and the endpoint exists, but the data references something the server cannot resolve. This maps precisely to the scenario: the body is syntactically valid, but the embedded `roomId` is a dangling reference.

The practical difference matters for client developers. A `404` sends them looking at their URL. A `422` with the message "roomId 'X' does not exist" sends them directly to the root cause: they need to create the room first. The `422` is actionable; the `404` is misleading. This mirrors the behaviour of a relational database, which distinguishes between a query that targets a missing table (analogous to 404) and a row whose foreign key constraint fails (analogous to 422).

---

### Part 5.2 — Security Risks of Exposing Stack Traces to API Consumers

A raw Java stack trace in an API response is an information disclosure vulnerability. An attacker who receives one gains several categories of sensitive information.

**Technology fingerprinting.** Package names in the trace — `org.glassfish.jersey`, `com.fasterxml.jackson` — reveal the exact framework and version. The attacker cross-references those versions against public CVE databases and crafts exploits targeting known vulnerabilities in those specific releases.

**Internal architecture mapping.** Class names and method signatures reveal the internal structure of the application: which classes handle routing, which handle data access, which perform validation. An attacker builds a map of the codebase from the call stack alone.

**Injection point identification.** A `NullPointerException` trace pointing to a specific line in `SensorReadingService.java` tells the attacker exactly which input combination triggered a null dereference. They use this to probe for additional crash points, craft denial-of-service payloads, or identify code paths that might bypass validation.

**File system disclosure.** Absolute paths in the trace reveal the deployment directory structure and operating system, which aids privilege escalation if a separate vulnerability is found.

**Data schema leakage.** In applications that interact with a database, traces from persistence layer exceptions can contain table names, column names, and fragments of SQL queries that expose the schema to an attacker who has not been granted any access.

`GlobalExceptionMapper<Throwable>` eliminates all of these risks by catching every unhandled exception, logging the full stack trace server-side where it is needed for debugging, and returning only `{ "status": 500, "error": "Internal Server Error", "message": "An unexpected error occurred." }` to the caller. The internal state of the application is never transmitted to an external consumer.

---

### Part 5.3 — Why Filters Are the Correct Approach for Cross-Cutting Concerns Like Logging

`LoggingFilter` implements `ContainerRequestFilter` and `ContainerResponseFilter` and is registered globally via `@Provider`. Every request and response passes through it automatically, with no changes required to any resource method.

The alternative is inserting `Logger.info(...)` calls at the start and end of each resource method. That approach has several fundamental problems.

It violates the Single Responsibility Principle. A method responsible for creating a room should not also be responsible for generating log output. Mixing those concerns makes the method harder to read and harder to reason about independently.

It produces an incomplete picture. JAX-RS rejects requests at the framework level before resource methods are called — a `415 Unsupported Media Type` rejection, for example, never reaches any resource method. Manual logging in method bodies would produce no log entry for those rejections, creating blind spots in observability precisely where they are most needed.

It does not scale. Adding a new endpoint means remembering to add logging. Changing the log format means touching every method in every resource class. A filter centralises both decisions: add a new endpoint and it is automatically logged; change the format and it changes everywhere at once.

Filters also have access to the final response status in a way that resource method code does not. The response filter receives the actual `ContainerResponseContext` after the entire processing pipeline — including exception mappers — has run. A resource method that intends to return `201` but causes an exception that a mapper converts to `409` would log the wrong status if logging were done inside the method. The filter always logs what was actually sent.
