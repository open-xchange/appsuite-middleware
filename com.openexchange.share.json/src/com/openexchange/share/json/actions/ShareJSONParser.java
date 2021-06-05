/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
                InternetAddress addr = new QuotedInternetAddress(address);
                addr.validate();
                return addr;
            } else if (jRecipient.length() == 2) {
                address = jRecipient.getString(1);
                InternetAddress addr = new QuotedInternetAddress(address, jRecipient.getString(0), "UTF-8");
                addr.validate();
                return addr;
            }

            throw ShareExceptionCodes.INVALID_MAIL_ADDRESS.create(jRecipient.get(0));
        } catch (AddressException | UnsupportedEncodingException e) {
            throw ShareExceptionCodes.INVALID_MAIL_ADDRESS.create(e, null != address ? address : jRecipient.opt(0));
        }
    }

}
