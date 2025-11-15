# PROYECTO INVENTARIO SPRINGBOOT

<details>
<summary><strong>💡Notas : Tabla BD</strong> </summary>

![image](/images/tabla.png)

### informacion de la tabla

¡**Exactamente**! 🎯

## Sí, se pueden generar distintos IDs de transacciones para un usuario

Con la relación `@ManyToOne` que tienes:

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name="user_id")
private User user;
```

### Lo que significa:

- **Cada transacción tiene su propio ID único** (clave primaria de TRANSACTION)
- **Múltiples transacciones pueden compartir el mismo `user_id`** (clave foránea)

### Ejemplo práctico:

```sql
-- Tabla TRANSACTION
ID  | USER_ID | TOTAL_PRICE | CREATED_AT
----|---------|-------------|------------
1   | 100     | 50.00       | 2025-01-15
2   | 100     | 75.50       | 2025-01-16  
3   | 100     | 120.00      | 2025-01-17
4   | 200     | 90.00       | 2025-01-15
5   | 200     | 45.00       | 2025-01-18
```

En este ejemplo:
- **Usuario 100** tiene 3 transacciones diferentes (IDs: 1, 2, 3)
- **Usuario 200** tiene 2 transacciones diferentes (IDs: 4, 5)

### Cada transacción es única porque:
- Tiene su propio **ID único**
- Puede ocurrir en **fechas diferentes**
- Puede tener **montos diferentes**
- Puede incluir **productos diferentes**

### En tu aplicación:
Un usuario como "codegaws" podría generar:
- Transacción #001 (compra del lunes)
- Transacción #002 (compra del miércoles)
- Transacción #003 (compra del viernes)

**¡¿que es LAZY?** 👏
¡Excelente pregunta! `fetch = FetchType.LAZY` es muy importante para el rendimiento de tu aplicación.

## ¿Qué significa `FetchType.LAZY`?

**LAZY** = "Perezoso" o "Bajo demanda"

### Comportamiento:
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name="user_id")
private User user;
```

Con `LAZY`, el objeto `User` **NO se carga automáticamente** cuando cargas una `Transaction`. Solo se carga cuando **realmente lo necesitas**.

## Ejemplo práctico:

### Con LAZY (Recomendado):
```java
// 1. Cargas la transacción - Solo ejecuta 1 query
Transaction transaction = transactionRepository.findById(1L);

// 2. En este punto, user NO está cargado aún
// No se ha ejecutado query para traer datos del usuario

// 3. Solo cuando accedes al user, se ejecuta el query
String userName = transaction.getUser().getName(); // ← Aquí se ejecuta query
```

### Con EAGER (No recomendado para ManyToOne):
```java
// Cargas la transacción - Ejecuta 2 queries automáticamente
Transaction transaction = transactionRepository.findById(1L);
// Query 1: SELECT * FROM transaction WHERE id = 1
// Query 2: SELECT * FROM user WHERE id = user_id (automático)
```

## Ventajas del LAZY:

### 🚀 **Mejor rendimiento**
- Solo trae datos cuando los necesitas
- Evita queries innecesarios

### 💾 **Menos memoria**
- No carga objetos que quizás no uses

### ⚡ **Queries más eficientes**
```java
// Si solo necesitas datos de la transacción:
List<Transaction> transactions = repo.findAll();
// Solo ejecuta 1 query, no trae todos los usuarios
```

## ⚠️ **Cuidado con el LazyInitializationException**

Si intentas acceder al `user` fuera del contexto de JPA:
```java
@Transactional
public void method1() {
    Transaction t = repo.findById(1L);
    return t; // user aún no cargado
}

// En otro método sin @Transactional:
t.getUser().getName(); // ❌ LazyInitializationException
```

## Resumen:
`LAZY` = "Tráeme el usuario solo cuando lo pida explícitamente"

¡Es la opción más eficiente para relaciones `@ManyToOne`! 👍
</details>

<details>
<summary><strong>💡ModelMapperConfig</strong></summary>

```java

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE)
                .setMatchingStrategy(MatchingStrategies.STANDARD);
        return modelMapper;
    }


```
# Explicación paso a paso de la configuración de ModelMapper en Spring

## 1. `@Configuration`
Esta anotación indica que la clase es una clase de configuración de Spring. Spring la utilizará para definir beans (componentes gestionados por el contenedor de Spring).

## 2. `public class ModelMapperConfig`
Es una clase Java donde defines la configuración para el bean `ModelMapper`.

## 3. `@Bean`
El método anotado con `@Bean` le dice a Spring que el objeto retornado debe ser gestionado como un bean y estará disponible para inyección de dependencias en otras partes de la aplicación.

