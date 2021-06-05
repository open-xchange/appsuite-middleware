/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.groupware.contact.helpers;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;

/**
 * This is an interface for classes that operate on the class ContactFields. Implementations might be used to set the value of an
 * ContactObject based on the database field name, for example. Reason for the existence of this class: A contat in OX has more than 100
 * fields. Imagine you have some values you want to insert into a contact. You might call every single setter method. But maybe you are
 * lazy. Or you don't know the names, but have only a list of values. Then you should use an object whose class implements this interface.
 * Note: This class was generated automagically.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 * @see com.openexchange.groupware.container.Contact
 */
public interface ContactSwitcher {

    public Object displayname(Object... objects) throws OXException;

    public Object surname(Object... objects) throws OXException;

    public Object givenname(Object... objects) throws OXException;

    public Object middlename(Object... objects) throws OXException;

    public Object suffix(Object... objects) throws OXException;

    public Object title(Object... objects) throws OXException;

    public Object streethome(Object... objects) throws OXException;

    public Object postalcodehome(Object... objects) throws OXException;

    public Object cityhome(Object... objects) throws OXException;

    public Object statehome(Object... objects) throws OXException;

    public Object countryhome(Object... objects) throws OXException;

    public Object maritalstatus(Object... objects) throws OXException;

    public Object numberofchildren(Object... objects) throws OXException;

    public Object profession(Object... objects) throws OXException;

    public Object nickname(Object... objects) throws OXException;

    public Object spousename(Object... objects) throws OXException;

    public Object note(Object... objects) throws OXException;

    public Object company(Object... objects) throws OXException;

    public Object department(Object... objects) throws OXException;

    public Object position(Object... objects) throws OXException;

    public Object employeetype(Object... objects) throws OXException;

    public Object roomnumber(Object... objects) throws OXException;

    public Object streetbusiness(Object... objects) throws OXException;

    public Object postalcodebusiness(Object... objects) throws OXException;

    public Object citybusiness(Object... objects) throws OXException;

    public Object statebusiness(Object... objects) throws OXException;

    public Object countrybusiness(Object... objects) throws OXException;

    public Object numberofemployee(Object... objects) throws OXException;

    public Object salesvolume(Object... objects) throws OXException;

    public Object taxid(Object... objects) throws OXException;

    public Object commercialregister(Object... objects) throws OXException;

    public Object branches(Object... objects) throws OXException;

    public Object businesscategory(Object... objects) throws OXException;

    public Object info(Object... objects) throws OXException;

    public Object managername(Object... objects) throws OXException;

    public Object assistantname(Object... objects) throws OXException;

    public Object streetother(Object... objects) throws OXException;

    public Object postalcodeother(Object... objects) throws OXException;

    public Object cityother(Object... objects) throws OXException;

    public Object stateother(Object... objects) throws OXException;

    public Object countryother(Object... objects) throws OXException;

    public Object telephoneassistant(Object... objects) throws OXException;

    public Object telephonebusiness1(Object... objects) throws OXException;

    public Object telephonebusiness2(Object... objects) throws OXException;

    public Object faxbusiness(Object... objects) throws OXException;

    public Object telephonecallback(Object... objects) throws OXException;

    public Object telephonecar(Object... objects) throws OXException;

    public Object telephonecompany(Object... objects) throws OXException;

    public Object telephonehome1(Object... objects) throws OXException;

    public Object telephonehome2(Object... objects) throws OXException;

    public Object faxhome(Object... objects) throws OXException;

    public Object telephoneisdn(Object... objects) throws OXException;

    public Object cellulartelephone1(Object... objects) throws OXException;

    public Object cellulartelephone2(Object... objects) throws OXException;

    public Object telephoneother(Object... objects) throws OXException;

    public Object faxother(Object... objects) throws OXException;

    public Object telephonepager(Object... objects) throws OXException;

    public Object telephoneprimary(Object... objects) throws OXException;

    public Object telephoneradio(Object... objects) throws OXException;

    public Object telephonetelex(Object... objects) throws OXException;

    public Object telephonettyttd(Object... objects) throws OXException;

    public Object instantmessenger1(Object... objects) throws OXException;

    public Object instantmessenger2(Object... objects) throws OXException;

    public Object telephoneip(Object... objects) throws OXException;

    public Object email1(Object... objects) throws OXException;

    public Object email2(Object... objects) throws OXException;

    public Object email3(Object... objects) throws OXException;

    public Object url(Object... objects) throws OXException;

    public Object categories(Object... objects) throws OXException;

    public Object userfield01(Object... objects) throws OXException;

    public Object userfield02(Object... objects) throws OXException;

    public Object userfield03(Object... objects) throws OXException;

    public Object userfield04(Object... objects) throws OXException;

    public Object userfield05(Object... objects) throws OXException;

    public Object userfield06(Object... objects) throws OXException;

    public Object userfield07(Object... objects) throws OXException;

    public Object userfield08(Object... objects) throws OXException;

    public Object userfield09(Object... objects) throws OXException;

    public Object userfield10(Object... objects) throws OXException;

    public Object userfield11(Object... objects) throws OXException;

    public Object userfield12(Object... objects) throws OXException;

    public Object userfield13(Object... objects) throws OXException;

    public Object userfield14(Object... objects) throws OXException;

    public Object userfield15(Object... objects) throws OXException;

    public Object userfield16(Object... objects) throws OXException;

    public Object userfield17(Object... objects) throws OXException;

    public Object userfield18(Object... objects) throws OXException;

    public Object userfield19(Object... objects) throws OXException;

    public Object userfield20(Object... objects) throws OXException;

    public Object objectid(Object... objects) throws OXException;

    public Object numberofdistributionlists(Object... objects) throws OXException;

    public Object distributionlist(Object... objects) throws OXException;

    public Object parentfolderid(Object... objects) throws OXException;

    public Object contextid(Object... objects) throws OXException;

    public Object privateflag(Object... objects) throws OXException;

    public Object createdby(Object... objects) throws OXException;

    public Object modifiedby(Object... objects) throws OXException;

    public Object creationdate(Object... objects) throws OXException;

    public Object lastmodified(Object... objects) throws OXException;

    public Object birthday(Object... objects) throws OXException;

    public Object anniversary(Object... objects) throws OXException;

    public Object imagelastmodified(Object... objects) throws OXException;

    public Object internaluserid(Object... objects) throws OXException;

    public Object label(Object... objects) throws OXException;

    public Object fileas(Object... objects) throws OXException;

    public Object defaultaddress(Object... objects) throws OXException;

    public Object numberofattachments(Object... objects) throws OXException;

    public Object numberofimages(Object... objects) throws OXException;

    public Object lastmodifiedofnewestattachment(Object... objects) throws OXException;

    public Object usecount(Object... objects) throws OXException;

    public Object markasdistributionlist(Object[] objects) throws OXException;

    public Object yomifirstname(Object[] objects) throws OXException;

    public Object yomilastname(Object[] objects) throws OXException;

    public Object yomicompanyname(Object[] objects) throws OXException;

    public Object image1contenttype(Object[] objects) throws OXException;

    public Object homeaddress(Object[] objects) throws OXException;

    public Object businessaddress(Object[] objects) throws OXException;

    public Object otheraddress(Object[] objects) throws OXException;

    public Object uid(Object[] objects) throws OXException;

    public Object image1(Object[] objects) throws OXException;

    public boolean _unknownfield(Contact contact, String fieldname, Object value, Object... additionalObjects) throws OXException;

}
