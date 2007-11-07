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

import com.openexchange.groupware.contact.ContactException;

/**
 * Makes strings out of given objects
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */
public class ContactStringGetter implements ContactSwitcher {
	private ContactSwitcher delegate;

	/**
	 * This method makes a string out of an object. 
	 * Opposed to toString, it does not break in case of null pointers.
	 *  
	 * @param obj a given object
	 * @return an empty string if <code>obj == null</code>, otherwise the result of <code>obj.toString()</obj>;
	 */
	public static final String stringify(final Object obj){
		if(obj == null){
			return "";
		}
		return obj.toString();
	}
	/* DELEGATE */
	public ContactSwitcher getDelegate() {
		return delegate;
	}

	public void setDelegate(final ContactSwitcher delegate) {
		this.delegate = delegate;
	}
	
	/* INTERFACE */
	public Object anniversary(final Object... objects) throws ContactException {
		return stringify(delegate.anniversary(objects));
	}

	public Object assistantname(final Object... objects) throws ContactException {
		return stringify(delegate.assistantname(objects));
	}

	public Object birthday(final Object... objects) throws ContactException {
		return stringify(delegate.birthday(objects));
	}

	public Object branches(final Object... objects) throws ContactException {
		return stringify(delegate.branches(objects));
	}

	public Object businesscategory(final Object... objects) throws ContactException {
		return stringify(delegate.businesscategory(objects));
	}

	public Object categories(final Object... objects) throws ContactException {
		return stringify(delegate.categories(objects));
	}

	public Object cellulartelephone1(final Object... objects) throws ContactException {
		return stringify(delegate.cellulartelephone1(objects));
	}

	public Object cellulartelephone2(final Object... objects) throws ContactException {
		return stringify(delegate.cellulartelephone2(objects));
	}

	public Object citybusiness(final Object... objects) throws ContactException {
		return stringify(delegate.citybusiness(objects));
	}

	public Object cityhome(final Object... objects) throws ContactException {
		return stringify(delegate.cityhome(objects));
	}

	public Object cityother(final Object... objects) throws ContactException {
		return stringify(delegate.cityother(objects));
	}

	public Object commercialregister(final Object... objects) throws ContactException {
		return stringify(delegate.commercialregister(objects));
	}

	public Object company(final Object... objects) throws ContactException {
		return stringify(delegate.company(objects));
	}

	public Object contextid(final Object... objects) throws ContactException {
		return stringify(delegate.contextid(objects));
	}

	public Object countrybusiness(final Object... objects) throws ContactException {
		return stringify(delegate.countrybusiness(objects));
	}

	public Object countryhome(final Object... objects) throws ContactException {
		return stringify(delegate.countryhome(objects));
	}

	public Object countryother(final Object... objects) throws ContactException {
		return stringify(delegate.countryother(objects));
	}

	public Object createdby(final Object... objects) throws ContactException {
		return stringify(delegate.createdby(objects));
	}

	public Object creationdate(final Object... objects) throws ContactException {
		return stringify(delegate.creationdate(objects));
	}

	public Object defaultaddress(final Object... objects) throws ContactException {
		return stringify(delegate.defaultaddress(objects));
	}

	public Object department(final Object... objects) throws ContactException {
		return stringify(delegate.department(objects));
	}

	public Object displayname(final Object... objects) throws ContactException {
		return stringify(delegate.displayname(objects));
	}

	public Object distributionlist(final Object... objects) throws ContactException {
		return stringify(delegate.distributionlist(objects));
	}

	public Object email1(final Object... objects) throws ContactException {
		return stringify(delegate.email1(objects));
	}

	public Object email2(final Object... objects) throws ContactException {
		return stringify(delegate.email2(objects));
	}

	public Object email3(final Object... objects) throws ContactException {
		return stringify(delegate.email3(objects));
	}

	public Object employeetype(final Object... objects) throws ContactException {
		return stringify(delegate.employeetype(objects));
	}

	public Object faxbusiness(final Object... objects) throws ContactException {
		return stringify(delegate.faxbusiness(objects));
	}

	public Object faxhome(final Object... objects) throws ContactException {
		return stringify(delegate.faxhome(objects));
	}

	public Object faxother(final Object... objects) throws ContactException {
		return stringify(delegate.faxother(objects));
	}

	public Object fileas(final Object... objects) throws ContactException {
		return stringify(delegate.fileas(objects));
	}

	public Object givenname(final Object... objects) throws ContactException {
		return stringify(delegate.givenname(objects));
	}

