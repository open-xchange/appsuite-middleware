package com.openexchange.tools.session;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.upload.ManagedUploadFile;
import com.openexchange.session.Session;

public class ServerSessionAdapter implements ServerSession{

    private final Session session;
    private final Context ctx;

    public ServerSessionAdapter(final Session session) throws ContextException {
        this.session = session;
        this.ctx = ContextStorage.getStorageContext(getContextId());
    }

    public ServerSessionAdapter(final Session session, final Context ctx) {
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

    public Object getParameter(final String name) {
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

    public ManagedUploadFile getUploadedFile(final String id) {
        return session.getUploadedFile(id);
    }

    public int getUserId() {
        return session.getUserId();
    }

    public String getUserlogin() {
        return session.getUserlogin();
    }

    public void putUploadedFile(final String id, final ManagedUploadFile uploadFile) {
        session.putUploadedFile(id, uploadFile);
    }

    public ManagedUploadFile removeUploadedFile(final String id) {
        return session.removeUploadedFile(id);
    }

    public void removeUploadedFileOnly(final String id) {
        session.removeUploadedFileOnly(id);
    }

    public void setParameter(final String name, final Object value) {
        session.setParameter(name, value);
    }

    public boolean touchUploadedFile(final String id) {
        return session.touchUploadedFile(id);
    }

    public void removeRandomToken() {
        session.removeRandomToken();
    }

    public Context getContext() {
       return ctx;
    }

	public String getLogin() {
		return session.getLogin();
	}
}
