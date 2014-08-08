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

import java.sql.Connection;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.userconfiguration.RdbUserPermissionBitsStorage;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.DeleteRequest;
import com.openexchange.share.Share;
import com.openexchange.share.storage.ShareStorage;
import com.openexchange.share.storage.StorageParameters;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;


/**
 * {@link DeleteHandler}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class DeleteHandler extends RequestHandler<DeleteRequest, Void> {

    private final Context context;

    /**
     * Initializes a new {@link DeleteHandler}.
     *
     * @param deleteRequest The delete request
     * @param session The server session
     * @param services The service lookup
     */
    public DeleteHandler(DeleteRequest deleteRequest, ServerSession session, ServiceLookup services) {
        super(deleteRequest, session, services);
        this.context = session.getContext();
    }

    /**
     * Initializes a new {@link DeleteHandler}.
     *
     * @param deleteRequest The delete request
     * @param context The context
     * @param services The service lookup
     */
    public DeleteHandler(DeleteRequest deleteRequest, Context context, ServiceLookup services) {
        super(deleteRequest, null, services);
        this.context = context;
    }

    @Override
    protected Void processRequest() throws OXException {
        DatabaseService dbService = getDatabaseService();
        UserService userService = getUserService();
        ShareStorage shareStorage = getShareStorage();

        boolean ownsConnection = false;
        Connection con = request.getConnection();
        if (con == null) {
            con = dbService.getWritable(context);
            ownsConnection = true;
        }

        List<Integer> guestIDs = request.getGuestIDs();
        try {
            if (ownsConnection) {
                Databases.startTransaction(con);
            }

            int contextID = context.getContextId();
            StorageParameters parameters = StorageParameters.newInstance(Connection.class.getName(), con);
            List<Share> shares = shareStorage.loadSharesForFolder(contextID, request.getFolder(), parameters);
            List<String> tokens = new LinkedList<String>();
            for (Integer guestID : guestIDs) {
                RdbUserPermissionBitsStorage.deleteUserPermissionBits(guestID, con, context); // TODO: service layer
                userService.deleteUser(con, context, guestID);
                if (request.getItem() == null) {
                    for (Share share : shares) {
                        if (share.getGuest() == guestID) {
                            tokens.add(share.getToken());
                        }
                    }
                }
            }

            for (String token : tokens) {
                shareStorage.deleteShare(contextID, token, parameters);
            }

            if (ownsConnection) {
                con.commit();
            }
        } catch (Exception e) {
            if (ownsConnection) {
                Databases.rollback(con);
            }
            throw new OXException(e); // TODO
        } finally {
            if (ownsConnection) {
                Databases.autocommit(con);
                dbService.backWritable(context, con);
            }
        }

        return null;
    }

}
