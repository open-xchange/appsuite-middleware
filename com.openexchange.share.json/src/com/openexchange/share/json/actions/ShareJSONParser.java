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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.apache.commons.validator.routines.EmailValidator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.exception.OXException;
import com.openexchange.java.Enums;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.core.tools.ShareTool;
import com.openexchange.share.groupware.ModuleSupport;
import com.openexchange.share.notification.ShareNotificationService.Transport;
import com.openexchange.share.notification.ShareNotifyExceptionCodes;
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
     * Parses the "transport" property of the supplied JSON object, throwing an appropriate exception in case of an unknown transport, as
     * well as falling back to the default <code>mail</code>-transport if none is specified.
     *
     * @param json The JSON object holding the transport
     * @return The transport
     */
    public Transport parseNotificationTransport(JSONObject json) throws OXException {
        String value = json.optString("transport", null);
        if (Strings.isEmpty(value)) {
            return Transport.MAIL;
        }
        try {
            return Enums.parse(Transport.class, value);
        } catch (IllegalArgumentException e) {
            throw ShareNotifyExceptionCodes.UNKNOWN_NOTIFICATION_TRANSPORT.create(e, value);
        }
    }

    /**
     * Parses the transport-specific infos for the supplied array of recipients.
     *
     * @param transport The transport
     * @param recipients The transport-specific JSON data for each recipient
     * @return The transport infos
     */
    public List<Object> parseTransportInfos(Transport transport, JSONArray recipients) throws OXException {
        if (null == recipients || 0 == recipients.length()) {
            return Collections.emptyList();
        }
        List<Object> transportInfos = new ArrayList<Object>(recipients.length());
        for (int i = 0; i < recipients.length(); i++) {
            switch (transport) {
                case MAIL:
                    try {
                        transportInfos.add(parseAddress(recipients.getJSONArray(i)));
                    } catch (JSONException e) {
                        throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage());
                    }
                    break;
                default:
                    throw ShareNotifyExceptionCodes.UNKNOWN_NOTIFICATION_TRANSPORT.create(transport.toString());
            }

        }
        return transportInfos;
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
     * Adds the timezone offset to the supplied timestamp.
     *
     * @param date The timestamp to add the timezone offset to
     * @param timeZone The timezone
     * @return The timestamp with added timezone offset
     */
    public long addTimeZoneOffset(long date, TimeZone timeZone) {
        return null == timeZone ? date : date + timeZone.getOffset(date);
    }

    /**
     * Parses a share recipient from the supplied JSON object.
     *
     * @param jsonRecipient The JSON object holding the share recipient
     * @param timeZone The timezone to use, or <code>null</code> to not apply timezone offsets to parsed timestamps
     * @return The share recipient
     */
    public ShareRecipient parseRecipient(JSONObject jsonRecipient, TimeZone timeZone) throws OXException, JSONException {
        return ShareTool.parseRecipient(jsonRecipient, timeZone);
    }

    private static InternetAddress parseAddress(JSONArray jRecipient) throws JSONException, OXException {
        String address = null;
        try {
            if (jRecipient.length() == 1) {
                address = jRecipient.getString(0);
                if (EmailValidator.getInstance(true).isValid(address)) {
                    return new QuotedInternetAddress(address);
                }
            } else if (jRecipient.length() == 2) {
                address = jRecipient.getString(1);
                if (EmailValidator.getInstance(true).isValid(address)) {
                    return new QuotedInternetAddress(address, jRecipient.getString(0), "UTF-8");
                }
            }

            throw ShareExceptionCodes.INVALID_MAIL_ADDRESS.create(null != address ? address : jRecipient.get(0));
        } catch (AddressException | UnsupportedEncodingException e) {
            throw ShareExceptionCodes.INVALID_MAIL_ADDRESS.create(null != address ? address : jRecipient.get(0));
        }
    }

}
