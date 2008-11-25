package com.openexchange.test.fixtures;

import com.openexchange.groupware.container.ContactObject;

public interface ContactFinder {
	public ContactObject getContact(SimpleCredentials credentials);
}
