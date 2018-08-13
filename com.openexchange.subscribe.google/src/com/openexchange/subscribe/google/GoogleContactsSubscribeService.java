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
 *     Copyright (C) 2018-2020 OX Software GmbH
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gdata.client.Query;
import com.google.gdata.client.Service.GDataRequest;
import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.Link;
import com.google.gdata.data.contacts.Birthday;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ContactFeed;
import com.google.gdata.data.extensions.Email;
import com.google.gdata.data.extensions.FamilyName;
import com.google.gdata.data.extensions.GivenName;
import com.google.gdata.data.extensions.Im;
import com.google.gdata.data.extensions.Name;
import com.google.gdata.data.extensions.PhoneNumber;
import com.google.gdata.data.extensions.PostalAddress;
import com.google.gdata.data.extensions.StructuredPostalAddress;
import com.google.gdata.util.ServiceException;
import com.openexchange.exception.OXException;
import com.openexchange.google.api.client.GoogleApiClients;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.generic.FolderUpdaterRegistry;
import com.openexchange.groupware.generic.FolderUpdaterService;
import com.openexchange.java.util.TimeZones;
import com.openexchange.oauth.KnownApi;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.oauth.AbstractOAuthSubscribeService;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.tools.iterator.SearchIteratorDelegator;

/**
 * {@link GoogleContactsSubscribeService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class GoogleContactsSubscribeService extends AbstractOAuthSubscribeService {

    private static final Logger LOG = LoggerFactory.getLogger(GoogleContactsSubscribeService.class);

    /**
     * The Google Contacts' feed URL
     */
    private static final String FEED_URL = "https://www.google.com/m8/feeds/contacts/default/full";
    private static final String APP_NAME = "ox-appsuite";
    private static final int CHUNK_SIZE = 25;
    /**
     * The birthday {@link Date} format
     * 
     * @see <a href="https://developers.google.com/contacts/v3/reference#gcBirthday">gContact:birthday</a>
     */
    private final static String dateFormatPattern = "yyyy-MM-dd";

    /**
     * Thread local {@link SimpleDateFormat} using "yyyy-MM-dd" as pattern.
     */
    private static final ThreadLocal<SimpleDateFormat> BIRTHDAY_FORMAT = new ThreadLocal<SimpleDateFormat>() {

        @Override
        protected SimpleDateFormat initialValue() {
            SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatPattern);
            dateFormat.setTimeZone(TimeZones.UTC);
            return dateFormat;
        }
    };

    private final ContactsService googleContactsService;
    private final ServiceLookup services;

    /**
     * Initialises a new {@link GoogleContactsSubscribeService}.
     * 
     * @param oAuthServiceMetaData The {@link OAuthServiceMetaData}
     * @param services The {@link ServiceLookup}
     */
    public GoogleContactsSubscribeService(OAuthServiceMetaData oauthServiceMetadata, ServiceLookup services) {
        super(oauthServiceMetadata, KnownApi.GOOGLE.getFullName() + ".contact", FolderObject.CONTACT, "Google Contacts", services);
        this.services = services;
        googleContactsService = new ContactsService(APP_NAME);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.subscribe.SubscribeService#getContent(com.openexchange.subscribe.Subscription)
     */
    @Override
    public Collection<?> getContent(Subscription subscription) throws OXException {
        Session session = subscription.getSession();
        OAuthAccount oauthAccount = GoogleApiClients.reacquireIfExpired(session, true, getOAuthAccount(session, subscription));
        googleContactsService.setOAuth2Credentials(GoogleApiClients.getCredentials(oauthAccount, session));

        try {
            Query cQuery = new Query(new URL(FEED_URL));
            cQuery.setMaxResults(CHUNK_SIZE);
            ContactFeed feed = googleContactsService.query(cQuery, ContactFeed.class);
            if (CHUNK_SIZE > feed.getTotalResults()) {
                return parseFeed(feed);
            }

            List<Contact> firstBatch = parseFeed(feed);
            int total = feed.getTotalResults();
            int startOffset = firstBatch.size();

            FolderUpdaterRegistry folderUpdaterRegistry = services.getOptionalService(FolderUpdaterRegistry.class);
            ThreadPoolService threadPool = services.getOptionalService(ThreadPoolService.class);
            FolderUpdaterService<Contact> folderUpdater = null == folderUpdaterRegistry ? null : folderUpdaterRegistry.<Contact> getFolderUpdater(subscription);
            if (threadPool == null || folderUpdater == null) {
                return fetchInForeground(cQuery, feed, firstBatch);
            }
            scheduleInBackground(subscription, cQuery, total, startOffset, threadPool, folderUpdater);
            return firstBatch;
        } catch (IOException | ServiceException e) {
            throw new OXException(666, "cannot fetch contacts", e);
        }
    }

    private List<Contact> fetchInForeground(Query cQuery, ContactFeed feed, List<Contact> firstBatch) throws IOException, ServiceException {
        int total = feed.getTotalResults();
        int offset = firstBatch.size();

        List<Contact> contacts = new ArrayList<Contact>(total);
        contacts.addAll(firstBatch);

        while (total > offset) {
            cQuery.setStartIndex(offset);
            feed = googleContactsService.query(cQuery, ContactFeed.class);
            List<Contact> batch = parseFeed(feed);
            contacts.addAll(batch);
            offset += batch.size();
        }

        return contacts;
    }

    private void scheduleInBackground(Subscription subscription, Query cQuery, int total, int startOffset, ThreadPoolService threadPool, FolderUpdaterService<Contact> folderUpdater) {
        // Schedule task for remainder...
        threadPool.submit(new AbstractTask<Void>() {

            @Override
            public Void call() throws Exception {
                int offset = startOffset;
                while (total > offset) {
                    cQuery.setStartIndex(offset);
                    ContactFeed feed = googleContactsService.query(cQuery, ContactFeed.class);
                    List<Contact> batch = parseFeed(feed);
                    folderUpdater.save(new SearchIteratorDelegator<Contact>(batch), subscription);
                    offset += batch.size();
                }
                return null;
            }
        });
    }

    private List<Contact> parseFeed(ContactFeed feed) throws FileNotFoundException {
        List<Contact> contacts = new LinkedList<Contact>();
        for (ContactEntry contact : feed.getEntries()) {
            Contact c = new Contact();
            if (contact.hasName()) {
                Name name = contact.getName();
                if (name.hasGivenName()) {
                    GivenName given = name.getGivenName();
                    c.setGivenName(contact.getName().getGivenName().getValue());
                    if (given.hasYomi()) {
                        c.setYomiFirstName(given.getYomi());
                    }
                }
                if (name.hasFamilyName()) {
                    FamilyName familyName = name.getFamilyName();
                    c.setSurName(familyName.getValue());
                    if (familyName.hasYomi()) {
                        c.setYomiLastName(familyName.getYomi());
                    }
                }
                if (name.hasFullName()) {
                    c.setDisplayName(name.getFullName().getValue());
                }
            }

            if (contact.hasEmailAddresses()) {
                int count = 0;
                for (Email email : contact.getEmailAddresses()) {
                    switch (count++) {
                        case 0:
                            c.setEmail1(email.getAddress());
                            break;
                        case 1:
                            c.setEmail1(email.getAddress());
                            break;
                        case 2:
                            c.setEmail1(email.getAddress());
                            break;
                    }
                }
            }

            if (contact.hasBirthday()) {
                Birthday birthday = contact.getBirthday();
                try {
                    c.setBirthday(BIRTHDAY_FORMAT.get().parse(birthday.getValue()));
                } catch (ParseException e) {
                    LOG.warn("Unable to parse '{}' as a birthday.", birthday.getValue());
                }
            }

            if (contact.hasImAddresses()) {
                int count = 0;
                for (Im im : contact.getImAddresses()) {
                    switch (count++) {
                        case 0:
                            c.setInstantMessenger1(im.getAddress());
                            break;
                        case 1:
                            c.setInstantMessenger2(im.getAddress());
                    }
                }
            }
            if (contact.hasNickname()) {
                c.setNickname(contact.getNickname().getValue());
            }

            if (contact.hasOccupation()) {
                c.setProfession(contact.getOccupation().getValue());
            }

            if (contact.hasPhoneNumbers()) {
                int count = 0;
                for (PhoneNumber pn : contact.getPhoneNumbers()) {
                    if (pn.getPrimary()) {
                        c.setTelephonePrimary(pn.getPhoneNumber());
                    }
                    // Unfortunately we do not have enough information
                    // about the type of the telephone number, nor we
                    // can make an educated guess. So we simply fetching
                    // as much as possible.
                    switch (count++) {
                        case 0:
                            c.setTelephoneOther(pn.getPhoneNumber());
                            break;
                        case 1:
                            c.setTelephoneHome1(pn.getPhoneNumber());
                            break;
                        case 2:
                            c.setTelephoneHome2(pn.getPhoneNumber());
                            break;
                        case 3:
                            c.setTelephoneBusiness1(pn.getPhoneNumber());
                            break;
                        case 4:
                            c.setTelephoneBusiness2(pn.getPhoneNumber());
                            break;
                        case 5:
                            c.setTelephoneAssistant(pn.getPhoneNumber());
                            break;
                        case 6:
                            c.setTelephoneCompany(pn.getPhoneNumber());
                            break;
                        // Maybe add more?
                    }
                }
            }

            if (contact.hasPostalAddresses()) {
                int count = 0;
                for (PostalAddress pa : contact.getPostalAddresses()) {
                    if (pa.getPrimary()) {
                        c.setAddressHome(pa.getValue());
                    }
                    switch (count++) {
                        case 0:
                            c.setAddressBusiness(pa.getValue());
                            break;
                        case 1:
                            c.setAddressOther(pa.getValue());
                            break;
                    }
                }
            }

            if (contact.hasStructuredPostalAddresses()) {
                int count = 0;
                for (StructuredPostalAddress spa : contact.getStructuredPostalAddresses()) {
                    if (spa.getPrimary()) {
                        if (spa.hasFormattedAddress()) {
                            c.setAddressHome(spa.getFormattedAddress().getValue());
                        } else {
                            if (spa.hasStreet()) {
                                c.setAddressHome(spa.getStreet().getValue());
                            }
                            if (spa.hasPostcode()) {
                                c.setPostalCodeHome(spa.getPobox().getValue());
                            }
                            if (spa.hasCity()) {
                                c.setCityHome(spa.getCity().getValue());
                            }
                            if (spa.hasCountry()) {
                                c.setCountryHome(spa.getCountry().getValue());
                            }
                        }
                    } else {

                        switch (count++) {
                            case 0:
                                if (spa.hasFormattedAddress()) {
                                    c.setAddressBusiness(spa.getFormattedAddress().getValue());
                                } else {
                                    if (spa.hasStreet()) {
                                        c.setAddressBusiness(spa.getStreet().getValue());
                                    }
                                    if (spa.hasPostcode()) {
                                        c.setPostalCodeBusiness(spa.getPobox().getValue());
                                    }
                                    if (spa.hasCity()) {
                                        c.setCityBusiness(spa.getCity().getValue());
                                    }
                                    if (spa.hasCountry()) {
                                        c.setCountryBusiness(spa.getCountry().getValue());
                                    }
                                }
                                break;
                            case 1:
                                if (spa.hasFormattedAddress()) {
                                    c.setAddressOther(spa.getFormattedAddress().getValue());
                                } else {
                                    if (spa.hasStreet()) {
                                        c.setAddressOther(spa.getStreet().getValue());
                                    }
                                    if (spa.hasPostcode()) {
                                        c.setPostalCodeOther(spa.getPobox().getValue());
                                    }
                                    if (spa.hasCity()) {
                                        c.setCityOther(spa.getCity().getValue());
                                    }
                                    if (spa.hasCountry()) {
                                        c.setCountryOther(spa.getCountry().getValue());
                                    }
                                }
                                break;
                        }
                    }
                }
            }
            Link photoLink = contact.getContactPhotoLink();
            if (photoLink != null) {
                GDataRequest request = null;
                InputStream resultStream = null;
                ByteArrayOutputStream out = null;
                try {
                    request = googleContactsService.createLinkQueryRequest(photoLink);
                    request.execute();
                    resultStream = request.getResponseStream();
                    out = new ByteArrayOutputStream();
                    int read = 0;
                    byte[] buffer = new byte[4096];
                    while ((read = resultStream.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                    c.setImage1(out.toByteArray());
                    c.setImageContentType(photoLink.getType());
                } catch (IOException | ServiceException e) {
                    LOG.debug("Error fetching contact's image from '{}'", photoLink.getHref());
                } finally {
                    if (request != null) {
                        request.end();
                    }
                    IOUtils.closeQuietly(resultStream);
                    IOUtils.closeQuietly(out);
                }
            }
            contacts.add(c);
        }
        return contacts;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.subscribe.oauth.AbstractOAuthSubscribeService#getKnownApi()
     */
    @Override
    protected KnownApi getKnownApi() {
        return KnownApi.GOOGLE;
    }
}
