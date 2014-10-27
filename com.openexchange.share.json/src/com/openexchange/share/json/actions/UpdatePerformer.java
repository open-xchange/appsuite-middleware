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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.util.Pair;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.GuestShare;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.groupware.TargetPermission;
import com.openexchange.share.recipient.AnonymousRecipient;
import com.openexchange.share.recipient.RecipientType;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link UpdatePerformer}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class UpdatePerformer extends AbstractPerformer<Void> {

    private final AnonymousRecipient recipient;

    private final Date expiry;

    private final String token;

    private final Date clientLastModified;

    /**
     * Initializes a new {@link UpdatePerformer}.
     * @param session
     * @param services
     */
    protected UpdatePerformer(String token, AnonymousRecipient recipient, Date expiry, Date clientLastModified, ServerSession session, ServiceLookup services) {
        super(session, services);
        this.token = token;
        this.recipient = recipient;
        this.clientLastModified = clientLastModified;
        this.expiry = expiry;
    }

    @Override
    protected Void perform() throws OXException {
        DatabaseService dbService = services.getService(DatabaseService.class);
        Context context = session.getContext();
        Connection writeCon = dbService.getWritable(context);
        session.setParameter(Connection.class.getName(), writeCon);
        try {
            Databases.startTransaction(writeCon);

            String[] paths = token.split("/");
            GuestShare share;
            List<ShareTarget> targetsToUpdate;
            if (paths.length == 1) {
                share = getShareService().resolveToken(paths[0]); // TODO: with session to re-use connection
                targetsToUpdate = share.getTargets();
            } else if (paths.length == 2) {
                share = getShareService().resolveToken(paths[0]); // TODO: with session to re-use connection
                ShareTarget target = share.resolveTarget(paths[1]);
                if (target == null) {
                    throw ShareExceptionCodes.UNKNOWN_SHARE.create(token);
                }

                targetsToUpdate = Collections.singletonList(target);
            } else {
                throw ShareExceptionCodes.UNKNOWN_SHARE.create(token);
            }

            List<ShareTarget> modifiedTargets = buildModifiedTargets(share, targetsToUpdate);
            updateAuthAndPermissions(share, modifiedTargets, writeCon);
            getShareService().updateTargets(session, modifiedTargets, share.getGuestID(), clientLastModified);

            writeCon.commit();
            return null;
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

    private List<ShareTarget> buildModifiedTargets(GuestShare share, List<ShareTarget> targetsToUpdate) {
        List<ShareTarget> modifiedTargets = new ArrayList<ShareTarget>(targetsToUpdate.size());
        for (ShareTarget target : share.getTargets()) {
            if (targetsToUpdate.contains(target)) {
                ShareTarget modifiedTarget = new ShareTarget(target.getModule(), target.getFolder(), target.getItem());
                modifiedTarget.setOwnedBy(target.getOwnedBy());
                if (expiry == null) {
                    modifiedTarget.setExpiryDate(target.getExpiryDate());
                } else {
                    modifiedTarget.setExpiryDate(expiry);
                }

                // modifiedTarget.setMeta(meta); FIXME
            } else {
                modifiedTargets.add(target);
            }
        }
        return modifiedTargets;
    }

    private void updateAuthAndPermissions(GuestShare share, List<ShareTarget> modifiedTargets, Connection writeCon) throws OXException {
        if (recipient != null) {
            if (recipient.getType() != RecipientType.ANONYMOUS) {
                // throw exception
            }

            /*
             * adjust recipients auth information
             */
            getShareService().updateRecipient(session, share.getGuestID(), recipient);

            /*
             * adjust folder & object permissions of share targets
             */
            int permissions = recipient.getBits();
            if (permissions >= 0) {
                Pair<Map<Integer, List<ShareTarget>>, Map<Integer, List<ShareTarget>>> origDistinguishedTargets = distinguishTargets(share.getTargets());
                Map<Integer, List<ShareTarget>> origFolders = origDistinguishedTargets.getFirst();
                Map<Integer, List<ShareTarget>> origObjects = origDistinguishedTargets.getSecond();
                Pair<Map<Integer, List<ShareTarget>>, Map<Integer, List<ShareTarget>>> newDistinguishedTargets = distinguishTargets(modifiedTargets);
                Map<Integer, List<ShareTarget>> newFolders = newDistinguishedTargets.getFirst();
                Map<Integer, List<ShareTarget>> newObjects = newDistinguishedTargets.getSecond();
                List<TargetPermission> targetPermissions = Collections.singletonList(new TargetPermission(share.getGuestID(), false, permissions));
                updateFolders(origFolders, newFolders, targetPermissions, session, writeCon);
                updateObjects(origObjects, newObjects, targetPermissions, session, writeCon);
            }
        }
    }

}
