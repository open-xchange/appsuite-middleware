/*
 *
 *    OPEN-XCHANGE - "the communication and information enviroment"
 *
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    OPEN-XCHANGE is a trademark of Netline Internet Service GmbH and all
 *    other brand and product names are or may be trademarks of, and are
 *    used to identify products or services of, their respective owners.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original code will still remain
 *    copyrighted by the copyright holder(s) or original author(s).
 *
 *
 *     Copyright (C) 1998 - 2005 Netline Internet Service GmbH
 *     mail:	                 info@netline-is.de
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License as published by the Free
 *     Software Foundation; either version 2 of the License, or (at your option)
 *     any later version.
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
 *
 */

package com.openexchange.webdav.xml.parser;

import com.openexchange.api.OXContact;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.groupware.container.LinkEntryObject;
import com.openexchange.webdav.xml.XmlServlet;
import com.sun.java_cup.internal.parser;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Element;

/**
 * ContactParser
 *
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public class ContactParser extends CommonParser {
	
	public ContactParser() {
		
	}
	
	protected void parse(ContactObject contactObj, Element eProp) throws Exception {
		if (hasElement(eProp.getChild("uid", XmlServlet.NS))) {
			contactObj.setInternalUserId(getValueAsInt(eProp.getChild("uid", XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(OXContact.LAST_NAME, XmlServlet.NS))) {
			contactObj.setSurName(getValue(eProp.getChild(OXContact.LAST_NAME, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.FIRST_NAME, XmlServlet.NS))) {
			contactObj.setGivenName(getValue(eProp.getChild(OXContact.FIRST_NAME, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.ANNIVERSARY, XmlServlet.NS))) {
			contactObj.setAnniversary(getValueAsDate(eProp.getChild(OXContact.ANNIVERSARY, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.ASSISTANTS_NAME, XmlServlet.NS))) {
			contactObj.setAssistantName(getValue(eProp.getChild(OXContact.ASSISTANTS_NAME, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.BIRTHDAY, XmlServlet.NS))) {
			contactObj.setBirthday(getValueAsDate(eProp.getChild(OXContact.BIRTHDAY, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.BRANCHES, XmlServlet.NS))) {
			contactObj.setBranches(getValue(eProp.getChild(OXContact.BRANCHES, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.CATEGORIES, XmlServlet.NS))) {
			contactObj.setCategories(getValue(eProp.getChild(OXContact.CATEGORIES, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.MOBILE1, XmlServlet.NS))) {
			contactObj.setCellularTelephone1(getValue(eProp.getChild(OXContact.MOBILE1, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.MOBILE2, XmlServlet.NS))) {
			contactObj.setCellularTelephone2(getValue(eProp.getChild(OXContact.MOBILE2, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.CITY, XmlServlet.NS))) {
			contactObj.setCityHome(getValue(eProp.getChild(OXContact.CITY, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.BUSINESS_CITY, XmlServlet.NS))) {
			contactObj.setCityBusiness(getValue(eProp.getChild(OXContact.BUSINESS_CITY, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.SECOND_CITY, XmlServlet.NS))) {
			contactObj.setCityOther(getValue(eProp.getChild(OXContact.SECOND_CITY, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.COMMERCIAL_REGISTER, XmlServlet.NS))) {
			contactObj.setCommercialRegister(getValue(eProp.getChild(OXContact.COMMERCIAL_REGISTER, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.COMPANY, XmlServlet.NS))) {
			contactObj.setCompany(getValue(eProp.getChild(OXContact.COMPANY, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.COUNTRY, XmlServlet.NS))) {
			contactObj.setCountryHome(getValue(eProp.getChild(OXContact.COUNTRY, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.BUSINESS_COUNTRY, XmlServlet.NS))) {
			contactObj.setCountryBusiness(getValue(eProp.getChild(OXContact.BUSINESS_COUNTRY, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.SECOND_COUNTRY, XmlServlet.NS))) {
			contactObj.setCountryOther(getValue(eProp.getChild(OXContact.SECOND_COUNTRY, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.DEPARTMENT, XmlServlet.NS))) {
			contactObj.setDepartment(getValue(eProp.getChild(OXContact.DEPARTMENT, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.DISPLAY_NAME, XmlServlet.NS))) {
			contactObj.setDisplayName(getValue(eProp.getChild(OXContact.DISPLAY_NAME, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.EMAIL1, XmlServlet.NS))) {
			contactObj.setEmail1(getValue(eProp.getChild(OXContact.EMAIL1, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.EMAIL2, XmlServlet.NS))) {
			contactObj.setEmail2(getValue(eProp.getChild(OXContact.EMAIL2, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.EMAIL3, XmlServlet.NS))) {
			contactObj.setEmail3(getValue(eProp.getChild(OXContact.EMAIL3, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.EMPLOYEE_TYPE, XmlServlet.NS))) {
			contactObj.setEmployeeType(getValue(eProp.getChild(OXContact.EMPLOYEE_TYPE, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.FAX_BUSINESS, XmlServlet.NS))) {
			contactObj.setFaxBusiness(getValue(eProp.getChild(OXContact.FAX_BUSINESS, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.FAX_HOME, XmlServlet.NS))) {
			contactObj.setFaxHome(getValue(eProp.getChild(OXContact.FAX_HOME, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.FAX_OTHER, XmlServlet.NS))) {
			contactObj.setFaxOther(getValue(eProp.getChild(OXContact.FAX_OTHER, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.IMAGE1, XmlServlet.NS))) {
			contactObj.setImage1(getValue(eProp.getChild(OXContact.IMAGE1, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.NOTE, XmlServlet.NS))) {
			contactObj.setNote(getValue(eProp.getChild(OXContact.NOTE, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.INSTANT_MESSENGER, XmlServlet.NS))) {
			contactObj.setInstantMessenger1(getValue(eProp.getChild(OXContact.INSTANT_MESSENGER, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.INSTANT_MESSENGER2, XmlServlet.NS))) {
			contactObj.setInstantMessenger2(getValue(eProp.getChild(OXContact.INSTANT_MESSENGER2, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild("uid", XmlServlet.NS))) {
			contactObj.setInternalUserId(getValueAsInt(eProp.getChild("uid", XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.MANAGERS_NAME, XmlServlet.NS))) {
			contactObj.setManagerName(getValue(eProp.getChild(OXContact.MANAGERS_NAME, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.SECOND_NAME, XmlServlet.NS))) {
			contactObj.setMiddleName(getValue(eProp.getChild(OXContact.SECOND_NAME, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.NICKNAME, XmlServlet.NS))) {
			contactObj.setNickname(getValue(eProp.getChild(OXContact.NICKNAME, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.NUMBER_OF_CHILDREN, XmlServlet.NS))) {
			contactObj.setNumberOfChildren(getValue(eProp.getChild(OXContact.NUMBER_OF_CHILDREN, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.NUMBER_OF_EMPLOYEE, XmlServlet.NS))) {
			contactObj.setNumberOfEmployee(getValue(eProp.getChild(OXContact.NUMBER_OF_EMPLOYEE, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.POSITION, XmlServlet.NS))) {
			contactObj.setPosition(getValue(eProp.getChild(OXContact.POSITION, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.POSTAL_CODE, XmlServlet.NS))) {
			contactObj.setPostalCodeHome(getValue(eProp.getChild(OXContact.POSTAL_CODE, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.BUSINESS_POSTAL_CODE, XmlServlet.NS))) {
			contactObj.setPostalCodeBusiness(getValue(eProp.getChild(OXContact.BUSINESS_POSTAL_CODE, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.SECOND_POSTAL_CODE, XmlServlet.NS))) {
			contactObj.setPostalCodeOther(getValue(eProp.getChild(OXContact.SECOND_POSTAL_CODE, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.PROFESSION, XmlServlet.NS))) {
			contactObj.setProfession(getValue(eProp.getChild(OXContact.PROFESSION, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.ROOM_NUMBER, XmlServlet.NS))) {
			contactObj.setRoomNumber(getValue(eProp.getChild(OXContact.ROOM_NUMBER, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.SALES_VOLUME, XmlServlet.NS))) {
			contactObj.setSalesVolume(getValue(eProp.getChild(OXContact.SALES_VOLUME, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.SPOUSE_NAME, XmlServlet.NS))) {
			contactObj.setSpouseName(getValue(eProp.getChild(OXContact.SPOUSE_NAME, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.STATE, XmlServlet.NS))) {
			contactObj.setStateHome(getValue(eProp.getChild(OXContact.STATE, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.BUSINESS_STATE, XmlServlet.NS))) {
			contactObj.setStreetBusiness(getValue(eProp.getChild(OXContact.BUSINESS_STATE, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.SECOND_STATE, XmlServlet.NS))) {
			contactObj.setStateOther(getValue(eProp.getChild(OXContact.SECOND_STATE, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.STREET, XmlServlet.NS))) {
			contactObj.setStreetHome(getValue(eProp.getChild(OXContact.STREET, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.BUSINESS_STREET, XmlServlet.NS))) {
			contactObj.setStreetBusiness(getValue(eProp.getChild(OXContact.BUSINESS_STREET, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.SECOND_STREET, XmlServlet.NS))) {
			contactObj.setStreetOther(getValue(eProp.getChild(OXContact.SECOND_STREET, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.SUFFIX, XmlServlet.NS))) {
			contactObj.setSuffix(getValue(eProp.getChild(OXContact.SUFFIX, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.TAX_ID, XmlServlet.NS))) {
			contactObj.setTaxID(getValue(eProp.getChild(OXContact.TAX_ID, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.PHONE_ASSISTANT, XmlServlet.NS))) {
			contactObj.setTelephoneAssistant(getValue(eProp.getChild(OXContact.PHONE_ASSISTANT, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.PHONE_BUSINESS, XmlServlet.NS))) {
			contactObj.setTelephoneBusiness1(getValue(eProp.getChild(OXContact.PHONE_BUSINESS, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.PHONE_BUSINESS2, XmlServlet.NS))) {
			contactObj.setTelephoneBusiness2(getValue(eProp.getChild(OXContact.PHONE_BUSINESS2, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.CALLBACK, XmlServlet.NS))) {
			contactObj.setTelephoneCallback(getValue(eProp.getChild(OXContact.CALLBACK, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.PHONE_CAR, XmlServlet.NS))) {
			contactObj.setTelephoneCar(getValue(eProp.getChild(OXContact.PHONE_CAR, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.PHONE_COMPANY, XmlServlet.NS))) {
			contactObj.setTelephoneCompany(getValue(eProp.getChild(OXContact.PHONE_COMPANY, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.PHONE_HOME, XmlServlet.NS))) {
			contactObj.setTelephoneHome1(getValue(eProp.getChild(OXContact.PHONE_HOME, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.PHONE_HOME2, XmlServlet.NS))) {
			contactObj.setTelephoneHome2(getValue(eProp.getChild(OXContact.PHONE_HOME2, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.IP_PHONE, XmlServlet.NS))) {
			contactObj.setTelephoneIP(getValue(eProp.getChild(OXContact.IP_PHONE, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.ISDN, XmlServlet.NS))) {
			contactObj.setTelephoneISDN(getValue(eProp.getChild(OXContact.ISDN, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.PHONE_OTHER, XmlServlet.NS))) {
			contactObj.setTelephoneOther(getValue(eProp.getChild(OXContact.PHONE_OTHER, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.PAGER, XmlServlet.NS))) {
			contactObj.setTelephonePager(getValue(eProp.getChild(OXContact.PAGER, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.PRIMARY, XmlServlet.NS))) {
			contactObj.setTelephonePrimary(getValue(eProp.getChild(OXContact.PRIMARY, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.RADIO, XmlServlet.NS))) {
			contactObj.setTelephoneRadio(getValue(eProp.getChild(OXContact.RADIO, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.TELEX, XmlServlet.NS))) {
			contactObj.setTelephoneTelex(getValue(eProp.getChild(OXContact.TELEX, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.TTY_TDD, XmlServlet.NS))) {
			contactObj.setTelephoneTTYTTD(getValue(eProp.getChild(OXContact.TTY_TDD, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.URL, XmlServlet.NS))) {
			contactObj.setURL(getValue(eProp.getChild(OXContact.URL, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.USERFIELD01, XmlServlet.NS))) {
			contactObj.setUserField01(getValue(eProp.getChild(OXContact.USERFIELD01, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.USERFIELD02, XmlServlet.NS))) {
			contactObj.setUserField02(getValue(eProp.getChild(OXContact.USERFIELD02, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.USERFIELD03, XmlServlet.NS))) {
			contactObj.setUserField03(getValue(eProp.getChild(OXContact.USERFIELD03, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.USERFIELD04, XmlServlet.NS))) {
			contactObj.setUserField04(getValue(eProp.getChild(OXContact.USERFIELD04, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.USERFIELD05, XmlServlet.NS))) {
			contactObj.setUserField05(getValue(eProp.getChild(OXContact.USERFIELD05, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.USERFIELD06, XmlServlet.NS))) {
			contactObj.setUserField06(getValue(eProp.getChild(OXContact.USERFIELD06, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.USERFIELD07, XmlServlet.NS))) {
			contactObj.setUserField07(getValue(eProp.getChild(OXContact.USERFIELD07, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.USERFIELD08, XmlServlet.NS))) {
			contactObj.setUserField08(getValue(eProp.getChild(OXContact.USERFIELD08, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.USERFIELD09, XmlServlet.NS))) {
			contactObj.setUserField09(getValue(eProp.getChild(OXContact.USERFIELD09, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.USERFIELD10, XmlServlet.NS))) {
			contactObj.setUserField10(getValue(eProp.getChild(OXContact.USERFIELD10, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.USERFIELD11, XmlServlet.NS))) {
			contactObj.setUserField11(getValue(eProp.getChild(OXContact.USERFIELD11, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.USERFIELD12, XmlServlet.NS))) {
			contactObj.setUserField12(getValue(eProp.getChild(OXContact.USERFIELD12, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.USERFIELD13, XmlServlet.NS))) {
			contactObj.setUserField13(getValue(eProp.getChild(OXContact.USERFIELD13, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.USERFIELD14, XmlServlet.NS))) {
			contactObj.setUserField14(getValue(eProp.getChild(OXContact.USERFIELD14, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.USERFIELD15, XmlServlet.NS))) {
			contactObj.setUserField15(getValue(eProp.getChild(OXContact.USERFIELD15, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.USERFIELD16, XmlServlet.NS))) {
			contactObj.setUserField16(getValue(eProp.getChild(OXContact.USERFIELD16, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.USERFIELD17, XmlServlet.NS))) {
			contactObj.setUserField17(getValue(eProp.getChild(OXContact.USERFIELD17, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.USERFIELD18, XmlServlet.NS))) {
			contactObj.setUserField18(getValue(eProp.getChild(OXContact.USERFIELD18, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.USERFIELD19, XmlServlet.NS))) {
			contactObj.setUserField19(getValue(eProp.getChild(OXContact.USERFIELD19, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.USERFIELD20, XmlServlet.NS))) {
			contactObj.setUserField20(getValue(eProp.getChild(OXContact.USERFIELD20, XmlServlet.NS)));
		}
		
		if (hasElement(eProp.getChild(OXContact.DISTRIBUTIONLIST, XmlServlet.NS))) {
			parseElementDistributionlists(contactObj, eProp.getChild(OXContact.DISTRIBUTIONLIST, XmlServlet.NS));
		}
		
		if (hasElement(eProp.getChild(OXContact.LINKS, XmlServlet.NS))) {
			parseElementLinks(contactObj, eProp.getChild(OXContact.LINKS, XmlServlet.NS));
		}
		parseElementCommon(contactObj, eProp);
	}
	
	
	protected void parseElementDistributionlists(ContactObject contactObj, Element eDistributionList) throws Exception {
		ArrayList distributionlist = new ArrayList();
		
		List elementEntries = eDistributionList.getChildren("email", XmlServlet.NS);
		
		for (int a = 0; a < elementEntries.size(); a++) {
			Element eEntry = (Element)elementEntries.get(a);
			
			DistributionListEntryObject entry = new DistributionListEntryObject();
			
			parseElementEntry(eEntry, entry);
			
			distributionlist.add(entry);
		}
		
		contactObj.setDistributionList((DistributionListEntryObject[])distributionlist.toArray(new DistributionListEntryObject[distributionlist.size()]));
	}
	
	protected void parseElementEntry(Element e, DistributionListEntryObject entry) throws Exception {
		String s = null;
		
		if ((s = e.getAttributeValue(OXContact.DistributionList.CONTACT_ID, XmlServlet.NS)) != null) {
			int contactId = Integer.parseInt(s);
			entry.setEntryID(contactId);
		}
		
		entry.setEmailfield(Integer.parseInt(e.getAttributeValue(OXContact.DistributionList.EMAILFIELD, XmlServlet.NS)));
		entry.setDisplayname(e.getAttributeValue(OXContact.DistributionList.DISPLAYNAME, XmlServlet.NS));
		entry.setEmailaddress(getValue(e));
	}
	
	protected void parseElementLinks(ContactObject contactObj, Element eLinks) throws Exception {
		ArrayList links = new ArrayList();
		
		List elementEntries = eLinks.getChildren("links", XmlServlet.NS);
		
		for (int a = 0; a < elementEntries.size(); a++) {
			Element eLink = (Element)elementEntries.get(a);
			
			LinkEntryObject link = new LinkEntryObject();
			
			parseElementLink(eLink, link);
			
			links.add(link);
		}
		
		contactObj.setLinks((LinkEntryObject[])links.toArray(new LinkEntryObject[links.size()]));
	}
	
	protected void parseElementLink(Element e, LinkEntryObject link) throws Exception {
		link.setLinkDisplayname(e.getAttributeValue(XmlServlet.NAMESPACE, OXContact.DistributionList.DISPLAYNAME));
		link.setLinkID(getValueAsInt(e));
	}
}




