package pe.vr.boleta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.vaadin.flow.component.dependency.StyleSheet;           
import com.vaadin.flow.component.page.AppShellConfigurator;        
import com.vaadin.flow.theme.aura.Aura;                            

@SpringBootApplication
@StyleSheet(Aura.STYLESHEET)                                       
@StyleSheet("styles.css")                                          
public class BoletaApplication implements AppShellConfigurator {    

	public static void main(String[] args) {
		SpringApplication.run(BoletaApplication.class, args);
	}

}