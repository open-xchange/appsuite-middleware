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

package com.openexchange.webdav.xml;

import java.io.OutputStream;
import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import com.openexchange.groupware.contact.ContactInterface;
import com.openexchange.groupware.contact.ContactInterfaceDiscoveryService;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.webdav.xml.fields.ContactFields;
import com.openexchange.webdav.xml.fields.DataFields;

/**
 * AppointmentWriter
 *
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */
public class GroupUserWriter extends ContactWriter {

    protected final static int[] changeFields = {
        // DataObject.OBJECT_ID,
        DataObject.CREATED_BY,
        DataObject.CREATION_DATE,
        DataObject.LAST_MODIFIED,
        DataObject.MODIFIED_BY,
        FolderChildObject.FOLDER_ID,
        CommonObject.PRIVATE_FLAG,
        CommonObject.CATEGORIES,
        Contact.GIVEN_NAME,
        Contact.SUR_NAME,
        Contact.ANNIVERSARY,
        Contact.ASSISTANT_NAME,
        Contact.BIRTHDAY,
        Contact.BRANCHES,
        Contact.BUSINESS_CATEGORY,
        // ContactObject.CATEGORIES,
        Contact.CELLULAR_TELEPHONE1,
        Contact.CELLULAR_TELEPHONE2,
        Contact.CITY_BUSINESS,
        Contact.CITY_HOME,
        Contact.CITY_OTHER,
        Contact.COMMERCIAL_REGISTER,
        Contact.COMPANY,
        Contact.COUNTRY_BUSINESS,
        Contact.COUNTRY_HOME,
        Contact.COUNTRY_OTHER,
        Contact.DEPARTMENT,
        Contact.DISPLAY_NAME,
        // ContactObject.DISTRIBUTIONLIST,
        Contact.EMAIL1,
        Contact.EMAIL2,
        Contact.EMAIL3,
        Contact.EMPLOYEE_TYPE,
        Contact.FAX_BUSINESS,
        Contact.FAX_HOME,
        Contact.FAX_OTHER,
        Contact.FOLDER_ID,
        Contact.GIVEN_NAME,
        Contact.IMAGE1,
        Contact.INFO,
        Contact.INSTANT_MESSENGER1,
        Contact.INSTANT_MESSENGER2,
        // ContactObject.LINKS,
        Contact.MANAGER_NAME,
        Contact.MARITAL_STATUS,
        Contact.MIDDLE_NAME,
        Contact.NICKNAME,
        Contact.NOTE,
        Contact.NUMBER_OF_CHILDREN,
        Contact.NUMBER_OF_EMPLOYEE,
        Contact.POSITION,
        Contact.POSTAL_CODE_BUSINESS,
        Contact.POSTAL_CODE_HOME,
        Contact.POSTAL_CODE_OTHER,
        Contact.PRIVATE_FLAG,
        Contact.PROFESSION,
        Contact.ROOM_NUMBER,
        Contact.SALES_VOLUME,
        Contact.SPOUSE_NAME,
        Contact.STATE_BUSINESS,
        Contact.STATE_HOME,
        Contact.STATE_OTHER,
        Contact.STREET_BUSINESS,
        Contact.STREET_HOME,
        Contact.STREET_OTHER,
        Contact.SUFFIX,
        Contact.TAX_ID,
        Contact.TELEPHONE_ASSISTANT,
        Contact.TELEPHONE_BUSINESS1,
        Contact.TELEPHONE_BUSINESS2,
        Contact.TELEPHONE_CALLBACK,
        Contact.TELEPHONE_CAR,
        Contact.TELEPHONE_COMPANY,
        Contact.TELEPHONE_HOME1,
        Contact.TELEPHONE_HOME2,
        Contact.TELEPHONE_IP,
        Contact.TELEPHONE_ISDN,
        Contact.TELEPHONE_OTHER,
        Contact.TELEPHONE_PAGER,
        Contact.TELEPHONE_PRIMARY,
        Contact.TELEPHONE_RADIO,
        Contact.TELEPHONE_TELEX,
        Contact.TELEPHONE_TTYTDD,
        Contact.TITLE,
        Contact.URL,
        Contact.USERFIELD01,
        Contact.USERFIELD02,
        Contact.USERFIELD03,
        Contact.USERFIELD04,
        Contact.USERFIELD05,
        Contact.USERFIELD06,
        Contact.USERFIELD07,
        Contact.USERFIELD08,
        Contact.USERFIELD09,
        Contact.USERFIELD10,
        Contact.USERFIELD11,
        Contact.USERFIELD12,
        Contact.USERFIELD13,
        Contact.USERFIELD14,
        Contact.USERFIELD15,
        Contact.USERFIELD16,
        Contact.USERFIELD17,
        Contact.USERFIELD18,
        Contact.USERFIELD19,
        Contact.USERFIELD20,
        Contact.INTERNAL_USERID
    };

