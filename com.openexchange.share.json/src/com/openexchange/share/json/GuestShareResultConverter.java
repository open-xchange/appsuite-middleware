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

import java.util.Date;
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
import com.openexchange.groupware.ldap.User;
import com.openexchange.session.Session;
import com.openexchange.share.Share;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link GuestShareResultConverter}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class GuestShareResultConverter implements ResultConverter {

    /**
     * Initializes a new {@link GuestShareResultConverter}.
     */
    public GuestShareResultConverter() {
        super();
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
            resultObject = convert((GuestShare)resultObject, timeZone, session);
        } else {
            resultObject = convert((List<GuestShare>) resultObject, timeZone, session);
        }
        result.setResultObject(resultObject, "json");
    }

    private JSONArray convert(List<GuestShare> shares, TimeZone timeZone, Session session) throws OXException {
        JSONArray jsonArray = new JSONArray(shares.size());
        for (GuestShare share : shares) {
            jsonArray.put(convert(share, timeZone, session));
        }
        return jsonArray;
    }

    private static long addTimeZoneOffset(final long date, final TimeZone timeZone) {
        return null == timeZone ? date : date + timeZone.getOffset(date);
    }

    private JSONObject convert(GuestShare guestShare, TimeZone timeZone, Session session) throws OXException {
        try {
            JSONObject json = new JSONObject();
            Share share = guestShare.getShare();
            json.putOpt("token", share.getToken());
            if (0 != share.getModule()) {
                json.put("module", share.getModule());
            }
            json.putOpt("folder", share.getFolder());
            json.putOpt("item", share.getItem());
            Date created = share.getCreated();
            if (null != created) {
                json.put("created", addTimeZoneOffset(created.getTime(), timeZone));
            }
            if (0 != share.getCreatedBy()) {
                json.put("created_by", share.getCreatedBy());
            }
            Date lastModified = share.getLastModified();
            if (null != lastModified) {
                json.put("last_modified", addTimeZoneOffset(lastModified.getTime(), timeZone));
            }
            if (0 != share.getModifiedBy()) {
                json.put("modified_by", share.getModifiedBy());
            }
            Date expires = share.getExpiryDate();
            if (null != expires) {
                json.put("expires", share.getExpiryDate().getTime());
            }
            if (0 != share.getGuest()) {
                json.put("guest", share.getGuest());
            }
            json.put("authentication", share.getAuthentication().toString().toLowerCase());
            User guest = guestShare.getGuest();
            if (null != guest) {
                json.putOpt("guest_mail_address", guest.getMail());
                json.putOpt("guest_display_name", guest.getDisplayName());
                json.putOpt("guest_password", guestShare.getGuestPassword());
            }
            json.putOpt("share_url", guestShare.getShareURL());
            return json;
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e);
        }
    }

}
