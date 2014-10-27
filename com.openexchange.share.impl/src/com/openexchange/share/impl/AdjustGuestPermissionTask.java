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
import java.util.Set;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserExceptionCode;
import com.openexchange.groupware.userconfiguration.UserConfigurationCodes;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.storage.ShareStorage;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserConfigurationService;
import com.openexchange.userconf.UserPermissionService;


/**
 * {@link AdjustGuestPermissionTask}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class AdjustGuestPermissionTask implements Callable<Void> {

    private static final Logger LOG = LoggerFactory.getLogger(AdjustGuestPermissionTask.class);

    private final ServiceLookup services;

    private final int contextId;

    private final int guestId;

    public AdjustGuestPermissionTask(ServiceLookup services, int contextId, int guestId) {
        super();
        this.services = services;
        this.contextId = contextId;
        this.guestId = guestId;
    }

    @Override
    public Void call() throws Exception {
        ConnectionHelper connectionHelper = new ConnectionHelper(contextId, services, true);
        try {
            connectionHelper.start();
            perform(connectionHelper);
            connectionHelper.commit();
        } catch (OXException e) {
            if ("CTX".equals(e.getPrefix()) && e.getCode() == 2) { // com.openexchange.groupware.contexts.impl.ContextExceptionCodes.NOT_FOUND - hidden in un-exported package...
                LOG.debug("Could not check permissions for guest {} in context {}. Context was deleted in the meantime.", guestId, contextId);
            } else if (UserExceptionCode.USER_NOT_FOUND.equals(e)) {
                LOG.debug("Could not check permissions for guest {} in context {}. Guest was deleted in the meantime.", guestId, contextId);
            } else if (UserConfigurationCodes.NOT_FOUND.equals(e)) {
                LOG.debug("Could not check permissions for guest {} in context {}. Guest was deleted in the meantime.", guestId, contextId);
            } else {
                LOG.error("Error while trying to adjust permissions for guest {} in context {}", guestId, contextId, e);
            }
        } catch (Exception e) {
            LOG.error("Error while trying to adjust permissions for guest {} in context {}", guestId, contextId, e);
        } finally {
            connectionHelper.finish();
        }

        return null;
    }

    public void perform(ConnectionHelper connectionHelper) throws OXException {
        ContextService contextService = requireService(ContextService.class, services);
        UserService userService = requireService(UserService.class, services);
        ShareStorage shareStorage = requireService(ShareStorage.class, services);
        UserPermissionService userPermissionService = requireService(UserPermissionService.class, services);

        Context context = contextService.getContext(contextId);
        User guest = userService.getUser(connectionHelper.getConnection(), guestId, context);
        UserPermissionBits userPermissionBits = userPermissionService.getUserPermissionBits(connectionHelper.getConnection(), guestId, context);
        shareStorage.loadShares(contextId, guestId, connectionHelper.getParameters());
        Set<Integer> modules = shareStorage.getSharedModules(contextId, guestId, connectionHelper.getParameters());
        int permissionBits = ShareTool.getRequiredPermissionBits(guest, modules);
        if (userPermissionBits.getPermissionBits() != permissionBits) {
            /*
             * update permission bits
             */
            userPermissionBits.setPermissionBits(permissionBits);
            userPermissionService.saveUserPermissionBits(connectionHelper.getConnection(), userPermissionBits);
            /*
             * invalidate affected user configuration
             */
            services.getService(UserConfigurationService.class).removeUserConfiguration(guestId, context);
        }
    }

}
