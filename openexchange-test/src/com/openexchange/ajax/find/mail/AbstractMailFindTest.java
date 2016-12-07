
package com.openexchange.ajax.find.mail;

import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import com.openexchange.ajax.find.AbstractFindTest;
import com.openexchange.ajax.find.PropDocument;
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
import com.openexchange.find.util.DisplayItems;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.util.TimeZones;
import com.openexchange.mail.utils.DateUtils;
import com.openexchange.test.ContactTestManager;

public abstract class AbstractMailFindTest extends AbstractFindTest {

    protected String defaultAddress;

    protected ContactTestManager contactManager;

    public AbstractMailFindTest() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        defaultAddress = getClient().getValues().getSendAddress();
        contactManager = new ContactTestManager(getClient());
    }

    @After
    public void tearDown() throws Exception {
        try {
            contactManager.cleanUp();
        } finally {
            super.tearDown();
        }
    }

    protected List<ActiveFacet> prepareFacets() throws OXException, IOException, JSONException {
        return prepareFacets(getClient().getValues().getInboxFolder());
    }

    protected List<ActiveFacet> prepareFacets(String folder) {
        List<ActiveFacet> facets = new LinkedList<ActiveFacet>();
        facets.add(createActiveFacet(CommonFacetType.FOLDER, folder, Filter.NO_FILTER));
        return facets;
    }

    protected List<Facet> autocomplete(String prefix) throws Exception {
        return autocomplete(Module.MAIL, prefix, prepareFacets());
    }

    protected List<Facet> autocomplete(String prefix, List<ActiveFacet> facets) throws Exception {
        return autocomplete(Module.MAIL, prefix, facets);
    }

    protected List<PropDocument> query(List<ActiveFacet> facets, int start, int size) throws Exception {
        return query(Module.MAIL, facets, start, size);
    }

    protected List<PropDocument> query(List<ActiveFacet> facets) throws Exception {
        return query(Module.MAIL, facets);
    }

    protected List<PropDocument> query(List<ActiveFacet> facets, Map<String, String> options) throws Exception {
        return query(Module.MAIL, facets, options);
    }

    protected List<PropDocument> query(List<ActiveFacet> facets, int[] columns) throws Exception {
        return query(Module.MAIL, facets, columns);
    }

    protected FacetValue detectContact(Contact contact, List<Facet> facets) throws OXException, IOException, JSONException {
        FacetValue found = findByDisplayName(facets, DisplayItems.convert(contact).getDisplayName());
        return found;
    }

    protected FacetValue detectContact(List<Facet> facets) throws OXException, IOException, JSONException {
        GetRequest getRequest = new GetRequest(getClient().getValues().getUserId(), getClient().getValues().getTimeZone());
        GetResponse getResponse = getClient().execute(getRequest);
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

    protected void findContactsInValues(List<Contact> contacts, List<FacetValue> values) {
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

    protected String[][] importMails(String folder, int num, String fromHeader, String toHeader) throws OXException, IOException, JSONException {
        InputStream[] streams = new InputStream[num];
        for (int i = 0; i < num; i++) {
            String mail = MAIL.replaceAll("#FROM#", fromHeader).replaceAll("#TO#", toHeader).replaceAll("#DATE#", DateUtils.toStringRFC822(new Date(), TimeZones.UTC)).replaceAll("#SUBJECT#", randomUID()).replaceAll("#BODY#", randomUID());
            streams[i] = new ByteArrayInputStream(mail.getBytes(com.openexchange.java.Charsets.UTF_8));
        }

        ImportMailRequest request = new ImportMailRequest(folder, 0, true, true, streams);
        ImportMailResponse response = getClient().execute(request);
        return response.getIds();
    }

    protected String[][] importMail(String folder, String fromHeader, String subject, String body) throws OXException, IOException, JSONException {
        return importMail(folder, defaultAddress, fromHeader, subject, body, new Date());
    }

    protected String[][] importMail(String folder, String toHeader, String fromHeader, String subject, String body, Date received) throws OXException, IOException, JSONException {
        String mail = MAIL.replaceAll("#FROM#", fromHeader).replaceAll("#TO#", toHeader).replaceAll("#DATE#", DateUtils.toStringRFC822(received, TimeZones.UTC)).replaceAll("#SUBJECT#", subject).replaceAll("#BODY#", body);
        ByteArrayInputStream mailStream = new ByteArrayInputStream(mail.getBytes(com.openexchange.java.Charsets.UTF_8));
        ImportMailRequest request = new ImportMailRequest(folder, 0, true, true, new ByteArrayInputStream[] { mailStream });
        ImportMailResponse response = getClient().execute(request);
        return response.getIds();
    }

    protected static final String MAIL = "From: #FROM#\n" + "To: #TO#\n" + "CC: #TO#\n" + "BCC: #TO#\n" + "Received: from ox.open-xchange.com;#DATE#\n" + "Date: #DATE#\n" + "Subject: #SUBJECT#\n" + "Disposition-Notification-To: #FROM#\n" + "Mime-Version: 1.0\n" + "Content-Type: text/plain; charset=\"UTF-8\"\n" + "Content-Transfer-Encoding: 8bit\n" + "\n" + "Content\n" + "#BODY#\n";

}