	public Object imagelastmodified(final Object... objects) throws ContactException {
		return stringify(delegate.imagelastmodified(objects));
	}

	public Object info(final Object... objects) throws ContactException {
		return stringify(delegate.info(objects));
	}

	public Object instantmessenger1(final Object... objects) throws ContactException {
		return stringify(delegate.instantmessenger1(objects));
	}

	public Object instantmessenger2(final Object... objects) throws ContactException {
		return stringify(delegate.instantmessenger2(objects));
	}

	public Object internaluserid(final Object... objects) throws ContactException {
		return stringify(delegate.internaluserid(objects));
	}

	public Object label(final Object... objects) throws ContactException {
		return stringify(delegate.label(objects));
	}

	public Object lastmodified(final Object... objects) throws ContactException {
		return stringify(delegate.lastmodified(objects));
	}

	public Object links(final Object... objects) throws ContactException {
		return stringify(delegate.links(objects));
	}

	public Object managername(final Object... objects) throws ContactException {
		return stringify(delegate.managername(objects));
	}

	public Object maritalstatus(final Object... objects) throws ContactException {
		return stringify(delegate.maritalstatus(objects));
	}

	public Object middlename(final Object... objects) throws ContactException {
		return stringify(delegate.middlename(objects));
	}

	public Object modifiedby(final Object... objects) throws ContactException {
		return stringify(delegate.modifiedby(objects));
	}

	public Object nickname(final Object... objects) throws ContactException {
		return stringify(delegate.nickname(objects));
	}

	public Object note(final Object... objects) throws ContactException {
		return stringify(delegate.note(objects));
	}

	public Object numberofattachments(final Object... objects) throws ContactException {
		return stringify(delegate.numberofattachments(objects));
	}

	public Object numberofchildren(final Object... objects) throws ContactException {
		return stringify(delegate.numberofchildren(objects));
	}

	public Object numberofdistributionlists(final Object... objects) throws ContactException {
		return stringify(delegate.numberofdistributionlists(objects));
	}

	public Object numberofemployee(final Object... objects) throws ContactException {
		return stringify(delegate.numberofemployee(objects));
	}

	public Object numberoflinks(final Object... objects) throws ContactException {
		return stringify(delegate.numberoflinks(objects));
	}

	public Object objectid(final Object... objects) throws ContactException {
		return stringify(delegate.objectid(objects));
	}

	public Object parentfolderid(final Object... objects) throws ContactException {
		return stringify(delegate.parentfolderid(objects));
	}

	public Object position(final Object... objects) throws ContactException {
		return stringify(delegate.position(objects));
	}

	public Object postalcodebusiness(final Object... objects) throws ContactException {
		return stringify(delegate.postalcodebusiness(objects));
	}

	public Object postalcodehome(final Object... objects) throws ContactException {
		return stringify(delegate.postalcodehome(objects));
	}

	public Object postalcodeother(final Object... objects) throws ContactException {
		return stringify(delegate.postalcodeother(objects));
	}

	public Object privateflag(final Object... objects) throws ContactException {
		return stringify(delegate.privateflag(objects));
	}

	public Object profession(final Object... objects) throws ContactException {
		return stringify(delegate.profession(objects));
	}

	public Object roomnumber(final Object... objects) throws ContactException {
		return stringify(delegate.roomnumber(objects));
	}

	public Object salesvolume(final Object... objects) throws ContactException {
		return stringify(delegate.salesvolume(objects));
	}

	public Object spousename(final Object... objects) throws ContactException {
		return stringify(delegate.spousename(objects));
	}

	public Object statebusiness(final Object... objects) throws ContactException {
		return stringify(delegate.statebusiness(objects));
	}

	public Object statehome(final Object... objects) throws ContactException {
		return stringify(delegate.statehome(objects));
	}

	public Object stateother(final Object... objects) throws ContactException {
		return stringify(delegate.stateother(objects));
	}

	public Object streetbusiness(final Object... objects) throws ContactException {
		return stringify(delegate.streetbusiness(objects));
	}

	public Object streethome(final Object... objects) throws ContactException {
		return stringify(delegate.streethome(objects));
	}

	public Object streetother(final Object... objects) throws ContactException {
		return stringify(delegate.streetother(objects));
	}

	public Object suffix(final Object... objects) throws ContactException {
		return stringify(delegate.suffix(objects));
	}

	public Object surname(final Object... objects) throws ContactException {
		return stringify(delegate.surname(objects));
	}

	public Object taxid(final Object... objects) throws ContactException {
		return stringify(delegate.taxid(objects));
	}

