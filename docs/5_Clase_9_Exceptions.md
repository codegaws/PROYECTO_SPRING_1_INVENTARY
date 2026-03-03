Te explico cada componente del directorio `exceptions`:

## 💡CLASE 09 EXCEPTIONS
```java
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException)
            throws IOException, ServletException {

        Response errorResponse = Response.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .message(accessDeniedException.getMessage())
                .build();

        response.setContentType("application/json");
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}

```

### 1. CustomAccessDeniedHandler.java

Es un manejador personalizado para errores de acceso denegado (HTTP 403). Se ejecuta cuando un usuario autenticado intenta acceder a un recurso para el cual no tiene permisos.

**Funcionamiento:**
- Implementa `AccessDeniedHandler` de Spring Security
- Cuando ocurre un `AccessDeniedException`, crea una respuesta JSON personalizada
- Establece el status HTTP 403 y devuelve el mensaje de error

**Ejemplo:**
```java
// Si un USER intenta acceder a un endpoint de ADMIN:
// GET /admin/users -> Devuelve:
{
  "status": 403,
  "message": "Access Denied"
}
```



#### CustomAccessDeniedHandler - Explicación Completa

---

#### 📋 Código del Método

```java
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException)
            throws IOException, ServletException {

        Response errorResponse = Response.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .message(accessDeniedException.getMessage())
                .build();

        response.setContentType("application/json");
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
```

---

#### 🎯 ¿Para qué sirve?

El `CustomAccessDeniedHandler` es un **manejador personalizado de errores de acceso denegado** en Spring Security. Su función principal es:

1. **Interceptar intentos de acceso no autorizados** a recursos protegidos
2. **Personalizar la respuesta de error** cuando un usuario autenticado intenta acceder a un recurso para el cual NO tiene permisos
3. **Devolver una respuesta JSON estructurada** en lugar del error HTML predeterminado de Spring Security

---

#### 🔄 ¿Cuándo se activa?

Este handler se activa en **situaciones de ACCESS DENIED (403 - FORBIDDEN)**, específicamente cuando:

1. ✅ **El usuario YA está autenticado** (tiene un token JWT válido)
2. ❌ **Pero NO tiene los permisos necesarios** para acceder al recurso solicitado

#### Ejemplos de cuándo se activa:

| Situación | ¿Se activa? | Explicación |
|-----------|-------------|-------------|
| Usuario con rol `USER` intenta acceder a endpoint `@PreAuthorize("hasRole('ADMIN')")` | ✅ SÍ | Usuario autenticado pero sin permisos |
| Usuario con rol `ADMIN` intenta acceder a endpoint protegido para `ADMIN` | ❌ NO | Tiene los permisos correctos |
| Usuario sin token JWT intenta acceder a un endpoint protegido | ❌ NO | Esto activa `CustomAuthenticationEntryPoint` (401) |
| Usuario con token expirado intenta acceder | ❌ NO | Esto activa `CustomAuthenticationEntryPoint` (401) |

---

#### 🚀 ¿Cómo se activa?

El handler se activa mediante la configuración de Spring Security en `SecurityFilter.java`:

```java
@Configuration
@EnableWebSecurity
public class SecurityFilter {
    
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(customAccessDeniedHandler)  // 👈 AQUÍ SE REGISTRA
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                );
        return httpSecurity.build();
    }
}
```

#### Flujo de activación paso a paso:

```
1. Cliente hace petición HTTP → /api/admin/users
2. AuthFilter valida el JWT → ✅ Token válido (usuario autenticado)
3. Spring Security verifica permisos → ❌ Usuario tiene rol USER, pero necesita ADMIN
4. Spring Security lanza AccessDeniedException
5. CustomAccessDeniedHandler.handle() se ejecuta 👈 AQUÍ
6. Se devuelve respuesta JSON con error 403
```

---

#### 🔍 ¿Por qué se activa?

