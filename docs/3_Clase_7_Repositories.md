## 💡CLASE 07 REPOSITORIES

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

### 📋 **Interfaz Base**
```java
public interface TransactionRepository extends JpaRepository<Transaction, Long>
```
- Extiende `JpaRepository` para operaciones CRUD básicas
- `Transaction`: entidad a manejar
- `Long`: tipo del ID de la entidad

### 🗓️ **Método 1: Buscar por Mes y Año**
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
### EN CONTEXTO DE SQL SERIA ASI :
Claro. La consulta JPQL que diste se puede transformar a SQL estándar así:

```sql
SELECT * FROM transaction
WHERE YEAR(created_at) = :year AND MONTH(created_at) = :month;
```

- `transaction` es el nombre de la tabla.
- `created_at` es el campo de fecha (ajusta el nombre si es diferente en tu base de datos).
- `:year` y `:month` son parámetros que debes reemplazar por los valores deseados.

### PORSICASO EN SQL NO ES VALIDO EL " = : "
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

### 🔍 **Método 2: Búsqueda Avanzada con Paginación**
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

### 🎯 **Casos de Uso Reales:**

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

### ✨ **Fortalezas del código:**
- 🔄 **Reutilizable** y **flexible**
- 🚀 **Eficiente** con paginación
- 🔍 **Búsqueda potente** en múltiples campos
- 📊 **Ideal para reportes** y **filtros de usuario**

---

### DETALLE DEL QUERY

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

### Explicacion adicional
### ✅ Exactamente, `:searchText` es el parámetro

Es como una **"caja vacía"** que espera recibir el valor que tú le mandes.

---

### 🔗 El flujo completo del parámetro

```java
//        Aquí defines la caja y le pones nombre
//                      ↓
Page<Transaction> searchTransactions(@Param("searchText") String searchText, Pageable pageable);
//                                                         ↑
//                                             Aquí llega el texto del usuario
```

```java
// En el query, aquí se "inserta" el valor dentro del SQL
WHERE LOWER(t.description) LIKE LOWER(CONCAT('%', :searchText, '%'))
//                                              ↑
//                                    Spring reemplaza esto
//                                    con el valor real
```

---

### 🧪 Ejemplo del reemplazo

Cuando llamas el método así:

```java
repo.searchTransactions("laptop", pageable);
```

Spring internamente convierte el query a esto:

```sql
-- Lo que tú escribiste:
LIKE LOWER(CONCAT('%', :searchText, '%'))

-- Lo que Spring ejecuta realmente:
LIKE LOWER(CONCAT('%', 'laptop', '%'))
--  lo que resulta en →  '%laptop%'
```

---

### 📦 El `@Param` es el conector

```java
                     // ① Nombre en el query       // ② Variable Java
Page<Transaction> searchTransactions(@Param("searchText") String searchText)
//                                         ↑                      ↑
//                                   :searchText   ←←←←   recibe "laptop"
//                                   en el SQL
```

El `@Param("searchText")` le dice a Spring:

> *"El valor de la variable Java `searchText` va donde diga `:searchText` en el query"*

---

### 🚨 ¿Qué pasaría si mandas `null`?

```java
repo.searchTransactions(null, pageable);
```

Por eso existe esta parte del query:

```sql
WHERE (:searchText IS NULL   -- ← si es null, esta condición es TRUE
OR                           --   y con OR, ya no evalúa las demás
LOWER(t.description) LIKE ...
```

```
null IS NULL → true
   ↓
El OR ya es true sin revisar nada más
   ↓
Trae TODAS las transacciones ✅
```

---

### 🎯 Resumen simple

```
Usuario escribe → "laptop"
        ↓
String searchText = "laptop"    ← variable Java
        ↓
@Param("searchText")            ← conecta variable con query
        ↓
:searchText en el SQL           ← aquí se inserta "laptop"
        ↓
LIKE '%laptop%'                 ← busca en los 4 campos
```

Es básicamente como una **plantilla** donde `:searchText` es el espacio en blanco que se rellena con lo que el usuario escriba. 📝

