# Spring Boot 2 with Keycloak 26

A minimal example showing how to secure a **Spring Boot 2** web application (JSP views) using the **Keycloak Spring Boot adapter** against a **Keycloak 26** server.

The app exposes a public landing page and a protected `/admin/*` area that only authenticated users (any authenticated role) can access.

---

## Tech Stack

| Component            | Version              |
|----------------------|----------------------|
| Java                 | 8                    |
| Spring Boot          | 2.2.6.RELEASE        |
| Keycloak Adapter     | 9.0.2 (spring-boot-starter) |
| Keycloak Server      | 26.x                 |
| View Technology      | JSP + JSTL           |
| Build Tool           | Maven                |

> Note: The Keycloak 9.0.2 adapter still works against a Keycloak 26 server as long as the client is configured with the OIDC protocol.

---

## Project Structure

```
spring-boot-2-and-keycloak-26/
├── pom.xml
└── src/main/
    ├── java/com/edw/
    │   ├── Application.java              # Spring Boot entry point
    │   └── controller/
    │       ├── IndexController.java      # GET /  -> index.jsp (public)
    │       └── AdminController.java      # GET /admin/index, /admin/logout (secured)
    ├── resources/
    │   └── application.properties        # Server + Keycloak configuration
    └── webapp/WEB-INF/jsp/
        ├── index.jsp
        └── admin-index.jsp
```

---

## Endpoints

| Method | Path            | Access        | Description                          |
|--------|-----------------|---------------|--------------------------------------|
| GET    | `/`             | Public        | Landing page                         |
| GET    | `/admin/index`  | Authenticated | Protected admin page                 |
| GET    | `/admin/logout` | Authenticated | Logs out locally and via Keycloak OIDC end-session, then redirects to `/` |

Security rule (see `application.properties`):

```properties
keycloak.security-constraints[0].securityCollections[0].patterns[0]=/admin/*
keycloak.security-constraints[0].authRoles[0]=**
```

`**` means "any authenticated user".

---

## Configuration

`src/main/resources/application.properties`:

```properties
server.port=8081
spring.application.name=Spring Boot 2 with Keycloak 26

# Keycloak
keycloak.auth-server-url=http://localhost:8080/
keycloak.realm=spring-boot
keycloak.resource=spring-boot-client
keycloak.public-client=false
keycloak.bearer-only=false
keycloak.principal-attribute=preferred_username
keycloak.credentials.secret=L64Y6rfiEyN2H6QNySsKOuOifr8KATCe

# JSP
spring.mvc.view.prefix=/WEB-INF/jsp/
spring.mvc.view.suffix=.jsp
```

---

## Keycloak Setup

1. Start Keycloak 26 (e.g. via Docker):
   ```bash
   docker run -p 8080:8080 \
     -e KEYCLOAK_ADMIN=admin \
     -e KEYCLOAK_ADMIN_PASSWORD=admin \
     quay.io/keycloak/keycloak:26.0.0 start-dev
   ```
2. Log in to the admin console at <http://localhost:8080>.
3. Create a **Realm** named `spring-boot`.
4. Create a **Client**:
   - Client ID: `spring-boot-client`
   - Client authentication: **On** (confidential)
   - Valid redirect URIs: `http://localhost:8081/*`
   - Valid post logout redirect URIs: `http://localhost:8081/*`
   - Web origins: `http://localhost:8081`
5. On the client's **Credentials** tab, copy the client secret and paste it into `keycloak.credentials.secret` in `application.properties`.
6. Create a user under the `spring-boot` realm and set a password.

---

## Build & Run

```bash
# Build
mvn clean package

# Run
mvn spring-boot:run
```

Then open:

- <http://localhost:8081/> — public page
- <http://localhost:8081/admin/index> — you will be redirected to Keycloak for login

---

## Logout Notes

`AdminController#logout` clears the local session via `request.logout()` and then redirects the browser to Keycloak's OIDC end-session endpoint. This ensures the Keycloak SSO cookies are also cleared, so a subsequent request to `/admin/index` will prompt for login again instead of silently re-authenticating via SSO:

```java
@GetMapping(path = "/admin/logout")
public String logout(HttpServletRequest request) throws Exception {
    request.logout();
    return "redirect:http://localhost:8080/realms/spring-boot/protocol/openid-connect/logout"
         + "?post_logout_redirect_uri=http://localhost:8081/"
         + "&client_id=spring-boot-client";
}
```

Notes:
- The `post_logout_redirect_uri` must match one of the **Valid post logout redirect URIs** configured on the `spring-boot-client` in Keycloak (e.g. `http://localhost:8081/*`), otherwise Keycloak will reject the redirect after logout.
- If you only call `request.logout()` and redirect to `/`, the local `HttpSession` is invalidated but the Keycloak SSO cookies on `localhost:8080` remain, and `/admin/index` may silently re-authenticate the user without prompting for credentials.

---

## Author

Muhammad Edwin — `edwin at redhat dot com`
