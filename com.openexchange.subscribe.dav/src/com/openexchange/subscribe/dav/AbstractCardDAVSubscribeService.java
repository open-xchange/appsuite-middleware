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

package com.openexchange.subscribe.dav;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.google.common.collect.Lists;
import com.openexchange.contact.vcard.VCardParameters;
import com.openexchange.contact.vcard.VCardService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.generic.FolderUpdaterRegistry;
import com.openexchange.groupware.generic.FolderUpdaterService;
import com.openexchange.groupware.generic.TargetFolderDefinition;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.rest.client.httpclient.HttpClients;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionErrorMessage;
import com.openexchange.subscribe.dav.osgi.Services;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.tools.iterator.SearchIteratorDelegator;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractCardDAVSubscribeService} - The abstract super class for CardDAV subscribe services.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public abstract class AbstractCardDAVSubscribeService extends AbstractDAVSubscribeService {

    /** The static logger */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractCardDAVSubscribeService.class);

    private static final DavPropertyName PROPERTY_ADDRESSBOOK_HOME_SET = DavPropertyName.create("addressbook-home-set", Namespace.getNamespace("", "urn:ietf:params:xml:ns:carddav"));
    private static final DavPropertyName PROPERTY_ADDRESS_DATA = DavPropertyName.create("address-data", Namespace.getNamespace("", "urn:ietf:params:xml:ns:carddav"));

    /**
     * Initializes a new {@link AbstractCardDAVSubscribeService}.
     */
    protected AbstractCardDAVSubscribeService(ServiceLookup services) {
        super(services);
    }

    @Override
    protected DAVFolderModule getFolderModule() {
        return DAVFolderModule.CONTACTS;
    }

    @Override
    public Collection<?> getContent(final Subscription subscription) throws OXException {
        final VCardService vcardService = Services.getOptionalService(VCardService.class);
        if (null == vcardService) {
            throw ServiceExceptionCode.absentService(VCardService.class);
        }

        Map<String, Object> configuration = subscription.getConfiguration();
        final String login = (String) configuration.get("login");
        final String password = (String) configuration.get("password");

        final HttpClient httpClient = initHttpClient(10, 5, 5000, 10000);
        try {
            String addressBookHome = getAddressBookHome(httpClient, login, password, subscription);
            List<DisplayNameAndHref> addressbooks = getAvailableCollectionsInAddressbook(addressBookHome, httpClient, login, password, subscription);

            ThreadPoolService threadPool = Services.getOptionalService(ThreadPoolService.class);
            FolderUpdaterRegistry folderUpdaterRegistry = Services.getOptionalService(FolderUpdaterRegistry.class);

            int chunkSize = getChunkSize(subscription.getSession());
            if (null == threadPool || null == folderUpdaterRegistry) {
                List<Contact> retval = new LinkedList<>();
                boolean firstAddressBook = true;
                for (DisplayNameAndHref addressBook : addressbooks) {
                    FolderUpdaterService<Contact> folderUpdater = null;
                    if (firstAddressBook) {
                        firstAddressBook = false;
                    } else {
                        folderUpdater = updaterForNewFolder(addressBook, subscription, folderUpdaterRegistry);
                    }

                    List<String> contactHrefs = getContactHrefsFrom(addressBook.getHref(), httpClient, login, password, subscription);
                    for (List<String> contactChunk : Lists.partition(contactHrefs, chunkSize)) {
                        List<Contact> contacts = processAddressBookChunk(addressBook, contactChunk, httpClient, login, password, subscription, vcardService);
                        if (null == folderUpdater) {
                            retval.addAll(contacts);
                        } else {
                            folderUpdater.save(new SearchIteratorDelegator<Contact>(contacts), subscription);
                        }
                    }
                }
                return retval;
            }

            boolean first = true;
            List<Contact> retval = null;
            boolean firstAddressBook = true;
            for (final DisplayNameAndHref addressBook : addressbooks) {
                FolderUpdaterService<Contact> folderUpdater = null;
                if (firstAddressBook) {
                    firstAddressBook = false;
                } else {
                    folderUpdater = updaterForNewFolder(addressBook, subscription, folderUpdaterRegistry);
                }

                List<String> contactHrefs = getContactHrefsFrom(addressBook.getHref(), httpClient, login, password, subscription);
                for (final List<String> contactChunk : Lists.partition(contactHrefs, chunkSize)) {
                    if (first) {
                        retval = processAddressBookChunk(addressBook, contactChunk, httpClient, login, password, subscription, vcardService);
                        first = false;
                    } else {
                        final FolderUpdaterService<Contact> updaterToUse = null == folderUpdater ? folderUpdaterRegistry.<Contact> getFolderUpdater(subscription) : folderUpdater;
                        Task<Void> task = new AbstractTask<Void>() {

                            @Override
                            public Void call() {
                                try {
                                    List<Contact> contacts = processAddressBookChunk(addressBook, contactChunk, httpClient, login, password, subscription, vcardService);
                                    updaterToUse.save(new SearchIteratorDelegator<Contact>(contacts), subscription);
                                } catch (Exception e) {
                                    LOG.error("Failed process vcard chunk", e);
                                }
                                return null;
                            }
                        };
                        threadPool.submit(task);
                    }
                }
            }

            return null== retval ? Collections.<Contact> emptyList() : retval;
        } finally {
            HttpClients.shutDown(httpClient);
        }
    }

    private FolderUpdaterService<Contact> updaterForNewFolder(DisplayNameAndHref addressBook, final Subscription subscription, FolderUpdaterRegistry folderUpdaterRegistry) throws OXException {
        if (null == folderUpdaterRegistry) {
            return null;
        }
        FolderUpdaterService<Contact> folderUpdater;
        FolderObject newContactFolder = new FolderObject();
        newContactFolder.setParentFolderID(subscription.getFolderIdAsInt());
        newContactFolder.setFolderName(addressBook.getDisplayName());
        newContactFolder.setModule(FolderObject.CONTACT);
        OCLPermission permission = new OCLPermission();
        permission.setEntity(null != subscription.getSession() ? subscription.getSession().getUserId() : -1);
        permission.setGroupPermission(false);
        permission.setAllPermission(OCLPermission.CREATE_SUB_FOLDERS, OCLPermission.READ_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_ALL_OBJECTS);
        permission.setFolderAdmin(true);
        newContactFolder.setPermissions(Arrays.asList(permission));
        newContactFolder = OXFolderManager.getInstance(subscription.getSession()).createFolder(newContactFolder, true, System.currentTimeMillis());
        folderUpdater = folderUpdaterRegistry.getFolderUpdater(new TargetFolderDefinition(Integer.toString(newContactFolder.getObjectID()), subscription.getSession().getUserId(), subscription.getSession().getContext()));
        return folderUpdater;
    }

    /**
     * Processes the specified chunk of contacts.
     *
     * @param addressBook The associated address book
     * @param contactHrefs The chunk of contacts' hrefs
     * @param httpClient The HTTP client to use
     * @param login The login
     * @param password The password
     * @param subscription The subscription representation
     * @param vcardService
     * @return The converted contacts
     * @throws OXException
     */
    protected List<Contact> processAddressBookChunk(DisplayNameAndHref addressBook, List<String> contactHrefs, HttpClient httpClient, String login, String password, Subscription subscription, VCardService vcardService) throws OXException {
        HttpEntityMethod propfind = null;
        try {
            propfind = new HttpEntityMethod("REPORT", buildUri(getBaseUrl(subscription.getSession()), null, addressBook.getHref()));
            setAuthorizationHeader(propfind, login, password);
            propfind.setHeader(HttpHeaders.DEPTH, "1");

            {
                StringBuilder xmlBody = new StringBuilder(
                    "<c:addressbook-multiget xmlns:d=\"DAV:\" xmlns:c=\"urn:ietf:params:xml:ns:carddav\">\n" +
                    "  <d:prop>\n" +
                    "     <c:address-data />\n" +
                    "  </d:prop>\n");

                for (String contactHref : contactHrefs) {
                    xmlBody.append("  <d:href>").append(contactHref).append("</d:href>\n");
                }
                xmlBody.append("</c:addressbook-multiget>");
                propfind.setEntity(new StringEntity(xmlBody.toString(), ContentType.TEXT_XML));
            }

            HttpResponse httpResponse = httpClient.execute(propfind);
            StatusLine statusLine = httpResponse.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode < 200 || statusCode >= 300) {
                String reason;
                try {
                    String body = Streams.reader2string(new InputStreamReader(httpResponse.getEntity().getContent(), Charsets.UTF_8));
                    reason = body;
                } catch (final Exception e) {
                    reason = statusLine.getReasonPhrase();
                }
                throw new HttpResponseException(statusCode, reason);
            }

            Document document = getResponseBodyAsDocument(httpResponse);
            if (null == document) {
                throw new IOException("Missing DOM document in HTTP response body");
            }

            Element rootElement = document.getDocumentElement();
            MultiStatus multiStatus = MultiStatus.createFromXml(rootElement);
            MultiStatusResponse[] multiStatusResponses = multiStatus.getResponses();

            VCardParameters parameters = vcardService.createParameters(subscription.getSession());
            parameters.setKeepOriginalVCard(false);
            List<Contact> contacts = new ArrayList<>(multiStatusResponses.length);
            for (MultiStatusResponse multiStatusResponse : multiStatusResponses) {
                String href = multiStatusResponse.getHref();
                if (!href.endsWith("/")) {
                    DavPropertySet propertySet = multiStatusResponse.getProperties(HttpStatus.SC_OK);
                    DavProperty<?> addressDataProperty = propertySet.get(PROPERTY_ADDRESS_DATA);
                    String vcard = addressDataProperty.getValue().toString();
                    contacts.add(vcardService.importVCard(Streams.newByteArrayInputStream(vcard.getBytes(Charsets.UTF_8)), null, parameters).getContact());
                }
            }
            return contacts;
        } catch (HttpResponseException e) {
            if (400 == e.getStatusCode() || 401 == e.getStatusCode()) {
                // Authentication failed
                throw SubscriptionErrorMessage.INVALID_LOGIN.create(e, e.getMessage());
            }
            throw SubscriptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (IOException e) {
            throw SubscriptionErrorMessage.IO_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw SubscriptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            reset(propfind);
        }
    }

    /**
     * Gets the href value of the address book.
     *
     * @param httpClient Tehe HTTP client to use
     * @param login The login
     * @param password The password
     * @param subscription The subscription representation
     * @return The href value
     * @throws OXException If href value cannot be returned
     */
    protected String getAddressBookHome(HttpClient httpClient, String login, String password, Subscription subscription) throws OXException {
        HttpEntityMethod propfind = null;
        try {
            propfind = new HttpEntityMethod("PROPFIND", getUserPrincipal(subscription.getSession()));
            setAuthorizationHeader(propfind, login, password);
            propfind.setHeader(HttpHeaders.DEPTH, "1");

            {
                String xmlBody =
                    "<d:propfind xmlns:d=\"DAV:\" xmlns:c=\"urn:ietf:params:xml:ns:carddav\" xmlns:cs=\"http://calendarserver.org/ns/\">\n" +
                    "  <d:prop>\n" +
                    "     <c:addressbook-home-set/>\n" +
                    "  </d:prop>\n" +
                    "</d:propfind>";
                propfind.setEntity(new StringEntity(xmlBody, ContentType.TEXT_XML));
            }

            HttpResponse httpResponse = httpClient.execute(propfind);
            StatusLine statusLine = httpResponse.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode < 200 || statusCode >= 300) {
                String reason;
                try {
                    String body = Streams.reader2string(new InputStreamReader(httpResponse.getEntity().getContent(), Charsets.UTF_8));
                    reason = body;
                } catch (final Exception e) {
                    reason = statusLine.getReasonPhrase();
                }
                throw new HttpResponseException(statusCode, reason);
            }

            Document document = getResponseBodyAsDocument(httpResponse);
            if (null == document) {
                throw new IOException("Missing DOM document in HTTP response body");
            }

            Element rootElement = document.getDocumentElement();
            MultiStatus multiStatus = MultiStatus.createFromXml(rootElement);
            for (MultiStatusResponse multiStatusResponse : multiStatus.getResponses()) {
                DavPropertySet propertySet = multiStatusResponse.getProperties(HttpStatus.SC_OK);
                DavProperty<Element> property = (DavProperty<Element>) propertySet.get(PROPERTY_ADDRESSBOOK_HOME_SET);
                if (null != property) {
                    Element addressBookHomeElement = property.getValue();
                    if ("href".equalsIgnoreCase(addressBookHomeElement.getLocalName())) {
                        return addressBookHomeElement.getTextContent();
                    }
                }
            }

            throw new IOException("Missing address book href");
        } catch (HttpResponseException e) {
            if (400 == e.getStatusCode() || 401 == e.getStatusCode()) {
                // Authentication failed
                throw SubscriptionErrorMessage.INVALID_LOGIN.create(e, e.getMessage());
            }
            throw SubscriptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (IOException e) {
            throw SubscriptionErrorMessage.IO_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw SubscriptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            reset(propfind);
        }
    }

    /**
     * Gets the hrefs from available contacts in specified address book
     *
     * @param addressBookHref The address book's href
     * @param httpClient The HTTP client to use
     * @param login The login
     * @param password The password
     * @param subscription The subscription representation
     * @return The hrefs of available collections
     * @throws OXException
     */
    protected List<String> getContactHrefsFrom(String addressBookHref, HttpClient httpClient, String login, String password, Subscription subscription) throws OXException {
        HttpEntityMethod propfind = null;
        try {
            propfind = new HttpEntityMethod("PROPFIND", buildUri(getBaseUrl(subscription.getSession()), null, addressBookHref));
            setAuthorizationHeader(propfind, login, password);
            propfind.setHeader(HttpHeaders.DEPTH, "1");

            {
                String xmlBody =
                    "<d:propfind xmlns:d=\"DAV:\" xmlns:c=\"urn:ietf:params:xml:ns:carddav\" xmlns:cs=\"http://calendarserver.org/ns/\">\n" +
                    "  <d:prop>\n" +
                    "     <d:getetag />\n" +
                    "     <d:getcontenttype />\n" +
                    "  </d:prop>\n" +
                    "</d:propfind>";
                propfind.setEntity(new StringEntity(xmlBody, ContentType.TEXT_XML));
            }

            HttpResponse httpResponse = httpClient.execute(propfind);
            StatusLine statusLine = httpResponse.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode < 200 || statusCode >= 300) {
                String reason;
                try {
                    String body = Streams.reader2string(new InputStreamReader(httpResponse.getEntity().getContent(), Charsets.UTF_8));
                    reason = body;
                } catch (final Exception e) {
                    reason = statusLine.getReasonPhrase();
                }
                throw new HttpResponseException(statusCode, reason);
            }

            Document document = getResponseBodyAsDocument(httpResponse);
            if (null == document) {
                throw new IOException("Missing DOM document in HTTP response body");
            }

            Element rootElement = document.getDocumentElement();
            MultiStatus multiStatus = MultiStatus.createFromXml(rootElement);
            MultiStatusResponse[] multiStatusResponses = multiStatus.getResponses();
            List<String> hrefs = new ArrayList<>(multiStatusResponses.length);
            for (MultiStatusResponse multiStatusResponse : multiStatusResponses) {
                String href = multiStatusResponse.getHref();
                if (!href.endsWith("/")) {
                    DavPropertySet propertySet = multiStatusResponse.getProperties(HttpStatus.SC_OK);
                    DavProperty<?> contentTypeProperty = propertySet.get(DavPropertyName.GETCONTENTTYPE);
                    if (isVCard(contentTypeProperty)) {
                        hrefs.add(href);
                    }
                }
            }

            return hrefs;
        } catch (HttpResponseException e) {
            if (400 == e.getStatusCode() || 401 == e.getStatusCode()) {
                // Authentication failed
                throw SubscriptionErrorMessage.INVALID_LOGIN.create(e, e.getMessage());
            }
            throw SubscriptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (IOException e) {
            throw SubscriptionErrorMessage.IO_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw SubscriptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            reset(propfind);
        }
    }

    private boolean isVCard(DavProperty<?> contentTypeProperty) {
        if (null == contentTypeProperty) {
            return false;
        }

        Object value = contentTypeProperty.getValue();
        if (value instanceof String) {
            return Strings.asciiLowerCase(value.toString()).startsWith("text/vcard");
        }

        if (!(value instanceof Element)) {
            return false;
        }

        String contentType = ((Element) value).getTextContent();
        return Strings.asciiLowerCase(contentType).startsWith("text/vcard");
    }

    /**
     * Gets the available collections/folders in address book
     *
     * @param addressBookHomeHref The address book home href
     * @param httpClient The HTTP client to use
     * @param login The login
     * @param password The password
     * @param subscription The subscription representation
     * @return The hrefs of available collections
     * @throws OXException
     */
    protected List<DisplayNameAndHref> getAvailableCollectionsInAddressbook(String addressBookHomeHref, HttpClient httpClient, String login, String password, Subscription subscription) throws OXException {
        HttpEntityMethod propfind = null;
        try {
            propfind = new HttpEntityMethod("PROPFIND", buildUri(getBaseUrl(subscription.getSession()), null, addressBookHomeHref));
            setAuthorizationHeader(propfind, login, password);
            propfind.setHeader(HttpHeaders.DEPTH, "1");

            {
                String xmlBody =
                    "<d:propfind xmlns:d=\"DAV:\" xmlns:cs=\"http://calendarserver.org/ns/\">\n" +
                    "  <d:prop>\n" +
                    "     <d:displayname />\n" +
                    "     <d:resourcetype />\n" +
                    "     <cs:getctag />\n" +
                    "     <d:current-user-principal />\n" +
                    "  </d:prop>\n" +
                    "</d:propfind>";
                propfind.setEntity(new StringEntity(xmlBody, ContentType.TEXT_XML));
            }

            HttpResponse httpResponse = httpClient.execute(propfind);
            StatusLine statusLine = httpResponse.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode < 200 || statusCode >= 300) {
                String reason;
                try {
                    String body = Streams.reader2string(new InputStreamReader(httpResponse.getEntity().getContent(), Charsets.UTF_8));
                    reason = body;
                } catch (final Exception e) {
                    reason = statusLine.getReasonPhrase();
                }
                throw new HttpResponseException(statusCode, reason);
            }

            Document document = getResponseBodyAsDocument(httpResponse);
            if (null == document) {
                throw new IOException("Missing DOM document in HTTP response body");
            }

            Element rootElement = document.getDocumentElement();
            MultiStatus multiStatus = MultiStatus.createFromXml(rootElement);
            MultiStatusResponse[] multiStatusResponses = multiStatus.getResponses();
            List<DisplayNameAndHref> addressBooks = new ArrayList<>(multiStatusResponses.length);
            for (MultiStatusResponse multiStatusResponse : multiStatusResponses) {
                String href = multiStatusResponse.getHref();

                DavPropertySet propertySet = multiStatusResponse.getProperties(HttpStatus.SC_OK);
                DavProperty<?> resourceTypeProperty = propertySet.get(DavPropertyName.RESOURCETYPE);
                if (isAddressBookCollection(resourceTypeProperty)) {
                    DavProperty<?> displayNameProperty = propertySet.get(DavPropertyName.DISPLAYNAME);
                    addressBooks.add(new DisplayNameAndHref(getDisplayNameFrom(displayNameProperty), href));
                }
            }

            return addressBooks;
        } catch (HttpResponseException e) {
            if (400 == e.getStatusCode() || 401 == e.getStatusCode()) {
                // Authentication failed
                throw SubscriptionErrorMessage.INVALID_LOGIN.create(e, e.getMessage());
            }
            throw SubscriptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (IOException e) {
            throw SubscriptionErrorMessage.IO_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw SubscriptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            reset(propfind);
        }
    }

    private boolean isAddressBookCollection(DavProperty<?> resourceTypeProperty) {
        if (null == resourceTypeProperty) {
            return false;
        }

        Object value = resourceTypeProperty.getValue();
        if (!(value instanceof Collection)) {
            return false;
        }

        Collection<Element> elements = (Collection<Element>) value;
        boolean isCollection = false;
        boolean isAddressBook = false;
        for (Iterator<Element> it = elements.iterator(); (!isCollection || !isAddressBook) && it.hasNext();) {
            Element element = it.next();
            if ("collection".equalsIgnoreCase(element.getLocalName())) {
                isCollection = true;
            } else if ("addressbook".equalsIgnoreCase(element.getLocalName())) {
                isAddressBook = true;
            }
        }
        return isCollection && isAddressBook;
    }

    private String getDisplayNameFrom(DavProperty<?> displayNameProperty) {
        if (null == displayNameProperty) {
            return null;
        }

        Object value = displayNameProperty.getValue();

        if (value instanceof String) {
            return value.toString();
        }

        if (!(value instanceof Element)) {
            return null;
        }

        return ((Element) value).getTextContent();
    }

    /**
     * Gets the base URL; e.g. <code>"https://carddav.providerx.org/CardDavProxy/carddav"</code>
     *
     * @param session The session for which to determine the base URL
     * @return The base URL
     */
    protected abstract URI getBaseUrl(ServerSession session);

    /**
     * Gets the URL for the user principal that will be queried in order to get the <code>"addressbook-home-set"</code> property; e.g. <code>"https://carddav.providerx.org/CardDavProxy/carddav/principals/1234"</code>
     *
     * @param session The session for which to determine the URL for the user principal
     * @return The user principal URL
     */
    protected abstract URI getUserPrincipal(ServerSession session);

    /**
     * @param session
     * @return
     */
    protected abstract int getChunkSize(ServerSession session);

}
