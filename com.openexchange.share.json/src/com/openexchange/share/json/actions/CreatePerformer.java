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

package com.openexchange.share.json.actions;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareInfo;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.groupware.TargetPermission;
import com.openexchange.share.groupware.TargetProxy;
import com.openexchange.share.groupware.TargetUpdate;
import com.openexchange.share.recipient.InternalRecipient;
import com.openexchange.share.recipient.RecipientType;
import com.openexchange.share.recipient.ShareRecipient;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link CreatePerformer}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class CreatePerformer extends AbstractPerformer<Map<ShareRecipient, List<ShareInfo>>> {

    private final List<ShareRecipient> recipients;

    private final List<ShareTarget> targets;

    /**
     * Initializes a new {@link CreatePerformer}.
     *
     * @param recipients The share recipients to add for each target
     * @param targets The targets to add shares for
     * @param session The session of the sharing user
     * @param services A service lookup reference
     */
    protected CreatePerformer(List<ShareRecipient> recipients, List<ShareTarget> targets, ServerSession session, ServiceLookup services) {
        super(session, services);
        this.recipients = recipients;
        this.targets = targets;
    }

    @Override
    protected Map<ShareRecipient, List<ShareInfo>> perform() throws OXException {
        /*
         * distinguish between internal and external recipients
         */
        List<ShareRecipient> internalRecipients = filterRecipients(recipients, RecipientType.USER, RecipientType.GROUP);
        List<ShareRecipient> externalRecipients = filterRecipients(recipients, RecipientType.ANONYMOUS, RecipientType.GUEST);
        List<TargetPermission> targetPermissions = new ArrayList<TargetPermission>(internalRecipients.size() + externalRecipients.size());
        for (ShareRecipient recipient : internalRecipients) {
            InternalRecipient internal = (InternalRecipient) recipient;
            targetPermissions.add(new TargetPermission(internal.getEntity(), internal.isGroup(), internal.getBits()));
        }
        /*
         * prepare transaction
         */
        DatabaseService dbService = services.getService(DatabaseService.class);
        Context context = session.getContext();
        Connection writeCon = dbService.getWritable(context);
        session.setParameter(Connection.class.getName(), writeCon);
        TargetUpdate update = null;
        try {
            Databases.startTransaction(writeCon);
            update = getModuleSupport().prepareUpdate(session, writeCon);
            /*
             * pre-fetch targets to add owner information to targets
             */
            update.fetch(targets);
            for (ShareTarget target : targets) {
                TargetProxy proxy = update.get(target);
                target.setOwnedBy(proxy.getOwner());
            }
            /*
             * create shares & corresponding guest user entities for external recipients first
             */
            Map<ShareRecipient, List<ShareInfo>> sharesPerRecipient;
            if (0 < externalRecipients.size()) {
                sharesPerRecipient = getShareService().addTargets(session, targets, externalRecipients);
                /*
                 * add appropriate target permissions for corresponding guest entities
                 * (only need to consider the first share per recipient, since the guest's permissions will be equal for each target
                 */
                for (Map.Entry<ShareRecipient, List<ShareInfo>> entry : sharesPerRecipient.entrySet()) {
                    ShareRecipient shareRecipient = entry.getKey();
                    ShareInfo shareInfo = entry.getValue().get(0);
                    targetPermissions.add(new TargetPermission(shareInfo.getGuest().getGuestID(), false, shareRecipient.getBits()));
                }
            } else {
                sharesPerRecipient = new HashMap<ShareRecipient, List<ShareInfo>>();
            }
            /*
             * adjust folder & object permissions of share targets
             */
            for (ShareTarget target : targets) {
                TargetProxy proxy = update.get(target);
                proxy.applyPermissions(targetPermissions);
            }
            /*
             * execute
             */
            update.run();
            writeCon.commit();
            /*
             * add internal users (not affected by sharing) to the resulting list for completeness
             */
            for (ShareRecipient recipient : internalRecipients) {
                sharesPerRecipient.put(recipient, null);
            }
            return sharesPerRecipient;
        } catch (OXException e) {
            Databases.rollback(writeCon);
            throw e;
        } catch (SQLException e) {
            Databases.rollback(writeCon);
            throw ShareExceptionCodes.SQL_ERROR.create(e.getMessage());
        } finally {
            session.setParameter(Connection.class.getName(), null);
            Databases.autocommit(writeCon);
            dbService.backWritable(context, writeCon);
            if (update != null) {
                update.close();
            }
        }
    }

    /**
     * Gets a filtered map only containing the share recipients of the specified type.
     *
     * @param recipients The recipients to filter
     * @param types The allowed types
     * @return The filtered recipients
     */
    private static List<ShareRecipient> filterRecipients(List<ShareRecipient> recipients, RecipientType...types) {
        List<ShareRecipient> filteredRecipients = new ArrayList<ShareRecipient>();
        for (ShareRecipient recipient : recipients) {
            for (RecipientType allowedType : types) {
                if (allowedType == recipient.getType()) {
                    filteredRecipients.add(recipient);
                    break;
                }
            }
        }
        return filteredRecipients;
    }

}
