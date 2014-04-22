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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.find.AutocompleteRequest;
import com.openexchange.find.AutocompleteResult;
import com.openexchange.find.common.CommonFacetType;
import com.openexchange.find.common.CommonStrings;
import com.openexchange.find.common.FolderTypeDisplayItem;
import com.openexchange.find.common.FormattableDisplayItem;
import com.openexchange.find.facet.ActiveFacet;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.FacetType;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.facet.FieldFacet;
import com.openexchange.find.facet.Filter;
import com.openexchange.tools.session.ServerSession;

/**
 * Provides some common logic for {@link ModuleSearchDriver} implementations.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public abstract class AbstractModuleSearchDriver implements ModuleSearchDriver {

    @Override
    public final AutocompleteResult autocomplete(AutocompleteRequest autocompleteRequest, ServerSession session) throws OXException {
        AutocompleteResult autocompleteResult = doAutocomplete(autocompleteRequest, session);
        List<Facet> modifiedFacets = new LinkedList<Facet>();
        if (!autocompleteRequest.getPrefix().isEmpty()) {
            Facet globalFacet = new FieldFacet(
                CommonFacetType.GLOBAL,
                new FormattableDisplayItem(getFormatStringForGlobalFacet(), autocompleteRequest.getPrefix()),
                new Filter(Collections.singletonList(CommonFacetType.GLOBAL.getId()),
                    Collections.singletonList(autocompleteRequest.getPrefix())));
            modifiedFacets.add(globalFacet);
        }

        filterFacets(autocompleteResult.getFacets(), autocompleteRequest.getActiveFacets(), modifiedFacets);
        autocompleteResult.setFacets(modifiedFacets);
        return autocompleteResult;
    }

    @Override
    public SearchConfiguration getSearchConfiguration(ServerSession session) throws OXException {
        return new SearchConfiguration();
    }

    /**
     * The format string to construct the display item for the global facet.
     * Something like "%1$s <i>in file name</i>". Must contain exactly one
     * string reference that will be replaced with the current prefix.
     */
    protected abstract String getFormatStringForGlobalFacet();

    /**
     * @see ModuleSearchDriver#autocomplete(ServerSession, AutocompleteRequest)
     */
    protected abstract AutocompleteResult doAutocomplete(AutocompleteRequest autocompleteRequest, ServerSession session) throws OXException;

    /**
     * Builds the facet for folder types (private, public , shared, external) that is indicated on {@link ModuleSearchDriver#getConfiguration(com.openexchange.tools.session.ServerSession) getConfiguration(ServerSession)} invocation.
     *
     * @return The facet for folder types
     */
    protected Facet buildFolderTypeFacet() {
        final List<FacetValue> folderTypes = new ArrayList<FacetValue>(4);
        final String sField = "folder_type";
        folderTypes.add(new FacetValue(FolderTypeDisplayItem.Type.PRIVATE.getIdentifier(), new FolderTypeDisplayItem(CommonStrings.FOLDER_TYPE_PRIVATE, FolderTypeDisplayItem.Type.PRIVATE), FacetValue.UNKNOWN_COUNT, new Filter(Collections.singletonList(sField), FolderTypeDisplayItem.Type.PRIVATE.getIdentifier())));
        folderTypes.add(new FacetValue(FolderTypeDisplayItem.Type.PUBLIC.getIdentifier(), new FolderTypeDisplayItem(CommonStrings.FOLDER_TYPE_PUBLIC, FolderTypeDisplayItem.Type.PUBLIC), FacetValue.UNKNOWN_COUNT, new Filter(Collections.singletonList(sField), FolderTypeDisplayItem.Type.PUBLIC.getIdentifier())));
        folderTypes.add(new FacetValue(FolderTypeDisplayItem.Type.SHARED.getIdentifier(), new FolderTypeDisplayItem(CommonStrings.FOLDER_TYPE_SHARED, FolderTypeDisplayItem.Type.SHARED), FacetValue.UNKNOWN_COUNT, new Filter(Collections.singletonList(sField), FolderTypeDisplayItem.Type.SHARED.getIdentifier())));
        folderTypes.add(new FacetValue(FolderTypeDisplayItem.Type.EXTERNAL.getIdentifier(), new FolderTypeDisplayItem(CommonStrings.FOLDER_TYPE_EXTERNAL, FolderTypeDisplayItem.Type.EXTERNAL), FacetValue.UNKNOWN_COUNT, new Filter(Collections.singletonList(sField), FolderTypeDisplayItem.Type.EXTERNAL.getIdentifier())));
        final Facet folderTypeFacet = new Facet(CommonFacetType.FOLDER_TYPE, folderTypes);
        return folderTypeFacet;
    }

    /**
     * Removes the currently active facets (respectively their values) from the ones returned from an autocomplete request.
     * Empty facets will be removed completely from the result. The original list stays unmodified.
     * All remaining facets are added to the result list.
     * @param facets The list to remove facets from.
     * @param active The facets (values) to remove.
     * @param results The list to append the filtered facets to.
     * @return The modified facet list.
     */
    protected void filterFacets(List<Facet> facets, List<ActiveFacet> active, List<Facet> results) {
        if (facets.isEmpty()) {
            return;
        }

        if (active.isEmpty()) {
            for (Facet facet : facets) {
                results.add(facet);
            }
            return;
        }

        Map<FacetType, Map<String, FacetValue>> typeMap = new HashMap<FacetType, Map<String, FacetValue>>(facets.size());
        for (Facet facet : facets) {
            Map<String, FacetValue> valueMap = new HashMap<String, FacetValue>(facet.getValues().size());
            typeMap.put(facet.getType(), valueMap);
            for (FacetValue value : facet.getValues()) {
                valueMap.put(value.getId(), value);
            }
        }

        for (ActiveFacet toRemove : active) {
            FacetType type = toRemove.getType();
            if (type == CommonFacetType.FOLDER) {
                typeMap.remove(CommonFacetType.FOLDER);
                typeMap.remove(CommonFacetType.FOLDER_TYPE);
                continue;
            } else if (type == CommonFacetType.FOLDER_TYPE) {
                typeMap.remove(CommonFacetType.FOLDER);
                typeMap.remove(CommonFacetType.FOLDER_TYPE);
                continue;
            } else if (!type.isFieldFacet()) {
                if (type.appliesOnce()) {
                    typeMap.remove(type);
                    continue;
                }

                Map<String, FacetValue> valueMap = typeMap.get(type);
                if (valueMap != null) {
                    valueMap.remove(toRemove.getValueId());
                    if (valueMap.isEmpty()) {
                        typeMap.remove(toRemove.getType());
                    }
                }
            }
        }

        for (Facet facet : facets) {
            Map<String, FacetValue> map = typeMap.get(facet.getType());
            if (map != null) {
                List<FacetValue> values = new LinkedList<FacetValue>();
                for (FacetValue value : facet.getValues()) {
                    if (map.containsKey(value.getId())) {
                        values.add(value);
                    }
                }

                results.add(new Facet(facet.getType(), values));
            }
        }
    }

    protected static String prepareFacetValueId(String prefix, int contextId, String objectId) {
        return prefix + '/' + Integer.toString(contextId) + '/' + objectId;
    }
}
