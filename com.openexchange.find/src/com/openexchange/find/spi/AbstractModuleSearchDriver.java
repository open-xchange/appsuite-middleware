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

package com.openexchange.find.spi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.exception.OXException;
import com.openexchange.find.AutocompleteRequest;
import com.openexchange.find.AutocompleteResult;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.FacetType;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.tools.session.ServerSession;

/**
 * Provides some common logic for {@link ModuleSearchDriver} implementations.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public abstract class AbstractModuleSearchDriver implements ModuleSearchDriver {

    /**
     * Removes a list of facets from another one. Indeed only the facet values
     * of the second list are removed from the first one. If the facet is empty
     * afterwards, it's completely removed from the list. The modified list
     * is returned. The original one stays unmodified.
     * @param facets The list to remove facets from.
     * @param toRemove The facets (values) to remove.
     * @return The modified facet list.
     */
    protected List<Facet> filterFacets(List<Facet> facets, List<Facet> toRemove) {
        if (facets.isEmpty() || toRemove.isEmpty()) {
            return facets;
        }

        Map<FacetType, Map<String, FacetValue>> typeMap = new HashMap<FacetType, Map<String, FacetValue>>(facets.size());
        for (Facet facet : facets) {
            Map<String, FacetValue> valueMap = new HashMap<String, FacetValue>(facet.getValues().size());
            typeMap.put(facet.getType(), valueMap);
            for (FacetValue value : facet.getValues()) {
                valueMap.put(value.getId(), value);
            }
        }

        for (Facet removable : toRemove) {
            Map<String, FacetValue> valueMap = typeMap.get(removable.getType());
            if (valueMap != null) {
                for (FacetValue value : removable.getValues()) {
                    valueMap.remove(value.getId());
                }

                if (valueMap.isEmpty()) {
                    typeMap.remove(removable.getType());
                }
            }
        }

        List<Facet> result;
        if (typeMap.isEmpty()) {
            result = Collections.emptyList();
        } else {
            result = new ArrayList<Facet>(typeMap.size());
            for (Entry<FacetType, Map<String, FacetValue>> entry : typeMap.entrySet()) {
                result.add(new Facet(entry.getKey(), new ArrayList<FacetValue>(entry.getValue().values())));
            }
        }

        return result;
    }

    protected String prepareFacetValueId(String prefix, int contextId, String objectId) {
        return prefix + '/' + Integer.toString(contextId) + '/' + objectId;
    }

    @Override
    public final AutocompleteResult autocomplete(AutocompleteRequest autocompleteRequest, ServerSession session) throws OXException {
        AutocompleteResult autocompleteResult = doAutocomplete(autocompleteRequest, session);
        List<Facet> filteredFacets = filterFacets(autocompleteResult.getFacets(), autocompleteRequest.getActiveFactes());
        autocompleteResult.setFacets(filteredFacets);
        return autocompleteResult;
    }

    /**
     * @see ModuleSearchDriver#autocomplete(ServerSession, AutocompleteRequest)
     */
    protected abstract AutocompleteResult doAutocomplete(AutocompleteRequest autocompleteRequest, ServerSession session) throws OXException;
}
