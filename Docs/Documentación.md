# Sistema de Boleta Electrónica

---

## Parte 1: Bitácora de construcción

### Paso 1: Verificar Java y Maven
**Para qué:** Vaadin necesita Java 21 o superior; Maven construye el proyecto.
**Dónde:** terminal.
```
java -version
mvn -version
```
**Resultado:** Java 25 (Temurin) instalado. Maven no instalado → no es necesario, el proyecto trae Maven Wrapper (`mvnw`).

### Paso 2: Generar el proyecto base
**Para qué:** crear la estructura del proyecto con Spring Boot + Vaadin ya configurados.
**Dónde:** navegador → https://start.spring.io
| Opción | Valor |
|---|---|
| Project | Maven |
| Language | Java |
| Spring Boot | 4.1.1 (versión estable; no usar SNAPSHOT, M o RC porque son versiones en desarrollo) |
| Group | pe.vr |
| Artifact | boleta |
| Packaging | Jar |
| Java | 25 |
| Dependencias | Vaadin |

**Nota:** el Group no acepta símbolos como `&`, solo minúsculas, números y puntos.

### Paso 3: Copiar el proyecto a la carpeta
**Para qué:** dejar los archivos directamente en la carpeta del proyecto.
**Dónde:** Explorador de Windows.
Se copió el **contenido** de la carpeta `boleta` (dentro del zip) a `D:\eval\Boleta_electronica`.

### Paso 4: Ejecutar el proyecto por primera vez
**Para qué:** comprobar que Java, Maven Wrapper y Vaadin funcionan.
**Dónde:** terminal de VS Code, en `D:\eval\Boleta_electronica`.
```
.\mvnw.cmd spring-boot:run
```
**Resultado:** `Application running at http://localhost:8080/`. El navegador muestra "No views found" (normal, aún no hay pantallas).
**Detener la app:** `Ctrl + C` en la terminal.

### Paso 5: Crear la clase Producto (campos)
**Para qué:** definir los datos de cada producto del inventario.
**Dónde:** `src\main\java\pe\vr\boleta\model\Producto.java`
Campos: `codigo`, `descripcion`, `unidadMedida`, `precio`, `stock`.

### Paso 6: Constructor vacío, getters y setters
**Para qué:** poder leer y modificar los datos del producto. Firestore exige el constructor vacío.
**Dónde:** `Producto.java`

### Paso 7: Crear la documentación
**Para qué:** registrar el paso a paso de la construcción y describir el sistema.
**Dónde:** `Docs\Documentación.md`
**Ver con formato en VS Code:** `Ctrl + Shift + V`

---

### Paso 8: Servicio de productos (en memoria)
**Para qué:** guardar y listar productos. Luego se cambiará para usar Firestore sin tocar la pantalla.
**Dónde:** `src\main\java\pe\vr\boleta\service\ProductoService.java`
Métodos: `listar()` y `guardar(Producto)`. Al reiniciar la app los datos se borran (temporal).