## 4. `public ModelMapper modelMapper()`
Es el método que crea y configura una instancia de `ModelMapper`.

---

## Configuración de ModelMapper

- `setFieldMatchingEnabled(true)`: Permite que ModelMapper haga mapeo directamente entre campos (no solo getters/setters).
- `setFieldAccessLevel(PRIVATE)`: Permite que ModelMapper acceda a campos privados.
- `setMatchingStrategy(MatchingStrategies.STANDARD)`: Usa la estrategia estándar de coincidencia de campos.

---

## ¿Para qué sirve ModelMapper?

ModelMapper es una librería que ayuda a mapear (copiar datos) entre objetos, por ejemplo, de una entidad a un DTO (Data Transfer Object) y viceversa. Esto es útil cuando quieres separar la lógica de negocio de la lógica de presentación o transporte de datos.

---

## ¿DTO?
Un DTO (Data Transfer Object) es un objeto simple cuyo propósito principal es transportar datos entre diferentes capas de una aplicación, especialmente entre la capa de presentación (como una API o interfaz de usuario) y la capa de negocio o persistencia (como la base de datos).

### ¿Para qué sirve un DTO?

- **Evita exponer directamente las entidades internas** de la base de datos o del modelo de negocio, ayudando a mantener el encapsulamiento y la seguridad.
- **Permite controlar exactamente qué datos se envían y reciben** en las operaciones de la API, lo que ayuda a reducir el tamaño de las respuestas y solicitudes.
- **Facilita la validación y transformación de datos**, ya que puedes adaptar los datos según las necesidades de cada capa o consumidor.
- **Reduce el acoplamiento** entre las diferentes partes de la aplicación, permitiendo cambiar el modelo interno sin afectar a los consumidores externos.

### Ejemplo típico de uso de un DTO

Supón que tienes una entidad `Usuario` con muchos campos sensibles (como contraseñas, tokens, etc.), pero solo quieres enviar el nombre y el email al cliente. El DTO contendría solo esos campos necesarios.

---

**En resumen:**  
Un DTO sirve para transferir datos de manera segura, específica y eficiente entre diferentes capas o sistemas, sin exponer detalles internos innecesarios.

</details>

<details>
<summary><strong>💡CLASE 07 REPOSITORIES</strong> </summary>

```java

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM TransactionDTO t " +
            "WHERE YEAR(t.createdAt) = :year AND MONTH(t.createdAt) = :month")
    List<Transaction> findAllByMonthAndYear(@Param("month") int month, @Param("year") int year);


    //we are searching these field; TransactionDTO's description, note, status, ProductDTO's name, sku
    @Query("SELECT t FROM TransactionDTO t " +
            "LEFT JOIN t.product p " +
            "WHERE (:searchText IS NULL OR " +
            "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
            "LOWER(t.status) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
            "LOWER(p.sku) LIKE LOWER(CONCAT('%', :searchText, '%')))")
    Page<Transaction> searchTransactions(@Param("searchText") String searchText, Pageable pageable);
}

```

Te explico qué hace cada parte de tu `TransactionRepository`:

## 📋 **Interfaz Base**
```java
public interface TransactionRepository extends JpaRepository<Transaction, Long>
```
- Extiende `JpaRepository` para operaciones CRUD básicas
- `Transaction`: entidad a manejar
- `Long`: tipo del ID de la entidad

## 🗓️ **Método 1: Buscar por Mes y Año**
```java
@Query("SELECT t FROM TransactionDTO t " +
        "WHERE YEAR(t.createdAt) = :year AND MONTH(t.createdAt) = :month")
List<Transaction> findAllByMonthAndYear(@Param("month") int month, @Param("year") int year);
```

### ¿Qué hace?
- Busca todas las transacciones de un **mes y año específicos**
- Usa funciones SQL `YEAR()` y `MONTH()` para extraer fecha
- Retorna una `List<Transaction>`

### Ejemplo de uso:
```java
// Buscar transacciones de enero 2025
List<Transaction> transactions = repo.findAllByMonthAndYear(1, 2025);
```
## EN CONTEXTO DE SQL SERIA ASI :
Claro. La consulta JPQL que diste se puede transformar a SQL estándar así:

```sql
SELECT * FROM transaction
WHERE YEAR(created_at) = :year AND MONTH(created_at) = :month;
```

- `transaction` es el nombre de la tabla.
- `created_at` es el campo de fecha (ajusta el nombre si es diferente en tu base de datos).
- `:year` y `:month` son parámetros que debes reemplazar por los valores deseados.

