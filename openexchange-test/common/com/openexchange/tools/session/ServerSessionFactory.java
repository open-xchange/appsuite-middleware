
package com.openexchange.tools.session;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.sessiond.impl.SessionObjectWrapper;

/**
 * {@link ServerSessionFactory} - A factory for instances of {@link ServerSession}.
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ServerSessionFactory {

    /**
     * Creates a server session.
     * 
     * @param userid The user ID
     * @param ctx The context ID
     * @param sessionid The session ID
     * @return A server session.
     */
    public static ServerSession createServerSession(final int userid, final Context ctx, final String sessionid) {
        return new ServerSessionAdapter(SessionObjectWrapper.createSessionObject(userid, ctx, "blupp"), ctx);
    }

    /**
     * Creates a server session.
     * 
     * @param userid The user ID
     * @param contextid The context ID
     * @param sessionid The session ID
     * @return A server session.
     * @throws ContextException If context look-up fails
     */
    public static ServerSession createServerSession(final int userid, final int contextid, final String sessionid) throws ContextException {
        return new ServerSessionAdapter(SessionObjectWrapper.createSessionObject(userid, contextid, sessionid));
    }
}
