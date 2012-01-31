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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.carddav;

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
import java.util.UUID;

import net.sourceforge.cardme.engine.VCardEngine;
import net.sourceforge.cardme.io.CompatibilityMode;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HeaderElement;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.DeleteMethod;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.client.methods.PutMethod;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.w3c.dom.Node;

import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.carddav.reports.AddressbookMultiGetReportInfo;
import com.openexchange.carddav.reports.SyncCollectionReportInfo;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.ConfigurationException;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.configuration.AJAXConfig.Property;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.test.ContactTestManager;

/**
 * {@link CardDAVTest}
 * 
 * Common base class for CardDAV tests
 * 
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class CardDAVTest extends AbstractAJAXSession {
	
	private CardDAVClient cardDAVClient = null;
	private ContactTestManager testManager = null;
	private int folderId;
	private VCardEngine vCardEngine;
	
	public CardDAVTest(final String name) {
		super(name);
	}
	
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        /*
         * init
         */
        this.testManager = new ContactTestManager(this.getAJAXClient());
    	this.getManager().setFailOnError(true);
        this.folderId = this.getAJAXClient().getValues().getPrivateContactFolder();
        this.vCardEngine = new VCardEngine(CompatibilityMode.MAC_ADDRESS_BOOK);
        /*
         * setup webdav client
         */
        this.cardDAVClient = new CardDAVClient();
    }
    
    /**
     * Remembers the supplied contact for deletion after the test is finished in the <code>tearDown()</code> method.  
     * @param contact
     */
    protected void rememberForCleanUp(final Contact contact) {
    	this.getManager().getCreatedEntities().add(contact);
    }
    
    /**
     * Gets the personal contacts folder id
     * @return
     */
    protected int getFolderId() {
    	return this.folderId;
    }

    /**
     * Gets the folder id of the global address book
     * @return
     */
    protected int getGABFolderId() {
    	return 6;
    }

    /**
     * Gets the underlying {@link ContactTestManager} instance.
     * @return
     */
    protected ContactTestManager getManager() {
    	return this.testManager;
    }
    
    protected VCardEngine getVCardEngine() {
    	return this.vCardEngine;
    }
    
    protected int delete(final String uid) throws OXException, HttpException, IOException {
    	DeleteMethod delete = null;    	
        try {
            final String href = "/carddav/Contacts/" + uid + ".vcf";
            delete = new DeleteMethod(getBaseUri() + href);
            return this.getCardDAVClient().executeMethod(delete);
        } finally {
            release(delete);
        }
    }
    
    protected String getCTag() throws OXException, IOException, DavException {
		PropFindMethod propFind = null;
		try {
			final DavPropertyNameSet props = new DavPropertyNameSet();
	        props.add(PropertyNames.GETCTAG);
	        propFind = new PropFindMethod(getBaseUri() + "/carddav/Contacts/", 
	        		DavConstants.PROPFIND_BY_PROPERTY, props, DavConstants.DEPTH_0);
	        final MultiStatusResponse response = assertSingleResponse(
	        		this.getCardDAVClient().doPropFind(propFind, StatusCodes.SC_MULTISTATUS));
	        return this.extractTextContent(PropertyNames.GETCTAG, response);
		} finally {
			release(propFind);
		}
    }
    
	protected int putVCard(final String uid, final String vCard) throws HttpException, IOException, OXException {
        PutMethod put = null;
        try {
            final String href = "/carddav/Contacts/" + uid + ".vcf";
            put = new PutMethod(getBaseUri() + href);
            put.addRequestHeader(Headers.IF_NONE_MATCH, "*");
            put.setRequestEntity(new StringRequestEntity(vCard, "text/vcard", "UTF-8"));
            return this.getCardDAVClient().executeMethod(put);
        } finally {
            release(put);
        }
	}
	
	protected int putVCardUpdate(final String uid, final String vCard) throws HttpException, IOException, OXException {
		return this.putVCardUpdate(uid, vCard, null);
	}
	
	protected int putVCardUpdate(final String uid, final String vCard, final String ifMatchEtag) throws HttpException, IOException, OXException {
        PutMethod put = null;
        try {
            final String href = "/carddav/Contacts/" + uid + ".vcf";
            put = new PutMethod(getBaseUri() + href);
            if (null != ifMatchEtag) {
                put.addRequestHeader(Headers.IF_MATCH, ifMatchEtag);
            }
            put.setRequestEntity(new StringRequestEntity(vCard, "text/vcard", "UTF-8"));
            return this.getCardDAVClient().executeMethod(put);
        } finally {
            release(put);
        }
	}
	
	protected String fetchSyncToken() throws OXException, IOException, DavException {
		PropFindMethod propFind = null;
		try {
			final DavPropertyNameSet props = new DavPropertyNameSet();
	        props.add(PropertyNames.SYNC_TOKEN);
	        propFind = new PropFindMethod(getBaseUri() + "/carddav/Contacts", 
	        		DavConstants.PROPFIND_BY_PROPERTY, props, DavConstants.DEPTH_0);
	        final MultiStatusResponse response = assertSingleResponse(
	        		this.getCardDAVClient().doPropFind(propFind, StatusCodes.SC_MULTISTATUS));
	        return this.extractTextContent(PropertyNames.SYNC_TOKEN, response);
		} finally {
			release(propFind);
		}
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
	protected Map<String, String> syncCollection(final String syncToken) throws OXException, IOException, DavException {
		final Map<String, String> eTags = new HashMap<String, String>();
    	final DavPropertyNameSet props = new DavPropertyNameSet();
    	props.add(PropertyNames.GETETAG);
    	final ReportInfo reportInfo = new SyncCollectionReportInfo(syncToken, props);
    	final MultiStatusResponse[] responses = this.getCardDAVClient().doReport(reportInfo, getBaseUri() + "/carddav/Contacts/");
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
	protected List<VCardResource> getVCardsSince(final String syncToken) throws OXException, IOException, DavException {
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
	protected List<VCardResource> getAllVCards() throws OXException, IOException, DavException {
		final Map<String, String> eTags = this.getAllETags();
		return this.addressbookMultiget(eTags.keySet());		
	}
	
	protected VCardResource getGlobalAddressbookVCard() throws OXException, IOException, DavException {
		final List<VCardResource> groupVCards = this.getAllGroupVCards();
		for (final VCardResource resource : groupVCards) {
			// the server is a grey box here, so we know how the global addressbook resource looks like
			if (resource.getHref().endsWith("_" + this.getGABFolderId() + ".vcf")) { 
				return resource;				
			}
		}
		fail("no vCard representing the global addressbook found");
		return null;
	}

	protected List<VCardResource> getAllGroupVCards() throws OXException, IOException, DavException {
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
	protected Map<String, String> getAllETags() throws OXException, IOException, DavException {
		final Map<String, String> eTags = new HashMap<String, String>();
    	final DavPropertyNameSet propertyNames = new DavPropertyNameSet();
    	propertyNames.add(PropertyNames.GETETAG);
		final PropFindMethod propFind = new PropFindMethod(getBaseUri() + "/carddav/Contacts", propertyNames, DavConstants.DEPTH_1);
    	final MultiStatusResponse[] responses = this.getCardDAVClient().doPropFind(propFind, StatusCodes.SC_MULTISTATUS);
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
	
	protected boolean removeFromETags(final Map<String, String> eTags, final String uid) {
		final String href = this.getHrefFromETags(eTags, uid);
		if (null != href) {
			eTags.remove(href);
			return true;
		} else {
			return false;
		}	}
	
	protected String getHrefFromETags(final Map<String, String> eTags, final String uid) {
		for (final String href : eTags.keySet()) {
			if (href.contains(uid)) {
				return href;
			}
		}
		return null; 
	}
	
	protected List<String> getChangedHrefs(final Map<String, String> previousETags, final Map<String, String> newETags) {
		final List<String> hrefs = new ArrayList<String>();
		for (final String href : newETags.keySet()) {
			if (false == previousETags.containsKey(href) || false == newETags.get(href).equals(newETags.get(href))) {
				hrefs.add(href);
			} 
		}
		return hrefs;
	}
	
	/**
	 * Performs a REPORT method at /carddav/Contacts/, requesting the address 
	 * data and ETags of all elements identified by the supplied hrefs. 
	 * @param hrefs
	 * @return
	 * @throws ConfigurationException
	 * @throws IOException
	 * @throws DavException
	 * @throws VCardException 
	 */
	protected List<VCardResource> addressbookMultiget(final Collection<String> hrefs) throws OXException, IOException, DavException {
		final List<VCardResource> addressData = new ArrayList<VCardResource>();		
    	final DavPropertyNameSet props = new DavPropertyNameSet();
    	props.add(PropertyNames.GETETAG);
    	props.add(PropertyNames.ADDRESS_DATA);
    	final ReportInfo reportInfo = new AddressbookMultiGetReportInfo(hrefs.toArray(new String[hrefs.size()]), props);
    	final MultiStatusResponse[] responses = this.getCardDAVClient().doReport(reportInfo, getBaseUri() + "/carddav/Contacts/");
        for (final MultiStatusResponse response : responses) {
        	final String href = response.getHref();
        	assertNotNull("got no href from response", href);
        	final String data = this.extractTextContent(PropertyNames.ADDRESS_DATA, response);
        	assertNotNull("got no address data from response", data);
        	final String eTag = this.extractTextContent(PropertyNames.GETETAG, response);
        	assertNotNull("got no etag data from response", eTag);
        	addressData.add(new VCardResource(data, href, eTag));
		}
		return addressData;
	}
	
	protected VCardResource getVCard(final String uid) throws OXException, IOException, DavException {
		final String href = "/carddav/Contacts/" + uid + ".vcf"; 
		final List<VCardResource> vCards = this.addressbookMultiget(Arrays.asList(href));
		assertNotNull("no vCards found", vCards);
    	assertEquals("zero or more than one vCards found", 1, vCards.size());
    	final VCardResource vCard = vCards.get(0);
    	assertNotNull("no vCard data found", vCard);
    	return vCard;		
	}
	
	protected Contact getContact(final String uid) {
		final Contact contact = this.getContact(uid, getFolderId());
		return null != contact ? contact : this.getContact(uid, getGABFolderId());
	}
	
	protected Contact getContact(final String uid, final int folderId) {
		final Contact[] contacts = this.getManager().allAction(folderId, new int[] { Contact.OBJECT_ID, Contact.FOLDER_ID, Contact.UID });
		for (final Contact contact : contacts) {
			if (uid.equals(contact.getUid())) {
				return this.getManager().getAction(contact);
			}
		}
		return null;
	}
    
    protected Contact[] findContacts(final String pattern) {
    	return this.getManager().searchAction(pattern, this.getFolderId());
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
     * @param contact
     * @return
     */
    protected Contact create(final Contact contact) {
    	contact.setParentFolderID(this.getFolderId());
    	return this.getManager().newAction(contact);
    }
    
    protected static String getBaseUri() throws OXException {
        return getProtocol() + "://" + getHostname();
    }
    
    protected static User getUser() {
    	return User.User1;
    }
    
    protected static String getLogin() throws OXException {
    	return getLogin(getUser());
    }
    
    protected static String getusername() throws OXException {
    	return getUsername(getUser());
    }
    
    protected static String getPassword() throws OXException {
    	return getPassword(getUser());
    }
    
    protected static String getHostname() throws OXException {
        final String hostname = AJAXConfig.getProperty(Property.HOSTNAME);
        if (null == hostname) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(Property.HOSTNAME.getPropertyName());
        }
        return hostname;
    }
    
    protected static String getProtocol() throws OXException {
        final String hostname = AJAXConfig.getProperty(Property.PROTOCOL);
        if (null == hostname) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(Property.PROTOCOL.getPropertyName());
        }
        return hostname;
    }
    
    protected static String getLogin(final User user) throws OXException {
        final String login = AJAXConfig.getProperty(user.getLogin());
        if (null == login) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(user.getLogin().getPropertyName());
        } else if (login.contains("@")) {
        	return login;
        } else {
            final String context = AJAXConfig.getProperty(Property.CONTEXTNAME);
            if (null == context) {
                throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(Property.CONTEXTNAME.getPropertyName());
            }
            return login + "@" + context;
        }
    }
    
    protected static String getUsername(final User user) throws OXException {
        final String username = AJAXConfig.getProperty(user.getLogin());
        if (null == username) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(user.getLogin().getPropertyName());
        } else {
        	return username.contains("@") ? username.substring(0, username.indexOf("@")) : username;
        }
    }
    
    protected static String getPassword(final User user) throws OXException {
        final String password = AJAXConfig.getProperty(user.getPassword());
        if (null == password) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(user.getPassword().getPropertyName());
        }
        return password;
    }    
    
	protected static void release(final HttpMethodBase method) {
		if (null != method) {
			method.releaseConnection();
		}
	}
	
	protected static String randomUID() {
		return UUID.randomUUID().toString();
	}

    @Override
    protected void tearDown() throws Exception {
    	if (null != this.getManager()) {
    		this.getManager().cleanUp();
    	}
        super.tearDown();
    }
    
    protected CardDAVClient getCardDAVClient() {
    	return this.cardDAVClient;
    }
    
    protected AJAXClient getAJAXClient() {
    	return super.getClient();
    }
    
    protected String extractHref(final DavPropertyName propertyName, final MultiStatusResponse response) {
    	final Node node = this.extractNode(propertyName, response);
    	assertMatches(PropertyNames.HREF, node);
    	final String content = node.getTextContent();
    	assertNotNull("no text content in " + PropertyNames.HREF + " child for " + propertyName, content);
    	return content;
    }
    
    protected Node extractNode(final DavPropertyName propertyName, final MultiStatusResponse response) {
    	assertNotEmpty(propertyName, response);
    	final Object value = response.getProperties(StatusCodes.SC_OK).get(propertyName).getValue();
    	assertTrue("value is not a node in " + propertyName, value instanceof Node);
    	return (Node)value;
    }
    
    protected String extractTextContent(final DavPropertyName propertyName, final MultiStatusResponse response) {
    	assertNotEmpty(propertyName, response);
    	final Object value = response.getProperties(StatusCodes.SC_OK).get(propertyName).getValue();
    	assertTrue("value is not a string in " + propertyName, value instanceof String);
    	return (String)value;
    }
    
    protected static String formatAsUTC(final Date date) {
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmm'00Z'");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		return dateFormat.format(date);
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
    
    public static void assertNotContains(final String uid, final Collection<VCardResource> vCards) {
    	if (null != vCards && 0 < vCards.size()) {
	    	for (final VCardResource vCard : vCards) {
	    		assertFalse("vCard with UID '" + uid + "' found", uid.equals(vCard.getUID()));
	    	}
    	}
    }
    
	public static void assertMatches(final DavPropertyName propertyName, final Node node) {
    	assertEquals("wrong element name", propertyName.getName(), node.getLocalName());
    	assertEquals("wrong element namespace", propertyName.getNamespace().getURI(), node.getNamespaceURI());
	}
	
	public static void assertIsPresent(final DavPropertyName propertyName, final MultiStatusResponse response) {
    	final DavProperty<?> property = response.getProperties(StatusCodes.SC_OK).get(propertyName);
    	assertNotNull(propertyName + " not found", property);
	}
	
	public static void assertNotEmpty(final DavPropertyName propertyName, final MultiStatusResponse response) {
		assertIsPresent(propertyName, response);
    	final Object value = response.getProperties(StatusCodes.SC_OK).get(propertyName).getValue();
    	assertNotNull("no value for " + propertyName, value);
	}
	
	public static MultiStatusResponse assertSingleResponse(final MultiStatusResponse[] responses) {
        assertNotNull("got no multistatus responses", responses);
        assertTrue("got zero multistatus responses", 0 < responses.length);
        assertTrue("got more than one multistatus responses", 1 == responses.length);
        final MultiStatusResponse response = responses[0];
        assertNotNull("no multistatus response", response);
        return response;
	}
	
	public static void assertResponseHeaders(final String[] expected, final String headerName, final HttpMethod method) {
		for (final String expectedHeader : expected) {
			boolean found = false;
			final Header[] actualHeaders = method.getResponseHeaders(headerName);
			assertTrue("header '" + headerName + "' not found", null != actualHeaders && 0 < actualHeaders.length);
			for (final Header actualHeader : actualHeaders) {
				final HeaderElement[] actualHeaderElements = actualHeader.getElements();
				assertTrue("no elements found in header '" + headerName + "'", null != actualHeaderElements && 0 < actualHeaderElements.length);
				for (final HeaderElement actualHeaderElement : actualHeaderElements) {
					if (expectedHeader.equals(actualHeaderElement.getName())) {
						found = true;
						break;
					}
				}
				if (found) {
					break;
				}
			}
			assertTrue("header element '" + expectedHeader + "'not found in header '" + headerName + "'", found);
		}
	}
}