## PORSICASO EN SQL NO ES VALIDO EL " = : "
No, en SQL estándar no puedes usar `:year` y `:month` directamente.  
Los dos puntos (`:`) indican **parámetros nombrados** y solo son válidos en JPQL/HQL o 
frameworks como JPA/Hibernate.

En SQL puro, debes reemplazar esos parámetros por valores concretos o usar `?` 
para parámetros posicionales (en JDBC):

```sql
SELECT * FROM transaction
WHERE YEAR(created_at) = ? AND MONTH(created_at) = ?;
```

O reemplazar manualmente:

```sql
SELECT * FROM transaction
WHERE YEAR(created_at) = 2025 AND MONTH(created_at) = 6;
```

**Resumen:**  
`:year` y `:month` no son válidos en SQL puro, solo en consultas parametrizadas de frameworks.

---

## 🔍 **Método 2: Búsqueda Avanzada con Paginación**
```java
@Query("SELECT t FROM TransactionDTO t " +
        "LEFT JOIN t.product p " +
        "WHERE (:searchText IS NULL OR " +
        "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
        "LOWER(t.status) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
        "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
        "LOWER(p.sku) LIKE LOWER(CONCAT('%', :searchText, '%')))")
Page<Transaction> searchTransactions(@Param("searchText") String searchText, Pageable pageable);
```

### ¿Qué hace?
**1. JOIN con Productos:**
- `LEFT JOIN t.product p` - Une transacciones con sus productos

**2. Búsqueda Flexible:**
Busca el texto en **4 campos diferentes**:
- ✅ `t.description` (descripción de transacción)
- ✅ `t.status` (estado de transacción)
- ✅ `p.name` (nombre del producto)
- ✅ `p.sku` (código del producto)

**3. Características:**
- **Case-insensitive:** `LOWER()` ignora mayúsculas/minúsculas
- **Búsqueda parcial:** `LIKE '%texto%'` busca coincidencias parciales
- **Null-safe:** `(:searchText IS NULL OR ...)` maneja valores nulos
- **Paginado:** Retorna `Page<Transaction>` para manejar grandes resultados

### Ejemplo de uso:
```java
// Buscar "iphone" en cualquier campo relevante, página 0, 10 resultados
Pageable pageable = PageRequest.of(0, 10);
Page<Transaction> results = repo.searchTransactions("iphone", pageable);

// También funciona con null (trae todas)
Page<Transaction> all = repo.searchTransactions(null, pageable);
```

## 🎯 **Casos de Uso Reales:**

```java
// Reporte mensual
List<Transaction> octubre2025 = repo.findAllByMonthAndYear(10, 2025);

// Búsqueda de usuario: "laptop"
// Encontrará transacciones que contengan "laptop" en:
// - descripción: "Compra de laptop gaming"
// - estado: si tuviera "laptop" (poco probable)
// - nombre producto: "Laptop Dell XPS"
// - SKU producto: "LAPTOP-001"
```

## ✨ **Fortalezas del código:**
- 🔄 **Reutilizable** y **flexible**
- 🚀 **Eficiente** con paginación
- 🔍 **Búsqueda potente** en múltiples campos
- 📊 **Ideal para reportes** y **filtros de usuario**

---

## DETALLE DEL QUERY 

La consulta JPQL transformada a SQL estándar para que la entiendas mejor:

```sql
     
SELECT t.*
FROM transaction t
LEFT JOIN product p ON t.product_id = p.id
WHERE (
    ? IS NULL OR
    LOWER(t.description) LIKE LOWER(CONCAT('%', ?, '%')) OR
    LOWER(t.status) LIKE LOWER(CONCAT('%', ?, '%')) OR
    LOWER(p.name) LIKE LOWER(CONCAT('%', ?, '%')) OR
    LOWER(p.sku) LIKE LOWER(CONCAT('%', ?, '%'))
)
     
```

- `?` representa el parámetro de búsqueda (`searchText`).
- `t.product_id = p.id` asume que la relación es por ese campo (ajústalo si tu FK es diferente).
- Así, busca el texto en descripción, estado, nombre y SKU del producto, ignorando mayúsculas/minúsculas y permitiendo búsquedas parciales.
- Si el parámetro es `NULL`, trae todos los resultados.



```sql

LOWER(t.description) LIKE LOWER(CONCAT('%', ?, '%'))
```

Claro. Esta expresión se usa en SQL para hacer una búsqueda de texto **sin distinguir mayúsculas/minúsculas** y permitiendo coincidencias parciales.

