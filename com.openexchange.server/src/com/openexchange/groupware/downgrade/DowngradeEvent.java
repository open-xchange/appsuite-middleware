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
