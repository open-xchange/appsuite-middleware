package com.openexchange.tools.session;

import com.openexchange.session.Session;
import com.openexchange.groupware.upload.ManagedUploadFile;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.contexts.impl.ContextException;

public class ServerSessionAdapter implements ServerSession{

    private Session session;
    private Context ctx;

    public ServerSessionAdapter(Session session) throws ContextException {
        this.session = session;
        this.ctx = ContextStorage.getStorageContext(getContextId());
    }

    public ServerSessionAdapter(Session session, Context ctx) {
        this.session = session;
        this.ctx = ctx;
    }

    public int getContextId() {
        return session.getContextId();
    }

    public String getLocalIp() {
        return session.getLocalIp();
    }

    public String getLoginName() {
        return session.getLoginName();
    }

    public Object getParameter(String name) {
        return session.getParameter(name);
    }

    public String getPassword() {
        return session.getPassword();
    }

    public String getRandomToken() {
        return session.getRandomToken();
    }

    public String getSecret() {
        return session.getSecret();
    }

    public String getSessionID() {
        return session.getSessionID();
    }

    public ManagedUploadFile getUploadedFile(String id) {
        return session.getUploadedFile(id);
    }

    public int getUserId() {
        return session.getUserId();
    }

    public String getUserlogin() {
        return session.getUserlogin();
    }

    public void putUploadedFile(String id, ManagedUploadFile uploadFile) {
        session.putUploadedFile(id, uploadFile);
    }

    public ManagedUploadFile removeUploadedFile(String id) {
        return session.removeUploadedFile(id);
    }

    public void removeUploadedFileOnly(String id) {
        session.removeUploadedFileOnly(id);
    }

    public void setParameter(String name, Object value) {
        session.setParameter(name, value);
    }

    public boolean touchUploadedFile(String id) {
        return session.touchUploadedFile(id);
    }

    public void removeRandomToken() {
        session.removeRandomToken();
    }

    public Context getContext() {
       return ctx;
    }
}
