
package com.openexchange.tools.session;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
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
        return ServerSessionAdapter.valueOf(SessionObjectWrapper.createSessionObject(userid, ctx, "blupp"), ctx);
    }

    /**
     * Creates a server session.
     *
     * @param userid The user ID
     * @param contextid The context ID
     * @param sessionid The session ID
     * @return A server session.
     * @throws OXException If context look-up fails
     */
    public static ServerSession createServerSession(final int userid, final int contextid, final String sessionid) throws OXException {
        return ServerSessionAdapter.valueOf(SessionObjectWrapper.createSessionObject(userid, contextid, sessionid));
    }
}
