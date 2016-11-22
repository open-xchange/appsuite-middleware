
package com.openexchange.ajax.contact;

import org.json.JSONObject;
import org.junit.Test;

public class TermSearchTest extends AbstractManagedContactTest {

    public TermSearchTest() {
        super();
    }

    @Test
    public void testSearchForFirstLetter() throws Exception {
        new JSONObject("{ \"AND\" : [\"yomiLastName >= A\", \"yomiLastName < B\"] }");

    }

    @Test
    public void testSearchForAll() throws Exception {
        new JSONObject("{ \"OR\": [" + "\"yomiLastName = Peter\", " + "\"yomiFirstName = Peter\"," + "\"yomiCompany = Peter\"," + "]" + "}");

    }

}
