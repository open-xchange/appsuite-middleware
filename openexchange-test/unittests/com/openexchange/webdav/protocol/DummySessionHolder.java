/**
 * 
 */
package com.openexchange.webdav.protocol;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.sessiond.impl.SessionHolder;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.sessiond.impl.SessionObjectWrapper;

public class DummySessionHolder implements SessionHolder{

	private SessionObject session = null;

	private final Context ctx;
	
	public DummySessionHolder(String username, Context ctx) throws LdapException {
		session =  SessionObjectWrapper.createSessionObject(UserStorage.getInstance().getUserId(username, ctx)  , ctx,"12345");
		this.ctx = ctx;
	}
	
	public SessionObject getSessionObject() {
		return session;
	}

	public Context getContext() {
		return ctx;
	}
	
}