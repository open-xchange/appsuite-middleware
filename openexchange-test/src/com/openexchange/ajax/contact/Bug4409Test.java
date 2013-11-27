package com.openexchange.ajax.contact;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

	private static final Log LOG = com.openexchange.log.Log.loggerFor(Bug4409Test.class);
    private int objectId = -1;

	public Bug4409Test(final String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testBug4409() throws Exception {
		final Contact contactObj = new Contact();
		contactObj.setSurName("testBug4409");
		contactObj.setParentFolderID(contactFolderId);

		objectId  = insertContact(getWebConversation(), contactObj, getHostName(), getSessionId());

		loadImage(getWebConversation(),objectId, contactFolderId, getHostName(), getSessionId());
	}

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if(objectId != -1){
            deleteContact(getWebConversation(), objectId, contactFolderId, getHostName(), getSessionId());
        }
    }

}