Se activa por razones de **autorización fallida**, específicamente cuando:

#### 1. Roles Insuficientes
```java
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin/users")
public ResponseEntity<?> getAllUsers() {
    // Solo ADMIN puede acceder
}
```
Si un usuario con rol `USER` intenta acceder → **CustomAccessDeniedHandler se activa**

#### 2. Permisos Específicos Faltantes
```java
@PreAuthorize("hasAuthority('WRITE_PRIVILEGE')")
@PostMapping("/products")
public ResponseEntity<?> createProduct() {
    // Solo usuarios con permiso WRITE_PRIVILEGE
}
```
Si un usuario sin ese permiso intenta acceder → **CustomAccessDeniedHandler se activa**

#### 3. Expresiones SpEL Complejas
```java
@PreAuthorize("@userSecurity.isOwner(#userId)")
@DeleteMapping("/users/{userId}")
public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
    // Solo el propietario puede eliminar su cuenta
}
```
Si la validación falla → **CustomAccessDeniedHandler se activa**

---

#### ⚙️ Desglose del Código

#### 1. Anotaciones de la Clase

```java
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler
```

- **`@Component`**: Registra la clase como un bean de Spring para que pueda ser inyectado
- **`@RequiredArgsConstructor`**: Lombok genera un constructor con los campos `final` (inyecta `ObjectMapper`)
- **`implements AccessDeniedHandler`**: Implementa la interfaz de Spring Security para manejar accesos denegados

---

#### 2. Inyección de Dependencias

```java
private final ObjectMapper objectMapper;
```

- **`ObjectMapper`**: Convierte objetos Java a JSON
- Se usa para serializar la respuesta de error a formato JSON
- Es inyectado automáticamente por Spring Boot (viene con Jackson)

---

#### 3. Método handle() - Manejo del Error

```java
@Override
public void handle(HttpServletRequest request,
                   HttpServletResponse response,
                   AccessDeniedException accessDeniedException)
        throws IOException, ServletException
```

**Parámetros:**
- **`HttpServletRequest request`**: La petición HTTP que causó el error
- **`HttpServletResponse response`**: La respuesta HTTP que se enviará al cliente
- **`AccessDeniedException accessDeniedException`**: La excepción lanzada con detalles del error

---

#### 4. Construcción de la Respuesta de Error

```java
Response errorResponse = Response.builder()
        .status(HttpStatus.FORBIDDEN.value())  // 403
        .message(accessDeniedException.getMessage())
        .build();
```

Crea un objeto `Response` personalizado con:
- **`status: 403`**: Código HTTP FORBIDDEN (acceso denegado)
- **`message`**: El mensaje de la excepción (ej: "Access Denied")

---

#### 5. Configuración de la Respuesta HTTP

```java
response.setContentType("application/json");
response.setStatus(HttpStatus.FORBIDDEN.value());
response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
```

- **`setContentType("application/json")`**: Indica que la respuesta será JSON
- **`setStatus(403)`**: Establece el código de estado HTTP 403
- **`objectMapper.writeValueAsString(errorResponse)`**: Convierte el objeto Java a JSON
- **`getWriter().write(...)`**: Escribe el JSON en el cuerpo de la respuesta

---

#### 📤 Ejemplo de Respuesta Generada

Cuando se activa, el cliente recibe:

```json
{
  "status": 403,
  "message": "Access Denied",
  "createdDate": "2026-03-01T10:30:45.123"
}
```

**Headers HTTP:**
```
HTTP/1.1 403 Forbidden
Content-Type: application/json
```

---

#### ⚠️ ¿Qué pasa si NO lo pongo?

#### 1. Respuesta HTML en lugar de JSON
```html
<!DOCTYPE html>
<html>
<head><title>Error</title></head>
<body>
    <h1>Whitelabel Error Page</h1>
    <p>This application has no explicit mapping for /error...</p>
    <div>There was an unexpected error (type=Forbidden, status=403).</div>
</body>
</html>
```

