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

package com.openexchange.calendar.itip;

import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.user.UserService;

public class ITipCalendarWrapper {

	protected Session session;

	protected Context ctx;

	protected User user;

	protected ServiceLookup services;

	public ITipCalendarWrapper(Session session, ServiceLookup services) {
		super();
		this.session = session;
		this.services = services;
	}

	protected void loadUser() throws OXException {
		if (user != null) {
			return;
		}
		loadContext();
		UserService users = services.getService(UserService.class);
		if (null == users) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(UserService.class.getName());
        }
		user = users.getUser(session.getUserId(), ctx);

	}

	protected void loadContext() throws OXException {
		if (ctx != null) {
			return;
		}
		ContextService contexts = services.getService(ContextService.class);
		if (null == contexts) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ContextService.class.getName());
        }
		ctx = contexts.getContext(session.getContextId());

	}

	// Returns the user id for a certain folder, if it is a shared folder, -1
	// otherwise
	protected int onBehalfOf(final int parentFolderID) throws OXException {
		loadContext();
		final OXFolderAccess ofa = new OXFolderAccess(ctx);
		if (!ofa.exists(parentFolderID)) {
		    return -1;
        }
		final int folderType = ofa.getFolderType(parentFolderID, session.getUserId());
		if (folderType == FolderObject.SHARED) {
			return ofa.getFolderOwner(parentFolderID);
		}
		return -1;
	}

}
