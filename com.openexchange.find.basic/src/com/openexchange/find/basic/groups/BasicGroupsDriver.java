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

package com.openexchange.find.basic.groups;

import static com.openexchange.find.facet.Facets.newDefaultBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.exception.OXException;
import com.openexchange.find.AutocompleteRequest;
import com.openexchange.find.AutocompleteResult;
import com.openexchange.find.Document;
import com.openexchange.find.Module;
import com.openexchange.find.SearchRequest;
import com.openexchange.find.SearchResult;
import com.openexchange.find.basic.Services;
import com.openexchange.find.common.FolderType;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.FacetType;
import com.openexchange.find.facet.FacetTypeLookUp;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.facet.Facets.DefaultFacetBuilder;
import com.openexchange.find.facet.Filter;
import com.openexchange.find.facet.SimpleDisplayItem;
import com.openexchange.find.group.GroupDocument;
import com.openexchange.find.spi.AbstractModuleSearchDriver;
import com.openexchange.group.Group;
import com.openexchange.group.UseCountAwareGroupService;
import com.openexchange.java.Strings;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link BasicGroupsDriver}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.2
 */
public class BasicGroupsDriver extends AbstractModuleSearchDriver implements FacetTypeLookUp {

    @Override
    public Module getModule() {
        return Module.GROUPS;
    }

    @Override
    public boolean isValidFor(ServerSession session) {
        return session.getUserPermissionBits().hasGroupware();
    }

    @Override
    protected AutocompleteResult doAutocomplete(AutocompleteRequest autocompleteRequest, ServerSession session) throws OXException {
        List<Facet> facets = new ArrayList<>();
        String prefix = autocompleteRequest.getPrefix();
        int minimumSearchCharacters = ServerConfig.getInt(ServerConfig.Property.MINIMUM_SEARCH_CHARACTERS);
        if (Strings.isNotEmpty(prefix) && prefix.length() >= minimumSearchCharacters) {
            UseCountAwareGroupService service = Services.getGroupService();
            // Search by name
            Group[] searchedGroups = service.searchGroups(session.getContext(), prefix, true, session.getUserId());
            List<Group> groups = Arrays.asList(searchedGroups);
            if (null != groups && !groups.isEmpty()) {
                DefaultFacetBuilder builder = newDefaultBuilder(GroupType.GROUP);
                for (Group group : groups) {
                    String id = GroupType.GROUP.getId();
                    Filter filter = Filter.of(id, String.valueOf(group.getIdentifier()));
                    String valueId = prepareFacetValueId(id, session.getContextId(), Integer.toString(group.getIdentifier()));
                    builder.addValue(FacetValue.newBuilder(valueId).withDisplayItem(new SimpleDisplayItem(group.getDisplayName())).withFilter(filter).build());
                }
                facets.add(builder.build());
            }
        }

        return new AutocompleteResult(facets);
    }

    @Override
    protected SearchResult doSearch(SearchRequest searchRequest, ServerSession session) throws OXException {
        List<Filter> filters = searchRequest.getFilters();
        if (filters.isEmpty() || filters.size() > 1) {
            return SearchResult.EMPTY;
        }

        Filter filter = filters.get(0);
        UseCountAwareGroupService service = Services.getGroupService();

        if (filter.getFields() == null || filter.getFields().size() != 1) {
            return SearchResult.EMPTY;
        }
        if (filter.getFields().get(0).equals(GroupType.GROUP.getId())) {
            Group[] searchGroups = service.searchGroups(session.getContext(), filter.getQueries().get(0), true, session.getUserId());
            List<Document> result = new ArrayList<>();
            for (Group group : searchGroups) {
                result.add(new GroupDocument(group));
            }
            return new SearchResult(result.size(), 0, result, searchRequest.getActiveFacets());
        }
        return SearchResult.EMPTY;
    }

    @Override
    protected Set<FolderType> getSupportedFolderTypes(AutocompleteRequest autocompleteRequest, ServerSession session) {
        return null;
    }

    @Override
    public FacetType facetTypeFor(String id) {
        return GroupType.getById(id);
    }

    private enum GroupType implements FacetType {
        GROUP("Group");
        private String name;
        
        /**
         * Initializes a new {@link BasicGroupsDriver.ResourceTypes}.
         */
        private GroupType(String name) {
            this.name = name;
        }
        
        @Override
        public String getId() {
            return name.toLowerCase();
        }

        @Override
        public String getDisplayName() {
            return name;
        }

        static GroupType getById(String id) {
            for (GroupType type : GroupType.values()) {
                if (type.getId().equals(id)) {
                    return type;
                }
            }
            return null;
        }

        @Override
        public List<FacetType> getConflictingFacets() {
            return Collections.emptyList();
        }
    }
}