❌ **Problemas:**
- No es útil para aplicaciones REST/SPA
- Los clientes frontend esperan JSON
- Dificulta el manejo de errores en el cliente

---

#### 2. Respuestas inconsistentes
Sin personalización, Spring Security usa diferentes formatos de error:
- A veces JSON
- A veces HTML
- Estructura inconsistente

❌ **Problemas:**
- El frontend no puede manejar errores de forma uniforme
- Dificulta la experiencia del usuario
- Más código en el frontend para manejar diferentes formatos

---

#### 3. Menos información de contexto
Las respuestas predeterminadas no incluyen:
- Timestamp personalizado
- Estructura de datos consistente con tu API
- Campos adicionales que tu frontend pueda necesitar

---

#### 4. Mala experiencia de desarrollo
Durante el desarrollo:
- Los mensajes de error son genéricos y poco informativos
- Dificulta la depuración
- No hay consistencia con el resto de tu API

---

#### 🔄 Diferencia con CustomAuthenticationEntryPoint

Es importante distinguir entre estos dos handlers:

| Aspecto | CustomAccessDeniedHandler | CustomAuthenticationEntryPoint |
|---------|---------------------------|--------------------------------|
| **Código HTTP** | 403 FORBIDDEN | 401 UNAUTHORIZED |
| **Situación** | Usuario autenticado sin permisos | Usuario NO autenticado o token inválido |
| **Interfaz** | `AccessDeniedHandler` | `AuthenticationEntryPoint` |
| **Método** | `handle()` | `commence()` |
| **Ejemplo** | Usuario con rol USER intenta acceder a /admin | Usuario sin token intenta acceder a /api/products |

#### Flujo completo de manejo de errores:

```
┌─────────────────────────────────────────────────┐
│         Cliente hace petición HTTP              │
└─────────────────────────┬───────────────────────┘
                          │
                          ▼
         ┌────────────────────────────────┐
         │  ¿Tiene token JWT válido?      │
         └────────┬───────────────┬────────┘
                  │ NO            │ SÍ
                  ▼               ▼
    ┌──────────────────────┐  ┌──────────────────────┐
    │ CustomAuthentication │  │ ¿Tiene los permisos │
    │    EntryPoint        │  │    necesarios?       │
    │   (401 UNAUTHORIZED) │  └──────┬───────┬───────┘
    └──────────────────────┘         │ NO    │ SÍ
                                     ▼       ▼
                      ┌────────────────────┐  ┌─────────────┐
                      │ CustomAccessDenied │  │  Acceso     │
                      │     Handler        │  │  Permitido  │
                      │  (403 FORBIDDEN)   │  └─────────────┘
                      └────────────────────┘
```

---

#### 💡 Importancia y Beneficios

#### ✅ Con CustomAccessDeniedHandler:

1. **API REST coherente**: Todas las respuestas en formato JSON consistente
2. **Mejor experiencia de usuario**: Mensajes de error claros y estructurados
3. **Facilita el frontend**: El cliente puede manejar errores de forma uniforme
4. **Debugging más fácil**: Respuestas informativas durante el desarrollo
5. **Profesionalismo**: API más pulida y lista para producción
6. **Seguridad**: No expone detalles internos innecesarios

---

#### 🔧 Configuración Completa en SecurityFilter

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // 👈 Habilita @PreAuthorize
@RequiredArgsConstructor
public class SecurityFilter {

    private final AuthFilter authFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                
                // 🔥 CONFIGURACIÓN DE MANEJADORES DE ERROR
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(customAccessDeniedHandler)           // 403
                        .authenticationEntryPoint(customAuthenticationEntryPoint) // 401
                )
                
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(manager -> 
                    manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class);
                
        return httpSecurity.build();
    }
}
```

---

#### 📚 Casos de Uso Reales

#### Ejemplo 1: Sistema de Inventario con Roles

```java
// Solo ADMIN puede crear productos
@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/api/products")
public ResponseEntity<?> createProduct(@RequestBody ProductDTO productDTO) {
    // Lógica de creación
}

