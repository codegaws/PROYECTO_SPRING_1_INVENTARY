## 💡CLASE 10 SECURITY CONFIG</strong> </summary>

### 🔒 Spring Boot Security: Explicación Detallada del Código `CustomUserDetailsService`

Este código define un **servicio personalizado de autenticación** en una aplicación Spring Boot utilizando Spring Security. Sirve para cargar los detalles de un usuario desde la base de datos, útil en procesos de login. A continuación, se describe **cada elemento con ejemplos y emojis** para mejor comprensión:

---

### 📦 Imports

```java
import com.george.invetorymanagementsystem.entity.User;
import com.george.invetorymanagementsystem.exceptions.NotFoundException;
import com.george.invetorymanagementsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
```

- **User**: Clase de entidad que representa un usuario en la base de datos.
- **NotFoundException**: Excepción personalizada para indicar usuario no encontrado.
- **UserRepository**: Acceso a métodos para consultar usuarios en la base de datos.
- **@RequiredArgsConstructor**: Anotación de Lombok que genera constructor con argumentos necesarios para campos finales.
- **UserDetails/UserDetailsService/UsernameNotFoundException**: Interfaces y excepciones de Spring Security necesarias para cargar y manejar detalles del usuario.
- **@Service**: Marca la clase como un servicio de Spring.

---

### 🏷️ Decoradores y Definición de Clase

```java
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService
```

- **@Service** 🛠️: Declara este componente como un servicio gestionado por Spring.
- **@RequiredArgsConstructor** ✏️: (Opcional en este contexto, ya que también se usa @Autowired) Genera constructor para inyectar dependencias.
- **CustomUserDetailsService**: Clase que implementa la lógica de carga de usuarios.
- **implements UserDetailsService**: Contrato que obliga a implementar el método `loadUserByUsername`.

---

### 🏡 Inyección de Dependencias

```java
@Autowired
private UserRepository userRepository;
```

- **@Autowired** 🧩: Pide a Spring que inyecte automáticamente el repositorio de usuarios.
- **userRepository**: Objeto para consultar usuarios en BD.

---

### ⚡ Sobrescritura de Método

```java
@Override
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByEmail(username)
            .orElseThrow(() -> new NotFoundException("User Email Not Found!"));
    return AuthUser.builder()
            .user(user)
            .build();
}
```

### Paso a paso:

1. **Método loadUserByUsername** 🕵️‍♂️
    - Parámetro: **username** (por lo general es el email).
    - Se ejecuta automáticamente cuando Spring Security busca autenticar un usuario.

2. **Buscar usuario** 🔍
   ```java
   userRepository.findByEmail(username)
   ```
    - Busca el usuario en la base de datos por su email.

3. **Manejo de usuario no encontrado** ⚠️
   ```java
   .orElseThrow(() -> new NotFoundException("User Email Not Found!"));
   ```
    - Si no existe, lanza una excepción personalizada.

4. **Construcción del UserDetails personalizado** 🛠️
   ```java
   return AuthUser.builder().user(user).build();
   ```
    - Devuelve un objeto de tipo `UserDetails`.
    - `AuthUser` es una clase personalizada (debes implementarla) que adapta tu entidad `User` al modelo de Spring Security.

---

### 💡 Ejemplo Completo

Supongamos que tienes un login con el correo y clave. El sistema usará este servicio cuando llamas al endpoint de autenticación.   
**Ejemplo de flujo:**

1. Usuario intenta iniciar sesión con `usuario@ejemplo.com`.
2. Spring Security invoca `CustomUserDetailsService.loadUserByUsername("usuario@ejemplo.com")`.
3. Se busca el usuario en la BD.
4. Si existe, se crea un objeto `AuthUser` que implementa UserDetails.
5. Si no existe, se lanza "User Email Not Found!".

---

### 🛠️ Ejemplo de AuthUser

Aquí tienes cómo podría verse la clase `AuthUser`:

```java
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class AuthUser implements UserDetails {

    private final User user;

    public AuthUser(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Retorna los roles/permisos del usuario
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
```
---

### 🔑 Resumen

- 📚 Este servicio personaliza cómo buscar usuarios al autenticar.
- 👤 Implementa la lógica de Spring Security buscando por email.
- 💥 Lanza error si no existe el usuario.
- 🔑 Devuelve un `UserDetails` usado internamente para seguridad.

---

### 📋 Referencia rápida

| Elemento               | Icono | Descripción breve                                             |
|------------------------|-------|--------------------------------------------------------------|
| @Service               | 🛠️   | Marca la clase como servicio de Spring                       |
| @Autowired             | 🧩    | Inyecta dependencias                                         |
| UserDetailsService     | 🔐    | Contrato usado por Spring Security para buscar usuarios      |
| UserRepository         | 💽    | Acceso a la base de datos de usuarios                        |
| NotFoundException      | ⚠️    | Excepción personalizada si usuario no existe                 |
| UserDetails            | 🗝️    | Objeto adaptador con la info de autenticación del usuario    |
| AuthUser               | 👤    | Implementación concreta de UserDetails (personalizada)       |

---
### 👤 Clase `AuthUser` en Spring Boot Security

