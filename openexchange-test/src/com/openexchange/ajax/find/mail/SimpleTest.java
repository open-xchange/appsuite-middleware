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

package com.openexchange.ajax.find.mail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONException;
import com.openexchange.ajax.find.AbstractFindTest;
import com.openexchange.ajax.find.PropDocument;
import com.openexchange.ajax.find.actions.AutocompleteRequest;
import com.openexchange.ajax.find.actions.AutocompleteResponse;
import com.openexchange.ajax.find.actions.QueryRequest;
import com.openexchange.ajax.find.actions.QueryResponse;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.mail.actions.ImportMailRequest;
import com.openexchange.ajax.mail.actions.ImportMailResponse;
import com.openexchange.ajax.user.actions.GetRequest;
import com.openexchange.ajax.user.actions.GetResponse;
import com.openexchange.exception.OXException;
import com.openexchange.find.Document;
import com.openexchange.find.Module;
import com.openexchange.find.SearchResult;
import com.openexchange.find.common.CommonFacetType;
import com.openexchange.find.facet.ActiveFacet;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.facet.Filter;
import com.openexchange.find.mail.MailFacetType;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;


/**
 * {@link SimpleTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class SimpleTest extends AbstractFindTest {

    private FolderObject testFolder;

    public SimpleTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        String inboxFolder = client.getValues().getInboxFolder();
        testFolder = new FolderObject();
        testFolder.setModule(FolderObject.MAIL);
        testFolder.setFullName(inboxFolder + "/" + "findApiMailTestFolder");
        testFolder.setFolderName("findApiMailTestFolder");
        InsertRequest insertRequestReq = new InsertRequest(EnumAPI.OX_NEW, testFolder);
        InsertResponse insertResponseResp = client.execute(insertRequestReq);
        insertResponseResp.fillObject(testFolder);
    }

    @Override
    protected void tearDown() throws Exception {
        if (testFolder != null) {
            DeleteRequest deleteRequestReq = new DeleteRequest(EnumAPI.OX_NEW, testFolder);
            client.execute(deleteRequestReq);
        }
        super.tearDown();
    }

    public void testAutocomplete() throws Exception {
        /*
         * Expect the clients contact in autocomplete response
         */
        String defaultAddress = client.getValues().getDefaultAddress();
        String prefix = defaultAddress.substring(0, 3);
        AutocompleteRequest autocompleteRequest = new AutocompleteRequest(prefix, Module.MAIL.getIdentifier());
        AutocompleteResponse autocompleteResponse = client.execute(autocompleteRequest);
        FacetValue found = detectContact(autocompleteResponse.getFacets(), defaultAddress);
        assertNotNull("own contact was missing in response", found);

        /*
         * Set own contact as activeFacet
         */
        ActiveFacet activeFacet = new ActiveFacet(MailFacetType.CONTACTS, found.getId(), found.getFilters().get(0));
        autocompleteRequest = new AutocompleteRequest(prefix, Module.MAIL.getIdentifier(), Collections.singletonList(activeFacet));
        autocompleteResponse = client.execute(autocompleteRequest);
        found = detectContact(autocompleteResponse.getFacets(), defaultAddress);
        assertNull("Own contact should've been missing in response", found);
    }

    public void testSearch() throws Exception {
        /*
         * Import a mail to our test folder...
         */
        String defaultAddress = client.getValues().getSendAddress();
        String mail = MAIL.replaceAll("#ADDR#", defaultAddress);
        ByteArrayInputStream mailStream = new ByteArrayInputStream(mail.getBytes(com.openexchange.java.Charsets.UTF_8));
        ImportMailRequest request = new ImportMailRequest(testFolder.getFullName(), 0, new ByteArrayInputStream[] { mailStream });
        ImportMailResponse response = client.execute(request);
        String[] mailIds = response.getIds()[0];
        assertNotNull("mail was not imported", mailIds);

        /*
         * ...and search for it using the subject as query and the default sender address
         */
        List<String> addressFields = new ArrayList<String>(3);
        addressFields.add("from");
        addressFields.add("to");
        addressFields.add("cc");
        ActiveFacet globalFacet = new ActiveFacet(CommonFacetType.GLOBAL, CommonFacetType.GLOBAL.getId(), new Filter(Collections.singletonList("global"), "Find me"));
        ActiveFacet contactFacet = new ActiveFacet(MailFacetType.CONTACTS, "some/id", new Filter(addressFields, defaultAddress));
        ActiveFacet folderFacet = new ActiveFacet(CommonFacetType.FOLDER, testFolder.getFullName(), Filter.NO_FILTER);
        List<ActiveFacet> facets = new ArrayList<ActiveFacet>(4);
        facets.add(folderFacet);
        facets.add(contactFacet);
        facets.add(globalFacet);

        QueryRequest queryRequest = new QueryRequest(0, 10, facets, Module.MAIL.getIdentifier());
        QueryResponse queryResponse = client.execute(queryRequest);
        SearchResult result = queryResponse.getSearchResult();
        List<Document> documents = result.getDocuments();
        assertEquals("Did not find mail", 1, documents.size());
        PropDocument document = (PropDocument) documents.get(0);
        Object mailId = document.getProps().get("id");
        assertEquals("Wrong mail found", mailIds[1], mailId);

        /*
         * Now filter additionally for a mail address not contained in the mail headers
         */
        facets.add(new ActiveFacet(MailFacetType.CONTACTS, "some/other/id", new Filter(addressFields, "unknown@example.com")));
        queryRequest = new QueryRequest(0, 10, facets, Module.MAIL.getIdentifier());
        queryResponse = client.execute(queryRequest);
        result = queryResponse.getSearchResult();
        documents = result.getDocuments();
        assertEquals("Mail found but should not", 0, documents.size());
    }

    private FacetValue detectContact(List<Facet> facets, String defaultAddress) throws OXException, IOException, JSONException {
        GetRequest getRequest = new GetRequest(client.getValues().getUserId(), client.getValues().getTimeZone());
        GetResponse getResponse = client.execute(getRequest);
        Contact contact = getResponse.getContact();
        FacetValue found = null;
        outer: for (Facet facet : facets) {
            if (MailFacetType.CONTACTS == facet.getType()) {
                List<FacetValue> values = facet.getValues();
                for (FacetValue value : values) {
                    if (contact.getDisplayName().equals(value.getDisplayItem().getDefaultValue())) {
                        found = value;
                        break outer;
                    }
                }
                break;
            }
        }

        return found;
    }

    private static final String MAIL =
        "From: #ADDR#\n" +
        "To: #ADDR#\n" +
        "Subject: Find me\n" +
        "Mime-Version: 1.0\n" +
        "Content-Type: text/plain; charset=\"UTF-8\"\n" +
        "Content-Transfer-Encoding: 8bit\n" +
        "\n" +
        "Content\n" +
        "\u00e4\u00f6\u00fc\u00c4\u00d6\u00dc\u00df\n";

}
