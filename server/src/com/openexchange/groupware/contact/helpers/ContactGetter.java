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

import com.openexchange.groupware.Component;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.contact.Classes;
import com.openexchange.groupware.contact.ContactException;
import com.openexchange.groupware.contact.ContactExceptionFactory;
import com.openexchange.groupware.container.ContactObject;

/**
 * This switcher enables us to get the values of a contact object.
 * As convention, the first argument of a method represents the ContactObject
 * which value is to be retrieved.
 * 
 * Note: This class was generated mostly - don't even try to keep this
 * up to date by hand...
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */

@OXExceptionSource(
	classId=Classes.COM_OPENEXCHANGE_GROUPWARE_CONTACTS_HELPERS_CONTACTGETTER, 
	component=Component.CONTACT)
@OXThrowsMultiple(
	category={Category.CODE_ERROR}, 
	desc={""}, 
	exceptionId={0}, 
	msg={"Need at least a ContactObject to get the value of %s"})
	
public class ContactGetter implements ContactSwitcher {

  private static final ContactExceptionFactory EXCEPTIONS = new ContactExceptionFactory(ContactGetter.class);

  public Object displayname(final Object... objects) throws ContactException{
	    if(objects.length < 1){
	      throw EXCEPTIONS.create(0, "DisplayName");
	    }
	    ContactObject conObj = (ContactObject) objects[0];
	    return conObj.getDisplayName();
	  }

