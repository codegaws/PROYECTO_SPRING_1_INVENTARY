## 💡Notas : Tabla BD

![image](/images/tabla.png)

### informacion de la tabla

¡**Exactamente**! 🎯

Sí, se pueden generar distintos IDs de transacciones para un usuario

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

### ¿Qué significa `FetchType.LAZY`?

**LAZY** = "Perezoso" o "Bajo demanda"

### Comportamiento:
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name="user_id")
private User user;
```

Con `LAZY`, el objeto `User` **NO se carga automáticamente** cuando cargas una `Transaction`. Solo se carga cuando **realmente lo necesitas**.

### Ejemplo práctico:

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

### Ventajas del LAZY:

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

### ⚠️ **Cuidado con el LazyInitializationException**

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

### Resumen:
`LAZY` = "Tráeme el usuario solo cuando lo pida explícitamente"

¡Es la opción más eficiente para relaciones `@ManyToOne`! 👍
