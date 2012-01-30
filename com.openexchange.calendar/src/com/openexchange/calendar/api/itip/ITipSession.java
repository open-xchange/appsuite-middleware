package com.openexchange.calendar.api.itip;

import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.session.Session;

public class ITipSession implements Session {

	private int ctxId;
	private int userId;

	public ITipSession(int uid, int ctxId) {
		this.userId = uid;
		this.ctxId = ctxId;
	}

	public int getContextId() {
		return ctxId;
	}

	public String getLocalIp() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setLocalIp(String ip) {
		// TODO Auto-generated method stub

	}

	public String getLoginName() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean containsParameter(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	public Object getParameter(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPassword() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getRandomToken() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getSecret() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getSessionID() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getUserId() {
		return userId;
	}

	public String getUserlogin() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getLogin() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setParameter(String name, Object value) {
		// TODO Auto-generated method stub

	}

	public void removeRandomToken() {
		// TODO Auto-generated method stub

	}

	public String getAuthId() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getHash() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setHash(String hash) {
		// TODO Auto-generated method stub

	}

	public String getClient() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setClient(String client) {
		// TODO Auto-generated method stub

	}

}