- `LOWER(t.description)`: Convierte el valor de la columna `description` a minúsculas.
- `LOWER(CONCAT('%', ?, '%'))`: Convierte el texto de búsqueda (el parámetro `?`) a minúsculas y le agrega `%` antes y después, para buscar cualquier coincidencia que contenga ese texto en cualquier parte.
- `LIKE`: Compara ambos valores y verifica si hay coincidencia parcial.

**¿Qué logra?**  
Busca todas las filas donde la descripción contenga el texto buscado, sin importar si está en mayúsculas o minúsculas.

**Ejemplo:**  
Si buscas `LapTop`, encontrará descripciones como `laptop nueva`, `LAPTOP usada`, `Accesorios para Laptop`, etc.

---
Claro. Supón que tienes los siguientes datos en la base de datos:

- Transaction 1:
    - description: "Compra de laptop"
    - status: "COMPLETADO"
    - Product name: "Laptop Dell"
    - Product sku: "DL-123"

- Transaction 2:
    - description: "Venta de mouse"
    - status: "PENDIENTE"
    - Product name: "Mouse Logitech"
    - Product sku: "LG-456"

Si llamas al método así:

```java
Page<Transaction> resultados = transactionRepository.searchTransactions("laptop", pageable);
```

El resultado incluirá la Transaction 1, porque "laptop" aparece en la descripción y en el nombre del producto, sin importar mayúsculas o minúsculas.

Si llamas con `null`:

```java
Page<Transaction> resultados = transactionRepository.searchTransactions(null, pageable);
```

El resultado incluirá todas las transacciones, porque la condición `:searchText IS NULL` se cumple y no se filtra nada.

---

</details>
<details>
<summary><strong>💡CLASE 08 DTO</strong> </summary>

```java
@JsonIgnoreProperties(ignoreUnknown = true)

```

> La anotación `@JsonIgnoreProperties(ignoreUnknown = true)` se usa para indicar que, 
> al deserializar un JSON a un objeto Java, se ignoren las propiedades desconocidas 
> (es decir, las que no existen en la clase). Así, si el JSON tiene campos extra que
> tu clase no define, no lanzará error y simplemente los omitirá. Esto es útil para 
> hacer tu API más tolerante a cambios o datos adicionales.
> 
>
---

## Ejemplo de cómo funciona `@JsonIgnoreProperties(ignoreUnknown = true)` en la clase `TransactionRequest`:

Supón que tu clase es así:

```java
// src/main/java/com/george/invetorymanagementsystem/dto/TransactionRequest.java
package com.george.invetorymanagementsystem.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionRequest {
    private String description;
    private Double amount;
}
```

Si recibes este JSON:

```json
{
  "description": "Compra de laptop",
  "amount": 1500.0,
  "extraField": "valor que no existe en la clase"
}
```

El campo `extraField` será ignorado al convertir el JSON a un objeto `TransactionRequest`, y no lanzará error. Solo se asignarán los valores de `description` y `amount`.
---
La anotación `@Positive` asegura que el valor de `quantity` sea mayor que cero.  
Ejemplo de uso en un controlador:

```java
// src/main/java/com/george/invetorymanagementsystem/controller/TransactionController.java
@PostMapping("/transactions")
public ResponseEntity<String> createTransaction(@Valid @RequestBody TransactionRequest request) {
    // Si quantity es <= 0, Spring devolverá un error de validación automáticamente
    return ResponseEntity.ok("Transacción creada correctamente");
}
```

Si envías este JSON:

```json
{
  "productId": 1,
  "quantity": -5
}
```

La respuesta será un error de validación con el mensaje:  
`Quantity id is requered` (porque -5 no es positivo).

---

La anotación `@JsonInclude(JsonInclude.Include.NON_NULL)` indica que, al convertir un objeto Java a JSON, solo se incluirán los campos que no sean `null`. Si un campo es `null`, no aparecerá en el JSON resultante.

**Ejemplo:**

Supón que tienes esta clase:

```java
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryDTO {
    private Long id;
    private String name;
    private String description;
}
```

Y creas un objeto así:

```java
CategoryDTO dto = new CategoryDTO();
dto.setId(1L);
dto.setName("Electrónica");
// dto.setDescription(null); // No se asigna valor

// Al serializar a JSON:
```

El JSON resultante será:

```json
{
  "id": 1,
  "name": "Electrónica"
}
```

El campo `description` no aparece porque es `null`. Esto ayuda a generar JSONs más limpios y compactos.


## CREAMOS DTOS

![img](/images/dtos.png)

## CONSIDERAR
La anotación `@JsonIgnore` se usa para que el campo `password` no se incluya al convertir el objeto a JSON. Así, cuando envías o recibes datos de usuario en la API, la contraseña no se muestra ni se expone por seguridad.