Esta clase es la implementación personalizada de `UserDetails` que adapta tu entidad propia de usuario (`User`) al modelo interno de autenticación de **Spring Security**. Aquí se explican **cada elemento** y su función, usando emojis y ejemplos claros para facilitar la comprensión.

---

### 📦 Imports

```java
import com.george.invetorymanagementsystem.entity.User;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;
```

- **User**: Entidad personalizada que representa al usuario en tu base de datos.
- **Lombok (`@Data`, `@Builder`)**: Generan automáticamente métodos útiles (getters/setters, constructor, builder pattern).
- **Spring Security (`GrantedAuthority`, `UserDetails`, etc.)**: Proveen interfaces que Spring Security entiende para protección de endpoints.

---

### 🏷️ Anotaciones de Clase

```java
@Data
@Builder
public class AuthUser implements UserDetails
```

- **@Data** 📝: Lombok genera automáticamente getters, setters, equals, hashCode y toString.
- **@Builder** 🧱: Lombok habilita el patrón builder para instanciar fácilmente objetos de esta clase.
- **implements UserDetails**: Obliga a implementar métodos que Spring Security necesita para autenticar y autorizar usuarios.

---

### 🧩 Atributo Interno

```java
private User user;
```
- **user** 👤: Instancia de tu entidad de usuario. Contiene toda la información relevante como email, contraseña, roles, etc.

---

### 🔑 Métodos sobrescritos de `UserDetails`

1. ### 🔗 Autoridades

    ```java
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(user.getRole().name()));
    }
    ```
    - Retorna la colección de roles/permisos asignados al usuario.
    - Envuelve el rol de tu entidad de usuario en un objeto `SimpleGrantedAuthority`.
    - **Ejemplo:** Si el usuario es ADMIN, retornará `[{"authority": "ADMIN"}]`.
    - **¿Por qué es importante?** Spring Security usa las autoridades para permitir o restringir acceso a los endpoints.

2. ### 🔒 Password

    ```java
    @Override
    public String getPassword() {
        return user.getPassword();
    }
    ```
    - Retorna la contraseña almacenada (debe estar hasheada).

3. ### 📧 Username

    ```java
    @Override
    public String getUsername() {
        return user.getEmail();
    }
    ```
    - Retorna el identificador único del usuario, **en este caso el email**.
    - Spring Security tomará este valor para hacer el login.

4. ### ⏳ ¿Cuenta Expirada?

    ```java
    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }
    ```
    - Indica si la cuenta ha expirado. Por defecto, retornará `true`.
    - **Tip:** Puedes personalizar para manejar lógicas de expiración.

5. ### 🚪 ¿Cuenta Bloqueada?

    ```java
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    ```
    - Siempre retorna `true`, significa que la cuenta nunca estará bloqueada.
    - **Tip:** Puedes cambiar esto según lógica de negocio (ej: muchos intentos fallidos).

6. ### 🔐 ¿Credenciales Expiradas?

    ```java
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    ```
    - Siempre `true`. Cambia si quieres forzar cambio de contraseña periódicamente.

7. ### ✅ ¿Cuenta Habilitada?

    ```java
    @Override
    public boolean isEnabled() {
        return true;
    }
    ```
    - Siempre `true`, indica que la cuenta está habilitada.
    - Puedes condicionar esto, por ejemplo, si el usuario no ha verificado el email.

---

### 🛠️ Ejemplo Práctico de Uso

Cuando Spring Security necesita autenticar un usuario, va a convertir la entidad de tu base de datos en un objeto `AuthUser`:

```java
User user = userRepository.findByEmail("usuario@ejemplo.com").get();
AuthUser authUser = AuthUser.builder().user(user).build();

String correo = authUser.getUsername();       // "usuario@ejemplo.com"
String clave = authUser.getPassword();        // "***hash***"
List<GrantedAuthority> roles = (List<GrantedAuthority>) authUser.getAuthorities(); // [SimpleGrantedAuthority("ADMIN")]
```

---

### 🧑‍💻 Comentarios Adicionales

- Así, cualquier lógica adicional (roles, expiración, bloqueo) puede ser controlada aquí y Spring Security la integrará automáticamente.
- Si quieres agregar más campos o controles, aquí es el lugar centralizado para hacerlo.

---

### 📋 Tabla Resumen

| Elemento                       | Icono | Descripción breve                                                  |
|--------------------------------|-------|--------------------------------------------------------------------|
| @Data, @Builder                | 📝🧱  | Genera getters/setters/constructor/builder automático              |
| implements UserDetails         | 🔐    | Indica que es compatible con Spring Security                       |
| getAuthorities()               | 🔗    | Devuelve los roles/permisos del usuario                            |
| getUsername(), getPassword()   | 📧🔒  | Email y contraseña de la entidad de usuario                        |
| isAccountNonExpired()          | ⏳    | Indica si la cuenta está expirada                                  |
| isAccountNonLocked()           | 🚪    | Indica si la cuenta está bloqueada                                 |
| isCredentialsNonExpired()      | 🔐    | Indica si la contraseña está expirada                              |
| isEnabled()                    | ✅    | Seguridad: indica si la cuenta está activa                         |

---
### Algunos Ejemplos de Uso Común de la Clase `AuthUser` de bloqueo

### 🧑‍💻 Ejemplos prácticos de uso de `AuthUser` en seguridad Spring Boot

