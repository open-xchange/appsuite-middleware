/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */



package com.openexchange.groupware.delete;

import java.sql.SQLException;
import java.util.EventObject;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.ContextException;
import com.openexchange.groupware.contexts.ContextStorage;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.server.DBPoolingException;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.sessiond.SessionObjectWrapper;


/**
 * DeleteEvent
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public class DeleteEvent extends EventObject {
	
	public static final int TYPE_USER = 1;
	
	public static final int TYPE_GROUP = 2;
	
	public static final int TYPE_RESOURCE = 3;
	
	public static final int TYPE_RESOURCE_GROUP = 4;

	private static final long serialVersionUID = 2636570955675454470L;
	
	private final Context ctx;
	
	private final int id;
	
	private final int type;
	
	private SessionObject session;
	
	public DeleteEvent(Object source, int id, int type, int cid) throws ContextException {
		super(source);
		this.id = id;
		this.type = type;
		this.ctx = ContextStorage.getInstance().getContext(cid);
	}

	public DeleteEvent(Object source, int id, int type, Context ctx) {
		super(source);
		this.id = id;
		this.type = type;
		this.ctx = ctx;
	}
	
	public Context getContext() {
		return ctx;
	}

	public int getId() {
		return id;
	}

	/**
	 * Check return value against public constants TYPE_USER, TYPE_GROUP,
	 * TYPE_RESOURCE & TYPE_RESOURCE_GROUP
	 * 
	 * @return the type
	 */
	public int getType() {
		return type;
	}
	
	/**
	 * 
	 * @return a SessionObject belonging to context's mailadmin
	 */
	public SessionObject getSession() throws LdapException, SQLException, DBPoolingException, OXException {
		if (session == null) {
			session = SessionObjectWrapper.createSessionObject(ctx.getMailadmin(), ctx, "DeleteEventSessionObject");
		}
		return session;
	}
	
}
