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

package com.openexchange.switchboard;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.json.JSONException;
import org.json.JSONInputStream;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.Conference;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ExtendedPropertyParameter;
import com.openexchange.chronos.common.DataHandlers;
import com.openexchange.conversion.ConversionResult;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataHandler;
import com.openexchange.conversion.SimpleData;
import com.openexchange.exception.OXException;
import com.openexchange.java.Autoboxing;
import com.openexchange.rest.client.httpclient.HttpClientService;
import com.openexchange.rest.client.httpclient.HttpClients;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.switchboard.exception.ZoomExceptionCodes;
import com.openexchange.switchboard.osgi.Services;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.FailsafeException;
import net.jodah.failsafe.RetryPolicy;

/**
 * {@link Switchboard}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.4
 */
public class Switchboard {

    private static final Logger LOG = LoggerFactory.getLogger(Switchboard.class);

    private static final String UPDATED = "conference.updated";
    private static final String DELETED = "conference.deleted";

    private SwitchboardConfiguration config;

    public Switchboard(SwitchboardConfiguration config) {
        this.config = config;
    }

    /**
     * Sends a update notification.
     *
     * @param conference The updated conference
     * @param timestamp The timestamp
     */
    public void update(Conference conference, Event update, long timestamp) {
        try {
            post(serialize(UPDATED, conference, update, timestamp));
            LOG.info("Successfully sent {} for event {} and conference {} to switchboard.", UPDATED, update.getId(), I(conference.getId()));
        } catch (JSONException | OXException e) {
            LOG.error("Unable to send conference update to the switchboard.", e);
        }
    }

    /**
     * Sends a delete notification.
     *
     * @param conference The deleted conference
     * @param original The original deleted event
     * @param timestamp The timestamp
     */
    public void delete(Conference conference, Event original, long timestamp) {
        try {
            post(serialize(DELETED, conference, original, timestamp));
            LOG.info("Successfully sent {} for conference {} to switchboard.", DELETED, I(conference.getId()));
        } catch (JSONException | OXException e) {
            LOG.error("Unable to send conference delete to the switchboard.", e);
        }
    }

    private static final String AUTHORIZATION_HEADER = "authorization";
    private static final String ID_PROPERTY = "X-OX-ID";
    private static final String OWNER_PROPERTY = "X-OX-OWNER";

    private void post(JSONObject json) throws OXException {
        try {
            Failsafe.with(new RetryPolicy().withMaxRetries(5).withBackoff(1000, 10000, TimeUnit.MILLISECONDS).withJitter(0.25f).retryOn(f -> OXException.class.isInstance(f) && ZoomExceptionCodes.SWITCHBOARD_SERVER_ERROR.equals((OXException) f))).onRetry(f -> LOG.info("Error posting event to Switchboard API, trying again.", f)).run(() -> doPost(json));
        } catch (FailsafeException e) {
            if (OXException.class.isInstance(e.getCause())) {
                throw (OXException) e.getCause();
            }
            throw e;
        }
    }

    private void doPost(JSONObject json) throws OXException {
        HttpClient client = Services.getService(HttpClientService.class).getHttpClient("zoom_conference");
        HttpPost post = null;
        HttpResponse response = null;
        try {
            post = new HttpPost(config.getUri());
            post.setEntity(new InputStreamEntity(new JSONInputStream(json, com.openexchange.java.Charsets.UTF_8_NAME), json.toString().length(), ContentType.APPLICATION_JSON));
            post.addHeader(AUTHORIZATION_HEADER, config.getWebhookSecret());

            long start = System.nanoTime();
            LOG.trace(">> POST {}{}   {}", post.getURI(), System.lineSeparator(), json);
            response = client.execute(post);
            StatusLine statusLine = response.getStatusLine();
            LOG.trace("<< {}, {} ms elapsed.", statusLine, Autoboxing.L(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)));
            int statusCode = statusLine.getStatusCode();
            if (statusCode < 200 || statusCode > 299) {
                if (500 <= statusCode && statusCode < 600) {
                    throw ZoomExceptionCodes.SWITCHBOARD_SERVER_ERROR.create(statusLine.getReasonPhrase());
                }
                throw ZoomExceptionCodes.SWITCHBOARD_ERROR.create(statusLine.getReasonPhrase());
            }
        } catch (IOException e) {
            throw ZoomExceptionCodes.IO_ERROR.create(e);
        } finally {
            HttpClients.close(post, response);
        }
    }

    private JSONObject serialize(String action, Conference conference, Event event, long timestamp) throws OXException, JSONException {
        JSONObject payload = new JSONObject();
        payload.putSafe("meetingId", getExtendedParameter(conference, ID_PROPERTY));
        payload.putSafe("owner", getExtendedParameter(conference, OWNER_PROPERTY));
        payload.putSafe("type", "zoom");

        if (action.equals(UPDATED)) {
            JSONObject eventJson = convertEvent(event);
            payload.putSafe("startDate", eventJson.getJSONObject("startDate"));
            payload.putSafe("endDate", eventJson.getJSONObject("endDate"));
            if (eventJson.hasAndNotNull("summary")) {
                payload.putSafe("summary", eventJson.get("summary"));
            }
            if (eventJson.hasAndNotNull("description")) {
                payload.putSafe("description", eventJson.get("description"));
            }
            payload.putSafe("appointment", eventJson);
        } else if (action.equals(DELETED)) {
            if (event != null) {
                JSONObject eventJson = convertEvent(event);
                payload.putSafe("appointment", eventJson);
            }
        }
        return new JSONObject(2).putSafe("event", action).putSafe("timestamp", L(timestamp)).putSafe("payload", payload);
    }

    private JSONObject convertEvent(Event event) throws OXException {
        ConversionService conversionService = Services.getService(ConversionService.class, true);
        DataHandler handler = conversionService.getDataHandler(DataHandlers.EVENT2JSON);
        if (null == handler) {
            throw ServiceExceptionCode.absentService(DataHandler.class);
        }
        ConversionResult result = handler.processData(new SimpleData<Event>(event, null), new DataArguments(), null);
        JSONObject eventJson = (JSONObject) result.getData();
        return eventJson;
    }

    private String getExtendedParameter(Conference conference, String param) {
        if (conference == null || conference.getExtendedParameters() == null || conference.getExtendedParameters().isEmpty()) {
            return null;
        }

        Optional<ExtendedPropertyParameter> optional = conference.getExtendedParameters().stream().filter(p -> p.getName().equals(param)).findAny();
        if (optional.isPresent()) {
            return optional.get().getValue();
        }

        return null;
    }

}