### Paso 9: Pantalla "Productos" (solo título)
**Para qué:** crear la primera pantalla de la app y comprobar que Vaadin la reconoce.
**Dónde:** `src\main\java\pe\vr\boleta\views\ProductosView.java`
- `@Route("")` → se abre en la dirección principal (http://localhost:8080).
- `@PageTitle("Productos")` → texto de la pestaña del navegador.
- `extends VerticalLayout` → acomoda los elementos uno debajo de otro.

**Ejecutar** (en `D:\eval\Boleta_electronica`):
```
.\mvnw.cmd spring-boot:run
```
**Resultado:** en http://localhost:8080 aparece el título "Registro de Productos" (ya no sale "No views found").

### Paso 10: Formulario de productos y diseño
**Para qué:** que el dueño ingrese sus productos en una pantalla clara y profesional.
**Dónde:** `ProductosView.java` (y campo `nombre` agregado en `Producto.java`)

**Campos:** Código, Nombre, Descripción, Unidad de medida (lista), Precio (S/ con IGV), Stock.
**Botones:** Guardar y Listar (aún sin función).

**Diseño aplicado:**
- `FormLayout` con 6 columnas (`setResponsiveSteps`): en celular 1 columna.
  - Fila 1: Código (1) + Nombre (2) + Descripción (3).
  - Fila 2: Unidad (2) + Precio (2) + Stock (2).
- Tarjeta (`VerticalLayout`) centrada, máx. 800 px, fondo blanco, esquinas redondeadas y sombra suave.
- Línea gris delgada (`Div` de 2 px) pegada debajo del título (`margin-top` negativo).
- Botones en fila con `HorizontalLayout`.

**Lecciones:**
- Tras cada cambio: guardar, reiniciar la app y recargar con `Ctrl + F5`.
- Si un nombre sale subrayado en rojo, falta declararlo o importarlo.

### Paso 11: Botones dinámicos
**Para qué:** mejorar la experiencia de usuario.
- Guardar se activa (azul acero `#4a6fa5`) solo con Código, Nombre y Precio llenos; si no, queda gris y desactivado (`actualizarGuardar()` + `ValueChangeMode.EAGER`).
- Efecto de hundirse al pasar el mouse y mismo ancho en ambos botones: `src\main\resources\META-INF\resources\styles.css` (clase `btn-hundir`).
- CSS conectado en `BoletaApplication.java` con `@StyleSheet(Aura.STYLESHEET)`, `@StyleSheet("styles.css")` e `implements AppShellConfigurator`.

### Paso 12: Botón Guardar funcional
**Para qué:** registrar productos desde el formulario.
- La pantalla recibe `ProductoService` por el constructor (inyección de dependencias de Spring).
- `guardar.addClickListener(e -> guardarProducto())` conecta el clic con el método.
- `guardarProducto()`: crea el `Producto`, copia los datos del formulario, lo guarda con el servicio, muestra una notificación y limpia el formulario.
- Se completaron en `Producto.java` el constructor vacío y los getters/setters (faltaban).
- Unidades de medida adaptadas a una agroveterinaria: Unidad, Frasco, Sobre, Bolsa, Saco, Caja, Kilogramo, Litro, Mililitro, Ampolla.

**Lección:** si un cambio no se ve, reiniciar la app y recargar con `Ctrl + F5`.

### Paso 13: Botón Listar y tabla de productos
**Para qué:** ver los productos registrados con su precio y stock.
**Dónde:** `ProductosView.java`

- Tabla: `Grid<Producto> tabla = new Grid<>(Producto.class, false)` → `false` evita columnas automáticas; se eligen a mano.
- Columnas: Código, Nombre, Descripción, Unidad, Precio y Stock.
  - Precio con formato `String.format("S/ %.2f", ...)` → muestra 2 decimales (ej. `S/ 12.50`).
- `setAllRowsVisible(true)` → la tabla crece según la cantidad de filas, sin barra de desplazamiento interna.
- La tabla va dentro de la tarjeta, debajo de los botones.
- Conexión del botón:
```java
listar.addClickListener(e -> tabla.setItems(productoService.listar()));
```
  Al hacer clic, pide la lista al servicio y la carga en la tabla.

**Observación:** al guardar un producto la tabla no se actualiza sola; hay que pulsar Listar.

### Paso 14: Clase DetalleBoleta (campos)
**Para qué:** representar cada línea de la boleta (un producto vendido). Ej.: 2 Frascos × S/ 15.00 = S/ 30.00.
**Dónde:** `src\main\java\pe\vr\boleta\model\DetalleBoleta.java` (archivo nuevo, junto a `Producto.java`)

Campos:
| Campo | Tipo | Qué guarda |
|---|---|---|
| `codigo` | String | código del producto vendido |
| `nombre` | String | nombre del producto |
| `unidadMedida` | String | unidad (Frasco, Saco, etc.) |
| `cantidad` | int | unidades vendidas (número entero) |
| `precioUnitario` | double | precio de una unidad al momento de la venta |
| `subtotal` | double | cantidad × precioUnitario |

**Decisión de diseño:** se copian los datos del producto en vez de guardar el `Producto` completo, porque una boleta emitida no debe cambiar. Si luego sube el precio en el inventario, las boletas antiguas conservan el precio con que se vendió.

### Paso 15: Constructor vacío, getters y setters de DetalleBoleta
**Para qué:** poder crear un detalle vacío (`new DetalleBoleta()`) y leer/asignar sus datos desde otras clases. Firestore exige el constructor vacío.
**Dónde:** `DetalleBoleta.java`
**Cómo:** VS Code → `Shift + Alt + S` → *Generate Constructors* (sin campos) y *Generate Getters and Setters* (todos los campos).

### Paso 16: Método calcularSubtotal()
**Para qué:** que el detalle calcule su propio subtotal (cantidad × precio unitario) y evitar errores de cálculo en la pantalla.
**Dónde:** `DetalleBoleta.java`
```java
public void calcularSubtotal() {
    this.subtotal = this.cantidad * this.precioUnitario;
}
```
Se llama después de asignar cantidad y precio.

### Paso 17: Clase Boleta (campos)
**Para qué:** representar la boleta completa (cabecera + lista de productos vendidos).
**Dónde:** `src\main\java\pe\vr\boleta\model\Boleta.java` (archivo nuevo)

| Campo | Tipo | Qué guarda |
|---|---|---|
| `numero` | String | serie y correlativo (ej. B001-00000001) |
| `fecha` | String | fecha y hora de emisión (ej. 20/10/2026 08:45) |
| `clienteDni` | String | DNI del cliente (opcional) |
| `clienteNombre` | String | nombre del cliente (opcional, "cliente varios") |
| `formaPago` | String | EFECTIVO o YAPE (luego TARJETA) |
| `total` | double | suma de subtotales |
| `detalles` | List<DetalleBoleta> | líneas de la boleta; se crea vacía con `new ArrayList<>()` |

### Paso 18: Getters/setters y cálculo del total en Boleta
**Dónde:** `Boleta.java`
- Getters y setters generados con `Shift + Alt + S` (constructor vacío lo crea Java automáticamente).
- `agregarDetalle(detalle)`: calcula el subtotal de la línea, la agrega a la lista y recalcula el total.
- `calcularTotal()`: recorre los detalles con `for` y suma los subtotales.

### Paso 19: Prueba automática (JUnit)
**Para qué:** comprobar el cálculo sin abrir el navegador.
**Dónde:** `src\test\java\pe\vr\boleta\model\BoletaTest.java` (en **test**, no en main: JUnit solo está disponible ahí).
Caso: 2 × S/ 15.00 + 1 × S/ 80.00 → subtotales 30 y 80, total 110, 2 detalles.
```
.\mvnw.cmd test -Dtest=BoletaTest
```
**Resultado:** `Tests run: 1, Failures: 0` → BUILD SUCCESS.

**Lecciones:**
- Si `@Test`/`assertEquals` salen en rojo, el archivo está en `src\main` en vez de `src\test`.
- Guardar (`Ctrl + S`) antes de ejecutar: un archivo con ● no está guardado.

### Paso 20: Buscar producto y descontar stock
**Dónde:** `src\main\java\pe\vr\boleta\service\ProductoService.java`
- `buscarPorCodigo(codigo)`: devuelve el producto con ese código.
- `descontarStock(codigo, cantidad)`: resta la cantidad vendida; si no alcanza el stock lanza `IllegalArgumentException`.

### Paso 21: Prueba del descuento de stock
**Dónde:** `src\test\java\pe\vr\boleta\service\ProductoServiceTest.java`
- `descuentaStockAlVender`: stock 10, vende 3 → quedan 7.
- `noPermiteVenderMasDelStock`: stock 2, intenta vender 5 → lanza `IllegalArgumentException` (`assertThrows`) y el stock sigue en 2.
```
.\mvnw.cmd test -Dtest=ProductoServiceTest
```
**Resultado:** `Tests run: 2, Failures: 0` → BUILD SUCCESS.
**Lección:** "No tests matching pattern" = el archivo no estaba guardado (● en la pestaña). Activar File → Auto Save.

### Paso 22: Servicio de boletas (BoletaService)
**Para qué:** emitir la boleta: validar stock, descontar, numerar, fechar y guardar.
**Dónde:** `src\main\java\pe\vr\boleta\service\BoletaService.java` (en **main**)
- Recibe `ProductoService` por el constructor (inyección de dependencias).
- `emitir(boleta)`: 1) error si está vacía; 2) primer `for` solo verifica existencia y stock de todos; 3) segundo `for` descuenta; 4) número `B001-` + correlativo de 8 dígitos (`String.format("%08d", n)`); 5) fecha `dd/MM/yyyy HH:mm`; 6) calcula total y guarda.
- Verificar todo antes de descontar evita dejar el inventario a medias.
- Temporal: correlativo y boletas en memoria (se reinician con la app).

