## 💡CLASE 08 DTO

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

### Ejemplo de cómo funciona `@JsonIgnoreProperties(ignoreUnknown = true)` en la clase `TransactionRequest`:

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


### CREAMOS DTOS

![img](images/dto.png)

### CONSIDERAR
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
