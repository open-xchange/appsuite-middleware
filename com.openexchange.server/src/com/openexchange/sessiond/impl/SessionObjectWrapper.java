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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.sessiond.impl;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;

/**
 * SessionObjectWrapper
 *
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SessionObjectWrapper {

	private SessionObjectWrapper() {
		super();
	}

	/**
	 * Creates a dummy instance of {@link SessionObject} assigned to user whose
	 * ID matches given <code>user_id</code>
	 *
	 * @param user_id
	 *            The user ID
	 * @param context_id
	 *            The context ID
	 * @param sessionobjectidentifier
	 *            The session identifier
	 * @return a dummy instance of {@link SessionObject}
	 * @throws OXException
	 *             If corresponding {@link Context} object could not be loaded
	 *             from given <code>context_id</code>
	 */
	public static SessionObject createSessionObject(final int user_id, final int context_id,
			final String sessionobjectidentifier) throws OXException {
		final Context context = ContextStorage.getInstance().getContext(context_id);
		return createSessionObject(user_id, context, sessionobjectidentifier);
	}

	/**
	 * Creates a dummy instance of {@link SessionObject} assigned to user whose
	 * ID matches given <code>user_id</code>
	 *
	 * @param user_id
	 *            The user ID
	 * @param ctx
	 *            The context
	 * @param sessionobjectidentifier
	 *            The session identifier
	 * @return a dummy instance of {@link SessionObject}
	 */
	public static SessionObject createSessionObject(final int user_id, final Context ctx,
			final String sessionobjectidentifier) {
		return createSessionObject(user_id, ctx, sessionobjectidentifier, null);
	}

	/**
	 * Creates a dummy instance of {@link SessionObject} assigned to user whose
	 * ID matches given <code>user_id</code>
	 *
	 * @param user_id
	 *            The user ID
	 * @param ctx
	 *            The context
	 * @param sessionobjectidentifier
	 *            The session identifier
	 * @param localIP
	 *            The local IP
	 * @return a dummy instance of {@link SessionObject}
	 */
	public static SessionObject createSessionObject(final int user_id, final Context ctx,
			final String sessionobjectidentifier, final String localIP) {
		final SessionObject so = new SessionObject(sessionobjectidentifier);
		so.setContextId(ctx.getContextId());
		so.setUsername(String.valueOf(user_id));
		so.setLocalIp(localIP);
		return so;
	}

}