### Paso 23: Prueba de BoletaService
**Dónde:** `src\test\java\pe\vr\boleta\service\BoletaServiceTest.java`
- `@BeforeEach`: crea servicios nuevos y 2 productos antes de cada prueba.
- Casos: emite y descuenta; correlativo aumenta; no emite vacía; si un producto no alcanza no descuenta ninguno.
```
.\mvnw.cmd test -Dtest=BoletaServiceTest
```
**Resultado:** `Tests run: 4, Failures: 0` → BUILD SUCCESS.
**Lección:** `main` = código de la app; `test` = solo archivos `...Test.java`.

### Paso 24: Ejecutar todas las pruebas juntas
**Dónde:** Terminal (en la carpeta del proyecto `D:\eval\Boleta_electronica`)
- Se ejecutan las 3 clases de prueba en un solo comando para verificar que todo el modelo y los servicios funcionan juntos.
```
.\mvnw.cmd test -Dtest="BoletaTest,ProductoServiceTest,BoletaServiceTest"
```
**Resultado:** `Tests run: 7, Failures: 0` → BUILD SUCCESS.
**Lección:** con `-Dtest="A,B,C"` se corren varias clases a la vez; las comillas son necesarias en PowerShell por las comas.

### Paso 25: Crear la vista BoletaView
**Dónde:** `src\main\java\pe\vr\boleta\views\BoletaView.java`
- Nueva pantalla Vaadin en la ruta `/boleta` (`@Route("boleta")`).
- Por ahora solo muestra el título; queda lista para agregar los campos de la boleta.
- Recibe `ProductoService` y `BoletaService` por el constructor (Spring los inyecta automáticamente).
```
.\mvnw.cmd spring-boot:run
```
**Resultado:** al abrir `http://localhost:8080/boleta` se ve el título → funciona.
**Lección:** cada clase con `@Route` es una pantalla distinta; Spring inyecta los `@Service` en el constructor de la vista.

