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
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.mail.internet.InternetAddress;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import com.openexchange.contact.ContactFieldOperand;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.SortOptions;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.search.Order;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ConstantOperand;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.StringCollection;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.webdav.xml.fields.DataFields;
import com.openexchange.webdav.xml.fields.FolderChildFields;

/**
 * AppointmentWriter
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public class GroupUserWriter extends ContactWriter {

    protected final static ContactField[] changeFields = {
        // DataObject.OBJECT_ID,
        ContactField.CREATED_BY,
        ContactField.CREATION_DATE,
        ContactField.LAST_MODIFIED,
        ContactField.MODIFIED_BY,
        ContactField.FOLDER_ID,
        ContactField.PRIVATE_FLAG,
        ContactField.CATEGORIES,
        ContactField.GIVEN_NAME,
        ContactField.SUR_NAME,
        ContactField.ANNIVERSARY,
        ContactField.ASSISTANT_NAME,
        ContactField.BIRTHDAY,
        ContactField.BRANCHES,
        ContactField.BUSINESS_CATEGORY,
        // ContactObject.CATEGORIES,
        ContactField.CELLULAR_TELEPHONE1,
        ContactField.CELLULAR_TELEPHONE2,
        ContactField.CITY_BUSINESS,
        ContactField.CITY_HOME,
        ContactField.CITY_OTHER,
        ContactField.COMMERCIAL_REGISTER,
        ContactField.COMPANY,
        ContactField.COUNTRY_BUSINESS,
        ContactField.COUNTRY_HOME,
        ContactField.COUNTRY_OTHER,
        ContactField.DEPARTMENT,
        ContactField.DISPLAY_NAME,
        // ContactObject.DISTRIBUTIONLIST,
        ContactField.EMAIL1, ContactField.EMAIL2,
        ContactField.EMAIL3,
        ContactField.EMPLOYEE_TYPE,
        ContactField.FAX_BUSINESS,
        ContactField.FAX_HOME,
        ContactField.FAX_OTHER,
        ContactField.FOLDER_ID,
        ContactField.GIVEN_NAME,
        ContactField.IMAGE1,
        ContactField.INFO,
        ContactField.INSTANT_MESSENGER1,
        ContactField.INSTANT_MESSENGER2,
        // ContactObject.LINKS,
        ContactField.MANAGER_NAME, ContactField.MARITAL_STATUS, ContactField.MIDDLE_NAME, ContactField.NICKNAME, ContactField.NOTE, ContactField.NUMBER_OF_CHILDREN,
        ContactField.NUMBER_OF_EMPLOYEE, ContactField.POSITION, ContactField.POSTAL_CODE_BUSINESS, ContactField.POSTAL_CODE_HOME, ContactField.POSTAL_CODE_OTHER,
        ContactField.PRIVATE_FLAG, ContactField.PROFESSION, ContactField.ROOM_NUMBER, ContactField.SALES_VOLUME, ContactField.SPOUSE_NAME, ContactField.STATE_BUSINESS,
        ContactField.STATE_HOME, ContactField.STATE_OTHER, ContactField.STREET_BUSINESS, ContactField.STREET_HOME, ContactField.STREET_OTHER, ContactField.SUFFIX,
        ContactField.TAX_ID, ContactField.TELEPHONE_ASSISTANT, ContactField.TELEPHONE_BUSINESS1, ContactField.TELEPHONE_BUSINESS2, ContactField.TELEPHONE_CALLBACK,
        ContactField.TELEPHONE_CAR, ContactField.TELEPHONE_COMPANY, ContactField.TELEPHONE_HOME1, ContactField.TELEPHONE_HOME2, ContactField.TELEPHONE_IP,
        ContactField.TELEPHONE_ISDN, ContactField.TELEPHONE_OTHER, ContactField.TELEPHONE_PAGER, ContactField.TELEPHONE_PRIMARY, ContactField.TELEPHONE_RADIO,
        ContactField.TELEPHONE_TELEX, ContactField.TELEPHONE_TTYTDD, ContactField.TITLE, ContactField.URL, ContactField.USERFIELD01, ContactField.USERFIELD02,
        ContactField.USERFIELD03, ContactField.USERFIELD04, ContactField.USERFIELD05, ContactField.USERFIELD06, ContactField.USERFIELD07, ContactField.USERFIELD08,
        ContactField.USERFIELD09, ContactField.USERFIELD10, ContactField.USERFIELD11, ContactField.USERFIELD12, ContactField.USERFIELD13, ContactField.USERFIELD14,
        ContactField.USERFIELD15, ContactField.USERFIELD16, ContactField.USERFIELD17, ContactField.USERFIELD18, ContactField.USERFIELD19, ContactField.USERFIELD20,
        ContactField.INTERNAL_USERID };

    protected final static ContactField[] deleteFields = { ContactField.OBJECT_ID, ContactField.LAST_MODIFIED, ContactField.INTERNAL_USERID };

    protected UserStorage userStorage = null;

    protected Element parent = null;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GroupUserWriter.class);

    public GroupUserWriter(final User userObj, final Context ctx, final Session sessionObj, final Element parent) {
        super(userObj, ctx, sessionObj);
        this.parent = parent;

        init();
    }

    protected void init() {
        userStorage = UserStorage.getInstance();
    }

    public void startWriter(final boolean modified, final boolean deleted, Date lastsync, final OutputStream os) throws Exception {
        final XMLOutputter xo = new XMLOutputter();
        final ContactService contactService = ServerServiceRegistry.getInstance().getService(ContactService.class);
        final String folderID = Integer.toString(FolderObject.SYSTEM_LDAP_FOLDER_ID);

        if (lastsync == null) {
            lastsync = new Date(0);
        }
        /*
         * Fist send all 'deletes', than all 'modified'
         */

        if (deleted) {
            SearchIterator<Contact> it = null;
            try {
                it = contactService.getDeletedContacts(sessionObj, folderID, lastsync, deleteFields);
                writeIterator(it, true, xo, os);
            } finally {
                if (it != null) {
                    it.close();
                }
            }
        }

        if (modified) {
            SearchIterator<Contact> it = null;
            try {
                it = contactService.getModifiedContacts(sessionObj, folderID, lastsync, changeFields);
                writeIterator(it, false, xo, os);
            } finally {
                if (it != null) {
                    it.close();
                }
            }
        }

    }

    public void startWriter(final String searchpattern, final OutputStream os) throws Exception {
        final XMLOutputter xo = new XMLOutputter();
        SearchIterator<Contact> it = null;
        try {
            final ContactService contactService = ServerServiceRegistry.getInstance().getService(ContactService.class);
            final String preparedPattern = StringCollection.prepareForSearch(searchpattern, false);
            final ContactField[] searchFields = { ContactField.DISPLAY_NAME, ContactField.GIVEN_NAME, ContactField.SUR_NAME,
                ContactField.EMAIL1, ContactField.EMAIL2, ContactField.EMAIL3, ContactField.CATEGORIES };
            final CompositeSearchTerm orTerm = new CompositeSearchTerm(CompositeOperation.OR);
            for (final ContactField field : searchFields) {
                final SingleSearchTerm term = new SingleSearchTerm(SingleOperation.EQUALS);
                term.addOperand(new ContactFieldOperand(field));
                term.addOperand(new ConstantOperand<String>(preparedPattern));
                orTerm.addSearchTerm(term);
            }
            final SingleSearchTerm folderTerm = new SingleSearchTerm(SingleOperation.EQUALS);
            folderTerm.addOperand(new ContactFieldOperand(ContactField.FOLDER_ID));
            folderTerm.addOperand(new ConstantOperand<String>(Integer.toString(FolderObject.SYSTEM_LDAP_FOLDER_ID)));
            final CompositeSearchTerm andTerm = new CompositeSearchTerm(CompositeOperation.AND);
            andTerm.addSearchTerm(folderTerm);
            andTerm.addSearchTerm(orTerm);

            it = contactService.searchContacts(sessionObj, andTerm, changeFields,
                new SortOptions(ContactField.DISPLAY_NAME, Order.ASCENDING));
            writeIterator(it, false, xo, os);
        } finally {
            if (it != null) {
                it.close();
            }
        }
    }

    @Override
    public void writeIterator(final SearchIterator<Contact> it, final boolean delete, final XMLOutputter xo, final OutputStream os) throws Exception {
        while (it.hasNext()) {
            writeObject(it.next(), delete, xo, os);
        }
    }

    @Override
    public void writeObject(final Contact contactobject, final boolean delete, final XMLOutputter xo, final OutputStream os) throws Exception {
        final Element e = new Element(parent.getName(), parent.getNamespace());

        try {
            addContent2Element(e, contactobject, delete);
            xo.output(e, os);
            os.flush();
        } catch (final Exception exc) {
            LOG.error("writeObject", exc);
        }
    }

    protected void addContent2Element(final Element e, final Contact contactobject, final boolean delete) throws Exception {
        if (delete) {
            final int userId = contactobject.getInternalUserId();

            addElement("uid", userId, e);
            addElement("object_id", contactobject.getObjectID(), e);
            addElement("object_status", "DELETE", e);
        } else {
            final int userId = contactobject.getInternalUserId();

            final User u = userStorage.getUser(userId, ctx);

            addElement("uid", userId, e);
            addElement(DataFields.OBJECT_ID, contactobject.getObjectID(), e);
            addElement(FolderChildFields.FOLDER_ID, FolderObject.SYSTEM_LDAP_FOLDER_ID, e);
            final String primaryAddress = u.getMail();
            {
                final Element child = addElement("email1", primaryAddress, e);
                child.setAttribute("isInternal", "true");
            }

            /*
             * Create set with internal email addresses
             */
            final Set<InternetAddress> internalAddresses;

            final String[] aliases = u.getAliases();
            if (null != aliases && aliases.length > 0) {
                internalAddresses = new HashSet<InternetAddress>(aliases.length + 1);
                internalAddresses.add(new QuotedInternetAddress(primaryAddress));

                for (final String alias : aliases) {
                    internalAddresses.add(new QuotedInternetAddress(alias));
                }
            } else {
                internalAddresses = new HashSet<InternetAddress>(1);
                internalAddresses.add(new QuotedInternetAddress(primaryAddress));
            }

            addElement(DataFields.LAST_MODIFIED, contactobject.getLastModified(), e);
            addElementMemberInGroups(e, u);

            if (userId == sessionObj.getUserId()) {
                addElement("myidentity", true, e);
                addElement("context_id", sessionObj.getContextId(), e);
            }

            writeContactElement(contactobject, e, internalAddresses);
        }
    }

    public void addElementMemberInGroups(final Element eProp, final User u) {
        final Element eMemberInGroups = new Element("memberingroups", XmlServlet.NS);
        final int groupId[] = u.getGroups();
        for (final int element : groupId) {
            final Element eMember = new Element("member", XmlServlet.NS);
            eMember.addContent(Integer.toString(element));
            eMemberInGroups.addContent(eMember);
        }

        eProp.addContent(eMemberInGroups);
    }

    private static void addIfNotEmpty(final String name, final String value, final Element parent) {
        if (com.openexchange.java.Strings.isEmpty(value)) {
            return;
        }
        addElement(name, value, parent);
    }
}