### 🔍 Cómo busca en 4 campos simultáneamente

La clave está en el operador **`OR`**. SQL evalúa cada condición y si **cualquiera** es verdadera, incluye el registro.

---

### 🧠 Piénsalo como una compuerta lógica

```
¿"laptop" está en description?  →  ✅ SÍ  → INCLUIR fila
¿"laptop" está en status?        →  ❌ NO  →   |
¿"laptop" está en p.name?        →  ❌ NO  →   |
¿"laptop" está en p.sku?         →  ❌ NO  →   |
                                              ↓
                                    Resultado: ✅ INCLUIDA
                                    (basta con UN campo verdadero)
```

SQL no "para" cuando encuentra el primero — **evalúa los 4 siempre**, pero con que uno sea `true`, la fila entra.

---

### 📍 ¿Cómo busca en "cualquier posición"?

Eso lo hace el patrón `'%texto%'`:

```sql
LOWER(t.description) LIKE LOWER(CONCAT('%', :searchText, '%'))
--                                      ↑            ↑
--                               % = cualquier    % = cualquier
--                                 cosa ANTES       cosa DESPUÉS
```

El `%` es un **comodín** que significa *"cualquier cantidad de caracteres"*:

```
Buscas: "lap"

'%lap%'  encuentra:
  ✅  "lap"top nueva"        → lap al inicio
  ✅  "Mi nueva lap"top"     → lap en el medio
  ✅  "Compré un portátilap" → lap al final
  ✅  "LAPTOP gaming"        → porque LOWER() lo convierte a "laptop"
  ❌  "computadora"          → no contiene "lap" en ningún lado
```

---

### 🔬 Disección completa del query

```sql
SELECT t FROM Transaction t        -- 1. Trae transacciones
LEFT JOIN t.product p              -- 2. Une con su producto (si tiene)
WHERE (
    :searchText IS NULL            -- 3. Si no hay texto → trae TODO
    OR
    LOWER(t.description)           -- 4. Convierte description a minúsculas
        LIKE                       --    y compara con el patrón
        LOWER(CONCAT('%','lap','%'))  -- → '%lap%'
    OR
    LOWER(t.status)                -- 5. Mismo proceso con status
        LIKE LOWER(CONCAT('%','lap','%'))
    OR
    LOWER(p.name)                  -- 6. Mismo proceso con nombre del producto
        LIKE LOWER(CONCAT('%','lap','%'))
    OR
    LOWER(p.sku)                   -- 7. Mismo proceso con SKU del producto
        LIKE LOWER(CONCAT('%','lap','%'))
)
```

---

### 🧪 Ejemplo con datos reales

Imagina esta BD, buscas `"lap"`:

```
TRANSACTION table:
┌────┬─────────────────────┬───────────┬────────────┐
│ ID │ description         │ status    │ product_id │
├────┼─────────────────────┼───────────┼────────────┤
│  1 │ "Compra de laptop"  │ COMPLETED │     10     │ ✅ description contiene "lap"
│  2 │ "Venta de mouse"    │ COMPLETED │     20     │ ❌ ningún campo coincide
│  3 │ "Devolucion item"   │ COMPLETED │     30     │ ✅ product.name = "Laptop Dell"
│  4 │ "Ajuste de stock"   │ COMPLETED │     40     │ ✅ product.sku = "LAP-001"
└────┴─────────────────────┴───────────┴────────────┘

PRODUCT table:
┌────┬──────────────┬─────────┐
│ ID │ name         │ sku     │
├────┼──────────────┼─────────┤
│ 10 │ "Mouse USB"  │ MS-001  │ (fila 1 ya entró por description)
│ 20 │ "Teclado"    │ TEC-002 │ ❌
│ 30 │ "Laptop Dell"│ DL-500  │ ✅ p.name contiene "lap"
│ 40 │ "Monitor"    │ LAP-001 │ ✅ p.sku contiene "lap"
└────┴──────────────┴─────────┘

Resultado: filas 1, 3 y 4
```