// Si un usuario con rol USER intenta acceder:
// → CustomAccessDeniedHandler se activa → Retorna 403
```

#### Ejemplo 2: Control de Propietario

```java
// Solo el dueño puede actualizar su perfil
@PreAuthorize("@securityService.isOwner(#userId)")
@PutMapping("/api/users/{userId}")
public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody UserDTO userDTO) {
    // Lógica de actualización
}

// Si otro usuario intenta acceder:
// → CustomAccessDeniedHandler se activa → Retorna 403
```

---

#### 🎓 Resumen

| Aspecto | Detalle |
|---------|---------|
| **Propósito** | Manejar errores cuando un usuario autenticado no tiene permisos |
| **Código HTTP** | 403 FORBIDDEN |
| **Se activa cuando** | Usuario autenticado intenta acceder a recurso sin permisos suficientes |
| **Importancia** | Crucial para APIs REST que devuelven JSON consistente |
| **Sin él** | Respuestas HTML inconsistentes, mala experiencia de usuario |
| **Trabaja con** | `CustomAuthenticationEntryPoint` (para errores 401) |

---

#### 🔗 Referencias

- [Spring Security - AccessDeniedHandler](https://docs.spring.io/spring-security/reference/servlet/authorization/exception-handling.html)
- [HTTP Status Codes](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status)
- 401 vs 403: [Stack Overflow](https://stackoverflow.com/questions/3297048/403-forbidden-vs-401-unauthorized-http-responses)

---

**Autor**: Documentación del Proyecto Inventory Management System  
**Fecha**: Marzo 2026  
**Versión**: 1.0
---

### 2. CustomAuthenticationEntryPoint.java

Maneja errores de autenticación (HTTP 401) cuando un usuario no está autenticado o tiene credenciales inválidas.

```java
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    
    @Override
    public void commence(HttpServletRequest request, 
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException {
        
        Response errorResponse = Response.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .message("Authentication required")
                .build();
        
        response.setContentType("application/json");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
```

### 3. GlobalExceptionHandler.java

Maneja todas las excepciones de la aplicación de forma centralizada usando `@ControllerAdvice`.

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Response> handleNotFoundException(NotFoundException e) {
        Response response = Response.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message(e.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Response> handleValidationException(ValidationException e) {
        return ResponseEntity.badRequest().body(
            Response.builder()
                .status(400)
                .message(e.getMessage())
                .build()
        );
    }
}
```

### 4. Excepciones Personalizadas

### NotFoundException.java
```java
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}

// Uso:
throw new NotFoundException("Product with ID 123 not found");
```

### InvalidCredentialsException.java
```java
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}

// Uso en AuthService:
if (!passwordEncoder.matches(password, user.getPassword())) {
    throw new InvalidCredentialsException("Invalid username or password");
}
```

### NameValueRequiredException.java
```java
public class NameValueRequiredException extends RuntimeException {
    public NameValueRequiredException(String message) {
        super(message);
    }
}

// Uso:
if (product.getName() == null || product.getName().trim().isEmpty()) {
    throw new NameValueRequiredException("Product name is required");
}
```

### Flujo completo de manejo de errores:

1. **Error de validación** → `GlobalExceptionHandler` → Respuesta JSON 400
2. **Usuario no autenticado** → `CustomAuthenticationEntryPoint` → Respuesta JSON 401
3. **Usuario sin permisos** → `CustomAccessDeniedHandler` → Respuesta JSON 403
4. **Recurso no encontrado** → `NotFoundException` → `GlobalExceptionHandler` → Respuesta JSON 404

Este sistema garantiza respuestas consistentes y manejables desde el frontend.