  public Object surname(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "SurName");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getSurName();
  }

  public Object givenname(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "GivenName");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getGivenName();
  }

  public Object middlename(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "MiddleName");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getMiddleName();
  }

  public Object suffix(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "Suffix");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getSuffix();
  }

  public Object title(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "Title");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getTitle();
  }

  public Object streethome(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "StreetHome");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getStreetHome();
  }

  public Object postalcodehome(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "PostalCodeHome");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getPostalCodeHome();
  }

  public Object cityhome(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "CityHome");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getCityHome();
  }

  public Object statehome(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "StateHome");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getStateHome();
  }

  public Object countryhome(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "CountryHome");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getCountryHome();
  }

  public Object maritalstatus(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "MaritalStatus");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getMaritalStatus();
  }

  public Object numberofchildren(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "NumberOfChildren");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getNumberOfChildren();
  }

  public Object profession(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "Profession");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getProfession();
  }

  public Object nickname(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "Nickname");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getNickname();
  }

  public Object spousename(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "SpouseName");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getSpouseName();
  }

  public Object note(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "Note");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getNote();
  }

  public Object company(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "Company");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getCompany();
  }

  public Object department(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "Department");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getDepartment();
  }

  public Object position(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "Position");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getPosition();
  }

  public Object employeetype(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "EmployeeType");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getEmployeeType();
  }

  public Object roomnumber(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "RoomNumber");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getRoomNumber();
  }

  public Object streetbusiness(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "StreetBusiness");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getStreetBusiness();
  }

  public Object postalcodebusiness(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "PostalCodeBusiness");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getPostalCodeBusiness();
  }

  public Object citybusiness(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "CityBusiness");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getCityBusiness();
  }

  public Object statebusiness(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "StateBusiness");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getStateBusiness();
  }

  public Object countrybusiness(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "CountryBusiness");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getCountryBusiness();
  }

  public Object numberofemployee(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "NumberOfEmployee");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getNumberOfEmployee();
  }

  public Object salesvolume(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "SalesVolume");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getSalesVolume();
  }

  public Object taxid(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "TaxID");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getTaxID();
  }

  public Object commercialregister(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "CommercialRegister");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getCommercialRegister();
  }

  public Object branches(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "Branches");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getBranches();
  }

  public Object businesscategory(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "BusinessCategory");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getBusinessCategory();
  }

  public Object info(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "Info");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getInfo();
  }

  public Object managername(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "ManagerName");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getManagerName();
  }

  public Object assistantname(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "AssistantName");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getAssistantName();
  }

  public Object streetother(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "StreetOther");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getStreetOther();
  }

  public Object postalcodeother(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "PostalCodeOther");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getPostalCodeOther();
  }

  public Object cityother(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "CityOther");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getCityOther();
  }

  public Object stateother(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "StateOther");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getStateOther();
  }

  public Object countryother(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "CountryOther");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getCountryOther();
  }

  public Object telephoneassistant(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "TelephoneAssistant");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getTelephoneAssistant();
  }

  public Object telephonebusiness1(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "TelephoneBusiness1");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getTelephoneBusiness1();
  }

  public Object telephonebusiness2(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "TelephoneBusiness2");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getTelephoneBusiness2();
  }

  public Object faxbusiness(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "FaxBusiness");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getFaxBusiness();
  }

  public Object telephonecallback(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "TelephoneCallback");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getTelephoneCallback();
  }

  public Object telephonecar(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "TelephoneCar");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getTelephoneCar();
  }

  public Object telephonecompany(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "TelephoneCompany");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getTelephoneCompany();
  }

  public Object telephonehome1(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "TelephoneHome1");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getTelephoneHome1();
  }

  public Object telephonehome2(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "TelephoneHome2");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getTelephoneHome2();
  }

  public Object faxhome(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "FaxHome");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getFaxHome();
  }

  public Object telephoneisdn(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "TelephoneISDN");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getTelephoneISDN();
  }

  public Object cellulartelephone1(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "CellularTelephone1");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getCellularTelephone1();
  }

  public Object cellulartelephone2(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "CellularTelephone2");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getCellularTelephone2();
  }

  public Object telephoneother(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "TelephoneOther");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getTelephoneOther();
  }

  public Object faxother(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "FaxOther");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getFaxOther();
  }

  public Object telephonepager(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "TelephonePager");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getTelephonePager();
  }

  public Object telephoneprimary(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "TelephonePrimary");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getTelephonePrimary();
  }

  public Object telephoneradio(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "TelephoneRadio");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getTelephoneRadio();
  }

  public Object telephonetelex(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "TelephoneTelex");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getTelephoneTelex();
  }

  public Object telephonettyttd(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "TelephoneTTYTTD");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getTelephoneTTYTTD();
  }

  public Object instantmessenger1(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "InstantMessenger1");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getInstantMessenger1();
  }

  public Object instantmessenger2(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "InstantMessenger2");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getInstantMessenger2();
  }

  public Object telephoneip(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "TelephoneIP");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getTelephoneIP();
  }

  public Object email1(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "Email1");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getEmail1();
  }

  public Object email2(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "Email2");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getEmail2();
  }

  public Object email3(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "Email3");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getEmail3();
  }

  public Object url(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "URL");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getURL();
  }

  public Object categories(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "Categories");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getCategories();
  }

  public Object userfield01(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "UserField01");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getUserField01();
  }

  public Object userfield02(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "UserField02");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getUserField02();
  }

  public Object userfield03(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "UserField03");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getUserField03();
  }

  public Object userfield04(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "UserField04");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getUserField04();
  }

  public Object userfield05(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "UserField05");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getUserField05();
  }

  public Object userfield06(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "UserField06");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getUserField06();
  }

  public Object userfield07(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "UserField07");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getUserField07();
  }

  public Object userfield08(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "UserField08");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getUserField08();
  }

  public Object userfield09(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "UserField09");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getUserField09();
  }

  public Object userfield10(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "UserField10");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getUserField10();
  }

  public Object userfield11(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "UserField11");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getUserField11();
  }

  public Object userfield12(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "UserField12");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getUserField12();
  }

  public Object userfield13(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "UserField13");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getUserField13();
  }

  public Object userfield14(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "UserField14");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getUserField14();
  }

  public Object userfield15(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "UserField15");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getUserField15();
  }

  public Object userfield16(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "UserField16");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getUserField16();
  }

  public Object userfield17(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "UserField17");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getUserField17();
  }

  public Object userfield18(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "UserField18");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getUserField18();
  }

  public Object userfield19(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "UserField19");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getUserField19();
  }

  public Object userfield20(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "UserField20");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getUserField20();
  }

  public Object objectid(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "ObjectID");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getObjectID();
  }

  public Object numberofdistributionlists(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "NumberOfDistributionLists");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getNumberOfDistributionLists();
  }

  public Object numberoflinks(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "NumberOfLinks");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getNumberOfLinks();
  }

  public Object distributionlist(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "DistributionList");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getDistributionList();
  }

  public Object links(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "Links");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getLinks();
  }

  public Object parentfolderid(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "ParentFolderID");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getParentFolderID();
  }

  public Object contextid(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "ContextId");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getContextId();
  }

  public Object privateflag(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "PrivateFlag");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getPrivateFlag();
  }

  public Object createdby(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "CreatedBy");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getCreatedBy();
  }

  public Object modifiedby(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "ModifiedBy");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getModifiedBy();
  }

  public Object creationdate(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "CreationDate");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getCreationDate();
  }

  public Object lastmodified(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "LastModified");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getLastModified();
  }

  public Object birthday(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "Birthday");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getBirthday();
  }

  public Object anniversary(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "Anniversary");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getAnniversary();
  }

  public Object imagelastmodified(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "ImageLastModified");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getImageLastModified();
  }

  public Object internaluserid(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "InternalUserId");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getInternalUserId();
  }

  public Object label(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "Label");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getLabel();
  }

  public Object fileas(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "FileAs");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getFileAs();
  }

  public Object defaultaddress(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "DefaultAddress");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getDefaultAddress();
  }

  public Object numberofattachments(final Object... objects) throws ContactException{
    if(objects.length < 1){
      throw EXCEPTIONS.create(0, "NumberOfAttachments");
    }
    ContactObject conObj = (ContactObject) objects[0];
    return conObj.getNumberOfAttachments();
  }
}
