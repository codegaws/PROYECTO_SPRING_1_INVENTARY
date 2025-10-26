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
<summary><strong>ModelMapperConfig</strong></summary>

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

    @Query("SELECT t FROM Transaction t " +
            "WHERE YEAR(t.createdAt) = :year AND MONTH(t.createdAt) = :month")
    List<Transaction> findAllByMonthAndYear(@Param("month") int month, @Param("year") int year);


    //we are searching these field; Transaction's description, note, status, Product's name, sku
    @Query("SELECT t FROM Transaction t " +
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
@Query("SELECT t FROM Transaction t " +
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

## 🔍 **Método 2: Búsqueda Avanzada con Paginación**
```java
@Query("SELECT t FROM Transaction t " +
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

¡Muy bien implementado! 👏
</details>