A continuación te presento ejemplos que puedes copiar directamente para entender y probar cómo se usa y cómo puedes personalizar la clase `AuthUser`:

---

### 🌐 Ejemplo básico: uso en el flujo de autenticación

Supón que tienes el siguiente login controller:

```java
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
        Authentication authentication = authenticationManager.authenticate(authToken);
        AuthUser userDetails = (AuthUser) authentication.getPrincipal();

        return ResponseEntity.ok("Usuario autenticado! Rol: " + userDetails.getAuthorities());
    }
}
```

---

### 🛠️ Ejemplo de creación manual de `AuthUser`

Supón que recibes una entidad usuario desde la base de datos (ejemplo simulado):

```java
User user = new User();
user.setEmail("admin@ejemplo.com");
user.setPassword("$2a$10$encryptedPassword...");
user.setRole(Role.ADMIN);

AuthUser authUser = AuthUser.builder()
    .user(user)
    .build();

System.out.println("Nombre de usuario: " + authUser.getUsername());    // admin@ejemplo.com
System.out.println("Roles: " + authUser.getAuthorities());             // [ADMIN]
System.out.println("Contraseña: " + authUser.getPassword());           // $2a$10$encryptedPassword...
```

---

### 🔗 Control de permisos usando roles

Si en tu controller tienes una restricción de acceso por rol:

```java
@PreAuthorize("hasAuthority('ADMIN')")
@GetMapping("/admin/secure-data")
public String secureAdminData() {
    return "Solo administradores pueden ver esto";
}
```
Cuando tu método `getAuthorities()` retorna `[SimpleGrantedAuthority("ADMIN")]`, este endpoint solo será accesible a usuarios con dicho rol.

---

### 🔒 Ejemplo de cuenta bloqueada (personalizado)

Puedes modificar el método `isAccountNonLocked()` así:

```java
@Override
public boolean isAccountNonLocked() {
    // Supón que tu entidad User tiene un campo booleano llamado locked
    return !user.isLocked();
}
```
Así, si el usuario está bloqueado en la base de datos, será rechazado el inicio de sesión.

---

### ✅ Ejemplo de cuenta habilitada (personalizado)

Supón que tu entidad `User` tiene un campo `boolean enabled` (usuario activado/desactivado):

```java
@Override
public boolean isEnabled() {
    return user.isEnabled();
}
```

---

### ⏳ Ejemplo de cuenta expirada (personalizado)

Supón que tienes fecha de expiración en la entidad:

```java
@Override
public boolean isAccountNonExpired() {
    return user.getExpirationDate().isAfter(LocalDateTime.now());
}
```

---

**TIP:**  
Todos estos métodos pueden personalizarse según tu modelo y tu lógica de negocio. Spring Security automáticamente verificará estas condiciones al autenticar usuarios y solo permitirá el acceso si todas retornan `true`.

---

### 🛡️ Explicación Detallada de `AuthFilter` (Spring Security, JWT)

Este filtro personalizado (`AuthFilter`) forma parte del sistema de autenticación **JWT** en tu aplicación Spring Boot. Se asegura de que cada petición HTTP verifique el token JWT del usuario antes de continuar con la lógica de la aplicación.

---

### 📦 Imports esenciales

```java
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
```

- **Filtros y servlet:** Permiten interceptar y manipular solicitudes HTTP.
- **Spring Security (`UsernamePasswordAuthenticationToken`, `SecurityContextHolder`):** Proveen las herramientas para crear un contexto autenticado.
- **Lombok:** Anotaciones para reducción de código.
- **WebAuthenticationDetailsSource:** Fuente de detalles de autenticación HTTP.
- **OncePerRequestFilter:** Garantiza que el filtro se ejecute una sola vez por solicitud.

---

### 🏷️ Anotaciones y Definición de Clase

```java
@Component
@Slf4j
@RequiredArgsConstructor
public class AuthFilter extends OncePerRequestFilter
```
- **@Component** 🏗️: Spring detecta y registra este filtro automáticamente.
- **@Slf4j** 📋: Habilita el logging (registros en consola/log).
- **@RequiredArgsConstructor** ⚡: Inyección automática por constructor de dependencias finales.
- **extends OncePerRequestFilter** 🔄: Garantiza una sola ejecución por solicitud HTTP.

---

### 🔗 Variables Inyectadas

```java
private final JwtUtils jwtUtils;
private final CustomUserDetailsService customUserDetailsService;
```
- **jwtUtils** 🧾: Lógica para validación y parsing de JWT.
- **customUserDetailsService** 👤: Permite cargar los detalles del usuario desde la base de datos, usando email extraído del token.

---

### 🔍 Método Principal: `doFilterInternal`

### **Flujo paso a paso:**

1. **Obtener token del request**
   ```java
   String token = getTokenFromRequest(request);
   ```
    - Busca el header **"Authorization"** (con formato `Bearer eyJhbGci...`).

2. **Verificar y procesar el token**
   ```java
   if (token != null) {
       String email = jwtUtils.getUsernameFromToken(token);
       UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
       if (StringUtils.hasText(email) && jwtUtils.isTokenValid(token, userDetails)) {
           // ...
       }
   }
   ```
    - Extrae el email del usuario usando el JWT.
    - Carga el usuario desde la BD.
    - Valida el JWT (firma, expiración...).

