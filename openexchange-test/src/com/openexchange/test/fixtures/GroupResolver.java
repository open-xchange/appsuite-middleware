
package com.openexchange.test.fixtures;

import com.openexchange.groupware.container.Contact;

public interface GroupResolver {

    public Contact[] resolveGroup(final String simpleName);

    public Contact[] resolveGroup(final int groupId);
}
