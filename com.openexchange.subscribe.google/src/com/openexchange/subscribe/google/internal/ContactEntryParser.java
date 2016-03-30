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

package com.openexchange.subscribe.google.internal;

import java.util.Calendar;
import java.util.PriorityQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.internet.AddressException;
import org.slf4j.Logger;
import com.google.gdata.data.Content;
import com.google.gdata.data.IContent.Type;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.extensions.Email;
import com.google.gdata.data.extensions.FamilyName;
import com.google.gdata.data.extensions.GivenName;
import com.google.gdata.data.extensions.Im;
import com.google.gdata.data.extensions.Name;
import com.google.gdata.data.extensions.OrgName;
import com.google.gdata.data.extensions.Organization;
import com.google.gdata.data.extensions.PhoneNumber;
import com.google.gdata.data.extensions.StructuredPostalAddress;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Strings;
import com.openexchange.java.util.TimeZones;
import com.openexchange.mail.mime.QuotedInternetAddress;

/**
 * {@link ContactParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class ContactEntryParser {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ContactEntryParser.class);

    /**
     * Initializes a new {@link ContactParser}.
     */
    public ContactEntryParser() {
        super();
    }

    public void parseContact(final ContactEntry entry, final Contact contact) {
        if (entry.hasName()) {
            Name name = entry.getName();
            if (name.hasFullName()) {
                contact.setDisplayName(name.getFullName().getValue());
            }
            if (name.hasNamePrefix()) {
                contact.setTitle(name.getNamePrefix().getValue());
            }
            if (name.hasGivenName()) {
                GivenName givenName = name.getGivenName();
                contact.setGivenName(givenName.getValue());
                if(false == Strings.isEmpty(givenName.getYomi())) {
                    contact.setYomiFirstName(givenName.getYomi());
                }
            }
            if (name.hasAdditionalName()) {
                contact.setMiddleName(name.getAdditionalName().getValue());
            }
            if (name.hasFamilyName()) {
                FamilyName surName = name.getFamilyName();
                contact.setSurName(surName.getValue());
                if(false == Strings.isEmpty(surName.getYomi())) {
                    contact.setYomiLastName(surName.getYomi());
                }
            }
            if (name.hasNameSuffix()) {
                contact.setSuffix(name.getNameSuffix().getValue());
            }
        }

        if (entry.hasOrganizations()) {
            for (final Organization o : entry.getOrganizations()) {
                if (o.hasOrgName()) {
                    OrgName company = o.getOrgName();
                    contact.setCompany(company.getValue());
                    if(false == Strings.isEmpty(company.getYomi())) {
                        contact.setYomiCompany(company.getYomi());
                    }
                }
                if(o.hasOrgTitle()) {
                    contact.setPosition(o.getOrgTitle().getValue());
                }
            }
        }

        if(entry.hasNickname()) {
            contact.setNickname(entry.getNickname().getValue());
        }

        if(entry.getContent() != null) {
            Content content = entry.getContent();
            if(content.getType() == Type.TEXT) {
                if(false == Strings.isEmpty(entry.getPlainTextContent())) {
                    contact.setNote(entry.getPlainTextContent());
                }
            }
        }

        PriorityQueue<Emails> pqEmails = new PreventDuplicatesPriorityQueue<Emails>();

        if (entry.hasEmailAddresses()) {
            for (final Email email : entry.getEmailAddresses()) {
                String contactsMail = email.getAddress();
                if(isValidMailAddress(contactsMail)) {
                    if (email.getRel() != null) {
                        String relType = email.getRel();
                        if (email.getPrimary()) {
                            pqEmails.add(new Emails(contactsMail, 10));
                        } else if (relType.endsWith("work")) {
                            pqEmails.add(new Emails(contactsMail, 9));
                        } else if (relType.endsWith("home")) {
                            pqEmails.add(new Emails(contactsMail, 8));
                        } else if (relType.endsWith("other")) {
                            pqEmails.add(new Emails(contactsMail, 7));
                        }
                    }
                    // if there are other user tagged mail addresses add them with low priority
                    else {
                        pqEmails.add(new Emails(contactsMail, 6));
                    }
                }
            }
        }

        //mapping of mails
        if(pqEmails.peek() != null) {
            contact.setEmail1(pqEmails.poll().getEmail());
        }
        if(pqEmails.peek() != null) {
            contact.setEmail2(pqEmails.poll().getEmail());
        }
        if(pqEmails.peek() != null) {
            contact.setEmail3(pqEmails.poll().getEmail());
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
    }

    /**
     * Sets the birthday for the contact based on the google information
     *
     * @param contact - the {@link Contact} to set the birthday for
     * @param birthday - the string the birthday is included in
     */
    private void setBirthday(Contact contact, String birthday) {
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

    /**
     * This method checks if an address contains invalid characters
     *
     * @param address The address string to check
     */
    private final boolean isValidMailAddress(final String address)  {
        if (null != address) {
            try {
                new QuotedInternetAddress(address, true);
                return true;
            } catch (final AddressException e) {
                return false;
            }
        }
        return false;
    }

    private class Emails implements Comparable<Emails> {
        private String emailAddress;
        private int priority;

        public Emails(String emailAddress, int priority) {
            if(emailAddress == null) {
                throw new IllegalStateException("Parameter emailAddress can't be null");
            }
            this.emailAddress = emailAddress;
            this.priority = priority;
        }

        public String getEmail() {
            return emailAddress;
        }

        public int getPriority() {
            return priority;
        }

        @Override
        public boolean equals(Object obj) {
            Emails o1 = (Emails) obj;
            if(o1.getEmail().equals(this.getEmail())) {
                return true;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return emailAddress.hashCode();
        }

        @Override
        public int compareTo(Emails o) {
            return o.getPriority() - this.getPriority();
        }
    }

    /**
     * Prevent the adding of duplicates in an priority queue
     **/
    private class PreventDuplicatesPriorityQueue<T> extends PriorityQueue<T> {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean add(T e){
            if(!super.contains(e)) {
                return super.add(e);
            }
            return false;
        }
    }
}
