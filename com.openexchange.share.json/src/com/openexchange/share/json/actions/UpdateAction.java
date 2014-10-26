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

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.GuestShare;
import com.openexchange.share.ShareService;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.groupware.ModuleHandler;
import com.openexchange.share.recipient.InternalRecipient;
import com.openexchange.share.recipient.ShareRecipient;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link UpdateAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class UpdateAction extends AbstractShareAction {

    /**
     * Initializes a new {@link UpdateAction}.
     *
     * @param services The service lookup
     * @param translatorFactory
     */
    public UpdateAction(ServiceLookup services) {
        super(services);
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        /*
         * extract parameters
         */
        String token = requestData.checkParameter("token");
        Date clientTimestamp = new Date(requestData.getParameter("timestamp", Long.class).longValue());

        /*
         * Parse recipient and targets
         */
        ShareRecipient recipient = null;
        List<ShareTarget> targets = null;
        try {
            JSONObject jsonObject = (JSONObject) requestData.requireData();
            if (jsonObject.hasAndNotNull("recipient")) {
                recipient = ShareJSONParser.parseRecipient(jsonObject.getJSONObject("recipient"));
            }

            if (jsonObject.hasAndNotNull("targets")) {
                targets = ShareJSONParser.parseTargets(jsonObject.getJSONArray("targets"));
            }
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage());
        }

        ShareService shareService = getShareService();
        GuestShare storedShare = shareService.resolveToken(token);
//        if (storedShare.getAuthentication() != share.getAuthentication()) {
//            if (storedShare.getAuthentication() == AuthenticationMode.ANONYMOUS && share.getAuthentication() == AuthenticationMode.ANONYMOUS_PASSWORD) {
//
//            } else if (storedShare.getAuthentication() == AuthenticationMode.ANONYMOUS_PASSWORD && share.getAuthentication() == AuthenticationMode.ANONYMOUS) {
//
//            } else {
//                // TODO: throw "An anonymous share cannot be converted to a guest invitation"
//            }
//        }

//        Share updatedShare = new Share(storedShare);
//        updatedShare.setTargets(targets);
        if (recipient != null) {
//            RecipientType type = recipient.getType();
//            if (type == RecipientType.ANONYMOUS) {
//                AnonymousRecipient anonymousRecipient = (AnonymousRecipient) recipient;
//                String password = anonymousRecipient.getPassword();
//                if (password == null) {
//                    updatedShare.setAuthentication(AuthenticationMode.ANONYMOUS);
//                } else {
//                    updatedShare.setAuthentication(AuthenticationMode.ANONYMOUS_PASSWORD); // TODO: set password?
//                }
//            } else if (type == RecipientType.GUEST) {
//
//            } else {
//                // TODO exception
//            }

            Map<Integer, List<ShareTarget>> targetsByModule = new HashMap<Integer, List<ShareTarget>>();
            for (ShareTarget target : storedShare.getTargets()) {
                List<ShareTarget> list = targetsByModule.get(target.getModule());
                if (list == null) {
                    list = new LinkedList<ShareTarget>();
                    targetsByModule.put(target.getModule(), list);
                }

                list.add(target);
            }

            InternalRecipient internalRecipient = new InternalRecipient();
            internalRecipient.setBits(recipient.getBits());
            internalRecipient.setEntity(storedShare.getGuestID());
            internalRecipient.setGroup(false);
            for (Entry<Integer, List<ShareTarget>> entry : targetsByModule.entrySet()) {
                ModuleHandler handler = getModuleHandler(entry.getKey());
//                handler.updateObjects(Collections.emptyList(), entry.getValue(), Collections.singletonList(internalRecipient), session, writeCon);
            }
        }

        /*
         * update share TODO: pin-code change and change of auth type requires user update
         */
//        shareService.updateShare(session, updatedShare, recipient, clientTimestamp);
        /*
         * return empty result in case of success
         */
        return AJAXRequestResult.EMPTY_REQUEST_RESULT;
    }



}
