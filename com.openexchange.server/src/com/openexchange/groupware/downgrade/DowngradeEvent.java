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

package com.openexchange.groupware.downgrade;

import java.sql.Connection;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.session.Session;
import com.openexchange.sessiond.impl.SessionObjectWrapper;

/**
 * {@link DowngradeEvent} - The event thrown to perform deletion of unused data
 * remaining from a user downgrade.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class DowngradeEvent {

	private final UserConfiguration newUserConfiguration;

	private final Context ctx;

	private final Connection readCon;

	private final Connection writeCon;

	private transient Session session;

	/**
	 * Initializes a new {@link DowngradeEvent}
	 *
	 * @param newUserConfiguration
	 *            The new user configuration reflecting the user's downgrade
	 * @param readCon
	 *            A readable connection to storage
	 * @param writeCon
	 *            A writable connection to storage
	 * @param ctx
	 *            The context
	 */
	public DowngradeEvent(final UserConfiguration newUserConfiguration, final Connection readCon, final Connection writeCon, final Context ctx) {
		super();
		this.ctx = ctx;
		this.newUserConfiguration = newUserConfiguration;
		this.readCon = readCon;
		this.writeCon = writeCon;
	}

	/**
	 * Initializes a new {@link DowngradeEvent}
	 *
	 * @param newUserConfiguration
	 *            The new user configuration reflecting the user's downgrade
	 * @param con
	 *            A readable/writable connection to storage
	 * @param ctx
	 *            The context
	 */
	public DowngradeEvent(final UserConfiguration newUserConfiguration, final Connection con, final Context ctx) {
		super();
		this.ctx = ctx;
		this.newUserConfiguration = newUserConfiguration;
		this.readCon = con;
		this.writeCon = con;
	}

	/**
	 * Gets the new user configuration
	 *
	 * @return the new user configuration
	 */
	public UserConfiguration getNewUserConfiguration() {
		return newUserConfiguration;
	}

	/**
	 * Gets the context
	 *
	 * @return The context
	 */
	public Context getContext() {
		return ctx;
	}

	/**
	 * Gets the readable connection
	 *
	 * @return The readable connection
	 */
	public Connection getReadCon() {
		return readCon;
	}

	/**
	 * Gets the writable connection
	 *
	 * @return The writable connection
	 */
	public Connection getWriteCon() {
		return writeCon;
	}

	/**
	 * Getter for the instance of {@link Session} belonging to context's admin
	 *
	 * @return an instance of {@link Session} belonging to context's admin
	 */
	public Session getSession() {
		if (session == null) {
			session = SessionObjectWrapper.createSessionObject(ctx.getMailadmin(), ctx, "DowngradeEventSessionObject");
		}
		return session;
	}
}
