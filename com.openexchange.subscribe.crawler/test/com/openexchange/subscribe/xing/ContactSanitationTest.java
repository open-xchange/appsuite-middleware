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

package com.openexchange.subscribe.xing;

import junit.framework.TestCase;
import com.openexchange.groupware.container.Contact;
import com.openexchange.subscribe.crawler.internal.ContactSanitizer;

/**
 * {@link ContactSanitationTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ContactSanitationTest extends TestCase {

    public void testSanitizesEmptyStrings() {
        Contact contact = new Contact();
        contact.setDisplayName("");
        contact.setGivenName("");
        contact.setSurName("");
        contact.setMiddleName("");
        contact.setSuffix("");
        contact.setTitle("");
        contact.setStreetHome("");
        contact.setPostalCodeHome("");
        contact.setCityHome("");
        contact.setStateHome("");
        contact.setCountryHome("");
        contact.setMaritalStatus("");
        contact.setNumberOfChildren("");
        contact.setProfession("");
        contact.setNickname("");
        contact.setSpouseName("");
        contact.setNote("");
        contact.setDepartment("");
        contact.setPosition("");
        contact.setEmployeeType("");
        contact.setRoomNumber("");
        contact.setStreetBusiness("");
        contact.setPostalCodeBusiness("");
        contact.setCityBusiness("");
        contact.setStateBusiness("");
        contact.setCountryBusiness("");
        contact.setNumberOfEmployee("");
        contact.setSalesVolume("");
        contact.setTaxID("");
        contact.setCommercialRegister("");
        contact.setBranches("");
        contact.setBusinessCategory("");
        contact.setInfo("");
        contact.setManagerName("");
        contact.setAssistantName("");
        contact.setStreetOther("");
        contact.setPostalCodeOther("");
        contact.setCityOther("");
        contact.setStateOther("");
        contact.setCountryOther("");
        contact.setTelephoneBusiness1("");
        contact.setTelephoneBusiness2("");
        contact.setFaxBusiness("");
        contact.setTelephoneCallback("");
        contact.setTelephoneCar("");
        contact.setTelephoneCompany("");
        contact.setTelephoneHome1("");
        contact.setTelephoneHome2("");
        contact.setFaxHome("");
        contact.setCellularTelephone1("");
        contact.setCellularTelephone2("");
        contact.setTelephoneOther("");
        contact.setFaxOther("");
        contact.setEmail1("");
        contact.setEmail2("");
        contact.setEmail3("");
        contact.setURL("");
        contact.setTelephoneISDN("");
        contact.setTelephonePager("");
        contact.setTelephonePrimary("");
        contact.setTelephoneRadio("");
        contact.setTelephoneTelex("");
        contact.setTelephoneTTYTTD("");
        contact.setInstantMessenger1("");
        contact.setInstantMessenger2("");
        contact.setTelephoneIP("");
        contact.setTelephoneAssistant("");
        contact.setCompany("");
        contact.setUserField01("");
        contact.setUserField02("");
        contact.setUserField03("");
        contact.setUserField04("");
        contact.setUserField05("");
        contact.setUserField06("");
        contact.setUserField07("");
        contact.setUserField08("");
        contact.setUserField09("");
        contact.setUserField10("");
        contact.setUserField11("");
        contact.setUserField12("");
        contact.setUserField13("");
        contact.setUserField14("");
        contact.setUserField15("");
        contact.setUserField16("");
        contact.setUserField17("");
        contact.setUserField18("");
        contact.setUserField19("");
        contact.setUserField20("");
        contact.setImageContentType("");
        contact.setFileAs("");

        new ContactSanitizer().sanitize(contact);

        assertFalse("DisplayName was not sanitized!", contact.containsDisplayName());
        assertFalse("GivenName was not sanitized!", contact.containsGivenName());
        assertFalse("SurName was not sanitized!", contact.containsSurName());
        assertFalse("MiddleName was not sanitized!", contact.containsMiddleName());
        assertFalse("Suffix was not sanitized!", contact.containsSuffix());
        assertFalse("Title was not sanitized!", contact.containsTitle());
        assertFalse("StreetHome was not sanitized!", contact.containsStreetHome());
        assertFalse("PostalCodeHome was not sanitized!", contact.containsPostalCodeHome());
        assertFalse("CityHome was not sanitized!", contact.containsCityHome());
        assertFalse("StateHome was not sanitized!", contact.containsStateHome());
        assertFalse("CountryHome was not sanitized!", contact.containsCountryHome());
        assertFalse("MaritalStatus was not sanitized!", contact.containsMaritalStatus());
        assertFalse("NumberOfChildren was not sanitized!", contact.containsNumberOfChildren());
        assertFalse("Profession was not sanitized!", contact.containsProfession());
        assertFalse("Nickname was not sanitized!", contact.containsNickname());
        assertFalse("SpouseName was not sanitized!", contact.containsSpouseName());
        assertFalse("Note was not sanitized!", contact.containsNote());
        assertFalse("Department was not sanitized!", contact.containsDepartment());
        assertFalse("Position was not sanitized!", contact.containsPosition());
        assertFalse("EmployeeType was not sanitized!", contact.containsEmployeeType());
        assertFalse("RoomNumber was not sanitized!", contact.containsRoomNumber());
        assertFalse("StreetBusiness was not sanitized!", contact.containsStreetBusiness());
        assertFalse("PostalCodeBusiness was not sanitized!", contact.containsPostalCodeBusiness());
        assertFalse("CityBusiness was not sanitized!", contact.containsCityBusiness());
        assertFalse("StateBusiness was not sanitized!", contact.containsStateBusiness());
        assertFalse("CountryBusiness was not sanitized!", contact.containsCountryBusiness());
        assertFalse("NumberOfEmployee was not sanitized!", contact.containsNumberOfEmployee());
        assertFalse("SalesVolume was not sanitized!", contact.containsSalesVolume());
        assertFalse("TaxID was not sanitized!", contact.containsTaxID());
        assertFalse("CommercialRegister was not sanitized!", contact.containsCommercialRegister());
        assertFalse("Branches was not sanitized!", contact.containsBranches());
        assertFalse("BusinessCategory was not sanitized!", contact.containsBusinessCategory());
        assertFalse("Info was not sanitized!", contact.containsInfo());
        assertFalse("ManagerName was not sanitized!", contact.containsManagerName());
        assertFalse("AssistantName was not sanitized!", contact.containsAssistantName());
        assertFalse("StreetOther was not sanitized!", contact.containsStreetOther());
        assertFalse("PostalCodeOther was not sanitized!", contact.containsPostalCodeOther());
        assertFalse("CityOther was not sanitized!", contact.containsCityOther());
        assertFalse("StateOther was not sanitized!", contact.containsStateOther());
        assertFalse("CountryOther was not sanitized!", contact.containsCountryOther());
        assertFalse("TelephoneBusiness1 was not sanitized!", contact.containsTelephoneBusiness1());
        assertFalse("TelephoneBusiness2 was not sanitized!", contact.containsTelephoneBusiness2());
        assertFalse("FaxBusiness was not sanitized!", contact.containsFaxBusiness());
        assertFalse("TelephoneCallback was not sanitized!", contact.containsTelephoneCallback());
        assertFalse("TelephoneCar was not sanitized!", contact.containsTelephoneCar());
        assertFalse("TelephoneCompany was not sanitized!", contact.containsTelephoneCompany());
        assertFalse("TelephoneHome1 was not sanitized!", contact.containsTelephoneHome1());
        assertFalse("TelephoneHome2 was not sanitized!", contact.containsTelephoneHome2());
        assertFalse("FaxHome was not sanitized!", contact.containsFaxHome());
        assertFalse("CellularTelephone1 was not sanitized!", contact.containsCellularTelephone1());
        assertFalse("CellularTelephone2 was not sanitized!", contact.containsCellularTelephone2());
        assertFalse("TelephoneOther was not sanitized!", contact.containsTelephoneOther());
        assertFalse("FaxOther was not sanitized!", contact.containsFaxOther());
        assertFalse("Email1 was not sanitized!", contact.containsEmail1());
        assertFalse("Email2 was not sanitized!", contact.containsEmail2());
        assertFalse("Email3 was not sanitized!", contact.containsEmail3());
        assertFalse("URL was not sanitized!", contact.containsURL());
        assertFalse("TelephoneISDN was not sanitized!", contact.containsTelephoneISDN());
        assertFalse("TelephonePager was not sanitized!", contact.containsTelephonePager());
        assertFalse("TelephonePrimary was not sanitized!", contact.containsTelephonePrimary());
        assertFalse("TelephoneRadio was not sanitized!", contact.containsTelephoneRadio());
        assertFalse("TelephoneTelex was not sanitized!", contact.containsTelephoneTelex());
        assertFalse("TelephoneTTYTTD was not sanitized!", contact.containsTelephoneTTYTTD());
        assertFalse("InstantMessenger1 was not sanitized!", contact.containsInstantMessenger1());
        assertFalse("InstantMessenger2 was not sanitized!", contact.containsInstantMessenger2());
        assertFalse("TelephoneIP was not sanitized!", contact.containsTelephoneIP());
        assertFalse("TelephoneAssistant was not sanitized!", contact.containsTelephoneAssistant());
        assertFalse("Company was not sanitized!", contact.containsCompany());
        assertFalse("UserField01 was not sanitized!", contact.containsUserField01());
        assertFalse("UserField02 was not sanitized!", contact.containsUserField02());
        assertFalse("UserField03 was not sanitized!", contact.containsUserField03());
        assertFalse("UserField04 was not sanitized!", contact.containsUserField04());
        assertFalse("UserField05 was not sanitized!", contact.containsUserField05());
        assertFalse("UserField06 was not sanitized!", contact.containsUserField06());
        assertFalse("UserField07 was not sanitized!", contact.containsUserField07());
        assertFalse("UserField08 was not sanitized!", contact.containsUserField08());
        assertFalse("UserField09 was not sanitized!", contact.containsUserField09());
        assertFalse("UserField10 was not sanitized!", contact.containsUserField10());
        assertFalse("UserField11 was not sanitized!", contact.containsUserField11());
        assertFalse("UserField12 was not sanitized!", contact.containsUserField12());
        assertFalse("UserField13 was not sanitized!", contact.containsUserField13());
        assertFalse("UserField14 was not sanitized!", contact.containsUserField14());
        assertFalse("UserField15 was not sanitized!", contact.containsUserField15());
        assertFalse("UserField16 was not sanitized!", contact.containsUserField16());
        assertFalse("UserField17 was not sanitized!", contact.containsUserField17());
        assertFalse("UserField18 was not sanitized!", contact.containsUserField18());
        assertFalse("UserField19 was not sanitized!", contact.containsUserField19());
        assertFalse("UserField20 was not sanitized!", contact.containsUserField20());
        assertFalse("ImageContentType was not sanitized!", contact.containsImageContentType());
        assertFalse("FileAs was not sanitized!", contact.containsFileAs());
    }
}