3. **Establecer usuario autenticado en el contexto**
   ```java
   UsernamePasswordAuthenticationToken authenticationToken =
       new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
   authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
   SecurityContextHolder.getContext().setAuthentication(authenticationToken);
   ```
    - Crea token autenticado de Spring.
    - Asigna detalles adicionales del request (IP, session...).
    - "Loguea" el usuario internamente para la duración de este request.

4. **Continuar con la cadena de filtros**
   ```java
   filterChain.doFilter(request, response);
   ```

5. **Manejo de errores**
   ```java
   catch (IOException e) {
       log.error("Error occured in Authfilter: {} ", e.getMessage());
   }
   ```

---

### 🧪 Ejemplo de uso

Supón que un frontend hace:
```
GET /api/usuarios
Authorization: Bearer eyJhbGciOi...
```
- `AuthFilter` procesa el JWT, valida, y, si es correcto, la petición continúa como autenticada y con los roles del usuario.
- Si el JWT es inválido, no se establece autenticación y pueden saltar errores 401/403 según configuración.

---

### 🏷️ Método Auxiliar: `getTokenFromRequest`

```java
private String getTokenFromRequest(HttpServletRequest request) {
    String tokenWithBearer = request.getHeader("Authorization");
    if (tokenWithBearer != null && tokenWithBearer.startsWith("Bearer ")) {
        return tokenWithBearer.substring(7);
    }
    return null;
}
```
- Extrae el token puro quitando el texto `"Bearer "` del header.

---

### 📋 Tabla resumen

| Elemento                                 | Icono | Funcionalidad resumida                                                        |
|------------------------------------------|-------|-------------------------------------------------------------------------------|
| @Component, @Slf4j, @RequiredArgs...     | 🏗️📋⚡ | Gestión automática con Spring, logging, inyección de dependencias              |
| JwtUtils                                 | 🧾    | Utilidad para validar y extraer datos del JWT                                 |
| CustomUserDetailsService                 | 👤    | Carga usuario desde la base según email del JWT                               |
| getTokenFromRequest()                    | 🔎    | Extrae el JWT del encabezado HTTP                                             |
| UsernamePasswordAuthenticationToken      | 🔐    | Crea el "login" interno para el request                                       |
| SecurityContextHolder                    | 🛡    | Guarda la autenticación para el resto del flujo Spring                        |
| filterChain.doFilter()                   | 🔄    | Continúa el procesamiento de la petición                                      |

---

### 🧑‍💻 Ejemplo de personalización

- **Podrías negar acceso si el usuario está inhabilitado**: comprueba si userDetails.isEnabled() antes de crear el UsernamePasswordAuthenticationToken.
- **Registrar logs detallados**: usando `log.info`.
- **Controlar excepciones personalizadas**: lanzar errores o responder 401 según reglas de negocio.

---

### 🛡️ Explicación Detallada de `SecurityFilter` (Spring Security Configuration)

Esta clase configura la seguridad global en tu aplicación **Spring Boot** usando Spring Security. Aquí se administran reglas de acceso, manejo de tokens JWT, gestión de excepciones y filtros de seguridad.

---

### 📦 Imports Clave

- **Spring Security:** Manejo de seguridad, filtros, autenticación.
- **Lombok:** Anotaciones para inyección y logging.
- **Custom Handlers:** Personalizan respuestas ante accesos denegados y errores de autenticación.
- **JWT AuthFilter:** Filtro personalizado que valida cada request usando tokens JWT.

---

