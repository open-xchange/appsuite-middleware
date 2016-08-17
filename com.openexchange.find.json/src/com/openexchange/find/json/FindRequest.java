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

package com.openexchange.find.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
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
import com.openexchange.find.facet.FacetTypeLookUp;
import com.openexchange.find.facet.Filter;
import com.openexchange.find.mail.MailFacetType;
import com.openexchange.find.spi.ModuleSearchDriver;
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

    private static final class DefaultFacetTypeLookUp implements FacetTypeLookUp {

        private static final ConcurrentMap<Module, DefaultFacetTypeLookUp> CACHE = new ConcurrentHashMap<Module, DefaultFacetTypeLookUp>(6);

        static DefaultFacetTypeLookUp getInstanceFor(Module module) {
            DefaultFacetTypeLookUp tmp = CACHE.get(module);
            if (null == tmp) {
                DefaultFacetTypeLookUp ntmp = new DefaultFacetTypeLookUp(module);
                tmp = CACHE.putIfAbsent(module, ntmp);
                if (null == tmp) {
                    tmp = ntmp;
                }
            }
            return tmp;
        }

        private final Module module;

        private DefaultFacetTypeLookUp(Module module) {
            super();
            this.module = module;
        }

        @Override
        public FacetType facetTypeFor(String id) {
            return FindRequest.facetTypeFor(module, id);
        }
    }

    private static final class FallbackFacetTypeLookUp implements FacetTypeLookUp {

        private final Module module;
        private final FacetTypeLookUp mainLookUp;

        FallbackFacetTypeLookUp(FacetTypeLookUp mainLookUp, Module module) {
            super();
            this.mainLookUp = mainLookUp;
            this.module = module;
        }

        @Override
        public FacetType facetTypeFor(String id) {
            FacetType facetType = mainLookUp.facetTypeFor(id);
            return null == facetType ? DefaultFacetTypeLookUp.getInstanceFor(module).facetTypeFor(id) : facetType;
        }
    }

    // -----------------------------------------------------------------------------------------------------------------------------------

    private static final String PARAM_MODULE = "module";
    private static final String PARAM_PREFIX = "prefix";
    private static final String PARAM_START = "start";
    private static final String PARAM_SIZE = "size";
    private static final String PARAM_FACETS = "facets";
    private static final String PARAM_OPTIONS = "options";
    private static final int DEFAULT_SIZE = 20;

    // -------------------------------------------------------------------------------------------- //

    private final AJAXRequestData request;
    private final ServerSession session;
    private final SearchServiceProvider searchServiceProvider;

    /**
     * Initializes a new {@link FindRequest}.
     *
     * @param request The AJAX request data
     * @param session The session providing user data
     * @param services The OSGi service look-up
     */
    public FindRequest(final AJAXRequestData request, final ServerSession session, SearchServiceProvider searchServiceProvider) {
        super();
        this.request = request;
        this.session = session;
        this.searchServiceProvider = searchServiceProvider;
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

    /**
     * Gets the offset
     *
     * @return The offset
     * @throws OXException if no request data exists.
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
     * Gets the active facets.
     * @return A list of {@link ActiveFacet}s. May be empty but not <code>null</code>.
     */
    public List<ActiveFacet> getActiveFacets() throws OXException {
        JSONObject json = (JSONObject) request.requireData();
        JSONArray jFacets = json.optJSONArray(PARAM_FACETS);
        if (jFacets == null || jFacets.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            Module module = requireModule();
            FacetTypeLookUp facetTypeLookUp = getFacetTypeLookUp(module);
            final int length = jFacets.length();
            List<ActiveFacet> facets = new ArrayList<ActiveFacet>(length);
            for (int i = 0; i < length; i++) {
                JSONObject jFacet = jFacets.getJSONObject(i);
                String jType = jFacet.getString("facet");
                FacetType type = facetTypeLookUp.facetTypeFor(jType);
                if (type == null) {
                    throw FindExceptionCode.UNSUPPORTED_FACET.create(jType, module.getIdentifier());
                }

                String valueId = getValueId(jFacet.optString("value", null));
                if (valueId == null) {
                    // ignore invalid facets
                    continue;
                }

                Filter filter;
                JSONObject jFilter = jFacet.optJSONObject("filter");
                if (jFilter == null || jFilter == JSONObject.NULL) {
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

    private FacetTypeLookUp getFacetTypeLookUp(Module module) throws OXException {
        ModuleSearchDriver driver = searchServiceProvider.getSearchService().getDriver(module, session);
        return driver instanceof FacetTypeLookUp ? new FallbackFacetTypeLookUp((FacetTypeLookUp) driver, module) : DefaultFacetTypeLookUp.getInstanceFor(module);
    }

    private static String getValueId(Object valueObj) {
        String valueId;
        if (valueObj == null || valueObj == JSONObject.NULL) {
            valueId = null;
        } else {
            valueId = valueObj.toString();
        }

        if (Strings.isEmpty(valueId)) {
            return null;
        }

        return valueId;
    }

    /**
     * Gets the optional options map that may contain module specific properties.
     *
     * @return The map; never <code>null</code>
     * @throws OXException if no request data exists.
     */
    public Map<String, String> getOptions() throws OXException {
        Map<String, String> options = new HashMap<String, String>();
        JSONObject json = (JSONObject) request.requireData();
        JSONObject jOptions = json.optJSONObject(PARAM_OPTIONS);
        if (jOptions == null) {
            return Collections.emptyMap();
        }

        for (String key : jOptions.keySet()) {
            try {
                options.put(key, jOptions.getString(key));
            } catch (JSONException e) {
                throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage(), e);
            }
        }

        return options;
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
     * Split pattern for CSV.
     */
    private static final Pattern SPLIT = Pattern.compile(" *, *");

    /**
     * Gets the requested columns that shall be filled in the response items.
     *
     * @return An array of columns or <code>null</code>.
     */
    public String[] getColumns() throws OXException {
        String valueStr = request.getParameter(AJAXServlet.PARAMETER_COLUMNS);
        if (null == valueStr) {
            return null;
        }

        return SPLIT.split(valueStr);
    }

    private static Filter parseFilter(JSONObject jFilter) throws JSONException {
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
     * Gets the facet type for specified identifier by look-up standard modules' facet types.
     *
     * @param module The module
     * @param id The identifier
     * @return The facet type or <code>null</code>
     */
    static FacetType facetTypeFor(Module module, String id) {
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
