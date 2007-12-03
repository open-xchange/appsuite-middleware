/**
 * 
 */
package com.openexchange.webdav.protocol;

import java.sql.SQLException;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.sessiond.impl.SessionHolder;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.sessiond.impl.SessionObjectWrapper;

public class DummySessionHolder implements SessionHolder{

	private SessionObject session = null;
	
	public DummySessionHolder(String username, Context ctx) throws LdapException, SQLException, DBPoolingException, OXException {
		session =  SessionObjectWrapper.createSessionObject(UserStorage.getInstance().getUserId(username, ctx)  , ctx,"12345");
	}
	
	public SessionObject getSessionObject() {
		return session;
	}
	
}