````java
    @JsonIgnore
    private String password;
````

Ejemplo:  
Si tienes este objeto:

```java
UserDTO user = new UserDTO();
user.setId(1L);
user.setName("Juan");
user.setPassword("secreta123");
```

El JSON generado será:

```json
{
  "id": 1,
  "name": "Juan"
}
```

El campo `password` no aparece en el JSON. Esto ayuda a proteger información sensible.

</details>

<details>
<summary><strong>💡CLASE 09 EXCEPTIONS</strong> </summary>

Te explico cada componente del directorio `exceptions`:

## 1. CustomAccessDeniedHandler.java

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

## 2. CustomAuthenticationEntryPoint.java

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

## 3. GlobalExceptionHandler.java

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

## 4. Excepciones Personalizadas

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

## Flujo completo de manejo de errores:

1. **Error de validación** → `GlobalExceptionHandler` → Respuesta JSON 400
2. **Usuario no autenticado** → `CustomAuthenticationEntryPoint` → Respuesta JSON 401
3. **Usuario sin permisos** → `CustomAccessDeniedHandler` → Respuesta JSON 403
4. **Recurso no encontrado** → `NotFoundException` → `GlobalExceptionHandler` → Respuesta JSON 404

Este sistema garantiza respuestas consistentes y manejables desde el frontend.

</details>

<details>
<summary><strong>💡CLASE 10 SECURITY CONFIG</strong> </summary>

# 🔒 Spring Boot Security: Explicación Detallada del Código `CustomUserDetailsService`

Este código define un **servicio personalizado de autenticación** en una aplicación Spring Boot utilizando Spring Security. Sirve para cargar los detalles de un usuario desde la base de datos, útil en procesos de login. A continuación, se describe **cada elemento con ejemplos y emojis** para mejor comprensión:

---

## 📦 Imports

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

## 🏷️ Decoradores y Definición de Clase

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

## 🏡 Inyección de Dependencias

```java
@Autowired
private UserRepository userRepository;
```

- **@Autowired** 🧩: Pide a Spring que inyecte automáticamente el repositorio de usuarios.
- **userRepository**: Objeto para consultar usuarios en BD.

---

## ⚡ Sobrescritura de Método

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

## 💡 Ejemplo Completo

Supongamos que tienes un login con el correo y clave. El sistema usará este servicio cuando llamas al endpoint de autenticación.   
**Ejemplo de flujo:**

1. Usuario intenta iniciar sesión con `usuario@ejemplo.com`.
2. Spring Security invoca `CustomUserDetailsService.loadUserByUsername("usuario@ejemplo.com")`.
3. Se busca el usuario en la BD.
4. Si existe, se crea un objeto `AuthUser` que implementa UserDetails.
5. Si no existe, se lanza "User Email Not Found!".

---

## 🛠️ Ejemplo de AuthUser

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

## 🔑 Resumen

- 📚 Este servicio personaliza cómo buscar usuarios al autenticar.
- 👤 Implementa la lógica de Spring Security buscando por email.
- 💥 Lanza error si no existe el usuario.
- 🔑 Devuelve un `UserDetails` usado internamente para seguridad.

---

## 📋 Referencia rápida

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
# 👤 Clase `AuthUser` en Spring Boot Security

Esta clase es la implementación personalizada de `UserDetails` que adapta tu entidad propia de usuario (`User`) al modelo interno de autenticación de **Spring Security**. Aquí se explican **cada elemento** y su función, usando emojis y ejemplos claros para facilitar la comprensión.

---

## 📦 Imports

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

## 🏷️ Anotaciones de Clase

```java
@Data
@Builder
public class AuthUser implements UserDetails
```

- **@Data** 📝: Lombok genera automáticamente getters, setters, equals, hashCode y toString.
- **@Builder** 🧱: Lombok habilita el patrón builder para instanciar fácilmente objetos de esta clase.
- **implements UserDetails**: Obliga a implementar métodos que Spring Security necesita para autenticar y autorizar usuarios.

---

## 🧩 Atributo Interno

```java
private User user;
```
- **user** 👤: Instancia de tu entidad de usuario. Contiene toda la información relevante como email, contraseña, roles, etc.

---

## 🔑 Métodos sobrescritos de `UserDetails`

1. ## 🔗 Autoridades

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

2. ## 🔒 Password

    ```java
    @Override
    public String getPassword() {
        return user.getPassword();
    }
    ```
    - Retorna la contraseña almacenada (debe estar hasheada).

