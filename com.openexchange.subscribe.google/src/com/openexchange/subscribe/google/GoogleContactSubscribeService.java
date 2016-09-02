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

package com.openexchange.subscribe.google;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.activation.FileTypeMap;
import org.slf4j.Logger;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.gdata.client.Query;
import com.google.gdata.client.Service.GDataRequest;
import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.Link;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ContactFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
import com.openexchange.ajax.container.ByteArrayFileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.google.api.client.GoogleApiClients;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.generic.FolderUpdaterRegistry;
import com.openexchange.groupware.generic.FolderUpdaterService;
import com.openexchange.java.ImageTypeDetector;
import com.openexchange.java.Streams;
import com.openexchange.oauth.API;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.scope.Module;
import com.openexchange.server.ServiceLookup;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionErrorMessage;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.google.internal.ContactEntryParser;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.tools.iterator.SearchIteratorDelegator;

/**
 * {@link GoogleContactSubscribeService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class GoogleContactSubscribeService extends AbstractGoogleSubscribeService {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(GoogleContactSubscribeService.class);

    // -------------------------------------------------------------------------------------------------------------------------- //

    /**
     * Sends an image request to Google service. Reads the mime type of the photo and return the result the corresponding photo as binary content.
     *
     * @param contactsService The contact service
     * @param entry The contact entry
     * @param photoLink The image URL
     * @return The appropriate file holder
     * @throws ServiceException if creation of request failed
     * @throws IOException if an error during the communication occured
     */
    static IFileHolder loadImageFromLink(final ContactsService contactsService, final ContactEntry entry, final Link photoLink) throws IOException, ServiceException {
        if (photoLink != null && photoLink.getEtag() != null) {
            String mimeType = null;
            byte[] bytes = null;
            GDataRequest gRequest = null;
            InputStream in = null;
            try {
                gRequest = contactsService.createLinkQueryRequest(photoLink);
                gRequest.execute();
                in = gRequest.getResponseStream();
                ByteArrayOutputStream out = Streams.newByteArrayOutputStream(in.available());
                transfer(in, out);

                com.google.gdata.util.ContentType ct = gRequest.getResponseContentType();
                if (ct != null) {
                    mimeType = ct.getMediaType();
                }
                bytes = out.toByteArray();
            } finally {
                Streams.close(in);
                if (gRequest != null) {
                    gRequest.end();
                }
            }

            if (null != bytes) {
                final ByteArrayFileHolder fileHolder = new ByteArrayFileHolder(bytes);
                if (mimeType == null) {
                    mimeType = ImageTypeDetector.getMimeType(bytes);
                    if ("application/octet-stream".equals(mimeType)) {
                        mimeType = getMimeType(photoLink.toString());
                    }
                }
                if (isValidImage(bytes)) {
                    // Mime type should be of image type. Otherwise web server send some error page instead of 404 error code.
                    if (null == mimeType) {
                        mimeType = "image/jpeg";
                    }
                    fileHolder.setContentType(mimeType);
                }
                return fileHolder;
            }
        }
        return null;
    }

    private static void transfer(final InputStream in, final OutputStream out) throws IOException {
        final byte[] buffer = new byte[4096];
        int length;
        while ((length = in.read(buffer)) > 0) {
            out.write(buffer, 0, length);
        }
        out.flush();
    }

    private static final FileTypeMap DEFAULT_FILE_TYPE_MAP = FileTypeMap.getDefaultFileTypeMap();

    private static String getMimeType(final String filename) {
        return DEFAULT_FILE_TYPE_MAP.getContentType(filename);
    }

    private static boolean isValidImage(final byte[] data) {
        java.awt.image.BufferedImage bimg = null;
        try {
            bimg = javax.imageio.ImageIO.read(Streams.newByteArrayInputStream(data));
        } catch (final Exception e) {
            return false;
        }
        return (bimg != null);
    }

    // -------------------------------------------------------------------------------------------------------------------------- //

    private interface PhotoHandler {

        void handlePhoto(ContactsService contactsService, ContactEntry entry, Contact contact) throws OXException;
    }

    private final PhotoHandler loadingPhotoHandler = new PhotoHandler() {

        @Override
        public void handlePhoto(ContactsService contactsService, ContactEntry entry, Contact contact) throws OXException {
            if (null == entry || null == contact) {
                return;
            }

            try {
                Link photoLink = entry.getContactPhotoLink();
                IFileHolder photo = loadImageFromLink(contactsService, entry, photoLink);
                if (photo != null) {
                    byte[] bytes = Streams.stream2bytes(photo.getStream());
                    contact.setImage1(bytes);
                    contact.setImageContentType(photo.getContentType());
                }
            } catch (ServiceException e) {
                throw SubscriptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
            } catch (IOException e) {
                throw SubscriptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
    };

    // -------------------------------------------------------------------------------------------------------------------------- //

    private final int pageSize;
    private static final URL CONTACT_URL;
    private static final URL GROUP_URL;

    static {
        try {
            URL feedUrl = new URL("https://www.google.com/m8/feeds/contacts/default/full");
            CONTACT_URL = feedUrl;

            feedUrl = new URL("https://www.google.com/m8/feeds/groups/default/full");
            GROUP_URL = feedUrl;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    // ------------------------------------------------------------------------------------------------------------------- //

    private final SubscriptionSource source;

    private final ContactEntryParser parser;

    public GoogleContactSubscribeService(final OAuthServiceMetaData googleMetaData, ServiceLookup services) {
        super(googleMetaData, services);
        source = initSS(FolderObject.CONTACT, "contact");
        parser = new ContactEntryParser();
        final ConfigurationService configService = services.getService(ConfigurationService.class);
        pageSize = configService.getIntProperty("com.openexchange.subscribe.google.contact.pageSize", 25);
    }

    @Override
    public SubscriptionSource getSubscriptionSource() {
        return source;
    }

    @Override
    public boolean handles(int folderModule) {
        return FolderObject.CONTACT == folderModule;
    }

    @Override
    public Collection<?> getContent(final Subscription subscription) throws OXException {
        try {
            // Establish GData contact service using OAuth v2 credentials
            final ContactsService contactsService;
            {
                GoogleCredential googleCreds = GoogleApiClients.getCredentials(subscription.getSession());
                String productName = GoogleApiClients.getGoogleProductName();
                contactsService = new ContactsService(productName);
                contactsService.setOAuth2Credentials(googleCreds);
            }

            // Compose the appropriate query for contacts
            final Query contactQuery = new Query(CONTACT_URL);
            contactQuery.setStringCustomParameter("orderby", "lastmodified");
            contactQuery.setStringCustomParameter("sortorder", "descending");

            // First page with this thread
            int page = 1;
            adjustQuery(contactQuery, page, pageSize);

            List<Contact> contacts;
            int resultsFound;
            {
                contacts = new LinkedList<Contact>();
                resultsFound = fetchResults(contactsService, contactQuery, contacts);
            }

            if (resultsFound != pageSize) {
                // No more available - return first chunk
                return contacts;
            }

            // More available
            page++;

            // Query page-wise with either this thread or background thread
            final FolderUpdaterRegistry folderUpdaterRegistry = services.getOptionalService(FolderUpdaterRegistry.class);
            final ThreadPoolService threadPool = services.getOptionalService(ThreadPoolService.class);
            final FolderUpdaterService<Contact> folderUpdater = null == folderUpdaterRegistry ? null : folderUpdaterRegistry.<Contact> getFolderUpdater(subscription);
            if (null == threadPool || null == folderUpdater) {
                // All with this thread
                do {
                    adjustQuery(contactQuery, page, pageSize);
                    resultsFound = fetchResults(contactsService, contactQuery, contacts);
                    page++;
                } while (resultsFound == pageSize);
                return contacts;
            }

            // Query more in the background and...
            {
                final int pageOffset = page;
                threadPool.submit(new AbstractTask<Void>() {

                    @Override
                    public Void call() throws Exception {
                        int page = pageOffset;
                        int resultsFound = pageSize;
                        while (resultsFound == pageSize) {
                            List<Contact> contacts = new ArrayList<Contact>();
                            adjustQuery(contactQuery, page, pageSize);
                            resultsFound = fetchResults(contactsService, contactQuery, contacts);
                            folderUpdater.save(new SearchIteratorDelegator<Contact>(contacts), subscription);

                            // Next page...
                            page++;
                        }
                        return null;
                    }
                });
            }

            // ... return first chunk
            return contacts;
        } catch (IOException e) {
            throw SubscriptionErrorMessage.IO_ERROR.create(e, e.getMessage());
        } catch (ServiceException e) {
            throw SubscriptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw SubscriptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private void adjustQuery(final Query query, final int page, final int pageSize) {
        query.setStartIndex((page - 1) * pageSize + 1);
        query.setMaxResults(pageSize);
    }

    private int fetchResults(final ContactsService contactsService, final Query query, final List<Contact> contacts) throws OXException, IOException, ServiceException {
        ContactFeed contactFeed;
        try {
            contactFeed = contactsService.getFeed(query, ContactFeed.class);
        } catch (AuthenticationException e) {
            throw OAuthExceptionCodes.NO_SCOPE_PERMISSION.create(API.GOOGLE.getShortName(), Module.contacts);
        } catch (NullPointerException e) {
            if (e.getMessage().equals("No authentication header information")) {
                throw OAuthExceptionCodes.NO_SCOPE_PERMISSION.create(API.GOOGLE.getShortName(), Module.contacts);
            }
            throw e;
        }

        for (ContactEntry entry : contactFeed.getEntries()) {
            Contact contact = new Contact();
            parser.parseContact(entry, contact);
            loadingPhotoHandler.handlePhoto(contactsService, entry, contact);
            contacts.add(contact);
        }
        return contactFeed.getEntries().size();
    }
}
