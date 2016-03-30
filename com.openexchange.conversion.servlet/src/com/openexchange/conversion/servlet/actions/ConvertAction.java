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

package com.openexchange.conversion.servlet.actions;

import java.util.Date;
import java.util.Map.Entry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.servlet.ConversionAJAXRequest;
import com.openexchange.conversion.servlet.ConversionServletExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link ConvertAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ConvertAction extends AbstractConversionAction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ConvertAction.class);

    private static final String JSON_ARGS = "args";

    private static final String JSON_IDENTIFIER = "identifier";

    private static final String JSON_DATAHANDLER = "datahandler";

    private static final String JSON_DATASOURCE = "datasource";

    /**
     * Initializes a new {@link ConvertAction}.
     * @param services
     */
    public ConvertAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final ConversionAJAXRequest req) throws OXException, JSONException {
        /*
         * Check for data source in JSON body
         */
        final JSONObject jsonBody = req.getData();
        if (!jsonBody.has(JSON_DATASOURCE) || jsonBody.isNull(JSON_DATASOURCE)) {
            throw ConversionServletExceptionCode.MISSING_PARAM.create(JSON_DATASOURCE);
        }
        final JSONObject jsonDataSource = jsonBody.getJSONObject(JSON_DATASOURCE);
        checkDataSourceOrHandler(jsonDataSource);
        /*
         * Check for data handler in JSON body
         */
        if (!jsonBody.has(JSON_DATAHANDLER) || jsonBody.isNull(JSON_DATAHANDLER)) {
            throw ConversionServletExceptionCode.MISSING_PARAM.create(JSON_DATAHANDLER);
        }
        final JSONObject jsonDataHandler = jsonBody.getJSONObject(JSON_DATAHANDLER);
        checkDataSourceOrHandler(jsonDataHandler);
        /*
         * Convert with conversion service
         */
        final ServerSession session = req.getSession();
        final ConversionService conversionService = getService(ConversionService.class);
        final Object result = conversionService.convert(
            jsonDataSource.getString(JSON_IDENTIFIER),
            parseDataSourceOrHandlerArguments(jsonDataSource),
            jsonDataHandler.getString(JSON_IDENTIFIER),
            parseDataSourceOrHandlerArguments(jsonDataHandler),
            session);
        /*
         * Compose response
         */
        if (result instanceof JSONValue) {
            return new AJAXRequestResult(result, "json");
        }
        if (result instanceof Number) {
            return new AJAXRequestResult(result, "int");
        }
        if (result instanceof Date) {
            return new AJAXRequestResult(result, "date");
        }
        return new AJAXRequestResult(result.toString(), "string");
    }

    private static void checkDataSourceOrHandler(final JSONObject json) throws OXException {
        if (!json.has(JSON_IDENTIFIER) || json.isNull(JSON_IDENTIFIER)) {
            throw ConversionServletExceptionCode.MISSING_PARAM.create(JSON_IDENTIFIER);
        }
    }

    private static DataArguments parseDataSourceOrHandlerArguments(final JSONObject json) throws JSONException {
        if (!json.has(JSON_ARGS) || json.isNull(JSON_ARGS)) {
            return DataArguments.EMPTY_ARGS;
        }
        final Object args = json.get(JSON_ARGS);
        if (args instanceof JSONArray) {
            final JSONArray jsonArray = (JSONArray) args;
            final int len = jsonArray.length();
            final DataArguments dataArguments = new DataArguments(len);
            for (int i = 0; i < len; i++) {
                final JSONObject elem = jsonArray.getJSONObject(i);
                if (elem.length() == 1) {
                    final String key = elem.keys().next();
                    dataArguments.put(key, elem.getString(key));
                } else {
                    LOG.warn("Corrupt data argument in JSON object: {}", elem);
                }
            }
            return dataArguments;
        }
        /*
         * Expect JSON object
         */
        final JSONObject argsObject = (JSONObject) args;
        final int len = argsObject.length();
        final DataArguments dataArguments = new DataArguments(len);
        for (final Entry<String, Object> entry : argsObject.entrySet()) {
            dataArguments.put(entry.getKey(), entry.getValue().toString());
        }
        return dataArguments;
    }

}
