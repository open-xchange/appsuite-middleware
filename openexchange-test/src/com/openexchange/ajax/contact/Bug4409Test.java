
package com.openexchange.ajax.contact;

import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import com.openexchange.ajax.ContactTest;
import com.openexchange.groupware.container.Contact;

/**
 *
 * {@link Bug4409Test}
 *
 * @author Offspring
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a> - clean-up added
 *
 */
public class Bug4409Test extends ContactTest {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Bug4409Test.class);
    private int objectId = -1;

    @Test
    public void testBug4409() throws Exception {
        final Contact contactObj = new Contact();
        contactObj.setSurName("testBug4409");
        contactObj.setParentFolderID(contactFolderId);

        objectId = cotm.newAction(contactObj).getObjectID();
        Contact reloaded = cotm.getAction(contactObj);
        assertNotNull(reloaded.getImage1());
    }
}
