
package com.openexchange.ajax.contact;

import static org.junit.Assert.fail;
import org.junit.Test;
import com.openexchange.ajax.contact.action.InsertRequest;

public class Bug17513Test extends AbstractManagedContactTest {

    public Bug17513Test() {
        super();
    }

    private final String json = "{\"anniversary\":\"1970-01-01T00:00:00.000Z\",\"last_name\":\"Aussendorf\",\"first_name\":\"Maik\",\"display_name\":\"Maik Aussendorf\",\"folder_id\":497}";

    @Test
    public void testResultIsNotEmpty() throws Exception {
        boolean fail = false;
        try {
            getClient().execute(new InsertRequest(json));
            fail = true;
        } catch (AssertionError e) {
        }
        if (fail) {
            fail("Should fail, because the date is not in a format the OX can parse");
        }
    }
}
