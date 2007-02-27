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

import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.openexchange.groupware.Component;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.contact.Classes;
import com.openexchange.groupware.contact.ContactException;
import com.openexchange.groupware.contact.ContactExceptionFactory;

@OXExceptionSource(
	classId=Classes.COM_OPENEXCHANGE_GROUPWARE_CONTACTS_HELPERS_CONTACTSETTERFORSIMPLEDATEGFORMAT, 
	component=Component.CONTACT)
@OXThrowsMultiple(
	category={Category.PROGRAMMING_ERROR}, 
	desc={""}, 
	exceptionId={0}, 
	msg={
		"Could not convert given String %s to a date."
	})

/**
 * This switcher is able to convert a given String into a date using
 * SimpleDateFormat and then pass it on to its delegate.
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 */
public class ContactSwitcherForSimpleDateFormat implements ContactSwitcher {

	private static final ContactExceptionFactory EXCEPTIONS = new ContactExceptionFactory(ContactSwitcherForSimpleDateFormat.class);
	private SimpleDateFormat dateFormat;
	private ContactSwitcher delegate;
	
	private Object[] makeDate(Object... objects) throws ParseException{
		objects[1] = dateFormat.parse( (String) objects[1] );
		return objects;
	}
	
	/* GETTERS & SETTERS */
	public SimpleDateFormat getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(SimpleDateFormat dateFormat) {
		this.dateFormat = dateFormat;
	}

	public ContactSwitcher getDelegate() {
		return delegate;
	}

	public void setDelegate(ContactSwitcher delegate) {
		this.delegate = delegate;
	}	
	
	/* CHANGED METHODS */
	public Object creationdate(Object... objects) throws ContactException {
		try {
			return delegate.creationdate( makeDate(objects) );
		} catch (ParseException e) {
			throw EXCEPTIONS.create(0, (String) objects[1] ,  "CreationDate", e);
		}
	}

	public Object anniversary(Object... objects) throws ContactException {
		try {
			return delegate.creationdate( makeDate(objects) );
		} catch (ParseException e) {
			throw EXCEPTIONS.create(0, (String) objects[1] ,  "Anniversary", e);
		}
	}

	public Object birthday(Object... objects) throws ContactException {
		try {
			return delegate.creationdate( makeDate(objects) );
		} catch (ParseException e) {
			throw EXCEPTIONS.create(0, (String) objects[1] ,  "Birthday", e);
		}
	}

	public Object imagelastmodified(Object... objects) throws ContactException {
		try {
			return delegate.creationdate( makeDate(objects) );
		} catch (ParseException e) {
			throw EXCEPTIONS.create(0, (String) objects[1] ,  "ImageLastModified", e);
		}
	}

	public Object lastmodified(Object... objects) throws ContactException {
		try {
			return delegate.creationdate( makeDate(objects) );
		} catch (ParseException e) {
			throw EXCEPTIONS.create(0, (String) objects[1] ,  "LastModified", e);
		}
	}

	/* NORMAL DELEGATE METHODS */
	public Object assistantname(Object... objects) throws ContactException {
		return delegate.assistantname(objects);
	}

	public Object branches(Object... objects) throws ContactException {
		return delegate.branches(objects);
	}

	public Object businesscategory(Object... objects) throws ContactException {
		return delegate.businesscategory(objects);
	}

	public Object categories(Object... objects) throws ContactException {
		return delegate.categories(objects);
	}

	public Object cellulartelephone1(Object... objects) throws ContactException {
		return delegate.cellulartelephone1(objects);
	}

	public Object cellulartelephone2(Object... objects) throws ContactException {
		return delegate.cellulartelephone2(objects);
	}

	public Object citybusiness(Object... objects) throws ContactException {
		return delegate.citybusiness(objects);
	}

	public Object cityhome(Object... objects) throws ContactException {
		return delegate.cityhome(objects);
	}

	public Object cityother(Object... objects) throws ContactException {
		return delegate.cityother(objects);
	}

	public Object commercialregister(Object... objects) throws ContactException {
		return delegate.commercialregister(objects);
	}

	public Object company(Object... objects) throws ContactException {
		return delegate.company(objects);
	}

	public Object contextid(Object... objects) throws ContactException {
		return delegate.contextid(objects);
	}

	public Object countrybusiness(Object... objects) throws ContactException {
		return delegate.countrybusiness(objects);
	}

	public Object countryhome(Object... objects) throws ContactException {
		return delegate.countryhome(objects);
	}

	public Object countryother(Object... objects) throws ContactException {
		return delegate.countryother(objects);
	}

	public Object createdby(Object... objects) throws ContactException {
		return delegate.createdby(objects);
	}

