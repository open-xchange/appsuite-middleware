package liquibase.database.core;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class InformixDatabaseTest {

	private InformixDatabase database;

    @Before
	public void setUp() throws Exception {
		database = new InformixDatabase();
	}

     @Test
     public void testGetDateLiteral() {
		String d;

		d = database.getDateLiteral("2010-11-12 13:14:15");
		assertEquals("DATETIME (2010-11-12 13:14:15) YEAR TO FRACTION(5)", d);

		d = database.getDateLiteral("2010-11-12");
		assertEquals("'2010-11-12'", d);

		d = database.getDateLiteral("13:14:15");
		assertEquals("INTERVAL (13:14:15) HOUR TO FRACTION(5)", d);
	}


     @Test
     public void testGetDefaultDriver() {
		assertEquals("com.informix.jdbc.IfxDriver",
				database.getDefaultDriver("jdbc:informix-sqli://localhost:9088/liquibase:informixserver=ol_ids_1150_1"));
	}
}
