/**
 *
 */

package com.openexchange.webdav.protocol;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.sessiond.impl.SessionObjectWrapper;
import com.openexchange.tools.session.SessionHolder;

public class DummySessionHolder implements SessionHolder {

    private SessionObject session = null;

    private final Context ctx;

    public DummySessionHolder(final String username, final Context ctx) throws OXException {
        session = SessionObjectWrapper.createSessionObject(UserStorage.getInstance().getUserId(username, ctx), ctx, "12345");
        this.ctx = ctx;
    }

    @Override
    public SessionObject getSessionObject() {
        return session;
    }

    @Override
    public Context getContext() {
        return ctx;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.sessiond.impl.SessionHolder#getUser()
     */
    @Override
    public User getUser() {
        // Nothing to do
        return null;
    }

}
