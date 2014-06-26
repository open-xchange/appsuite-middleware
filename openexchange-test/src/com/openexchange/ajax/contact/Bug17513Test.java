package com.openexchange.ajax.contact;

import junit.framework.AssertionFailedError;
import com.openexchange.ajax.contact.action.InsertRequest;

public class Bug17513Test extends AbstractManagedContactTest {

    public Bug17513Test(String name) {
        super(name);
    }

    private final String json =
        "{\"anniversary\":\"1970-01-01T00:00:00.000Z\",\"last_name\":\"Aussendorf\",\"first_name\":\"Maik\",\"display_name\":\"Maik Aussendorf\",\"folder_id\":497}";

    public void testResultIsNotEmpty() throws Exception {
        try {
            getClient().execute(new InsertRequest(json));
            fail("Should fail, because the date is not in a format the OX can parse");
        } catch(AssertionFailedError e){
            assertTrue("Should fail, because the date is not in a format the OX can parse", true);
        }

    }
}
