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


</details>