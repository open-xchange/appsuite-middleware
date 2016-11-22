package liquibase.logging.jvm;

import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import liquibase.logging.LogFactory;

public class LogFactoryTest {
    
    @Test
    public void getLogger() {
        assertNotNull(LogFactory.getLogger());
    }
}
