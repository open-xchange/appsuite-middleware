package liquibase.util;

import static org.junit.Assert.assertEquals;
import java.util.Date;
import org.junit.Test;


public class ISODateFormatTest {
    @Test
    public void isoDateFormatWithNoLeadingZeroFractions() throws Exception {
        ISODateFormat dateFormat = new ISODateFormat();
        Date date = dateFormat.parse("2012-09-12T09:47:54.664");
        assertEquals("2012-09-12T09:47:54.664", dateFormat.format(date));
    }

    @Test
    public void isoDateFormatWithLeadingZeroFractions() throws Exception {
        ISODateFormat dateFormat = new ISODateFormat();
        Date date = dateFormat.parse("2011-04-21T10:13:40.044");
        assertEquals("2011-04-21T10:13:40.044", dateFormat.format(date));
    }

}