### Paso 26: Lista desplegable de productos en BoletaView
**Dónde:** `src\main\java\pe\vr\boleta\views\BoletaView.java`
- Se agregó un `ComboBox<Producto>` con la etiqueta "Producto", llenado con `productoService.listar()`.
- Cada producto se muestra como "código - nombre" con `setItemLabelGenerator`.
```java
ComboBox<Producto> cboProducto = new ComboBox<>("Producto");
cboProducto.setItems(productoService.listar());
cboProducto.setItemLabelGenerator(p -> p.getCodigo() + " - " + p.getNombre());
add(cboProducto);
```
**Resultado:** en `/boleta` la lista muestra P001 - Arroz, P002 - Azucar.
**Lección:** el ComboBox guarda objetos completos (`Producto`), no solo texto; los datos están en memoria y se pierden al reiniciar la app.

### Paso 27: Campo Cantidad en BoletaView
**Dónde:** `src\main\java\pe\vr\boleta\views\BoletaView.java`
- Se agregó un `IntegerField` "Cantidad" que empieza en 1, no acepta valores menores a 1 y muestra los botones − y +.
```java
IntegerField txtCantidad = new IntegerField("Cantidad");
txtCantidad.setValue(1);
txtCantidad.setMin(1);
txtCantidad.setStepButtonsVisible(true);
add(txtCantidad);
```
**Resultado:** en `/boleta` se ve "Cantidad" con valor 1 y botones − +.
**Lección:** `IntegerField` solo acepta números enteros, así se evitan letras o decimales.

### Pasos 28–30: Botón Agregar y tabla de detalle en BoletaView
**Dónde:** `src\main\java\pe\vr\boleta\views\BoletaView.java`
- Se agregó el botón "Agregar": valida que haya producto y cantidad, y muestra un aviso si falta algo.
- Debajo, un `Grid<DetalleBoleta>` con columnas Código, Producto, Cantidad, P. Unit. y Subtotal.
- "Agregar" crea un `DetalleBoleta`, lo añade a `boletaActual` y refresca la tabla.
```java
// (pegar aquí el código del botón Agregar y del Grid)
```
**Resultado:** al elegir un producto y pulsar "Agregar", aparece una fila en la tabla.
**Lección:** la tabla muestra los detalles de `boletaActual`; cada fila es un objeto `DetalleBoleta`.

### Paso 31: Total de la boleta
**Dónde:** `src\main\java\pe\vr\boleta\views\BoletaView.java`
- Se agregó una etiqueta "Total: S/ ..." que se actualiza cada vez que se agrega un producto.
```java
// (pegar aquí el código del total)
```
**Resultado:** con Aceite Primor (10.50) y Leche (4.20) muestra "Total: S/ 14.70".
**Lección:** el total se calcula con `calcularTotal()` de `Boleta`, no en la vista.

### Paso 32: Decisión de usar Firebase (Firestore)
- Los datos vivían en memoria y se perdían al reiniciar la app.
- Se eligió Firestore: base NoSQL en la nube, plan gratis Spark.
- Quedan pendientes en la boleta: datos del cliente y botón "Emitir".

### Paso 33: Proyecto y base de datos en Firebase
**Dónde:** console.firebase.google.com
- Proyecto `boleta-electronica` (ID `boleta-electronica-ba08c`), plan Spark.
- Base Firestore `(default)`, edición Standard, ubicación `nam5`, modo producción.
**Lección:** el modo producción cierra el acceso desde navegadores; el servidor Java entra con la cuenta de servicio.

### Paso 34: Clave privada de la cuenta de servicio
**Dónde:** Firebase → Configuración del proyecto → Cuentas de servicio → Generar nueva clave privada
- Se guardó **fuera del proyecto**: `D:\claves\boleta-electronica.json`.
**Lección:** la clave es como una contraseña; nunca debe ir dentro del proyecto ni subirse a GitHub.

### Paso 35: Librería firebase-admin
**Dónde:** `pom.xml`, dentro de `<dependencies>`
```xml
<dependency>
    <groupId>com.google.firebase</groupId>
    <artifactId>firebase-admin</artifactId>
    <version>9.11.0</version>
</dependency>
```
**Resultado:** `.\mvnw.cmd compile` → BUILD SUCCESS (descarga grpc, netty, protobuf, etc.).
**Lección:** el `pom.xml` es la lista de librerías; Maven las descarga solo.

### Paso 36: Ruta de la clave en la configuración
**Dónde:** `src\main\resources\application.properties`
```properties
firebase.credenciales=${FIREBASE_CREDENCIALES:D:/claves/boleta-electronica.json}
```
**Resultado:** la app sabe dónde está la clave sin escribir la ruta en el código Java.
**Lección:** `${VARIABLE:valor}` usa la variable de entorno si existe (Render) y si no, el valor por defecto (mi PC). En `.properties` se usa `/` en vez de `\`. El aviso amarillo "unknown property" es normal porque la propiedad la inventamos nosotros.

### Pasos 37–38: Clase FirebaseConfig
**Dónde:** `src\main\java\pe\vr\boleta\config\FirebaseConfig.java` (carpeta nueva `config`)
```java
@Configuration
public class FirebaseConfig {

