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

import static com.openexchange.java.Autoboxing.b;
import static com.openexchange.java.Autoboxing.i;
import java.util.Date;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;

/**
 * This switcher enables us to set the values of a contact object. As convention, the first argument of a method represents a ContactObject,
 * the second one the value to be set. Note: This class was generated mostly - don't even try to keep this up to date by hand...
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 */
public class ContactSetter implements ContactSwitcher {

    @Override
    public Object displayname(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("DisplayName");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setDisplayName(value);
        return conObj;
    }

    @Override
    public Object surname(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("SurName");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setSurName(value);
        return conObj;
    }

    @Override
    public Object givenname(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("GivenName");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setGivenName(value);
        return conObj;
    }

    @Override
    public Object middlename(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("MiddleName");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setMiddleName(value);
        return conObj;
    }

    @Override
    public Object suffix(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("Suffix");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setSuffix(value);
        return conObj;
    }

    @Override
    public Object title(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("Title");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setTitle(value);
        return conObj;
    }

    @Override
    public Object streethome(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("StreetHome");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setStreetHome(value);
        return conObj;
    }

    @Override
    public Object postalcodehome(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("PostalCodeHome");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setPostalCodeHome(value);
        return conObj;
    }

    @Override
    public Object cityhome(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("CityHome");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setCityHome(value);
        return conObj;
    }

    @Override
    public Object statehome(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("StateHome");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setStateHome(value);
        return conObj;
    }

    @Override
    public Object countryhome(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("CountryHome");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setCountryHome(value);
        return conObj;
    }

    @Override
    public Object maritalstatus(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("MaritalStatus");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setMaritalStatus(value);
        return conObj;
    }

    @Override
    public Object numberofchildren(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("NumberOfChildren");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setNumberOfChildren(value);
        return conObj;
    }

    @Override
    public Object profession(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("Profession");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setProfession(value);
        return conObj;
    }

    @Override
    public Object nickname(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("Nickname");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setNickname(value);
        return conObj;
    }

    @Override
    public Object spousename(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("SpouseName");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setSpouseName(value);
        return conObj;
    }

    @Override
    public Object note(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("Note");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setNote(value);
        return conObj;
    }

    @Override
    public Object company(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("Company");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setCompany(value);
        return conObj;
    }

    @Override
    public Object department(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("Department");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setDepartment(value);
        return conObj;
    }

    @Override
    public Object position(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("Position");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setPosition(value);
        return conObj;
    }

    @Override
    public Object employeetype(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("EmployeeType");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setEmployeeType(value);
        return conObj;
    }

    @Override
    public Object roomnumber(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("RoomNumber");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setRoomNumber(value);
        return conObj;
    }

    @Override
    public Object streetbusiness(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("StreetBusiness");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setStreetBusiness(value);
        return conObj;
    }

    @Override
    public Object postalcodebusiness(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("PostalCodeBusiness");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setPostalCodeBusiness(value);
        return conObj;
    }

    @Override
    public Object citybusiness(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("CityBusiness");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setCityBusiness(value);
        return conObj;
    }

    @Override
    public Object statebusiness(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("StateBusiness");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setStateBusiness(value);
        return conObj;
    }

    @Override
    public Object countrybusiness(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("CountryBusiness");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setCountryBusiness(value);
        return conObj;
    }

    @Override
    public Object numberofemployee(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("NumberOfEmployee");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setNumberOfEmployee(value);
        return conObj;
    }

    @Override
    public Object salesvolume(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("SalesVolume");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setSalesVolume(value);
        return conObj;
    }

    @Override
    public Object taxid(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("TaxID");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setTaxID(value);
        return conObj;
    }

    @Override
    public Object commercialregister(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("CommercialRegister");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setCommercialRegister(value);
        return conObj;
    }

    @Override
    public Object branches(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("Branches");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setBranches(value);
        return conObj;
    }

    @Override
    public Object businesscategory(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("BusinessCategory");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setBusinessCategory(value);
        return conObj;
    }

    @Override
    public Object info(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("Info");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setInfo(value);
        return conObj;
    }

    @Override
    public Object managername(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("ManagerName");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setManagerName(value);
        return conObj;
    }

    @Override
    public Object assistantname(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("AssistantName");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setAssistantName(value);
        return conObj;
    }

    @Override
    public Object streetother(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("StreetOther");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setStreetOther(value);
        return conObj;
    }

