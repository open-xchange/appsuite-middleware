
package com.openexchange.ajax.contact;

import org.junit.Test;
import com.openexchange.ajax.ContactTest;
import com.openexchange.groupware.container.Contact;

public class DeleteTest extends ContactTest {

    @Test
    public void testDelete() {
        final Contact contactObj = createContactObject("testDelete");
        cotm.newAction(contactObj).getObjectID();
    }
}
