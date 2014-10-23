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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Strings;
import com.openexchange.java.util.Pair;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.Share;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.groupware.ShareTargetDiff;
import com.openexchange.share.notification.ShareNotification.NotificationType;
import com.openexchange.share.notification.ShareNotificationService;
import com.openexchange.share.notification.mail.MailNotification;
import com.openexchange.share.recipient.InternalRecipient;
import com.openexchange.share.recipient.RecipientType;
import com.openexchange.share.recipient.ShareRecipient;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;

/**
 * {@link NewAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class NewAction extends AbstractShareAction {

    /**
     * Initializes a new {@link NewAction}.
     *
     * @param services The service lookup
     * @param translatorFactory
     */
    public NewAction(ServiceLookup services) {
        super(services);
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        NewRequest request = NewRequest.parse(requestData);
        List<Share> shares = shareTargets(request, session);

        AJAXRequestResult result = new AJAXRequestResult();
        List<OXException> warnings = sendNotifications(shares, request, session);
        result.addWarnings(warnings);

        List<ShareRecipient> recipients = request.getRecipients();
        JSONArray jTokens = new JSONArray(recipients.size());

        int sharesIndex = 0;
        for (ShareRecipient recipient : recipients) {
            if (recipient.getType() == RecipientType.USER || recipient.getType() == RecipientType.GROUP) {
                jTokens.put(JSONObject.NULL);
            } else {
                jTokens.put(shares.get(sharesIndex++).getToken());
            }
        }

        result.setResultObject(jTokens, "json");
        result.setTimestamp(new Date());
        return result;
    }

    private  List<OXException> sendNotifications(List<Share> shares, NewRequest request, ServerSession session) {
        List<OXException> warnings = new LinkedList<OXException>();
        try {
            if (!shares.isEmpty()) {
                List<String> urls = generateShareURLs(shares, request.getRequestData());
                ShareNotificationService notificationService = getNotificationService();
                UserService userService = getUserService();
                for (int i = 0; i < urls.size(); i++) {
                    Share share = shares.get(i);
                    String url = urls.get(i);
                    User guest = userService.getUser(share.getGuest(), share.getContextID());
                    String mailAddress = guest.getMail();
                    if (!Strings.isEmpty(mailAddress)) {
                        try {
                            notificationService.notify(new MailNotification(NotificationType.SHARE_CREATED, share, url, request.getMessage(), mailAddress), session);
                        } catch (Exception e) {
                            if (e instanceof OXException) {
                                warnings.add((OXException) e);
                            } else {
                                warnings.add(ShareExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage()));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (e instanceof OXException) {
                warnings.add((OXException) e);
            } else {
                warnings.add(ShareExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage()));
            }
        }

        return warnings;
    }

    private List<Share> shareTargets(NewRequest request, ServerSession session) throws OXException {
        DatabaseService dbService = services.getService(DatabaseService.class);
        Context context = session.getContext();
        Connection writeCon = dbService.getWritable(context);
        session.setParameter(Connection.class.getName(), writeCon);
        try {
            Databases.startTransaction(writeCon);
            /*
             * distinguish between internal and external recipients
             */
            List<ShareTarget> targets = request.getTargets();
            List<ShareRecipient> internalRecipients = request.getInternalRecipients();
            List<ShareRecipient> externalRecipients = request.getExternalRecipients();
            List<Integer> guestIDs = Collections.emptyList();
            List<Share> shares;
            if (externalRecipients.isEmpty()) {
                shares = Collections.emptyList();
            } else {
                /*
                 * create shares & corresponding guest user entities for external recipients first
                 */
                shares = getShareService().addTargets(session, targets, externalRecipients);
                guestIDs = new ArrayList<Integer>(externalRecipients.size());
                for (Share share : shares) {
                    guestIDs.add(share.getGuest());
                }
            }

            /*
             * adjust folder & object permissions of share targets
             */
            List<InternalRecipient> finalRecipients = determineFinalRecipients(internalRecipients, externalRecipients, guestIDs);
            Pair<Map<Integer, List<ShareTarget>>, Map<Integer, List<ShareTarget>>> distinguishedTargets = distinguishTargets(targets);
            Map<Integer, List<ShareTarget>> folders = distinguishedTargets.getFirst();
            Map<Integer, List<ShareTarget>> objects = distinguishedTargets.getSecond();

            updateFolders(folders, finalRecipients, session, writeCon);
            updateObjects(objects, finalRecipients, session, writeCon);
            writeCon.commit();
            return shares;
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

    private void updateObjects(Map<Integer, List<ShareTarget>> objectsByModule, List<InternalRecipient> finalRecipients, ServerSession session, Connection writeCon) throws OXException {
        for (Entry<Integer, List<ShareTarget>> entry : objectsByModule.entrySet()) {
            int module = entry.getKey();
            List<ShareTarget> objects = entry.getValue();
            getModuleHandler(module).updateObjects(new ShareTargetDiff(Collections.<ShareTarget>emptyList(), objects), finalRecipients, session, writeCon);
        }
    }


    private void updateFolders(Map<Integer, List<ShareTarget>> foldersByModule, List<InternalRecipient> finalRecipients, ServerSession session, Connection writeCon) throws OXException {
        for (Entry<Integer, List<ShareTarget>> entry : foldersByModule.entrySet()) {
            int module = entry.getKey();
            List<ShareTarget> folders = entry.getValue();
            getModuleHandler(module).updateFolders(new ShareTargetDiff(Collections.<ShareTarget>emptyList(), folders), finalRecipients, session, writeCon);
        }
    }

    /**
     * Takes a list of {@link ShareTarget}s and splits them up into two maps. The first map
     * contains all folder targets mapped to their according module. The second map contains
     * all object targets, again mapped to their according module.
     *
     * @param targets The targets to share
     * @return A {@link Pair} with the folders as first and the objects as second entry.
     */
    private Pair<Map<Integer, List<ShareTarget>>, Map<Integer, List<ShareTarget>>> distinguishTargets(List<ShareTarget> targets) {
        Map<Integer, List<ShareTarget>> folders = new HashMap<Integer, List<ShareTarget>>();
        Map<Integer, List<ShareTarget>> objects = new HashMap<Integer, List<ShareTarget>>();
        for (ShareTarget target : targets) {
            int module = target.getModule();
            List<ShareTarget> finalTargets;
            if (target.isFolder()) {
                finalTargets = folders.get(module);
                if (finalTargets == null) {
                    finalTargets = new LinkedList<ShareTarget>();
                    folders.put(module, finalTargets);
                }
            } else {
                finalTargets = objects.get(module);
                if (finalTargets == null) {
                    finalTargets = new LinkedList<ShareTarget>();
                    objects.put(module, finalTargets);
                }
            }

            finalTargets.add(target);
        }

        return new Pair<Map<Integer,List<ShareTarget>>, Map<Integer,List<ShareTarget>>>(folders, objects);
    }

    /**
     * Gets some internal recipients as well as external ones and their according guest IDs and
     * combines them into a single list of internal recipients.
     *
     * @param internalRecipients
     * @param externalRecipients
     * @param guestIDs
     * @return
     */
    private List<InternalRecipient> determineFinalRecipients(List<ShareRecipient> internalRecipients, List<ShareRecipient> externalRecipients, List<Integer> guestIDs) {
        List<InternalRecipient> finalRecipients = new ArrayList<InternalRecipient>(internalRecipients.size() + externalRecipients.size());
        for (ShareRecipient internal : internalRecipients) {
            finalRecipients.add((InternalRecipient) internal);
        }

        for (int i = 0; i < externalRecipients.size(); i++) {
            ShareRecipient external = externalRecipients.get(i);
            InternalRecipient internal = new InternalRecipient();
            internal.setEntity(guestIDs.get(i).intValue());
            internal.setGroup(false);
            internal.setBits(external.getBits());
            finalRecipients.add(internal);
        }

        return finalRecipients;
    }



}