    @Value("${firebase.credenciales}")
    private String rutaCredenciales;
}
```
**Lección:** `@Configuration` hace que Spring lea la clase al arrancar; `@Value` trae un dato de `application.properties`.

### Paso 39: Conexión con Firestore
**Dónde:** `config\FirebaseConfig.java`
```java
@Bean
public Firestore firestore() throws IOException {
    try (FileInputStream archivo = new FileInputStream(rutaCredenciales)) {
        GoogleCredentials credenciales = GoogleCredentials.fromStream(archivo);
        FirebaseOptions opciones = FirebaseOptions.builder()
                .setCredentials(credenciales)
                .build();
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(opciones);
        }
    }
    return FirestoreClient.getFirestore();
}
```
**Resultado:** `.\mvnw.cmd spring-boot:run` → la app arranca igual que antes (prueba A).
**Lección:** `@Bean` entrega el objeto `Firestore` a quien lo pida. El `if` evita iniciar Firebase dos veces cuando la app se reinicia.

### Prueba de conexión: FirebaseConexionTest
**Dónde:** `src\test\java\pe\vr\boleta\FirebaseConexionTest.java`
- Guarda el documento `pruebas/conexion` con `mensaje: "Hola desde Java"` y lo vuelve a leer.
- La ruta se pone con `ReflectionTestUtils.setField(...)` porque la prueba no lee `application.properties`.
**Resultado:** `.\mvnw.cmd test -Dtest="FirebaseConexionTest"` → Tests run: 1, Failures: 0 → BUILD SUCCESS. El documento se ve en la consola de Firebase → Firestore Database → Datos.
**Lección:** guardar el archivo (Ctrl + S) antes de ejecutar; si no, Maven dice "No tests matching". No escribir dos comandos en una sola línea. Los `WARNING` de grpc-netty y `sun.misc.Unsafe` son avisos de Java 25, no errores.


### Paso 40: ProductoService recibe Firestore
**Dónde:** `src\main\java\pe\vr\boleta\service\ProductoService.java`
- `Producto` (`pe.vr.boleta.model`) ya servía para Firestore: constructor vacío + getters/setters.
- Diseño: colección `productos`, ID de cada documento = código del producto.
```java
import com.google.cloud.firestore.Firestore;

private final Firestore db;

public ProductoService(Firestore db) {
    this.db = db;
}
```
**Lección:** Spring entrega el `Firestore` al constructor, igual que entrega `ProductoService` a las vistas.

### Paso 41: Guardar productos en Firestore
**Para qué:** que los productos no se pierdan al reiniciar la app.
**Dónde:** `ProductoService.java` → método `guardar()`
```java
db.collection("productos").document(producto.getCodigo()).set(producto).get();
```
**Resultado:** el producto aparece en la consola de Firebase → Firestore Database → colección `productos`, con el código como ID.
**Lección:** `document(id).set(objeto)` crea o reemplaza el documento. Usar el código como ID evita duplicados.

### Pasos 42–43: Leer productos desde Firestore
**Para qué:** que la tabla de productos y la boleta usen los datos guardados en la nube.
**Dónde:** `ProductoService.java` → métodos `listar()` y `buscarPorCodigo()`
```java
// listar()
return db.collection("productos").get().get().toObjects(Producto.class);

// buscarPorCodigo()
DocumentSnapshot doc = db.collection("productos").document(codigo).get().get();
return doc.exists() ? doc.toObject(Producto.class) : null;
```
**Resultado:** los productos siguen ahí después de reiniciar la app, y la boleta los muestra en la lista.
**Lección:** el primer `get()` prepara la consulta y el segundo espera la respuesta. `toObjects()` convierte los documentos en objetos Java (por eso hace falta el constructor vacío).

### Paso 44: Descontar stock en Firestore
**Dónde:** `ProductoService.java` → método `descontarStock()`
```java
db.collection("productos").document(codigo).update("stock", nuevoStock).get();
```
**Resultado:** al vender, el stock baja también en la nube.
**Lección:** `update("campo", valor)` cambia un solo campo sin reescribir todo el documento.

### Paso 45: Rechazar código repetido
**Dónde:** `ProductoService.java` → método `guardar()`
```java
if (buscarPorCodigo(producto.getCodigo()) != null) {
    throw new IllegalArgumentException("Ya existe un producto con código " + producto.getCodigo());
}
```
**Resultado:** no se puede registrar dos veces el mismo código. Queda como respaldo del código automático.
**Lección:** validar en el servicio, no solo en la pantalla, protege los datos aunque la pantalla cambie.

### Pasos 46–48: Código de producto automático (P001, P002…)
**Para qué:** que el dueño del negocio no tenga que inventar ni escribir códigos.
**Dónde:** `ProductoService.java` (método nuevo `siguienteCodigo()`) y `views\ProductosView.java` (campo `codigo`)
```java
// ProductoService.siguienteCodigo()
int max = listar().stream()
        .map(Producto::getCodigo)
        .filter(c -> c != null && c.matches("P\\d+"))
        .mapToInt(c -> Integer.parseInt(c.substring(1)))
        .max().orElse(0);
return String.format("P%03d", max + 1);

