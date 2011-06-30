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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.groupware.contact.helpers;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import com.openexchange.groupware.contact.ContactException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.container.Contact;

/**
 * This switcher enables us to get the values of a contact object. As convention, the first argument of a method represents the
 * ContactObject which value is to be retrieved. Note: This class was generated mostly - don't even try to keep this up to date by hand...
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 */
public class ContactGetter implements ContactSwitcher {

    public Object displayname(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("DisplayName");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getDisplayName();
    }

    public Object surname(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("SurName");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getSurName();
    }

    public Object givenname(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("GivenName");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getGivenName();
    }

    public Object middlename(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("MiddleName");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getMiddleName();
    }

    public Object suffix(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("Suffix");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getSuffix();
    }

    public Object title(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("Title");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getTitle();
    }

    public Object streethome(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("StreetHome");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getStreetHome();
    }

    public Object postalcodehome(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("PostalCodeHome");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getPostalCodeHome();
    }

    public Object cityhome(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("CityHome");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getCityHome();
    }

    public Object statehome(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("StateHome");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getStateHome();
    }

    public Object countryhome(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("CountryHome");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getCountryHome();
    }

    public Object maritalstatus(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("MaritalStatus");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getMaritalStatus();
    }

    public Object numberofchildren(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("NumberOfChildren");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getNumberOfChildren();
    }

    public Object profession(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("Profession");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getProfession();
    }

    public Object nickname(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("Nickname");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getNickname();
    }

    public Object spousename(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("SpouseName");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getSpouseName();
    }

    public Object note(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("Note");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getNote();
    }

    public Object company(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("Company");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getCompany();
    }

    public Object department(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("Department");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getDepartment();
    }

    public Object position(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("Position");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getPosition();
    }

    public Object employeetype(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("EmployeeType");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getEmployeeType();
    }

    public Object roomnumber(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("RoomNumber");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getRoomNumber();
    }

    public Object streetbusiness(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("StreetBusiness");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getStreetBusiness();
    }

    public Object postalcodebusiness(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("PostalCodeBusiness");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getPostalCodeBusiness();
    }

    public Object citybusiness(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("CityBusiness");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getCityBusiness();
    }

    public Object statebusiness(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("StateBusiness");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getStateBusiness();
    }

    public Object countrybusiness(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("CountryBusiness");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getCountryBusiness();
    }

    public Object numberofemployee(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("NumberOfEmployee");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getNumberOfEmployee();
    }

    public Object salesvolume(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("SalesVolume");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getSalesVolume();
    }

    public Object taxid(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("TaxID");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getTaxID();
    }

    public Object commercialregister(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("CommercialRegister");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getCommercialRegister();
    }

    public Object branches(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("Branches");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getBranches();
    }

    public Object businesscategory(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("BusinessCategory");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getBusinessCategory();
    }

    public Object info(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("Info");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getInfo();
    }

    public Object managername(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("ManagerName");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getManagerName();
    }

    public Object assistantname(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("AssistantName");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getAssistantName();
    }

    public Object streetother(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("StreetOther");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getStreetOther();
    }

    public Object postalcodeother(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("PostalCodeOther");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getPostalCodeOther();
    }

    public Object cityother(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("CityOther");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getCityOther();
    }

    public Object stateother(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("StateOther");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getStateOther();
    }

    public Object countryother(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("CountryOther");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getCountryOther();
    }

    public Object telephoneassistant(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("TelephoneAssistant");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getTelephoneAssistant();
    }

    public Object telephonebusiness1(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("TelephoneBusiness1");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getTelephoneBusiness1();
    }

    public Object telephonebusiness2(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("TelephoneBusiness2");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getTelephoneBusiness2();
    }

    public Object faxbusiness(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("FaxBusiness");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getFaxBusiness();
    }

    public Object telephonecallback(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("TelephoneCallback");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getTelephoneCallback();
    }

    public Object telephonecar(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("TelephoneCar");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getTelephoneCar();
    }

    public Object telephonecompany(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("TelephoneCompany");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getTelephoneCompany();
    }

    public Object telephonehome1(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("TelephoneHome1");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getTelephoneHome1();
    }

    public Object telephonehome2(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("TelephoneHome2");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getTelephoneHome2();
    }

    public Object faxhome(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("FaxHome");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getFaxHome();
    }

    public Object telephoneisdn(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("TelephoneISDN");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getTelephoneISDN();
    }

    public Object cellulartelephone1(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("CellularTelephone1");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getCellularTelephone1();
    }

    public Object cellulartelephone2(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("CellularTelephone2");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getCellularTelephone2();
    }

    public Object telephoneother(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("TelephoneOther");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getTelephoneOther();
    }

    public Object faxother(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("FaxOther");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getFaxOther();
    }

    public Object telephonepager(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("TelephonePager");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getTelephonePager();
    }

    public Object telephoneprimary(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("TelephonePrimary");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getTelephonePrimary();
    }

