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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONException;
import com.openexchange.ajax.find.AbstractFindTest;
import com.openexchange.ajax.find.PropDocument;
import com.openexchange.ajax.find.actions.AutocompleteRequest;
import com.openexchange.ajax.find.actions.AutocompleteResponse;
import com.openexchange.ajax.mail.actions.ImportMailRequest;
import com.openexchange.ajax.mail.actions.ImportMailResponse;
import com.openexchange.ajax.user.actions.GetRequest;
import com.openexchange.ajax.user.actions.GetResponse;
import com.openexchange.exception.OXException;
import com.openexchange.find.Module;
import com.openexchange.find.common.CommonFacetType;
import com.openexchange.find.facet.ActiveFacet;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.facet.Filter;
import com.openexchange.find.mail.MailFacetType;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;


/**
 * {@link BasicMailTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class BasicMailTest extends AbstractFindTest {

    private FolderObject testFolder;

    public BasicMailTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        String inboxFolder = client.getValues().getInboxFolder();
        String folderName = "findApiMailTestFolder_" + System.currentTimeMillis();
        testFolder = new FolderObject();
        testFolder.setModule(FolderObject.MAIL);
        testFolder.setFullName(inboxFolder + "/" + folderName);
        testFolder.setFolderName(folderName);
        testFolder = folderManager.insertFolderOnServer(testFolder);
    }

    @Override
    protected void tearDown() throws Exception {
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
        FacetValue found = detectContact(autocompleteResponse.getFacets());
        assertNotNull("own contact was missing in response", found);

        /*
         * Set own contact as activeFacet
         */
        ActiveFacet activeFacet = new ActiveFacet(MailFacetType.CONTACTS, found.getId(), found.getFilters().get(0));
        autocompleteRequest = new AutocompleteRequest(prefix, Module.MAIL.getIdentifier(), Collections.singletonList(activeFacet));
        autocompleteResponse = client.execute(autocompleteRequest);
        found = detectContact(autocompleteResponse.getFacets());
        assertNull("Own contact should've been missing in response", found);
    }

    public void testSearch() throws Exception {
        /*
         * Import test mail
         */
        String defaultAddress = client.getValues().getSendAddress();
        String[][] mailIds = importMail(defaultAddress, defaultAddress, "Find me", "");
        assertNotNull("mail was not imported", mailIds);

        /*
         * ...and search for it using the subject as query and the default sender address
         */
        List<String> addressFields = new ArrayList<String>(3);
        addressFields.add("from");
        addressFields.add("to");
        addressFields.add("cc");

        List<ActiveFacet> facets = prepareFacets();
        facets.add(createActiveFacet(MailFacetType.CONTACTS, "some/id", new Filter(addressFields, defaultAddress)));
        facets.add(createQuery("Find me"));

        List<PropDocument> documents = query(facets);
        assertEquals("Did not find mail", 1, documents.size());
        PropDocument document = documents.get(0);
        Object mailId = document.getProps().get("id");
        assertEquals("Wrong mail found", mailIds[0][1], mailId);

        /*
         * Now filter additionally for a mail address not contained in the mail headers
         */
        facets.add(createActiveFacet(MailFacetType.CONTACTS, "some/other/id", new Filter(addressFields, "unknown@example.com")));
        documents = query(facets);
        assertEquals("Mail found but should not", 0, documents.size());
    }

    public void testMultipleGlobalFacets() throws Exception {
        /*
         * Import test mail
         */
        String defaultAddress = client.getValues().getSendAddress();
        String[][] mailIds = importMail(defaultAddress, defaultAddress, "Find me", "");
        assertNotNull("mail was not imported", mailIds);

        /*
         * Add two global facets with terms contained in the mails subject and search for it
         */
        List<ActiveFacet> facets = prepareFacets();
        facets.add(createQuery("Find"));
        facets.add(createQuery("me"));
        List<PropDocument> documents = query(facets);
        assertEquals("Did not find mail", 1, documents.size());
        PropDocument document = documents.get(0);
        Object mailId = document.getProps().get("id");
        assertEquals("Wrong mail found", mailIds[0][1], mailId);

        /*
         * Add another one that is not contained. Now nothing should be found
         */
        facets.add(createQuery("again"));
        documents = query(facets);
        assertEquals("Mail found but should not", 0, documents.size());
    }

    public void testPagination() throws Exception {
        /*
         * Import test mails
         */
        String defaultAddress = client.getValues().getSendAddress();
        String[][] mailIds = importMails(3, defaultAddress, defaultAddress);
        assertNotNull("mail was not imported", mailIds);

        /*
         * And look for them
         */
        List<ActiveFacet> facets = prepareFacets();
        facets.add(createActiveFacet(MailFacetType.CONTACTS, "", "from", defaultAddress));
        List<PropDocument> documents = query(facets, 0, 5);
        assertEquals("Should only find 3 mail", 3, documents.size());
        documents = query(facets, 1, 5);
        assertEquals("Should only find 2 mail", 2, documents.size());
        documents = query(facets, 2, 5);
        assertEquals("Should only find 1 mail", 1, documents.size());
        documents = query(facets, 3, 5);
        assertEquals("Should only find 0 mail", 0, documents.size());
    }

    private List<ActiveFacet> prepareFacets() {
        List<ActiveFacet> facets = new LinkedList<ActiveFacet>();
        facets.add(createActiveFacet(CommonFacetType.FOLDER, testFolder.getFullName(), Filter.NO_FILTER));
        return facets;
    }

    private List<PropDocument> query(List<ActiveFacet> facets, int start, int size) throws Exception {
        return query(Module.MAIL, facets, start, size);
    }

    private List<PropDocument> query(List<ActiveFacet> facets) throws Exception {
        return query(Module.MAIL, facets);
    }

    private String[][] importMails(int num, String fromHeader, String toHeader) throws OXException, IOException, JSONException {
        InputStream[] streams = new InputStream[num];
        for (int i = 0; i < num; i++) {
            String mail = MAIL1
                .replaceAll("#FROM#", fromHeader)
                .replaceAll("#TO#", toHeader)
                .replaceAll("#SUBJECT#", randomUID())
                .replaceAll("#BODY#", randomUID());
            streams[i] = new ByteArrayInputStream(mail.getBytes(com.openexchange.java.Charsets.UTF_8));
        }

        ImportMailRequest request = new ImportMailRequest(testFolder.getFullName(), 0, streams);
        ImportMailResponse response = client.execute(request);
        return response.getIds();
    }

    private String[][] importMail(String fromHeader, String toHeader, String subject, String body) throws OXException, IOException, JSONException {
        String mail = MAIL1
            .replaceAll("#FROM#", fromHeader)
            .replaceAll("#TO#", toHeader)
            .replaceAll("#SUBJECT#", subject)
            .replaceAll("#BODY#", body);
        ByteArrayInputStream mailStream = new ByteArrayInputStream(mail.getBytes(com.openexchange.java.Charsets.UTF_8));
        ImportMailRequest request = new ImportMailRequest(testFolder.getFullName(), 0, new ByteArrayInputStream[] { mailStream });
        ImportMailResponse response = client.execute(request);
        return response.getIds();
    }

    private FacetValue detectContact(List<Facet> facets) throws OXException, IOException, JSONException {
        GetRequest getRequest = new GetRequest(client.getValues().getUserId(), client.getValues().getTimeZone());
        GetResponse getResponse = client.execute(getRequest);
        Contact contact = getResponse.getContact();
        FacetValue found = findByDisplayName(facets, contact.getDisplayName());
        return found;
    }

    private static final String MAIL1 =
        "From: #FROM#\n" +
        "To: #TO#\n" +
        "Subject: #SUBJECT#\n" +
        "Mime-Version: 1.0\n" +
        "Content-Type: text/plain; charset=\"UTF-8\"\n" +
        "Content-Transfer-Encoding: 8bit\n" +
        "\n" +
        "Content\n" +
        "#BODY#\n";

}
