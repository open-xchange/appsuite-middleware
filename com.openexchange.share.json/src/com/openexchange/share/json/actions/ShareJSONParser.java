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
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.exception.OXException;
import com.openexchange.java.Enums;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.groupware.ModuleSupport;
import com.openexchange.share.recipient.GuestRecipient;
import com.openexchange.share.recipient.InternalRecipient;
import com.openexchange.share.recipient.RecipientType;
import com.openexchange.share.recipient.ShareRecipient;
import com.openexchange.tools.servlet.AjaxExceptionCodes;


/**
 * {@link ShareJSONParser}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class ShareJSONParser {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link ShareJSONParser}.
     *
     * @param services A service lookup reference
     */
    public ShareJSONParser(ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * Parses a list of share targets from the supplied JSON array.
     *
     * @param jsonTargets The JSON array holding the share targets
     * @return The share targets
     */
    public List<ShareTarget> parseTargets(JSONArray jsonTargets) throws OXException {
        try {
            if (null == jsonTargets || 0 == jsonTargets.length()) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("targets");
            }
            List<ShareTarget> targets = new ArrayList<ShareTarget>();
            for (int i = 0; i < jsonTargets.length(); i++) {
                targets.add(parseTarget(jsonTargets.getJSONObject(i)));
            }
            return targets;
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage());
        }
    }

    /**
     * Parses a share target from the supplied JSON object.
     *
     * @param jsonTargets The JSON object holding the share target
     * @return The parsed share target
     */
    public ShareTarget parseTarget(JSONObject jsonTarget) throws OXException {
        try {
            if (false == jsonTarget.hasAndNotNull("module")) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("module");
            }
            int module = services.getService(ModuleSupport.class).getShareModuleId(jsonTarget.getString("module"));
            if (false == jsonTarget.hasAndNotNull("folder")) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("folder");
            }
            String folder = jsonTarget.getString("folder");
            ShareTarget target;
            if (jsonTarget.hasAndNotNull("item")) {
                target = new ShareTarget(module, folder, jsonTarget.getString("item"));
            } else {
                target = new ShareTarget(module, folder);
            }
            return target;
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage());
        }
    }

    /**
     * Parses arbitrary metadata from the supplied JSON object.
     *
     * @param jsonMeta The JSON object holding the metadata
     * @return The parsed metadata
     */
    public Map<String, Object> parseMeta(JSONObject jsonMeta) throws OXException {
        try {
            return (Map<String, Object>) JSONCoercion.coerceToNative(jsonMeta);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage());
        }
    }

    /**
     * Removes the timezone offset from the supplied timestamp.
     *
     * @param date The timestamp to remove the timezone offset from
     * @param timeZone The timezone
     * @return The timestamp with removed timezone offset
     */
    public long removeTimeZoneOffset(long date, TimeZone timeZone) {
        return null == timeZone ? date : date - timeZone.getOffset(date);
    }

    /**
     * Parses a list of share recipients from the supplied JSON array.
     *
     * @param jsonRecipients The JSON array holding the share recipients
     * @return The share recipients
     */
    public List<ShareRecipient> parseRecipients(JSONArray jsonRecipients) throws OXException {
        try {
            if (null == jsonRecipients || 0 == jsonRecipients.length()) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("recipients");
            }
            List<ShareRecipient> recipients = new ArrayList<ShareRecipient>();
            for (int i = 0; i < jsonRecipients.length(); i++) {
                recipients.add(parseRecipient(jsonRecipients.getJSONObject(i)));
            }
            return recipients;
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage());
        }
    }

    /**
     * Parses a share recipient from the supplied JSON object.
     *
     * @param jsonTargets The JSON object holding the share recipient
     * @return The share recipient
     */
    public ShareRecipient parseRecipient(JSONObject jsonRecipient) throws OXException, JSONException {
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
            if (false == jsonRecipient.hasAndNotNull("entity")) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("entity");
            }
            internalRecipient.setEntity(jsonRecipient.getInt("entity"));
            recipient = internalRecipient;
            break;
        case GUEST:
            GuestRecipient guestRecipient = new GuestRecipient();
            if (false == jsonRecipient.hasAndNotNull("email_address")) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("email_address");
            }
            guestRecipient.setEmailAddress(jsonRecipient.getString("email_address"));
            if (jsonRecipient.hasAndNotNull("override_password")) {
                guestRecipient.setPassword(jsonRecipient.getString("override_password"));
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

        return recipient;
    }

}
