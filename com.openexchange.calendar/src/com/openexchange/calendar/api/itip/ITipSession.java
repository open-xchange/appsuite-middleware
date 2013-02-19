package com.openexchange.calendar.api.itip;

import com.openexchange.session.Session;

public class ITipSession implements Session {

	private final int ctxId;
	private final int userId;

	public ITipSession(final int uid, final int ctxId) {
		this.userId = uid;
		this.ctxId = ctxId;
	}

	@Override
    public int getContextId() {
		return ctxId;
	}

	@Override
    public String getLocalIp() {
		// Nothing to do
		return null;
	}

	@Override
    public void setLocalIp(final String ip) {
		// Nothing to do

	}

	@Override
    public String getLoginName() {
		// Nothing to do
		return null;
	}

	@Override
    public boolean containsParameter(final String name) {
		// Nothing to do
		return false;
	}

	@Override
    public Object getParameter(final String name) {
		// Nothing to do
		return null;
	}

	@Override
    public String getPassword() {
		// Nothing to do
		return null;
	}

	@Override
    public String getRandomToken() {
		// Nothing to do
		return null;
	}

	@Override
    public String getSecret() {
		// Nothing to do
		return null;
	}

	@Override
    public String getSessionID() {
		// Nothing to do
		return null;
	}

	@Override
    public int getUserId() {
		return userId;
	}

	@Override
    public String getUserlogin() {
		// Nothing to do
		return null;
	}

	@Override
    public String getLogin() {
		// Nothing to do
		return null;
	}

	@Override
    public void setParameter(final String name, final Object value) {
		// Nothing to do
	}

	@Override
    public String getAuthId() {
		// Nothing to do
		return null;
	}

	@Override
    public String getHash() {
		// Nothing to do
		return null;
	}

	@Override
    public void setHash(final String hash) {
		// Nothing to do
	}

	@Override
    public String getClient() {
		// Nothing to do
		return null;
	}

	@Override
    public void setClient(final String client) {
		// Nothing to do

	}

    @Override
    public boolean isTransient() {
        return false;
    }

}
