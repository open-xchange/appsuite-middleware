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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.share.notification;

import static com.openexchange.osgi.Tools.requireService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.core.performer.CreatedShare;
import com.openexchange.share.groupware.ModuleSupport;
import com.openexchange.share.groupware.TargetProxy;
import com.openexchange.share.notification.ShareNotificationService.Transport;
import com.openexchange.share.notification.impl.NotifyDecision;
import com.openexchange.share.recipient.ShareRecipient;


/**
 * {@link DefaultDecision}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class DefaultDecision implements NotifyDecision {

    private final ServiceLookup services;

    public DefaultDecision(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public boolean notifyAboutCreatedShare(Transport transport, CreatedShare share, Session session) throws OXException {
        if (share.size() == 0) {
            return false;
        }

        if (share.isInternal()) {
            boolean notifyInternalUsers = requireService(ConfigurationService.class, services).getBoolProperty("com.openexchange.share.notifyInternal", true);
            if (!notifyInternalUsers) {
                return false;
            }

            /*
             * If the/all target(s) are public internals shall only be notified if
             *  - they are user entities, no groups
             *  - they will be admins of any target
             */
            ShareRecipient recipient = share.getShareRecipient();
            int[] permissionBits = Permissions.parsePermissionBits(recipient.getBits());
            if (!recipient.toInternal().isGroup() && permissionBits[4] > 0) {
                return true;
            }

            boolean onlyPublics = true;
            ModuleSupport moduleSupport = requireService(ModuleSupport.class, services);
            for (ShareTarget target : share.getTargets()) {
                TargetProxy proxy = moduleSupport.load(target, session);
                onlyPublics &= proxy.isPublic();
            }

            if (onlyPublics) {
                return false;
            }
        }

        return true;
    }

}