3. ## 📧 Username

    ```java
    @Override
    public String getUsername() {
        return user.getEmail();
    }
    ```
    - Retorna el identificador único del usuario, **en este caso el email**.
    - Spring Security tomará este valor para hacer el login.

4. ## ⏳ ¿Cuenta Expirada?

    ```java
    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }
    ```
    - Indica si la cuenta ha expirado. Por defecto, retornará `true`.
    - **Tip:** Puedes personalizar para manejar lógicas de expiración.

5. ## 🚪 ¿Cuenta Bloqueada?

    ```java
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    ```
    - Siempre retorna `true`, significa que la cuenta nunca estará bloqueada.
    - **Tip:** Puedes cambiar esto según lógica de negocio (ej: muchos intentos fallidos).

6. ## 🔐 ¿Credenciales Expiradas?

    ```java
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    ```
    - Siempre `true`. Cambia si quieres forzar cambio de contraseña periódicamente.

7. ## ✅ ¿Cuenta Habilitada?

    ```java
    @Override
    public boolean isEnabled() {
        return true;
    }
    ```
    - Siempre `true`, indica que la cuenta está habilitada.
    - Puedes condicionar esto, por ejemplo, si el usuario no ha verificado el email.

---

## 🛠️ Ejemplo Práctico de Uso

Cuando Spring Security necesita autenticar un usuario, va a convertir la entidad de tu base de datos en un objeto `AuthUser`:

```java
User user = userRepository.findByEmail("usuario@ejemplo.com").get();
AuthUser authUser = AuthUser.builder().user(user).build();

String correo = authUser.getUsername();       // "usuario@ejemplo.com"
String clave = authUser.getPassword();        // "***hash***"
List<GrantedAuthority> roles = (List<GrantedAuthority>) authUser.getAuthorities(); // [SimpleGrantedAuthority("ADMIN")]
```

---

## 🧑‍💻 Comentarios Adicionales

- Así, cualquier lógica adicional (roles, expiración, bloqueo) puede ser controlada aquí y Spring Security la integrará automáticamente.
- Si quieres agregar más campos o controles, aquí es el lugar centralizado para hacerlo.

---

## 📋 Tabla Resumen

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
## Algunos Ejemplos de Uso Común de la Clase `AuthUser` de bloqueo 

# 🧑‍💻 Ejemplos prácticos de uso de `AuthUser` en seguridad Spring Boot

A continuación te presento ejemplos que puedes copiar directamente para entender y probar cómo se usa y cómo puedes personalizar la clase `AuthUser`:

---

## 🌐 Ejemplo básico: uso en el flujo de autenticación

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

## 🛠️ Ejemplo de creación manual de `AuthUser`

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

## 🔗 Control de permisos usando roles

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

## 🔒 Ejemplo de cuenta bloqueada (personalizado)

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

## ✅ Ejemplo de cuenta habilitada (personalizado)

Supón que tu entidad `User` tiene un campo `boolean enabled` (usuario activado/desactivado):

```java
@Override
public boolean isEnabled() {
    return user.isEnabled();
}
```

---

## ⏳ Ejemplo de cuenta expirada (personalizado)

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

# 🛡️ Explicación Detallada de `AuthFilter` (Spring Security, JWT)

Este filtro personalizado (`AuthFilter`) forma parte del sistema de autenticación **JWT** en tu aplicación Spring Boot. Se asegura de que cada petición HTTP verifique el token JWT del usuario antes de continuar con la lógica de la aplicación.

---

## 📦 Imports esenciales

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

## 🏷️ Anotaciones y Definición de Clase

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

## 🔗 Variables Inyectadas

```java
private final JwtUtils jwtUtils;
private final CustomUserDetailsService customUserDetailsService;
```
- **jwtUtils** 🧾: Lógica para validación y parsing de JWT.
- **customUserDetailsService** 👤: Permite cargar los detalles del usuario desde la base de datos, usando email extraído del token.

---

## 🔍 Método Principal: `doFilterInternal`

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

## 🧪 Ejemplo de uso

Supón que un frontend hace:
```
GET /api/usuarios
Authorization: Bearer eyJhbGciOi...
```
- `AuthFilter` procesa el JWT, valida, y, si es correcto, la petición continúa como autenticada y con los roles del usuario.
- Si el JWT es inválido, no se establece autenticación y pueden saltar errores 401/403 según configuración.

---

## 🏷️ Método Auxiliar: `getTokenFromRequest`

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

## 📋 Tabla resumen

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

## 🧑‍💻 Ejemplo de personalización

