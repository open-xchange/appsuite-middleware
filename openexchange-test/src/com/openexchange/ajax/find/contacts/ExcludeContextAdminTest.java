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

package com.openexchange.ajax.find.contacts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.ajax.find.AbstractFindTest;
import com.openexchange.ajax.find.PropDocument;
import com.openexchange.ajax.find.actions.AutocompleteRequest;
import com.openexchange.ajax.find.actions.AutocompleteResponse;
import com.openexchange.ajax.find.actions.QueryRequest;
import com.openexchange.ajax.find.actions.QueryResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
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

    public ExcludeContextAdminTest(String name) {
        super(name);
    }

    /*
     * Perform autocomplete and query on contacts with showAdmin=false and
     * expect the context admin to be excluded.
     */
    public void testAdminIsExcluded() throws Exception {
        AJAXClient adminClient = new AJAXClient(User.OXAdmin);
        int adminId = adminClient.getValues().getUserId();
        adminClient.logout();

        Contact adminContact = client.execute(new GetRequest(adminId, TimeZones.UTC)).getContact();
        assertNotNull("admin contact was null", adminContact);

        String prefix = adminContact.getDisplayName().substring(0, 3);
        List<Facet> facets = autocomplete(prefix, false);
        FacetValue found = findByDisplayName(facets, adminContact.getDisplayName());
        assertNull("admin contact was included in autocomplete response", found);

        List<ActiveFacet> activeFacets = new LinkedList<ActiveFacet>();
        activeFacets.add(createQuery(adminContact.getDisplayName()));
        List<PropDocument> documents = query(activeFacets, false);
        PropDocument adminDoc = findByProperty(documents, "display_name", DisplayItems.convert(adminContact).getDisplayName());
        assertNull("admin contact was included in query response", adminDoc);
    }

    /*
     * Perform autocomplete and query on contacts with showAdmin=true and
     * expect the context admin to be included.
     */
    public void testAdminIsIncluded() throws Exception {
        AJAXClient adminClient = new AJAXClient(User.OXAdmin);
        int adminId = adminClient.getValues().getUserId();
        adminClient.logout();

        Contact adminContact = client.execute(new GetRequest(adminId, TimeZones.UTC)).getContact();
        assertNotNull("admin contact was null", adminContact);

        String prefix = adminContact.getDisplayName().substring(0, 3);
        List<Facet> facets = autocomplete(prefix, true);
        FacetValue found = findByDisplayName(facets, DisplayItems.convert(adminContact).getDisplayName());
        assertNotNull("admin contact was not included in autocomplete response", found);

        List<ActiveFacet> activeFacets = new LinkedList<ActiveFacet>();
        activeFacets.add(createQuery(adminContact.getDisplayName()));
        List<PropDocument> documents = query(activeFacets, true);
        PropDocument adminDoc = findByProperty(documents, "display_name", adminContact.getDisplayName());
        assertNotNull("admin contact was not included in query response", adminDoc);
    }

    protected List<PropDocument> query(List<ActiveFacet> facets, boolean showAdmin) throws Exception {
        Map<String, String> options = new HashMap<String, String>();
        options.put("admin", Boolean.toString(showAdmin));
        QueryRequest queryRequest = new QueryRequest(true, 0, Integer.MAX_VALUE, facets, options, Module.CONTACTS.getIdentifier(), null);
        QueryResponse queryResponse = client.execute(queryRequest);
        SearchResult result = queryResponse.getSearchResult();
        List<PropDocument> propDocuments = new ArrayList<PropDocument>();
        List<Document> documents = result.getDocuments();
        for (Document document : documents) {
            propDocuments.add((PropDocument) document);
        }
        return propDocuments;
    }

    private List<Facet> autocomplete(String prefix, boolean showAdmin) throws Exception {
        Map<String, String> options = new HashMap<String, String>();
        options.put("admin", Boolean.toString(showAdmin));
        AutocompleteRequest autocompleteRequest = new AutocompleteRequest(prefix, Module.CONTACTS.getIdentifier(), options);
        AutocompleteResponse autocompleteResponse = client.execute(autocompleteRequest);
        return autocompleteResponse.getFacets();
    }

}
