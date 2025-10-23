# PROYECTO INVENTARIO SPRINGBOOT

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