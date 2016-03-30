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

package com.openexchange.webdav.xml;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.Text;
import org.jdom2.output.XMLOutputter;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.SortOptions;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.Generic;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.encoding.Base64;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.webdav.xml.fields.CommonFields;
import com.openexchange.webdav.xml.fields.ContactFields;
import com.openexchange.webdav.xml.fields.DataFields;
import com.openexchange.webdav.xml.fields.FolderChildFields;

/**
 * ContactWriter
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */

public class ContactWriter extends CommonWriter {

    protected final static ContactField[] changeFields = {
    	ContactField.OBJECT_ID, ContactField.CREATED_BY, ContactField.CREATION_DATE, ContactField.LAST_MODIFIED, ContactField.MODIFIED_BY,
    	ContactField.FOLDER_ID, ContactField.PRIVATE_FLAG, ContactField.CATEGORIES, ContactField.GIVEN_NAME, ContactField.SUR_NAME,
        ContactField.ANNIVERSARY, ContactField.ASSISTANT_NAME, ContactField.BIRTHDAY, ContactField.BRANCHES, ContactField.BUSINESS_CATEGORY, ContactField.CATEGORIES,
        ContactField.CELLULAR_TELEPHONE1, ContactField.CELLULAR_TELEPHONE2, ContactField.CITY_BUSINESS, ContactField.CITY_HOME, ContactField.CITY_OTHER,
        ContactField.COMMERCIAL_REGISTER, ContactField.COMPANY, ContactField.COUNTRY_BUSINESS, ContactField.COUNTRY_HOME, ContactField.COUNTRY_OTHER,
        ContactField.DEPARTMENT, ContactField.DISPLAY_NAME, ContactField.DISTRIBUTIONLIST, ContactField.EMAIL1, ContactField.EMAIL2, ContactField.EMAIL3,
        ContactField.EMPLOYEE_TYPE, ContactField.FAX_BUSINESS, ContactField.FAX_HOME, ContactField.FAX_OTHER, ContactField.FILE_AS, ContactField.FOLDER_ID,
        ContactField.GIVEN_NAME, ContactField.IMAGE1, ContactField.IMAGE1_CONTENT_TYPE, ContactField.INFO, ContactField.INSTANT_MESSENGER1,
        ContactField.INSTANT_MESSENGER2, ContactField.MANAGER_NAME, ContactField.MARITAL_STATUS, ContactField.MIDDLE_NAME, ContactField.NICKNAME,
        ContactField.NOTE, ContactField.NUMBER_OF_CHILDREN, ContactField.NUMBER_OF_EMPLOYEE, ContactField.POSITION, ContactField.POSTAL_CODE_BUSINESS,
        ContactField.POSTAL_CODE_HOME, ContactField.POSTAL_CODE_OTHER, ContactField.PRIVATE_FLAG, ContactField.PROFESSION, ContactField.ROOM_NUMBER,
        ContactField.SALES_VOLUME, ContactField.SPOUSE_NAME, ContactField.STATE_BUSINESS, ContactField.STATE_HOME, ContactField.STATE_OTHER,
        ContactField.STREET_BUSINESS, ContactField.STREET_HOME, ContactField.STREET_OTHER, ContactField.SUFFIX, ContactField.TAX_ID, ContactField.TELEPHONE_ASSISTANT,
        ContactField.TELEPHONE_BUSINESS1, ContactField.TELEPHONE_BUSINESS2, ContactField.TELEPHONE_CALLBACK, ContactField.TELEPHONE_CAR,
        ContactField.TELEPHONE_COMPANY, ContactField.TELEPHONE_HOME1, ContactField.TELEPHONE_HOME2, ContactField.TELEPHONE_IP, ContactField.TELEPHONE_ISDN,
        ContactField.TELEPHONE_OTHER, ContactField.TELEPHONE_PAGER, ContactField.TELEPHONE_PRIMARY, ContactField.TELEPHONE_RADIO, ContactField.TELEPHONE_TELEX,
        ContactField.TELEPHONE_TTYTDD, ContactField.TITLE, ContactField.URL, ContactField.USERFIELD01, ContactField.USERFIELD02, ContactField.USERFIELD03,
        ContactField.USERFIELD04, ContactField.USERFIELD05, ContactField.USERFIELD06, ContactField.USERFIELD07, ContactField.USERFIELD08, ContactField.USERFIELD09,
        ContactField.USERFIELD10, ContactField.USERFIELD11, ContactField.USERFIELD12, ContactField.USERFIELD13, ContactField.USERFIELD14, ContactField.USERFIELD15,
        ContactField.USERFIELD16, ContactField.USERFIELD17, ContactField.USERFIELD18, ContactField.USERFIELD19, ContactField.USERFIELD20, ContactField.DEFAULT_ADDRESS,
        ContactField.NUMBER_OF_ATTACHMENTS };