// ProductosView: en el constructor y después de Guardar
codigo.setValue(productoService.siguienteCodigo());
codigo.setReadOnly(true);
```
**Resultado:** el campo Código muestra el siguiente número (P006), es de solo lectura y pasa solo a P007 después de Guardar.
**Lección:** `String.format("P%03d", n)` completa con ceros a la izquierda (1 → P001).

### Paso 49: Pruebas de BoletaService con Mockito
**Para qué:** probar las reglas de la boleta sin conectarse a Firebase, para que las pruebas también funcionen en Render.
**Dónde:** `src\test\java\pe\vr\boleta\service\BoletaServiceTest.java`
```java
productoService = mock(ProductoService.class);
when(productoService.buscarPorCodigo("P001")).thenReturn(vitamina);   // stock 10
when(productoService.buscarPorCodigo("P002")).thenReturn(alimento);   // stock 2

verify(productoService).descontarStock("P001", 3);
verify(productoService, never()).descontarStock(anyString(), anyInt());
```
**Resultado:** las 4 pruebas pasan (emite y descuenta, correlativo, no emite vacía, no descuenta si falta stock).
**Lección:** un *mock* es un servicio simulado: responde lo que le digo con `when(...)` y con `verify(...)` compruebo si fue llamado. `ProductoServiceTest` se eliminó porque probaba la lista en memoria, que ya no existe.

### Paso 50: Prueba de conexión solo en mi PC
**Dónde:** `src\test\java\pe\vr\boleta\FirebaseConexionTest.java`
```java
assumeTrue(new File("D:/claves/boleta-electronica.json").exists());
```
**Resultado:** en mi PC la prueba corre. Donde no está la clave (Render), se salta sola en vez de fallar.
**Lección:** `assumeTrue` marca la prueba como "omitida", no como "fallida".

### Paso 51: Prueba de arranque solo con clave
**Dónde:** `src\test\java\pe\vr\boleta\BoletaApplicationTests.java`
```java
@EnabledIf("hayClaveFirebase")
class BoletaApplicationTests {

    static boolean hayClaveFirebase() {
        return new File("D:/claves/boleta-electronica.json").exists();
    }
}
```
**Resultado:** `.\mvnw.cmd test` → todas las pruebas OK.
**Lección:** `@EnabledIf` (de JUnit) decide antes de arrancar Spring si la prueba se ejecuta; así no intenta conectarse a Firebase sin clave.

---

### ETAPA 1: Publicar en Render (versión de prueba)

### Paso 52: Revisar el pom.xml
**Dónde:** `pom.xml`
**Resultado:** no hace falta un perfil `production`: `vaadin-dev` es `optional` y el `vaadin-maven-plugin` ya tiene la meta `build-frontend`.

### Pasos 53–54: Generar el .jar (error de Vite)
**Para qué:** juntar toda la app en un solo archivo `.jar` para publicarla.
**Dónde:** terminal, en `D:\eval\Boleta_electronica`
```
.\mvnw.cmd clean package
```
**Problema:** `Unable to locate Vite executable`.
**Diagnóstico:**
| Comando | Resultado |
|---|---|
| `Test-Path node_modules` | False |
| `node -v` | v22.16.0 |
| `npm -v` | 10.9.2 |
| `Test-Path package.json` | False |

**Solución:**
```
.\mvnw.cmd vaadin:prepare-frontend
.\mvnw.cmd clean package -DskipTests > build.log 2>&1
Select-String -Path build.log -Pattern "npm|vite|bundle"
```
El log dijo: *"A production mode bundle build is not needed"*.
**Resultado:** BUILD SUCCESS → `target\boleta-0.0.1-SNAPSHOT.jar` (~103 MB).
**Lección:** si la app solo usa componentes estándar de Vaadin, se usa un frontend ya compilado y no hacen falta npm ni Vite. Guardar el log en un archivo y filtrarlo con `Select-String` ayuda a encontrar la causa real.

### Paso 55: Probar el .jar
**Dónde:** terminal
```
java -jar target\boleta-0.0.1-SNAPSHOT.jar
```
**Resultado:** "Vaadin is running in production mode", "Tomcat started on port 8080", "Started BoletaApplication". En `/boleta` el combo carga los productos de Firestore. Detener con `Ctrl + C`.
**Lección:** esto es lo mismo que hará Render; si funciona en la PC, en Render solo puede fallar la configuración (clave o puerto).

### Paso 56: Dockerfile
**Para qué:** indicarle a Render cómo construir y ejecutar la app.
**Dónde:** archivo nuevo `Dockerfile` (sin extensión) en la raíz
```
New-Item -ItemType File -Name Dockerfile
code Dockerfile
```
```dockerfile
# ---------- Etapa 1: construir el .jar ----------
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# ---------- Etapa 2: ejecutar la app ----------
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/boleta-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]
```
**Lección:** dos etapas: la primera (JDK) compila y la segunda (JRE, más liviana) solo ejecuta. El `.jar` se construye dentro de Render porque pesa 103 MB y GitHub no acepta archivos de más de 100 MB. `${PORT:-8080}` usa el puerto de Render o 8080 por defecto.

### Paso 57: .dockerignore
**Para qué:** que Docker no copie archivos pesados o sensibles.
**Dónde:** archivo nuevo `.dockerignore` en la raíz
```
target/
build.log
.vscode/
.idea/
*.log
node_modules/
*.json
!package.json
!tsconfig.json
```
**Lección:** `*.json` protege contra copiar una clave de Firebase por error; `!` marca una excepción.

### Pasos 58–59: Revisar y completar .gitignore
**Dónde:** `.gitignore` (ya existía, lo creó Spring Initializr y excluía `target/`)
Se agregó al final:
```
### Boleta - exclusiones extra ###
build.log
*.log
*.json
!package.json
!tsconfig.json
```
**Lección:** una clave de Firebase subida a GitHub, aunque sea una vez, da acceso a la base de datos.

### Paso 60: Repositorio Git local
**Dónde:** terminal
```
git init -b main
```
**Resultado:** `Initialized empty Git repository in D:/eval/Boleta_electronica/.git/`
**Lección:** `git init` solo afecta a la carpeta actual; mis otros proyectos tienen su propio `.git` y no se tocan.

### Paso 61: Repositorio en GitHub
**Dónde:** github.com → **New**
| Campo | Valor |
|---|---|
| Nombre | `boleta-electronica` |
| Visibilidad | Private |
| README / .gitignore / licencia | ninguno (vacío) |

**Resultado:** `https://github.com/denisvillalba/boleta-electronica.git`
**Lección:** el repositorio debe nacer vacío para no chocar con los archivos locales; es privado porque el sistema se piensa vender.

