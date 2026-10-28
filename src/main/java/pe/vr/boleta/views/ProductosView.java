package pe.vr.boleta.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.value.ValueChangeMode;
import pe.vr.boleta.service.ProductoService;
import com.vaadin.flow.component.notification.Notification;
import pe.vr.boleta.model.Producto;
import com.vaadin.flow.component.grid.Grid;

@Route("")
@PageTitle("Productos")
public class ProductosView extends VerticalLayout {

    private final TextField codigo = new TextField("Código");
    private final TextField nombre = new TextField("Nombre");
    private final TextField descripcion = new TextField("Descripción");
    private final ComboBox<String> unidadMedida = new ComboBox<>("Unidad de medida");
    private final NumberField precio = new NumberField("Precio (S/ con IGV)");
    private final IntegerField stock = new IntegerField("Stock");
       
    private final Button guardar = new Button("Guardar");
    private final Button listar = new Button("Listar");
        private final Grid<Producto> tabla = new Grid<>(Producto.class, false);
    private final ProductoService productoService;

     public ProductosView(ProductoService productoService){
        this.productoService = productoService;
        codigo.setValue(productoService.siguienteCodigo());
        codigo.setReadOnly(true);
        unidadMedida.setItems("Unidad", "Frasco", "Sobre", "Bolsa", "Saco", "Caja",
                "Kilogramo", "Litro", "Mililitro", "Ampolla");

        FormLayout formulario = new FormLayout(codigo, nombre, descripcion, unidadMedida, precio, stock);
        formulario.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 6));
        formulario.setColspan(codigo, 1);
        formulario.setColspan(nombre, 2);
        formulario.setColspan(descripcion, 3);
        formulario.setColspan(unidadMedida, 2);
        formulario.setColspan(precio, 2);
        formulario.setColspan(stock, 2);

        H2 titulo = new H2("Registro de Productos");
        titulo.setWidthFull();
        
        titulo.getStyle()
                .set("margin-bottom", "0")
                .set("padding-bottom", "0");
        
        Div canaleta = new Div();
        canaleta.setWidthFull();
        canaleta.getStyle()
                .set("height", "2px")
                .set("background", "#dddddd")
                .set("border-radius", "2px")
                .set("margin-bottom", "16px");
        
         listar.getStyle()
                .set("background", "#4a6fa5")
                .set("color", "white");

        codigo.setValueChangeMode(ValueChangeMode.EAGER);
        nombre.setValueChangeMode(ValueChangeMode.EAGER);
        precio.setValueChangeMode(ValueChangeMode.EAGER);

        codigo.addValueChangeListener(e -> actualizarGuardar());
        nombre.addValueChangeListener(e -> actualizarGuardar());
        precio.addValueChangeListener(e -> actualizarGuardar());

        actualizarGuardar();
        guardar.addClickListener(e -> guardarProducto());
        listar.addClickListener(e -> tabla.setItems(productoService.listar()));

        guardar.addClassName("btn-hundir");
        listar.addClassName("btn-hundir");
               
        HorizontalLayout botones = new HorizontalLayout(guardar, listar);
        botones.setSpacing(true);
        botones.getStyle().set("margin-top", "8px");

        tabla.addColumn(Producto::getCodigo).setHeader("Código");
        tabla.addColumn(Producto::getNombre).setHeader("Nombre");
        tabla.addColumn(Producto::getDescripcion).setHeader("Descripción");
        tabla.addColumn(Producto::getUnidadMedida).setHeader("Unidad");
        tabla.addColumn(p -> String.format("S/ %.2f", p.getPrecio())).setHeader("Precio");
        tabla.addColumn(Producto::getStock).setHeader("Stock");
        tabla.setAllRowsVisible(true);
        tabla.getStyle().set("margin-top", "16px");

        VerticalLayout tarjeta = new VerticalLayout(titulo, canaleta, formulario, botones, tabla);
        tarjeta.setWidthFull();
        tarjeta.setMaxWidth("800px");
        tarjeta.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("box-shadow", "0 2px 10px rgba(0, 0, 0, 0.1)");

        setAlignItems(Alignment.CENTER);
        
        add(tarjeta);
    }

    private void actualizarGuardar() {
        boolean completo = !codigo.isEmpty() && !nombre.isEmpty() && !precio.isEmpty();
        guardar.setEnabled(completo);
        guardar.getStyle()
                .set("background", completo ? "#4a6fa5" : "#bdbdbd")
                .set("color", "white");
    }

    private void guardarProducto() {
        Producto producto = new Producto();
        producto.setCodigo(codigo.getValue());
        producto.setNombre(nombre.getValue());
        producto.setDescripcion(descripcion.getValue());
        producto.setUnidadMedida(unidadMedida.getValue());
        producto.setPrecio(precio.getValue());
        producto.setStock(stock.getValue() == null ? 0 : stock.getValue());

        productoService.guardar(producto);

        Notification.show("Producto guardado: " + producto.getNombre());

        codigo.setValue(productoService.siguienteCodigo());
        nombre.clear();
        descripcion.clear();
        unidadMedida.clear();
        precio.clear();
        stock.clear();
    }

}