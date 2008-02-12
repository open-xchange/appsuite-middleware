package com.openexchange.tools.session;

import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.sessiond.impl.SessionObjectWrapper;

public class ServerSessionFactory {
    public static ServerSession createServerSession(int userid, Context ctx, String sessionid){
       return new ServerSessionAdapter(SessionObjectWrapper.createSessionObject(userid, ctx, "blupp"), ctx); 
    }

    public static ServerSession createServerSession(int userid, int contextid, String sessionid) throws ContextException {
        return new ServerSessionAdapter(SessionObjectWrapper.createSessionObject(userid,contextid,sessionid));
    }
}
