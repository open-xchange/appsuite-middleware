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

package com.openexchange.freebusy.provider.google.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.freebusy.BusyStatus;
import com.openexchange.freebusy.FreeBusyData;
import com.openexchange.freebusy.FreeBusyExceptionCodes;
import com.openexchange.freebusy.FreeBusyInterval;


/**
 * {@link JsonClient}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class JsonClient {

    private static final ThreadLocal<DateFormat> DATE_FORMAT = new ThreadLocal<DateFormat>() {

        @Override
        protected DateFormat initialValue() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            return dateFormat;
        }
    };

    private final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(JsonClient.class);

    private final String apiKey;
    private final String apiEndpoint;
    private final HttpClient httpClient;

    /**
     * Initializes a new {@link JsonClient}.
     */
    public JsonClient(String apiEndpoint, String apiKey) {
        super();
        this.apiKey = apiKey;
        this.apiEndpoint = apiEndpoint;
        this.httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
    }

    public List<FreeBusyData> getFreeBusy(List<String> ids, Date from, Date until) throws OXException {
        JSONObject freeBusyRequest = this.createFreeBusyRequest(ids, from, until);
        JSONObject response = getAndCheckResponse(getRequestURL(), freeBusyRequest);
        return this.deserializeResponse(response, ids, from, until);
    }

    private List<FreeBusyData> deserializeResponse(JSONObject response, List<String> ids, Date from, Date until) {
        List<FreeBusyData> freeBusyInformation = new ArrayList<FreeBusyData>();
        if (null != response && response.hasAndNotNull("calendars")) {
            JSONObject calendars = response.optJSONObject("calendars");
            if (null != calendars) {
                for (String id : ids) {
                    freeBusyInformation.add(deserializeData(id, from, until, calendars.optJSONObject(id)));
                }
            }
        }
        return freeBusyInformation;
    }

    private FreeBusyData deserializeData(String id, Date from, Date until, JSONObject data) {
        FreeBusyData freeBusyData = new FreeBusyData(id, from, until);
        if (null != data) {
            try {
                if (data.hasAndNotNull("busy")) {
                    JSONArray busy = data.getJSONArray("busy");
                    for (int i = 0; i < busy.length(); i++) {
                        JSONObject interval = busy.getJSONObject(i);
                        Date start = DATE_FORMAT.get().parse(interval.getString("start"));
                        Date end = DATE_FORMAT.get().parse(interval.getString("end"));
                        freeBusyData.add(new FreeBusyInterval(start, end, BusyStatus.RESERVED));
                    }
                }
                if (data.hasAndNotNull("errors")) {
                    JSONArray errors = data.getJSONArray("errors");
                    for (int i = 0; i < errors.length(); i++) {
                        freeBusyData.addWarning(deserializeError(id, errors.getJSONObject(i)));
                    }
                }
            } catch (JSONException e) {
                freeBusyData.addWarning(FreeBusyExceptionCodes.INTERNAL_ERROR.create(e, e.getMessage()));
            } catch (ParseException e) {
                freeBusyData.addWarning(FreeBusyExceptionCodes.INTERNAL_ERROR.create(e, e.getMessage()));
            }
        } else {
            freeBusyData.addWarning(FreeBusyExceptionCodes.DATA_NOT_AVAILABLE.create(id));
        }
        return freeBusyData;
    }

    private OXException deserializeError(String id, JSONObject error) throws JSONException {
        String reason = error.hasAndNotNull("reason") ? error.getString("reason") : null;
        String domain = error.hasAndNotNull("domain") ? error.getString("domain") : null;
        if (null != reason && "notFound".equals(reason)) {
            return FreeBusyExceptionCodes.PARTICIPANT_NOT_FOUND.create(id);
        }
        return FreeBusyExceptionCodes.EXTERNAL_ERROR.create("Reason: " + reason + ", Domain: " + domain);
    }

    private JSONObject createFreeBusyRequest(List<String> ids, Date from, Date until) throws OXException {
        try {
            JSONObject freeBusyRequest = new JSONObject();
            freeBusyRequest.put("timeMin", DATE_FORMAT.get().format(from));
            freeBusyRequest.put("timeMax", DATE_FORMAT.get().format(until));
            JSONArray items = new JSONArray();
            for (String id : ids) {
                JSONObject item = new JSONObject();
                item.put("id", id);
                items.put(item);
            }
            freeBusyRequest.put("items", items);
            return freeBusyRequest;
        } catch (JSONException e) {
            throw FreeBusyExceptionCodes.INTERNAL_ERROR.create(e, e.getMessage());
        }
    }

    private String getRequestURL() {
        return String.format("%s/freeBusy?key=%s", apiEndpoint, apiKey);
    }

    private JSONObject getResponse(String requestURL, JSONObject request) throws OXException {
        final boolean traceEnabled = LOG.isTraceEnabled();
        long start = 0;
        if (traceEnabled) {
            start = System.currentTimeMillis();
            LOG.trace("==> POST {}{}  > {}", requestURL, System.getProperty("line.separator"), request);
        }
        PostMethod method = createPostMethod(requestURL, request);
        executeMethod(method);
        try {
            //TODO: upgrade our json.jar
            //JSONObject response = new JSONObject(new JSONTokener(new InputStreamReader(method.getResponseBodyAsStream())));
            String body = method.getResponseBodyAsString();
            if (traceEnabled) {
                LOG.trace("<== {} ({}ms elapsed){}<  {}", method.getStatusLine(), System.currentTimeMillis() - start, System.getProperty("line.separator"), body);
            }
            return null != body ? new JSONObject(body) : null;
        } catch (IOException e) {
            throw FreeBusyExceptionCodes.COMMUNICATION_FAILURE.create(e, e.getMessage());
        } catch (JSONException e) {
            throw FreeBusyExceptionCodes.COMMUNICATION_FAILURE.create(e, e.getMessage());
        } finally {
            method.releaseConnection();
        }
    }

    private JSONObject getAndCheckResponse(String requestURL, JSONObject request) throws OXException {
        JSONObject jsonData = getResponse(requestURL, request);
        if (null == jsonData) {
            throw FreeBusyExceptionCodes.COMMUNICATION_FAILURE.create("got no response");
        }
        if (jsonData.hasAndNotNull("error")) {
            try {
                StringBuilder stringBuilder = new StringBuilder();
                JSONObject error = jsonData.getJSONObject("error");
                if (error.hasAndNotNull("message")) {
                    stringBuilder.append(error.getString("message")).append(' ');
                }
                if (error.hasAndNotNull("code")) {
                    stringBuilder.append("(Code: ").append(error.getString("code")).append(") ");
                }
                if (error.hasAndNotNull("errors")) {
                    JSONArray errors = error.getJSONArray("errors");
                    for (int i = 0; i < errors.length(); i++) {
                        stringBuilder.append("; ");
                        JSONObject errorObject = errors.getJSONObject(i);
                        if (errorObject.hasAndNotNull("message")) {
                            stringBuilder.append(errorObject.getString("message")).append(' ');
                        }
                        if (errorObject.hasAndNotNull("domain")) {
                            stringBuilder.append("(Domain: ").append(errorObject.getString("domain")).append(") ");
                        }
                        if (errorObject.hasAndNotNull("reason")) {
                            stringBuilder.append("(Reason: ").append(errorObject.getString("reason")).append(") ");
                        }
                    }
                }
                throw FreeBusyExceptionCodes.EXTERNAL_ERROR.create(stringBuilder.toString());
            } catch (JSONException e) {
                throw FreeBusyExceptionCodes.INTERNAL_ERROR.create(e, e.getMessage());
            }
        }

        if (false == "calendar#freeBusy".equals(jsonData.optString("kind"))) {
            throw FreeBusyExceptionCodes.COMMUNICATION_FAILURE.create("got no calendar#freeBusy response");
        }
        return jsonData;
    }

    private int executeMethod(HttpMethod method) throws OXException {
        try {
            return httpClient.executeMethod(method);
        } catch (HttpException e) {
            throw FreeBusyExceptionCodes.COMMUNICATION_FAILURE.create(e, e.getMessage());
        } catch (IOException e) {
            throw FreeBusyExceptionCodes.COMMUNICATION_FAILURE.create(e, e.getMessage());
        }
    }

    private PostMethod createPostMethod(String requestURL, JSONObject request) throws OXException {
        try {
            PostMethod postMethod = new PostMethod();
            postMethod.setURI(new URI(requestURL, false));
            postMethod.setRequestEntity(new StringRequestEntity(request.toString(), "application/json", "UTF-8"));
            return postMethod;
        } catch (UnsupportedEncodingException e) {
            throw FreeBusyExceptionCodes.COMMUNICATION_FAILURE.create(e, e.getMessage());
        } catch (URIException e) {
            throw FreeBusyExceptionCodes.COMMUNICATION_FAILURE.create(e, e.getMessage());
        }
    }

}