### Paso 62: .gitattributes
**Para qué:** Render usa Linux; si `mvnw` llega con saltos de línea de Windows (CRLF) falla con *"bad interpreter"*.
**Dónde:** terminal
```
Set-Content .gitattributes "mvnw text eol=lf`n*.cmd text eol=crlf`nDockerfile text eol=lf"
```
**Resultado:** `mvnw` y `Dockerfile` en formato Linux; `mvnw.cmd` en formato Windows.

### Paso 63: Revisar qué se sube
**Dónde:** terminal
```
git add .
git diff --cached --name-only | Select-String "target/|\.json|\.log|\.jar"
git diff --cached --name-only | Measure-Object | Select-Object Count
```
**Problema:** 494 archivos, 470 de ellos en `src\main\frontend\generated\` (los genera Vaadin).
**Solución:**
```
Add-Content .gitignore "src/main/frontend/generated/"
git rm -r --cached src/main/frontend/generated
git add .
```
**Resultado:** 24 archivos, sin `.jar`, logs ni claves.
**Lección:** revisar siempre antes del primer commit. `git rm --cached` saca archivos del commit sin borrarlos del disco.

### Paso 64: Primer commit
**Dónde:** terminal
```
git commit -m "Versión inicial: productos y boleta con Vaadin + Firebase, Dockerfile para Render"
```
**Resultado:** commit `9716c45`, 24 archivos, 1914 líneas.

### Paso 65: Subir a GitHub (en curso)
**Dónde:** terminal
```
git remote add origin https://github.com/denisvillalba/boleta-electronica.git
git remote -v
git push -u origin main
```
**Resultado esperado:** `* [new branch] main -> main`
**Lección:** `origin` es el nombre de la conexión con GitHub; `-u` enlaza `main` local con la de GitHub y después basta `git push`.

---

## Parte 2: Documentación del sistema

### Objetivo
App web para emitir boletas de venta en PDF. El dueño del negocio administra sus productos y stock; la boleta usa esos productos. Se piensa vender a varios negocios (bodega, agroveterinaria, municipalidad, universidad).

### Tecnologías
| Tecnología | Uso | Estado |
|---|---|---|
| Java 25 | lenguaje | ✅ |
| Spring Boot 4.1.1 | base de la app, inyección de dependencias | ✅ |
| Vaadin 25.3.2 | pantallas web en Java | ✅ |
| Firebase Firestore | base de datos NoSQL en la nube (plan Spark gratis) | ✅ |
| firebase-admin 9.11.0 | conexión Java ↔ Firestore | ✅ |
| JUnit + Mockito | pruebas automáticas | ✅ |
| Docker | empaquetar la app para Render | ✅ |
| Git + GitHub | control de versiones (repo privado) | ✅ |
| Render | publicación (plan gratis) | ⏳ |

### Estructura de carpetas
```
Boleta_electronica\
├── Docs\
│   └── Documentación.md                   ← este archivo
├── src\
│   ├── main\
│   │   ├── java\pe\vr\boleta\
│   │   │   ├── BoletaApplication.java     ← arranca la app y conecta los estilos
│   │   │   ├── config\
│   │   │   │   └── FirebaseConfig.java    ← conecta con Firestore usando la clave
│   │   │   ├── model\
│   │   │   │   ├── Boleta.java            ← boleta completa (cabecera + detalles)
│   │   │   │   ├── DetalleBoleta.java     ← línea de la boleta (producto vendido)
│   │   │   │   └── Producto.java          ← datos del producto
│   │   │   ├── service\
│   │   │   │   ├── BoletaService.java     ← emite boletas (valida, descuenta, numera)
│   │   │   │   └── ProductoService.java   ← guarda, lista, busca, descuenta stock y genera código (Firestore)
│   │   │   └── views\
│   │   │       ├── BoletaView.java        ← pantalla para armar la boleta (/boleta)
│   │   │       └── ProductosView.java     ← registro y tabla de productos (/)
│   │   ├── frontend\generated\            ← lo genera Vaadin (no se sube a GitHub)
│   │   └── resources\
│   │       ├── application.properties     ← ruta de la clave (firebase.credenciales)
│   │       └── META-INF\resources\
│   │           └── styles.css             ← estilos (botones, efectos)
│   └── test\java\pe\vr\boleta\
│       ├── BoletaApplicationTests.java    ← arranque de la app (solo si hay clave)
│       ├── FirebaseConexionTest.java      ← escribe y lee en Firestore (solo si hay clave)
│       ├── model\
│       │   └── BoletaTest.java            ← subtotales y total
│       └── service\
│           └── BoletaServiceTest.java     ← emisión, correlativo y stock (con Mockito)
├── target\                                ← .jar generado (no se sube)
├── .dockerignore                          ← lo que Docker no copia
├── .gitattributes                         ← saltos de línea (mvnw en formato Linux)
├── .gitignore                             ← lo que Git no sube
├── Dockerfile                             ← receta para construir y ejecutar en Render
├── mvnw / mvnw.cmd                        ← Maven Wrapper (Linux / Windows)
└── pom.xml                                ← librerías del proyecto

