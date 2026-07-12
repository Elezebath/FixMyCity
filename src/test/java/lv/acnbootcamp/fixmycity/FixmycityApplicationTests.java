package lv.acnbootcamp.fixmycity;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // uses application-test.yaml (H2) instead of the default MySQL config
class FixmycityApplicationTests {

	@Test
	void contextLoads() {
		// Verifies the full Spring context starts successfully.
	}
}