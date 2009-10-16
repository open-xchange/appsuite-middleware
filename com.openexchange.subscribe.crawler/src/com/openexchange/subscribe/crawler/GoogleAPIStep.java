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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.gargoylesoftware.htmlunit.WebClient;
import com.google.gdata.client.Service;
import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.Link;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ContactFeed;
import com.google.gdata.data.extensions.Email;
import com.google.gdata.data.extensions.Organization;
import com.google.gdata.data.extensions.PhoneNumber;
import com.google.gdata.data.extensions.Im;
import com.google.gdata.data.extensions.StructuredPostalAddress;
import com.google.gdata.util.ServiceException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.subscribe.SubscriptionException;

/**
 * {@link GoogleAPIStep}
 * 
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class GoogleAPIStep extends AbstractStep<Contact[], Object> implements LoginStep {

    private String username, password;
    
    private static final Log LOG = LogFactory.getLog(GoogleAPIStep.class);

    public GoogleAPIStep() {
    }

    public GoogleAPIStep(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void execute(WebClient webClient) throws SubscriptionException {

        List<Contact> contacts = new ArrayList<Contact>();

        // Request the feed
        URL feedUrl;
        try {
            ContactsService myService = new ContactsService("com.openexchange");
            myService.setUserCredentials(username, password);
            feedUrl = new URL("http://www.google.com/m8/feeds/contacts/" + username + "/full");
            ContactFeed resultFeed = myService.getFeed(feedUrl, ContactFeed.class);

            for (int i = 0; i < resultFeed.getEntries().size(); i++) {
                Contact contact = new Contact();
                ContactEntry entry = resultFeed.getEntries().get(i);
                if (entry.hasName()) {
                    com.google.gdata.data.extensions.Name name = entry.getName();
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
                
                for (Organization o : entry.getOrganizations()) {
                    contact.setCompany(o.getOrgName().getValue());
                }

                for (Email email : entry.getEmailAddresses()) {
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

                for (PhoneNumber pn : entry.getPhoneNumbers()) {
                    if (pn.getRel() != null) {
                        if (pn.getRel().endsWith("work")) {
                            contact.setTelephoneBusiness1(pn.getPhoneNumber());
                        } else if (pn.getRel().endsWith("home")) {
                            contact.setTelephoneHome1(pn.getPhoneNumber());
                        } else if (pn.getRel().endsWith("other")) {
                            contact.setTelephoneOther(pn.getPhoneNumber());
                        } else if (pn.getRel().endsWith("work_fax")) {
                            contact.setFaxBusiness(pn.getPhoneNumber());
                        } else if (pn.getRel().endsWith("home_fax")) {
                            contact.setFaxHome(pn.getPhoneNumber());
                        } else if (pn.getRel().endsWith("mobile")) {
                            contact.setCellularTelephone1(pn.getPhoneNumber());
                        }
                    }
                }
                
                if (entry.getBirthday() != null){
                    String birthday = entry.getBirthday().getValue();
                    String regex = "([0-9]{4})\\-([0-9]{2})\\-([0-9]{2})";        
                    if (birthday.matches(regex)){
                        Pattern pattern = Pattern.compile(regex);
                        Matcher matcher = pattern.matcher(birthday);
                        if (matcher.matches() && matcher.groupCount() == 3){
                            int year = Integer.valueOf(matcher.group(1));
                            int month = Integer.valueOf(matcher.group(2));
                            int day = Integer.valueOf(matcher.group(3));
                            Calendar cal = Calendar.getInstance();
                            cal.clear();
                            cal.set(year, month, day);
                            contact.setBirthday(cal.getTime());
                        }
                    }
                }
                for (StructuredPostalAddress pa :entry.getStructuredPostalAddresses()){
                    if (pa.getRel() != null){
                        if (pa.getRel().endsWith("work")){                            
                            if (pa.getStreet() != null) contact.setStreetBusiness(pa.getStreet().getValue());
                            if (pa.getPostcode() != null) contact.setPostalCodeBusiness(pa.getPostcode().getValue());
                            if (pa.getCity() != null) contact.setCityBusiness(pa.getCity().getValue());
                            if (pa.getCountry() != null) contact.setCountryBusiness(pa.getCountry().getValue());
                            //TODO: This will be used to write the address to the contacts note-field if the data is not structured
                            //System.out.println("***** "+"Work:\n"+pa.getFormattedAddress().getValue()+"\n");
                        }
                        if (pa.getRel().endsWith("home")){
                            if (pa.getStreet() != null) contact.setStreetHome(pa.getStreet().getValue());
                            if (pa.getPostcode() != null) contact.setPostalCodeHome(pa.getPostcode().getValue());
                            if (pa.getCity() != null) contact.setCityHome(pa.getCity().getValue());
                            if (pa.getCountry() != null) contact.setCountryHome(pa.getCountry().getValue());
                        }
                        if (pa.getRel().endsWith("other")){
                            if (pa.getStreet() != null) contact.setStreetOther(pa.getStreet().getValue());
                            if (pa.getPostcode() != null) contact.setPostalCodeOther(pa.getPostcode().getValue());
                            if (pa.getCity() != null) contact.setCityOther(pa.getCity().getValue());
                            if (pa.getCountry() != null) contact.setCountryOther(pa.getCountry().getValue());
                        }
                    }
                }   
                for (Im im : entry.getImAddresses()) {
                    if (im.getProtocol() != null) {
                        String regex = "[^#]*#([a-zA-ZŠšŸ€…†]*)";
                        Pattern pattern = Pattern.compile(regex);
                        Matcher matcher = pattern.matcher(im.getProtocol());
                        if (matcher.matches()){
                            contact.setInstantMessenger1(im.getAddress()+" ("+matcher.group(1)+")");
                        }
                    }
                    

                }

                if (entry.getContactPhotoLink() != null && entry.getContactPhotoLink().getEtag() != null){
                    Link photoLink = entry.getContactPhotoLink();
                    Service.GDataRequest request = myService.createLinkQueryRequest(photoLink);
                    request.execute();
                    InputStream in = request.getResponseStream();
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    byte[] buffer = new byte[4096];
                    for (int read = 0; (read = in.read(buffer)) != -1; 
                        out.write(buffer, 0, read)) {}
                    contact.setImage1(out.toByteArray());
                    contact.setImageContentType("image/jpeg");
                }

                contacts.add(contact);
            }
        } catch (MalformedURLException e) {
            LOG.error(e);
        } catch (IOException e) {
            LOG.error(e);
        } catch (ServiceException e) {
            LOG.error(e);
        }

        output = new Contact[contacts.size()];
        for (int i = 0; i < output.length && i < contacts.size(); i++) {
            output[i] = contacts.get(i);
        }
        executedSuccessfully = true;

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBaseUrl() {
        return "";
    }

}
