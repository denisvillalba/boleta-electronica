package pe.vr.boleta.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import pe.vr.boleta.service.BoletaService;
import pe.vr.boleta.service.ProductoService;

import com.vaadin.flow.component.combobox.ComboBox;

import pe.vr.boleta.model.Boleta;
import pe.vr.boleta.model.DetalleBoleta;
import pe.vr.boleta.model.Producto;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.notification.Notification;

import com.vaadin.flow.component.grid.Grid;

import com.vaadin.flow.component.textfield.TextField;

import com.vaadin.flow.component.radiobutton.RadioButtonGroup;


@Route("boleta")
@PageTitle("Nueva Boleta")
public class BoletaView extends VerticalLayout {

    private final ProductoService productoService;
    private final BoletaService boletaService;

    private Boleta boletaActual = new Boleta();

    public BoletaView(ProductoService productoService, BoletaService boletaService) {
        this.productoService = productoService;
        this.boletaService = boletaService;

        H2 titulo = new H2("Nueva Boleta de Venta");
        setAlignItems(Alignment.CENTER);
        add(titulo);

        TextField txtDni = new TextField("DNI del cliente (opcional)");
        txtDni.setAllowedCharPattern("[0-9]");
        txtDni.setMaxLength(8);

        TextField txtNombre = new TextField("Nombre del cliente (opcional)");
        txtNombre.setWidth("300px");

        add(txtDni, txtNombre);

        ComboBox<Producto> cboProducto = new ComboBox<>("Producto");
        cboProducto.setItems(productoService.listar());
        cboProducto.setItemLabelGenerator(p -> p.getCodigo() + " - " + p.getNombre());
        add(cboProducto);

        IntegerField txtCantidad = new IntegerField("Cantidad");   
        txtCantidad.setValue(1);                                    
        txtCantidad.setMin(1);                                      
        txtCantidad.setStepButtonsVisible(true);                    
        add(txtCantidad);  

        Grid<DetalleBoleta> gridDetalle = new Grid<>(DetalleBoleta.class, false);
        gridDetalle.addColumn(DetalleBoleta::getCodigo).setHeader("Código");
        gridDetalle.addColumn(DetalleBoleta::getNombre).setHeader("Producto");
        gridDetalle.addColumn(DetalleBoleta::getCantidad).setHeader("Cantidad");
        gridDetalle.addColumn(DetalleBoleta::getPrecioUnitario).setHeader("P. Unit.");
        gridDetalle.addColumn(DetalleBoleta::getSubtotal).setHeader("Subtotal");
                
        H3 lblTotal = new H3("Total: S/ 0.00");

        Button btnAgregar = new Button("Agregar");
        btnAgregar.addClickListener(e -> {
            Producto p = cboProducto.getValue();
            Integer cant = txtCantidad.getValue();
            if (p == null) {
                Notification.show("Seleccione un producto");
                return;
            }
            if (cant == null || cant < 1) {
                Notification.show("Ingrese una cantidad válida");
                return;
            }
            
            DetalleBoleta detalle = new DetalleBoleta();
            detalle.setCodigo(p.getCodigo());
            detalle.setNombre(p.getNombre());
            detalle.setUnidadMedida(p.getUnidadMedida());
            detalle.setCantidad(cant);
            detalle.setPrecioUnitario(p.getPrecio());
            detalle.calcularSubtotal();
            boletaActual.agregarDetalle(detalle);
            gridDetalle.setItems(boletaActual.getDetalles());
            boletaActual.calcularTotal();                                                      
            lblTotal.setText("Total: S/ " + String.format("%.2f", boletaActual.getTotal()));
            cboProducto.clear();
            txtCantidad.setValue(1);
        });

        add(btnAgregar);

        add(gridDetalle);
        gridDetalle.setHeight("250px");

        add(lblTotal);

        RadioButtonGroup<String> rbPago = new RadioButtonGroup<>("Forma de pago");
        rbPago.setItems("Efectivo", "Yape");
        rbPago.setValue("Efectivo");
        add(rbPago);

        Button btnEmitir = new Button("Emitir boleta");
        btnEmitir.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnEmitir.getStyle().set("margin-top", "20px");
        btnEmitir.addClickListener(e -> {
            String dni = txtDni.getValue().trim();
            if (!dni.isEmpty() && dni.length() != 8) {
                Notification.show("El DNI debe tener 8 dígitos");
                return;
            }
            Notification.show("DNI correcto");
        });

        add(btnEmitir);

    }

    
}