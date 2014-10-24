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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.share.impl;

import static com.openexchange.osgi.Tools.requireService;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.contact.storage.ContactUserStorage;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.storage.ShareStorage;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserPermissionService;

/**
 * {@link DeleteExpiredGuestsTask}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class DeleteExpiredGuestsTask implements Callable<Void> {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteExpiredGuestsTask.class);

    private static final long GUEST_EXPIRY = TimeUnit.DAYS.toMillis(14);

    private final ServiceLookup services;

    public DeleteExpiredGuestsTask(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public Void call() throws Exception {
        ContextService contextService = requireService(ContextService.class, services);
        List<Integer> contextIds = contextService.getAllContextIds();
        for (int contextId : contextIds) {
            Context context = contextService.getContext(contextId);
            ConnectionHelper connectionHelper = new ConnectionHelper(contextId, services, true);
            try {
                connectionHelper.start();
                perform(context, connectionHelper);
                connectionHelper.commit();
            } catch (OXException e) {
                if ("CTX".equals(e.getPrefix()) && e.getCode() == 2) { // com.openexchange.groupware.contexts.impl.ContextExceptionCodes.NOT_FOUND - hidden in un-exported package...
                    LOG.debug("Could not delete expired guests in context {}. Context was deleted in the meantime.", contextId);
                } else {
                    throw e;
                }
            } catch (Exception e) {
                LOG.error("Error while trying to delete expired guests in context {}", contextId, e);
            } finally {
                connectionHelper.finish();
            }
        }

        return null;
    }

    public void perform(Context context, ConnectionHelper connectionHelper) throws OXException {
        UserService userService = requireService(UserService.class, services);
        UserPermissionService userPermissionService = requireService(UserPermissionService.class, services);
        ContactUserStorage contactUserStorage = requireService(ContactUserStorage.class, services);
        ShareStorage shareStorage = requireService(ShareStorage.class, services);

        User[] guests = userService.getUser(connectionHelper.getConnection(), context, true, true);
        for (User guest : guests) {
            try {
                if (!shareStorage.existShares(context.getContextId(), guest.getId(), connectionHelper.getParameters())) {
                    if (ShareTool.userNotModifiedSince(guest, new Date(System.currentTimeMillis() - GUEST_EXPIRY))) {
                        /*
                         * No shares exist for this guest user and he has not been modified since two weeks. Therefore we can delete him.
                         */
                        userPermissionService.deleteUserPermissionBits(connectionHelper.getConnection(), context, guest.getId());
                        contactUserStorage.deleteGuestContact(context.getContextId(), guest.getId(), new Date(), connectionHelper.getConnection());
                        userService.deleteUser(connectionHelper.getConnection(), context, guest.getId());
                    }
                }
            } catch (OXException e) {
                if (UserExceptionCode.USER_NOT_FOUND.equals(e)) {
                    LOG.debug("Could not check if guest {} in context {} is expired. Guest was deleted in the meantime.", guest.getId(), context.getContextId());
                } else {
                    LOG.error("Error while trying to check or delete expired guest user", e);
                }
            }
        }
    }

}
