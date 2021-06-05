/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.find.contacts;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.find.AbstractFindTest;
import com.openexchange.ajax.find.PropDocument;
import com.openexchange.ajax.find.actions.AutocompleteRequest;
import com.openexchange.ajax.find.actions.AutocompleteResponse;
import com.openexchange.ajax.find.actions.QueryRequest;
import com.openexchange.ajax.find.actions.QueryResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.user.actions.GetRequest;
import com.openexchange.find.Document;
import com.openexchange.find.Module;
import com.openexchange.find.SearchResult;
import com.openexchange.find.facet.ActiveFacet;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.util.DisplayItems;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.util.TimeZones;

/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class ExcludeContextAdminTest extends AbstractFindTest {

    /*
     * Perform autocomplete and query on contacts with showAdmin=false and
     * expect the context admin to be excluded.
     */
    @Test
    public void testAdminIsExcluded() throws Exception {
        JSONObject oldConfig = setShowAdmin(Boolean.FALSE);
        try {
            AJAXClient adminClient = admin.getAjaxClient();
            int adminId = adminClient.getValues().getUserId();
            adminClient.logout();

            Contact adminContact = getClient().execute(new GetRequest(adminId, TimeZones.UTC)).getContact();
            assertNotNull("admin contact was null", adminContact);

            String prefix = adminContact.getDisplayName().substring(0, 3);
            List<Facet> facets = autocomplete(prefix);
            FacetValue found = findByDisplayName(facets, adminContact.getDisplayName());
            assertNull("admin contact was included in autocomplete response", found);

            List<ActiveFacet> activeFacets = new LinkedList<ActiveFacet>();
            activeFacets.add(createQuery(adminContact.getDisplayName()));
            List<PropDocument> documents = query(activeFacets);
            PropDocument adminDoc = findByProperty(documents, "display_name", DisplayItems.convert(adminContact, getClient().getValues().getLocale(), i18nServiceRegistry).getDisplayName());
            assertNull("admin contact was included in query response", adminDoc);
        } finally {
            restoreOldConfig(oldConfig);
        }
    }

    /*
     * Perform autocomplete and query on contacts with showAdmin=true and
     * expect the context admin to be included.
     */
    @Test
    public void testAdminIsIncluded() throws Exception {
        JSONObject oldConfig = setShowAdmin(Boolean.TRUE);
        try {
            AJAXClient adminClient = admin.getAjaxClient();
            int adminId = adminClient.getValues().getUserId();
            adminClient.logout();

            Contact adminContact = getClient().execute(new GetRequest(adminId, TimeZones.UTC)).getContact();
            assertNotNull("admin contact was null", adminContact);

            String prefix = adminContact.getDisplayName().substring(0, 3);
            List<Facet> facets = autocomplete(prefix);
            FacetValue found = findByDisplayName(facets, DisplayItems.convert(adminContact, getClient().getValues().getLocale(), i18nServiceRegistry).getDisplayName());
            assertNotNull("admin contact was not included in autocomplete response", found);

            List<ActiveFacet> activeFacets = new LinkedList<ActiveFacet>();
            activeFacets.add(createQuery(adminContact.getDisplayName()));
            List<PropDocument> documents = query(activeFacets);
            PropDocument adminDoc = findByProperty(documents, "display_name", adminContact.getDisplayName());
            assertNotNull("admin contact was not included in query response", adminDoc);
        } finally {
            restoreOldConfig(oldConfig);
        }
    }

    protected List<PropDocument> query(List<ActiveFacet> facets) throws Exception {
        Map<String, String> options = new HashMap<String, String>();
        QueryRequest queryRequest = new QueryRequest(true, 0, Integer.MAX_VALUE, facets, options, Module.CONTACTS.getIdentifier(), null);
        QueryResponse queryResponse = getClient().execute(queryRequest);
        SearchResult result = queryResponse.getSearchResult();
        List<PropDocument> propDocuments = new ArrayList<PropDocument>();
        List<Document> documents = result.getDocuments();
        for (Document document : documents) {
            propDocuments.add((PropDocument) document);
        }
        return propDocuments;
    }

    private List<Facet> autocomplete(String prefix) throws Exception {
        Map<String, String> options = new HashMap<String, String>();
        AutocompleteRequest autocompleteRequest = new AutocompleteRequest(prefix, Module.CONTACTS.getIdentifier(), options);
        AutocompleteResponse autocompleteResponse = getClient().execute(autocompleteRequest);
        return autocompleteResponse.getFacets();
    }

}
