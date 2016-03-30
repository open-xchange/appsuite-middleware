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

package com.openexchange.groupware.contact.helpers;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.container.Contact;

/**
 * This switcher enables us to get the values of a contact object. As convention, the first argument of a method represents the
 * ContactObject which value is to be retrieved. Note: This class was generated mostly - don't even try to keep this up to date by hand...
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 */
public class ContactGetter implements ContactSwitcher {

    @Override
    public Object displayname(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("DisplayName");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getDisplayName();
    }

    @Override
    public Object surname(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("SurName");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getSurName();
    }

    @Override
    public Object givenname(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("GivenName");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getGivenName();
    }

    @Override
    public Object middlename(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("MiddleName");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getMiddleName();
    }

    @Override
    public Object suffix(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("Suffix");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getSuffix();
    }

    @Override
    public Object title(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("Title");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getTitle();
    }

    @Override
    public Object streethome(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("StreetHome");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getStreetHome();
    }

    @Override
    public Object postalcodehome(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("PostalCodeHome");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getPostalCodeHome();
    }

    @Override
    public Object cityhome(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("CityHome");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getCityHome();
    }

    @Override
    public Object statehome(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("StateHome");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getStateHome();
    }

    @Override
    public Object countryhome(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("CountryHome");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getCountryHome();
    }

    @Override
    public Object maritalstatus(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("MaritalStatus");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getMaritalStatus();
    }

    @Override
    public Object numberofchildren(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("NumberOfChildren");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getNumberOfChildren();
    }

    @Override
    public Object profession(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("Profession");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getProfession();
    }

    @Override
    public Object nickname(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("Nickname");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getNickname();
    }

    @Override
    public Object spousename(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("SpouseName");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getSpouseName();
    }

    @Override
    public Object note(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("Note");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getNote();
    }

    @Override
    public Object company(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("Company");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getCompany();
    }

    @Override
    public Object department(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("Department");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getDepartment();
    }

    @Override
    public Object position(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("Position");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getPosition();
    }

    @Override
    public Object employeetype(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("EmployeeType");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getEmployeeType();
    }

    @Override
    public Object roomnumber(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("RoomNumber");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getRoomNumber();
    }

    @Override
    public Object streetbusiness(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("StreetBusiness");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getStreetBusiness();
    }

    @Override
    public Object postalcodebusiness(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("PostalCodeBusiness");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getPostalCodeBusiness();
    }

    @Override
    public Object citybusiness(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("CityBusiness");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getCityBusiness();
    }

    @Override
    public Object statebusiness(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("StateBusiness");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getStateBusiness();
    }

    @Override
    public Object countrybusiness(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("CountryBusiness");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getCountryBusiness();
    }

    @Override
    public Object numberofemployee(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("NumberOfEmployee");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getNumberOfEmployee();
    }

    @Override
    public Object salesvolume(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("SalesVolume");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getSalesVolume();
    }

    @Override
    public Object taxid(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("TaxID");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getTaxID();
    }

    @Override
    public Object commercialregister(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("CommercialRegister");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getCommercialRegister();
    }

    @Override
    public Object branches(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("Branches");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getBranches();
    }

    @Override
    public Object businesscategory(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("BusinessCategory");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getBusinessCategory();
    }

    @Override
    public Object info(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("Info");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getInfo();
    }

    @Override
    public Object managername(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("ManagerName");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getManagerName();
    }

    @Override
    public Object assistantname(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("AssistantName");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getAssistantName();
    }

    @Override
    public Object streetother(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("StreetOther");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getStreetOther();
    }

    @Override
    public Object postalcodeother(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("PostalCodeOther");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getPostalCodeOther();
    }

    @Override
    public Object cityother(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("CityOther");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getCityOther();
    }

    @Override
    public Object stateother(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("StateOther");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getStateOther();
    }

    @Override
    public Object countryother(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("CountryOther");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getCountryOther();
    }

    @Override
    public Object telephoneassistant(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("TelephoneAssistant");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getTelephoneAssistant();
    }

    @Override
    public Object telephonebusiness1(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("TelephoneBusiness1");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getTelephoneBusiness1();
    }

    @Override
    public Object telephonebusiness2(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("TelephoneBusiness2");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getTelephoneBusiness2();
    }

