
package com.openexchange.ajax.contact;

import org.junit.Test;
import com.openexchange.ajax.ContactTest;
import com.openexchange.groupware.container.Contact;

public class DeleteTest extends ContactTest {

    @Test
    public void testDelete() throws Exception {
        final Contact contactObj = createContactObject("testDelete");
        final int id = cotm.newAction(contactObj).getObjectID();
    }
}
