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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.GuestShare;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareService;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.groupware.TargetPermission;
import com.openexchange.share.groupware.TargetProxy;
import com.openexchange.share.groupware.TargetUpdate;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link DeletePerformer}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class DeletePerformer extends AbstractPerformer<Void> {

    private final List<String> tokens;

    /**
     * Initializes a new {@link DeletePerformer}.
     * @param session
     * @param services
     */
    DeletePerformer(String token, ServerSession session, ServiceLookup services) {
        super(session, services);
        this.tokens = Collections.singletonList(token);
    }

    /**
     * Initializes a new {@link DeletePerformer}.
     * @param session
     * @param services
     */
    DeletePerformer(List<String> tokens, ServerSession session, ServiceLookup services) {
        super(session, services);
        this.tokens = tokens;
    }

    @Override
    protected Void perform() throws OXException {
        DatabaseService dbService = services.getService(DatabaseService.class);
        Context context = session.getContext();
        Connection writeCon = dbService.getWritable(context);
        session.setParameter(Connection.class.getName(), writeCon);
        TargetUpdate update = getModuleSupport().prepareUpdate(session, writeCon);
        try {
            Databases.startTransaction(writeCon);
            ShareService shareService = getShareService();
            Map<Integer, List<ShareTarget>> targetsByGuest = new HashMap<Integer, List<ShareTarget>>();
            Map<ShareTarget, Set<Integer>> guestsByTarget = new HashMap<ShareTarget, Set<Integer>>();
            for (String token : tokens) {
                GuestShare guestShare = TokenParser.resolveShare(token, shareService);
                List<ShareTarget> targets = TokenParser.resolveTargets(guestShare, token);
                List<ShareTarget> targetsToDelete = targetsByGuest.get(guestShare.getGuestID());
                if (targetsToDelete == null) {
                    targetsToDelete = new ArrayList<ShareTarget>(guestShare.getTargets().size());
                    targetsByGuest.put(guestShare.getGuestID(), targetsToDelete);
                }

                for (ShareTarget target : targets) {
                    targetsToDelete.add(target);
                    Set<Integer> guestIDs = guestsByTarget.get(target);
                    if (guestIDs == null) {
                        guestIDs = new HashSet<Integer>();
                        guestsByTarget.put(target, guestIDs);
                    }

                    guestIDs.add(guestShare.getGuestID());
                }
            }

            update.fetch(guestsByTarget.keySet());

            for (Entry<ShareTarget, Set<Integer>> targetAndGuests : guestsByTarget.entrySet()) {
                ShareTarget target = targetAndGuests.getKey();
                Set<Integer> guestIDs = targetAndGuests.getValue();

                /*
                 * Remove folder and object permissions
                 */
                List<TargetPermission> permissions = new ArrayList<TargetPermission>(guestIDs.size());
                for (int guestID : guestIDs) {
                    permissions.add(new TargetPermission(guestID, false, 0));
                }

                TargetProxy proxy = update.get(target);
                proxy.removePermissions(permissions);
            }

            update.run();

            /*
             * Remove share targets
             */
            for (Entry<Integer, List<ShareTarget>> guestAndTarget : targetsByGuest.entrySet()) {
                shareService.deleteTargets(session, guestAndTarget.getValue(), Collections.singletonList(guestAndTarget.getKey()));
            }

            writeCon.commit();
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
            update.close();
        }

        return null;
    }

}
