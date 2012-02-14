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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.indexedSearch.json.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.exception.OXException;
import com.openexchange.indexedSearch.json.FieldResults;
import com.openexchange.indexedSearch.json.IndexAJAXRequest;
import com.openexchange.indexedSearch.json.ResultConverters;
import com.openexchange.indexedSearch.json.SearchHandler;
import com.openexchange.indexedSearch.json.mail.MailSearchHandler;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link SearchAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SearchAction extends AbstractIndexAction {

    private static final int[] RANGE_DEFAULT = {0, 10};

    private final Map<String, SearchHandler> handlers;

    /**
     * Initializes a new {@link SearchAction}.
     * @param services
     */
    public SearchAction(final ServiceLookup services, final ResultConverters registry) {
        super(services, registry);
        handlers = new HashMap<String, SearchHandler>(2);
        handlers.put("mail", new MailSearchHandler(services));
    }

    @Override
    protected AJAXRequestResult perform(final IndexAJAXRequest req) throws OXException, JSONException {
        // Get parameters
        final int[] columns = req.checkIntArray("columns");
        final String[] modules = req.checkStringArray("modules");
        // Parse range
        final int[] range;
        {
            String sRange = req.getParameter("range");
            if (null == sRange) {
                range = RANGE_DEFAULT;
            } else {
                int[] tmp;
                try {
                    sRange = sRange.trim();
                    final int pos = sRange.indexOf(',');
                    tmp = new int[] { Integer.parseInt(sRange.substring(0, pos)), Integer.parseInt(sRange.substring(pos+1))};
                } catch (final NumberFormatException e) {
                    tmp = RANGE_DEFAULT;
                }
                range = tmp;
            }
        }
        // Get JSON body
        final JSONObject jBody = req.getData();
        // Check modules
        final AJAXRequestData requestData = req.getRequest();
        final ServerSession session = req.getSession();
        final JSONObject jsonResult = new JSONObject();
        requestData.setAction(AJAXServlet.ACTION_ALL);
        requestData.putParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_ALL);
        for (final String module : modules) {
            final SearchHandler handler = handlers.get(module);
            if (null == handler) {
                throw AjaxExceptionCodes.UNKNOWN_MODULE.create(module);
            }
            final List<FieldResults> search = handler.search(jBody, range, columns, requestData, session);
            for (final FieldResults fieldResults : search) {
                final String format = fieldResults.getFormat();
                final ResultConverter converter = registry.getFor(format);
                if (null == converter) {
                    throw AjaxExceptionCodes.UNKNOWN_MODULE.create(module);
                }
                final AJAXRequestResult result = new AJAXRequestResult();
                result.setResultObject(fieldResults.getResults(), format);
                converter.convert(requestData, result, session, null);
                jsonResult.put(fieldResults.getFieldName(), new JSONObject().put("results", result.getResultObject()).put("more", fieldResults.hasMore()));
            }
        }
        // Return
        return new AJAXRequestResult(jsonResult, "json");
    }

    @Override
    public String getAction() {
        return "search";
    }

}
