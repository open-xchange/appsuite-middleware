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

package com.openexchange.groupware.folder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.context.ContextService;
import com.openexchange.event.LoginEvent;
import com.openexchange.event.LoginEventListener;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserException;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.oxfolder.OXFolderException;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.user.UserService;

/**
 * {@link ContactCollectorFolderCreator}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ContactCollectorFolderCreator extends LoginEventListener {

    private static final Log LOG = LogFactory.getLog(ContactCollectorFolderCreator.class);

    @Override
    public void handle(final LoginEvent event) {
        final Session session;
        {
            final SessiondService sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class);
            if (sessiondService == null) {
                LOG.warn("Sessiond service not available.");
                return;
            }
            session = sessiondService.getSession(event.getSessionId());
            if (session == null) {
                LOG.warn("Session " + event.getSessionId() + " does not exist or is expired.");
                return;
            }
        }
        final Context ctx;
        {
            final ContextService contextService = ServerServiceRegistry.getInstance().getService(ContextService.class);
            if (contextService == null) {
                LOG.warn("Context service not available.");
                return;
            }
            try {
                ctx = contextService.getContext(event.getContextId());
            } catch (final ContextException e) {
                LOG.warn("Context " + event.getContextId() + " could not be retrieved", e);
                return;
            }
        }

        final User user;
        {
            final UserService userService = ServerServiceRegistry.getInstance().getService(UserService.class);
            if (userService == null) {
                LOG.warn("User service not available.");
                return;
            }
            try {
                user = userService.getUser(event.getUserId(), ctx);
            } catch (final UserException e) {
                LOG.warn("User " + event.getUserId() + " could not be retrieved", e);
                return;
            }
        }

        // ServerUserSetting.

        try {
            final OXFolderManager manager = OXFolderManager.getInstance(session);
        } catch (final OXFolderException e) {
            LOG.error(e.getMessage(), e);
        }

        // TODO: Weiter bidde
    }

}
