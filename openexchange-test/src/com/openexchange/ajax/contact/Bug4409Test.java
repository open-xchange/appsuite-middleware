
package com.openexchange.ajax.contact;

import org.junit.After;
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

        objectId = insertContact(getWebConversation(), contactObj, getHostName(), getSessionId());

        loadImage(getWebConversation(), objectId, contactFolderId, getHostName(), getSessionId());
    }

    @After
    public void tearDown() throws Exception {
        try {
            if (objectId != -1) {
                deleteContact(getWebConversation(), objectId, contactFolderId, getHostName(), getSessionId());
            }
        } finally {
            super.tearDown();
        }
    }

}
