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

package com.openexchange.find.spi;

import static com.openexchange.find.facet.Facets.newExclusiveBuilder;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.find.AbstractFindRequest;
import com.openexchange.find.AutocompleteRequest;
import com.openexchange.find.AutocompleteResult;
import com.openexchange.find.FindExceptionCode;
import com.openexchange.find.SearchRequest;
import com.openexchange.find.SearchResult;
import com.openexchange.find.common.CommonFacetType;
import com.openexchange.find.common.CommonStrings;
import com.openexchange.find.common.FolderType;
import com.openexchange.find.facet.ActiveFacet;
import com.openexchange.find.facet.DefaultFacet;
import com.openexchange.find.facet.ExclusiveFacet;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.FacetType;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.facet.Facets;
import com.openexchange.find.facet.Facets.ExclusiveFacetBuilder;
import com.openexchange.find.facet.Filter;
import com.openexchange.find.facet.SimpleFacet;
import com.openexchange.tools.session.ServerSession;

/**
 * Provides some common logic for {@link ModuleSearchDriver} implementations.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public abstract class AbstractModuleSearchDriver implements ModuleSearchDriver {

    /**
     * A speaking constant that should be returned by {@link AbstractModuleSearchDriver#getSupportedFolderTypes()},
     * if the folder type facet is not supported.
     */
    protected static final Set<FolderType> FOLDER_TYPE_NOT_SUPPORTED = Collections.emptySet();

    /**
     * A set containing all folder types.
     */
    protected static final Set<FolderType> ALL_FOLDER_TYPES = EnumSet.allOf(FolderType.class);

    @Override
    public boolean isValidFor(ServerSession session, AbstractFindRequest findRequest) throws OXException {
        return isValidFor(session);
    }

    @Override
    public final AutocompleteResult autocomplete(AutocompleteRequest autocompleteRequest, ServerSession session) throws OXException {
        checkActiveFacets(autocompleteRequest);
        AutocompleteResult autocompleteResult = doAutocomplete(autocompleteRequest, session);
        List<Facet> modifiedFacets = new LinkedList<Facet>(autocompleteResult.getFacets());
        Facet folderTypeFacet = getFolderTypeFacet(getSupportedFolderTypes(autocompleteRequest, session));
        if (folderTypeFacet != null) {
            modifiedFacets.add(folderTypeFacet);
        }

        LinkedList<Facet> filteredFacets = filterFacets(modifiedFacets, autocompleteRequest.getActiveFacets());
        autocompleteResult.setFacets(filteredFacets);
        return autocompleteResult;
    }

    @Override
    public SearchResult search(SearchRequest searchRequest, ServerSession session) throws OXException {
        checkActiveFacets(searchRequest);
        return doSearch(searchRequest, session);
    }

    @Override
    public SearchConfiguration getSearchConfiguration(ServerSession session) throws OXException {
        return new SearchConfiguration();
    }

    private void checkActiveFacets(AbstractFindRequest req) throws OXException {
        List<ActiveFacet> facets = req.getActiveFacets();
        for (ActiveFacet facet : facets) {
            for (FacetType conflictingType : facet.getType().getConflictingFacets()) {
                List<ActiveFacet> conflicts = req.getActiveFacets(conflictingType);
                if (conflicts != null && !conflicts.isEmpty()) {
                    throw FindExceptionCode.FACET_CONFLICT.create(facet.getType().getId(), conflicts.get(0).getType().getId());
                }
            }
        }
    }

    /**
     * @see ModuleSearchDriver#autocomplete(ServerSession, AutocompleteRequest)
     */
    protected abstract AutocompleteResult doAutocomplete(AutocompleteRequest autocompleteRequest, ServerSession session) throws OXException;

    /**
     * @see ModuleSearchDriver#search(SearchRequest, ServerSession)
     */
    protected abstract SearchResult doSearch(SearchRequest searchRequest, ServerSession session) throws OXException;

    /**
     * Specifies if the {@link CommonFacetType#FOLDER_TYPE} facet is supported.
     *
     * @param autocompleteRequest The auto-complete request
     * @param session The session
     * @return A set of folder types.
     * Returns <code>null</code> or an empty set if the facet is not supported at all.
     * @see {@link AbstractModuleSearchDriver#ALL_FOLDER_TYPES} and {@link AbstractModuleSearchDriver#FOLDER_TYPE_NOT_SUPPORTED}
     * for convenience.
     */
    protected abstract Set<FolderType> getSupportedFolderTypes(AutocompleteRequest autocompleteRequest, ServerSession session) throws OXException;

    protected LinkedList<Facet> filterFacets(List<Facet> facets, List<ActiveFacet> active) {
        if (facets.isEmpty() || active.isEmpty()) {
            return new LinkedList<Facet>(facets);
        }

        Map<FacetType, List<ActiveFacet>> type2active = new HashMap<FacetType, List<ActiveFacet>>(active.size());
        for (ActiveFacet activeFacet : active) {
            FacetType type = activeFacet.getType();
            List<ActiveFacet> list = type2active.get(type);
            if (list == null) {
                list = new LinkedList<ActiveFacet>();
                type2active.put(type, list);
            }

            list.add(activeFacet);
        }

        LinkedList<Facet> filtered = new LinkedList<Facet>();
        outer: for (Facet facet : facets) {
            List<FacetType> conflictingFacets = facet.getType().getConflictingFacets();
            for (FacetType conflicting : conflictingFacets) {
                if (type2active.containsKey(conflicting)) {
                    continue outer;
                }
            }

            if (facet instanceof SimpleFacet) {
                filtered.add(facet);
            } else if (facet instanceof ExclusiveFacet) {
                if (!type2active.containsKey(facet.getType())) {
                    filtered.add(facet);
                }
            } else if (facet instanceof DefaultFacet) {
                DefaultFacet defaultFacet = (DefaultFacet) facet;
                List<ActiveFacet> activeFacets = type2active.get(facet.getType());
                if (activeFacets == null) {
                    filtered.add(facet);
                } else {
                    List<FacetValue> filteredValues = new LinkedList<FacetValue>();
                    Set<String> valuesToRemove = new HashSet<String>(activeFacets.size());
                    for (ActiveFacet activeFacet : activeFacets) {
                        valuesToRemove.add(activeFacet.getValueId());
                    }

                    for (FacetValue value : defaultFacet.getValues()) {
                        if (!valuesToRemove.contains(value.getId())) {
                            filteredValues.add(value);
                        }
                    }

                    if (!filteredValues.isEmpty()) {
                        filtered.add(Facets.newDefaultBuilder(defaultFacet.getType())
                            .withValues(filteredValues)
                            .build());
                    }
                }
            }
        }

        return filtered;
    }

    protected static String prepareFacetValueId(String prefix, int contextId, String objectId) {
        return prefix + '/' + Integer.toString(contextId) + '/' + objectId;
    }

    private static Facet getFolderTypeFacet(Set<FolderType> supportedTypes) {
        if (supportedTypes == null || supportedTypes.isEmpty()) {
            return null;
        }

        ExclusiveFacetBuilder builder = newExclusiveBuilder(CommonFacetType.FOLDER_TYPE);
        if (supportedTypes.contains(FolderType.PRIVATE)) {
            builder.addValue(FacetValue.newBuilder(FolderType.PRIVATE.getIdentifier())
                .withLocalizableDisplayItem(CommonStrings.FOLDER_TYPE_PRIVATE)
                .withFilter(Filter.of(CommonFacetType.FOLDER_TYPE.getId(), FolderType.PRIVATE.getIdentifier()))
                .build());
        }

        if (supportedTypes.contains(FolderType.PUBLIC)) {
            builder.addValue(FacetValue.newBuilder(FolderType.PUBLIC.getIdentifier())
                .withLocalizableDisplayItem(CommonStrings.FOLDER_TYPE_PUBLIC)
                .withFilter(Filter.of(CommonFacetType.FOLDER_TYPE.getId(), FolderType.PUBLIC.getIdentifier()))
                .build());
        }

        if (supportedTypes.contains(FolderType.SHARED)) {
            builder.addValue(FacetValue.newBuilder(FolderType.SHARED.getIdentifier())
                .withLocalizableDisplayItem(CommonStrings.FOLDER_TYPE_SHARED)
                .withFilter(Filter.of(CommonFacetType.FOLDER_TYPE.getId(), FolderType.SHARED.getIdentifier()))
                .build());
        }

        return builder.build();
    }
}