- **Podrías negar acceso si el usuario está inhabilitado**: comprueba si userDetails.isEnabled() antes de crear el UsernamePasswordAuthenticationToken.
- **Registrar logs detallados**: usando `log.info`.
- **Controlar excepciones personalizadas**: lanzar errores o responder 401 según reglas de negocio.

---

# 🛡️ Explicación Detallada de `SecurityFilter` (Spring Security Configuration)

Esta clase configura la seguridad global en tu aplicación **Spring Boot** usando Spring Security. Aquí se administran reglas de acceso, manejo de tokens JWT, gestión de excepciones y filtros de seguridad.

---

## 📦 Imports Clave

- **Spring Security:** Manejo de seguridad, filtros, autenticación.
- **Lombok:** Anotaciones para inyección y logging.
- **Custom Handlers:** Personalizan respuestas ante accesos denegados y errores de autenticación.
- **JWT AuthFilter:** Filtro personalizado que valida cada request usando tokens JWT.

---

## 🏷️ Anotaciones y Definición

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

## 🔌 Inyección de Dependencias

```java
private final AuthFilter authFilter;
private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
private final CustomAccessDeniedHandler customAccessDeniedHandler;
```

- **AuthFilter**: Filtro personalizado donde se valida cada JWT.
- **CustomAuthenticationEntryPoint**: Lógica personalizada cuando un usuario NO autenticado intenta acceder.
- **CustomAccessDeniedHandler**: Gestión personalizada cuando un usuario autenticado intenta acceder a un recurso prohibido.

---

## 🔗 Método `securityFilterChain`

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

## 🔑 Beans Adicionales

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

## 🧪 Ejemplo práctico de flujo

1. Cliente hace **POST /api/auth/login**: acceso permitido sin token.
2. Cliente recibe un JWT.
3. Cliente hace **GET /api/usuarios** con JWT:
    - `authFilter` valida el token.
    - Si es válido, la request sigue autenticada.
    - Si no es válido o falta, intervienen los handlers personalizados (`CustomAuthenticationEntryPoint`, etc.).
4. Si pide un recurso protegido sin JWT, recibe 401 o 403 según el caso.

---

## 📋 Tabla Resumen

| Elemento                        | Icono | Breve descripción                                                      |
|----------------------------------|-------|-----------------------------------------------------------------------|
| @EnableWebSecurity               | 🔐    | Habilita la protección de endpoints web                               |
| @EnableMethodSecurity            | 🚥    | Permite anotaciones como @PreAuthorize/@Secured en tus controladores  |
| SessionCreationPolicy.STATELESS  | 📦    | Sin sesiones, ideal para JWT                                          |
| addFilterBefore(AuthFilter, ...) | 🛡️    | Filtro JWT se ejecuta antes del filtro de Spring                      |
| PasswordEncoder (BCrypt)         | 🔑    | Hash seguro para contraseñas                                          |
| Custom handlers                  | ⚠️    | Personalizas mensajes de error/autorización                           |

---

# 🔑 Explicación Detallada de `JwtUtils` (JWT Utility para Spring Security)

Esta clase en tu proyecto sirve como **herramienta centralizada para la generación, validación y extracción de información** desde tokens JWT (JSON Web Token), que es la base estándar para autenticación stateless en modern apps.

---

## 📦 Imports Clave

- **JJWT (io.jsonwebtoken):** Librería popular para manejar JWT en Java/Spring.
- **Spring Security:** Facilita la integración con el modelo de usuario de seguridad.
- **Lombok (`@Slf4j`):** Añade un logger de manera automática.
- **@Service:** Marca el componente como un servicio para Spring.

---

## 🚀 Propiedades y Variables

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

## 🔄 Inicialización (`@PostConstruct`)

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

## 🪄 Método: `generateToken(String email)`

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

## 🧑‍💻 Método: `getUsernameFromToken(String token)`

```java
public String getUsernameFromToken(String token) {
    return extractClaims(token, Claims::getSubject);
}
```
- Extrae el "subject" (email) de un JWT.

---

## 🕵️ Método genérico: `extractClaims(...)`

```java
private <T> T extractClaims(String token, Function<Claims, T> claimsTFunction) {
    return claimsTFunction.apply(Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload());
}
```
- Abre, valida y decodifica el JWT usando la clave.
- Usa una función (por ejemplo, para obtener el email o la expiración) sobre los claims.
- 🛡️ Si la firma o token es inválido/llegó corrupto, lanzará excepción.

---

## ✅ Método: `isTokenValid(String token, UserDetails userDetails)`

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

## ⏳ Método Privado: `isTokenExpired(String token)`

```java
private boolean isTokenExpired(String token) {
    return extractClaims(token, Claims::getExpiration).before(new Date());
}
```
- 📆 Revisa si la fecha de expiración (`expiration`) ya pasó.

