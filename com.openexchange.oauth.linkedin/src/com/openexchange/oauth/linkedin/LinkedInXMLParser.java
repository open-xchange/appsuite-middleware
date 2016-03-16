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
package com.openexchange.oauth.linkedin;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.subscribe.helpers.HTTPToolkit;

public class LinkedInXMLParser {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LinkedInXMLParser.class);

    private String getTextValue(final Element ele, final String tagName) {
        String textVal = null;
        final NodeList nl = ele.getElementsByTagName(tagName);
        if (nl != null && nl.getLength() > 0) {
            final Element el = (Element) nl.item(0);
            if (null != el.getFirstChild()){
                textVal = el.getFirstChild().getNodeValue();
            }
        }
        return textVal;
    }

	public Contact parse(final Element person){
        final Contact contact = new Contact();
        contact.setGivenName(getTextValue(person, "first-name"));
        contact.setSurName(getTextValue(person, "last-name"));
        if (null != getTextValue(person, "main-address")) {
            contact.setNote(getTextValue(person, "main-address"));
        }
        try {
            final String imageUrl = getTextValue(person, "picture-url");
            if (null != imageUrl) {
                HTTPToolkit.loadImageFromURL(contact, imageUrl);
            }
        } catch (final OXException e) {
            LOG.error("", e);
        }

        // get the current job and company
        final NodeList positions = person.getElementsByTagName("positions");
        if (null != positions && positions.getLength() > 0) {
            final Element position = (Element) positions.item(0);
            contact.setPosition(getTextValue(position, "title"));
            final NodeList companies = position.getElementsByTagName("company");
            if (companies != null && companies.getLength() > 0) {
                final Element company = (Element) companies.item(0);
                contact.setCompany(getTextValue(company, "name"));
            }
        }

        // get the first IM-account
        final NodeList imAccounts = person.getElementsByTagName("im-account");
        if (null != imAccounts && imAccounts.getLength() > 0 ){
            final Element imAccount = (Element) imAccounts.item(0);
            contact.setInstantMessenger1(getTextValue(imAccount, "im-account-name") + " ("+getTextValue(imAccount, "im-account-type")+")");
        }

        // parse the phone numbers, saving the first occurrence of "home" and "work"
        final NodeList phoneNumbers = person.getElementsByTagName("phone-number");
        if (null != phoneNumbers && phoneNumbers.getLength() > 0 ){
            for (int p = 0; p < phoneNumbers.getLength(); p++){
                final Element phoneNumber = (Element) phoneNumbers.item(p);
                if (null != getTextValue(phoneNumber, "phone-type")){
                    if (getTextValue(phoneNumber, "phone-type").equals("work")) {
                        contact.setTelephoneBusiness1(getTextValue(phoneNumber, "phone-number"));
                    }
                    else if (getTextValue(phoneNumber, "phone-type").equals("home")){
                        contact.setTelephoneHome1(getTextValue(phoneNumber, "phone-number"));
                    }
                }
            }
        }

        // get the birthdate
        final NodeList dateOfBirths = person.getElementsByTagName("date-of-birth");
        if (null != dateOfBirths && dateOfBirths.getLength() > 0){
            final Element dateOfBirth = (Element) dateOfBirths.item(0);
            int year = 0;
            if (null != getTextValue(dateOfBirth, "year")){
                year = Integer.parseInt(getTextValue(dateOfBirth, "year")) - 1900;
            }
            int month = 0;
            if (null != getTextValue(dateOfBirth, "month")){
                month = Integer.parseInt(getTextValue(dateOfBirth, "month")) -1;
            }
            int date = 0;
            if (null != getTextValue(dateOfBirth, "day")){
                date = Integer.parseInt(getTextValue(dateOfBirth, "day"));
            }
            contact.setBirthday(new Date(year, month, date));
        }
        return contact;
    }

    public List<Contact> parseConnections(final String body) throws OXException {
        final List<Contact> contacts = new ArrayList<Contact>();
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            final DocumentBuilder db = dbf.newDocumentBuilder();
            final Document doc = db.parse(new ByteArrayInputStream(body.getBytes(com.openexchange.java.Charsets.UTF_8)));
            final Element root = doc.getDocumentElement();
            final NodeList connections = root.getElementsByTagName("person");
            if (connections != null && connections.getLength() > 0) {
                for (int i = 0; i < connections.getLength(); i++) {
                    final Element person = (Element) connections.item(i);
                    final Contact contact = parse(person);
                    contacts.add(contact);
                }
            }

            final NodeList errors = root.getElementsByTagName("error");
            if (errors.getLength() > 0) {
            	final Element error = (Element) errors.item(0);
            	final String message = error.getElementsByTagName("message").item(0).getTextContent();
            	throw OXException.general(message);
            }
        } catch (final ParserConfigurationException pce) {
            LOG.error("", pce);
        } catch (final SAXException se) {
            LOG.error("", se);
        } catch (final IOException ioe) {
            LOG.error("", ioe);
        }
        return contacts;
    }

    public Contact parseProfile(final String body) {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            final DocumentBuilder db = dbf.newDocumentBuilder();
            final Document doc = db.parse(new ByteArrayInputStream(body.getBytes(com.openexchange.java.Charsets.UTF_8)));
            final Element root = doc.getDocumentElement();
            final Contact contact = parse(root);
            return contact;
        } catch (final ParserConfigurationException pce) {
            LOG.error("", pce);
        } catch (final SAXException se) {
            LOG.error("", se);
        } catch (final IOException ioe) {
            LOG.error("", ioe);
        }
        return null;
    }

    // ---------------------------------- JSON STUFF --------------------------------- //

    public Contact parse(final JSONObject person) throws OXException {
        try {
            final Contact contact = new Contact();
            if (person.hasAndNotNull("id")) {
                contact.setUserField20(person.getString("id"));
            }
            contact.setGivenName(person.optString("firstName", null));
            contact.setSurName(person.optString("lastName", null));
            final String mainAddress = person.optString("mainAddress", null);
            if (null != mainAddress) {
                contact.setNote(mainAddress);
            }
            try {
                final String imageUrl = person.optString("pictureUrl", null);
                if (null != imageUrl) {
                    HTTPToolkit.loadImageFromURL(contact, imageUrl);
                }
            } catch (final OXException e) {
                LOG.error("", e);
            }

            // get the current job and company
            {
                final JSONObject positions = person.optJSONObject("positions");
                if (null != positions && positions.optInt("_total", 0) > 0) {
                    final JSONArray ja = positions.optJSONArray("values");
                    final int length = ja.length();
                    JSONObject candidate = null;
                    for (int i = 0; i < length; i++) {
                        final JSONObject position = ja.optJSONObject(i);
                        if (position.optBoolean("isCurrent", false)) {
                            contact.setPosition(position.optString("title", null));
                            final JSONObject company = position.optJSONObject("company");
                            if (null != company) {
                                contact.setCompany(company.optString("name", null));
                                candidate = null;
                                i = length;
                            }
                        } else {
                            candidate = position;
                        }
                    }
                    if (null != candidate) {
                        contact.setPosition(candidate.optString("title", null));
                        final JSONObject company = candidate.optJSONObject("company");
                        if (null != company) {
                            contact.setCompany(company.optString("name", null));
                        }
                    }
                }
            }
            // get the first IM-account
            {
                final JSONObject imAccounts = person.optJSONObject("imAccounts");
                if (null != imAccounts && imAccounts.optInt("_total", 0) > 0) {
                    final JSONArray ja = imAccounts.optJSONArray("values");
                    final JSONObject imAccount = ja.optJSONObject(0);
                    contact.setInstantMessenger1(imAccount.optString("imAccountName", null) + " (" + imAccount.optString("im-account-type", null) + ")");
                }
            }
            // parse the phone numbers, saving the first occurrence of "home" and "work"
            {
                final JSONObject phoneNumbers = person.optJSONObject("phoneNumbers");
                if (null != phoneNumbers && phoneNumbers.optInt("_total", 0) > 0) {
                    final JSONArray ja = phoneNumbers.optJSONArray("values");
                    final int length = ja.length();
                    for (int i = 0; i < length; i++) {
                        final JSONObject phoneNumber = ja.optJSONObject(i);
                        final String phoneType = phoneNumber.optString("phoneType", null);
                        if ("work".equals(phoneType)) {
                            contact.setTelephoneBusiness1(phoneNumber.optString("phoneNumber", null));
                        } else if ("home".equals(phoneType)) {
                            contact.setTelephoneHome1(phoneNumber.optString("phoneNumber", null));
                        }
                    }
                }
            }
            // get the birthdate
            {
                final JSONObject dateOfBirth = person.optJSONObject("dateOfBirth");
                if (null != dateOfBirth) {
                    int year = 0;
                    final String sYear = dateOfBirth.optString("year", null);
                    if (null != sYear) {
                        year = Integer.parseInt(sYear) - 1900;
                    }
                    int month = 0;
                    final String sMonth = dateOfBirth.optString("month", null);
                    if (null != sMonth) {
                        month = Integer.parseInt(sMonth) - 1;
                    }
                    int date = 0;
                    final String sDay = dateOfBirth.optString("day", null);
                    if (null != sDay) {
                        date = Integer.parseInt(sDay);
                    }
                    contact.setBirthday(new Date(year, month, date));
                }
            }
            return contact;
        } catch (final JSONException e) {
            throw OAuthExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Parses given connections.
     *
     * <pre>
     * {
     *   "values": [
     *     {
     *       "_key": "~",
     *       "connections": {"_total": 129},
     *       "firstName": "Adam"
     *     },
     *     {
     *       "_key": "hks0NPUMZF",
     *       "connections": {"_total": 500},
     *       "firstName": "Brandon"
     *     }
     *   ],
     *   "_total": 2
     * }
     * </pre>
     *
     * @param body The received JSON body
     * @return The parsed contacts
     * @throws OXException If operation fails
     */
    public List<Contact> parseConnections(final JSONValue body) throws OXException {
        final JSONArray persons;
        if (body.isObject()) {
            persons = body.toObject().optJSONArray("values");
        } else {
            persons = body.toArray();
        }
        if (null == persons) {
            return Collections.emptyList();
        }
        final int length = persons.length();
        final List<Contact> contacts = new ArrayList<Contact>(length);
        for (int i = 0; i < length; i++) {
            try {
                contacts.add(parse(persons.optJSONObject(i)));
            } catch (final RuntimeException e) {
                // Ignore
                LOG.warn("Runtime error occurred", e);
            }
        }
        return contacts;
    }

}
