/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
