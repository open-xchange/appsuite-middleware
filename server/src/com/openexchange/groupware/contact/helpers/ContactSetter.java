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
import com.openexchange.groupware.container.ContactObject;
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
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setDisplayName(value);
    return conObj;
  }

  public Object surname(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "SurName");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setSurName(value);
    return conObj;
  }

  public Object givenname(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "GivenName");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setGivenName(value);
    return conObj;
  }

  public Object middlename(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "MiddleName");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setMiddleName(value);
    return conObj;
  }

  public Object suffix(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "Suffix");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setSuffix(value);
    return conObj;
  }

  public Object title(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "Title");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setTitle(value);
    return conObj;
  }

  public Object streethome(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "StreetHome");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setStreetHome(value);
    return conObj;
  }

  public Object postalcodehome(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "PostalCodeHome");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setPostalCodeHome(value);
    return conObj;
  }

  public Object cityhome(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "CityHome");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setCityHome(value);
    return conObj;
  }

  public Object statehome(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "StateHome");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setStateHome(value);
    return conObj;
  }

  public Object countryhome(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "CountryHome");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setCountryHome(value);
    return conObj;
  }

  public Object maritalstatus(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "MaritalStatus");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setMaritalStatus(value);
    return conObj;
  }

  public Object numberofchildren(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "NumberOfChildren");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setNumberOfChildren(value);
    return conObj;
  }

  public Object profession(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "Profession");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setProfession(value);
    return conObj;
  }

  public Object nickname(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "Nickname");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setNickname(value);
    return conObj;
  }

  public Object spousename(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "SpouseName");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setSpouseName(value);
    return conObj;
  }

  public Object note(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "Note");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setNote(value);
    return conObj;
  }

  public Object company(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "Company");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setCompany(value);
    return conObj;
  }

  public Object department(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "Department");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setDepartment(value);
    return conObj;
  }

  public Object position(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "Position");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setPosition(value);
    return conObj;
  }

  public Object employeetype(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "EmployeeType");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setEmployeeType(value);
    return conObj;
  }

  public Object roomnumber(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "RoomNumber");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setRoomNumber(value);
    return conObj;
  }

  public Object streetbusiness(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "StreetBusiness");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setStreetBusiness(value);
    return conObj;
  }

  public Object postalcodebusiness(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "PostalCodeBusiness");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setPostalCodeBusiness(value);
    return conObj;
  }

  public Object citybusiness(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "CityBusiness");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setCityBusiness(value);
    return conObj;
  }

  public Object statebusiness(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "StateBusiness");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setStateBusiness(value);
    return conObj;
  }

  public Object countrybusiness(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "CountryBusiness");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setCountryBusiness(value);
    return conObj;
  }

  public Object numberofemployee(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "NumberOfEmployee");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setNumberOfEmployee(value);
    return conObj;
  }

  public Object salesvolume(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "SalesVolume");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setSalesVolume(value);
    return conObj;
  }

  public Object taxid(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "TaxID");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setTaxID(value);
    return conObj;
  }

  public Object commercialregister(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "CommercialRegister");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setCommercialRegister(value);
    return conObj;
  }

  public Object branches(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "Branches");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setBranches(value);
    return conObj;
  }

  public Object businesscategory(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "BusinessCategory");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setBusinessCategory(value);
    return conObj;
  }

  public Object info(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "Info");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setInfo(value);
    return conObj;
  }

  public Object managername(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "ManagerName");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setManagerName(value);
    return conObj;
  }

  public Object assistantname(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "AssistantName");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setAssistantName(value);
    return conObj;
  }

  public Object streetother(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "StreetOther");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setStreetOther(value);
    return conObj;
  }

  public Object postalcodeother(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "PostalCodeOther");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setPostalCodeOther(value);
    return conObj;
  }

  public Object cityother(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "CityOther");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setCityOther(value);
    return conObj;
  }

  public Object stateother(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "StateOther");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setStateOther(value);
    return conObj;
  }

  public Object countryother(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "CountryOther");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setCountryOther(value);
    return conObj;
  }

  public Object telephoneassistant(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "TelephoneAssistant");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setTelephoneAssistant(value);
    return conObj;
  }

  public Object telephonebusiness1(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "TelephoneBusiness1");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setTelephoneBusiness1(value);
    return conObj;
  }

  public Object telephonebusiness2(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "TelephoneBusiness2");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setTelephoneBusiness2(value);
    return conObj;
  }

  public Object faxbusiness(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "FaxBusiness");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setFaxBusiness(value);
    return conObj;
  }

  public Object telephonecallback(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "TelephoneCallback");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setTelephoneCallback(value);
    return conObj;
  }

  public Object telephonecar(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "TelephoneCar");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setTelephoneCar(value);
    return conObj;
  }

  public Object telephonecompany(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "TelephoneCompany");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setTelephoneCompany(value);
    return conObj;
  }

  public Object telephonehome1(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "TelephoneHome1");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setTelephoneHome1(value);
    return conObj;
  }

  public Object telephonehome2(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "TelephoneHome2");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setTelephoneHome2(value);
    return conObj;
  }

  public Object faxhome(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "FaxHome");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setFaxHome(value);
    return conObj;
  }

  public Object telephoneisdn(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "TelephoneISDN");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setTelephoneISDN(value);
    return conObj;
  }

  public Object cellulartelephone1(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "CellularTelephone1");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setCellularTelephone1(value);
    return conObj;
  }

  public Object cellulartelephone2(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "CellularTelephone2");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setCellularTelephone2(value);
    return conObj;
  }

  public Object telephoneother(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "TelephoneOther");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setTelephoneOther(value);
    return conObj;
  }

  public Object faxother(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "FaxOther");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setFaxOther(value);
    return conObj;
  }

  public Object telephonepager(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "TelephonePager");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setTelephonePager(value);
    return conObj;
  }

  public Object telephoneprimary(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "TelephonePrimary");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setTelephonePrimary(value);
    return conObj;
  }

  public Object telephoneradio(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "TelephoneRadio");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setTelephoneRadio(value);
    return conObj;
  }

  public Object telephonetelex(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "TelephoneTelex");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setTelephoneTelex(value);
    return conObj;
  }

  public Object telephonettyttd(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "TelephoneTTYTTD");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setTelephoneTTYTTD(value);
    return conObj;
  }

  public Object instantmessenger1(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "InstantMessenger1");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setInstantMessenger1(value);
    return conObj;
  }

  public Object instantmessenger2(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "InstantMessenger2");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setInstantMessenger2(value);
    return conObj;
  }

  public Object telephoneip(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "TelephoneIP");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setTelephoneIP(value);
    return conObj;
  }

  public Object email1(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "Email1");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setEmail1(value);
    return conObj;
  }

  public Object email2(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "Email2");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setEmail2(value);
    return conObj;
  }

  public Object email3(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "Email3");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setEmail3(value);
    return conObj;
  }

  public Object url(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "URL");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setURL(value);
    return conObj;
  }

  public Object categories(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "Categories");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setCategories(value);
    return conObj;
  }

  public Object userfield01(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "UserField01");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setUserField01(value);
    return conObj;
  }

  public Object userfield02(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "UserField02");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setUserField02(value);
    return conObj;
  }

  public Object userfield03(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "UserField03");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setUserField03(value);
    return conObj;
  }

  public Object userfield04(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "UserField04");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setUserField04(value);
    return conObj;
  }

  public Object userfield05(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "UserField05");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setUserField05(value);
    return conObj;
  }

  public Object userfield06(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "UserField06");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setUserField06(value);
    return conObj;
  }

  public Object userfield07(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "UserField07");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setUserField07(value);
    return conObj;
  }

  public Object userfield08(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "UserField08");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setUserField08(value);
    return conObj;
  }

  public Object userfield09(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "UserField09");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setUserField09(value);
    return conObj;
  }

  public Object userfield10(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "UserField10");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setUserField10(value);
    return conObj;
  }

  public Object userfield11(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "UserField11");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setUserField11(value);
    return conObj;
  }

  public Object userfield12(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "UserField12");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setUserField12(value);
    return conObj;
  }

  public Object userfield13(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "UserField13");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setUserField13(value);
    return conObj;
  }

  public Object userfield14(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "UserField14");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setUserField14(value);
    return conObj;
  }

  public Object userfield15(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "UserField15");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setUserField15(value);
    return conObj;
  }

  public Object userfield16(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "UserField16");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setUserField16(value);
    return conObj;
  }

  public Object userfield17(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "UserField17");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setUserField17(value);
    return conObj;
  }

  public Object userfield18(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "UserField18");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setUserField18(value);
    return conObj;
  }

  public Object userfield19(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "UserField19");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setUserField19(value);
    return conObj;
  }

  public Object userfield20(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "UserField20");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setUserField20(value);
    return conObj;
  }

  public Object objectid(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "ObjectID");
    }
    ContactObject conObj = (ContactObject) objects[0];
    int value = (Integer) objects[1];
    conObj.setObjectID(value);
    return conObj;
  }

  public Object numberofdistributionlists(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "NumberOfDistributionLists");
    }
    ContactObject conObj = (ContactObject) objects[0];
    int value = (Integer) objects[1];
    conObj.setNumberOfDistributionLists(value);
    return conObj;
  }

  public Object numberoflinks(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "NumberOfLinks");
    }
    ContactObject conObj = (ContactObject) objects[0];
    int value = (Integer) objects[1];
    conObj.setNumberOfLinks(value);
    return conObj;
  }

  public Object distributionlist(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "DistributionList");
    }
    ContactObject conObj = (ContactObject) objects[0];
    DistributionListEntryObject[] value = (DistributionListEntryObject[]) objects[1];
    conObj.setDistributionList(value);
    return conObj;
  }

  public Object links(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "Links");
    }
    ContactObject conObj = (ContactObject) objects[0];
    LinkEntryObject[] value = (LinkEntryObject[]) objects[1];
    conObj.setLinks(value);
    return conObj;
  }

  public Object parentfolderid(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "ParentFolderID");
    }
    ContactObject conObj = (ContactObject) objects[0];
    int value = (Integer) objects[1];
    conObj.setParentFolderID(value);
    return conObj;
  }

  public Object contextid(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "ContextId");
    }
    ContactObject conObj = (ContactObject) objects[0];
    int  value = (Integer) objects[1];
    conObj.setContextId(value);
    return conObj;
  }

  public Object privateflag(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "PrivateFlag");
    }
    ContactObject conObj = (ContactObject) objects[0];
    boolean value = (Boolean) objects[1];
    conObj.setPrivateFlag(value);
    return conObj;
  }

  public Object createdby(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "CreatedBy");
    }
    ContactObject conObj = (ContactObject) objects[0];
    int  value = (Integer) objects[1];
    conObj.setCreatedBy(value);
    return conObj;
  }

  public Object modifiedby(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "ModifiedBy");
    }
    ContactObject conObj = (ContactObject) objects[0];
    int value = (Integer) objects[1];
    conObj.setModifiedBy(value);
    return conObj;
  }

  public Object creationdate(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "CreationDate");
    }
    ContactObject conObj = (ContactObject) objects[0];
    Date value = (Date) objects[1];
    conObj.setCreationDate(value);
    return conObj;
  }

  public Object lastmodified(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "LastModified");
    }
    ContactObject conObj = (ContactObject) objects[0];
    Date value = (Date) objects[1];
    conObj.setLastModified(value);
    return conObj;
  }

  public Object birthday(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "Birthday");
    }
    ContactObject conObj = (ContactObject) objects[0];
    Date value = (Date) objects[1];
    conObj.setBirthday(value);
    return conObj;
  }

  public Object anniversary(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "Anniversary");
    }
    ContactObject conObj = (ContactObject) objects[0];
    Date value = (Date) objects[1];
    conObj.setAnniversary(value);
    return conObj;
  }

  public Object imagelastmodified(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "ImageLastModified");
    }
    ContactObject conObj = (ContactObject) objects[0];
    Date value = (Date) objects[1];
    conObj.setImageLastModified(value);
    return conObj;
  }

  public Object internaluserid(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "InternalUserId");
    }
    ContactObject conObj = (ContactObject) objects[0];
    int value = (Integer) objects[1];
    conObj.setInternalUserId(value);
    return conObj;
  }

  public Object label(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "Label");
    }
    ContactObject conObj = (ContactObject) objects[0];
    int value = (Integer) objects[1];
    conObj.setLabel(value);
    return conObj;
  }

  public Object fileas(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "FileAs");
    }
    ContactObject conObj = (ContactObject) objects[0];
    String value = (String) objects[1];
    conObj.setFileAs(value);
    return conObj;
  }

  public Object defaultaddress(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "DefaultAddress");
    }
    ContactObject conObj = (ContactObject) objects[0];
    int value = (Integer) objects[1];
    conObj.setDefaultAddress(value);
    return conObj;
  }

  public Object numberofattachments(final Object... objects) throws ContactException{
    if(objects.length < 2){
      throw EXCEPTIONS.create(0, "NumberOfAttachments");
    }
    ContactObject conObj = (ContactObject) objects[0];
    int value = (Integer) objects[1];
    conObj.setNumberOfAttachments(value);
    return conObj;
  }

}
