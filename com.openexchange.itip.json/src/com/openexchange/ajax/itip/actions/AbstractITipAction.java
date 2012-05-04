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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.ajax.itip.actions;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.calendar.itip.ITipAnalysis;
import com.openexchange.calendar.itip.ITipAnalyzerService;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataProperties;
import com.openexchange.conversion.DataSource;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.itip.ITipMessage;
import com.openexchange.data.conversion.ical.itip.ITipParser;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link AbstractITipAction}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class AbstractITipAction implements AJAXActionService{
    
    protected static final Log LOG = LogFactory.getLog(AbstractITipAction.class);

    protected ServiceLookup services;

    public AbstractITipAction(ServiceLookup services) {
        super();
        this.services = services;
    }

    
    @Override
    public AJAXRequestResult perform(AJAXRequestData request, ServerSession session) throws OXException {
        List<ConversionError> errors = new ArrayList<ConversionError>();
        List<ConversionWarning> warnings = new ArrayList<ConversionWarning>();

        ITipParser itipParser = services.getService(ITipParser.class);
        ITipAnalyzerService analyzer = services.getService(ITipAnalyzerService.class);
        
        String optTimezone = request.getParameter("timezone");
		TimeZone tz = TimeZone.getTimeZone(optTimezone != null ? optTimezone : session.getUser().getTimeZone());
        Map<String, String> mailHeader = new HashMap<String, String>();
        List<ITipMessage> messages = itipParser.parseMessage(getInputStreamAndFillMailHeader(request, session, mailHeader), tz, session.getContext(), errors, warnings);

        List<ITipAnalysis> analysis = analyzer.analyze(messages, request.getParameter("descriptionFormat"), session, mailHeader);
        Object result;
        try {
			result = process(analysis, request, session, tz);
		} catch (JSONException e) {
			throw AjaxExceptionCodes.JSON_ERROR.create(e);
		}
        
        return new AJAXRequestResult(result, new Date());
    }


    protected abstract Object process(List<ITipAnalysis> analysis, AJAXRequestData request, ServerSession session, TimeZone tz) throws JSONException, OXException;


    private InputStream getInputStreamAndFillMailHeader(AJAXRequestData request, ServerSession session, Map<String, String> mailHeader) throws OXException {
        String ds = request.getParameter("dataSource");
        if (ds != null) {
            DataArguments dataArguments = new DataArguments();

            Object data = request.getData();
            if (data != null) {
                JSONObject body = (JSONObject) data;
                for (String string : body.keySet()) {
                    dataArguments.put(string, body.opt(string).toString());
                }
            } else {
                for (Map.Entry<String, String> entry : request.getParameters().entrySet()) {
                    dataArguments.put(entry.getKey(), entry.getValue());
                }
            }

            ConversionService conversionEngine = services.getService(ConversionService.class);

            DataSource dataSource = conversionEngine.getDataSource(ds);

            Data<InputStream> dsData = dataSource.getData(InputStream.class, dataArguments, session);
            fillMailHeader(dsData, mailHeader);
            return dsData.getData();
        }
        Object data = request.getData();
        if (data != null) {
            JSONObject body = (JSONObject) data;
            try {
                return new ByteArrayInputStream(body.getString("ical").getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                LOG.error(e.getMessage(), e);
                return null;
            } catch (JSONException x) {
    			throw AjaxExceptionCodes.JSON_ERROR.create(x);
            }
        }
        return null;
    }

    private void fillMailHeader(Data<InputStream> dsData, Map<String, String> mailHeader) {
        Map<String, String> properties = dsData.getDataProperties().toMap();
        
        for (String key : properties.keySet()) {
            if (key.startsWith(DataProperties.PROPERTY_EMAIL_HEADER_PREFIX)) {
                mailHeader.put(key.substring(DataProperties.PROPERTY_EMAIL_HEADER_PREFIX.length() + 1, key.length()), properties.get(key));
            }
        }
    }
}
