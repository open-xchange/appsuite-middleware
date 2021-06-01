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

package com.openexchange.dav.carddav;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.DeleteMethod;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.client.methods.PutMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.PropContainer;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.w3c.dom.Node;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.configuration.ConfigurationException;
import com.openexchange.dav.Config;
import com.openexchange.dav.Headers;
import com.openexchange.dav.PropertyNames;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.WebDAVTest;
import com.openexchange.dav.carddav.reports.AddressbookMultiGetReportInfo;
import com.openexchange.dav.reports.SyncCollectionResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.java.Strings;
import com.openexchange.testing.httpclient.models.ConfigResponse;
import com.openexchange.testing.httpclient.modules.ConfigApi;
import net.sourceforge.cardme.engine.VCardEngine;
import net.sourceforge.cardme.io.CompatibilityMode;
import net.sourceforge.cardme.vcard.exceptions.VCardException;

/**
 * {@link CardDAVTest} - Common base class for CardDAV tests
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
@RunWith(Parameterized.class)
public abstract class CardDAVTest extends WebDAVTest {

    @SuppressWarnings("hiding")
    protected static final int TIMEOUT = 10000;

    private int folderId;
    private VCardEngine vCardEngine;
    private String defaultCollectionHref;

    @Parameters(name = "AuthMethod={0}")
    public static Iterable<Object[]> params() {
        return availableAuthMethods();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ConfigApi configApi = new ConfigApi(getApiClient());
        // Expect contact collect folder to be created (contactCollectEnabled == true)
        ConfigResponse configResp = configApi.getConfigNode("modules/mail/contactCollectEnabled"); // modules/mail/
        assertNull(configResp.getErrorDesc(), configResp.getError());
        Assert.assertTrue(Boolean.valueOf(configResp.getData().toString()).booleanValue());
        boolean created = false;
        long start = System.currentTimeMillis();
        // Wait until the ContactCollectorFolderCreator finished
        while (created == false && (start + TimeUnit.SECONDS.toMillis(30)) > System.currentTimeMillis()) {
            ConfigResponse resp = configApi.getConfigNode("modules/mail/contactCollectFolder");
            assertNull(resp.getErrorDesc(), resp.getError());
            Object data = resp.getData();
            if (data != null && data.equals("null") == false) {
                created = true;
                break;
            }
            Thread.sleep(1000);
        }
        if (created == false) {
            Assert.fail("Contact collect folder not created in time");
        }

        /*
         * init
         */
        this.folderId = this.getAJAXClient().getValues().getPrivateContactFolder();
        this.vCardEngine = new VCardEngine(CompatibilityMode.MAC_ADDRESS_BOOK);
    }

    @Override
    protected String getDefaultUserAgent() {
        return UserAgents.MACOS_10_7_3;
    }

    protected String getDefaultCollectionName() throws Exception {
        return getDefaultCollectionName(false);
    }

    protected String getDefaultCollectionName(boolean rediscover) throws Exception {
        if (rediscover || null == defaultCollectionHref) {
            defaultCollectionHref = discoverDefaultCollectionName();
        }
        return defaultCollectionHref;
    }

    protected String buildVCardHref(String contactUid) throws Exception {
        return buildVCardHref(getDefaultCollectionName(), contactUid);
    }

    protected String buildVCardHref(String collectionName, String contactUid) {
        return buildCollectionHref(collectionName) + contactUid + ".vcf";
    }

    protected String buildCollectionHref(String collectionName) {
        return Config.getPathPrefix() + "/carddav/" + collectionName + '/';
    }

    private static String trimSlashes(String string) {
        return Strings.trimStart(Strings.trimEnd(string, '/'), '/');
    }

    protected String discoverDefaultCollectionName() throws Exception {
        DavPropertyNameSet propertyNames = new DavPropertyNameSet();
        propertyNames.add(PropertyNames.DISPLAYNAME);
        propertyNames.add(PropertyNames.RESOURCETYPE);
        PropFindMethod propFind = new PropFindMethod(getBaseUri() + Config.getPathPrefix() + "/carddav/", propertyNames, DavConstants.DEPTH_1);
        MultiStatusResponse[] responses = getWebDAVClient().doPropFind(propFind, StatusCodes.SC_MULTISTATUS);
        for (MultiStatusResponse response : responses) {
            for (Node node : extractNodeListValue(PropertyNames.RESOURCETYPE, response)) {
                if (PropertyNames.NS_CARDDAV.getURI().equals(node.getNamespaceURI()) && "addressbook".equals(node.getLocalName())) {
                    String href = response.getHref();
                    assertNotNull("got no href from response", href);
                    int idx = href.indexOf("/carddav/");
                    if (-1 == idx) {
                        fail("no /carddav/ in href");
                    }
                    return trimSlashes(href.substring(idx + 9));
                }
            }
        }
        return null;
    }

    /**
     * Remembers the supplied contact for deletion after the test is finished in the <code>tearDown()</code> method.
     *
     * @param contact
     */
    protected void rememberForCleanUp(final Contact contact) {
        cotm.getCreatedEntities().add(contact);
    }

    /**
     * Gets the personal contacts folder id
     *
     * @return
     */
    protected int getDefaultFolderID() {
        return this.folderId;
    }

    /**
     * Gets the folder id of the global address book
     *
     * @return
     */
    protected int getGABFolderID() {
        return 6;
    }

    protected VCardEngine getVCardEngine() {
        return this.vCardEngine;
    }

    protected int delete(String contactUid) throws Exception {
        return delete(getDefaultCollectionName(), contactUid);
    }

    protected int delete(String collectionName, String contactUid) throws Exception {
        DeleteMethod delete = null;
        try {
            delete = new DeleteMethod(getBaseUri() + buildVCardHref(collectionName, contactUid));
            return getWebDAVClient().executeMethod(delete);
        } finally {
            release(delete);
        }
    }

    protected void delete(Contact contact) {
        cotm.deleteAction(contact);
    }

    protected String getCTag() throws Exception {
        PropFindMethod propFind = null;
        try {
            DavPropertyNameSet props = new DavPropertyNameSet();
            props.add(PropertyNames.GETCTAG);
            propFind = new PropFindMethod(getBaseUri() + buildCollectionHref(getDefaultCollectionName()), DavConstants.PROPFIND_BY_PROPERTY, props, DavConstants.DEPTH_0);
            MultiStatusResponse response = assertSingleResponse(getWebDAVClient().doPropFind(propFind, StatusCodes.SC_MULTISTATUS));
            return this.extractTextContent(PropertyNames.GETCTAG, response);
        } finally {
            release(propFind);
        }
    }

    public int putVCard(String uid, String vCard) throws Exception {
        return putVCard(uid, vCard, getDefaultCollectionName());
    }

    protected int putVCard(String uid, String vCard, String collection) throws Exception {
        PutMethod put = null;
        try {
            final String href = buildVCardHref(collection, uid);
            put = new PutMethod(getBaseUri() + href);
            put.addRequestHeader(Headers.IF_NONE_MATCH, "*");
            put.setRequestEntity(new StringRequestEntity(vCard, "text/vcard", "UTF-8"));
            return getWebDAVClient().executeMethod(put);
        } finally {
            release(put);
        }
    }

    protected String postVCard(String vCard, float maxSimilarity) throws Exception {
        return postVCard(vCard, getDefaultCollectionName(), maxSimilarity);
    }

    protected String postVCard(String vCard, String collection, float maxSimilarity) throws Exception {
        PostMethod post = null;
        try {
            final String href = Config.getPathPrefix() + "/carddav/" + collection;
            post = new PostMethod(getBaseUri() + href);
            if (maxSimilarity > 0) {
                post.addRequestHeader(Headers.MAX_SIMILARITY, String.valueOf(maxSimilarity));
            }
            post.setRequestEntity(new StringRequestEntity(vCard, "text/vcard", "UTF-8"));
            return getWebDAVClient().doPost(post, 207);
        } finally {
            release(post);
        }
    }

    protected int putVCardUpdate(String uid, String vCard) throws Exception {
        return this.putVCardUpdate(uid, vCard, null);
    }

    protected int putVCardUpdate(String uid, String vCard, String ifMatchEtag) throws Exception {
        return putVCardUpdate(uid, vCard, getDefaultCollectionName(), ifMatchEtag);
    }

    protected int putVCardUpdate(String uid, String vCard, String collection, String ifMatchEtag) throws Exception {
        PutMethod put = null;
        try {
            final String href = Config.getPathPrefix() + "/carddav/" + collection + "/" + uid + ".vcf";
            put = new PutMethod(getBaseUri() + href);
            if (null != ifMatchEtag) {
                put.addRequestHeader(Headers.IF_MATCH, ifMatchEtag);
            }
            put.setRequestEntity(new StringRequestEntity(vCard, "text/vcard", "UTF-8"));
            return getWebDAVClient().executeMethod(put);
        } finally {
            release(put);
        }
    }

    protected String fetchSyncToken() throws Exception {
        return fetchSyncToken(getDefaultCollectionName());
    }

    @Override
    protected String fetchSyncToken(String collection) throws Exception {
        return super.fetchSyncToken(Config.getPathPrefix() + "/carddav/" + collection);
    }

    /**
     * Performs a REPORT method at /carddav/Contacts/ with a Depth of 1, requesting the
     * ETag property of all resources that were changed since the supplied sync token.
     *
     * @param syncToken
     * @return
     * @throws IOException
     * @throws ConfigurationException
     * @throws DavException
     */
    protected Map<String, String> syncCollection(final String syncToken) throws Exception {
        return syncCollection(getDefaultCollectionName(), syncToken);
    }

    @Override
    protected Map<String, String> syncCollection(String collection, final String syncToken) throws Exception {
        return super.syncCollection(syncToken, "/carddav/" + collection);
    }

    protected SyncCollectionResponse syncCollection(SyncToken syncToken) throws Exception {
        return syncCollection(getDefaultCollectionName(), syncToken);
    }

    protected SyncCollectionResponse syncCollection(String collection, SyncToken syncToken) throws Exception {
        return super.syncCollection(syncToken, "/carddav/" + collection);
    }

    /**
     * Gets all changed vCards by performing a sync-collection REPORT with the
     * supplied sync-token, followed by an addressbook-multiget REPORT for
     * all of reported resources.
     *
     * @param syncToken
     * @return
     * @throws ConfigurationException
     * @throws IOException
     * @throws DavException
     * @throws VCardException
     */
    protected List<VCardResource> getVCardsSince(final String syncToken) throws Exception {
        final Map<String, String> eTags = this.syncCollection(syncToken);
        return this.addressbookMultiget(eTags.keySet());
    }

    /**
     * Gets all available vCards by executing a REPORT method at /carddav/Contacts/
     * with a depth of 1, followed by an addressbook-multiget REPORT for all of the
     * available resources.
     *
     * @return
     * @throws DavException
     * @throws IOException
     * @throws ConfigurationException
     * @throws VCardException
     */
    protected List<VCardResource> getAllVCards() throws Exception {
        final Map<String, String> eTags = this.getAllETags();
        return this.addressbookMultiget(eTags.keySet());
    }

    protected VCardResource getGlobalAddressbookVCard() throws Exception {
        GetResponse response = getClient().execute(new com.openexchange.ajax.folder.actions.GetRequest(EnumAPI.OX_NEW, getGABFolderID()));
        String gabFolderName = response.getFolder().getFolderName();
        return getGroupVCard(gabFolderName);
    }

    protected VCardResource getGroupVCard(String folderName) throws Exception {
        List<VCardResource> groupVCards = this.getAllGroupVCards();
        for (VCardResource resource : groupVCards) {
            if (folderName.equals(resource.getVCard().getFN().getFormattedName())) {
                return resource;
            }
        }
        fail("no vCard representing the folder '" + folderName + "' found");
        return null;
    }

    protected List<VCardResource> getAllGroupVCards() throws Exception {
        final Map<String, String> eTags = this.getAllETags();
        final List<VCardResource> vCards = this.addressbookMultiget(eTags.keySet());
        final List<VCardResource> groupVCards = new ArrayList<VCardResource>();
        for (final VCardResource resource : vCards) {
            if (resource.isGroup()) {
                groupVCards.add(resource);
            }
        }
        return groupVCards;
    }

    /**
     * Performs a REPORT method at /carddav/Contacts/ with a Depth of 1, requesting the
     * ETag property of all resources. This models a "poor man's sync-collection" as
     * executed by the addressbook client of Mac OS 10.6.
     *
     * @return
     * @throws DavException
     * @throws IOException
     * @throws ConfigurationException
     */
    protected Map<String, String> getAllETags() throws Exception {
        final Map<String, String> eTags = new HashMap<String, String>();
        final DavPropertyNameSet propertyNames = new DavPropertyNameSet();
        propertyNames.add(PropertyNames.GETETAG);
        final PropFindMethod propFind = new PropFindMethod(getBaseUri() + buildCollectionHref(getDefaultCollectionName()), propertyNames, DavConstants.DEPTH_1);
        final MultiStatusResponse[] responses = this.getWebDAVClient().doPropFind(propFind, StatusCodes.SC_MULTISTATUS);
        for (final MultiStatusResponse response : responses) {
            if (response.getProperties(StatusCodes.SC_OK).contains(PropertyNames.GETETAG)) {
                final String href = response.getHref();
                assertNotNull("got no href from response", href);
                final String eTag = this.extractTextContent(PropertyNames.GETETAG, response);
                assertNotNull("got no ETag from response", eTag);
                eTags.put(href, eTag);
            }
        }
        return eTags;
    }

    /**
     * Performs a REPORT method at /carddav/Contacts/, requesting the address data and ETags of all elements identified by the
     * supplied hrefs.
     *
     * @param hrefs The hrefs to request
     * @return The vCard resources
     */
    protected List<VCardResource> addressbookMultiget(final Collection<String> hrefs) throws Exception {
        return addressbookMultiget(getDefaultCollectionName(), hrefs);
    }

    /**
     * Performs a REPORT method in a specific collection, requesting the address data and ETags of all elements identified by the
     * supplied hrefs.
     *
     * @param collection The collection to perform the report action in
     * @param hrefs The hrefs to request
     * @return The vCard resources
     */
    protected List<VCardResource> addressbookMultiget(String collection, Collection<String> hrefs) throws Exception {
        DavPropertyNameSet props = new DavPropertyNameSet();
        props.add(PropertyNames.GETETAG);
        props.add(PropertyNames.ADDRESS_DATA);
        return addressbookMultiget(collection, hrefs, props);
    }

    /**
     * Performs a REPORT method in a specific collection, requesting the address data and ETags of all elements identified by the
     * supplied hrefs.
     *
     * @param collection The collection to perform the report action in
     * @param hrefs The hrefs to request
     * @param props The properties to request
     * @return The vCard resources
     */
    protected List<VCardResource> addressbookMultiget(String collection, Collection<String> hrefs, PropContainer props) throws Exception {
        List<VCardResource> addressData = new ArrayList<VCardResource>();
        ReportInfo reportInfo = new AddressbookMultiGetReportInfo(hrefs.toArray(new String[hrefs.size()]), props);
        MultiStatusResponse[] responses = this.getWebDAVClient().doReport(reportInfo, getBaseUri() + buildCollectionHref(collection));
        for (MultiStatusResponse response : responses) {
            if (response.getProperties(StatusCodes.SC_OK).contains(PropertyNames.GETETAG)) {
                String href = response.getHref();
                assertNotNull("got no href from response", href);
                String data = this.extractTextContent(PropertyNames.ADDRESS_DATA, response);
                assertNotNull("got no address data from response", data);
                String eTag = this.extractTextContent(PropertyNames.GETETAG, response);
                assertNotNull("got no etag data from response", eTag);
                addressData.add(new VCardResource(data, href, eTag));
            }
        }
        return addressData;
    }

    protected VCardResource getVCard(final String uid) throws Exception {
        final String href = buildVCardHref(uid);
        final List<VCardResource> vCards = this.addressbookMultiget(Arrays.asList(href));
        assertNotNull("no vCards found", vCards);
        assertEquals("zero or more than one vCards found", 1, vCards.size());
        final VCardResource vCard = vCards.get(0);
        assertNotNull("no vCard data found", vCard);
        return vCard;
    }

    protected VCardResource getVCard(final String uid, String collection) throws Exception {
        String href = buildVCardHref(collection, uid);
        return getVCardResource(href);
    }

    protected VCardResource getVCardResource(String href) throws Exception {
        GetMethod get = new GetMethod(getWebDAVClient().getBaseURI() + href);
        String vCard = getWebDAVClient().doGet(get);
        assertNotNull(vCard);
        Header eTagHeader = get.getResponseHeader("ETag");
        String eTag = null != eTagHeader ? eTagHeader.getValue() : null;
        return new VCardResource(vCard, href, eTag);
    }

    private static JSONObject getSearchFilter(String uid) throws JSONException {
        return new JSONObject("{'filter' : [ '=' , {'field' : 'uid'} , '" + uid + "']}");
    }

    protected Contact searchContact(String uid, int[] folderIDs, int[] columnIDs) throws JSONException {
        List<String> folders = null;
        if (null != folderIDs) {
            folders = new ArrayList<String>(folderIDs.length);
            for (int folderID : folderIDs) {
                folders.add(Integer.toString(folderID));
            }
        }
        Contact[] contacts = cotm.searchAction(getSearchFilter(uid), folders, null == columnIDs ? Contact.ALL_COLUMNS : columnIDs, -1, null);
        return null != contacts && 0 < contacts.length ? contacts[0] : null;
    }

    protected Contact getContact(String uid) throws JSONException {
        return getContact(uid, null);
    }

    protected List<Contact> getContacts(int folderID) {
        Contact[] contacts = cotm.allAction(folderID);
        return Arrays.asList(contacts);
    }

    protected Contact getContact(String uid, int folderID) throws JSONException {
        return getContact(uid, new int[] { folderID });
    }

    protected Contact getContact(String uid, int[] folderIDs) throws JSONException {
        return this.searchContact(uid, folderIDs, null);
    }

    protected Contact waitForContact(String uid) throws InterruptedException, JSONException {
        return waitForContact(uid, null);
    }

    protected Contact waitForContact(String uid, int folderID) throws InterruptedException, JSONException {
        return waitForContact(uid, new int[] { folderID });
    }

    protected Contact waitForContact(String uid, int[] folderIDs) throws InterruptedException, JSONException {
        long timeoutTime = new Date().getTime() + TIMEOUT;
        do {
            Contact contact = this.getContact(uid, folderIDs);
            if (null != contact) {
                return contact;
            }
            Thread.sleep(TIMEOUT / 20);
        } while (new Date().getTime() < timeoutTime);
        return null;
    }

    protected Contact[] findContacts(final String pattern) {
        return cotm.searchAction(pattern, this.getDefaultFolderID());
    }

    /**
     * Searches for a contact with the supplied pattern in the default contact folder on the server,
     * asserting that exactly one contact is found during the search.
     *
     * @param pattern
     * @return
     */
    protected Contact findContact(final String pattern) {
        final Contact[] contacts = this.findContacts(pattern);
        assertNotNull("no contacts found", contacts);
        assertEquals("zero or more than one contact found", 1, contacts.length);
        return contacts[0];
    }

    /**
     * Creates the given contact in the default folder.
     *
     * @param contact
     * @return
     */
    protected Contact create(final Contact contact) {
        return this.create(contact, this.getDefaultFolderID());
    }

    protected Contact update(int originalFolderID, Contact contact) {
        return cotm.updateAction(originalFolderID, contact);
    }

    protected Contact update(Contact contact) {
        return cotm.updateAction(contact);
    }

    /**
     * Creates a contact in the given folder.
     *
     * @param contact
     * @return
     */
    protected Contact create(Contact contact, int folderID) {
        contact.setParentFolderID(folderID);
        return cotm.newAction(contact);
    }

    @Override
    protected FolderObject updateFolder(FolderObject folder) throws OXException, IOException, JSONException {
        InsertResponse response = getClient().execute(new com.openexchange.ajax.folder.actions.UpdateRequest(EnumAPI.OX_NEW, folder));
        folder.setLastModified(response.getTimestamp());
        return folder;
    }

    protected FolderObject getDefaultFolder() {
        return getFolder(getDefaultFolderID());
    }

    protected FolderObject getGABFolder() {
        return getFolder(getGABFolderID());
    }

    protected FolderObject createFolder(String folderName) {
        return super.createFolder(this.getDefaultFolder(), folderName);
    }

    protected static String formatAsUTC(final Date date) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmm'00Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(date);
    }

    protected static DistributionListEntryObject asDistListMember(Contact contact) throws OXException {
        DistributionListEntryObject member = new DistributionListEntryObject();
        member.setFolderID(contact.getParentFolderID());
        member.setEntryID(contact.getObjectID());
        member.setDisplayname(contact.getDisplayName());
        member.setFirstname(contact.getGivenName());
        member.setLastname(contact.getSurName());
        member.setEmailfield(DistributionListEntryObject.EMAILFIELD1);
        if (contact.containsEmail1()) {
            member.setEmailaddress(contact.getEmail1());
        } else if (contact.containsEmail2()) {
            member.setEmailaddress(contact.getEmail2());
            member.setEmailfield(DistributionListEntryObject.EMAILFIELD2);
        } else if (contact.containsEmail3()) {
            member.setEmailaddress(contact.getEmail3());
            member.setEmailfield(DistributionListEntryObject.EMAILFIELD3);
        }
        return member;
    }

    /*
     * Additional assertXXX methods
     */

    public static VCardResource assertContains(final String uid, final Collection<VCardResource> vCards) {
        VCardResource match = null;
        for (final VCardResource vCard : vCards) {
            if (uid.equals(vCard.getUID())) {
                assertNull("duplicate match for UID '" + uid + "'", match);
                match = vCard;
            }
        }
        assertNotNull("no vCard with UID '" + uid + "' found", match);
        return match;
    }

    public static VCardResource assertContainsFN(String formattedName, Collection<VCardResource> vCards) {
        VCardResource match = null;
        for (VCardResource vCard : vCards) {
            if (formattedName.equals(vCard.getFN())) {
                match = vCard;
                break;
            }
        }
        assertNotNull("no vCard with FN '" + formattedName + "' found", match);
        return match;
    }

    public static String assertContainsMemberUID(String uid, VCardResource groupVCard) {
        List<String> members = groupVCard.getMemberUIDs();
        assertNotNull("no members found in group vcard", members);
        String match = null;
        for (String memberUid : members) {
            if (uid.equals(memberUid)) {
                match = memberUid;
                break;
            }
        }
        assertNotNull("no group member with UID '" + uid + "' found", match);
        return match;
    }

    public static void assertNotContainsMemberUID(String uid, VCardResource groupVCard) {
        List<String> members = groupVCard.getMemberUIDs();
        String match = null;
        for (String memberUid : members) {
            if (uid.equals(memberUid)) {
                match = memberUid;
                break;
            }
        }
        assertNull("group member with UID '" + uid + "' found", match);
    }

    public static void assertNotContainsFN(String formattedName, Collection<VCardResource> vCards) {
        if (null != vCards && 0 < vCards.size()) {
            for (VCardResource vCard : vCards) {
                assertFalse("vCard with FN '" + formattedName + "' found", formattedName.equals(vCard.getFN()));
            }
        }
    }

    public static void assertNotContains(final String uid, final Collection<VCardResource> vCards) {
        if (null != vCards && 0 < vCards.size()) {
            for (final VCardResource vCard : vCards) {
                assertFalse("vCard with UID '" + uid + "' found", uid.equals(vCard.getUID()));
            }
        }
    }

}