    public Object telephoneradio(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("TelephoneRadio");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getTelephoneRadio();
    }

    public Object telephonetelex(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("TelephoneTelex");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getTelephoneTelex();
    }

    public Object telephonettyttd(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("TelephoneTTYTTD");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getTelephoneTTYTTD();
    }

    public Object instantmessenger1(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("InstantMessenger1");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getInstantMessenger1();
    }

    public Object instantmessenger2(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("InstantMessenger2");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getInstantMessenger2();
    }

    public Object telephoneip(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("TelephoneIP");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getTelephoneIP();
    }

    public Object email1(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("Email1");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getEmail1();
    }

    public Object email2(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("Email2");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getEmail2();
    }

    public Object email3(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("Email3");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getEmail3();
    }

    public Object url(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("URL");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getURL();
    }

    public Object categories(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("Categories");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getCategories();
    }

    public Object userfield01(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("UserField01");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getUserField01();
    }

    public Object userfield02(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("UserField02");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getUserField02();
    }

    public Object userfield03(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("UserField03");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getUserField03();
    }

    public Object userfield04(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("UserField04");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getUserField04();
    }

    public Object userfield05(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("UserField05");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getUserField05();
    }

    public Object userfield06(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("UserField06");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getUserField06();
    }

    public Object userfield07(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("UserField07");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getUserField07();
    }

    public Object userfield08(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("UserField08");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getUserField08();
    }

    public Object userfield09(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("UserField09");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getUserField09();
    }

    public Object userfield10(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("UserField10");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getUserField10();
    }

    public Object userfield11(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("UserField11");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getUserField11();
    }

    public Object userfield12(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("UserField12");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getUserField12();
    }

    public Object userfield13(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("UserField13");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getUserField13();
    }

    public Object userfield14(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("UserField14");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getUserField14();
    }

    public Object userfield15(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("UserField15");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getUserField15();
    }

    public Object userfield16(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("UserField16");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getUserField16();
    }

    public Object userfield17(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("UserField17");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getUserField17();
    }

    public Object userfield18(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("UserField18");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getUserField18();
    }

    public Object userfield19(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("UserField19");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getUserField19();
    }

    public Object userfield20(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("UserField20");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getUserField20();
    }

    public Object objectid(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("ObjectID");
        }
        final Contact conObj = (Contact) objects[0];
        return I(conObj.getObjectID());
    }

    public Object numberofdistributionlists(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("NumberOfDistributionLists");
        }
        final Contact conObj = (Contact) objects[0];
        return I(conObj.getNumberOfDistributionLists());
    }

    public Object numberoflinks(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("NumberOfLinks");
        }
        final Contact conObj = (Contact) objects[0];
        return I(conObj.getNumberOfLinks());
    }

    public Object distributionlist(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("DistributionList");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getDistributionList();
    }

    public Object links(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("Links");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getLinks();
    }

    public Object parentfolderid(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("ParentFolderID");
        }
        final Contact conObj = (Contact) objects[0];
        return I(conObj.getParentFolderID());
    }

    public Object contextid(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("ContextId");
        }
        final Contact conObj = (Contact) objects[0];
        return I(conObj.getContextId());
    }

    public Object privateflag(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("PrivateFlag");
        }
        final Contact conObj = (Contact) objects[0];
        return B(conObj.getPrivateFlag());
    }

    public Object createdby(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("CreatedBy");
        }
        final Contact conObj = (Contact) objects[0];
        return I(conObj.getCreatedBy());
    }

    public Object modifiedby(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("ModifiedBy");
        }
        final Contact conObj = (Contact) objects[0];
        return I(conObj.getModifiedBy());
    }

    public Object creationdate(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("CreationDate");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getCreationDate();
    }

    public Object lastmodified(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("LastModified");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getLastModified();
    }

    public Object birthday(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("Birthday");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getBirthday();
    }

    public Object anniversary(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("Anniversary");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getAnniversary();
    }

    public Object imagelastmodified(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("ImageLastModified");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getImageLastModified();
    }

    public Object internaluserid(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("InternalUserId");
        }
        final Contact conObj = (Contact) objects[0];
        return I(conObj.getInternalUserId());
    }

    public Object label(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("Label");
        }
        final Contact conObj = (Contact) objects[0];
        return I(conObj.getLabel());
    }

    public Object fileas(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("FileAs");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getFileAs();
    }

    public Object defaultaddress(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("DefaultAddress");
        }
        final Contact conObj = (Contact) objects[0];
        return I(conObj.getDefaultAddress());
    }

    public Object numberofattachments(final Object... objects) throws ContactException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("NumberOfAttachments");
        }
        final Contact conObj = (Contact) objects[0];
        return I(conObj.getNumberOfAttachments());
    }

    public boolean _unknownfield(Contact contact, String fieldname, Object value, Object... additionalObjects){
        return false;
    }
}
