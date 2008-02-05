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

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.api2.ContactSQLInterface;
import com.openexchange.api2.RdbContactSQLInterface;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.calendar.CalendarSql;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.container.LinkEntryObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.session.Session;
import com.openexchange.tools.encoding.Base64;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.webdav.xml.fields.ContactFields;

/**
 * ContactWriter
 *
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public class ContactWriter extends CommonWriter {
	
	protected final static int[] changeFields = {
		DataObject.OBJECT_ID,
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
		ContactObject.CATEGORIES,
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
		ContactObject.DISTRIBUTIONLIST,
		ContactObject.EMAIL1,
		ContactObject.EMAIL2,
		ContactObject.EMAIL3,
		ContactObject.EMPLOYEE_TYPE,
		ContactObject.FAX_BUSINESS,
		ContactObject.FAX_HOME,
		ContactObject.FAX_OTHER,
		ContactObject.FILE_AS,
		ContactObject.FOLDER_ID,
		ContactObject.GIVEN_NAME,
		ContactObject.IMAGE1,
		ContactObject.IMAGE1_CONTENT_TYPE,
		ContactObject.INFO,
		ContactObject.INSTANT_MESSENGER1,
		ContactObject.INSTANT_MESSENGER2,
		ContactObject.LINKS,
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
		ContactObject.DEFAULT_ADDRESS,
                ContactObject.NUMBER_OF_ATTACHMENTS
	};
	
	private ContactSQLInterface contactsql = null;
	
	protected final static int[] deleteFields = {
		DataObject.OBJECT_ID,
		DataObject.LAST_MODIFIED		
	};
	
	private static final Log LOG = LogFactory.getLog(ContactWriter.class);
	
	public ContactWriter() {
		
	}
	
	public ContactWriter(Session sessionObj, Context ctx) {
		this.sessionObj = sessionObj;
		this.ctx = ctx;
		contactsql = new RdbContactSQLInterface(sessionObj, ctx);
	}
	
	public void startWriter(int objectId, int folderId, OutputStream os) throws Exception {
		AppointmentSQLInterface appointmentsql = new CalendarSql(sessionObj);
		
		final Element eProp = new Element("prop", "D", "DAV:");
		final XMLOutputter xo = new XMLOutputter();
		try {
			final ContactObject contactobject = contactsql.getObjectById(objectId, folderId);
			writeObject(contactobject, eProp, false, xo, os);
		} catch (OXObjectNotFoundException exc) {
			writeResponseElement(eProp, 0, HttpServletResponse.SC_NOT_FOUND, XmlServlet.OBJECT_NOT_FOUND_EXCEPTION, xo, os);
		} catch (Exception ex) {
			writeResponseElement(eProp, 0, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, XmlServlet.SERVER_ERROR_EXCEPTION, xo, os);
		}
	}
	
	public void startWriter(boolean bModified, boolean bDeleted, boolean bList, int folder_id, Date lastsync, OutputStream os) throws Exception {
		XMLOutputter xo = new XMLOutputter();
		
		if (bModified) {
			SearchIterator it = null;
			try {
				it = contactsql.getModifiedContactsInFolder(folder_id, changeFields, lastsync);
				writeIterator(it, false, xo, os);
			} finally {
				if (it != null) {
					it.close();
				}
			}
		}
		
		if (bDeleted) {
			SearchIterator it = null;
			try {
				it = contactsql.getDeletedContactsInFolder(folder_id, deleteFields, lastsync);
				writeIterator(it, true, xo, os);
			} finally {
				if (it != null) {
					it.close();
				}
			}
		}
		
		if (bList) {
			SearchIterator it = null;
			try {
				it = contactsql.getContactsInFolder(folder_id, 0, 50000, 0, null, deleteFields);
				writeList(it, xo, os);
			} finally {
				if (it != null) {
					it.close();
				}
			}
		}
	}
	
	public void writeIterator(SearchIterator it, boolean delete, XMLOutputter xo, OutputStream os) throws Exception {
		while (it.hasNext()) {
			writeObject((ContactObject)it.next(), delete, xo, os);
		}
	}
	
	public void writeObject(ContactObject contactObj, boolean delete, XMLOutputter xo, OutputStream os) throws Exception {
		writeObject(contactObj, new Element("prop", "D", "DAV:"), delete, xo, os); 
	}
	
	public void writeObject(ContactObject contactObj, Element eProp, boolean delete, XMLOutputter xo, OutputStream os) throws Exception {
		int status = 200;
		String description = "OK";
		int object_id = 0;
		
		try {
			object_id = contactObj.getObjectID();
			if (contactObj.containsImage1()&& !delete) {
				ContactObject contactObjectWithImage = contactsql.getObjectById(object_id, contactObj.getParentFolderID());
				addContent2PropElement(eProp, contactObjectWithImage, delete);
			} else {
				addContent2PropElement(eProp, contactObj, delete);
			}
		} catch (Exception exc) {
			LOG.error("writeObject", exc);
			status = 500;
			description = "Server Error: " + exc.getMessage();
			object_id = 0;
		}
		
		writeResponseElement(eProp, object_id, status, description, xo, os);
	}
	
	protected void addContent2PropElement(Element e, ContactObject contactobject, boolean delete) throws Exception {
		addContent2PropElement(e, contactobject, delete, false);
	}
	
	protected void addContent2PropElement(Element e, ContactObject contactobject, boolean delete, boolean externalUser) throws Exception {
		if (delete) {
			addElement(ContactFields.OBJECT_ID, contactobject.getObjectID(), e);
			addElement(ContactFields.LAST_MODIFIED, contactobject.getLastModified(), e);
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
			
			if (contactobject.getNumberOfLinks() > 0) {
				writeLinks(contactobject, e);
			}
		}
	}
	
	protected void writeContactElement(ContactObject contactobject, Element e) throws Exception {
		addElement("object_status", "CREATE", e);
		addElement(ContactFields.LAST_NAME, contactobject.getSurName(), e);
		addElement(ContactFields.FIRST_NAME, contactobject.getGivenName(), e);
		addElement(ContactFields.ANNIVERSARY, contactobject.getAnniversary(), e);
		addElement(ContactFields.ASSISTANTS_NAME, contactobject.getAssistantName(), e);
		addElement(ContactFields.BIRTHDAY, contactobject.getBirthday(), e);
		addElement(ContactFields.BRANCHES, contactobject.getBranches(), e);
		addElement(ContactFields.BUSINESS_CATEGORY, contactobject.getBusinessCategory(), e);
		addElement(ContactFields.CATEGORIES, contactobject.getCategories(), e);
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
		addElement(ContactFields.EMAIL1, contactobject.getEmail1(), e);
		addElement(ContactFields.EMAIL2, contactobject.getEmail2(), e);
		addElement(ContactFields.EMAIL3, contactobject.getEmail3(), e);
		addElement(ContactFields.EMPLOYEE_TYPE, contactobject.getEmployeeType(), e);
		addElement(ContactFields.FAX_BUSINESS, contactobject.getFaxBusiness(), e);
		addElement(ContactFields.FAX_HOME, contactobject.getFaxHome(), e);
		addElement(ContactFields.FAX_OTHER, contactobject.getFaxOther(), e);
		addElement("fileas", contactobject.getFileAs(), e);
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
	
	protected void writeLinks(ContactObject contactobject, Element e_prop) throws Exception {
		Element e_links = new Element(ContactFields.LINKS, XmlServlet.NS);
		
		LinkEntryObject[] links = contactobject.getLinks();
		for (int a = 0; a < links.length; a++) {
			int id = links[a].getLinkID();
			String displayname = links[a].getLinkDisplayname();
			if (displayname == null) {
				displayname = String.valueOf(id);
			}
			
			Element e = new Element("link", XmlServlet.NS);
			e.addContent(String.valueOf(id));
			e.setAttribute("displayname", displayname, XmlServlet.NS);
			
			e_links.addContent(e);
		}
		
		e_prop.addContent(e_links);
	}
	
	protected void writeDistributionList(ContactObject contactobject, Element e_prop) throws Exception {
		Element e_distributionlist = new Element(ContactFields.DISTRIBUTIONLIST, XmlServlet.NS);
		
		DistributionListEntryObject[] distributionlist = contactobject.getDistributionList();
		for (int a = 0; a < distributionlist.length; a++) {
			String displayname = distributionlist[a].getDisplayname();
			String email = distributionlist[a].getEmailaddress();
			
			if (displayname == null) {
				displayname = email;
			}
			
			Element e = new Element("email", XmlServlet.NS);
			e.addContent(email);
			e.setAttribute("id", String.valueOf(distributionlist[a].getEntryID()), XmlServlet.NS);
			e.setAttribute(ContactFields.FOLDER_ID, String.valueOf(distributionlist[a].getFolderID()), XmlServlet.NS);
			e.setAttribute("displayname", displayname.trim(), XmlServlet.NS);
			e.setAttribute("emailfield", String.valueOf(distributionlist[a].getEmailfield()), XmlServlet.NS);
			
			e_distributionlist.addContent(e);
		}
		
		e_prop.addContent(e_distributionlist);
	}
	
	protected int getModule() {
		return Types.CONTACT;
	}
}




