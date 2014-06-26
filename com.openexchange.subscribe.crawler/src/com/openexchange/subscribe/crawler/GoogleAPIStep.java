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

package com.openexchange.subscribe.crawler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.internet.AddressException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.google.gdata.client.GoogleService.InvalidCredentialsException;
import com.google.gdata.client.Service;
import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.Link;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ContactFeed;
import com.google.gdata.data.extensions.Email;
import com.google.gdata.data.extensions.Im;
import com.google.gdata.data.extensions.Organization;
import com.google.gdata.data.extensions.PhoneNumber;
import com.google.gdata.data.extensions.StructuredPostalAddress;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Strings;
import com.openexchange.java.util.TimeZones;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.subscribe.SubscriptionErrorMessage;
import com.openexchange.subscribe.crawler.internal.AbstractStep;
import com.openexchange.subscribe.crawler.internal.LoginStep;

/**
 * {@link GoogleAPIStep}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class GoogleAPIStep extends AbstractStep<Contact[], Object> implements LoginStep {

    private String username, password;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GoogleAPIStep.class);

    public GoogleAPIStep() {
        super();
    }

    public GoogleAPIStep(final String username, final String password) {
        this();
        this.username = username;
        this.password = password;
    }

    @Override
    public void execute(final WebClient webClient) throws OXException {
        // Request the feed
        URL feedUrl;
        try {
            // Check login to be an E-Mail address
            try {
                new QuotedInternetAddress(username, true);
            } catch (final AddressException e) {
                throw SubscriptionErrorMessage.EMAIL_ADDR_LOGIN.create();
            }
            // Go ahead...
            final ContactsService myService = new ContactsService("com.openexchange");
            myService.setUserCredentials(username, password);
            feedUrl = new URL("https://www.google.com/m8/feeds/contacts/" + username + "/full?max-results=5000");
            exchangeURLStreamHandler(feedUrl);

            final List<ContactEntry> entries = myService.getFeed(feedUrl, ContactFeed.class).getEntries();
            final int size = entries.size();

            final List<Contact> contacts = new ArrayList<Contact>(size);
            boolean overQuotaEncountered = false;

            // Iterate one-by-one
            for (int i = 0; i < size; i++) {
                try {
                    final Contact contact = new Contact();
                    final ContactEntry entry = entries.get(i);
                    if (entry.hasName()) {
                        final com.google.gdata.data.extensions.Name name = entry.getName();
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

                    final Link contactPhotoLink = entry.getContactPhotoLink();
                    if (contactPhotoLink != null && contactPhotoLink.getEtag() != null) {
                        Service.GDataRequest request = null;
                        InputStream in = null;
                        try {
                            request = myService.createLinkQueryRequest(contactPhotoLink);
                            request.execute();
                            in = request.getResponseStream();
                            final ByteArrayOutputStream out = new ByteArrayOutputStream();
                            final byte[] buffer = new byte[4096];
                            for (int read = 0; (read = in.read(buffer)) > 0;) {
                                out.write(buffer, 0, read);
                            }
                            contact.setImage1(out.toByteArray());
                            contact.setImageContentType("image/jpeg");
                        } catch (final ServiceException e) {
                            final String lcm = Strings.toLowerCase(e.getMessage());
                            if (lcm.indexOf("temporary problem - please try again later and consider using batch operations.") > 0) {
                                if (!overQuotaEncountered) {
                                    LOG.warn("Unable to load more Google contact images due to quota restrictions.", e);
                                    overQuotaEncountered = true;
                                }
                            } else {
                                LOG.warn("Error while trying to load Google contact's image", e);
                            }
                        } finally {
                            if (null != in) {
                                try {
                                    in.close();
                                } catch (final Exception e) {
                                    // Ignore
                                }
                            }
                            if (null != request) {
                                try {
                                    request.end();
                                } catch (final Exception e) {
                                    // Ignore
                                }
                            }
                        }
                    }

                    contacts.add(contact);
                } catch (final NullPointerException e) {
                    LOG.error(e.toString());
                }
            }

            output = new Contact[contacts.size()];
            for (int i = 0; i < output.length && i < contacts.size(); i++) {
                output[i] = contacts.get(i);
            }
            executedSuccessfully = true;

        } catch (final MalformedURLException e) {
            LOG.error(e.toString());
            LOG.error("User with id={} and context={} failed to subscribe source={} with display_name={}", workflow.getSubscription().getUserId(), workflow.getSubscription().getContext(), workflow.getSubscription().getSource().getDisplayName(), workflow.getSubscription().getDisplayName());
            throw SubscriptionErrorMessage.COMMUNICATION_PROBLEM.create();
        } catch (final IOException e) {
            LOG.error(e.toString());
            LOG.error("User with id={} and context={} failed to subscribe source={} with display_name={}", workflow.getSubscription().getUserId(), workflow.getSubscription().getContext(), workflow.getSubscription().getSource().getDisplayName(), workflow.getSubscription().getDisplayName());
            throw SubscriptionErrorMessage.COMMUNICATION_PROBLEM.create();
        } catch (final InvalidCredentialsException e) {
            LOG.error("User with id={} and context={} failed to subscribe source={} with display_name={}", workflow.getSubscription().getUserId(), workflow.getSubscription().getContext(), workflow.getSubscription().getSource().getDisplayName(), workflow.getSubscription().getDisplayName());
            throw SubscriptionErrorMessage.INVALID_LOGIN.create();
        } catch (final AuthenticationException e) {
            LOG.error("User with id={} and context={} failed to subscribe source={} with display_name={}", workflow.getSubscription().getUserId(), workflow.getSubscription().getContext(), workflow.getSubscription().getSource().getDisplayName(), workflow.getSubscription().getDisplayName());
            throw SubscriptionErrorMessage.INVALID_LOGIN.create();
        } catch (final ServiceException e) {
            LOG.error("", e);
            LOG.error("User with id={} and context={} failed to subscribe source={} with display_name={}", workflow.getSubscription().getUserId(), workflow.getSubscription().getContext(), workflow.getSubscription().getSource().getDisplayName(), workflow.getSubscription().getDisplayName());
            throw SubscriptionErrorMessage.COMMUNICATION_PROBLEM.create();
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

    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(final String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(final String password) {
        this.password = password;
    }

    @Override
    public String getBaseUrl() {
        return "";
    }

}