    protected final static int[] deleteFields = {
        DataObject.OBJECT_ID,
        DataObject.LAST_MODIFIED,
        Contact.INTERNAL_USERID
    };

    protected UserStorage userStorage = null;

    protected Element parent = null;

    private static final Log LOG = LogFactory.getLog(GroupUserWriter.class);

    public GroupUserWriter(final User userObj, final Context ctx, final Session sessionObj, final Element parent) {
        super(userObj, ctx, sessionObj);
        this.parent = parent;

        init();
    }

    protected void init() {
        userStorage = UserStorage.getInstance();
    }

    public void startWriter(final boolean modified, final boolean deleted, Date lastsync, final OutputStream os) throws Exception {
        //final ContactSQLInterface contactsql = new RdbContactSQLInterface(sessionObj);
        final XMLOutputter xo = new XMLOutputter();

        if (lastsync == null) {
            lastsync = new Date(0);
        }
        /*
         * Fist send all 'deletes', than all 'modified'
         */

        if (deleted) {
            SearchIterator<Contact> it = null;
            try {
                final ContactInterface contactInterface = ServerServiceRegistry.getInstance().getService(
                    ContactInterfaceDiscoveryService.class).newContactInterface(FolderObject.SYSTEM_LDAP_FOLDER_ID, sessionObj);
                it = contactInterface.getDeletedContactsInFolder(FolderObject.SYSTEM_LDAP_FOLDER_ID, deleteFields, lastsync);
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
                final ContactInterface contactInterface = ServerServiceRegistry.getInstance().getService(
                    ContactInterfaceDiscoveryService.class).newContactInterface(FolderObject.SYSTEM_LDAP_FOLDER_ID, sessionObj);
                it = contactInterface.getModifiedContactsInFolder(FolderObject.SYSTEM_LDAP_FOLDER_ID, changeFields, lastsync);
                writeIterator(it, false, xo, os);
            } finally {
                if (it != null) {
                    it.close();
                }
            }
        }

    }

    public void startWriter(final String searchpattern, final OutputStream os) throws Exception {
        // final ContactSQLInterface contactsql = new RdbContactSQLInterface(sessionObj);
        final XMLOutputter xo = new XMLOutputter();
        SearchIterator<Contact> it = null;
        try {
            final ContactInterface contactInterface = ServerServiceRegistry.getInstance().getService(
                ContactInterfaceDiscoveryService.class).newContactInterface(FolderObject.SYSTEM_LDAP_FOLDER_ID, sessionObj);
            it = contactInterface.searchContacts(searchpattern, FolderObject.SYSTEM_LDAP_FOLDER_ID, Contact.DISPLAY_NAME, "asc", changeFields);
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
            addElement(ContactFields.OBJECT_ID, contactobject.getObjectID(), e);
            addElement(ContactFields.FOLDER_ID, FolderObject.SYSTEM_LDAP_FOLDER_ID, e);
            addElement("email1", u.getMail(), e);
            addElement(DataFields.LAST_MODIFIED, contactobject.getLastModified(), e);
            addElementMemberInGroups(e, u);

            if (userId == sessionObj.getUserId()) {
                addElement("myidentity", true, e);
                addElement("context_id", sessionObj.getContextId(), e);
            }

            writeContactElement(contactobject, e);
        }
    }

    public void addElementMemberInGroups(final Element eProp, final User u) {
        final Element eMemberInGroups = new Element("memberingroups", XmlServlet.NS);
        final int groupId[] = u.getGroups();
        for (int a = 0; a < groupId.length; a++) {
            final Element eMember = new Element("member", XmlServlet.NS);
            eMember.addContent(String.valueOf(groupId[a]));
            eMemberInGroups.addContent(eMember);
        }

        eProp.addContent(eMemberInGroups);
    }
}
