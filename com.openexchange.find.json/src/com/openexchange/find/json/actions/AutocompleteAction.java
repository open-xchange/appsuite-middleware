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

package com.openexchange.find.json.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.find.AutocompleteRequest;
import com.openexchange.find.AutocompleteResult;
import com.openexchange.find.FindExceptionCode;
import com.openexchange.find.Module;
import com.openexchange.find.SearchService;
import com.openexchange.find.calendar.CalendarFacetType;
import com.openexchange.find.contacts.ContactsFacetType;
import com.openexchange.find.drive.DriveFacetType;
import com.openexchange.find.facet.DisplayItem;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.FacetType;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.facet.Filter;
import com.openexchange.find.json.FindRequest;
import com.openexchange.find.mail.MailFacetType;
import com.openexchange.find.tasks.TasksFacetType;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;


/**
 * {@link AutocompleteAction}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since 7.6.0
 */
public class AutocompleteAction extends AbstractFindAction {

    /**
     * Initializes a new {@link AutocompleteAction}.
     *
     * @param services The service look-up
     */
    public AutocompleteAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult doPerform(FindRequest request) throws OXException {
        SearchService searchService = getSearchService();
        String prefix = request.requirePrefix();
        String folder = request.getFolder();
        List<Facet> activeFactes = parseActiveFacets(request);
        AutocompleteResult result = searchService.autocomplete(
            new AutocompleteRequest(prefix, folder, activeFactes),
            request.requireModule(),
            request.getServerSession());
        return new AJAXRequestResult(result, AutocompleteResult.class.getName());
    }

    private List<Facet> parseActiveFacets(FindRequest request) throws OXException {
        JSONArray activeFacets = request.optActiveFacets();
        if (activeFacets == null || activeFacets.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            Module module = request.getModule();
            List<Facet> facets = new ArrayList<Facet>(activeFacets.length());
            for (int i = 0; i < activeFacets.length(); i++) {
                JSONObject facetJSON = activeFacets.getJSONObject(i);
                String typeName = facetJSON.getString("type");
                FacetType type = facetTypeFor(module, typeName);
                if (type == null) {
                    throw FindExceptionCode.UNSUPPORTED_FACET.create(typeName, module.getIdentifier());
                }

                JSONArray valuesJSON = facetJSON.getJSONArray("values");
                List<FacetValue> valueList = new ArrayList<FacetValue>(valuesJSON.length());
                for (int j = 0; j < valuesJSON.length(); j++) {
                    JSONObject valueJSON = valuesJSON.getJSONObject(j);
                    String valueId = valueJSON.getString("id");
                    valueList.add(new FacetValue(
                        valueId,
                        DisplayItem.NO_DISPLAY_ITEM,
                        FacetValue.UNKNOWN_COUNT,
                        Filter.NO_FILTER));
                }
                Facet facet = new Facet(type, valueList);
                facets.add(facet);
            }

            return facets;
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage());
        }
    }

    private FacetType facetTypeFor(Module module, String name) {
        switch(module) {
            case MAIL:
                return MailFacetType.getByName(name);

            case CALENDAR:
                return CalendarFacetType.getByName(name);

            case CONTACTS:
                return ContactsFacetType.getByName(name);

            case DRIVE:
                return DriveFacetType.getByName(name);

            case TASKS:
                return TasksFacetType.getByName(name);

            default:
                return null;
        }
    }

}
