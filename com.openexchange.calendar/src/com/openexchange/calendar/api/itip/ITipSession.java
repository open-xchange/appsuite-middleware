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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
    public void setLocalIp(final String ip) {
		// TODO Auto-generated method stub

	}

	@Override
    public String getLoginName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
    public boolean containsParameter(final String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
    public Object getParameter(final String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
    public String getPassword() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
    public String getRandomToken() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
    public String getSecret() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
    public String getSessionID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
    public int getUserId() {
		return userId;
	}

	@Override
    public String getUserlogin() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
    public String getLogin() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
    public void setParameter(final String name, final Object value) {
		// TODO Auto-generated method stub

	}

	@Override
    public void removeRandomToken() {
		// TODO Auto-generated method stub

	}

	@Override
    public String getAuthId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
    public String getHash() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
    public void setHash(final String hash) {
		// TODO Auto-generated method stub

	}

	@Override
    public String getClient() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
    public void setClient(final String client) {
		// TODO Auto-generated method stub

	}

}
