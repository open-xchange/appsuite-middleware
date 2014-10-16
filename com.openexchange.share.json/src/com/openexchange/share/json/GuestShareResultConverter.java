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

package com.openexchange.share.json;

import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.Share;
import com.openexchange.share.ShareCryptoService;
import com.openexchange.share.recipient.RecipientType;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link GuestShareResultConverter}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class GuestShareResultConverter implements ResultConverter {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link GuestShareResultConverter}.
     *
     * @param services The service lookup
     */
    public GuestShareResultConverter(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public String getInputFormat() {
        return "guestshare";
    }

    @Override
    public String getOutputFormat() {
        return "json";
    }

    @Override
    public Quality getQuality() {
        return Quality.GOOD;
    }

    @Override
    public void convert(AJAXRequestData requestData, AJAXRequestResult result, ServerSession session, Converter converter) throws OXException {
    	/*
    	 * determine timezone
    	 */
        String timeZoneID = requestData.getParameter("timezone");
        if (null == timeZoneID) {
        	timeZoneID = session.getUser().getTimeZone();
        }
        TimeZone timeZone = TimeZone.getTimeZone(timeZoneID);
        /*
         * convert result object
         */
        Object resultObject = result.getResultObject();
        if (GuestShare.class.isInstance(resultObject)) {
            resultObject = convert((GuestShare)resultObject, timeZone);
        } else {
            resultObject = convert((List<GuestShare>) resultObject, timeZone);
        }
        result.setResultObject(resultObject, "json");
    }

    /**
     * Serializes multiple guest shares to JSON.
     *
     * @param shares The guest shares to serialize
     * @param timeZone The client timezone
     * @return The serialized guest shares
     */
    private JSONArray convert(List<GuestShare> shares, TimeZone timeZone) throws OXException {
        JSONArray jsonArray = new JSONArray(shares.size());
        for (GuestShare share : shares) {
            jsonArray.put(convert(share, timeZone));
        }
        return jsonArray;
    }

    /**
     * Serializes a guest share to JSON.
     *
     * @param guestShare The guest share to serialize
     * @param timeZone The client timezone
     * @return The serialized guest share
     */
    private JSONObject convert(GuestShare guestShare, TimeZone timeZone) throws OXException {
        try {
            JSONObject json = new JSONObject();
            /*
             * common share properties
             */
            Share share = guestShare.getShare();
            json.putOpt("token", share.getToken());
            json.putOpt("share_url", guestShare.getShareURL());
            json.putOpt("authentication", null != share.getAuthentication() ? share.getAuthentication().toString().toLowerCase() : null);
            json.putOpt("created", null != share.getCreated() ? addTimeZoneOffset(share.getCreated().getTime(), timeZone) : null);
            json.put("created_by", share.getCreatedBy());
            json.putOpt("last_modified", null != share.getLastModified() ? addTimeZoneOffset(share.getLastModified().getTime(), timeZone) : null);
            json.put("modified_by", share.getModifiedBy());
            /*
             * share target & recipient
             */
            json.put("target", serializeShareTarget(guestShare));
            json.put("recipient", serializeShareRecipient(guestShare, timeZone));
            return json;
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e);
        }
    }

    /**
     * Extracts the share target from a guest share and serializes it to JSON.
     *
     * @param guestShare The guest share to serialize the share recipient for
     * @param timeZone The client timezone
     * @return The serialized share target
     * @throws JSONException
     */
    private JSONObject serializeShareTarget(GuestShare guestShare) throws JSONException {
        JSONObject jsonTarget = new JSONObject(3);
        //TODO
//        Module module = Module.getForFolderConstant(guestShare.getShare().getModule());
//        jsonTarget.putOpt("module", null != module ? module.getName() : null);
//        jsonTarget.putOpt("folder", guestShare.getShare().getFolder());
//        jsonTarget.putOpt("item", guestShare.getShare().getItem());
        return jsonTarget;
    }

    /**
     * Extracts the share recipient from a guest share and serializes it to JSON.
     *
     * @param guestShare The guest share to serialize the share recipient for
     * @param timeZone The client timezone
     * @return The serialized share recipient
     * @throws OXException
     */
    private JSONObject serializeShareRecipient(GuestShare guestShare, TimeZone timeZone) throws JSONException, OXException {
        JSONObject jsonRecipient = new JSONObject(8);
        switch (guestShare.getShare().getAuthentication()) {
        case ANONYMOUS:
            jsonRecipient.put("type", RecipientType.ANONYMOUS.toString().toLowerCase());
            break;
        case ANONYMOUS_PASSWORD:
            jsonRecipient.put("type", RecipientType.ANONYMOUS.toString().toLowerCase());
            String cryptedPassword = guestShare.getGuest().getUserPassword();
            if (false == Strings.isEmpty(cryptedPassword)) {
                jsonRecipient.put("password", services.getService(ShareCryptoService.class).decrypt(cryptedPassword));
            }
            break;
        case GUEST_PASSWORD:
            jsonRecipient.put("type", RecipientType.GUEST.toString().toLowerCase());
            jsonRecipient.put("email_address", guestShare.getGuest().getMail());
            jsonRecipient.put("display_name", guestShare.getGuest().getDisplayName());
            if (0 < guestShare.getGuest().getContactId()) {
                jsonRecipient.put("contact_id", String.valueOf(guestShare.getGuest().getContactId()));
                jsonRecipient.put("contact_folder", String.valueOf(FolderObject.VIRTUAL_GUEST_CONTACT_FOLDER_ID));
            }
            break;
        default:
            throw new UnsupportedOperationException("Unsupported authentication: " + guestShare.getShare().getAuthentication());
        }
        jsonRecipient.put("entity", String.valueOf(guestShare.getGuest().getId()));
        //TODO
//        Date activationDate = guestShare.getShare().getActivationDate();
//        if (null != activationDate) {
//            jsonRecipient.put("activation_date", addTimeZoneOffset(activationDate.getTime(), timeZone));
//        }
//        Date expiryDate = guestShare.getShare().getExpiryDate();
//        if (null != expiryDate) {
//            jsonRecipient.put("expiry_date", addTimeZoneOffset(expiryDate.getTime(), timeZone));
//        }
        return jsonRecipient;
    }

    private static long addTimeZoneOffset(final long date, final TimeZone timeZone) {
        return null == timeZone ? date : date + timeZone.getOffset(date);
    }

}
