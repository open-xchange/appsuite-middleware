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
package com.openexchange.oauth.linkedin;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.tools.versit.converter.ConverterException;
import com.openexchange.tools.versit.converter.OXContainerConverter;

public class LinkedInXMLParser {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(LinkedInXMLParser.class));

    private String getTextValue(Element ele, String tagName) {
        String textVal = null;
        NodeList nl = ele.getElementsByTagName(tagName);
        if (nl != null && nl.getLength() > 0) {
            Element el = (Element) nl.item(0);
            if (null != el.getFirstChild()){
                textVal = el.getFirstChild().getNodeValue();
            }
        }
        return textVal;
    }

	public Contact parse(Element person){
        Contact contact = new Contact();
        contact.setGivenName(getTextValue(person, "first-name"));
        contact.setSurName(getTextValue(person, "last-name"));
        if (null != getTextValue(person, "main-address")) {
            contact.setNote(getTextValue(person, "main-address"));
        }
        try {
            String imageUrl = getTextValue(person, "picture-url");
            if (null != imageUrl) {
                OXContainerConverter.loadImageFromURL(contact, imageUrl);
            }
        } catch (ConverterException e) {
            LOG.error(e);
        }

        // get the current job and company
        NodeList positions = person.getElementsByTagName("positions");
        if (null != positions && positions.getLength() > 0) {
            Element position = (Element) positions.item(0);
            contact.setTitle(getTextValue(position, "title"));
            NodeList companies = position.getElementsByTagName("company");
            if (companies != null && companies.getLength() > 0) {
                Element company = (Element) companies.item(0);
                contact.setCompany(getTextValue(company, "name"));
            }
        }

        // get the first IM-account
        NodeList imAccounts = person.getElementsByTagName("im-account");
        if (null != imAccounts && imAccounts.getLength() > 0 ){
            Element imAccount = (Element) imAccounts.item(0);
            contact.setInstantMessenger1(getTextValue(imAccount, "im-account-name") + " ("+getTextValue(imAccount, "im-account-type")+")");
        }

        // parse the phone numbers, saving the first occurrence of "home" and "work"
        NodeList phoneNumbers = person.getElementsByTagName("phone-number");
        if (null != phoneNumbers && phoneNumbers.getLength() > 0 ){
            for (int p = 0; p < phoneNumbers.getLength(); p++){
                Element phoneNumber = (Element) phoneNumbers.item(p);
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
        NodeList dateOfBirths = person.getElementsByTagName("date-of-birth");
        if (null != dateOfBirths && dateOfBirths.getLength() > 0){
            Element dateOfBirth = (Element) dateOfBirths.item(0);
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

    public List<Contact> parseConnections(String body) throws OXException {
        final List<Contact> contacts = new ArrayList<Contact>();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(body.getBytes(com.openexchange.java.Charsets.UTF_8)));
            Element root = doc.getDocumentElement();
            NodeList connections = root.getElementsByTagName("person");
            if (connections != null && connections.getLength() > 0) {
                for (int i = 0; i < connections.getLength(); i++) {
                    Element person = (Element) connections.item(i);
                    Contact contact = parse(person);
                    contacts.add(contact);
                }
            }
            
            NodeList errors = root.getElementsByTagName("error");
            if (errors.getLength() > 0) {
            	Element error = (Element) errors.item(0);
            	String message = error.getElementsByTagName("message").item(0).getTextContent();
            	throw OXException.general(message);
            }
        } catch (ParserConfigurationException pce) {
            LOG.error(pce);
        } catch (SAXException se) {
            LOG.error(se);
        } catch (IOException ioe) {
            LOG.error(ioe);
        }
        return contacts;
    }

    public Contact parseProfile(String body) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(body.getBytes(com.openexchange.java.Charsets.UTF_8)));
            Element root = doc.getDocumentElement();
            Contact contact = parse(root);
            return contact;
        } catch (ParserConfigurationException pce) {
            LOG.error(pce);
        } catch (SAXException se) {
            LOG.error(se);
        } catch (IOException ioe) {
            LOG.error(ioe);
        }
        return null;
    }
}