	public Object telephoneassistant(final Object... objects) throws ContactException {
		return stringify(delegate.telephoneassistant(objects));
	}

	public Object telephonebusiness1(final Object... objects) throws ContactException {
		return stringify(delegate.telephonebusiness1(objects));
	}

	public Object telephonebusiness2(final Object... objects) throws ContactException {
		return stringify(delegate.telephonebusiness2(objects));
	}

	public Object telephonecallback(final Object... objects) throws ContactException {
		return stringify(delegate.telephonecallback(objects));
	}

	public Object telephonecar(final Object... objects) throws ContactException {
		return stringify(delegate.telephonecar(objects));
	}

	public Object telephonecompany(final Object... objects) throws ContactException {
		return stringify(delegate.telephonecompany(objects));
	}

	public Object telephonehome1(final Object... objects) throws ContactException {
		return stringify(delegate.telephonehome1(objects));
	}

	public Object telephonehome2(final Object... objects) throws ContactException {
		return stringify(delegate.telephonehome2(objects));
	}

	public Object telephoneip(final Object... objects) throws ContactException {
		return stringify(delegate.telephoneip(objects));
	}

	public Object telephoneisdn(final Object... objects) throws ContactException {
		return stringify(delegate.telephoneisdn(objects));
	}

	public Object telephoneother(final Object... objects) throws ContactException {
		return stringify(delegate.telephoneother(objects));
	}

	public Object telephonepager(final Object... objects) throws ContactException {
		return stringify(delegate.telephonepager(objects));
	}

	public Object telephoneprimary(final Object... objects) throws ContactException {
		return stringify(delegate.telephoneprimary(objects));
	}

	public Object telephoneradio(final Object... objects) throws ContactException {
		return stringify(delegate.telephoneradio(objects));
	}

	public Object telephonetelex(final Object... objects) throws ContactException {
		return stringify(delegate.telephonetelex(objects));
	}

	public Object telephonettyttd(final Object... objects) throws ContactException {
		return stringify(delegate.telephonettyttd(objects));
	}

	public Object title(final Object... objects) throws ContactException {
		return stringify(delegate.title(objects));
	}

	public Object url(final Object... objects) throws ContactException {
		return stringify(delegate.url(objects));
	}

	public Object userfield01(final Object... objects) throws ContactException {
		return stringify(delegate.userfield01(objects));
	}

	public Object userfield02(final Object... objects) throws ContactException {
		return stringify(delegate.userfield02(objects));
	}

	public Object userfield03(final Object... objects) throws ContactException {
		return stringify(delegate.userfield03(objects));
	}

	public Object userfield04(final Object... objects) throws ContactException {
		return stringify(delegate.userfield04(objects));
	}

	public Object userfield05(final Object... objects) throws ContactException {
		return stringify(delegate.userfield05(objects));
	}

	public Object userfield06(final Object... objects) throws ContactException {
		return stringify(delegate.userfield06(objects));
	}

	public Object userfield07(final Object... objects) throws ContactException {
		return stringify(delegate.userfield07(objects));
	}

	public Object userfield08(final Object... objects) throws ContactException {
		return stringify(delegate.userfield08(objects));
	}

	public Object userfield09(final Object... objects) throws ContactException {
		return stringify(delegate.userfield09(objects));
	}

	public Object userfield10(final Object... objects) throws ContactException {
		return stringify(delegate.userfield10(objects));
	}

	public Object userfield11(final Object... objects) throws ContactException {
		return stringify(delegate.userfield11(objects));
	}

	public Object userfield12(final Object... objects) throws ContactException {
		return stringify(delegate.userfield12(objects));
	}

	public Object userfield13(final Object... objects) throws ContactException {
		return stringify(delegate.userfield13(objects));
	}

	public Object userfield14(final Object... objects) throws ContactException {
		return stringify(delegate.userfield14(objects));
	}

	public Object userfield15(final Object... objects) throws ContactException {
		return stringify(delegate.userfield15(objects));
	}

	public Object userfield16(final Object... objects) throws ContactException {
		return stringify(delegate.userfield16(objects));
	}

	public Object userfield17(final Object... objects) throws ContactException {
		return stringify(delegate.userfield17(objects));
	}

	public Object userfield18(final Object... objects) throws ContactException {
		return stringify(delegate.userfield18(objects));
	}

	public Object userfield19(final Object... objects) throws ContactException {
		return stringify(delegate.userfield19(objects));
	}

	public Object userfield20(final Object... objects) throws ContactException {
		return stringify(delegate.userfield20(objects));
	}

}
