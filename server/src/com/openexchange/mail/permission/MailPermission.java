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

package com.openexchange.mail.permission;

import java.lang.reflect.InvocationTargetException;

import com.openexchange.mail.MailException;
import com.openexchange.server.OCLPermission;
import com.openexchange.sessiond.Session;

/**
 * {@link MailPermission}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class MailPermission extends OCLPermission {

	private static Class<? extends MailPermission> clazz;

	protected final Session session;

	/**
	 * No instance
	 */
	protected MailPermission(final Session session) {
		super();
		this.session = session;
	}

	static void initialzeMailPermission(final Class<? extends MailPermission> clazz) {
		MailPermission.clazz = clazz;
	}

	private static final Class[] CONSTRUCTOR_ARGS = new Class[] { Session.class };

	/**
	 * Gets the proper mail permission implementation
	 * 
	 * @param session
	 *            The session
	 * @return The proper mail permission implementation
	 * @throws MailException
	 */
	public static MailPermission getInstance(final Session session) throws MailException {
		/*
		 * Create a new mail permission
		 */
		try {
			return clazz.getConstructor(CONSTRUCTOR_ARGS).newInstance(new Object[] { session });
		} catch (final SecurityException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (final NoSuchMethodException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (final IllegalArgumentException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (final InstantiationException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (final IllegalAccessException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (final InvocationTargetException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		}
	}
}