Fuera del proyecto:
D:\claves\boleta-electronica.json          ← clave de Firebase (NUNCA dentro del proyecto)
```

### Base de datos (Firestore)
| Colección | ID del documento | Campos |
|---|---|---|
| `productos` | código (P001, P002…) | codigo, nombre, descripcion, unidadMedida, precio, stock |
| `pruebas` | `conexion` | mensaje (solo para la prueba de conexión) |
| `boletas` | ⏳ pendiente | numero, fecha, cliente, formaPago, total, detalles |

### Configuración
| Dato | Dónde | Valor |
|---|---|---|
| Ruta de la clave | `application.properties` | `${FIREBASE_CREDENCIALES:D:/claves/boleta-electronica.json}` |
| En mi PC | por defecto | `D:/claves/boleta-electronica.json` |
| En Render | variable de entorno + Secret File | `FIREBASE_CREDENCIALES` ⏳ |
| Puerto | `Dockerfile` | `${PORT:-8080}` |

### Cómo ejecutarlo
En la terminal, ubicada en `D:\eval\Boleta_electronica`:

**Modo desarrollo:**
```
.\mvnw.cmd spring-boot:run
```
**Modo producción (.jar):**
```
.\mvnw.cmd clean package
java -jar target\boleta-0.0.1-SNAPSHOT.jar
```
- Productos: http://localhost:8080
- Boleta: http://localhost:8080/boleta

**Pruebas automáticas:**
```
.\mvnw.cmd test
```

### Cómo publicar cambios
```
git add .
git commit -m "descripción del cambio"
git push
```
Render vuelve a construir la app con cada `push` (cuando esté configurado).

### Reglas de negocio
- El código de producto es automático (P001, P002…) y no se repite.
- No se emite una boleta vacía.
- Antes de descontar se verifica el stock de **todos** los productos; si uno no alcanza, no se descuenta ninguno.
- Numeración: `B001-` + correlativo de 8 dígitos.
- La boleta copia nombre y precio del producto: si luego cambia el precio, la boleta conserva el original.
- El DNI del cliente **no es obligatorio** ni se rellena con ceros; se llena solo cuando el negocio lo requiere.
- Pago presencial: efectivo o Yape (QR del negocio); tarjeta con pasarela en una segunda etapa.

### Seguridad
- La clave de Firebase está fuera del proyecto y excluida en `.gitignore` y `.dockerignore` (`*.json`).
- Firestore en modo producción: solo el servidor Java entra, con la cuenta de servicio.
- Repositorio de GitHub privado.
- Pendiente: login de usuarios y separar los datos de cada negocio.

### Módulos
- **Productos (inventario):** registrar con código automático, listar, validar campos y guardar en Firestore ✅
- **Boleta:** modelo, servicio y pruebas ✅. Pantalla: producto, cantidad, Agregar, tabla del detalle y total ✅; datos del cliente, forma de pago, botón Emitir, guardar en Firestore y PDF ⏳
- **Publicación:** .jar, Dockerfile y GitHub ✅; Render ⏳

### Pendientes
| Orden | Tarea |
|---|---|
| 1 | Subir a GitHub (`git push`) |
| 2 | Crear el servicio en Render (Docker, repo privado) |
| 3 | Clave como Secret File + variable `FIREBASE_CREDENCIALES` |
| 4 | Probar la URL pública |
| 5 | Login |
| 6 | Datos del cliente (opcionales) y forma de pago |
| 7 | Botón Emitir y guardar boletas en Firestore |
| 8 | PDF de la boleta (y envío por correo) |
| 9 | Varios negocios: un Firebase por negocio o uno multi-empresa |
