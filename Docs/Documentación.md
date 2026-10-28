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

### Paso 40: ProductoService recibe Firestore (en curso)
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
**Pendiente:** cambiar los métodos para usar Firestore y ajustar las pruebas que usan `new ProductoService()`.

---

## Parte 2: Documentación del sistema

### Objetivo
App web para emitir boletas de venta en PDF. El dueño del negocio administra sus productos y stock; la boleta usa esos productos.

### Tecnologías
- Java 25
- Spring Boot 4.1.1
- Vaadin 25.3.2 (pantallas web en Java)
- JUnit (pruebas automáticas)
- Firestore (base de datos NoSQL, plan gratuito) — pendiente
- Render (publicación, plan gratuito) — pendiente

### Estructura de carpetas
```
Boleta_electronica\
├── Docs\
│   └── Documentación.md                   ← este archivo
├── src\
│   ├── main\
│   │   ├── java\pe\vr\boleta\
│   │   │   ├── BoletaApplication.java     ← arranca la app y conecta los estilos
│   │   │   ├── model\
│   │   │   │   ├── Boleta.java            ← boleta completa (cabecera + detalles)
│   │   │   │   ├── DetalleBoleta.java     ← línea de la boleta (producto vendido)
│   │   │   │   └── Producto.java          ← datos del producto
│   │   │   ├── service\
│   │   │   │   ├── BoletaService.java     ← emite boletas (valida, descuenta, numera)
│   │   │   │   └── ProductoService.java   ← guarda, lista, busca y descuenta stock (en memoria)
│   │   │   └── views\
│   │   │       ├── BoletaView.java        ← pantalla para armar y emitir la boleta (/boleta)
│   │   │       └── ProductosView.java     ← pantalla de registro y tabla de productos (/)
│   │   └── resources\META-INF\resources\
│   │       └── styles.css                 ← estilos (botones, efectos)
│   └── test\java\pe\vr\boleta\
│       ├── BoletaApplicationTests.java    ← prueba que la app arranque (creada por Spring)
│       ├── model\
│       │   └── BoletaTest.java            ← prueba subtotales y total
│       └── service\
│           ├── BoletaServiceTest.java     ← prueba emisión, correlativo y validaciones
│           └── ProductoServiceTest.java   ← prueba descuento de stock
├── mvnw.cmd                               ← Maven Wrapper (Windows)
└── pom.xml                                ← librerías del proyecto
```

### Cómo ejecutarlo
En la terminal, ubicada en `D:\eval\Boleta_electronica`:
```
.\mvnw.cmd spring-boot:run
```
- Productos: http://localhost:8080
- Boleta: http://localhost:8080/boleta

Pruebas automáticas:
```
.\mvnw.cmd test -Dtest="BoletaTest,ProductoServiceTest,BoletaServiceTest"
```

### Módulos
- **Productos (inventario):** registrar, listar en tabla y validar campos ✅ (datos en memoria; Firestore pendiente).
- **Boleta:** modelo, servicio y pruebas ✅. Pantalla en construcción: producto y cantidad ✅; Agregar, tabla del detalle, cliente, forma de pago, emitir y PDF ⏳.
