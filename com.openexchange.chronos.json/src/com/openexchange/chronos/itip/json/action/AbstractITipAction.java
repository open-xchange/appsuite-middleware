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

package com.openexchange.chronos.itip.json.action;

import static com.openexchange.chronos.itip.json.action.Utils.initCalendarSession;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.common.collect.ImmutableSet;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.chronos.itip.ITipAnalysis;
import com.openexchange.chronos.itip.ITipAnalyzerService;
import com.openexchange.chronos.itip.IncomingSchedulingMailData;
import com.openexchange.chronos.itip.IncomingSchedulingMailFactory;
import com.openexchange.chronos.scheduling.IncomingSchedulingMessage;
import com.openexchange.chronos.scheduling.SchedulingMethod;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.config.ConfigurationService;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataProperties;
import com.openexchange.conversion.DataSource;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 *
 * {@link AbstractITipAction}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public abstract class AbstractITipAction implements AJAXActionService {

    private static final String OWNER = "com.openexchange.conversion.owner";

    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractITipAction.class);

    protected ServiceLookup services;

    protected RankingAwareNearRegistryServiceTracker<ITipAnalyzerService> analyzerListing;

    private final ImmutableSet<IncomingSchedulingAction> schedulingActions;

    /**
     * Initializes a new {@link AbstractITipAction}.
     * 
     * @param services The service lookup
     * @param analyzerListing The service tracker for {@link ITipAnalyzerService}
     */
    public AbstractITipAction(final ServiceLookup services, RankingAwareNearRegistryServiceTracker<ITipAnalyzerService> analyzerListing) {
        this.services = services;
        this.analyzerListing = analyzerListing;
        ImmutableSet.Builder<IncomingSchedulingAction> builder = ImmutableSet.builder();
        builder.add(new IncomingSchedulingAction(SchedulingMethod.ADD, services));
        builder.add(new IncomingSchedulingAction(SchedulingMethod.CANCEL, services));
        builder.add(new IncomingSchedulingAction(SchedulingMethod.REPLY, services));
        builder.add(new IncomingSchedulingAction(SchedulingMethod.REQUEST, services));
        this.schedulingActions = builder.build();
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        AJAXRequestResult result = performSchedulingAction(requestData, session);
        if (null != result) {
            return result;
        }
        ITipAnalyzerService analyzer = getAnalyzerService();
        TimeZone tz = Utils.getTimeZone(requestData, session);

        Map<String, String> mailHeader = new HashMap<String, String>();
        InputStream stream = null;
        try {
            stream = getInputStreamAndFillMailHeader(requestData, session, mailHeader);
            List<ITipAnalysis> analysis = analyzer.analyze(stream, requestData.getParameter("descriptionFormat"), initCalendarSession(services, session), mailHeader);
            return process(analysis, requestData, session, tz);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e);
        } finally {
            Streams.close(stream);
        }
    }

    private ITipAnalyzerService getAnalyzerService() throws OXException {
        List<ITipAnalyzerService> serviceList = analyzerListing.getServiceList();
        if (serviceList == null || serviceList.isEmpty()) {
            throw ServiceExceptionCode.serviceUnavailable(ITipAnalyzerService.class);
        }
        ITipAnalyzerService analyzer = serviceList.get(0);
        if (analyzer == null) {
            throw ServiceExceptionCode.serviceUnavailable(ITipAnalyzerService.class);
        }
        return analyzer;
    }

    protected abstract AJAXRequestResult process(List<ITipAnalysis> analysis, AJAXRequestData request, ServerSession session, TimeZone tz) throws JSONException, OXException;

    private InputStream getInputStreamAndFillMailHeader(final AJAXRequestData request, final ServerSession session, final Map<String, String> mailHeader) throws OXException {
        final String ds = request.getParameter("dataSource");
        if (ds != null) {
            final DataArguments dataArguments = getDataSource(request);

            final ConversionService conversionEngine = services.getService(ConversionService.class);
            if (null == conversionEngine) {
                throw ServiceExceptionCode.serviceUnavailable(ConversionService.class);
            }

            final DataSource dataSource = conversionEngine.getDataSource(ds);

            final Data<InputStream> dsData = dataSource.getData(InputStream.class, dataArguments, session);
            fillMailHeader(dsData, mailHeader);
            return dsData.getData();
        }
        final Object data = request.getData();
        if (data != null) {
            final JSONObject body = (JSONObject) data;
            try {
                return new ByteArrayInputStream(body.getString("ical").getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                LOG.error("", e);
                return null;
            } catch (JSONException x) {
                throw AjaxExceptionCodes.JSON_ERROR.create(x);
            }
        }
        return null;
    }

    protected DataArguments getDataSource(final AJAXRequestData request) {
        final DataArguments dataArguments = new DataArguments();

        final Object data = request.getData();
        if (data != null) {
            final JSONObject body = (JSONObject) data;
            for (final Map.Entry<String, Object> entry : body.entrySet()) {
                dataArguments.put(entry.getKey(), entry.getValue().toString());
            }
        } else {
            for (final Map.Entry<String, String> entry : request.getParameters().entrySet()) {
                dataArguments.put(entry.getKey(), entry.getValue());
            }
        }
        return dataArguments;
    }

    private void fillMailHeader(final Data<InputStream> dsData, final Map<String, String> mailHeader) {
        final Map<String, String> properties = dsData.getDataProperties().toMap();

        for (final Entry<String, String> entry : properties.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(DataProperties.PROPERTY_EMAIL_HEADER_PREFIX)) {
                mailHeader.put(key.substring(DataProperties.PROPERTY_EMAIL_HEADER_PREFIX.length() + 1, key.length()), entry.getValue());
            }

            if (key.equals(OWNER)) {
                mailHeader.put(OWNER, entry.getValue());
            }
            if (key.equalsIgnoreCase("from")) {
                mailHeader.put("from", entry.getValue());
            }
        }
    }

    /**
     * Tries to perform the action via the scheduling stack
     *
     * @param requestData The request data to get the action from.
     * @return A {@link AJAXRequestResult} containing data of the updated resources or <code>null</code>
     * @throws OXException In case of an error while serving the request
     */
    private AJAXRequestResult performSchedulingAction(AJAXRequestData requestData, ServerSession session) throws OXException {
        if (ITipActionFactory.ANALYZE.equals(requestData.getAction())) {
            return null;
        }
        if (isLegacyScheduling()) {
            return null;
        }

        CalendarSession calendarSession = Utils.initCalendarSession(services, session);
        IncomingSchedulingMailFactory factory = services.getServiceSafe(IncomingSchedulingMailFactory.class);
        IncomingSchedulingMessage message = factory.createPatched(calendarSession, IncomingSchedulingMailData.fromRequest(requestData));
        for (IncomingSchedulingAction action : schedulingActions) {
            if (action.canPerform(message)) {
                return action.perform(requestData, message, calendarSession, Utils.getTimeZone(requestData, session));
            }
        }
        return null;
    }

    private boolean isLegacyScheduling() {
        ConfigurationService configurationService = services.getService(ConfigurationService.class);
        return null == configurationService || configurationService.getBoolProperty("com.openexchange.calendar.useLegacyScheduling", false);
    }

}
