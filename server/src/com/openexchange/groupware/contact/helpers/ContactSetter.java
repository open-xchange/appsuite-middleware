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

package com.openexchange.groupware.contact.helpers;

import java.util.Date;

import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.contact.Classes;
import com.openexchange.groupware.contact.ContactException;
import com.openexchange.groupware.contact.ContactExceptionFactory;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.groupware.container.LinkEntryObject;

/**
 * This switcher enables us to set the values of a contact object.
 * As convention, the first argument of a method represents a ContactObject,
 * the second one the value to be set. 
 * 
 * Note: This class was generated mostly - don't even try to keep this
 * up to date by hand...
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */

@OXExceptionSource(
	classId=Classes.COM_OPENEXCHANGE_GROUPWARE_CONTACTS_HELPERS_CONTACTSETTER, 
	component=EnumComponent.CONTACT)
@OXThrowsMultiple(
	category={Category.CODE_ERROR}, 
	desc={""}, 
	exceptionId={0}, 
	msg={"Need at least a ContactObject and a value to set %s"})
public class ContactSetter implements ContactSwitcher {

  private static final ContactExceptionFactory EXCEPTIONS = new ContactExceptionFactory(ContactSetter.class);
  
  public Object displayname(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "DisplayName");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setDisplayName(value);
    return conObj;
  }

  public Object surname(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "SurName");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setSurName(value);
    return conObj;
  }

  public Object givenname(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "GivenName");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setGivenName(value);
    return conObj;
  }

  public Object middlename(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "MiddleName");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setMiddleName(value);
    return conObj;
  }

  public Object suffix(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "Suffix");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setSuffix(value);
    return conObj;
  }

  public Object title(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "Title");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setTitle(value);
    return conObj;
  }

  public Object streethome(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "StreetHome");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setStreetHome(value);
    return conObj;
  }

  public Object postalcodehome(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "PostalCodeHome");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setPostalCodeHome(value);
    return conObj;
  }

  public Object cityhome(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "CityHome");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setCityHome(value);
    return conObj;
  }

  public Object statehome(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "StateHome");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setStateHome(value);
    return conObj;
  }

  public Object countryhome(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "CountryHome");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setCountryHome(value);
    return conObj;
  }

  public Object maritalstatus(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "MaritalStatus");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setMaritalStatus(value);
    return conObj;
  }

  public Object numberofchildren(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "NumberOfChildren");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setNumberOfChildren(value);
    return conObj;
  }

  public Object profession(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "Profession");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setProfession(value);
    return conObj;
  }

  public Object nickname(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "Nickname");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setNickname(value);
    return conObj;
  }

  public Object spousename(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "SpouseName");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setSpouseName(value);
    return conObj;
  }

  public Object note(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "Note");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setNote(value);
    return conObj;
  }

  public Object company(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "Company");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setCompany(value);
    return conObj;
  }

  public Object department(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "Department");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setDepartment(value);
    return conObj;
  }

  public Object position(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "Position");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setPosition(value);
    return conObj;
  }

  public Object employeetype(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "EmployeeType");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setEmployeeType(value);
    return conObj;
  }

  public Object roomnumber(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "RoomNumber");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setRoomNumber(value);
    return conObj;
  }

  public Object streetbusiness(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "StreetBusiness");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setStreetBusiness(value);
    return conObj;
  }

  public Object postalcodebusiness(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "PostalCodeBusiness");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setPostalCodeBusiness(value);
    return conObj;
  }

  public Object citybusiness(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "CityBusiness");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setCityBusiness(value);
    return conObj;
  }

  public Object statebusiness(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "StateBusiness");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setStateBusiness(value);
    return conObj;
  }

  public Object countrybusiness(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "CountryBusiness");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setCountryBusiness(value);
    return conObj;
  }

  public Object numberofemployee(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "NumberOfEmployee");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setNumberOfEmployee(value);
    return conObj;
  }

  public Object salesvolume(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "SalesVolume");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setSalesVolume(value);
    return conObj;
  }

  public Object taxid(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "TaxID");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setTaxID(value);
    return conObj;
  }

  public Object commercialregister(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "CommercialRegister");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setCommercialRegister(value);
    return conObj;
  }

  public Object branches(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "Branches");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setBranches(value);
    return conObj;
  }

  public Object businesscategory(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "BusinessCategory");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setBusinessCategory(value);
    return conObj;
  }

  public Object info(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "Info");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setInfo(value);
    return conObj;
  }

  public Object managername(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "ManagerName");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setManagerName(value);
    return conObj;
  }

  public Object assistantname(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "AssistantName");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setAssistantName(value);
    return conObj;
  }

  public Object streetother(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "StreetOther");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setStreetOther(value);
    return conObj;
  }

  public Object postalcodeother(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "PostalCodeOther");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setPostalCodeOther(value);
    return conObj;
  }

  public Object cityother(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "CityOther");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setCityOther(value);
    return conObj;
  }

  public Object stateother(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "StateOther");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setStateOther(value);
    return conObj;
  }

  public Object countryother(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "CountryOther");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setCountryOther(value);
    return conObj;
  }

  public Object telephoneassistant(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "TelephoneAssistant");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setTelephoneAssistant(value);
    return conObj;
  }

  public Object telephonebusiness1(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "TelephoneBusiness1");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setTelephoneBusiness1(value);
    return conObj;
  }

  public Object telephonebusiness2(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "TelephoneBusiness2");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setTelephoneBusiness2(value);
    return conObj;
  }

  public Object faxbusiness(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "FaxBusiness");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setFaxBusiness(value);
    return conObj;
  }

  public Object telephonecallback(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "TelephoneCallback");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setTelephoneCallback(value);
    return conObj;
  }

  public Object telephonecar(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "TelephoneCar");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setTelephoneCar(value);
    return conObj;
  }

  public Object telephonecompany(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "TelephoneCompany");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setTelephoneCompany(value);
    return conObj;
  }

  public Object telephonehome1(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "TelephoneHome1");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setTelephoneHome1(value);
    return conObj;
  }

  public Object telephonehome2(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "TelephoneHome2");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setTelephoneHome2(value);
    return conObj;
  }

  public Object faxhome(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "FaxHome");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setFaxHome(value);
    return conObj;
  }

  public Object telephoneisdn(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "TelephoneISDN");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setTelephoneISDN(value);
    return conObj;
  }

  public Object cellulartelephone1(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "CellularTelephone1");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setCellularTelephone1(value);
    return conObj;
  }

  public Object cellulartelephone2(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "CellularTelephone2");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setCellularTelephone2(value);
    return conObj;
  }

  public Object telephoneother(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "TelephoneOther");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setTelephoneOther(value);
    return conObj;
  }

  public Object faxother(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "FaxOther");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setFaxOther(value);
    return conObj;
  }

  public Object telephonepager(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "TelephonePager");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setTelephonePager(value);
    return conObj;
  }

  public Object telephoneprimary(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "TelephonePrimary");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setTelephonePrimary(value);
    return conObj;
  }

  public Object telephoneradio(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "TelephoneRadio");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setTelephoneRadio(value);
    return conObj;
  }

  public Object telephonetelex(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "TelephoneTelex");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setTelephoneTelex(value);
    return conObj;
  }

  public Object telephonettyttd(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "TelephoneTTYTTD");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setTelephoneTTYTTD(value);
    return conObj;
  }

  public Object instantmessenger1(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "InstantMessenger1");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setInstantMessenger1(value);
    return conObj;
  }

  public Object instantmessenger2(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "InstantMessenger2");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setInstantMessenger2(value);
    return conObj;
  }

  public Object telephoneip(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "TelephoneIP");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setTelephoneIP(value);
    return conObj;
  }

  public Object email1(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "Email1");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setEmail1(value);
    return conObj;
  }

  public Object email2(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "Email2");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setEmail2(value);
    return conObj;
  }

  public Object email3(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "Email3");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setEmail3(value);
    return conObj;
  }

  public Object url(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "URL");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setURL(value);
    return conObj;
  }

  public Object categories(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "Categories");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setCategories(value);
    return conObj;
  }

  public Object userfield01(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "UserField01");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setUserField01(value);
    return conObj;
  }

  public Object userfield02(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "UserField02");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setUserField02(value);
    return conObj;
  }

  public Object userfield03(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "UserField03");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setUserField03(value);
    return conObj;
  }

  public Object userfield04(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "UserField04");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setUserField04(value);
    return conObj;
  }

  public Object userfield05(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "UserField05");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setUserField05(value);
    return conObj;
  }

  public Object userfield06(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "UserField06");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setUserField06(value);
    return conObj;
  }

  public Object userfield07(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "UserField07");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setUserField07(value);
    return conObj;
  }

  public Object userfield08(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "UserField08");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setUserField08(value);
    return conObj;
  }

  public Object userfield09(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "UserField09");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setUserField09(value);
    return conObj;
  }

  public Object userfield10(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "UserField10");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setUserField10(value);
    return conObj;
  }

  public Object userfield11(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "UserField11");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setUserField11(value);
    return conObj;
  }

  public Object userfield12(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "UserField12");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setUserField12(value);
    return conObj;
  }

  public Object userfield13(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "UserField13");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setUserField13(value);
    return conObj;
  }

  public Object userfield14(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "UserField14");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setUserField14(value);
    return conObj;
  }

  public Object userfield15(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "UserField15");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setUserField15(value);
    return conObj;
  }

  public Object userfield16(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "UserField16");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setUserField16(value);
    return conObj;
  }

  public Object userfield17(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "UserField17");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setUserField17(value);
    return conObj;
  }

  public Object userfield18(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "UserField18");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setUserField18(value);
    return conObj;
  }

  public Object userfield19(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "UserField19");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setUserField19(value);
    return conObj;
  }

  public Object userfield20(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "UserField20");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setUserField20(value);
    return conObj;
  }

  public Object objectid(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "ObjectID");
    }
    final Contact conObj = (Contact) objects[0];
    final int value = toInt( objects[1] );
    conObj.setObjectID(value);
    return conObj;
  }

  public Object numberofdistributionlists(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "NumberOfDistributionLists");
    }
    final Contact conObj = (Contact) objects[0];
    final int value = toInt( objects[1] );
    conObj.setNumberOfDistributionLists(value);
    return conObj;
  }

  public Object numberoflinks(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "NumberOfLinks");
    }
    final Contact conObj = (Contact) objects[0];
    final int value = toInt( objects[1] );
    conObj.setNumberOfLinks(value);
    return conObj;
  }

  public Object distributionlist(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "DistributionList");
    }
    final Contact conObj = (Contact) objects[0];
    final DistributionListEntryObject[] value = (DistributionListEntryObject[]) objects[1];
    conObj.setDistributionList(value);
    return conObj;
  }

  public Object links(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "Links");
    }
    final Contact conObj = (Contact) objects[0];
    final LinkEntryObject[] value = (LinkEntryObject[]) objects[1];
    conObj.setLinks(value);
    return conObj;
  }

  public Object parentfolderid(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "ParentFolderID");
    }
    final Contact conObj = (Contact) objects[0];
    final int value = toInt( objects[1] );
    conObj.setParentFolderID(value);
    return conObj;
  }

  public Object contextid(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "ContextId");
    }
    final Contact conObj = (Contact) objects[0];
    final int  value = toInt( objects[1] );
    conObj.setContextId(value);
    return conObj;
  }

  public Object privateflag(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "PrivateFlag");
    }
    final Contact conObj = (Contact) objects[0];
    final boolean value = (Boolean) objects[1];
    conObj.setPrivateFlag(value);
    return conObj;
  }

  public Object createdby(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "CreatedBy");
    }
    final Contact conObj = (Contact) objects[0];
    final int  value = toInt( objects[1] );
    conObj.setCreatedBy(value);
    return conObj;
  }

  public Object modifiedby(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "ModifiedBy");
    }
    final Contact conObj = (Contact) objects[0];
    final int value = toInt( objects[1] );
    conObj.setModifiedBy(value);
    return conObj;
  }

  public Object creationdate(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "CreationDate");
    }
    final Contact conObj = (Contact) objects[0];
    final Date value = (Date) objects[1];
    conObj.setCreationDate(value);
    return conObj;
  }

  public Object lastmodified(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "LastModified");
    }
    final Contact conObj = (Contact) objects[0];
    final Date value = (Date) objects[1];
    conObj.setLastModified(value);
    return conObj;
  }

  public Object birthday(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "Birthday");
    }
    final Contact conObj = (Contact) objects[0];
    final Date value = (Date) objects[1];
    conObj.setBirthday(value);
    return conObj;
  }

  public Object anniversary(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "Anniversary");
    }
    final Contact conObj = (Contact) objects[0];
    final Date value = (Date) objects[1];
    conObj.setAnniversary(value);
    return conObj;
  }

  public Object imagelastmodified(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "ImageLastModified");
    }
    final Contact conObj = (Contact) objects[0];
    final Date value = (Date) objects[1];
    conObj.setImageLastModified(value);
    return conObj;
  }

  public Object internaluserid(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "InternalUserId");
    }
    final Contact conObj = (Contact) objects[0];
    final int value = toInt( objects[1] );
    conObj.setInternalUserId(value);
    return conObj;
  }

  public Object label(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "Label");
    }
    final Contact conObj = (Contact) objects[0];
    final int value = toInt( objects[1] );
    conObj.setLabel(value);
    return conObj;
  }

  public Object fileas(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "FileAs");
    }
    final Contact conObj = (Contact) objects[0];
    final String value = (String) objects[1];
    conObj.setFileAs(value);
    return conObj;
  }

  public Object defaultaddress(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "DefaultAddress");
    }
    final Contact conObj = (Contact) objects[0];
    final int value = toInt( objects[1] );
    conObj.setDefaultAddress(value);
    return conObj;
  }

  public Object numberofattachments(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "NumberOfAttachments");
    }
    final Contact conObj = (Contact) objects[0];
    final int value = toInt( objects[1] );
    conObj.setNumberOfAttachments(value);
    return conObj;
  }

  private int toInt(Object candidate) {
    if(candidate instanceof Integer) { return (Integer) candidate; }
    return Integer.valueOf(candidate.toString());
  }


}