    @Override
    public Object postalcodeother(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("PostalCodeOther");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setPostalCodeOther(value);
        return conObj;
    }

    @Override
    public Object cityother(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("CityOther");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setCityOther(value);
        return conObj;
    }

    @Override
    public Object stateother(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("StateOther");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setStateOther(value);
        return conObj;
    }

    @Override
    public Object countryother(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("CountryOther");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setCountryOther(value);
        return conObj;
    }

    @Override
    public Object telephoneassistant(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("TelephoneAssistant");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setTelephoneAssistant(value);
        return conObj;
    }

    @Override
    public Object telephonebusiness1(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("TelephoneBusiness1");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setTelephoneBusiness1(value);
        return conObj;
    }

    @Override
    public Object telephonebusiness2(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("TelephoneBusiness2");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setTelephoneBusiness2(value);
        return conObj;
    }

    @Override
    public Object faxbusiness(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("FaxBusiness");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setFaxBusiness(value);
        return conObj;
    }

    @Override
    public Object telephonecallback(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("TelephoneCallback");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setTelephoneCallback(value);
        return conObj;
    }

    @Override
    public Object telephonecar(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("TelephoneCar");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setTelephoneCar(value);
        return conObj;
    }

    @Override
    public Object telephonecompany(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("TelephoneCompany");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setTelephoneCompany(value);
        return conObj;
    }

    @Override
    public Object telephonehome1(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("TelephoneHome1");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setTelephoneHome1(value);
        return conObj;
    }

    @Override
    public Object telephonehome2(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("TelephoneHome2");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setTelephoneHome2(value);
        return conObj;
    }

    @Override
    public Object faxhome(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("FaxHome");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setFaxHome(value);
        return conObj;
    }

    @Override
    public Object telephoneisdn(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("TelephoneISDN");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setTelephoneISDN(value);
        return conObj;
    }

    @Override
    public Object cellulartelephone1(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("CellularTelephone1");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setCellularTelephone1(value);
        return conObj;
    }

    @Override
    public Object cellulartelephone2(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("CellularTelephone2");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setCellularTelephone2(value);
        return conObj;
    }

    @Override
    public Object telephoneother(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("TelephoneOther");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setTelephoneOther(value);
        return conObj;
    }

    @Override
    public Object faxother(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("FaxOther");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setFaxOther(value);
        return conObj;
    }

    @Override
    public Object telephonepager(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("TelephonePager");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setTelephonePager(value);
        return conObj;
    }

    @Override
    public Object telephoneprimary(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("TelephonePrimary");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setTelephonePrimary(value);
        return conObj;
    }

    @Override
    public Object telephoneradio(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("TelephoneRadio");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setTelephoneRadio(value);
        return conObj;
    }

    @Override
    public Object telephonetelex(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("TelephoneTelex");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setTelephoneTelex(value);
        return conObj;
    }

    @Override
    public Object telephonettyttd(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("TelephoneTTYTTD");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setTelephoneTTYTTD(value);
        return conObj;
    }

    @Override
    public Object instantmessenger1(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("InstantMessenger1");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setInstantMessenger1(value);
        return conObj;
    }

    @Override
    public Object instantmessenger2(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("InstantMessenger2");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setInstantMessenger2(value);
        return conObj;
    }

    @Override
    public Object telephoneip(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("TelephoneIP");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setTelephoneIP(value);
        return conObj;
    }

    @Override
    public Object email1(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("Email1");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setEmail1(value);
        return conObj;
    }

    @Override
    public Object email2(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("Email2");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setEmail2(value);
        return conObj;
    }

    @Override
    public Object email3(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("Email3");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setEmail3(value);
        return conObj;
    }

    @Override
    public Object url(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("URL");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setURL(value);
        return conObj;
    }

    @Override
    public Object categories(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("Categories");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setCategories(value);
        return conObj;
    }

    @Override
    public Object userfield01(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("UserField01");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setUserField01(value);
        return conObj;
    }

    @Override
    public Object userfield02(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("UserField02");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setUserField02(value);
        return conObj;
    }

    @Override
    public Object userfield03(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("UserField03");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setUserField03(value);
        return conObj;
    }

    @Override
    public Object userfield04(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("UserField04");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setUserField04(value);
        return conObj;
    }