---

### ⚠️ El papel del `LEFT JOIN`

```sql
LEFT JOIN t.product p
```

Esto es importante porque significa:

```
Transaction SIN producto →  p.name = NULL, p.sku = NULL
                            NULL LIKE '%lap%' = false  (no falla, solo no coincide)
                            La transacción igual puede entrar por description o status
```

Si fuera `INNER JOIN`, las transacciones sin producto quedarían **excluidas** automáticamente aunque su `description` coincida.

---

### 💡 Resumen visual

```
searchText = "lap"
       ↓
   LOWER() → "lap"
   CONCAT → '%lap%'
       ↓
┌─────────────────────────────────────────┐
│  description LIKE '%lap%'  → true/false │
│           OR                            │  → si alguno es true
│  status LIKE '%lap%'       → true/false │     la fila SE INCLUYE
│           OR                            │
│  p.name LIKE '%lap%'       → true/false │
│           OR                            │
│  p.sku LIKE '%lap%'        → true/false │
└─────────────────────────────────────────┘
```

La búsqueda es **inclusiva** — favorece mostrar resultados. Es el mismo concepto de cuando buscas en Google y coincide con cualquier palabra.


---

### ¿entonces cuando llame a ese repositorio se ejecutara esa
    query que he implementado yo manualmente ya que no lo ha hecho springboot sino que e smia?
```
🤖 Lo que Spring Boot hace AUTOMÁTICAMENTE
``` 

```java
// Spring LEE el nombre del método y genera el SQL solo
Page<Transaction> findByStatus(String status);
//                    ↑
//         Spring traduce esto a:
//         SELECT * FROM transaction WHERE status = ?

Page<Transaction> findByDescriptionContaining(String text);
//                         ↑
//         Spring traduce esto a:
//         SELECT * FROM transaction WHERE description LIKE '%?%'
```

Spring analiza el nombre del método y **adivina** el SQL.

---

### ✍️ Lo que tú hiciste con `@Query`

```java
// Le dices a Spring: "OLVIDA el nombre del método,
//                    ejecuta MI query"
@Query("SELECT t FROM Transaction t " +
        "LEFT JOIN t.product p " +
        "WHERE (:searchText IS NULL OR " +
        "LOWER(t.description) LIKE ...")
Page<Transaction> searchTransactions(@Param("searchText") String searchText, Pageable pageable);
//                ↑
//    El nombre ya no importa, Spring ignora "searchTransactions"
//    y ejecuta TU query del @Query
```

---

### 🧠 La regla simple

```
Sin @Query  →  Spring genera el SQL leyendo el nombre del método
Con @Query  →  Spring ejecuta TU SQL, el nombre del método da igual
```

---

### 🧪 Prueba de que el nombre no importa

```java
// Estos dos métodos hacen EXACTAMENTE lo mismo
// porque @Query manda sobre el nombre

@Query("SELECT t FROM Transaction t WHERE ...")
Page<Transaction> searchTransactions(...);

@Query("SELECT t FROM Transaction t WHERE ...")
Page<Transaction> miMetodoConNombreRaro(...);

@Query("SELECT t FROM Transaction t WHERE ...")
Page<Transaction> buscarCosas(...);
// ↑ Los 3 ejecutan el mismo SQL tuyo ✅
```

---

### 📦 Por eso usaste `@Query` y no el nombre automático

Porque lo que querías hacer era **demasiado complejo** para que Spring lo genere solo:

```java
// ❌ Spring NO puede generar esto solo con el nombre:
//    - LEFT JOIN con producto
//    - Buscar en 4 campos distintos
//    - LOWER() para ignorar mayúsculas
//    - NULL check
//    - Paginación sobre todo eso

// ✅ Por eso tú lo escribiste manualmente con @Query
```

Básicamente le quitaste el trabajo a Spring y dijiste:

> *"Yo sé exactamente qué query necesito, tú solo ejecútalo cuando llamen a este método"* 💪
---