	public Object defaultaddress(Object... objects) throws ContactException {
		return delegate.defaultaddress(objects);
	}

	public Object department(Object... objects) throws ContactException {
		return delegate.department(objects);
	}

	public Object displayname(Object... objects) throws ContactException {
		return delegate.displayname(objects);
	}

	public Object distributionlist(Object... objects) throws ContactException {
		return delegate.distributionlist(objects);
	}

	public Object email1(Object... objects) throws ContactException {
		return delegate.email1(objects);
	}

	public Object email2(Object... objects) throws ContactException {
		return delegate.email2(objects);
	}

	public Object email3(Object... objects) throws ContactException {
		return delegate.email3(objects);
	}

	public Object employeetype(Object... objects) throws ContactException {
		return delegate.employeetype(objects);
	}

	public boolean equals(Object obj) {
		return delegate.equals(obj);
	}

	public Object faxbusiness(Object... objects) throws ContactException {
		return delegate.faxbusiness(objects);
	}

	public Object faxhome(Object... objects) throws ContactException {
		return delegate.faxhome(objects);
	}

	public Object faxother(Object... objects) throws ContactException {
		return delegate.faxother(objects);
	}

	public Object fileas(Object... objects) throws ContactException {
		return delegate.fileas(objects);
	}

	public Object givenname(Object... objects) throws ContactException {
		return delegate.givenname(objects);
	}

	public int hashCode() {
		return delegate.hashCode();
	}

	public Object info(Object... objects) throws ContactException {
		return delegate.info(objects);
	}

	public Object instantmessenger1(Object... objects) throws ContactException {
		return delegate.instantmessenger1(objects);
	}

	public Object instantmessenger2(Object... objects) throws ContactException {
		return delegate.instantmessenger2(objects);
	}

	public Object internaluserid(Object... objects) throws ContactException {
		return delegate.internaluserid(objects);
	}

	public Object label(Object... objects) throws ContactException {
		return delegate.label(objects);
	}

	public Object links(Object... objects) throws ContactException {
		return delegate.links(objects);
	}

	public Object managername(Object... objects) throws ContactException {
		return delegate.managername(objects);
	}

	public Object maritalstatus(Object... objects) throws ContactException {
		return delegate.maritalstatus(objects);
	}

	public Object middlename(Object... objects) throws ContactException {
		return delegate.middlename(objects);
	}

	public Object modifiedby(Object... objects) throws ContactException {
		return delegate.modifiedby(objects);
	}

	public Object nickname(Object... objects) throws ContactException {
		return delegate.nickname(objects);
	}

	public Object note(Object... objects) throws ContactException {
		return delegate.note(objects);
	}

	public Object numberofattachments(Object... objects) throws ContactException {
		return delegate.numberofattachments(objects);
	}

	public Object numberofchildren(Object... objects) throws ContactException {
		return delegate.numberofchildren(objects);
	}

	public Object numberofdistributionlists(Object... objects) throws ContactException {
		return delegate.numberofdistributionlists(objects);
	}

	public Object numberofemployee(Object... objects) throws ContactException {
		return delegate.numberofemployee(objects);
	}

	public Object numberoflinks(Object... objects) throws ContactException {
		return delegate.numberoflinks(objects);
	}

	public Object objectid(Object... objects) throws ContactException {
		return delegate.objectid(objects);
	}

	public Object parentfolderid(Object... objects) throws ContactException {
		return delegate.parentfolderid(objects);
	}

	public Object position(Object... objects) throws ContactException {
		return delegate.position(objects);
	}

	public Object postalcodebusiness(Object... objects) throws ContactException {
		return delegate.postalcodebusiness(objects);
	}

	public Object postalcodehome(Object... objects) throws ContactException {
		return delegate.postalcodehome(objects);
	}

	public Object postalcodeother(Object... objects) throws ContactException {
		return delegate.postalcodeother(objects);
	}

	public Object privateflag(Object... objects) throws ContactException {
		return delegate.privateflag(objects);
	}

	public Object profession(Object... objects) throws ContactException {
		return delegate.profession(objects);
	}

	public Object roomnumber(Object... objects) throws ContactException {
		return delegate.roomnumber(objects);
	}

	public Object salesvolume(Object... objects) throws ContactException {
		return delegate.salesvolume(objects);
	}

	public Object spousename(Object... objects) throws ContactException {
		return delegate.spousename(objects);
	}

	public Object statebusiness(Object... objects) throws ContactException {
		return delegate.statebusiness(objects);
	}

	public Object statehome(Object... objects) throws ContactException {
		return delegate.statehome(objects);
	}

	public Object stateother(Object... objects) throws ContactException {
		return delegate.stateother(objects);
	}

