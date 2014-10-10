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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.modules.Module;
import com.openexchange.java.Enums;
import com.openexchange.java.util.Pair;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.Share;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.json.internal.InternalRecipient;
import com.openexchange.share.json.internal.PermissionUpdater;
import com.openexchange.share.json.internal.PermissionUpdaters;
import com.openexchange.share.json.internal.RecipientType;
import com.openexchange.share.recipient.AnonymousRecipient;
import com.openexchange.share.recipient.GuestRecipient;
import com.openexchange.share.recipient.ShareRecipient;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

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
     */
    public NewAction(ServiceLookup services) {
        super(services);
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        /*
         * get request body
         */
        JSONObject data = (JSONObject) requestData.getData();
        if (null == data) {
            throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
        }
        /*
         * parse targets & recipients
         */
        List<ShareTarget> targets;
        List<ShareRecipient> recipients;
        try {
            targets = parseTargets(data.getJSONArray("targets"));
            recipients = parseRecipients(data.getJSONArray("recipients"));
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }

        shareTargets(targets, recipients, session);
        return AJAXRequestResult.EMPTY_REQUEST_RESULT;
    }

    private void shareTargets(List<ShareTarget> targets, List<ShareRecipient> recipients, ServerSession session) throws OXException {
        // TODO: start transaction here

        /*
         * distinguish between internal and external recipients
         */
        List<ShareRecipient> internalRecipients = filterRecipients(recipients, RecipientType.USER, RecipientType.GROUP);
        List<ShareRecipient> externalRecipients = filterRecipients(recipients, RecipientType.ANONYMOUS, RecipientType.GUEST);
        List<Integer> guestIDs = Collections.emptyList();
        if (!externalRecipients.isEmpty()) {
            /*
             * create shares & corresponding guest user entities for external recipients first
             */
            Map<ShareTarget, List<Share>> createdShares = getShareService().createShares(session, targets, externalRecipients);
            guestIDs = new ArrayList<Integer>(externalRecipients.size());
            for (Share share : createdShares.values().iterator().next()) {
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

        updateFolders(folders, finalRecipients, session);
        updateObjects(objects, finalRecipients, session);

        // TODO: commit transaction
    }

    /**
     * @param objectsByModule
     * @param finalRecipients
     * @throws OXException
     */
    private void updateObjects(Map<Integer, List<ShareTarget>> objectsByModule, List<InternalRecipient> finalRecipients, ServerSession session) throws OXException {
        for (Entry<Integer, List<ShareTarget>> entry : objectsByModule.entrySet()) {
            int module = entry.getKey();
            List<ShareTarget> objects = entry.getValue();
            PermissionUpdater updater = PermissionUpdaters.forModule(module);
            if (updater != null) {
                updater.updateObjects(objects, finalRecipients, session);
            }

            // TODO: throw exception
        }
    }

    /**
     * @param foldersByModule
     * @param finalRecipients
     * @throws OXException
     */
    private void updateFolders(Map<Integer, List<ShareTarget>> foldersByModule, List<InternalRecipient> finalRecipients, ServerSession session) throws OXException {
        for (Entry<Integer, List<ShareTarget>> entry : foldersByModule.entrySet()) {
            int module = entry.getKey();
            List<ShareTarget> folders = entry.getValue();
            PermissionUpdater updater = PermissionUpdaters.forModule(module);
            if (updater != null) {
                updater.updateFolders(folders, finalRecipients, session);
            }

            // TODO: throw exception
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
            internal.setId(guestIDs.get(i));
            internal.setGroup(false);
            internal.setBits(external.getBits());
            finalRecipients.add(internal);
        }

        return finalRecipients;
    }

    /**
     * Gets a filtered list only containing the share recipients of the spcieid type.
     *
     * @param recipients The recipients to filter
     * @param types The allowed type
     * @return The filtered recipients
     */
    private static List<ShareRecipient> filterRecipients(List<ShareRecipient> recipients, RecipientType...types) {
        List<ShareRecipient> filteredRecipients = new ArrayList<ShareRecipient>();
        for (ShareRecipient recipient : recipients) {
            RecipientType type = RecipientType.of(recipient);
            for (RecipientType allowedType : types) {
                if (allowedType == type) {
                    filteredRecipients.add(recipient);
                    break;
                }
            }
        }
        return filteredRecipients;
    }

    /**
     * Parses a list of share recipients from the supplied JSON array.
     *
     * @param jsonRecipients The JSON array holding the share recipients
     * @return The share recipients
     * @throws JSONException
     */
    private static List<ShareRecipient> parseRecipients(JSONArray jsonRecipients) throws OXException, JSONException {
        if (null == jsonRecipients || 0 == jsonRecipients.length()) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("recipients");
        }
        List<ShareRecipient> recipients = new ArrayList<ShareRecipient>();
        for (int i = 0; i < jsonRecipients.length(); i++) {
            recipients.add(parseRecipient(jsonRecipients.getJSONObject(i)));
        }
        return recipients;
    }

    /**
     * Parses a list of share targets from the supplied JSON array.
     *
     * @param jsonTargets The JSON array holding the share targets
     * @return The share targets
     */
    private static List<ShareTarget> parseTargets(JSONArray jsonTargets) throws OXException, JSONException {
        if (null == jsonTargets || 0 == jsonTargets.length()) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("targets");
        }
        List<ShareTarget> targets = new ArrayList<ShareTarget>();
        for (int i = 0; i < jsonTargets.length(); i++) {
            targets.add(parseTarget(jsonTargets.getJSONObject(i)));
        }
        return targets;
    }

    /**
     * Parses a share target from the supplied JSON object.
     *
     * @param jsonTargets The JSON object holding the share target
     * @return The share target
     * @throws OXException
     */
    private static ShareTarget parseTarget(JSONObject jsonTarget) throws JSONException, OXException {
        if (false == jsonTarget.hasAndNotNull("module")) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("module");
        }
        int module = Module.getModuleInteger(jsonTarget.getString("module"));
        if (false == jsonTarget.hasAndNotNull("folder")) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("folder");
        }
        String folder = jsonTarget.getString("folder");
        if (jsonTarget.hasAndNotNull("item")) {
            return new ShareTarget(module, folder, jsonTarget.getString("item"));
        } else {
            return new ShareTarget(module, folder);
        }
    }

    /**
     * Parses a share recipient from the supplied JSON object.
     *
     * @param jsonTargets The JSON object holding the share recipient
     * @return The share recipient
     * @throws OXException
     */
    private static ShareRecipient parseRecipient(JSONObject jsonRecipient) throws JSONException, OXException {
        /*
         * determine type
         */
        if (false == jsonRecipient.hasAndNotNull("type")) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("type");
        }
        RecipientType type;
        try {
            type = Enums.parse(RecipientType.class, jsonRecipient.getString("type"));
        } catch (IllegalArgumentException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(e, "type", jsonRecipient.getString("type"));
        }
        /*
         * parse recipient type specific properties
         */
        ShareRecipient recipient;
        switch (type) {
        case USER:
        case GROUP:
            InternalRecipient internalRecipient = new InternalRecipient();
            internalRecipient.setGroup(RecipientType.GROUP == type);
            if (false == jsonRecipient.hasAndNotNull("id")) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("id");
            }
            internalRecipient.setId(jsonRecipient.getInt("id"));
            recipient = internalRecipient;
            break;
        case ANONYMOUS:
            AnonymousRecipient anonymousRecipient = new AnonymousRecipient();
            if (jsonRecipient.hasAndNotNull("password")) {
                anonymousRecipient.setPassword(jsonRecipient.getString("password"));
            }
            recipient = anonymousRecipient;
            break;
        case GUEST:
            GuestRecipient guestRecipient = new GuestRecipient();
            if (false == jsonRecipient.hasAndNotNull("email_address")) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("email_address");
            }
            guestRecipient.setEmailAddress(jsonRecipient.getString("email_address"));
            if (jsonRecipient.hasAndNotNull("password")) {
                guestRecipient.setPassword(jsonRecipient.getString("password"));
            }
            if (jsonRecipient.hasAndNotNull("display_name")) {
                guestRecipient.setDisplayName(jsonRecipient.getString("display_name"));
            }
            if (jsonRecipient.hasAndNotNull("contact_id")) {
                guestRecipient.setContactID(jsonRecipient.getString("contact_id"));
                if (false == jsonRecipient.hasAndNotNull("contact_folder")) {
                    throw AjaxExceptionCodes.MISSING_PARAMETER.create("contact_folder");
                }
                guestRecipient.setContactFolder(jsonRecipient.getString("contact_folder"));
            }
            recipient = guestRecipient;
            break;
        default:
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create("type", jsonRecipient.getString("type"));
        }
        /*
         * parse common properties
         */
        if (false == jsonRecipient.hasAndNotNull("bits")) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("bits");
        }
        int bits = jsonRecipient.getInt("bits");
        recipient.setBits(bits);
        if (jsonRecipient.hasAndNotNull("activation_date")) {
            recipient.setActivationDate(new Date(jsonRecipient.getLong("activation_date")));
        }
        if (jsonRecipient.hasAndNotNull("expiry_date")) {
            recipient.setExpiryDate(new Date(jsonRecipient.getLong("expiry_date")));
        }
        return recipient;
    }

}