---

## 🧪 Ejemplo de Uso

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

## 📋 Tabla Resumida

| Elemento                        | Icono | Breve explicación                                   |
|----------------------------------|-------|-----------------------------------------------------|
| SecretKey + secreteJwtString     | 🔑🛡️  | Seguridad: solo tu app puede firmar/verificar tokens|
| generateToken                    | 🪄    | Crea JWT con duración, subject, y firma secreta     |
| getUsernameFromToken             | 🧑‍💻 | Obtiene email/username codificado en el JWT         |
| isTokenValid                     | ✅    | Controla que el token pertenezca al usuario y no expire |
| isTokenExpired                   | ⏳    | Expiración de la validez del token                  |
| extractClaims                    | 🕵️   | Método genérico para leer cualquier claim           |

---

## 🚨 Buenas Prácticas & Consejos

- **Guarda** `secreteJwtString` en variables de entorno o archivos seguros.
- **No compartas** la clave secreta.
- **Cambia la expiración** según tus necesidades (6 meses puede ser mucho para apps públicas).

---

# 🌎 Explicación Detallada de `CorsConfig` (CORS en Spring Boot)

Esta clase configura las reglas CORS (**Cross-Origin Resource Sharing**) en tu API de Spring Boot, permitiendo (o restringiendo) el acceso a tu backend desde otros dominios/frontends. Es muy importante para exponer APIs a aplicaciones cliente en distintos orígenes (puertos, dominios).

---

## 📦 IMPORTS CLAVE

- **WebMvcConfigurer:** Interfaz de configuración para funcionalidades web en Spring.
- **CorsRegistry:** Clase para registrar reglas CORS.
- **@Configuration:** Marca la clase como configuración de Spring Boot.

---

## 🏷️ ANOTACIONES

```java
@Configuration
public class CorsConfig { ... }
```
- **@Configuration** 🏗️: Esta clase define configuraciones que Spring Boot aplica automáticamente al arrancar la app.

---

## ⚙️ MÉTODO PRINCIPAL

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

## 🧑‍💻 EJEMPLO DE USO

Si tienes:
- **Frontend** en `http://localhost:3000`
- **Backend** en `http://localhost:8080`

Esto es lo que permite esta configuración:
- Peticiones `GET`, `POST`, `PUT`, `DELETE` desde **cualquier** dominio/puerto.
- Se suele usar para desarrollo o APIs públicas.
- Para mayor _seguridad_, restringe a `.allowedOrigins("http://localhost:3000")`

---

## 🛠️ CÓMO MEJORAR (con @Bean)

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

## 📋 TABLA RESUMEN

| Elemento               | Icono | Explicación                                     |
|------------------------|-------|-------------------------------------------------|
| @Configuration         | 🏗️   | Declara la clase como configuración de Spring   |
| addMapping("/**")      | ✨    | Aplica reglas a todos los endpoints             |
| allowedMethods(...)    | 🚦    | Permite solo ciertos métodos HTTP               |
| allowedOrigins("*")    | 🌍   | Permite cualquier origen                        |
| @Bean                  | 🫘    | Registra el configurador como bean de Spring    |

---
# RESUMEN TOTAL
# 🎓 Relación entre Clases de Spring Security: Guía Completa del Profesor

¡Hola! Como tu profesor con alta seniority en Java y Spring Security, te voy a explicar paso a paso cómo **todas estas clases trabajan juntas** para crear un sistema de autenticación JWT completo y robusto.

---

## 🧩 VISTA GENERAL: ¿Qué hace cada clase?

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

## 🔄 FLUJO COMPLETO: De la Petición a la Respuesta

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

## 🔗 RELACIONES DETALLADAS

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

## 📖 EJEMPLO PASO A PASO: Login de Usuario

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

## 🚀 EJEMPLO: Request Protegido

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

## 🎨 DIAGRAMA DE FLUJO COMPLETO

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

## 💡 PUNTOS CLAVE PARA RECORDAR

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

## 🚨 ERRORES COMUNES DE PRINCIPIANTES

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

## 🎓 EJERCICIO PARA PRACTICAR

**Crea un endpoint de logout que:**
1. Reciba un JWT
2. Lo valide con JwtUtils
3. Agregue el token a una "blacklist"
4. Modifique AuthFilter para rechazar tokens en blacklist

**Pista:** Necesitarás modificar **JwtUtils** y **AuthFilter** 😉

---

¿Te queda claro cómo todas las piezas encajan? ¡Pregúntame cualquier duda específica! 🚀

</details>