package com.openexchange.test.fixtures;

import com.openexchange.groupware.container.ContactObject;

public interface GroupResolver {
	
	public ContactObject[] resolveGroup(final String simpleName);
	public ContactObject[] resolveGroup(final int groupId);
}
