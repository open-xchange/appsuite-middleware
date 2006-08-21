package com.openexchange.groupware.infostore;

import com.openexchange.groupware.contexts.RdbContextWrapper;
import com.openexchange.groupware.infostore.UserData;
import com.openexchange.sessiond.SessionObject;

public class SimpleUserData implements UserData {

	private String password;
	private String user;
	private int userid;
	private String sessionid;
	private int ctx;

	public SimpleUserData(String sessionid, int ctx, int userid, String user, String password) {
		this.sessionid = sessionid;
		this.ctx = ctx;
		this.userid = userid;
		this.user = user;
		this.password = password;
	}

	public int getContextId() {
		return ctx;
	}

	public int getUserId() {
		return userid;
	}

	public String getSessionId() {
		return sessionid;
	}

	public SessionObject getSession() {
		return new SimpleSession(sessionid, user,password,ctx);
	}
	
	private static final class SimpleSession extends SessionObject{

		private String user;
		private String password;
		private int ctx;
		
		public SimpleSession(String sessionid, String user, String password, int ctx) {
			super(sessionid);
			setUsername(user);
			setUserlogin(user);
			setPassword(password);
			setContext(new RdbContextWrapper(ctx));
		}
		
		
		
	}

}
