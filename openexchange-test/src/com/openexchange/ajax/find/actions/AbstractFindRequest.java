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

package com.openexchange.ajax.find.actions;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.Header;
import com.openexchange.find.facet.ActiveFacet;
import com.openexchange.find.facet.Filter;

/**
 * {@link AbstractFindRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractFindRequest<T extends AbstractAJAXResponse> implements AJAXRequest<T> {

    /**
     * URL of the find AJAX interface.
     */
    public static final String FIND_URL = "/ajax/find";

    private final Map<String, String> options;

    /**
     * Initializes a new {@link AbstractFindRequest}.
     */
    protected AbstractFindRequest(final Map<String, String> options) {
        super();
        this.options = options;
    }

    @Override
    public String getServletPath() {
        return FIND_URL;
    }

    @Override
    public Header[] getHeaders() {
        return NO_HEADER;
    }

    protected void addOptions(JSONObject jBody) throws JSONException {
        if (options != null) {
            JSONObject jOptions = new JSONObject();
            for (Entry<String, String> entry : options.entrySet()) {
                jOptions.put(entry.getKey(), entry.getValue());
            }
            jBody.put("options", jOptions);
        }
    }

    protected void addFacets(JSONObject jBody, List<ActiveFacet> activeFacets) throws JSONException {
        if (activeFacets != null) {
            JSONArray jFacets = new JSONArray();
            for (ActiveFacet facet : activeFacets) {
                JSONObject jFacet = new JSONObject();
                jFacet.put("facet", facet.getType().getId());
                jFacet.put("value", facet.getValueId());

                // filter
                Filter filter = facet.getFilter();
                if (filter != null && filter != Filter.NO_FILTER) {
                    final JSONObject jFilter = new JSONObject(3);
                    final List<String> filterQueries = filter.getQueries();
                    final JSONArray jQueries = new JSONArray(filterQueries.size());
                    for (final String sQuery : filterQueries) {
                        jQueries.put(sQuery);
                    }
                    jFilter.put("queries", jQueries);

                    final List<String> fields = filter.getFields();
                    final JSONArray jFields = new JSONArray(fields.size());
                    for (final String sField : fields) {
                        jFields.put(sField);
                    }
                    jFilter.put("fields", jFields);
                    jFacet.put("filter", jFilter);
                }

                jFacets.put(jFacet);
            }
            jBody.put("facets", jFacets);
        }
    }

}
