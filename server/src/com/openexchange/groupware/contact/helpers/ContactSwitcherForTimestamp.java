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
import java.util.Date;

import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.contact.Classes;
import com.openexchange.groupware.contact.ContactException;
import com.openexchange.groupware.contact.ContactExceptionFactory;

@OXExceptionSource(
	classId=Classes.COM_OPENEXCHANGE_GROUPWARE_CONTACTS_HELPERS_CONTACTSETTERFORSIMPLEDATEGFORMAT, 
	component=EnumComponent.CONTACT)
@OXThrowsMultiple(
	category={Category.CODE_ERROR}, 
	desc={""}, 
	exceptionId={0}, 
	msg={
		"Could not convert given object %s to a date when setting %s."
	})
/**
 * This switcher is able to convert a given String into a date by
 * interpreting is as a timestamp (type: String holding a long) and 
 * then pass it on to its delegate.
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 */
public class ContactSwitcherForTimestamp extends AbstractContactSwitcherWithDelegate {

	protected static final ContactExceptionFactory EXCEPTIONS = new ContactExceptionFactory(ContactSwitcherForSimpleDateFormat.class);
	
	protected Object[] makeDate(final Object... objects) throws ParseException, NumberFormatException{
		if(objects[1] instanceof String){
			objects[1] = new Date( Long.parseLong((String) objects[1]));
		} else {
			objects[1] = new Date( (Long) objects[1] ); 
		}
		return objects;
	}

	/* CHANGED METHODS */
	public Object creationdate(final Object... objects) throws ContactException {
		try {
			return delegate.creationdate( makeDate(objects) );
		} catch (final ParseException e) {
			throw EXCEPTIONS.create(0, objects[1] ,   "CreationDate", e);
		} catch (final ClassCastException e){
			throw EXCEPTIONS.create(0, objects[1] ,   "CreationDate", e);
		} catch (final NumberFormatException e){
			throw EXCEPTIONS.create(0, objects[1] ,   "CreationDate", e);
		}
	}

	public Object anniversary(final Object... objects) throws ContactException {
		try {
			return delegate.anniversary( makeDate(objects) );
		} catch (final ParseException e) {
			throw EXCEPTIONS.create(0, objects[1] ,   "Anniversary", e);
		} catch (final ClassCastException e){
			throw EXCEPTIONS.create(0, objects[1] ,   "Anniversary", e);
		} catch (final NumberFormatException e){
			throw EXCEPTIONS.create(0, objects[1] ,   "Anniversary", e);
		}
	}

	public Object birthday(final Object... objects) throws ContactException {
		try {
			return delegate.birthday( makeDate(objects) );
		} catch (final ParseException e) {
			throw EXCEPTIONS.create(0, objects[1] ,   "Birthday", e);
		} catch (final ClassCastException e){
			throw EXCEPTIONS.create(0, objects[1] ,   "Birthday", e);
		} catch (final NumberFormatException e){
			throw EXCEPTIONS.create(0, objects[1] ,   "Birthday", e);
		}
	}

	public Object imagelastmodified(final Object... objects) throws ContactException {
		try {
			return delegate.imagelastmodified( makeDate(objects) );
		} catch (final ParseException e) {
			throw EXCEPTIONS.create(0, objects[1] ,   "ImageLastModified", e);
		} catch (final ClassCastException e){
			throw EXCEPTIONS.create(0, objects[1] ,   "ImageLastModified", e);
		} catch (final NumberFormatException e){
			throw EXCEPTIONS.create(0, objects[1] ,   "ImageLastModified", e);
		}
	}

	public Object lastmodified(final Object... objects) throws ContactException {
		try {
			return delegate.lastmodified( makeDate(objects) );
		} catch (final ParseException e) {
			throw EXCEPTIONS.create(0, objects[1] ,   "LastModified", e);
		} catch (final ClassCastException e){
			throw EXCEPTIONS.create(0, objects[1] ,   "LastModified", e);
		} catch (final NumberFormatException e){
			throw EXCEPTIONS.create(0, objects[1] ,   "LastModified", e);
		}
	}
	
}
