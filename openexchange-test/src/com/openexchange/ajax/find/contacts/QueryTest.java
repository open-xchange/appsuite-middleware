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

package com.openexchange.ajax.find.contacts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.ajax.find.PropDocument;
import com.openexchange.ajax.find.actions.QueryRequest;
import com.openexchange.ajax.find.actions.QueryResponse;
import com.openexchange.find.Document;
import com.openexchange.find.Module;
import com.openexchange.find.SearchResult;
import com.openexchange.find.facet.Filter;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;
import edu.emory.mathcs.backport.java.util.Arrays;


/**
 * {@link QueryTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class QueryTest extends ContactsFindTest {

    /**
     * Initializes a new {@link QueryTest}.
     *
     * @param name The test name
     */
    public QueryTest(String name) {
        super(name);
    }

    public void testFilterChaining() throws Exception {
        Contact contact = randomContact();
        List<Filter> filters = new ArrayList<Filter>();
        filters.add(applyMatchingFilter(contact, randomUID(), "phone", PHONE_COLUMNS, true));
        filters.add(applyMatchingFilter(contact, randomUID(), "name", NAME_COLUMNS, true));
        filters.add(applyMatchingFilter(contact, randomUID(), "address", ADDRESS_COLUMNS, true));
        filters.add(applyMatchingFilter(contact, randomUID() + "@example.org", "email", EMAIL_COLUMNS, true));
        filters.add(applyMatchingFilter(contact, randomUID(), "address_book", ADDRESSBOOK_COLUMNS, true));
        filters.add(new Filter(Collections.singletonList("contact_type"), "contact"));
        filters.add(new Filter(Collections.singletonList("folder_type"), "private"));
        contact = manager.newAction(contact);
        assertFoundDocumentInSearch(Collections.<String>emptyList(), filters, contact.getEmail1());
    }

    public void testFilterPhone() throws Exception {
        testStringFilter("phone", PHONE_COLUMNS);
    }

    public void testFilterName() throws Exception {
        testStringFilter("name", NAME_COLUMNS);
    }

    public void testFilterAddressbook() throws Exception {
        testStringFilter("address_book", ADDRESS_COLUMNS);
    }

    public void testFilterAddress() throws Exception {
        testStringFilter("address", ADDRESS_COLUMNS);
    }

    public void testFilterEmail() throws Exception {
        testStringFilter("email", randomUID() + "@example.com", EMAIL_COLUMNS);
    }

    public void testFilterContactType() throws Exception {
        Contact contact = randomContact();
        Contact distributionList = randomContact();
        distributionList.setDistributionList(new DistributionListEntryObject[] {
            new DistributionListEntryObject(randomUID(), randomUID() + "@example.com", DistributionListEntryObject.INDEPENDENT),
            new DistributionListEntryObject(randomUID(), randomUID() + "@example.com", DistributionListEntryObject.INDEPENDENT),
            new DistributionListEntryObject(randomUID(), randomUID() + "@example.com", DistributionListEntryObject.INDEPENDENT),
        });
        manager.newAction(contact, distributionList);

        List<PropDocument> contactDocuments = query(Collections.<String>emptyList(),
            Collections.singletonList(new Filter(Collections.singletonList("contact_type"), "contact")));
        assertTrue("no contacts found", 0 < contactDocuments.size());
        assertNotNull("contact not found", findByProperty(contactDocuments, "email1", contact.getEmail1()));
        assertNull("distribution list found", findByProperty(contactDocuments, "display_name", distributionList.getDisplayName()));

        List<PropDocument> distListDocuments = query(Collections.<String>emptyList(),
            Collections.singletonList(new Filter(Collections.singletonList("contact_type"), "distribution list")));
        assertTrue("no distribution lists found", 0 < distListDocuments.size());
        assertNull("contact found", findByProperty(distListDocuments, "email1", contact.getEmail1()));
        assertNotNull("distribution list not found", findByProperty(distListDocuments, "display_name", distributionList.getDisplayName()));
    }

    public void testFilterFolderType() throws Exception {
        Contact contact = manager.newAction(randomContact());
        List<PropDocument> privateFolderDocuments = query(Collections.<String>emptyList(),
            Collections.singletonList(new Filter(Collections.singletonList("folder_type"), "private")));
        assertTrue("no contacts found", 0 < privateFolderDocuments.size());
        assertNotNull("contact not found", findByProperty(privateFolderDocuments, "email1", contact.getEmail1()));
        List<PropDocument> sharedFolderDocuments = query(Collections.<String>emptyList(),
            Collections.singletonList(new Filter(Collections.singletonList("folder_type"), "shared")));
        assertNull("contact found", findByProperty(sharedFolderDocuments, "email1", contact.getEmail1()));
        List<PropDocument> publicFolderDocuments = query(Collections.<String>emptyList(),
            Collections.singletonList(new Filter(Collections.singletonList("folder_type"), "public")));
        assertNull("contact found", findByProperty(publicFolderDocuments, "email1", contact.getEmail1()));
        assertNotNull("user contact not found", findByProperty(publicFolderDocuments, "email1", client.getValues().getDefaultAddress()));
    }

    private Filter applyMatchingFilter(Contact contact, String value, String filterField, int[] searchedColumns, boolean unassignedOnly) {
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
            contact.set(unassignedColumns.get(random.nextInt(unassignedColumns.size())), value);
        } else {
            contact.set(searchedColumns[random.nextInt(searchedColumns.length)], value);
        }
        int start = random.nextInt(value.length() - 4);
        int stop = start + 4 + random.nextInt(value.length() - start - 4);
        String substring = value.substring(start, stop);
        return new Filter(Collections.singletonList(filterField), substring);
    }

    private void testStringFilter(String filterField, int[] searchedColumns) throws Exception {
        testStringFilter(filterField, randomUID(), searchedColumns);
    }

    private void testStringFilter(String filterField, String value, int[] searchedColumns) throws Exception {
        Contact contact = randomContact();
        Filter filter = applyMatchingFilter(contact, value, filterField, searchedColumns, true);
        contact = manager.newAction(contact);
        assertFoundDocumentInSearch(Collections.<String>emptyList(), Collections.singletonList(filter), contact.getEmail1());
        assertEmptyResults(Collections.<String>emptyList(), Collections.singletonList(
            new Filter(Collections.singletonList(filterField), randomUID())));
    }

    private PropDocument assertFoundDocumentInSearch(List<String> queries, List<Filter> filters, String expectedEmail1) throws Exception {
        List<PropDocument> documents = query(queries, filters);
        assertTrue("No contact documents found", 0 < documents.size());
        PropDocument document = findByProperty(documents, "email1", expectedEmail1);
        assertNotNull("no document found for: " + expectedEmail1, document);
        return document;
    }

    private void assertEmptyResults(List<String> queries, List<Filter> filters) throws Exception {
        QueryRequest queryRequest = new QueryRequest(0, 10, queries, filters, Module.CONTACTS.getIdentifier());
        QueryResponse queryResponse = client.execute(queryRequest);
        SearchResult result = queryResponse.getSearchResult();
        List<Document> documents = result.getDocuments();
        assertEquals("Documents were found", 0, documents.size());
    }

}
