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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import com.openexchange.ajax.find.AbstractFindTest;
import com.openexchange.ajax.find.PropDocument;
import com.openexchange.ajax.find.actions.AutocompleteRequest;
import com.openexchange.ajax.find.actions.AutocompleteResponse;
import com.openexchange.ajax.find.actions.TestDisplayItem;
import com.openexchange.ajax.mail.actions.ImportMailRequest;
import com.openexchange.ajax.mail.actions.ImportMailResponse;
import com.openexchange.ajax.user.actions.GetRequest;
import com.openexchange.ajax.user.actions.GetResponse;
import com.openexchange.configuration.MailConfig;
import com.openexchange.exception.OXException;
import com.openexchange.find.Module;
import com.openexchange.find.common.CommonConstants;
import com.openexchange.find.common.CommonFacetType;
import com.openexchange.find.facet.ActiveFacet;
import com.openexchange.find.facet.DefaultFacet;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.facet.Filter;
import com.openexchange.find.facet.SimpleFacet;
import com.openexchange.find.mail.MailFacetType;
import com.openexchange.find.util.DisplayItems;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.java.util.TimeZones;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.utils.DateUtils;
import com.openexchange.test.ContactTestManager;


/**
 * {@link BasicMailTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class BasicMailTest extends AbstractFindTest {

    private FolderObject testFolder;

    private String defaultAddress;

    private ContactTestManager contactManager;

    public BasicMailTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        defaultAddress = client.getValues().getSendAddress();
        String inboxFolder = client.getValues().getInboxFolder();
        String folderName = "findApiMailTestFolder_" + System.currentTimeMillis();
        testFolder = new FolderObject();
        testFolder.setModule(FolderObject.MAIL);
        testFolder.setFullName(inboxFolder + "/" + folderName);
        testFolder.setFolderName(folderName);
        testFolder = folderManager.insertFolderOnServer(testFolder);
        contactManager = new ContactTestManager(client);
    }

    @Override
    protected void tearDown() throws Exception {
        contactManager.cleanUp();
        super.tearDown();
    }

    public void testAutocomplete() throws Exception {
        /*
         * Set an image for autocomplete tests
         */
        GetRequest getRequest = new GetRequest(client.getValues().getUserId(), client.getValues().getTimeZone());
        GetResponse getResponse = client.execute(getRequest);
        Contact ownContact = getResponse.getContact();
        Contact modified = new Contact();
        modified.setObjectID(ownContact.getObjectID());

        MailConfig.init();
        String testDataDir = MailConfig.getProperty(MailConfig.Property.TEST_MAIL_DIR);
        byte[] image = FileUtils.readFileToByteArray(new File(testDataDir, "contact_image.png"));
        modified.setImage1(image);
        modified.setImageContentType("image/png");
        modified.setLastModified(new Date());
        contactManager.updateAction(ownContact.getParentFolderID(), modified);

        /*
         * Expect the clients contact in autocomplete response
         */
        String prefix = defaultAddress.substring(0, 3);
        AutocompleteRequest autocompleteRequest = new AutocompleteRequest(prefix, Module.MAIL.getIdentifier());
        AutocompleteResponse autocompleteResponse = client.execute(autocompleteRequest);
        List<Facet> facets = autocompleteResponse.getFacets();
        FacetValue found = detectContact(facets);
        assertNotNull("own contact was missing in response", found);

        /*
         * Mail address included in detail info?
         */
        String mailAddress = ((TestDisplayItem) found.getDisplayItem()).getDetail();
        assertEquals(defaultAddress, mailAddress);

        /*
         * Contact image url included?
         */
        String imageUrl = ((TestDisplayItem) found.getDisplayItem()).getImageUrl();
        assertNotNull("image_url missing in contact", imageUrl);

        /*
         * Set own contact as activeFacet
         */
        ActiveFacet activeFacet = createActiveFacet(MailFacetType.CONTACTS, found.getId(), found.getOptions().get(0).getFilter());
        facets = autocomplete(prefix, Collections.singletonList(activeFacet));
        found = detectContact(facets);
        assertNull("Own contact should've been missing in response", found);
    }

    public void testSearch() throws Exception {
        /*
         * Import test mail
         */
        String[][] mailIds = importMail(defaultAddress, "Find me", "");
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
        String[][] mailIds = importMail(defaultAddress, "Find me", "");
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
        String[][] mailIds = importMails(3, defaultAddress, defaultAddress);
        assertNotNull("mails not imported", mailIds);
        assertEquals("mails not imported", 3, mailIds.length);

        /*
         * And look for them
         */
        List<ActiveFacet> facets = prepareFacets();
        facets.add(createActiveFacet(MailFacetType.CONTACTS, "", "from", defaultAddress));
        List<PropDocument> documents = query(facets, 0, 5);
        assertEquals("Should only find 3 mails", 3, documents.size());
        documents = query(facets, 1, 5);
        assertEquals("Should only find 2 mails", 2, documents.size());
        documents = query(facets, 2, 5);
        assertEquals("Should only find 1 mails", 1, documents.size());
        documents = query(facets, 3, 5);
        assertEquals("Should only find 0 mails", 0, documents.size());
    }

    public void testDateFilter() throws Exception {
        Calendar calWithinLastWeek = new GregorianCalendar(TimeZones.UTC);
        calWithinLastWeek.add(Calendar.WEEK_OF_YEAR, -1);
        calWithinLastWeek.add(Calendar.DAY_OF_YEAR, 1);
        String idWithinLastWeek = importMail(defaultAddress, defaultAddress, randomUID(), randomUID(), calWithinLastWeek.getTime())[0][1];
        Calendar calWithinLastMonth = new GregorianCalendar(TimeZones.UTC);
        calWithinLastMonth.add(Calendar.MONTH, -1);
        calWithinLastMonth.add(Calendar.DAY_OF_YEAR, 1);
        String idWithinLastMonth = importMail(defaultAddress, defaultAddress, randomUID(), randomUID(), calWithinLastMonth.getTime())[0][1];
        Calendar calWithinLastYear = new GregorianCalendar(TimeZones.UTC);
        calWithinLastYear.add(Calendar.YEAR, -1);
        calWithinLastYear.add(Calendar.DAY_OF_YEAR, 1);
        String idWithinLastYear = importMail(defaultAddress, defaultAddress, randomUID(), randomUID(), calWithinLastYear.getTime())[0][1];
        Calendar calBeforeLastYear = new GregorianCalendar(TimeZones.UTC);
        calBeforeLastYear.add(Calendar.YEAR, -1);
        calBeforeLastYear.add(Calendar.DAY_OF_YEAR, -1);
        String idBeforeLastYear = importMail(defaultAddress, defaultAddress, randomUID(), randomUID(), calBeforeLastYear.getTime())[0][1];

        List<ActiveFacet> facets = prepareFacets();
        facets.add(createActiveFacet(CommonFacetType.DATE, CommonConstants.QUERY_LAST_WEEK, CommonConstants.FIELD_DATE, CommonConstants.QUERY_LAST_WEEK));
        List<PropDocument> documents = query(facets);
        assertEquals("Should only find 1 mails", 1, documents.size());
        assertNotNull("Mail not found", findByProperty(documents, "id", idWithinLastWeek));
        assertNull("Wrong mail found", findByProperty(documents, "id", idBeforeLastYear));
        /*
         * And again with time range
         */
        String timeRange = "[" + calWithinLastWeek.getTime().getTime() + " TO " + System.currentTimeMillis() + "]";
        facets = prepareFacets();
        facets.add(createActiveFacet(CommonFacetType.DATE, timeRange, Filter.NO_FILTER));
        documents = query(facets);
        assertEquals("Should only find 1 mails", 1, documents.size());
        assertNotNull("Mail not found", findByProperty(documents, "id", idWithinLastWeek));
        assertNull("Wrong mail found", findByProperty(documents, "id", idBeforeLastYear));

        facets = prepareFacets();
        facets.add(createActiveFacet(CommonFacetType.DATE, CommonConstants.QUERY_LAST_MONTH, CommonConstants.FIELD_DATE, CommonConstants.QUERY_LAST_MONTH));
        documents = query(facets);
        assertEquals("Should only find 2 mails", 2, documents.size());
        assertNotNull("Mail not found", findByProperty(documents, "id", idWithinLastWeek));
        assertNotNull("Mail not found", findByProperty(documents, "id", idWithinLastMonth));
        assertNull("Wrong mail found", findByProperty(documents, "id", idBeforeLastYear));
        /*
         * And again with time range
         */
        timeRange = "[" + calWithinLastMonth.getTime().getTime() + " TO *]";
        facets = prepareFacets();
        facets.add(createActiveFacet(CommonFacetType.DATE, timeRange, Filter.NO_FILTER));
        documents = query(facets);
        assertEquals("Should only find 2 mails", 2, documents.size());
        assertNotNull("Mail not found", findByProperty(documents, "id", idWithinLastWeek));
        assertNotNull("Mail not found", findByProperty(documents, "id", idWithinLastMonth));
        assertNull("Wrong mail found", findByProperty(documents, "id", idBeforeLastYear));

        facets = prepareFacets();
        facets.add(createActiveFacet(CommonFacetType.DATE, CommonConstants.QUERY_LAST_YEAR, CommonConstants.FIELD_DATE, CommonConstants.QUERY_LAST_YEAR));
        documents = query(facets);
        assertEquals("Should only find 3 mails", 3, documents.size());
        assertNotNull("Mail not found", findByProperty(documents, "id", idWithinLastWeek));
        assertNotNull("Mail not found", findByProperty(documents, "id", idWithinLastMonth));
        assertNotNull("Mail not found", findByProperty(documents, "id", idWithinLastYear));
        assertNull("Wrong mail found", findByProperty(documents, "id", idBeforeLastYear));
        /*
         * And again with time range
         */
        timeRange = "[" + calWithinLastYear.getTime().getTime() + " TO *]";
        facets = prepareFacets();
        facets.add(createActiveFacet(CommonFacetType.DATE, timeRange, Filter.NO_FILTER));
        documents = query(facets);
        assertEquals("Should only find 3 mails", 3, documents.size());
        assertNotNull("Mail not found", findByProperty(documents, "id", idWithinLastWeek));
        assertNotNull("Mail not found", findByProperty(documents, "id", idWithinLastMonth));
        assertNotNull("Mail not found", findByProperty(documents, "id", idWithinLastYear));
        assertNull("Wrong mail found", findByProperty(documents, "id", idBeforeLastYear));

        /*
         * Find all with time range. This time we test exclusive ranges and wildcards...
         */
        timeRange = "{* TO *}";
        facets = prepareFacets();
        facets.add(createActiveFacet(CommonFacetType.DATE, timeRange, Filter.NO_FILTER));
        documents = query(facets);
        assertEquals("Should find 4 mails", 4, documents.size());
        assertNotNull("Mail not found", findByProperty(documents, "id", idWithinLastWeek));
        assertNotNull("Mail not found", findByProperty(documents, "id", idWithinLastMonth));
        assertNotNull("Mail not found", findByProperty(documents, "id", idWithinLastYear));
        assertNotNull("Mail not found", findByProperty(documents, "id", idBeforeLastYear));
    }

    public void testFilterChaining() throws Exception {
        /*
         * We're chaining some filters in subsequent query requests
         * to finally find the first row of the below table.
         * +-------------------------+
         * | Sender | Subject | Body |
         * +-------------------------+
         * |   A    |    B    |   D  |
         * |   A    |    B    |   E  |
         * |   A    |    C    |   F  |
         * |   G    |    H    |   I  |
         * +-------------------------+
         */

        String A = "some.body@find.me";
        String B = randomUID();
        String C = randomUID();
        String D = randomUID();
        String E = randomUID();
        String F = randomUID();
        String G = "another.dude@example.org";
        String H = randomUID();
        String I = randomUID();

        importMail(A, B, D);
        importMail(A, B, E);
        importMail(A, C, F);
        importMail(G, H, I);

        List<ActiveFacet> facets = prepareFacets();
        List<PropDocument> documents = query(facets);
        assertEquals("Wrong number of mails", 4, documents.size());

        facets.add(createActiveFacet(MailFacetType.CONTACTS, A, "from", A));
        documents = query(facets);
        assertEquals("Wrong number of mails", 3, documents.size());

        facets.add(createActiveFacet(MailFacetType.SUBJECT, MailFacetType.SUBJECT.getId(), "subject", B));
        documents = query(facets);
        assertEquals("Wrong number of mails", 2, documents.size());

        facets.add(createActiveFacet(MailFacetType.MAIL_TEXT, MailFacetType.MAIL_TEXT.getId(), "body", D));
        documents = query(facets);
        assertEquals("Wrong number of mails", 1, documents.size());
    }

    public void testPrefixItemIsLastInContactsFacet() throws Exception {
        FolderObject contactFolder = folderManager.generatePrivateFolder(
            "findApiMailTestFolder_" + System.currentTimeMillis(),
            FolderObject.CONTACT,
            client.getValues().getPrivateContactFolder(),
            client.getValues().getUserId());

        contactFolder = folderManager.insertFolderOnServer(contactFolder);
        List<Contact> contacts = new LinkedList<Contact>();
        contacts.add(contactManager.newAction(randomContact("Marc", contactFolder.getObjectID())));
        contacts.add(contactManager.newAction(randomContact("Marcus", contactFolder.getObjectID())));
        contacts.add(contactManager.newAction(randomContact("Martin", contactFolder.getObjectID())));
        contacts.add(contactManager.newAction(randomContact("Marek", contactFolder.getObjectID())));
        contacts.add(contactManager.newAction(randomContact("Marion", contactFolder.getObjectID())));
        String prefix = "Mar";
        List<Facet> facets = autocomplete(prefix);
        Facet facet = findByType(MailFacetType.CONTACTS, facets);
        assertNotNull("Contacts facet not found", facet);
        List<FacetValue> values = ((DefaultFacet) facet).getValues();
        int nValues = values.size();
        assertTrue("Missing contacts in facets", values.size() > 5);
        findContactsInValues(contacts, values);
        FacetValue last = values.get(values.size() - 1);
        assertEquals("Prefix item is at wrong position in result set", prefix, last.getId());

        facets = autocomplete(prefix, Collections.singletonList(new ActiveFacet(MailFacetType.CONTACTS, values.get(0).getId(), values.get(0).getOptions().get(0).getFilter())));
        facet = findByType(MailFacetType.CONTACTS, facets);
        assertNotNull("Contacts facet not found", facet);
        values = ((DefaultFacet) facet).getValues();
        assertTrue("Wrong contacts in facets", values.size() == (nValues - 1));
        last = values.get(values.size() - 1);
        assertEquals("Prefix item is at wrong position in result set", prefix, last.getId());
    }

    public void testQueryActionWithColumns() throws Exception {
        /*
         * Import test mail
         */
        String[][] mailIds = importMail(defaultAddress, "Find me", "");
        assertNotNull("mail was not imported", mailIds);

        List<ActiveFacet> facets = prepareFacets();
        facets.add(createQuery("Find me"));

        // list request columns from /ui/apps/io.ox/mail/api.js + account id
        int[] columns = new int[] {102,600,601,602,603,604,605,607,608,610,611,614,652,653};
        MailListField[] fields = MailListField.getFields(columns);
        List<PropDocument> documents = query(facets, columns);
        assertTrue("Did not find mail", documents.size() > 0);
        PropDocument document = documents.get(0);

        List<String> jsonFields = new LinkedList<String>();
        for (MailListField field : fields) {
            jsonFields.add(field.getKey());
        }

        Map<String, Object> props = document.getProps();
        for (String jsonField :jsonFields) {
            Object value = props.remove(jsonField);
            assertNotNull("Missing field " + jsonField, value);
        }

        assertTrue("Document contained more fields than requested: " + props.keySet(), props.size() == 0);
    }

    public void testQueryActionWithoutColumns() throws Exception {
        /*
         * Import test mail
         */
        String[][] mailIds = importMail(defaultAddress, "Find me", "");
        assertNotNull("mail was not imported", mailIds);

        List<ActiveFacet> facets = prepareFacets();
        facets.add(createQuery("Find me"));


        MailListField[] fields = MailField.toListFields(MailField.FIELDS_LOW_COST);
        List<PropDocument> documents = query(facets);
        assertTrue("Did not find mail", documents.size() > 0);
        PropDocument document = documents.get(0);

        List<String> jsonFields = new LinkedList<String>();
        for (MailListField field : fields) {
            jsonFields.add(field.getKey());
        }

        Map<String, Object> props = document.getProps();
        for (String jsonField :jsonFields) {
            Object value = props.remove(jsonField);
            assertNotNull("Missing field " + jsonField, value);
        }

        assertTrue("Document contained more fields than requested: " + props.keySet(), props.size() == 0);
    }


    public void testTokenizedQuery() throws Exception {
        String t1 = randomUID();
        String t2 = randomUID();
        String t3 = randomUID();
        String[][] mailIds = importMail(defaultAddress, t1 + " " + t2 + " " + t3, "");


        List<ActiveFacet> facets = prepareFacets();
        SimpleFacet globalFacet = (SimpleFacet) findByType(CommonFacetType.GLOBAL, autocomplete(Module.MAIL, t1 + " " + t3));
        facets.add(createActiveFacet(globalFacet));
        List<PropDocument> documents = query(Module.MAIL, facets);
        assertTrue("no document found", 0 < documents.size());
        assertNotNull("document not found", findByProperty(documents, "id", mailIds[0][1]));

        prepareFacets();
        globalFacet = (SimpleFacet) findByType(CommonFacetType.GLOBAL, autocomplete(Module.MAIL, "\"" + t1 + " " + t2 + "\""));
        facets.add(createActiveFacet(globalFacet));
        documents = query(Module.MAIL, facets);
        assertTrue("no document found", 0 < documents.size());
        assertNotNull("document not found", findByProperty(documents, "id", mailIds[0][1]));

        prepareFacets();
        globalFacet = (SimpleFacet) findByType(CommonFacetType.GLOBAL, autocomplete(Module.MAIL, "\"" + t1 + " " + t3 + "\""));
        facets.add(createActiveFacet(globalFacet));
        documents = query(Module.MAIL, facets);
        assertTrue("document found", 0 == documents.size());
    }

    private void findContactsInValues(List<Contact> contacts, List<FacetValue> values) {
        for (Contact contact : contacts) {
            boolean found = false;
            String contactDN = DisplayItems.convert(contact).getDisplayName();
            for (FacetValue value : values) {
                String valueDN = value.getDisplayItem().getDisplayName();
                if (contactDN.equals(valueDN)) {
                    found = true;
                    break;
                }
            }

            assertTrue("Did not find contact '" + contactDN + "'", found);
        }
    }

    private List<ActiveFacet> prepareFacets() {
        List<ActiveFacet> facets = new LinkedList<ActiveFacet>();
        facets.add(createActiveFacet(CommonFacetType.FOLDER, testFolder.getFullName(), Filter.NO_FILTER));
        return facets;
    }

    private List<Facet> autocomplete(String prefix) throws Exception {
        return autocomplete(Module.MAIL, prefix);
    }

    private List<Facet> autocomplete(String prefix, List<ActiveFacet> facets) throws Exception {
        return autocomplete(Module.MAIL, prefix, facets);
    }

    private List<PropDocument> query(List<ActiveFacet> facets, int start, int size) throws Exception {
        return query(Module.MAIL, facets, start, size);
    }

    private List<PropDocument> query(List<ActiveFacet> facets) throws Exception {
        return query(Module.MAIL, facets);
    }

    private List<PropDocument> query(List<ActiveFacet> facets, int[] columns) throws Exception {
        return query(Module.MAIL, facets, columns);
    }

    private String[][] importMails(int num, String fromHeader, String toHeader) throws OXException, IOException, JSONException {
        InputStream[] streams = new InputStream[num];
        for (int i = 0; i < num; i++) {
            String mail = MAIL
                .replaceAll("#FROM#", fromHeader)
                .replaceAll("#TO#", toHeader)
                .replaceAll("#DATE#", DateUtils.toStringRFC822(new Date(), TimeZones.UTC))
                .replaceAll("#SUBJECT#", randomUID())
                .replaceAll("#BODY#", randomUID());
            streams[i] = new ByteArrayInputStream(mail.getBytes(com.openexchange.java.Charsets.UTF_8));
        }

        ImportMailRequest request = new ImportMailRequest(testFolder.getFullName(), 0, true, true, streams);
        ImportMailResponse response = client.execute(request);
        return response.getIds();
    }

    private String[][] importMail(String fromHeader, String subject, String body) throws OXException, IOException, JSONException {
        return importMail(defaultAddress, fromHeader, subject, body, new Date());
    }

    private String[][] importMail(String toHeader, String fromHeader, String subject, String body, Date received) throws OXException, IOException, JSONException {
        String mail = MAIL
            .replaceAll("#FROM#", fromHeader)
            .replaceAll("#TO#", toHeader)
            .replaceAll("#DATE#", DateUtils.toStringRFC822(received, TimeZones.UTC))
            .replaceAll("#SUBJECT#", subject)
            .replaceAll("#BODY#", body);
        ByteArrayInputStream mailStream = new ByteArrayInputStream(mail.getBytes(com.openexchange.java.Charsets.UTF_8));
        ImportMailRequest request = new ImportMailRequest(testFolder.getFullName(), 0, true, true, new ByteArrayInputStream[] { mailStream });
        ImportMailResponse response = client.execute(request);
        return response.getIds();
    }

    private FacetValue detectContact(List<Facet> facets) throws OXException, IOException, JSONException {
        GetRequest getRequest = new GetRequest(client.getValues().getUserId(), client.getValues().getTimeZone());
        GetResponse getResponse = client.execute(getRequest);
        Contact contact = getResponse.getContact();
        FacetValue found = findByDisplayName(facets, DisplayItems.convert(contact).getDisplayName());
        return found;
    }

    protected Contact randomContact(String givenName, int folderId) {
        Contact contact = new Contact();
        contact.setParentFolderID(folderId);
        contact.setSurName(randomUID());
        contact.setGivenName(givenName);
        contact.setDisplayName(contact.getGivenName() + " " + contact.getSurName());
        contact.setEmail1(randomUID() + "@example.com");
        contact.setUid(randomUID());
        return contact;
    }

    protected Contact specificContact(String givenName, String surName, String mailAddress, int folderId) {
        Contact contact = new Contact();
        contact.setParentFolderID(folderId);
        if (surName != null) {
            contact.setSurName(surName);
        }

        if (givenName != null) {
            contact.setGivenName(givenName);
        }

        contact.setEmail1(mailAddress);
        contact.setUid(randomUID());
        return contact;
    }

    private static final String MAIL =
        "From: #FROM#\n" +
        "To: #TO#\n" +
        "CC: #TO#\n" +
        "BCC: #TO#\n" +
        "Received: from ox.open-xchange.com;#DATE#\n" +
        "Date: #DATE#\n" +
        "Subject: #SUBJECT#\n" +
        "Disposition-Notification-To: #FROM#\n" +
        "Mime-Version: 1.0\n" +
        "Content-Type: text/plain; charset=\"UTF-8\"\n" +
        "Content-Transfer-Encoding: 8bit\n" +
        "\n" +
        "Content\n" +
        "#BODY#\n";

}