    @Override
    public Object userfield05(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("UserField05");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setUserField05(value);
        return conObj;
    }

    @Override
    public Object userfield06(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("UserField06");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setUserField06(value);
        return conObj;
    }

    @Override
    public Object userfield07(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("UserField07");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setUserField07(value);
        return conObj;
    }

    @Override
    public Object userfield08(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("UserField08");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setUserField08(value);
        return conObj;
    }

    @Override
    public Object userfield09(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("UserField09");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setUserField09(value);
        return conObj;
    }

    @Override
    public Object userfield10(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("UserField10");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setUserField10(value);
        return conObj;
    }

    @Override
    public Object userfield11(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("UserField11");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setUserField11(value);
        return conObj;
    }

    @Override
    public Object userfield12(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("UserField12");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setUserField12(value);
        return conObj;
    }

    @Override
    public Object userfield13(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("UserField13");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setUserField13(value);
        return conObj;
    }

    @Override
    public Object userfield14(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("UserField14");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setUserField14(value);
        return conObj;
    }

    @Override
    public Object userfield15(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("UserField15");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setUserField15(value);
        return conObj;
    }

    @Override
    public Object userfield16(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("UserField16");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setUserField16(value);
        return conObj;
    }

    @Override
    public Object userfield17(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("UserField17");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setUserField17(value);
        return conObj;
    }

    @Override
    public Object userfield18(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("UserField18");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setUserField18(value);
        return conObj;
    }

    @Override
    public Object userfield19(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("UserField19");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setUserField19(value);
        return conObj;
    }

    @Override
    public Object userfield20(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("UserField20");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setUserField20(value);
        return conObj;
    }

    @Override
    public Object objectid(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("ObjectID");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final int value = toInt(objects[1]);
        conObj.setObjectID(value);
        return conObj;
    }

    @Override
    public Object numberofdistributionlists(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("NumberOfDistributionLists");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final int value = toInt(objects[1]);
        conObj.setNumberOfDistributionLists(value);
        return conObj;
    }

    @Override
    public Object distributionlist(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("DistributionList");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final DistributionListEntryObject[] value;
        if (String.class.isInstance(objects[1])) {
            String[] splitted = ((String)objects[1]).split("(?<!/);");
            if (0 != splitted.length % 2) {
                throw ContactExceptionCodes.UNEXPECTED_ERROR.create("Invalid number of properties for distribution list");
            }
            value = new DistributionListEntryObject[splitted.length / 2];
            for (int i = 0; i < value.length; i++) {
                int offset = 2 * i;
                value[i] = new DistributionListEntryObject();
                value[i].setEmailaddress(splitted[0 + offset].replaceAll("/;", ";"));
                value[i].setDisplayname(splitted[1 + offset].replaceAll("/;", ";"));
            }
        } else {
            value = (DistributionListEntryObject[]) objects[1];
        }
        conObj.setDistributionList(value);
        return conObj;
    }

    @Override
    public Object parentfolderid(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("ParentFolderID");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final int value = toInt(objects[1]);
        conObj.setParentFolderID(value);
        return conObj;
    }

    @Override
    public Object contextid(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("ContextId");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final int value = toInt(objects[1]);
        conObj.setContextId(value);
        return conObj;
    }

    @Override
    public Object privateflag(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("PrivateFlag");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final boolean value = b((Boolean) objects[1]);
        conObj.setPrivateFlag(value);
        return conObj;
    }

    @Override
    public Object createdby(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("CreatedBy");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final int value = toInt(objects[1]);
        conObj.setCreatedBy(value);
        return conObj;
    }

    @Override
    public Object modifiedby(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("ModifiedBy");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final int value = toInt(objects[1]);
        conObj.setModifiedBy(value);
        return conObj;
    }

    @Override
    public Object creationdate(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("CreationDate");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }

