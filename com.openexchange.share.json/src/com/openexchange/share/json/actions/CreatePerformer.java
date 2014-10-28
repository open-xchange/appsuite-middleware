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
import java.util.Map.Entry;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.GuestShare;
import com.openexchange.share.ShareExceptionCodes;
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
public class CreatePerformer extends AbstractPerformer<List<GuestShare>> {

    private final List<ShareRecipient> recipients;

    private final List<ShareTarget> targets;

    /**
     * Initializes a new {@link CreatePerformer}.
     * @param services
     */
    protected CreatePerformer(List<ShareRecipient> recipients, List<ShareTarget> targets, ServerSession session, ServiceLookup services) {
        super(session, services);
        this.recipients = recipients;
        this.targets = targets;
    }

    @Override
    protected List<GuestShare> perform() throws OXException {
        Map<Integer, ShareRecipient> recipientsByIndex = new HashMap<Integer, ShareRecipient>();
        int c = 0;
        for (ShareRecipient recipient : recipients) {
            recipientsByIndex.put(c++, recipient);
        }

        Map<Integer, ShareRecipient> internalRecipients = filterRecipients(recipientsByIndex, RecipientType.USER, RecipientType.GROUP);
        Map<Integer, ShareRecipient> externalRecipients = filterRecipients(recipientsByIndex, RecipientType.ANONYMOUS, RecipientType.GUEST);
        DatabaseService dbService = services.getService(DatabaseService.class);
        Context context = session.getContext();
        Connection writeCon = dbService.getWritable(context);
        session.setParameter(Connection.class.getName(), writeCon);
        try {
            Databases.startTransaction(writeCon);
            /*
             * distinguish between internal and external recipients
             */
            List<TargetPermission> permissions = new ArrayList<TargetPermission>(internalRecipients.size() + externalRecipients.size());
            for (ShareRecipient recipient : internalRecipients.values()) {
                InternalRecipient internal = (InternalRecipient) recipient;
                permissions.add(new TargetPermission(internal.getEntity(), internal.isGroup(), internal.getBits()));
            }

            TargetUpdate update = getModuleSupport().prepareUpdate(session, writeCon);
            update.prepare(targets);
            for (ShareTarget target : targets) {
                TargetProxy proxy = update.get(target);
                target.setOwnedBy(proxy.getOwner());
            }

            List<GuestShare> createdShares;
            if (externalRecipients.isEmpty()) {
                createdShares = new ArrayList<GuestShare>(recipients.size());
                for (int i = 0; i < recipients.size(); i++) {
                    createdShares.add(null);
                }
            } else {
                createdShares = addTargets(externalRecipients, permissions);
            }

            /*
             * adjust folder & object permissions of share targets
             */
            for (ShareTarget target : targets) {
                TargetProxy proxy = update.get(target);
                proxy.applyPermissions(permissions);
            }

            update.run();
            update.close();

            writeCon.commit();

            return createdShares;
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
        }
    }

    private List<GuestShare> addTargets(Map<Integer, ShareRecipient> externalRecipients, List<TargetPermission> permissions) throws OXException {
        /*
         * create shares & corresponding guest user entities for external recipients first
         */
        List<Integer> indices = new ArrayList<Integer>(externalRecipients.size());
        List<ShareRecipient> shareRecipients = new ArrayList<ShareRecipient>(externalRecipients.size());
        for (Entry<Integer, ShareRecipient> entry : externalRecipients.entrySet()) {
            indices.add(entry.getKey());
            shareRecipients.add(entry.getValue());
        }

        List<GuestShare> shares = getShareService().addTargets(session, targets, shareRecipients);
        List<GuestShare> resultList = new ArrayList<GuestShare>(recipients.size());
        for (int i = 0; i < recipients.size(); i++) {
            resultList.add(null);
        }
        for (int j = 0; j < shares.size(); j++) {
            GuestShare share = shares.get(j);
            Integer index = indices.get(j);
            permissions.add(new TargetPermission(share.getGuestID(), false, shareRecipients.get(j).getBits()));
            resultList.set(index, share);
        }
        return resultList;
    }

    /**
     * Gets a filtered map only containing the share recipients of the specified type.
     *
     * @param recipients The recipients to filter
     * @param types The allowed type
     * @return The filtered recipients
     */
    private static Map<Integer, ShareRecipient> filterRecipients(Map<Integer, ShareRecipient> recipientsByIndex, RecipientType...types) {
        Map<Integer, ShareRecipient> filteredRecipients = new HashMap<Integer, ShareRecipient>();
        for (Entry<Integer, ShareRecipient> entry : recipientsByIndex.entrySet()) {
            ShareRecipient recipient = entry.getValue();
            RecipientType type = RecipientType.of(recipient);
            for (RecipientType allowedType : types) {
                if (allowedType == type) {
                    filteredRecipients.put(entry.getKey(), recipient);
                    break;
                }
            }
        }
        return filteredRecipients;
    }

}
