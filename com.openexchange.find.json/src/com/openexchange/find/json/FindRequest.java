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

package com.openexchange.find.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.find.FindExceptionCode;
import com.openexchange.find.Module;
import com.openexchange.find.calendar.CalendarFacetType;
import com.openexchange.find.common.CommonFacetType;
import com.openexchange.find.contacts.ContactsFacetType;
import com.openexchange.find.drive.DriveFacetType;
import com.openexchange.find.facet.ActiveFacet;
import com.openexchange.find.facet.FacetType;
import com.openexchange.find.facet.Filter;
import com.openexchange.find.mail.MailFacetType;
import com.openexchange.find.tasks.TasksFacetType;
import com.openexchange.java.Strings;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link FindRequest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since 7.6.0
 */
public class FindRequest {

    private static final String PARAM_MODULE = "module";
    private static final String PARAM_PREFIX = "prefix";
    private static final String PARAM_START = "start";
    private static final String PARAM_SIZE = "size";
    private static final String PARAM_FACETS = "facets";
    private static final int DEFAULT_SIZE = 20;

    // -------------------------------------------------------------------------------------------- //


    private final AJAXRequestData request;
    private final ServerSession session;

    /**
     * Initializes a new {@link FindRequest}.
     *
     * @param request
     * @param session
     */
    public FindRequest(final AJAXRequestData request, final ServerSession session) {
        super();
        this.request = request;
        this.session = session;
    }

    /**
     * Gets the associated session.
     *
     * @return The session
     */
    public ServerSession getServerSession() {
        return session;
    }

    /**
     * Gets the offset
     *
     * @return The offset
     */
    public Offset getOffset() throws OXException {
        JSONObject json = (JSONObject) request.requireData();
        int off = 0;
        int len = -1;
        try {
            if (json.has(PARAM_START)) {
                off = json.getInt(PARAM_START);
            }
            if (json.has(PARAM_SIZE)) {
                len = json.getInt(PARAM_SIZE);
            }
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage());
        }

        if (off < 0 || len < 0) {
            return new Offset(0, DEFAULT_SIZE);
        }

        return new Offset(off, len);
    }

    /**
     * Gets the module associated with this request.
     *
     * @return The module or <code>null</code>
     */
    public Module getModule() {
        final String module = request.getParameter(PARAM_MODULE);
        if (module == null) {
            return null;
        }

        return Module.moduleFor(module);
    }

    /**
     * Gets the module associated with this request.
     *
     * @return The module
     * @throws OXException If module cannot be returned
     */
    public Module requireModule() throws OXException {
        final String moduleValue = request.getParameter(PARAM_MODULE);
        if (moduleValue == null) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(PARAM_MODULE);
        }

        final Module module = Module.moduleFor(moduleValue);
        if (module == null) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(PARAM_MODULE, moduleValue);
        }

        return module;
    }

    /**
     * Gets the checked prefix to auto-complete on.
     *
     * @return The checked prefix
     * @throws OXException If prefix is missing or invalid
     */
    public String requirePrefix() throws OXException {
        final JSONObject json = (JSONObject) request.requireData();
        try {
            String prefix = json.getString(PARAM_PREFIX).trim();
            if (Strings.isEmpty((prefix = prefix.trim()))) {
                return "";
            }

            /*-
             *
            final char lastChar = prefix.charAt(prefix.length() - 1);
            if ('*' == lastChar || '?' == lastChar) {
                throw AjaxExceptionCodes.IMVALID_PARAMETER.create(PARAM_PREFIX);
            }
            */

            return prefix;
        } catch (final JSONException e) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(PARAM_PREFIX);
        }
    }