	public Object streetbusiness(Object... objects) throws ContactException {
		return delegate.streetbusiness(objects);
	}

	public Object streethome(Object... objects) throws ContactException {
		return delegate.streethome(objects);
	}

	public Object streetother(Object... objects) throws ContactException {
		return delegate.streetother(objects);
	}

	public Object suffix(Object... objects) throws ContactException {
		return delegate.suffix(objects);
	}

	public Object surname(Object... objects) throws ContactException {
		return delegate.surname(objects);
	}

	public Object taxid(Object... objects) throws ContactException {
		return delegate.taxid(objects);
	}

	public Object telephoneassistant(Object... objects) throws ContactException {
		return delegate.telephoneassistant(objects);
	}

	public Object telephonebusiness1(Object... objects) throws ContactException {
		return delegate.telephonebusiness1(objects);
	}

	public Object telephonebusiness2(Object... objects) throws ContactException {
		return delegate.telephonebusiness2(objects);
	}

	public Object telephonecallback(Object... objects) throws ContactException {
		return delegate.telephonecallback(objects);
	}

	public Object telephonecar(Object... objects) throws ContactException {
		return delegate.telephonecar(objects);
	}

	public Object telephonecompany(Object... objects) throws ContactException {
		return delegate.telephonecompany(objects);
	}

	public Object telephonehome1(Object... objects) throws ContactException {
		return delegate.telephonehome1(objects);
	}

	public Object telephonehome2(Object... objects) throws ContactException {
		return delegate.telephonehome2(objects);
	}

	public Object telephoneip(Object... objects) throws ContactException {
		return delegate.telephoneip(objects);
	}

	public Object telephoneisdn(Object... objects) throws ContactException {
		return delegate.telephoneisdn(objects);
	}

	public Object telephoneother(Object... objects) throws ContactException {
		return delegate.telephoneother(objects);
	}

	public Object telephonepager(Object... objects) throws ContactException {
		return delegate.telephonepager(objects);
	}

	public Object telephoneprimary(Object... objects) throws ContactException {
		return delegate.telephoneprimary(objects);
	}

	public Object telephoneradio(Object... objects) throws ContactException {
		return delegate.telephoneradio(objects);
	}

	public Object telephonetelex(Object... objects) throws ContactException {
		return delegate.telephonetelex(objects);
	}

	public Object telephonettyttd(Object... objects) throws ContactException {
		return delegate.telephonettyttd(objects);
	}

	public Object title(Object... objects) throws ContactException {
		return delegate.title(objects);
	}

	public String toString() {
		return delegate.toString();
	}

	public Object url(Object... objects) throws ContactException {
		return delegate.url(objects);
	}

	public Object userfield01(Object... objects) throws ContactException {
		return delegate.userfield01(objects);
	}

	public Object userfield02(Object... objects) throws ContactException {
		return delegate.userfield02(objects);
	}

	public Object userfield03(Object... objects) throws ContactException {
		return delegate.userfield03(objects);
	}

	public Object userfield04(Object... objects) throws ContactException {
		return delegate.userfield04(objects);
	}

	public Object userfield05(Object... objects) throws ContactException {
		return delegate.userfield05(objects);
	}

	public Object userfield06(Object... objects) throws ContactException {
		return delegate.userfield06(objects);
	}

	public Object userfield07(Object... objects) throws ContactException {
		return delegate.userfield07(objects);
	}

	public Object userfield08(Object... objects) throws ContactException {
		return delegate.userfield08(objects);
	}

	public Object userfield09(Object... objects) throws ContactException {
		return delegate.userfield09(objects);
	}

	public Object userfield10(Object... objects) throws ContactException {
		return delegate.userfield10(objects);
	}

	public Object userfield11(Object... objects) throws ContactException {
		return delegate.userfield11(objects);
	}

	public Object userfield12(Object... objects) throws ContactException {
		return delegate.userfield12(objects);
	}

	public Object userfield13(Object... objects) throws ContactException {
		return delegate.userfield13(objects);
	}

	public Object userfield14(Object... objects) throws ContactException {
		return delegate.userfield14(objects);
	}

	public Object userfield15(Object... objects) throws ContactException {
		return delegate.userfield15(objects);
	}

	public Object userfield16(Object... objects) throws ContactException {
		return delegate.userfield16(objects);
	}

	public Object userfield17(Object... objects) throws ContactException {
		return delegate.userfield17(objects);
	}

	public Object userfield18(Object... objects) throws ContactException {
		return delegate.userfield18(objects);
	}

	public Object userfield19(Object... objects) throws ContactException {
		return delegate.userfield19(objects);
	}

	public Object userfield20(Object... objects) throws ContactException {
		return delegate.userfield20(objects);
	}


	
	
}
