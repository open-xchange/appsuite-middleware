package liquibase.database.core;

import static org.junit.Assert.assertFalse;
import org.junit.Assert;
import org.junit.Test;
import liquibase.database.AbstractJdbcDatabaseTest;

public class CacheDatabaseTest extends AbstractJdbcDatabaseTest {

	public CacheDatabaseTest() throws Exception {
        super(new CacheDatabase());
    }
	
	@Override
    protected String getProductNameString() {
	      return "Cache";
	    }
	
	@Test
	public void supportsSequences() {
		assertFalse(database.supportsSequences());
	}


	@Test
	public void getLineComment() {
		Assert.assertEquals("--", database.getLineComment());
	}

	@Test
	public void getDefaultDriver() {
		Assert.assertEquals("com.intersys.jdbc.CacheDriver",
				database.getDefaultDriver("jdbc:Cache://127.0.0.1:56773/TESTMIGRATE"));
	}

	@Test
	public void getTypeName() {
		Assert.assertEquals("cache", database.getShortName());
	}



     @Test
	public void getCurrentDateTimeFunction() {
		Assert.assertEquals("SYSDATE", database.getCurrentDateTimeFunction());
	}

     @Test
	public void supportsInitiallyDeferrableColumns() {
		assertFalse(database.supportsInitiallyDeferrableColumns());
	}
}
