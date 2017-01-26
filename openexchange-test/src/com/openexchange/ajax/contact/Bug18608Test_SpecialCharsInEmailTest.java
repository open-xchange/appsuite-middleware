
package com.openexchange.ajax.contact;

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.groupware.container.Contact;

public class Bug18608Test_SpecialCharsInEmailTest extends AbstractManagedContactTest {

    public Bug18608Test_SpecialCharsInEmailTest() {
        super();
    }

    @Test
    public void testUmlaut() {
        testEMail("california\u00fcberalles@host.invalid");
    }

    @Test
    public void testHanCharacter() {
        testEMail("\u6279@somewhere.invalid");
    }

    private void testEMail(String email1) {
        cotm.setFailOnError(false);
        Contact c = generateContact();
        c.setEmail1(email1);
        c = cotm.newAction(c);
        AbstractAJAXResponse lastResponse = cotm.getLastResponse();
        assertTrue("We do bit allow special characters in e-mail addresses", lastResponse.hasError());
    }

}
