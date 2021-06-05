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

import static com.openexchange.find.common.CommonFacetType.GLOBAL;
import static com.openexchange.find.contacts.ContactsFacetType.ADDRESS;
import static com.openexchange.find.contacts.ContactsFacetType.CONTACT_TYPE;
import static com.openexchange.find.contacts.ContactsFacetType.EMAIL;
import static com.openexchange.find.contacts.ContactsFacetType.NAME;
import static com.openexchange.find.contacts.ContactsFacetType.PHONE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.contact.action.AllRequest;
import com.openexchange.ajax.find.PropDocument;
import com.openexchange.ajax.find.actions.AutocompleteRequest;
import com.openexchange.ajax.find.actions.AutocompleteResponse;
import com.openexchange.ajax.find.actions.QueryRequest;
import com.openexchange.ajax.find.actions.QueryResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.find.Document;
import com.openexchange.find.Module;
import com.openexchange.find.SearchResult;
import com.openexchange.find.common.CommonFacetType;
import com.openexchange.find.common.FolderType;
import com.openexchange.find.facet.ActiveFacet;
import com.openexchange.find.facet.ExclusiveFacet;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.FacetType;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.facet.Filter;
import com.openexchange.find.facet.SimpleFacet;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link QueryTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class QueryTest extends ContactsFindTest {

    @Test
    public void testFilterChaining() throws Exception {
        Contact contact = randomContact();
        List<ActiveFacet> facets = new ArrayList<ActiveFacet>();
        facets.add(applyMatchingFacet(PHONE, contact, randomUID(), "phone", PHONE_COLUMNS, true));
        facets.add(applyMatchingFacet(NAME, contact, randomUID(), "name", NAME_COLUMNS, true));
        facets.add(applyMatchingFacet(ADDRESS, contact, randomUID(), "address", ADDRESS_COLUMNS, true));
        facets.add(applyMatchingFacet(EMAIL, contact, randomUID() + "@example.org", "email", EMAIL_COLUMNS, true));
        facets.add(applyMatchingFacet(GLOBAL, contact, randomUID(), "address_book", ADDRESSBOOK_COLUMNS, true));
        facets.add(createActiveFieldFacet(CONTACT_TYPE, "contact_type", "contact"));
        facets.add(createFolderTypeFacet(FolderType.PRIVATE));
        contact = cotm.newAction(contact);
        assertFalse(cotm.getLastResponse().getErrorMessage(), cotm.getLastResponse().hasError());
        assertFoundDocumentInSearch(facets, contact.getEmail1());
    }

    @Test
    public void testFilterPhone() throws Exception {
        testStringFilter(PHONE, "phone", PHONE_COLUMNS);
    }

    @Test
    public void testFilterName() throws Exception {
        testStringFilter(NAME, "name", NAME_COLUMNS);
    }

    @Test
    public void testFilterAddressbook() throws Exception {
        testStringFilter(GLOBAL, "address_book", ADDRESS_COLUMNS);
    }

    @Test
    public void testFilterAddress() throws Exception {
        testStringFilter(ADDRESS, "address", ADDRESS_COLUMNS);
    }

    @Test
    public void testFilterEmail() throws Exception {
        testStringFilter(EMAIL, "email", randomUID() + "@example.com", EMAIL_COLUMNS);
    }

    @Test
    public void testSearchEmailInDistributionList() throws Exception {
        // Test case for bug 38260
        Contact distributionList = randomContact();
        distributionList.setDistributionList(new DistributionListEntryObject[] { new DistributionListEntryObject(randomUID(), randomUID() + "someuniquestring@example.com", DistributionListEntryObject.INDEPENDENT)
        });
        cotm.newAction(distributionList);
        List<PropDocument> response = query(Collections.singletonList(createQuery("someuniquestring")));
        assertTrue("Distribution list not found", 0 < response.size());
    }

    @Test
    public void testFilterContactType() throws Exception {
        Contact contact = randomContact();
        Contact distributionList = randomContact();
        distributionList.setDistributionList(new DistributionListEntryObject[] { new DistributionListEntryObject(randomUID(), randomUID() + "@example.com", DistributionListEntryObject.INDEPENDENT), new DistributionListEntryObject(randomUID(), randomUID() + "@example.com", DistributionListEntryObject.INDEPENDENT), new DistributionListEntryObject(randomUID(), randomUID() + "@example.com", DistributionListEntryObject.INDEPENDENT),
        });
        cotm.newAction(contact, distributionList);

        List<PropDocument> contactDocuments = query(Collections.singletonList(createActiveFacet(CONTACT_TYPE, "contact", "contact_type", "contact")));
        assertTrue("no contacts found", 0 < contactDocuments.size());
        assertNotNull("contact not found", findByProperty(contactDocuments, "email1", contact.getEmail1()));
        assertNull("distribution list found", findByProperty(contactDocuments, "display_name", distributionList.getDisplayName()));

        List<PropDocument> distListDocuments = query(Collections.singletonList(createActiveFacet(CONTACT_TYPE, "distribution list", "contact_type", "distribution list")));
        assertTrue("no distribution lists found", 0 < distListDocuments.size());
        assertNull("contact found", findByProperty(distListDocuments, "email1", contact.getEmail1()));
        assertNotNull("distribution list not found", findByProperty(distListDocuments, "display_name", distributionList.getDisplayName()));
    }

    @Test
    public void testFilterFolderType() throws Exception {
        Contact contact = cotm.newAction(randomContact());
        ActiveFacet query = createQuery(contact.getEmail1());
        List<PropDocument> privateFolderDocuments = query(Arrays.asList(createFolderTypeFacet(FolderType.PRIVATE), query));
        assertTrue("no contacts found", 0 < privateFolderDocuments.size());
        assertNotNull("contact not found", findByProperty(privateFolderDocuments, "email1", contact.getEmail1()));
        List<PropDocument> sharedFolderDocuments = query(Arrays.asList(createFolderTypeFacet(FolderType.SHARED), query));
        assertNull("contact found", findByProperty(sharedFolderDocuments, "email1", contact.getEmail1()));
        List<PropDocument> publicFolderDocuments = query(Arrays.asList(createFolderTypeFacet(FolderType.PUBLIC), query));
        assertNull("contact found", findByProperty(publicFolderDocuments, "email1", contact.getEmail1()));
        publicFolderDocuments = query(Arrays.asList(createFolderTypeFacet(FolderType.PUBLIC), createQuery(getClient().getValues().getDefaultAddress())));
        assertNotNull("user contact not found", findByProperty(publicFolderDocuments, "email1", getClient().getValues().getDefaultAddress()));
    }

    @Test
    public void testTokenizedQuery() throws Exception {
        Contact contact = randomContact();
        String t1 = randomUID();
        String t2 = randomUID();
        String t3 = randomUID();
        contact.setSurName(t1 + " " + t2 + " " + t3);
        contact = cotm.newAction(contact);

        SimpleFacet globalFacet = (SimpleFacet) findByType(CommonFacetType.GLOBAL, autocomplete(Module.CONTACTS, t1 + " " + t3));
        List<PropDocument> documents = query(Collections.singletonList(createActiveFacet(globalFacet)));
        assertTrue("no contact found", 0 < documents.size());
        assertNotNull("contact not found", findByProperty(documents, "last_name", contact.getSurName()));

        globalFacet = (SimpleFacet) findByType(CommonFacetType.GLOBAL, autocomplete(Module.CONTACTS, "\"" + t1 + " " + t2 + "\""));
        documents = query(Collections.singletonList(createActiveFacet(globalFacet)));
        assertTrue("no contact found", 0 < documents.size());
        assertNotNull("contact not found", findByProperty(documents, "last_name", contact.getSurName()));

        globalFacet = (SimpleFacet) findByType(CommonFacetType.GLOBAL, autocomplete(Module.CONTACTS, "\"" + t1 + " " + t3 + "\""));
        documents = query(Collections.singletonList(createActiveFacet(globalFacet)));
        assertTrue("contact found", 0 == documents.size());
    }

    private ActiveFacet applyMatchingFacet(FacetType type, Contact contact, String value, String filterField, int[] searchedColumns, boolean unassignedOnly) {
        if (unassignedOnly) {
            List<Integer> unassignedColumns = new ArrayList<Integer>();
            for (int column : searchedColumns) {
                if (false == contact.contains(column)) {
                    unassignedColumns.add(Integer.valueOf(column));
                }
            }
            if (0 == unassignedColumns.size()) {
                fail("no unassigned fields from " + Arrays.toString(searchedColumns) + " left.");
            }
            contact.set(unassignedColumns.get(random.nextInt(unassignedColumns.size())).intValue(), value);
        } else {
            contact.set(searchedColumns[random.nextInt(searchedColumns.length)], value);
        }
        return createActiveFacet(type, String.valueOf(contact.getObjectID()), filterField, value);
    }

    private void testStringFilter(FacetType type, String filterField, int[] searchedColumns) throws Exception {
        testStringFilter(type, filterField, randomUID(), searchedColumns);
    }

    private void testStringFilter(FacetType type, String filterField, String value, int[] searchedColumns) throws Exception {
        Contact contact = randomContact();
        ActiveFacet facet = applyMatchingFacet(type, contact, value, filterField, searchedColumns, true);
        contact = cotm.newAction(contact);
        assertFoundDocumentInSearch(Collections.singletonList(facet), contact.getEmail1());
        assertEmptyResults(Collections.singletonList(createActiveFacet(type, filterField, filterField, randomUID())));
    }

    private PropDocument assertFoundDocumentInSearch(List<ActiveFacet> facets, String expectedEmail1) throws Exception {
        List<PropDocument> documents = query(facets);
        assertTrue("No contact documents found", 0 < documents.size());
        PropDocument document = findByProperty(documents, "email1", expectedEmail1);
        assertNotNull("no document found for: " + expectedEmail1, document);
        return document;
    }

    private void assertEmptyResults(List<ActiveFacet> facets) throws Exception {
        QueryRequest queryRequest = new QueryRequest(0, 10, facets, Module.CONTACTS.getIdentifier());
        QueryResponse queryResponse = getClient().execute(queryRequest);
        SearchResult result = queryResponse.getSearchResult();
        List<Document> documents = result.getDocuments();
        assertEquals("Documents were found", 0, documents.size());
    }

    @Test
    public void testFolderTypeFacet() throws Exception {
        try {
            FolderType[] typesInOrder = new FolderType[] { FolderType.PRIVATE, FolderType.PUBLIC, FolderType.SHARED };
            AJAXClient[] clients = new AJAXClient[] { getClient(), getClient(), client2 };
            FolderObject[] folders = new FolderObject[3];
            folders[0] = ftm.insertFolderOnServer(ftm.generatePrivateFolder(randomUID(), FolderObject.CONTACT, getClient().getValues().getPrivateContactFolder(), getClient().getValues().getUserId()));
            folders[1] = ftm.insertFolderOnServer(ftm.generatePublicFolder(randomUID(), FolderObject.CONTACT, FolderObject.SYSTEM_PUBLIC_FOLDER_ID, getClient().getValues().getUserId()));
            folders[2] = ftm.insertFolderOnServer(ftm.generateSharedFolder(randomUID(), FolderObject.CONTACT, getClient().getValues().getPrivateContactFolder(), getClient().getValues().getUserId(), client2.getValues().getUserId()));

            Contact[] contacts = new Contact[3];
            contacts[0] = cotm.newAction(randomContact(folders[0].getObjectID()));
            contacts[1] = cotm.newAction(randomContact(folders[1].getObjectID()));
            contacts[2] = cotm.newAction(randomContact(folders[2].getObjectID()));

            ActiveFacet wildcardQuery = createQuery("*");
            for (int i = 0; i < 3; i++) {
                FolderType folderType = typesInOrder[i];
                List<Facet> facets = autocomplete(clients[i], "");
                ExclusiveFacet folderTypeFacet = (ExclusiveFacet) findByType(CommonFacetType.FOLDER_TYPE, facets);
                FacetValue typeValue = findByValueId(folderType.getIdentifier(), folderTypeFacet);
                List<PropDocument> docs = query(clients[i], Arrays.asList(wildcardQuery, createActiveFacet(folderTypeFacet, typeValue)));
                PropDocument[] foundDocs = new PropDocument[3];
                for (PropDocument doc : docs) {
                    Map<String, Object> props = doc.getProps();
                    if (contacts[0].getSurName().equals(props.get("last_name"))) {
                        foundDocs[0] = doc;
                        continue;
                    } else if (contacts[1].getSurName().equals(props.get("last_name"))) {
                        foundDocs[1] = doc;
                        continue;
                    } else if (contacts[2].getSurName().equals(props.get("last_name"))) {
                        foundDocs[2] = doc;
                        continue;
                    }
                }

                switch (folderType) {
                    case PRIVATE:
                        assertNotNull("Private contact not found", foundDocs[0]);
                        assertNull("Public contact found but should not", foundDocs[1]);
                        assertNotNull("Shared contact not found", foundDocs[2]);
                        break;

                    case PUBLIC:
                        assertNull("Private contact found but should not", foundDocs[0]);
                        assertNotNull("Public contact not found", foundDocs[1]);
                        assertNull("Shared contact found but should not", foundDocs[2]);
                        break;

                    case SHARED:
                        assertNull("Private contact found but should not", foundDocs[0]);
                        assertNull("Public contact found but should not", foundDocs[1]);
                        assertNotNull("Shared contact not found", foundDocs[2]);
                        break;
                }
            }
        } finally {
            client2.logout();
        }
    }

    @Test
    public void testPaging() throws Exception {
        //TODO: number of results is wrong when admin is excluded (MW-1100)
        //      therefore ensure that admin contact filtering is disabled when testing paging
        JSONObject oldConfig = setShowAdmin(Boolean.TRUE);
        try {
            int numberOfContacts = getClient().execute(new AllRequest(6, AllRequest.GUI_COLUMNS)).getArray().length;
            Set<Integer> names = new HashSet<Integer>();
            ActiveFacet folderFacet = createActiveFacet(CommonFacetType.FOLDER, 6, Filter.NO_FILTER);
            ActiveFacet wildcardQuery = createQuery("*");

            int start = 0;
            int size = numberOfContacts / 2;
            List<PropDocument> results = query(Arrays.asList(wildcardQuery, folderFacet), start, size, null);
            assertEquals("Wrong number of results", size, results.size());
            for (PropDocument result : results) {
                names.add(Integer.valueOf(String.class.cast(result.getProps().get("id"))));
            }
            assertEquals("Wrong number of unique results", size, names.size());

            start = size;
            size = numberOfContacts - start;
            results = query(Arrays.asList(wildcardQuery, folderFacet), start, size, null);
            assertEquals("Wrong number of results", size, results.size());
            for (PropDocument result : results) {
                names.add(Integer.valueOf(String.class.cast(result.getProps().get("id"))));
            }
            assertEquals("Wrong number of unique results", numberOfContacts, names.size());
        } finally {
            restoreOldConfig(oldConfig);
        }
    }

    protected List<Facet> autocomplete(AJAXClient client, String prefix) throws Exception {
        AutocompleteRequest autocompleteRequest = new AutocompleteRequest(prefix, Module.CONTACTS.getIdentifier());
        AutocompleteResponse autocompleteResponse = client.execute(autocompleteRequest);
        return autocompleteResponse.getFacets();
    }

    protected List<PropDocument> query(AJAXClient client, List<ActiveFacet> facets) throws Exception {
        QueryRequest queryRequest = new QueryRequest(0, Integer.MAX_VALUE, facets, Module.CONTACTS.getIdentifier());
        QueryResponse queryResponse = client.execute(queryRequest);
        SearchResult result = queryResponse.getSearchResult();
        List<PropDocument> propDocuments = new ArrayList<PropDocument>();
        List<Document> documents = result.getDocuments();
        for (Document document : documents) {
            propDocuments.add((PropDocument) document);
        }
        return propDocuments;
    }

    protected List<PropDocument> query(List<ActiveFacet> facets, int start, int size, Map<String, String> options) throws Exception {
        QueryRequest queryRequest = new QueryRequest(true, start, size, facets, options, Module.CONTACTS.getIdentifier(), null);
        QueryResponse queryResponse = getClient().execute(queryRequest);
        SearchResult result = queryResponse.getSearchResult();
        List<PropDocument> propDocuments = new ArrayList<PropDocument>();
        List<Document> documents = result.getDocuments();
        for (Document document : documents) {
            propDocuments.add((PropDocument) document);
        }
        return propDocuments;
    }

}
