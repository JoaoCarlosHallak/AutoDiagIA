package com.autodiag.AutoDiagIA;

import com.autodiag.AutoDiagIA.communication.Elm327Client;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AutoDiagIaApplicationTests {
	@Mock
    private Elm327Client elm327Client;
	@Test
	void contextLoads() {
	}

}
