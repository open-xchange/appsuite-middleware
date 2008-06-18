package com.openexchange.tools.session;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.sessiond.impl.SessionObjectWrapper;

public class ServerSessionFactory {
    public static ServerSession createServerSession(final int userid, final Context ctx, final String sessionid){
       return new ServerSessionAdapter(SessionObjectWrapper.createSessionObject(userid, ctx, "blupp"), ctx); 
    }

    public static ServerSession createServerSession(final int userid, final int contextid, final String sessionid) throws ContextException {
        return new ServerSessionAdapter(SessionObjectWrapper.createSessionObject(userid,contextid,sessionid));
    }
}
