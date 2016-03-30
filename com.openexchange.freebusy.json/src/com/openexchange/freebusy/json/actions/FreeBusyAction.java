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

package com.openexchange.freebusy.json.actions;

import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.exception.OXException;
import com.openexchange.freebusy.FreeBusyData;
import com.openexchange.freebusy.FreeBusyInterval;
import com.openexchange.freebusy.json.FreeBusyRequest;
import com.openexchange.freebusy.service.FreeBusyService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link FreeBusyAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class FreeBusyAction implements AJAXActionService {

    private final ServiceLookup serviceLookup;

    /**
     * Initializes a new {@link FreeBusyAction}.
     *
     * @param serviceLookup The service lookup to use
     */
    public FreeBusyAction(ServiceLookup serviceLookup) {
        super();
        this.serviceLookup = serviceLookup;
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        return this.perform(new FreeBusyRequest(requestData, session));
    }

    /**
     * Performs the free/busy request.
     *
     * @param request The request
     * @return The AJAX result
     * @throws OXException
     */
    protected abstract AJAXRequestResult perform(FreeBusyRequest request) throws OXException;

    /**
     * Gets the free/busy service.
     *
     * @return The free/busy service
     * @throws OXException
     */
    protected FreeBusyService getFreeBusyService() throws OXException {
        return serviceLookup.getService(FreeBusyService.class);
    }

    protected JSONObject serialize(Map<String, FreeBusyData> freeBusyData, TimeZone timeZone, Locale locale) throws OXException {
        try {
            JSONObject jsonObject = new JSONObject();
            for (Entry<String, FreeBusyData> entry : freeBusyData.entrySet()) {
                jsonObject.put(entry.getKey(), serialize(entry.getValue(), timeZone, locale));
            }
            return jsonObject;
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    protected JSONObject serialize(FreeBusyData freeBusyData, TimeZone timeZone, Locale locale) throws OXException {
        try {
            JSONObject jsonObject = new JSONObject();
            if (freeBusyData.hasWarnings()) {
                ResponseWriter.addWarnings(jsonObject, freeBusyData.getWarnings(), locale);
            }
            jsonObject.put("data", serialize(freeBusyData.getIntervals(), timeZone));
            return jsonObject;
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    protected JSONObject serialize(FreeBusyInterval freeBusyInterval, TimeZone timeZone) throws OXException {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("start_date", serialize(freeBusyInterval.getStartTime(), timeZone));
            jsonObject.put("end_date", serialize(freeBusyInterval.getEndTime(), timeZone));
            jsonObject.put("shown_as", freeBusyInterval.getStatus().getValue());
            if (null != freeBusyInterval.getObjectID()) {
                jsonObject.put("id", freeBusyInterval.getObjectID());
            }
            if (null != freeBusyInterval.getFolderID()) {
                jsonObject.put("folder_id", freeBusyInterval.getFolderID());
            }
            if (freeBusyInterval.isFullTime()) {
                jsonObject.put("full_time", freeBusyInterval.getFolderID());
            }
            if (null != freeBusyInterval.getTitle()) {
                jsonObject.put("title", freeBusyInterval.getTitle());
            }
            if (null != freeBusyInterval.getLocation()) {
                jsonObject.put("location", freeBusyInterval.getLocation());
            }
            return jsonObject;
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    protected JSONArray serialize(Iterable<FreeBusyInterval> freeBusyIntervals, TimeZone timeZone) throws OXException {
        JSONArray jsonArray = new JSONArray();
        if (null != freeBusyIntervals) {
            for (FreeBusyInterval freeBusyInterval : freeBusyIntervals) {
                jsonArray.put(serialize(freeBusyInterval, timeZone));
            }
        }
        return jsonArray;
    }

    protected Object serialize(Date date, TimeZone timeZone) throws JSONException {
        if (null == date) {
            return JSONObject.NULL;
        } else {
            long time = date.getTime();
            return null == timeZone ? time : time + timeZone.getOffset(time);
        }
    }

}