    // private ContactSQLInterface contactsql;

    protected final static ContactField[] deleteFields = { ContactField.OBJECT_ID, ContactField.LAST_MODIFIED };

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ContactWriter.class);

    public ContactWriter() {

    }

    public ContactWriter(final User userObj, final Context ctx, final Session sessionObj) {
        this.userObj = userObj;
        this.ctx = ctx;
        this.sessionObj = sessionObj;
        // contactsql = new RdbContactSQLInterface(sessionObj, ctx);
    }

    public void startWriter(final int objectId, final int folderId, final OutputStream os) throws Exception {
        final Element eProp = new Element("prop", "D", "DAV:");
        final XMLOutputter xo = new XMLOutputter();
        try {
        	final ContactService contactService = ServerServiceRegistry.getInstance().getService(ContactService.class);
            final Contact contactobject = contactService.getContact(sessionObj, Integer.toString(folderId), Integer.toString(objectId));
            writeObject(contactobject, eProp, false, xo, os);
        } catch (final OXException exc) {
            if (exc.isGeneric(Generic.NOT_FOUND)) {
                writeResponseElement(eProp, 0, HttpServletResponse.SC_NOT_FOUND, XmlServlet.OBJECT_NOT_FOUND_EXCEPTION, xo, os);
            } else {
                writeResponseElement(eProp, 0, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, getErrorMessage(XmlServlet.SERVER_ERROR_EXCEPTION, XmlServlet.SERVER_ERROR_STATUS), xo, os);
            }
        } catch (final Exception ex) {
            LOG.error("", ex);
            writeResponseElement(eProp, 0, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, getErrorMessage(XmlServlet.SERVER_ERROR_EXCEPTION, XmlServlet.SERVER_ERROR_STATUS), xo, os);
        }
    }

    public void startWriter(final boolean bModified, final boolean bDeleted, final boolean bList, final int folder_id, final Date lastsync, final OutputStream os) throws Exception {
        final XMLOutputter xo = new XMLOutputter();
    	final ContactService contactService = ServerServiceRegistry.getInstance().getService(ContactService.class);
        /*
         * Fist send all 'deletes', than all 'modified'
         */
        if (bDeleted) {
            SearchIterator<Contact> it = null;
            try {
            	it = contactService.getDeletedContacts(sessionObj, Integer.toString(folder_id), lastsync, deleteFields);
                writeIterator(it, true, xo, os);
            } finally {
                if (it != null) {
                    it.close();
                }
            }
        }

        if (bModified) {
            SearchIterator<Contact> it = null;
            try {
            	it = contactService.getModifiedContacts(sessionObj, Integer.toString(folder_id), lastsync, changeFields);
                writeIterator(it, false, xo, os);
            } finally {
                if (it != null) {
                    it.close();
                }
            }
        }

        if (bList) {
            SearchIterator<Contact> it = null;
            try {
            	it = contactService.getAllContacts(sessionObj, Integer.toString(folder_id), deleteFields, new SortOptions(0, 50000));
                writeList(it, xo, os);
            } finally {
                if (it != null) {
                    it.close();
                }
            }
        }
    }

    public void writeIterator(final SearchIterator<Contact> it, final boolean delete, final XMLOutputter xo, final OutputStream os) throws Exception {
        while (it.hasNext()) {
            writeObject(it.next(), delete, xo, os);
        }
    }

    public void writeObject(final Contact contactObj, final boolean delete, final XMLOutputter xo, final OutputStream os) throws Exception {
        writeObject(contactObj, new Element("prop", "D", "DAV:"), delete, xo, os);
    }

    public void writeObject(final Contact contactObj, final Element eProp, final boolean delete, final XMLOutputter xo, final OutputStream os) throws Exception {
        int status = 200;
        String description = "OK";
        int object_id = 0;

        try {
            object_id = contactObj.getObjectID();
            if (contactObj.containsImage1() && !delete) {
            	final ContactService contactService = ServerServiceRegistry.getInstance().getService(ContactService.class);
            	final Contact contactObjectWithImage = contactService.getContact(sessionObj,
            			Integer.toString(contactObj.getParentFolderID()), Integer.toString(contactObj.getObjectID()));
                addContent2PropElement(eProp, contactObjectWithImage, delete);
            } else {
                addContent2PropElement(eProp, contactObj, delete);
            }
        } catch (final Exception exc) {
            LOG.error("writeObject", exc);
            status = 500;
            description = "Server Error: " + exc.getMessage();
            object_id = 0;
        }

        writeResponseElement(eProp, object_id, status, description, xo, os);
    }

    protected void addContent2PropElement(final Element e, final Contact contactobject, final boolean delete) throws Exception {
        addContent2PropElement(e, contactobject, delete, false);
    }

    public void addContent2PropElement(final Element e, final Contact contactobject, final boolean delete, final boolean externalUser) throws OXException, SearchIteratorException, UnsupportedEncodingException, AddressException {
        if (delete) {
            addElement(DataFields.OBJECT_ID, contactobject.getObjectID(), e);
            addElement(DataFields.LAST_MODIFIED, contactobject.getLastModified(), e);
            addElement("object_status", "DELETE", e);
        } else {
            writeCommonElements(contactobject, e);
            writeContactElement(contactobject, e);

            if (contactobject.containsImage1()) {
                addElement(ContactFields.IMAGE_CONTENT_TYPE, contactobject.getImageContentType(), e);
                addElement(ContactFields.IMAGE1, Base64.encode(contactobject.getImage1()), e);
            }

            if (contactobject.getDistributionList() != null) {
                addElement(ContactFields.DISTRIBUTIONLIST_FLAG, true, e);
                writeDistributionList(contactobject, e);
            } else {
                addElement(ContactFields.DISTRIBUTIONLIST_FLAG, false, e);
            }

        }
    }

    protected void writeContactElement(final Contact contactobject, final Element e) throws AddressException {
        writeContactElement(contactobject, e, null);
    }

    protected void writeContactElement(final Contact contactobject, final Element e, final Set<InternetAddress> internalAddresses) throws AddressException {
        addElement("object_status", "CREATE", e);
        addElement(ContactFields.LAST_NAME, contactobject.getSurName(), e);
        addElement(ContactFields.FIRST_NAME, contactobject.getGivenName(), e);
        addElement(ContactFields.ANNIVERSARY, contactobject.getAnniversary(), e);
        addElement(ContactFields.ASSISTANTS_NAME, contactobject.getAssistantName(), e);
        addElement(ContactFields.BIRTHDAY, contactobject.getBirthday(), e);
        addElement(ContactFields.BRANCHES, contactobject.getBranches(), e);
        addElement(ContactFields.BUSINESS_CATEGORY, contactobject.getBusinessCategory(), e);
        addElement(CommonFields.CATEGORIES, contactobject.getCategories(), e);
        addElement(ContactFields.MOBILE1, contactobject.getCellularTelephone1(), e);
        addElement(ContactFields.MOBILE2, contactobject.getCellularTelephone2(), e);
        addElement(ContactFields.CITY, contactobject.getCityHome(), e);
        addElement(ContactFields.BUSINESS_CITY, contactobject.getCityBusiness(), e);
        addElement(ContactFields.SECOND_CITY, contactobject.getCityOther(), e);
        addElement(ContactFields.COMMERCIAL_REGISTER, contactobject.getCommercialRegister(), e);
        addElement(ContactFields.COMPANY, contactobject.getCompany(), e);
        addElement(ContactFields.COUNTRY, contactobject.getCountryHome(), e);
        addElement(ContactFields.BUSINESS_COUNTRY, contactobject.getCountryBusiness(), e);
        addElement(ContactFields.SECOND_COUNTRY, contactobject.getCountryOther(), e);
        addElement(ContactFields.DEPARTMENT, contactobject.getDepartment(), e);
        addElement(ContactFields.DISPLAY_NAME, contactobject.getDisplayName(), e);
        /*
         * Write email addresses
         */
        if (null == internalAddresses || internalAddresses.isEmpty()) {
            addElement(ContactFields.EMAIL1, contactobject.getEmail1(), e);
            addElement(ContactFields.EMAIL2, contactobject.getEmail2(), e);
            addElement(ContactFields.EMAIL3, contactobject.getEmail3(), e);
        } else {
            addEmailAddress(ContactFields.EMAIL1, contactobject.getEmail1(), e, internalAddresses);
            addEmailAddress(ContactFields.EMAIL2, contactobject.getEmail2(), e, internalAddresses);
            addEmailAddress(ContactFields.EMAIL3, contactobject.getEmail3(), e, internalAddresses);
        }
        addElement(ContactFields.EMPLOYEE_TYPE, contactobject.getEmployeeType(), e);
        addElement(ContactFields.FAX_BUSINESS, contactobject.getFaxBusiness(), e);
        addElement(ContactFields.FAX_HOME, contactobject.getFaxHome(), e);
        addElement(ContactFields.FAX_OTHER, contactobject.getFaxOther(), e);
        addElement(ContactFields.FILE_AS, contactobject.getFileAs(), e);
        addElement(ContactFields.NOTE, contactobject.getNote(), e);
        addElement(ContactFields.MORE_INFO, contactobject.getInfo(), e);
        addElement(ContactFields.INSTANT_MESSENGER, contactobject.getInstantMessenger1(), e);
        addElement(ContactFields.INSTANT_MESSENGER2, contactobject.getInstantMessenger2(), e);
        addElement(ContactFields.MARTITAL_STATUS, contactobject.getMaritalStatus(), e);
        addElement(ContactFields.MANAGERS_NAME, contactobject.getManagerName(), e);
        addElement(ContactFields.SECOND_NAME, contactobject.getMiddleName(), e);
        addElement(ContactFields.NICKNAME, contactobject.getNickname(), e);
        addElement(ContactFields.NUMBER_OF_CHILDREN, contactobject.getNumberOfChildren(), e);
        addElement(ContactFields.NUMBER_OF_EMPLOYEE, contactobject.getNumberOfEmployee(), e);
        addElement(ContactFields.POSITION, contactobject.getPosition(), e);
        addElement(ContactFields.POSTAL_CODE, contactobject.getPostalCodeHome(), e);
        addElement(ContactFields.BUSINESS_POSTAL_CODE, contactobject.getPostalCodeBusiness(), e);
        addElement(ContactFields.SECOND_POSTAL_CODE, contactobject.getPostalCodeOther(), e);
        addElement(ContactFields.PROFESSION, contactobject.getProfession(), e);
        addElement(ContactFields.ROOM_NUMBER, contactobject.getRoomNumber(), e);
        addElement(ContactFields.SALES_VOLUME, contactobject.getSalesVolume(), e);
        addElement(ContactFields.SPOUSE_NAME, contactobject.getSpouseName(), e);
        addElement(ContactFields.STATE, contactobject.getStateHome(), e);
        addElement(ContactFields.BUSINESS_STATE, contactobject.getStateBusiness(), e);
        addElement(ContactFields.SECOND_STATE, contactobject.getStateOther(), e);
        addElement(ContactFields.STREET, contactobject.getStreetHome(), e);
        addElement(ContactFields.BUSINESS_STREET, contactobject.getStreetBusiness(), e);
        addElement(ContactFields.SECOND_STREET, contactobject.getStreetOther(), e);
        addElement(ContactFields.SUFFIX, contactobject.getSuffix(), e);
        addElement(ContactFields.TAX_ID, contactobject.getTaxID(), e);
        addElement(ContactFields.PHONE_ASSISTANT, contactobject.getTelephoneAssistant(), e);
        addElement(ContactFields.PHONE_BUSINESS, contactobject.getTelephoneBusiness1(), e);
        addElement(ContactFields.PHONE_BUSINESS2, contactobject.getTelephoneBusiness2(), e);
        addElement(ContactFields.CALLBACK, contactobject.getTelephoneCallback(), e);
        addElement(ContactFields.PHONE_CAR, contactobject.getTelephoneCar(), e);
        addElement(ContactFields.PHONE_COMPANY, contactobject.getTelephoneCompany(), e);
        addElement(ContactFields.PHONE_HOME, contactobject.getTelephoneHome1(), e);
        addElement(ContactFields.PHONE_HOME2, contactobject.getTelephoneHome2(), e);
        addElement(ContactFields.IP_PHONE, contactobject.getTelephoneIP(), e);
        addElement(ContactFields.ISDN, contactobject.getTelephoneISDN(), e);
        addElement(ContactFields.PHONE_OTHER, contactobject.getTelephoneOther(), e);
        addElement(ContactFields.PAGER, contactobject.getTelephonePager(), e);
        addElement(ContactFields.PRIMARY, contactobject.getTelephonePrimary(), e);
        addElement(ContactFields.RADIO, contactobject.getTelephoneRadio(), e);
        addElement(ContactFields.TELEX, contactobject.getTelephoneTelex(), e);
        addElement(ContactFields.TTY_TDD, contactobject.getTelephoneTTYTTD(), e);
        addElement(ContactFields.TITLE, contactobject.getTitle(), e);
        addElement(ContactFields.URL, contactobject.getURL(), e);
        addElement(ContactFields.USERFIELD01, contactobject.getUserField01(), e);
        addElement(ContactFields.USERFIELD02, contactobject.getUserField02(), e);
        addElement(ContactFields.USERFIELD03, contactobject.getUserField03(), e);
        addElement(ContactFields.USERFIELD04, contactobject.getUserField04(), e);
        addElement(ContactFields.USERFIELD05, contactobject.getUserField05(), e);
        addElement(ContactFields.USERFIELD06, contactobject.getUserField06(), e);
        addElement(ContactFields.USERFIELD07, contactobject.getUserField07(), e);
        addElement(ContactFields.USERFIELD08, contactobject.getUserField08(), e);
        addElement(ContactFields.USERFIELD09, contactobject.getUserField09(), e);
        addElement(ContactFields.USERFIELD10, contactobject.getUserField10(), e);
        addElement(ContactFields.USERFIELD11, contactobject.getUserField11(), e);
        addElement(ContactFields.USERFIELD12, contactobject.getUserField12(), e);
        addElement(ContactFields.USERFIELD13, contactobject.getUserField13(), e);
        addElement(ContactFields.USERFIELD14, contactobject.getUserField14(), e);
        addElement(ContactFields.USERFIELD15, contactobject.getUserField15(), e);
        addElement(ContactFields.USERFIELD16, contactobject.getUserField16(), e);
        addElement(ContactFields.USERFIELD17, contactobject.getUserField17(), e);
        addElement(ContactFields.USERFIELD18, contactobject.getUserField18(), e);
        addElement(ContactFields.USERFIELD19, contactobject.getUserField19(), e);
        addElement(ContactFields.USERFIELD20, contactobject.getUserField20(), e);
        addElement(ContactFields.DEFAULTADDRESS, contactobject.getDefaultAddress(), e);
    }

    @SuppressWarnings("unchecked")
    private static void addEmailAddress(final String name, final String emailAddress, final Element e, final Set<InternetAddress> internalAddresses) throws AddressException {
        if (null != emailAddress) {
            final List<Element> children = e.getChildren(name, Namespace.getNamespace(XmlServlet.PREFIX, XmlServlet.NAMESPACE));
            final QuotedInternetAddress ia = new QuotedInternetAddress(emailAddress);
            if (null != children && !children.isEmpty()) {
                /*
                 * A child element with the same name already exists
                 */
                if (children.size() > 1) {
                    LOG.warn("Conflicting email address detected! Multiple elements named \"{}\" already exist.", name);
                    return;
                }
                final Element elem = children.get(0);
                final Text text = (Text) elem.getContent().get(0);
                if (!new QuotedInternetAddress(text.getText()).equals(ia)) {
                    LOG.warn("Conflicting email address detected! An element named \"{}\" already exists.", name);
                    return;
                }
                /*
                 * An element with the same name and same value already exists. check for "isInternal" attribute.
                 */
                final org.jdom2.Attribute attr = elem.getAttribute("isInternal");
                if (null == attr) {
                    if (internalAddresses.contains(ia)) {
                        elem.setAttribute("isInternal", "true");
                    }
                }
                return;
            }
            /*
             * No equally named element exists, create it.
             */
            final Element child = addElement(name, emailAddress, e);
            if (internalAddresses.contains(ia)) {
                child.setAttribute("isInternal", "true");
            }
        }
    }

    protected void writeDistributionList(final Contact contactobject, final Element e_prop) {
        final Element e_distributionlist = new Element(ContactFields.DISTRIBUTIONLIST, XmlServlet.NS);

        final DistributionListEntryObject[] distributionlist = contactobject.getDistributionList();
        for (int a = 0; a < distributionlist.length; a++) {
            String displayname = distributionlist[a].getDisplayname();
            final String email = distributionlist[a].getEmailaddress();

            if (displayname == null) {
                displayname = email;
            }

            final Element e = new Element("email", XmlServlet.NS);
            e.addContent(correctCharacterData(email));
            e.setAttribute("id", Integer.toString(distributionlist[a].getEntryID()), XmlServlet.NS);
            e.setAttribute(FolderChildFields.FOLDER_ID, Integer.toString(distributionlist[a].getFolderID()), XmlServlet.NS);
            e.setAttribute("displayname", displayname.trim(), XmlServlet.NS);
            e.setAttribute("emailfield", Integer.toString(distributionlist[a].getEmailfield()), XmlServlet.NS);

            e_distributionlist.addContent(e);
        }

        e_prop.addContent(e_distributionlist);
    }

    @Override
    protected int getModule() {
        return Types.CONTACT;
    }
}
