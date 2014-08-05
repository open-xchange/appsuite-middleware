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

package com.openexchange.subscribe.google;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.activation.FileTypeMap;
import org.slf4j.Logger;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.gdata.client.Query;
import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.Link;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ContactFeed;
import com.google.gdata.data.extensions.Email;
import com.google.gdata.data.extensions.Im;
import com.google.gdata.data.extensions.Name;
import com.google.gdata.data.extensions.Organization;
import com.google.gdata.data.extensions.PhoneNumber;
import com.google.gdata.data.extensions.StructuredPostalAddress;
import com.google.gdata.util.ServiceException;
import com.openexchange.ajax.container.ByteArrayFileHolder;
import com.openexchange.ajax.container.IFileHolder;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.google.api.client.GoogleApiClients;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.generic.FolderUpdaterRegistry;
import com.openexchange.groupware.generic.FolderUpdaterService;
import com.openexchange.java.ImageTypeDetector;
import com.openexchange.java.Streams;
import com.openexchange.java.util.TimeZones;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.server.ServiceLookup;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionErrorMessage;
import com.openexchange.subscribe.SubscriptionSource;
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
     * Open a new {@link URLConnection URL connection} to specified parameter's value which indicates to be an URI/URL. The image's data and
     * its MIME type is then read from opened connection.
     *
     * @param url The URI parameter's value
     * @return The appropriate file holder
     * @throws IOException If converting image's data fails
     */
    static IFileHolder loadImageFromURL(final String url) throws IOException {
        try {
            return loadImageFromURL(new URL(url));
        } catch (final MalformedURLException e) {
            throw new IOException("Problem loading photo from URL: " + url, e);
        }
    }

    /**
     * Open a new {@link URLConnection URL connection} to specified parameter's value which indicates to be an URI/URL. The image's data and
     * its MIME type is then read from opened connection.
     *
     * @param url The image URL
     * @return The appropriate file holder
     * @throws XingException If converting image's data fails
     */
    private static IFileHolder loadImageFromURL(final URL url) throws IOException {
        String mimeType = null;
        byte[] bytes = null;
        try {
            final URLConnection urlCon = url.openConnection();
            urlCon.setConnectTimeout(2500);
            urlCon.setReadTimeout(2500);
            urlCon.connect();
            mimeType = urlCon.getContentType();
            final InputStream in = urlCon.getInputStream();
            try {
                final ByteArrayOutputStream buffer = Streams.newByteArrayOutputStream(in.available());
                transfer(in, buffer);
                bytes = buffer.toByteArray();
            } finally {
                Streams.close(in);
            }
        } catch (final SocketTimeoutException e) {
            throw e;
        } catch (final IOException e) {
            throw e;
        }
        if (null != bytes) {
            final ByteArrayFileHolder fileHolder = new ByteArrayFileHolder(bytes);
            if (mimeType == null) {
                mimeType = ImageTypeDetector.getMimeType(bytes);
                if ("application/octet-stream".equals(mimeType)) {
                    mimeType = getMimeType(url.toString());
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

        void handlePhoto(ContactEntry entry, Contact contact) throws OXException;
    }

    private final class CollectingPhotoHandler implements PhotoHandler {

        private final Map<String, String> photoUrlsMap;

        /**
         * Initializes a new {@link CollectingPhotoHandler}.
         */
        CollectingPhotoHandler(Map<String, String> photoUrlsMap) {
            super();
            this.photoUrlsMap = photoUrlsMap;
        }

        @Override
        public void handlePhoto(ContactEntry entry, Contact contact) throws OXException {
            Link photoLink = entry.getContactPhotoLink();
            if (photoLink != null) {
                String photoLinkHref = photoLink.getHref();
                if (null != photoLinkHref) {
                    photoUrlsMap.put(entry.getId(), photoLinkHref);
                }
            }
        }
    }

    private final PhotoHandler loadingPhotoHandler = new PhotoHandler() {

        @Override
        public void handlePhoto(ContactEntry entry, Contact contact) throws OXException {
            if (null == entry || null == contact) {
                return;
            }

            String url = null;
            {
                Link photoLink = entry.getContactPhotoLink();
                if (photoLink != null) {
                    url = photoLink.getHref();
                }
            }

            if (url != null) {
                try {
                    IFileHolder photo = loadImageFromURL(url);
                    if (photo != null) {
                        byte[] bytes = Streams.stream2bytes(photo.getStream());
                        contact.setImage1(bytes);
                        contact.setImageContentType(photo.getContentType());
                    }
                } catch (IOException e) {
                    throw SubscriptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
                }
            }
        }
    };

    // -------------------------------------------------------------------------------------------------------------------------- //

    private static final int PAGE_SIZE = 25;
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

    public GoogleContactSubscribeService(final OAuthServiceMetaData googleMetaData, ServiceLookup services) {
        super(googleMetaData, services);
        source = initSS(FolderObject.CONTACT, "contact");
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
                String productName = services.getService(ConfigurationService.class).getProperty("com.openexchange.oauth.google.productName", "");
                contactsService = new ContactsService(productName);
                contactsService.setOAuth2Credentials(googleCreds);
            }

            // Compose the appropriate query for contacts
            final Query contactQuery = new Query(CONTACT_URL);

            // First page with this thread
            final int pageSize = PAGE_SIZE;
            int page = 1;
            contactQuery.setStartIndex((page - 1) * pageSize + 1);
            contactQuery.setMaxResults(pageSize);
            contactQuery.setStringCustomParameter("orderby", "lastmodified");
            contactQuery.setStringCustomParameter("sortorder", "descending");

            List<Contact> contacts;
            int resultsFound;
            {
                ContactFeed resultFeed = contactsService.getFeed(contactQuery, ContactFeed.class);
                resultsFound = resultFeed.getEntries().size();
                contacts = new LinkedList<Contact>();
                handleContactFeed(resultFeed, contacts, loadingPhotoHandler);
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
                    contactQuery.setStartIndex((page - 1) * pageSize + 1);
                    contactQuery.setMaxResults(pageSize);
                    contactQuery.setStringCustomParameter("orderby", "lastmodified");
                    contactQuery.setStringCustomParameter("sortorder", "descending");

                    ContactFeed resultFeed = contactsService.getFeed(contactQuery, ContactFeed.class);
                    resultsFound = resultFeed.getEntries().size();
                    handleContactFeed(resultFeed, contacts, loadingPhotoHandler);
                    page++;
                } while (resultsFound == pageSize);
                return contacts;
            }

            // Query more in the background and...
            {
                final int pageOffset = page;
                final PhotoHandler ph = loadingPhotoHandler;
                threadPool.submit(new AbstractTask<Void>() {

                    @Override
                    public Void call() throws Exception {
                        int myPage = pageOffset;
                        int resultsFound = pageSize;
                        while (resultsFound == pageSize) {
                            List<Contact> contacts = new ArrayList<Contact>();
                            contactQuery.setStartIndex((myPage - 1) * pageSize + 1);
                            contactQuery.setMaxResults(pageSize);
                            contactQuery.setStringCustomParameter("orderby", "lastmodified");
                            contactQuery.setStringCustomParameter("sortorder", "descending");

                            ContactFeed resultFeed = contactsService.getFeed(contactQuery, ContactFeed.class);
                            handleContactFeed(resultFeed, contacts, ph);

                            folderUpdater.save(new SearchIteratorDelegator<Contact>(contacts), subscription);

                            // Next page...
                            myPage++;
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

    protected void handleContactFeed(ContactFeed contactFeed, List<Contact> contacts, PhotoHandler photoHandler) throws OXException {
        for (ContactEntry entry : contactFeed.getEntries()) {
            Contact contact = new Contact();

            {
                String emailId = null;
                for (Email email : entry.getEmailAddresses()) {
                    if (email.getPrimary()) {
                        emailId = email.getAddress();
                        break;
                    }
                }
                if (null != emailId) {
                    contact.setEmail1(emailId);
                }
            }

            if(entry.hasName()) {
                Name name = entry.getName();
                if (name.hasFullName()) {
                    contact.setDisplayName(name.getFullName().getValue());
                }
                if (name.hasNamePrefix()) {
                    contact.setTitle(name.getNamePrefix().getValue());
                }
                if (name.hasGivenName()) {
                    contact.setGivenName(name.getGivenName().getValue());
                }
                if (name.hasAdditionalName()) {
                    contact.setMiddleName(name.getAdditionalName().getValue());
                }
                if (name.hasFamilyName()) {
                    contact.setSurName(name.getFamilyName().getValue());
                }
                if (name.hasNameSuffix()) {
                    contact.setSuffix(name.getNameSuffix().getValue());
                }
            }

            if (entry.hasOrganizations()) {
                for (final Organization o : entry.getOrganizations()) {
                    if (o.hasOrgName()) {
                        contact.setCompany(o.getOrgName().getValue());
                    }
                    if (o.hasOrgJobDescription()) {
                        contact.setTitle(o.getOrgJobDescription().getValue());
                    }
                }
            }

            if (entry.hasEmailAddresses()) {
                for (final Email email : entry.getEmailAddresses()) {
                    if (email.getRel() != null) {
                        if (email.getRel().endsWith("work")) {
                            contact.setEmail1(email.getAddress());
                        } else if (email.getRel().endsWith("home")) {
                            contact.setEmail2(email.getAddress());
                        } else if (email.getRel().endsWith("other")) {
                            contact.setEmail3(email.getAddress());
                        }
                    }
                }
            }

            if (entry.hasPhoneNumbers()) {
                boolean mobile1Vacant = true;
                boolean mobile2Vacant = true;
                boolean otherVacant = true;
                for (final PhoneNumber pn : entry.getPhoneNumbers()) {
                    final String rel = pn.getRel();
                    if (rel != null) {
                        if (rel.endsWith("work")) {
                            contact.setTelephoneBusiness1(pn.getPhoneNumber());
                        } else if (rel.endsWith("home")) {
                            contact.setTelephoneHome1(pn.getPhoneNumber());
                        } else if (rel.endsWith("other")) {
                            contact.setTelephoneOther(pn.getPhoneNumber());
                            otherVacant = false;
                        } else if (rel.endsWith("work_fax")) {
                            contact.setFaxBusiness(pn.getPhoneNumber());
                        } else if (rel.endsWith("home_fax")) {
                            contact.setFaxHome(pn.getPhoneNumber());
                        } else if (rel.endsWith("mobile")) {
                            if (mobile1Vacant) {
                                contact.setCellularTelephone1(pn.getPhoneNumber());
                                mobile1Vacant = false;
                            } else if (mobile2Vacant) {
                                contact.setCellularTelephone2(pn.getPhoneNumber());
                                mobile2Vacant = false;
                            } else if (otherVacant) {
                                contact.setTelephoneOther(pn.getPhoneNumber());
                                // No, don't set 'otherVacant = false'
                            } else {
                                LOG.debug("Could not map \"mobile\" number {} to a vacant contact field", pn.getPhoneNumber());
                            }
                        }
                    }
                }
            }

            if (entry.getBirthday() != null) {
                setBirthday(contact, entry.getBirthday().getValue());
            }

            if (entry.hasStructuredPostalAddresses()) {
                for (final StructuredPostalAddress pa : entry.getStructuredPostalAddresses()) {
                    if (pa.getRel() != null) {
                        if (pa.getRel().endsWith("work")) {
                            if (pa.getStreet() != null) {
                                contact.setStreetBusiness(pa.getStreet().getValue());
                            }
                            if (pa.getPostcode() != null) {
                                contact.setPostalCodeBusiness(pa.getPostcode().getValue());
                            }
                            if (pa.getCity() != null) {
                                contact.setCityBusiness(pa.getCity().getValue());
                            }
                            if (pa.getCountry() != null) {
                                contact.setCountryBusiness(pa.getCountry().getValue());
                                // TODO: This will be used to write the address to the contacts note-field if the data is not
                                // structured
                                // System.out.println("***** "+"Work:\n"+pa.getFormattedAddress().getValue()+"\n");
                            }
                        }
                        if (pa.getRel().endsWith("home")) {
                            if (pa.getStreet() != null) {
                                contact.setStreetHome(pa.getStreet().getValue());
                            }
                            if (pa.getPostcode() != null) {
                                contact.setPostalCodeHome(pa.getPostcode().getValue());
                            }
                            if (pa.getCity() != null) {
                                contact.setCityHome(pa.getCity().getValue());
                            }
                            if (pa.getCountry() != null) {
                                contact.setCountryHome(pa.getCountry().getValue());
                            }
                        }
                        if (pa.getRel().endsWith("other")) {
                            if (pa.getStreet() != null) {
                                contact.setStreetOther(pa.getStreet().getValue());
                            }
                            if (pa.getPostcode() != null) {
                                contact.setPostalCodeOther(pa.getPostcode().getValue());
                            }
                            if (pa.getCity() != null) {
                                contact.setCityOther(pa.getCity().getValue());
                            }
                            if (pa.getCountry() != null) {
                                contact.setCountryOther(pa.getCountry().getValue());
                            }
                        }
                    }
                }
            }
            if (entry.hasImAddresses()) {
                for (final Im im : entry.getImAddresses()) {
                    if (im.getProtocol() != null) {
                        final String regex = "[^#]*#([a-zA-Z\u00e4\u00f6\u00fc\u00c4\u00d6\u00dc]*)";
                        final Pattern pattern = Pattern.compile(regex);
                        final Matcher matcher = pattern.matcher(im.getProtocol());
                        if (matcher.matches()) {
                            contact.setInstantMessenger1(im.getAddress() + " (" + matcher.group(1) + ")");
                        }
                    }

                }
            }

            photoHandler.handlePhoto(entry, contact);

            /*-
             *
            Link photoLink = entry.getContactPhotoLink();
            if (photoLink != null && !Strings.isEmpty(photoLink.getEtag())) {
                photoLinkHref = photoLink.getHref();
            }

            List<PhoneNumber> phoneNumbers = entry.getPhoneNumbers();
            for (PhoneNumber phoneNumber : phoneNumbers) {


                String rel = phoneNumber.getRel() != null ? phoneNumber.getRel() : (phoneNumber.getLabel() != null ? phoneNumber.getLabel() : "");
                if (rel.contains("#")) {
                    rel = rel.substring(rel.indexOf("#") + 1);
                }
                contact.setRel(rel);
                rel = StringUtils.isBlank(rel) ? "" : "[" + rel + "]";
                contact.setName(name + " <" + phoneNumber.getPhoneNumber() + ">" + " " + rel + " ");
                contact.setPhotoLink(photoLinkHref);
                contact.setValue(phoneNumber.getPhoneNumber());
                contacts.add(contact);
                if (StringUtils.isBlank(mainContactId)) {
                    mainContactId = contactId;
                }
                if (Rel.MOBILE.equalsIgnoreCase(phoneNumber.getRel())) {
                    mainContactId = contactId;
                }
                if (phoneNumber.getPrimary()) {
                    mainContactId = contactId;
                }
            }

            //Note: mainContactId will be "" in case if the contact doesn't have any phone number.
            if (!Strings.isEmpty(mainContactId)) {
                List<GroupMembershipInfo> groupMembershipInfos = entry.getGroupMembershipInfos();
                for (GroupMembershipInfo groupMembershipInfo : groupMembershipInfos) {
                    String groupId = groupMembershipInfo.getHref();
                    if (groupId.contains("/")) {
                        groupId = groupId.substring(groupId.lastIndexOf("/") + 1);
                    }
                    String contactIds = (groupContactMap.get(groupId) != null ? groupContactMap.get(groupId) : "") + mainContactId + ",";
                    groupContactMap.put(groupId, contactIds);
                }
            }
            */

            contacts.add(contact);
        }
    }

    /**
     * Sets the birthday for the contact based on the google information
     *
     * @param contact - the {@link Contact} to set the birthday for
     * @param birthday - the string the birthday is included in
     */
    protected void setBirthday(Contact contact, String birthday) {
        if (birthday != null) {

            final String regex = "([0-9]{4})\\-([0-9]{2})\\-([0-9]{2})";
            if (birthday.matches(regex)) {
                final Pattern pattern = Pattern.compile(regex);
                final Matcher matcher = pattern.matcher(birthday);
                if (matcher.matches() && matcher.groupCount() == 3) {
                    final int year = Integer.parseInt(matcher.group(1));
                    final int month = Integer.parseInt(matcher.group(2));
                    final int day = Integer.parseInt(matcher.group(3));
                    final Calendar cal = Calendar.getInstance(TimeZones.UTC);
                    cal.clear();
                    cal.set(Calendar.DAY_OF_MONTH, day);
                    cal.set(Calendar.MONTH, month - 1);
                    cal.set(Calendar.YEAR, year);
                    contact.setBirthday(cal.getTime());
                }
            }
        }
    }
}