        final Date value = (Date) objects[1];
        conObj.setCreationDate(value);
        return conObj;
    }

    @Override
    public Object lastmodified(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("LastModified");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }

        final Date value = (Date) objects[1];
        conObj.setLastModified(value);
        return conObj;
    }

    @Override
    public Object birthday(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("Birthday");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final Date value = (Date) objects[1];
        conObj.setBirthday(value);
        return conObj;
    }

    @Override
    public Object anniversary(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("Anniversary");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final Date value = (Date) objects[1];
        conObj.setAnniversary(value);
        return conObj;
    }

    @Override
    public Object imagelastmodified(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("ImageLastModified");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final Date value = (Date) objects[1];
        conObj.setImageLastModified(value);
        return conObj;
    }

    @Override
    public Object internaluserid(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("InternalUserId");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final int value = toInt(objects[1]);
        conObj.setInternalUserId(value);
        return conObj;
    }

    @Override
    public Object label(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("Label");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final int value = toInt(objects[1]);
        conObj.setLabel(value);
        return conObj;
    }

    @Override
    public Object fileas(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("FileAs");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setFileAs(value);
        return conObj;
    }

    @Override
    public Object defaultaddress(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("DefaultAddress");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final int value = toInt(objects[1]);
        conObj.setDefaultAddress(value);
        return conObj;
    }

    @Override
    public Object numberofattachments(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("NumberOfAttachments");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final int value = toInt(objects[1]);
        conObj.setNumberOfAttachments(value);
        return conObj;
    }




    private int toInt(final Object candidate) {
        if (candidate instanceof Integer) {
            return i((Integer) candidate);
        }
        return Integer.parseInt(candidate.toString());
    }

    private boolean isMatching(final String needle, final ContactField haystack){
        return(
            needle.matches(haystack.getAjaxName())
         || needle.matches(haystack.getFieldName())
         || needle.matches(haystack.getDbName())
         || needle.matches(String.valueOf(haystack.getNumber()))
        );
    }

    private boolean markasdistributionlist(final Contact contact, final Object value2) {
        Boolean value;
        try {
            value = (Boolean) value2;
        } catch (final ClassCastException c) {
            value = Boolean.valueOf((String) value2);
        }
        contact.setMarkAsDistributionlist(b(value));
        return true;
    }

    @Override
    public Object numberofimages(Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("NumberOfImages");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final int value = toInt(objects[1]);
        conObj.setNumberOfImages(value);
        return conObj;
    }

    @Override
    public Object lastmodifiedofnewestattachment(Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("Anniversary");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final Date value = (Date) objects[1];
        conObj.setLastModifiedOfNewestAttachment(value);
        return conObj;
    }

    @Override
    public Object usecount(Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("UseCount");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final int value = toInt(objects[1]);
        conObj.setUseCount(value);
        return conObj;
    }

    @Override
    public Object markasdistributionlist(Object[] objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("MarkAsDistributionList");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final boolean value = b((Boolean) objects[1]);
        conObj.setMarkAsDistributionlist(value);
        return conObj;
    }

    @Override
    public Object yomifirstname(Object[] objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("yomiFirstName");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setYomiFirstName(value);
        return conObj;
    }

    @Override
    public Object yomilastname(Object[] objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("yomiLastName");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setYomiLastName(value);
        return conObj;
    }

    @Override
    public Object yomicompanyname(Object[] objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("yomiCompanyName");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setYomiCompany(value);
        return conObj;
    }

    @Override
    public Object image1contenttype(Object[] objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("image1_content_type");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setImageContentType(value);
        return conObj;
    }

    @Override
    public Object homeaddress(Object[] objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create(ContactField.HOME_ADDRESS.getReadableName());
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setAddressHome(value);
        return conObj;
    }

    @Override
    public Object businessaddress(Object[] objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create(ContactField.BUSINESS_ADDRESS.getReadableName());
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setAddressBusiness(value);
        return conObj;
    }

    @Override
    public Object otheraddress(Object[] objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create(ContactField.OTHER_ADDRESS.getReadableName());
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setAddressOther(value);
        return conObj;
    }

    @Override
    public Object uid(Object[] objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create(ContactField.UID.getReadableName());
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final String value = (String) objects[1];
        conObj.setUid(value);
        return conObj;
    }

    @Override
    public Object image1(Object[] objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create(ContactField.IMAGE1.getReadableName());
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }
        final byte[] value = (byte[])objects[1];
        conObj.setImage1(value);
        return conObj;
    }

    @Override
    public boolean _unknownfield(final Contact contact, final String fieldname, final Object value, final Object... additionalObjects) {
        if(fieldname == null || fieldname.equals("")) {
            return false;
        }

        if(isMatching(fieldname, ContactField.MARK_AS_DISTRIBUTIONLIST)) {
            return markasdistributionlist(contact, value);
        }

        return false;
    }
}