    @Override
    public Object faxbusiness(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("FaxBusiness");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getFaxBusiness();
    }

    @Override
    public Object telephonecallback(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("TelephoneCallback");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getTelephoneCallback();
    }

    @Override
    public Object telephonecar(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("TelephoneCar");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getTelephoneCar();
    }

    @Override
    public Object telephonecompany(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("TelephoneCompany");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getTelephoneCompany();
    }

    @Override
    public Object telephonehome1(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("TelephoneHome1");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getTelephoneHome1();
    }

    @Override
    public Object telephonehome2(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("TelephoneHome2");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getTelephoneHome2();
    }

    @Override
    public Object faxhome(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("FaxHome");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getFaxHome();
    }

    @Override
    public Object telephoneisdn(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("TelephoneISDN");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getTelephoneISDN();
    }

    @Override
    public Object cellulartelephone1(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("CellularTelephone1");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getCellularTelephone1();
    }

    @Override
    public Object cellulartelephone2(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("CellularTelephone2");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getCellularTelephone2();
    }

    @Override
    public Object telephoneother(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("TelephoneOther");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getTelephoneOther();
    }

    @Override
    public Object faxother(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("FaxOther");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getFaxOther();
    }

    @Override
    public Object telephonepager(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("TelephonePager");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getTelephonePager();
    }

    @Override
    public Object telephoneprimary(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("TelephonePrimary");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getTelephonePrimary();
    }

    @Override
    public Object telephoneradio(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("TelephoneRadio");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getTelephoneRadio();
    }

    @Override
    public Object telephonetelex(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("TelephoneTelex");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getTelephoneTelex();
    }

    @Override
    public Object telephonettyttd(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("TelephoneTTYTTD");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getTelephoneTTYTTD();
    }

    @Override
    public Object instantmessenger1(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("InstantMessenger1");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getInstantMessenger1();
    }

    @Override
    public Object instantmessenger2(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("InstantMessenger2");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getInstantMessenger2();
    }

    @Override
    public Object telephoneip(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("TelephoneIP");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getTelephoneIP();
    }

    @Override
    public Object email1(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("Email1");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getEmail1();
    }

    @Override
    public Object email2(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("Email2");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getEmail2();
    }

    @Override
    public Object email3(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("Email3");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getEmail3();
    }

    @Override
    public Object url(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("URL");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getURL();
    }

    @Override
    public Object categories(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("Categories");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getCategories();
    }

    @Override
    public Object userfield01(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("UserField01");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getUserField01();
    }

    @Override
    public Object userfield02(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("UserField02");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getUserField02();
    }

    @Override
    public Object userfield03(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("UserField03");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getUserField03();
    }

    @Override
    public Object userfield04(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("UserField04");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getUserField04();
    }

    @Override
    public Object userfield05(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("UserField05");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getUserField05();
    }

    @Override
    public Object userfield06(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("UserField06");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getUserField06();
    }

    @Override
    public Object userfield07(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("UserField07");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getUserField07();
    }

    @Override
    public Object userfield08(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("UserField08");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getUserField08();
    }

    @Override
    public Object userfield09(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("UserField09");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getUserField09();
    }

    @Override
    public Object userfield10(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("UserField10");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getUserField10();
    }

    @Override
    public Object userfield11(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("UserField11");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getUserField11();
    }

    @Override
    public Object userfield12(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("UserField12");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getUserField12();
    }

    @Override
    public Object userfield13(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("UserField13");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getUserField13();
    }

    @Override
    public Object userfield14(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("UserField14");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getUserField14();
    }

    @Override
    public Object userfield15(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("UserField15");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getUserField15();
    }

    @Override
    public Object userfield16(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("UserField16");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getUserField16();
    }

    @Override
    public Object userfield17(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("UserField17");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getUserField17();
    }

    @Override
    public Object userfield18(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("UserField18");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getUserField18();
    }

    @Override
    public Object userfield19(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("UserField19");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getUserField19();
    }

    @Override
    public Object userfield20(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("UserField20");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getUserField20();
    }

