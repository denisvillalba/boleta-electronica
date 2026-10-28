package pe.vr.boleta;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

import org.junit.jupiter.api.condition.EnabledIf;

@SpringBootTest
@EnabledIf("hayClaveFirebase")
class BoletaApplicationTests {

	@Test
	void contextLoads() {
	}

	static boolean hayClaveFirebase() {
        return new File("D:/claves/boleta-electronica.json").exists();
    }

}
