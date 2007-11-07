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
 * This is an interface for classes that operate on the class
 * ContactFields. Implementations might be used to set the value
 * of an ContactObject based on the database field name, for example. 
 * 
 * Reason for the existence of this class: A contat in OX has more than 
 * 100 fields. Imagine you have some values you want to insert into 
 * a contact. You might call every single setter method. But maybe you
 * are lazy. Or you don't know the names, but have only a list of values.
 * Then you should use an object whose class implements this interface. 
 * 
 * Note: This class was generated automagically.
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 * @see com.openexchange.groupware.container.ContactObject
 */
public interface ContactSwitcher{
	  public Object displayname(Object... objects) throws ContactException;
	  public Object surname(Object... objects) throws ContactException;
	  public Object givenname(Object... objects) throws ContactException;
	  public Object middlename(Object... objects) throws ContactException;
	  public Object suffix(Object... objects) throws ContactException;
	  public Object title(Object... objects) throws ContactException;
	  public Object streethome(Object... objects) throws ContactException;
	  public Object postalcodehome(Object... objects) throws ContactException;
	  public Object cityhome(Object... objects) throws ContactException;
	  public Object statehome(Object... objects) throws ContactException;
	  public Object countryhome(Object... objects) throws ContactException;
	  public Object maritalstatus(Object... objects) throws ContactException;
	  public Object numberofchildren(Object... objects) throws ContactException;
	  public Object profession(Object... objects) throws ContactException;
	  public Object nickname(Object... objects) throws ContactException;
	  public Object spousename(Object... objects) throws ContactException;
	  public Object note(Object... objects) throws ContactException;
	  public Object company(Object... objects) throws ContactException;
	  public Object department(Object... objects) throws ContactException;
	  public Object position(Object... objects) throws ContactException;
	  public Object employeetype(Object... objects) throws ContactException;
	  public Object roomnumber(Object... objects) throws ContactException;
	  public Object streetbusiness(Object... objects) throws ContactException;
	  public Object postalcodebusiness(Object... objects) throws ContactException;
	  public Object citybusiness(Object... objects) throws ContactException;
	  public Object statebusiness(Object... objects) throws ContactException;
	  public Object countrybusiness(Object... objects) throws ContactException;
	  public Object numberofemployee(Object... objects) throws ContactException;
	  public Object salesvolume(Object... objects) throws ContactException;
	  public Object taxid(Object... objects) throws ContactException;
	  public Object commercialregister(Object... objects) throws ContactException;
	  public Object branches(Object... objects) throws ContactException;
	  public Object businesscategory(Object... objects) throws ContactException;
	  public Object info(Object... objects) throws ContactException;
	  public Object managername(Object... objects) throws ContactException;
	  public Object assistantname(Object... objects) throws ContactException;
	  public Object streetother(Object... objects) throws ContactException;
	  public Object postalcodeother(Object... objects) throws ContactException;
	  public Object cityother(Object... objects) throws ContactException;
	  public Object stateother(Object... objects) throws ContactException;
	  public Object countryother(Object... objects) throws ContactException;
	  public Object telephoneassistant(Object... objects) throws ContactException;
	  public Object telephonebusiness1(Object... objects) throws ContactException;
	  public Object telephonebusiness2(Object... objects) throws ContactException;
	  public Object faxbusiness(Object... objects) throws ContactException;
	  public Object telephonecallback(Object... objects) throws ContactException;
	  public Object telephonecar(Object... objects) throws ContactException;
	  public Object telephonecompany(Object... objects) throws ContactException;
	  public Object telephonehome1(Object... objects) throws ContactException;
	  public Object telephonehome2(Object... objects) throws ContactException;
	  public Object faxhome(Object... objects) throws ContactException;
	  public Object telephoneisdn(Object... objects) throws ContactException;
	  public Object cellulartelephone1(Object... objects) throws ContactException;
	  public Object cellulartelephone2(Object... objects) throws ContactException;
	  public Object telephoneother(Object... objects) throws ContactException;
	  public Object faxother(Object... objects) throws ContactException;
	  public Object telephonepager(Object... objects) throws ContactException;
	  public Object telephoneprimary(Object... objects) throws ContactException;
	  public Object telephoneradio(Object... objects) throws ContactException;
	  public Object telephonetelex(Object... objects) throws ContactException;
	  public Object telephonettyttd(Object... objects) throws ContactException;
	  public Object instantmessenger1(Object... objects) throws ContactException;
	  public Object instantmessenger2(Object... objects) throws ContactException;
	  public Object telephoneip(Object... objects) throws ContactException;
	  public Object email1(Object... objects) throws ContactException;
	  public Object email2(Object... objects) throws ContactException;
	  public Object email3(Object... objects) throws ContactException;
	  public Object url(Object... objects) throws ContactException;
	  public Object categories(Object... objects) throws ContactException;
	  public Object userfield01(Object... objects) throws ContactException;
	  public Object userfield02(Object... objects) throws ContactException;
	  public Object userfield03(Object... objects) throws ContactException;
	  public Object userfield04(Object... objects) throws ContactException;
	  public Object userfield05(Object... objects) throws ContactException;
	  public Object userfield06(Object... objects) throws ContactException;
	  public Object userfield07(Object... objects) throws ContactException;
	  public Object userfield08(Object... objects) throws ContactException;
	  public Object userfield09(Object... objects) throws ContactException;
	  public Object userfield10(Object... objects) throws ContactException;
	  public Object userfield11(Object... objects) throws ContactException;
	  public Object userfield12(Object... objects) throws ContactException;
	  public Object userfield13(Object... objects) throws ContactException;
	  public Object userfield14(Object... objects) throws ContactException;
	  public Object userfield15(Object... objects) throws ContactException;
	  public Object userfield16(Object... objects) throws ContactException;
	  public Object userfield17(Object... objects) throws ContactException;
	  public Object userfield18(Object... objects) throws ContactException;
	  public Object userfield19(Object... objects) throws ContactException;
	  public Object userfield20(Object... objects) throws ContactException;
	  public Object objectid(Object... objects) throws ContactException;
	  public Object numberofdistributionlists(Object... objects) throws ContactException;
	  public Object numberoflinks(Object... objects) throws ContactException;
	  public Object distributionlist(Object... objects) throws ContactException;
	  public Object links(Object... objects) throws ContactException;
	  public Object parentfolderid(Object... objects) throws ContactException;
	  public Object contextid(Object... objects) throws ContactException;
	  public Object privateflag(Object... objects) throws ContactException;
	  public Object createdby(Object... objects) throws ContactException;
	  public Object modifiedby(Object... objects) throws ContactException;
	  public Object creationdate(Object... objects) throws ContactException;
	  public Object lastmodified(Object... objects) throws ContactException;
	  public Object birthday(Object... objects) throws ContactException;
	  public Object anniversary(Object... objects) throws ContactException;
	  public Object imagelastmodified(Object... objects) throws ContactException;
	  public Object internaluserid(Object... objects) throws ContactException;
	  public Object label(Object... objects) throws ContactException;
	  public Object fileas(Object... objects) throws ContactException;
	  public Object defaultaddress(Object... objects) throws ContactException;
	  public Object numberofattachments(Object... objects) throws ContactException;
	}