    @Override
    public Object objectid(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("ObjectID");
        }
        final Contact conObj = (Contact) objects[0];
        return I(conObj.getObjectID());
    }

    @Override
    public Object numberofdistributionlists(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("NumberOfDistributionLists");
        }
        final Contact conObj = (Contact) objects[0];
        return I(conObj.getNumberOfDistributionLists());
    }

    @Override
    public Object distributionlist(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("DistributionList");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getDistributionList();
    }

    @Override
    public Object parentfolderid(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("ParentFolderID");
        }
        final Contact conObj = (Contact) objects[0];
        return I(conObj.getParentFolderID());
    }

    @Override
    public Object contextid(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("ContextId");
        }
        final Contact conObj = (Contact) objects[0];
        return I(conObj.getContextId());
    }

    @Override
    public Object privateflag(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("PrivateFlag");
        }
        final Contact conObj = (Contact) objects[0];
        return B(conObj.getPrivateFlag());
    }

    @Override
    public Object createdby(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("CreatedBy");
        }
        final Contact conObj = (Contact) objects[0];
        return I(conObj.getCreatedBy());
    }

    @Override
    public Object modifiedby(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("ModifiedBy");
        }
        final Contact conObj = (Contact) objects[0];
        return I(conObj.getModifiedBy());
    }

    @Override
    public Object creationdate(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("CreationDate");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getCreationDate();
    }

    @Override
    public Object lastmodified(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("LastModified");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getLastModified();
    }

    @Override
    public Object birthday(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("Birthday");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getBirthday();
    }

    @Override
    public Object anniversary(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("Anniversary");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getAnniversary();
    }

    @Override
    public Object imagelastmodified(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("ImageLastModified");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getImageLastModified();
    }

    @Override
    public Object internaluserid(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("InternalUserId");
        }
        final Contact conObj = (Contact) objects[0];
        return I(conObj.getInternalUserId());
    }

    @Override
    public Object label(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("Label");
        }
        final Contact conObj = (Contact) objects[0];
        return I(conObj.getLabel());
    }

    @Override
    public Object fileas(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("FileAs");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getFileAs();
    }

    @Override
    public Object defaultaddress(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("DefaultAddress");
        }
        final Contact conObj = (Contact) objects[0];
        return I(conObj.getDefaultAddress());
    }

    @Override
    public Object numberofattachments(final Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("NumberOfAttachments");
        }
        final Contact conObj = (Contact) objects[0];
        return I(conObj.getNumberOfAttachments());
    }

    @Override
    public Object numberofimages(Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("NumberOfImages");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getNumberOfImages();
    }

    @Override
    public Object lastmodifiedofnewestattachment(Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("LastModifiedOfNewestAttachment");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getLastModifiedOfNewestAttachment();
    }

    @Override
    public Object usecount(Object... objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("UseCount");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getUseCount();
    }

    @Override
    public Object markasdistributionlist(Object[] objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("MarkAsDistributionList");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getMarkAsDistribtuionlist();
    }

    @Override
    public Object yomifirstname(Object[] objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("yomiFirstName");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getYomiFirstName();
    }

    @Override
    public Object yomilastname(Object[] objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("yomiLastName");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getYomiLastName();
    }

    @Override
    public Object yomicompanyname(Object[] objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("yomiCompanyName");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getYomiCompany();
    }

    @Override
    public Object image1contenttype(Object[] objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create("image1_content_type");
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getImageContentType();
    }

    @Override
    public Object homeaddress(Object[] objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create(ContactField.HOME_ADDRESS.getReadableName());
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getAddressHome();
    }

    @Override
    public Object businessaddress(Object[] objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create(ContactField.BUSINESS_ADDRESS.getReadableName());
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getAddressBusiness();
    }

    @Override
    public Object otheraddress(Object[] objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create(ContactField.OTHER_ADDRESS.getReadableName());
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getAddressOther();
    }

    @Override
    public Object uid(Object[] objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create(ContactField.UID.getReadableName());
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getUid();
    }

    @Override
    public Object image1(Object[] objects) throws OXException {
        if (objects.length < 1) {
            throw ContactExceptionCodes.CONTACT_OBJECT_MISSING.create(ContactField.IMAGE1.getReadableName());
        }
        final Contact conObj = (Contact) objects[0];
        return conObj.getImage1();
    }

    @Override
    public boolean _unknownfield(final Contact contact, final String fieldname, final Object value, final Object... additionalObjects){
        return false;
    }
}
