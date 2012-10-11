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
 *     mail:	                 info@open-xchange.com
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

import java.util.ArrayList;
import java.util.List;
import org.jdom2.Element;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.groupware.container.LinkEntryObject;
import com.openexchange.webdav.xml.XmlServlet;
import com.openexchange.webdav.xml.fields.ContactFields;

/**
 * ContactParser
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */

public class ContactParser extends CommonParser {

	public ContactParser() {

	}

	protected void parse(final Contact contactObj, final Element eProp) {
		if (hasElement(eProp.getChild("uid", XmlServlet.NS))) {
			contactObj.setInternalUserId(getValueAsInt(eProp.getChild("uid", XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.LAST_NAME, XmlServlet.NS))) {
			contactObj.setSurName(getValue(eProp.getChild(ContactFields.LAST_NAME, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.FIRST_NAME, XmlServlet.NS))) {
			contactObj.setGivenName(getValue(eProp.getChild(ContactFields.FIRST_NAME, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.ANNIVERSARY, XmlServlet.NS))) {
			contactObj.setAnniversary(getValueAsDate(eProp.getChild(ContactFields.ANNIVERSARY, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.ASSISTANTS_NAME, XmlServlet.NS))) {
			contactObj.setAssistantName(getValue(eProp.getChild(ContactFields.ASSISTANTS_NAME, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.BIRTHDAY, XmlServlet.NS))) {
			contactObj.setBirthday(getValueAsDate(eProp.getChild(ContactFields.BIRTHDAY, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.BRANCHES, XmlServlet.NS))) {
			contactObj.setBranches(getValue(eProp.getChild(ContactFields.BRANCHES, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.CATEGORIES, XmlServlet.NS))) {
			contactObj.setCategories(getValue(eProp.getChild(ContactFields.CATEGORIES, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.BUSINESS_CATEGORY, XmlServlet.NS))) {
			contactObj.setBusinessCategory(getValue(eProp.getChild(ContactFields.BUSINESS_CATEGORY, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.MOBILE1, XmlServlet.NS))) {
			contactObj.setCellularTelephone1(getValue(eProp.getChild(ContactFields.MOBILE1, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.MOBILE2, XmlServlet.NS))) {
			contactObj.setCellularTelephone2(getValue(eProp.getChild(ContactFields.MOBILE2, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.CITY, XmlServlet.NS))) {
			contactObj.setCityHome(getValue(eProp.getChild(ContactFields.CITY, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.BUSINESS_CITY, XmlServlet.NS))) {
			contactObj.setCityBusiness(getValue(eProp.getChild(ContactFields.BUSINESS_CITY, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.SECOND_CITY, XmlServlet.NS))) {
			contactObj.setCityOther(getValue(eProp.getChild(ContactFields.SECOND_CITY, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.COMMERCIAL_REGISTER, XmlServlet.NS))) {
			contactObj.setCommercialRegister(getValue(eProp.getChild(ContactFields.COMMERCIAL_REGISTER, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.COMPANY, XmlServlet.NS))) {
			contactObj.setCompany(getValue(eProp.getChild(ContactFields.COMPANY, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.COUNTRY, XmlServlet.NS))) {
			contactObj.setCountryHome(getValue(eProp.getChild(ContactFields.COUNTRY, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.BUSINESS_COUNTRY, XmlServlet.NS))) {
			contactObj.setCountryBusiness(getValue(eProp.getChild(ContactFields.BUSINESS_COUNTRY, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.SECOND_COUNTRY, XmlServlet.NS))) {
			contactObj.setCountryOther(getValue(eProp.getChild(ContactFields.SECOND_COUNTRY, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.DEPARTMENT, XmlServlet.NS))) {
			contactObj.setDepartment(getValue(eProp.getChild(ContactFields.DEPARTMENT, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.DISPLAY_NAME, XmlServlet.NS))) {
			contactObj.setDisplayName(getValue(eProp.getChild(ContactFields.DISPLAY_NAME, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.EMAIL1, XmlServlet.NS))) {
			contactObj.setEmail1(getValue(eProp.getChild(ContactFields.EMAIL1, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.EMAIL2, XmlServlet.NS))) {
			contactObj.setEmail2(getValue(eProp.getChild(ContactFields.EMAIL2, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.EMAIL3, XmlServlet.NS))) {
			contactObj.setEmail3(getValue(eProp.getChild(ContactFields.EMAIL3, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.EMPLOYEE_TYPE, XmlServlet.NS))) {
			contactObj.setEmployeeType(getValue(eProp.getChild(ContactFields.EMPLOYEE_TYPE, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.FAX_BUSINESS, XmlServlet.NS))) {
			contactObj.setFaxBusiness(getValue(eProp.getChild(ContactFields.FAX_BUSINESS, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.FAX_HOME, XmlServlet.NS))) {
			contactObj.setFaxHome(getValue(eProp.getChild(ContactFields.FAX_HOME, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.FAX_OTHER, XmlServlet.NS))) {
			contactObj.setFaxOther(getValue(eProp.getChild(ContactFields.FAX_OTHER, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.IMAGE1, XmlServlet.NS))) {
			contactObj.setImage1(getValue(eProp.getChild(ContactFields.IMAGE1, XmlServlet.NS)).getBytes());
		}

		if (hasElement(eProp.getChild(ContactFields.IMAGE_CONTENT_TYPE, XmlServlet.NS))) {
			contactObj.setImageContentType(getValue(eProp.getChild(ContactFields.IMAGE_CONTENT_TYPE, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.NOTE, XmlServlet.NS))) {
			contactObj.setNote(getValue(eProp.getChild(ContactFields.NOTE, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.MORE_INFO, XmlServlet.NS))) {
			contactObj.setInfo(getValue(eProp.getChild(ContactFields.MORE_INFO, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.MARTITAL_STATUS, XmlServlet.NS))) {
			contactObj.setMaritalStatus(getValue(eProp.getChild(ContactFields.MARTITAL_STATUS, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.INSTANT_MESSENGER, XmlServlet.NS))) {
			contactObj.setInstantMessenger1(getValue(eProp.getChild(ContactFields.INSTANT_MESSENGER, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.INSTANT_MESSENGER2, XmlServlet.NS))) {
			contactObj.setInstantMessenger2(getValue(eProp.getChild(ContactFields.INSTANT_MESSENGER2, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild("uid", XmlServlet.NS))) {
			contactObj.setInternalUserId(getValueAsInt(eProp.getChild("uid", XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.MANAGERS_NAME, XmlServlet.NS))) {
			contactObj.setManagerName(getValue(eProp.getChild(ContactFields.MANAGERS_NAME, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.SECOND_NAME, XmlServlet.NS))) {
			contactObj.setMiddleName(getValue(eProp.getChild(ContactFields.SECOND_NAME, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.NICKNAME, XmlServlet.NS))) {
			contactObj.setNickname(getValue(eProp.getChild(ContactFields.NICKNAME, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.NUMBER_OF_CHILDREN, XmlServlet.NS))) {
			contactObj.setNumberOfChildren(getValue(eProp.getChild(ContactFields.NUMBER_OF_CHILDREN, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.NUMBER_OF_EMPLOYEE, XmlServlet.NS))) {
			contactObj.setNumberOfEmployee(getValue(eProp.getChild(ContactFields.NUMBER_OF_EMPLOYEE, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.POSITION, XmlServlet.NS))) {
			contactObj.setPosition(getValue(eProp.getChild(ContactFields.POSITION, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.POSTAL_CODE, XmlServlet.NS))) {
			contactObj.setPostalCodeHome(getValue(eProp.getChild(ContactFields.POSTAL_CODE, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.BUSINESS_POSTAL_CODE, XmlServlet.NS))) {
			contactObj.setPostalCodeBusiness(getValue(eProp.getChild(ContactFields.BUSINESS_POSTAL_CODE, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.SECOND_POSTAL_CODE, XmlServlet.NS))) {
			contactObj.setPostalCodeOther(getValue(eProp.getChild(ContactFields.SECOND_POSTAL_CODE, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.PROFESSION, XmlServlet.NS))) {
			contactObj.setProfession(getValue(eProp.getChild(ContactFields.PROFESSION, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.ROOM_NUMBER, XmlServlet.NS))) {
			contactObj.setRoomNumber(getValue(eProp.getChild(ContactFields.ROOM_NUMBER, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.SALES_VOLUME, XmlServlet.NS))) {
			contactObj.setSalesVolume(getValue(eProp.getChild(ContactFields.SALES_VOLUME, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.SPOUSE_NAME, XmlServlet.NS))) {
			contactObj.setSpouseName(getValue(eProp.getChild(ContactFields.SPOUSE_NAME, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.STATE, XmlServlet.NS))) {
			contactObj.setStateHome(getValue(eProp.getChild(ContactFields.STATE, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.BUSINESS_STATE, XmlServlet.NS))) {
			contactObj.setStateBusiness(getValue(eProp.getChild(ContactFields.BUSINESS_STATE, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.SECOND_STATE, XmlServlet.NS))) {
			contactObj.setStateOther(getValue(eProp.getChild(ContactFields.SECOND_STATE, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.STREET, XmlServlet.NS))) {
			contactObj.setStreetHome(getValue(eProp.getChild(ContactFields.STREET, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.BUSINESS_STREET, XmlServlet.NS))) {
			contactObj.setStreetBusiness(getValue(eProp.getChild(ContactFields.BUSINESS_STREET, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.SECOND_STREET, XmlServlet.NS))) {
			contactObj.setStreetOther(getValue(eProp.getChild(ContactFields.SECOND_STREET, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.SUFFIX, XmlServlet.NS))) {
			contactObj.setSuffix(getValue(eProp.getChild(ContactFields.SUFFIX, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.TAX_ID, XmlServlet.NS))) {
			contactObj.setTaxID(getValue(eProp.getChild(ContactFields.TAX_ID, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.PHONE_ASSISTANT, XmlServlet.NS))) {
			contactObj.setTelephoneAssistant(getValue(eProp.getChild(ContactFields.PHONE_ASSISTANT, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.PHONE_BUSINESS, XmlServlet.NS))) {
			contactObj.setTelephoneBusiness1(getValue(eProp.getChild(ContactFields.PHONE_BUSINESS, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.PHONE_BUSINESS2, XmlServlet.NS))) {
			contactObj.setTelephoneBusiness2(getValue(eProp.getChild(ContactFields.PHONE_BUSINESS2, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.CALLBACK, XmlServlet.NS))) {
			contactObj.setTelephoneCallback(getValue(eProp.getChild(ContactFields.CALLBACK, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.PHONE_CAR, XmlServlet.NS))) {
			contactObj.setTelephoneCar(getValue(eProp.getChild(ContactFields.PHONE_CAR, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.PHONE_COMPANY, XmlServlet.NS))) {
			contactObj.setTelephoneCompany(getValue(eProp.getChild(ContactFields.PHONE_COMPANY, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.PHONE_HOME, XmlServlet.NS))) {
			contactObj.setTelephoneHome1(getValue(eProp.getChild(ContactFields.PHONE_HOME, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.PHONE_HOME2, XmlServlet.NS))) {
			contactObj.setTelephoneHome2(getValue(eProp.getChild(ContactFields.PHONE_HOME2, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.IP_PHONE, XmlServlet.NS))) {
			contactObj.setTelephoneIP(getValue(eProp.getChild(ContactFields.IP_PHONE, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.ISDN, XmlServlet.NS))) {
			contactObj.setTelephoneISDN(getValue(eProp.getChild(ContactFields.ISDN, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.PHONE_OTHER, XmlServlet.NS))) {
			contactObj.setTelephoneOther(getValue(eProp.getChild(ContactFields.PHONE_OTHER, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.PAGER, XmlServlet.NS))) {
			contactObj.setTelephonePager(getValue(eProp.getChild(ContactFields.PAGER, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.PRIMARY, XmlServlet.NS))) {
			contactObj.setTelephonePrimary(getValue(eProp.getChild(ContactFields.PRIMARY, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.RADIO, XmlServlet.NS))) {
			contactObj.setTelephoneRadio(getValue(eProp.getChild(ContactFields.RADIO, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.TELEX, XmlServlet.NS))) {
			contactObj.setTelephoneTelex(getValue(eProp.getChild(ContactFields.TELEX, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.TTY_TDD, XmlServlet.NS))) {
			contactObj.setTelephoneTTYTTD(getValue(eProp.getChild(ContactFields.TTY_TDD, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.URL, XmlServlet.NS))) {
			contactObj.setURL(getValue(eProp.getChild(ContactFields.URL, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.TITLE, XmlServlet.NS))) {
			contactObj.setTitle(getValue(eProp.getChild(ContactFields.TITLE, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.USERFIELD01, XmlServlet.NS))) {
			contactObj.setUserField01(getValue(eProp.getChild(ContactFields.USERFIELD01, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.USERFIELD02, XmlServlet.NS))) {
			contactObj.setUserField02(getValue(eProp.getChild(ContactFields.USERFIELD02, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.USERFIELD03, XmlServlet.NS))) {
			contactObj.setUserField03(getValue(eProp.getChild(ContactFields.USERFIELD03, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.USERFIELD04, XmlServlet.NS))) {
			contactObj.setUserField04(getValue(eProp.getChild(ContactFields.USERFIELD04, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.USERFIELD05, XmlServlet.NS))) {
			contactObj.setUserField05(getValue(eProp.getChild(ContactFields.USERFIELD05, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.USERFIELD06, XmlServlet.NS))) {
			contactObj.setUserField06(getValue(eProp.getChild(ContactFields.USERFIELD06, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.USERFIELD07, XmlServlet.NS))) {
			contactObj.setUserField07(getValue(eProp.getChild(ContactFields.USERFIELD07, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.USERFIELD08, XmlServlet.NS))) {
			contactObj.setUserField08(getValue(eProp.getChild(ContactFields.USERFIELD08, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.USERFIELD09, XmlServlet.NS))) {
			contactObj.setUserField09(getValue(eProp.getChild(ContactFields.USERFIELD09, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.USERFIELD10, XmlServlet.NS))) {
			contactObj.setUserField10(getValue(eProp.getChild(ContactFields.USERFIELD10, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.USERFIELD11, XmlServlet.NS))) {
			contactObj.setUserField11(getValue(eProp.getChild(ContactFields.USERFIELD11, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.USERFIELD12, XmlServlet.NS))) {
			contactObj.setUserField12(getValue(eProp.getChild(ContactFields.USERFIELD12, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.USERFIELD13, XmlServlet.NS))) {
			contactObj.setUserField13(getValue(eProp.getChild(ContactFields.USERFIELD13, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.USERFIELD14, XmlServlet.NS))) {
			contactObj.setUserField14(getValue(eProp.getChild(ContactFields.USERFIELD14, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.USERFIELD15, XmlServlet.NS))) {
			contactObj.setUserField15(getValue(eProp.getChild(ContactFields.USERFIELD15, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.USERFIELD16, XmlServlet.NS))) {
			contactObj.setUserField16(getValue(eProp.getChild(ContactFields.USERFIELD16, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.USERFIELD17, XmlServlet.NS))) {
			contactObj.setUserField17(getValue(eProp.getChild(ContactFields.USERFIELD17, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.USERFIELD18, XmlServlet.NS))) {
			contactObj.setUserField18(getValue(eProp.getChild(ContactFields.USERFIELD18, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.USERFIELD19, XmlServlet.NS))) {
			contactObj.setUserField19(getValue(eProp.getChild(ContactFields.USERFIELD19, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.USERFIELD20, XmlServlet.NS))) {
			contactObj.setUserField20(getValue(eProp.getChild(ContactFields.USERFIELD20, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.DEFAULTADDRESS, XmlServlet.NS))) {
			contactObj.setDefaultAddress(getValueAsInt(eProp.getChild(ContactFields.DEFAULTADDRESS, XmlServlet.NS)));
		}

		if (hasElement(eProp.getChild(ContactFields.DISTRIBUTIONLIST, XmlServlet.NS))) {
			parseElementDistributionlists(contactObj, eProp.getChild(ContactFields.DISTRIBUTIONLIST, XmlServlet.NS));
		}

		if (hasElement(eProp.getChild(ContactFields.LINKS, XmlServlet.NS))) {
			parseElementLinks(contactObj, eProp.getChild(ContactFields.LINKS, XmlServlet.NS));
		}
		parseElementCommon(contactObj, eProp);
	}


	protected void parseElementDistributionlists(final Contact contactObj, final Element eDistributionList) {
		final ArrayList distributionlist = new ArrayList();

		final List elementEntries = eDistributionList.getChildren("email", XmlServlet.NS);

		Next: for (int a = 0; a < elementEntries.size(); a++) {
			final Element eEntry = (Element)elementEntries.get(a);

			final DistributionListEntryObject entry = new DistributionListEntryObject();

			try {
				parseElementEntry(eEntry, entry);
			} catch (final Exception e) {
				e.printStackTrace();
				continue Next;
			}

			distributionlist.add(entry);
		}

		contactObj.setDistributionList((DistributionListEntryObject[])distributionlist.toArray(new DistributionListEntryObject[distributionlist.size()]));
	}

	protected void parseElementEntry(final Element e, final DistributionListEntryObject entry) throws Exception {
		String s = null;

		if ((s = e.getAttributeValue(ContactFields.ID, XmlServlet.NS)) != null) {
			final int contactId = Integer.parseInt(s);
			entry.setEntryID(contactId);
		}

		entry.setEmailfield(Integer.parseInt(e.getAttributeValue("emailfield", XmlServlet.NS)));
		entry.setDisplayname(e.getAttributeValue(ContactFields.DISPLAY_NAME, XmlServlet.NS));
		entry.setEmailaddress(getValue(e));
	}

	protected void parseElementLinks(final Contact contactObj, final Element eLinks) {
		final ArrayList links = new ArrayList();

		final List elementEntries = eLinks.getChildren("link", XmlServlet.NS);

		for (int a = 0; a < elementEntries.size(); a++) {
			final Element eLink = (Element)elementEntries.get(a);

			final LinkEntryObject link = new LinkEntryObject();

			parseElementLink(eLink, link);

			links.add(link);
		}

		contactObj.setLinks((LinkEntryObject[])links.toArray(new LinkEntryObject[links.size()]));
	}

	protected void parseElementLink(final Element e, final LinkEntryObject link) {
		link.setLinkDisplayname(e.getAttributeValue(ContactFields.DISPLAY_NAME, XmlServlet.NS));
		link.setLinkID(getValueAsInt(e));
	}
}