//    /**
//     * Gets the checked filters for search.
//     *
//     * @return The filters
//     * @throws OXException If filters are invalid
//     */
//    public List<Filter> optFilters() throws OXException {
//        final JSONObject json = (JSONObject) request.requireData();
//        try {
//            final JSONArray jFilters = json.optJSONArray(PARAM_FILTERS);
//            if (null == jFilters) {
//                return Collections.emptyList();
//            }
//
//            final int length = jFilters.length();
//            final List<Filter> filters = new ArrayList<Filter>(length);
//            for (int i = 0; i < length; i++) {
//                final JSONObject jFilter = jFilters.getJSONObject(i);
//
//                final JSONArray jQueries = jFilter.getJSONArray("queries");
//                int len = jQueries.length();
//                final List<String> queries = new ArrayList<String>(len);
//                for (int j = 0; j < len; j++) {
//                    queries.add(jQueries.getString(j));
//                }
//
//                final JSONArray jFields = jFilter.getJSONArray("fields");
//                len = jFields.length();
//                final List<String> fields = new ArrayList<String>(len);
//                for (int j = 0; j < len; j++) {
//                    fields.add(jFields.getString(j));
//                }
//
//                filters.add(parseFilter(jValue));
//            }
//
//            return filters;
//        } catch (final JSONException e) {
//            throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage());
//        }
//    }

    public Filter parseFilter(JSONObject jFilter) throws JSONException {
        JSONArray jQueries = jFilter.getJSONArray("queries");
        int len = jQueries.length();
        List<String> queries = new ArrayList<String>(len);
        for (int j = 0; j < len; j++) {
            queries.add(jQueries.getString(j));
        }

        JSONArray jFields = jFilter.getJSONArray("fields");
        len = jFields.length();
        List<String> fields = new ArrayList<String>(len);
        for (int j = 0; j < len; j++) {
            fields.add(jFields.getString(j));
        }

        return new Filter(fields, queries);
    }

    /**
     * Gets the facets.
     *
     * @return The facets.
     * @throws OXException If facets are invalid
     */
    public JSONArray optFacets() throws OXException {
        JSONObject json = (JSONObject) request.requireData();
        Object facetsObj = json.opt(PARAM_FACETS);
        if (facetsObj != null && facetsObj instanceof JSONArray) {
            return (JSONArray) facetsObj;
        }

        return null;
    }

    /**
     * Gets specified parameter.
     *
     * @param name The parameter name
     * @return The parameter value
     * @throws OXException If parameter is absent
     */
    public String requireParameter(final String name) throws OXException {
        return request.requireParameter(name);
    }

    /**
     * Gets specified parameter.
     *
     * @param name The parameter name
     * @return The parameter value or <code>null</code>
     */
    public String getParameter(final String name) {
        return request.getParameter(name);
    }

    /**
     * Gets specified <code>int</code> parameter.
     *
     * @param name The parameter name
     * @return The <code>int</code> value or <code>-1</code> if absent
     * @throws OXException If parameter is <code>NaN</code>
     */
    public int getIntParameter(final String name) throws OXException {
        return request.getIntParameter(name);
    }

    /**
     * Gets specified parameter.
     *
     * @param name The parameter name
     * @param coerceTo The parameter type to coerce to
     * @return The parameter value or <code>null</code> if absent
     * @throws OXException If parameter cannot be coerced to specified type
     */
    public <T> T getParameter(final String name, final Class<T> coerceTo) throws OXException {
        return request.getParameter(name, coerceTo);
    }

    /**
     * Gets the active facets.
     * @return A list of {@link ActiveFacet}s. May be empty but not <code>null</code>.
     */
    public List<ActiveFacet> getActiveFacets() throws OXException {
        JSONArray jFacets = optFacets();
        if (jFacets == null || jFacets.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            Module module = requireModule();
            final int length = jFacets.length();
            List<ActiveFacet> facets = new ArrayList<ActiveFacet>(length);
            for (int i = 0; i < length; i++) {
                JSONObject jFacet = jFacets.getJSONObject(i);
                String jType = jFacet.getString("facet");
                FacetType type = facetTypeFor(module, jType);
                if (type == null) {
                    throw FindExceptionCode.UNSUPPORTED_FACET.create(jType, module.getIdentifier());
                }

                String valueId = jFacet.getString("value");
                JSONObject jFilter = jFacet.optJSONObject("filter");
                Filter filter;
                if (jFilter == null) {
                    filter = Filter.NO_FILTER;
                } else {
                    filter = parseFilter(jFilter);
                }
                facets.add(new ActiveFacet(type, valueId, filter));
            }

            return facets;
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage());
        }
    }

    private static FacetType facetTypeFor(Module module, String id) {
        FacetType type = null;
        switch(module) {
            case MAIL:
                type = MailFacetType.getById(id);
                break;

            case CALENDAR:
                type = CalendarFacetType.getById(id);
                break;

            case CONTACTS:
                type = ContactsFacetType.getById(id);
                break;

            case DRIVE:
                type = DriveFacetType.getById(id);
                break;

            case TASKS:
                type = TasksFacetType.getById(id);
                break;

            default:
                return null;
        }

        if (type == null) {
            type = CommonFacetType.getById(id);
        }

        return type;
    }

}
