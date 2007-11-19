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

import com.openexchange.api2.ContactSQLInterface;
import com.openexchange.api2.RdbContactSQLInterface;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
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
		ContactObject.GIVEN_NAME,
		ContactObject.SUR_NAME,
		ContactObject.ANNIVERSARY,
		ContactObject.ASSISTANT_NAME,
		ContactObject.BIRTHDAY,
		ContactObject.BRANCHES,
		ContactObject.BUSINESS_CATEGORY,
		// ContactObject.CATEGORIES,
		ContactObject.CELLULAR_TELEPHONE1,
		ContactObject.CELLULAR_TELEPHONE2,
		ContactObject.CITY_BUSINESS,
		ContactObject.CITY_HOME,
		ContactObject.CITY_OTHER,
		ContactObject.COMMERCIAL_REGISTER,
		ContactObject.COMPANY,
		ContactObject.COUNTRY_BUSINESS,
		ContactObject.COUNTRY_HOME,
		ContactObject.COUNTRY_OTHER,
		ContactObject.DEPARTMENT,
		ContactObject.DISPLAY_NAME,
		// ContactObject.DISTRIBUTIONLIST,
		ContactObject.EMAIL1,
		ContactObject.EMAIL2,
		ContactObject.EMAIL3,
		ContactObject.EMPLOYEE_TYPE,
		ContactObject.FAX_BUSINESS,
		ContactObject.FAX_HOME,
		ContactObject.FAX_OTHER,
		ContactObject.FOLDER_ID,
		ContactObject.GIVEN_NAME,
		ContactObject.IMAGE1,
		ContactObject.INFO,
		ContactObject.INSTANT_MESSENGER1,
		ContactObject.INSTANT_MESSENGER2,
		// ContactObject.LINKS,
		ContactObject.MANAGER_NAME,
		ContactObject.MARITAL_STATUS,
		ContactObject.MIDDLE_NAME,
		ContactObject.NICKNAME,
		ContactObject.NOTE,
		ContactObject.NUMBER_OF_CHILDREN,
		ContactObject.NUMBER_OF_EMPLOYEE,
		ContactObject.POSITION,
		ContactObject.POSTAL_CODE_BUSINESS,
		ContactObject.POSTAL_CODE_HOME,
		ContactObject.POSTAL_CODE_OTHER,
		ContactObject.PRIVATE_FLAG,
		ContactObject.PROFESSION,
		ContactObject.ROOM_NUMBER,
		ContactObject.SALES_VOLUME,
		ContactObject.SPOUSE_NAME,
		ContactObject.STATE_BUSINESS,
		ContactObject.STATE_HOME,
		ContactObject.STATE_OTHER,
		ContactObject.STREET_BUSINESS,
		ContactObject.STREET_HOME,
		ContactObject.STREET_OTHER,
		ContactObject.SUFFIX,
		ContactObject.TAX_ID,
		ContactObject.TELEPHONE_ASSISTANT,
		ContactObject.TELEPHONE_BUSINESS1,
		ContactObject.TELEPHONE_BUSINESS2,
		ContactObject.TELEPHONE_CALLBACK,
		ContactObject.TELEPHONE_CAR,
		ContactObject.TELEPHONE_COMPANY,
		ContactObject.TELEPHONE_HOME1,
		ContactObject.TELEPHONE_HOME2,
		ContactObject.TELEPHONE_IP,
		ContactObject.TELEPHONE_ISDN,
		ContactObject.TELEPHONE_OTHER,
		ContactObject.TELEPHONE_PAGER,
		ContactObject.TELEPHONE_PRIMARY,
		ContactObject.TELEPHONE_RADIO,
		ContactObject.TELEPHONE_TELEX,
		ContactObject.TELEPHONE_TTYTDD,
		ContactObject.TITLE,
		ContactObject.URL,
		ContactObject.USERFIELD01,
		ContactObject.USERFIELD02,
		ContactObject.USERFIELD03,
		ContactObject.USERFIELD04,
		ContactObject.USERFIELD05,
		ContactObject.USERFIELD06,
		ContactObject.USERFIELD07,
		ContactObject.USERFIELD08,
		ContactObject.USERFIELD09,
		ContactObject.USERFIELD10,
		ContactObject.USERFIELD11,
		ContactObject.USERFIELD12,
		ContactObject.USERFIELD13,
		ContactObject.USERFIELD14,
		ContactObject.USERFIELD15,
		ContactObject.USERFIELD16,
		ContactObject.USERFIELD17,
		ContactObject.USERFIELD18,
		ContactObject.USERFIELD19,
		ContactObject.USERFIELD20,
		ContactObject.INTERNAL_USERID
	};
	
	protected UserStorage userStorage = null;
	
	protected Element parent = null;
	
	private static final Log LOG = LogFactory.getLog(GroupUserWriter.class);
	
	public GroupUserWriter(Session sessionObj, Element parent) throws Exception {
		super(sessionObj);
		this.parent = parent;
		
		init();
	}
	
	protected void init() throws Exception {
		userStorage = UserStorage.getInstance();
	}
	
	public void startWriter(boolean modified, boolean deleted, Date lastsync, OutputStream os) throws Exception {
		ContactSQLInterface contactsql = new RdbContactSQLInterface(sessionObj);
		XMLOutputter xo = new XMLOutputter();
		
		if (lastsync == null) {
			lastsync = new Date(0);
		}
		
		if (modified) {
			SearchIterator it = null;
			try {
				it = contactsql.getModifiedContactsInFolder(FolderObject.SYSTEM_LDAP_FOLDER_ID, changeFields, lastsync);
				writeIterator(it, false, xo, os);
			} finally {
				if (it != null) {
					it.close();
				}
			}
		}
		
		if (deleted) {
			SearchIterator it = null;
			try {
				it = contactsql.getDeletedContactsInFolder(FolderObject.SYSTEM_LDAP_FOLDER_ID, deleteFields, lastsync);
				writeIterator(it, true, xo, os);
			} finally {
				if (it != null) {
					it.close();
				}
			}
		}
	}
	
	public void startWriter(String searchpattern, OutputStream os) throws Exception {
		ContactSQLInterface contactsql = new RdbContactSQLInterface(sessionObj);
		XMLOutputter xo = new XMLOutputter();
		SearchIterator it = null;
		try {
			it = contactsql.searchContacts(searchpattern, false, FolderObject.SYSTEM_LDAP_FOLDER_ID, ContactObject.DISPLAY_NAME, "asc", changeFields);
			writeIterator(it, false, xo, os);
		} finally {
			if (it != null) {
				it.close();
			}
		}
	}
	
	public void writeIterator(SearchIterator it, boolean delete, XMLOutputter xo, OutputStream os) throws Exception {
		while (it.hasNext()) {
			writeObject((ContactObject)it.next(), delete, xo, os);
		}
	}
	
	public void writeObject(ContactObject contactobject, boolean delete, XMLOutputter xo, OutputStream os) throws Exception {
		Element e = new Element(parent.getName(), parent.getNamespace());
		
		try {
			addContent2Element(e, contactobject, false);
			xo.output(e, os);
		} catch (Exception exc) {
			LOG.error("writeObject", exc);
		}
	}
	
	protected void addContent2Element(Element e, ContactObject contactobject, boolean delete) throws Exception {
		if (delete) {
			int userId = contactobject.getInternalUserId();
			
			addElement("uid", userId, e);
			addElement("object_id", contactobject.getObjectID(), e);
			addElement("object_status", "DELETE", e);
		} else {
			int userId = contactobject.getInternalUserId();
			
			User u = userStorage.getUser(userId, sessionObj.getContext());
			
			addElement("uid", userId, e);
            addElement(ContactFields.OBJECT_ID, contactobject.getObjectID(), e);
            addElement(ContactFields.FOLDER_ID, FolderObject.SYSTEM_LDAP_FOLDER_ID, e);
			addElement("email1", u.getMail(), e);
			addElement(DataFields.LAST_MODIFIED, contactobject.getLastModified(), e);
			addElementMemberInGroups(e, u);
			addElementAliases(e, u);
			
			if (userId == sessionObj.getUserId()) {
				addElement("myidentity", true, e);
				addElement("context_id", sessionObj.getContext().getContextId(), e);
			}
			
			writeContactElement(contactobject, e);
		}
	}
	
	public void addElementMemberInGroups(Element eProp, User u) throws Exception {
		Element eMemberInGroups = new Element("memberingroups", XmlServlet.NS);
		int groupId[] = u.getGroups();
		for (int a = 0; a < groupId.length; a++) {
			Element eMember = new Element("member", XmlServlet.NS);
			eMember.addContent(String.valueOf(groupId[a]));
			eMemberInGroups.addContent(eMember);
		}
		
		eProp.addContent(eMemberInGroups);
	}
	
	public void addElementAliases(Element eProp, User u) throws Exception {
		
	}
}




