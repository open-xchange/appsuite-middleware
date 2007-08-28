/**
 * 
 */
package com.openexchange.webdav.protocol;

import java.sql.SQLException;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.server.DBPoolingException;
import com.openexchange.sessiond.SessionHolder;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.sessiond.SessionObjectWrapper;

public class DummySessionHolder implements SessionHolder{

	private SessionObject session = null;
	
	public DummySessionHolder(String username, Context ctx) throws LdapException, SQLException, DBPoolingException, OXException {
		session =  SessionObjectWrapper.createSessionObject(UserStorage.getInstance(ctx).getUserId(username)  , ctx,"12345");
	}
	
	public SessionObject getSessionObject() {
		return session;
	}
	
}