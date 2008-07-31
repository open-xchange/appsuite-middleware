package com.openexchange.data.conversion.ical.ical4j.internal;

import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.contexts.Context;

import java.util.List;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public interface UserResolver {
    public List<User> findUsers(List<String> mails, Context ctx);
}