### 🏷️ Anotaciones y Definición

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
```

- **@Configuration** 🏗️: Define la clase como configuración de Spring.
- **@EnableWebSecurity** 🔐: Activa la seguridad web.
- **@EnableMethodSecurity** 🚥: Permite usar anotaciones de seguridad a nivel de método (`@PreAuthorize`, etc).
- **@RequiredArgsConstructor** ⚡: Inyección automática vía constructor.
- **@Slf4j** 📋: Habilita registro/logging.

---

### 🔌 Inyección de Dependencias

```java
private final AuthFilter authFilter;
private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
private final CustomAccessDeniedHandler customAccessDeniedHandler;
```

- **AuthFilter**: Filtro personalizado donde se valida cada JWT.
- **CustomAuthenticationEntryPoint**: Lógica personalizada cuando un usuario NO autenticado intenta acceder.
- **CustomAccessDeniedHandler**: Gestión personalizada cuando un usuario autenticado intenta acceder a un recurso prohibido.

---

### 🔗 Método `securityFilterChain`

Configura la cadena de seguridad para todas las requests HTTP.

```java
public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
    httpSecurity.csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .exceptionHandling(exception -> exception.accessDeniedHandler(customAccessDeniedHandler)
                    .authenticationEntryPoint(customAuthenticationEntryPoint)
            )
            .authorizeHttpRequests(request -> request
                    .requestMatchers("/api/auth/**").permitAll()
                    .anyRequest().authenticated()
            )
            .sessionManagement(manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class);
    return httpSecurity.build();
}
```

### 🔍 Explicación línea por línea

1. **Deshabilita CSRF (CSRF protection)** 🤚  
   Útil para APIs RESTful/stateless — CSRF solo es relevante cuando hay sesiones web tradicionales.

2. **CORS por defecto** 🌍  
   Permite peticiones de orígenes cruzados (útil para APIs consumidas desde otros dominios/frontends).

3. **Excepciones personalizadas** ⚠️
    - Si el usuario está autenticado pero no tiene permisos: usa `customAccessDeniedHandler`.
    - Si el usuario NO está autenticado: usa `customAuthenticationEntryPoint`.

4. **Reglas de Autorización** 🚦
    - Permite libre acceso a endpoints bajo `/api/auth/**` (por ejemplo, login y registro).
    - Exige autenticación para el resto de requests.

5. **Sesión Stateless** 📦
    - Usa `SessionCreationPolicy.STATELESS`; no se guarda sesión: toda autenticación debe ser con JWT.

6. **Agrega el Filtro JWT** 🛡️
    - Inserta `authFilter` ANTES de `UsernamePasswordAuthenticationFilter`.
    - Así, cada request entrante será validado con JWT antes de procesar usuario/clave.

7. **Compila la configuración** ✔️

---

### 🔑 Beans Adicionales

### PasswordEncoder

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```
- BCrypt es un algoritmo seguro de hash para contraseñas.
- Spring lo usará automáticamente en autenticaciones y almacenamiento de nuevos usuarios.

---

### AuthenticationManager

```java
@Bean
public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
}
```
- Expone como bean el `AuthenticationManager` de Spring, usado para autenticar usuarios manualmente (por ejemplo, en POST login).

---

### 🧪 Ejemplo práctico de flujo

1. Cliente hace **POST /api/auth/login**: acceso permitido sin token.
2. Cliente recibe un JWT.
3. Cliente hace **GET /api/usuarios** con JWT:
    - `authFilter` valida el token.
    - Si es válido, la request sigue autenticada.
    - Si no es válido o falta, intervienen los handlers personalizados (`CustomAuthenticationEntryPoint`, etc.).
4. Si pide un recurso protegido sin JWT, recibe 401 o 403 según el caso.

---

### 📋 Tabla Resumen

| Elemento                        | Icono | Breve descripción                                                      |
|----------------------------------|-------|-----------------------------------------------------------------------|
| @EnableWebSecurity               | 🔐    | Habilita la protección de endpoints web                               |
| @EnableMethodSecurity            | 🚥    | Permite anotaciones como @PreAuthorize/@Secured en tus controladores  |
| SessionCreationPolicy.STATELESS  | 📦    | Sin sesiones, ideal para JWT                                          |
| addFilterBefore(AuthFilter, ...) | 🛡️    | Filtro JWT se ejecuta antes del filtro de Spring                      |
| PasswordEncoder (BCrypt)         | 🔑    | Hash seguro para contraseñas                                          |
| Custom handlers                  | ⚠️    | Personalizas mensajes de error/autorización                           |

---

### 🔑 Explicación Detallada de `JwtUtils` (JWT Utility para Spring Security)

Esta clase en tu proyecto sirve como **herramienta centralizada para la generación, validación y extracción de información** desde tokens JWT (JSON Web Token), que es la base estándar para autenticación stateless en modern apps.

---

### 📦 Imports Clave

- **JJWT (io.jsonwebtoken):** Librería popular para manejar JWT en Java/Spring.
- **Spring Security:** Facilita la integración con el modelo de usuario de seguridad.
- **Lombok (`@Slf4j`):** Añade un logger de manera automática.
- **@Service:** Marca el componente como un servicio para Spring.

---

### 🚀 Propiedades y Variables

```java
private static final long EXPIRATION_TIME_IN_MILLISEC = ... // 6 meses
private SecretKey key;

@Value("${secreteJwtString}")
private String secreteJwtString;
```
- **EXPIRATION_TIME_IN_MILLISEC:** 🕒 Define la duración del token en milisegundos (aquí equivale a 6 meses).
- **key:** 🔑 Llave secreta usada para firmar y verificar JWT, generada a partir de un string secreto.
- **secreteJwtString (@Value):** 🛡️ Cadena secreta configurada en tu `application.properties` o variables de entorno.

---

### 🔄 Inicialización (`@PostConstruct`)

```java
@PostConstruct
private void init() {
    byte[] keyByte = secreteJwtString.getBytes(StandardCharsets.UTF_8);
    this.key = new SecretKeySpec(keyByte, "HmacSHA256");
}
```
- Convertir la cadena secreta en un arreglo de bytes y crear una clave HMAC-SHA256.
- Esencial: Así, cada token solo será válido si fue firmado con la misma llave secreta.

---

### 🪄 Método: `generateToken(String email)`

```java
public String generateToken(String email) {
    return Jwts.builder()
            .subject(email)
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME_IN_MILLISEC))
            .signWith(key)
            .compact();
}
```
- 💡 **Crea un nuevo token JWT** con:
    - `subject`: el email (identificador del usuario).
    - `issuedAt`: fecha de creación.
    - `expiration`: fecha de expiración.
    - Firmado con la clave secreta.
- **🔧 Uso típico:** Se llama al loguear un usuario y se retorna este token.

---

### 🧑‍💻 Método: `getUsernameFromToken(String token)`

```java
public String getUsernameFromToken(String token) {
    return extractClaims(token, Claims::getSubject);
}
```
- Extrae el "subject" (email) de un JWT.

---

### 🕵️ Método genérico: `extractClaims(...)`

```java
private <T> T extractClaims(String token, Function<Claims, T> claimsTFunction) {
    return claimsTFunction.apply(Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload());
}
```
- Abre, valida y decodifica el JWT usando la clave.
- Usa una función (por ejemplo, para obtener el email o la expiración) sobre los claims.
- 🛡️ Si la firma o token es inválido/llegó corrupto, lanzará excepción.

---

### ✅ Método: `isTokenValid(String token, UserDetails userDetails)`

```java
public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = getUsernameFromToken(token);
    return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
}
```
- Comprueba que:
    - El usuario extraído de JWT coincide con el del sistema.
    - El token NO haya expirado.

---

### ⏳ Método Privado: `isTokenExpired(String token)`

```java
private boolean isTokenExpired(String token) {
    return extractClaims(token, Claims::getExpiration).before(new Date());
}
```
- 📆 Revisa si la fecha de expiración (`expiration`) ya pasó.

---

### 🧪 Ejemplo de Uso

### 1. **Generar token:**
```java
String jwt = jwtUtils.generateToken("usuario@ejemplo.com");
// jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 2. **Extraer usuario desde token:**
```java
String email = jwtUtils.getUsernameFromToken(jwt); // "usuario@ejemplo.com"
```

### 3. **Validar token:**
```java
UserDetails user = ...;
boolean esValido = jwtUtils.isTokenValid(jwt, user);
```

---

### 📋 Tabla Resumida

| Elemento                        | Icono | Breve explicación                                   |
|----------------------------------|-------|-----------------------------------------------------|
| SecretKey + secreteJwtString     | 🔑🛡️  | Seguridad: solo tu app puede firmar/verificar tokens|
| generateToken                    | 🪄    | Crea JWT con duración, subject, y firma secreta     |
| getUsernameFromToken             | 🧑‍💻 | Obtiene email/username codificado en el JWT         |
| isTokenValid                     | ✅    | Controla que el token pertenezca al usuario y no expire |
| isTokenExpired                   | ⏳    | Expiración de la validez del token                  |
| extractClaims                    | 🕵️   | Método genérico para leer cualquier claim           |

---

### 🚨 Buenas Prácticas & Consejos

- **Guarda** `secreteJwtString` en variables de entorno o archivos seguros.
- **No compartas** la clave secreta.
- **Cambia la expiración** según tus necesidades (6 meses puede ser mucho para apps públicas).

---

### 🌎 Explicación Detallada de `CorsConfig` (CORS en Spring Boot)

Esta clase configura las reglas CORS (**Cross-Origin Resource Sharing**) en tu API de Spring Boot, permitiendo (o restringiendo) el acceso a tu backend desde otros dominios/frontends. Es muy importante para exponer APIs a aplicaciones cliente en distintos orígenes (puertos, dominios).

---

### 📦 IMPORTS CLAVE

- **WebMvcConfigurer:** Interfaz de configuración para funcionalidades web en Spring.
- **CorsRegistry:** Clase para registrar reglas CORS.
- **@Configuration:** Marca la clase como configuración de Spring Boot.

---

### 🏷️ ANOTACIONES

```java
@Configuration
public class CorsConfig { ... }
```
- **@Configuration** 🏗️: Esta clase define configuraciones que Spring Boot aplica automáticamente al arrancar la app.

---

### ⚙️ MÉTODO PRINCIPAL

```java
public WebMvcConfigurer webMvcConfigurer() {
    return new WebMvcConfigurer() {
        @Override
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/**")
                    .allowedMethods("GET", "POST", "PUT", "DELETE")
                    .allowedOrigins("*");
        }
    };
}
```

### 📑 Paso a paso:

1. **Definición de Bean WebMvcConfigurer**
    - Devuelve un nuevo `WebMvcConfigurer` anónimo para personalizar configuración web.
    - (Nota: Le falta la anotación `@Bean` para registrarse correctamente como bean en Spring.)

2. **Método addCorsMappings**
    - Define las reglas CORS.

3. **registry.addMapping("/**")**
    - Aplica la configuración a todas las rutas del backend (`/**`).

4. **allowedMethods("GET", "POST", "PUT", "DELETE")**
    - Permite únicamente los métodos HTTP indicados desde el frontend.

5. **allowedOrigins("*")**
    - Permite cualquier origen (dominio).  
      ⚠️ **CUIDADO:** El uso del comodín `*` es muy permisivo y se recomienda restringir a dominios específicos en producción.

---

### 🧑‍💻 EJEMPLO DE USO

Si tienes:
- **Frontend** en `http://localhost:3000`
- **Backend** en `http://localhost:8080`

Esto es lo que permite esta configuración:
- Peticiones `GET`, `POST`, `PUT`, `DELETE` desde **cualquier** dominio/puerto.
- Se suele usar para desarrollo o APIs públicas.
- Para mayor _seguridad_, restringe a `.allowedOrigins("http://localhost:3000")`

---

### 🛠️ CÓMO MEJORAR (con @Bean)

Debes anotar el método con `@Bean` para que Spring lo recoja correctamente como configuración:

```java
@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        .allowedOrigins("*");
            }
        };
    }
}
```

---

### 📋 TABLA RESUMEN

| Elemento               | Icono | Explicación                                     |
|------------------------|-------|-------------------------------------------------|
| @Configuration         | 🏗️   | Declara la clase como configuración de Spring   |
| addMapping("/**")      | ✨    | Aplica reglas a todos los endpoints             |
| allowedMethods(...)    | 🚦    | Permite solo ciertos métodos HTTP               |
| allowedOrigins("*")    | 🌍   | Permite cualquier origen                        |
| @Bean                  | 🫘    | Registra el configurador como bean de Spring    |

---
### RESUMEN TOTAL
### 🎓 Relación entre Clases de Spring Security: Guía Completa del Profesor

¡Hola! Como tu profesor con alta seniority en Java y Spring Security, te voy a explicar paso a paso cómo **todas estas clases trabajan juntas** para crear un sistema de autenticación JWT completo y robusto.

---

### 🧩 VISTA GENERAL: ¿Qué hace cada clase?

Imagina que tu aplicación es una **fortaleza** 🏰. Cada clase tiene un rol específico para protegerla:

| Clase | Rol en la Fortaleza | Icono |
|-------|-------------------|-------|
| **SecurityFilter** | 👑 **General Principal** - Define las reglas generales de seguridad |
| **AuthFilter** | 🛡️ **Guardia de la Puerta** - Revisa cada visitante (request) |
| **JwtUtils** | 🔑 **Maestro de llaves** - Crea y valida pases de acceso (JWT) |
| **CustomUserDetailsService** | 📚 **Archivero** - Busca información de usuarios en registros |
| **AuthUser** | 🎭 **Adaptador de identidad** - Convierte usuario BD → Spring Security |
| **CorsConfig** | 🌉 **Constructor de puentes** - Permite acceso desde otros dominios |

---

### 🔄 FLUJO COMPLETO: De la Petición a la Respuesta

### **Escenario**: Un usuario quiere acceder a `/api/usuarios`

```
🌐 Frontend (React/Vue/Angular)
        ↓
📡 HTTP Request + JWT Token
        ↓
🌉 CorsConfig (permite el origen)
        ↓
🛡️ AuthFilter (valida JWT)
        ↓
🔑 JwtUtils (decodifica token)
        ↓
📚 CustomUserDetailsService (busca usuario)
        ↓
🎭 AuthUser (adapta para Spring)
        ↓
👑 SecurityFilter (aplicar reglas)
        ↓
✅ Controller (procesa lógica)
```

---

### 🔗 RELACIONES DETALLADAS

### 1️⃣ **SecurityFilter** 👑 → **AuthFilter** 🛡️
```java
// En SecurityFilter
.addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class)
```
- **SecurityFilter** le dice a Spring: "Usa AuthFilter ANTES del filtro estándar"
- **AuthFilter** se convierte en el **primer guardián** de cada petición

### 2️⃣ **AuthFilter** 🛡️ → **JwtUtils** 🔑
```java
// En AuthFilter
String email = jwtUtils.getUsernameFromToken(token);
boolean isValid = jwtUtils.isTokenValid(token, userDetails);
```
- **AuthFilter** le pide a **JwtUtils**: "Dame el email de este token y valídalo"
- **JwtUtils** decodifica y verifica la firma del JWT

### 3️⃣ **AuthFilter** 🛡️ → **CustomUserDetailsService** 📚
```java
// En AuthFilter
UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
```
- **AuthFilter** le dice a **CustomUserDetailsService**: "Necesito los detalles del usuario con este email"
- **CustomUserDetailsService** busca en la base de datos

### 4️⃣ **CustomUserDetailsService** 📚 → **AuthUser** 🎭
```java
// En CustomUserDetailsService
return AuthUser.builder().user(user).build();
```
- **CustomUserDetailsService** crea un **AuthUser** que envuelve la entidad User
- **AuthUser** adapta tu modelo de BD al formato que Spring Security entiende

### 5️⃣ **SecurityFilter** 👑 → **CorsConfig** 🌉
```java
// En SecurityFilter
.cors(Customizer.withDefaults())
```
- **SecurityFilter** activa las reglas CORS definidas en **CorsConfig**
- Permite que frontends en otros dominios accedan a tu API

---

### 📖 EJEMPLO PASO A PASO: Login de Usuario

### **Paso 1: Usuario hace Login** 🔐
```http
POST /api/auth/login
{
  "email": "juan@ejemplo.com",
  "password": "miPassword123"
}
```

### **Paso 2: Controller valida credenciales** ✅
```java
// En tu LoginController
AuthenticationManager authManager = ...; // Bean de SecurityFilter
UsernamePasswordAuthenticationToken authToken = 
    new UsernamePasswordAuthenticationToken(email, password);
Authentication auth = authManager.authenticate(authToken);
```

### **Paso 3: CustomUserDetailsService busca usuario** 📚
```java
// Spring llama automáticamente a:
public UserDetails loadUserByUsername(String email) {
    User user = userRepository.findByEmail(email).orElseThrow(...);
    return AuthUser.builder().user(user).build();
}
```

### **Paso 4: AuthUser adapta la información** 🎭
```java
// Spring Security puede usar el AuthUser porque implementa UserDetails
String password = authUser.getPassword();
Collection<GrantedAuthority> roles = authUser.getAuthorities();
```

### **Paso 5: Generar JWT** 🔑
```java
// En tu controller, después de autenticar:
String jwt = jwtUtils.generateToken(email);
return ResponseEntity.ok(new LoginResponse(jwt));
```

---

### 🚀 EJEMPLO: Request Protegido

### **Paso 1: Frontend envía request** 📡
```http
GET /api/usuarios
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### **Paso 2: CorsConfig permite el origen** 🌉
Si el frontend está en `localhost:3000`, CorsConfig permite la petición.

### **Paso 3: AuthFilter intercepta** 🛡️
```java
String token = getTokenFromRequest(request); // Extrae JWT
String email = jwtUtils.getUsernameFromToken(token); // Decodifica
UserDetails user = customUserDetailsService.loadUserByUsername(email); // Busca usuario
if (jwtUtils.isTokenValid(token, user)) {
    // Establece autenticación en Spring Security
    SecurityContextHolder.getContext().setAuthentication(...);
}
```

### **Paso 4: SecurityFilter aplica reglas** 👑
```java
// Como el usuario está autenticado, permite acceso:
.authorizeHttpRequests(request -> request
    .anyRequest().authenticated() // ✅ PASS
)
```

### **Paso 5: Controller procesa** 🎯
```java
@GetMapping("/api/usuarios")
public List<Usuario> getUsuarios() {
    // El usuario está autenticado y autorizado
    return usuarioService.findAll();
}
```

---

### 🎨 DIAGRAMA DE FLUJO COMPLETO

```mermaid
graph TB
    A[🌐 Frontend Request] --> B{🌉 CorsConfig<br/>¿Origen permitido?}
    B -->|❌ No| C[🚫 CORS Error]
    B -->|✅ Sí| D[🛡️ AuthFilter]
    
    D --> E{¿Tiene JWT?}
    E -->|❌ No| F[🔑 JwtUtils<br/>Token inválido]
    E -->|✅ Sí| G[🔑 JwtUtils<br/>Decodifica token]
    
    G --> H[📚 CustomUserDetailsService<br/>Busca usuario por email]
    H --> I[🎭 AuthUser<br/>Adapta User → UserDetails]
    I --> J{🔑 JwtUtils<br/>¿Token válido?}
    
    J -->|❌ No| K[⚠️ Authentication Error]
    J -->|✅ Sí| L[👑 SecurityFilter<br/>Aplica reglas de acceso]
    
    L --> M{¿Usuario autorizado?}
    M -->|❌ No| N[🚫 Access Denied]
    M -->|✅ Sí| O[🎯 Controller<br/>Procesa request]
    
    F --> P[🔴 Response 401]
    K --> P
    N --> Q[🔴 Response 403]
    O --> R[🟢 Response 200]
    C --> S[🔴 CORS Error]
```

---

### 💡 PUNTOS CLAVE PARA RECORDAR

### **Orden de Ejecución** 📋
1. **CorsConfig** → Permite origen
2. **AuthFilter** → Valida JWT
3. **JwtUtils** → Decodifica y valida token
4. **CustomUserDetailsService** → Busca usuario
5. **AuthUser** → Adapta formato
6. **SecurityFilter** → Aplica reglas de acceso

### **Dependencias** 🔗
- **AuthFilter** necesita **JwtUtils** y **CustomUserDetailsService**
- **CustomUserDetailsService** necesita **AuthUser**
- **SecurityFilter** coordina **AuthFilter** y **CorsConfig**
- **JwtUtils** es independiente (solo maneja tokens)

### **Responsabilidades Únicas** 🎯
- Cada clase tiene **una sola responsabilidad**
- **Separación de concerns**: JWT ≠ Usuario ≠ Filtros ≠ Configuración
- **Fácil testing**: Puedes probar cada clase por separado

---

### 🚨 ERRORES COMUNES DE PRINCIPIANTES

### ❌ **Error 1**: Mezclar responsabilidades
```java
// MAL: AuthFilter que también genera tokens
public class AuthFilter {
    public String login(String email) { ... } // ¡NO!
}
```

### ✅ **Correcto**: Separar responsabilidades
```java
// AuthFilter solo filtra, JwtUtils solo maneja JWT
```

### ❌ **Error 2**: No entender el flujo
"¿Por qué mi JWT no funciona?" → Revisar **cada paso** del flujo

### ✅ **Correcto**: Debuggear paso a paso
1. ¿CorsConfig permite el origen?
2. ¿AuthFilter recibe el token?
3. ¿JwtUtils puede decodificarlo?
4. ¿CustomUserDetailsService encuentra el usuario?

---

### 🎓 EJERCICIO PARA PRACTICAR

**Crea un endpoint de logout que:**
1. Reciba un JWT
2. Lo valide con JwtUtils
3. Agregue el token a una "blacklist"
4. Modifique AuthFilter para rechazar tokens en blacklist

**Pista:** Necesitarás modificar **JwtUtils** y **AuthFilter** 😉

---

¿Te queda claro cómo todas las piezas encajan? ¡Pregúntame cualquier duda específica! 🚀

</details>