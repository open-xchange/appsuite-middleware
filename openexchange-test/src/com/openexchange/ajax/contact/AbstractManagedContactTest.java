package com.openexchange.ajax.contact;

import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Contact;
import com.openexchange.test.ContactTestManager;

public abstract class AbstractManagedContactTest extends AbstractAJAXSession {

	protected ContactTestManager manager;
	protected int folderID;

	public AbstractManagedContactTest(String name) {
		super(name);
	}

	@Override
	public void setUp() throws Exception {
	    super.setUp();
	    manager = new ContactTestManager(getClient());
	    manager.setFailOnError(false);
	    folderID = getClient().getValues().getPrivateContactFolder();
	}

	@Override
	public void tearDown() throws Exception {
	    manager.cleanUp();
	    super.tearDown();
	}

	protected Contact generateContact() {
	    Contact contact = new Contact();
	    contact.setSurName("Surname");
	    contact.setGivenName("Given name");
	    contact.setParentFolderID(folderID);
	    return contact;
	}

}