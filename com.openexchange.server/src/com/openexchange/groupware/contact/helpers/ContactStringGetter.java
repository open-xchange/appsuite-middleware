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

import java.text.SimpleDateFormat;
import java.util.Date;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.tools.TimeZoneUtils;

/**
 * Makes strings out of given objects
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */
public class ContactStringGetter implements ContactSwitcher {
	private ContactSwitcher delegate;

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	static {
	    DATE_FORMAT.setTimeZone(TimeZoneUtils.getTimeZone("UTC"));
	}

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

	public static final String stringifyDate(final Date date) {
	    if(date == null) {
	        return "";
	    }
	    synchronized (DATE_FORMAT) {
            return DATE_FORMAT.format(date);
        }
	}

    public static final String stringifyTimestamp(final Date date) {
        if (date == null) {
            return "";
        }
        return Long.toString(date.getTime());
    }

    public static String stringifyDistributionList(DistributionListEntryObject[] distList) {
        if (null == distList || 0 == distList.length) {
            return "";
        }
        StringBuilder allocator = new StringBuilder();
        for (DistributionListEntryObject member : distList) {
            if (null != member) {
                if (null != member.getEmailaddress()) {
                    allocator.append(member.getEmailaddress().replaceAll(";", "/;"));
                }
                allocator.append(';');
                if (null != member.getDisplayname()) {
                    allocator.append(member.getDisplayname().replaceAll(";", "/;"));
                }
                allocator.append(';');
            }
        }
        return allocator.toString();
    }

	/* DELEGATE */
	public ContactSwitcher getDelegate() {
		return delegate;
	}

	public void setDelegate(final ContactSwitcher delegate) {
		this.delegate = delegate;
	}

	/* INTERFACE */
	@Override
    public Object anniversary(final Object... objects) throws OXException {
		return stringifyDate((Date)delegate.anniversary(objects));
	}

	@Override
    public Object assistantname(final Object... objects) throws OXException {
		return stringify(delegate.assistantname(objects));
	}

	@Override
    public Object birthday(final Object... objects) throws OXException {
		return stringifyDate((Date) delegate.birthday(objects));
	}

	@Override
    public Object branches(final Object... objects) throws OXException {
		return stringify(delegate.branches(objects));
	}

	@Override
    public Object businesscategory(final Object... objects) throws OXException {
		return stringify(delegate.businesscategory(objects));
	}

	@Override
    public Object categories(final Object... objects) throws OXException {
		return stringify(delegate.categories(objects));
	}

	@Override
    public Object cellulartelephone1(final Object... objects) throws OXException {
		return stringify(delegate.cellulartelephone1(objects));
	}

	@Override
    public Object cellulartelephone2(final Object... objects) throws OXException {
		return stringify(delegate.cellulartelephone2(objects));
	}

	@Override
    public Object citybusiness(final Object... objects) throws OXException {
		return stringify(delegate.citybusiness(objects));
	}

	@Override
    public Object cityhome(final Object... objects) throws OXException {
		return stringify(delegate.cityhome(objects));
	}

	@Override
    public Object cityother(final Object... objects) throws OXException {
		return stringify(delegate.cityother(objects));
	}

	@Override
    public Object commercialregister(final Object... objects) throws OXException {
		return stringify(delegate.commercialregister(objects));
	}

	@Override
    public Object company(final Object... objects) throws OXException {
		return stringify(delegate.company(objects));
	}

	@Override
    public Object contextid(final Object... objects) throws OXException {
		return stringify(delegate.contextid(objects));
	}

	@Override
    public Object countrybusiness(final Object... objects) throws OXException {
		return stringify(delegate.countrybusiness(objects));
	}

	@Override
    public Object countryhome(final Object... objects) throws OXException {
		return stringify(delegate.countryhome(objects));
	}

	@Override
    public Object countryother(final Object... objects) throws OXException {
		return stringify(delegate.countryother(objects));
	}

	@Override
    public Object createdby(final Object... objects) throws OXException {
		return stringify(delegate.createdby(objects));
	}

	@Override
    public Object creationdate(final Object... objects) throws OXException {
		return stringifyTimestamp((Date)delegate.creationdate(objects));
	}

	@Override
    public Object defaultaddress(final Object... objects) throws OXException {
		return stringify(delegate.defaultaddress(objects));
	}

	@Override
    public Object department(final Object... objects) throws OXException {
		return stringify(delegate.department(objects));
	}

	@Override
    public Object displayname(final Object... objects) throws OXException {
		return stringify(delegate.displayname(objects));
	}

	@Override
    public Object distributionlist(final Object... objects) throws OXException {
		return stringifyDistributionList((DistributionListEntryObject[]) delegate.distributionlist(objects));
	}

	@Override
    public Object email1(final Object... objects) throws OXException {
		return stringify(delegate.email1(objects));
	}

	@Override
    public Object email2(final Object... objects) throws OXException {
		return stringify(delegate.email2(objects));
	}

	@Override
    public Object email3(final Object... objects) throws OXException {
		return stringify(delegate.email3(objects));
	}

	@Override
    public Object employeetype(final Object... objects) throws OXException {
		return stringify(delegate.employeetype(objects));
	}

	@Override
    public Object faxbusiness(final Object... objects) throws OXException {
		return stringify(delegate.faxbusiness(objects));
	}

	@Override
    public Object faxhome(final Object... objects) throws OXException {
		return stringify(delegate.faxhome(objects));
	}

	@Override
    public Object faxother(final Object... objects) throws OXException {
		return stringify(delegate.faxother(objects));
	}

	@Override
    public Object fileas(final Object... objects) throws OXException {
		return stringify(delegate.fileas(objects));
	}

	@Override
    public Object givenname(final Object... objects) throws OXException {
		return stringify(delegate.givenname(objects));
	}

	@Override
    public Object imagelastmodified(final Object... objects) throws OXException {
		return stringify(delegate.imagelastmodified(objects));
	}

	@Override
    public Object info(final Object... objects) throws OXException {
		return stringify(delegate.info(objects));
	}

	@Override
    public Object instantmessenger1(final Object... objects) throws OXException {
		return stringify(delegate.instantmessenger1(objects));
	}

	@Override
    public Object instantmessenger2(final Object... objects) throws OXException {
		return stringify(delegate.instantmessenger2(objects));
	}

	@Override
    public Object internaluserid(final Object... objects) throws OXException {
		return stringify(delegate.internaluserid(objects));
	}

	@Override
    public Object label(final Object... objects) throws OXException {
		return stringify(delegate.label(objects));
	}

	@Override
    public Object lastmodified(final Object... objects) throws OXException {
		return stringifyTimestamp((Date)delegate.lastmodified(objects));
	}

	@Override
    public Object managername(final Object... objects) throws OXException {
		return stringify(delegate.managername(objects));
	}

	@Override
    public Object maritalstatus(final Object... objects) throws OXException {
		return stringify(delegate.maritalstatus(objects));
	}

	@Override
    public Object middlename(final Object... objects) throws OXException {
		return stringify(delegate.middlename(objects));
	}

	@Override
    public Object modifiedby(final Object... objects) throws OXException {
		return stringify(delegate.modifiedby(objects));
	}

	@Override
    public Object nickname(final Object... objects) throws OXException {
		return stringify(delegate.nickname(objects));
	}

	@Override
    public Object note(final Object... objects) throws OXException {
		return stringify(delegate.note(objects));
	}

	@Override
    public Object numberofattachments(final Object... objects) throws OXException {
		return stringify(delegate.numberofattachments(objects));
	}

	@Override
    public Object numberofchildren(final Object... objects) throws OXException {
		return stringify(delegate.numberofchildren(objects));
	}

	@Override
    public Object numberofdistributionlists(final Object... objects) throws OXException {
		return stringify(delegate.numberofdistributionlists(objects));
	}

	@Override
    public Object numberofemployee(final Object... objects) throws OXException {
		return stringify(delegate.numberofemployee(objects));
	}

	@Override
    public Object objectid(final Object... objects) throws OXException {
		return stringify(delegate.objectid(objects));
	}

	@Override
    public Object parentfolderid(final Object... objects) throws OXException {
		return stringify(delegate.parentfolderid(objects));
	}

	@Override
    public Object position(final Object... objects) throws OXException {
		return stringify(delegate.position(objects));
	}

	@Override
    public Object postalcodebusiness(final Object... objects) throws OXException {
		return stringify(delegate.postalcodebusiness(objects));
	}

	@Override
    public Object postalcodehome(final Object... objects) throws OXException {
		return stringify(delegate.postalcodehome(objects));
	}

	@Override
    public Object postalcodeother(final Object... objects) throws OXException {
		return stringify(delegate.postalcodeother(objects));
	}

	@Override
    public Object privateflag(final Object... objects) throws OXException {
		return stringify(delegate.privateflag(objects));
	}

	@Override
    public Object profession(final Object... objects) throws OXException {
		return stringify(delegate.profession(objects));
	}

	@Override
    public Object roomnumber(final Object... objects) throws OXException {
		return stringify(delegate.roomnumber(objects));
	}

	@Override
    public Object salesvolume(final Object... objects) throws OXException {
		return stringify(delegate.salesvolume(objects));
	}

	@Override
    public Object spousename(final Object... objects) throws OXException {
		return stringify(delegate.spousename(objects));
	}

	@Override
    public Object statebusiness(final Object... objects) throws OXException {
		return stringify(delegate.statebusiness(objects));
	}

	@Override
    public Object statehome(final Object... objects) throws OXException {
		return stringify(delegate.statehome(objects));
	}

	@Override
    public Object stateother(final Object... objects) throws OXException {
		return stringify(delegate.stateother(objects));
	}

	@Override
    public Object streetbusiness(final Object... objects) throws OXException {
		return stringify(delegate.streetbusiness(objects));
	}

	@Override
    public Object streethome(final Object... objects) throws OXException {
		return stringify(delegate.streethome(objects));
	}

	@Override
    public Object streetother(final Object... objects) throws OXException {
		return stringify(delegate.streetother(objects));
	}

	@Override
    public Object suffix(final Object... objects) throws OXException {
		return stringify(delegate.suffix(objects));
	}

	@Override
    public Object surname(final Object... objects) throws OXException {
		return stringify(delegate.surname(objects));
	}

	@Override
    public Object taxid(final Object... objects) throws OXException {
		return stringify(delegate.taxid(objects));
	}

	@Override
    public Object telephoneassistant(final Object... objects) throws OXException {
		return stringify(delegate.telephoneassistant(objects));
	}

	@Override
    public Object telephonebusiness1(final Object... objects) throws OXException {
		return stringify(delegate.telephonebusiness1(objects));
	}

	@Override
    public Object telephonebusiness2(final Object... objects) throws OXException {
		return stringify(delegate.telephonebusiness2(objects));
	}

	@Override
    public Object telephonecallback(final Object... objects) throws OXException {
		return stringify(delegate.telephonecallback(objects));
	}

	@Override
    public Object telephonecar(final Object... objects) throws OXException {
		return stringify(delegate.telephonecar(objects));
	}

	@Override
    public Object telephonecompany(final Object... objects) throws OXException {
		return stringify(delegate.telephonecompany(objects));
	}

	@Override
    public Object telephonehome1(final Object... objects) throws OXException {
		return stringify(delegate.telephonehome1(objects));
	}

	@Override
    public Object telephonehome2(final Object... objects) throws OXException {
		return stringify(delegate.telephonehome2(objects));
	}

	@Override
    public Object telephoneip(final Object... objects) throws OXException {
		return stringify(delegate.telephoneip(objects));
	}

	@Override
    public Object telephoneisdn(final Object... objects) throws OXException {
		return stringify(delegate.telephoneisdn(objects));
	}

	@Override
    public Object telephoneother(final Object... objects) throws OXException {
		return stringify(delegate.telephoneother(objects));
	}

	@Override
    public Object telephonepager(final Object... objects) throws OXException {
		return stringify(delegate.telephonepager(objects));
	}

	@Override
    public Object telephoneprimary(final Object... objects) throws OXException {
		return stringify(delegate.telephoneprimary(objects));
	}

	@Override
    public Object telephoneradio(final Object... objects) throws OXException {
		return stringify(delegate.telephoneradio(objects));
	}

	@Override
    public Object telephonetelex(final Object... objects) throws OXException {
		return stringify(delegate.telephonetelex(objects));
	}

	@Override
    public Object telephonettyttd(final Object... objects) throws OXException {
		return stringify(delegate.telephonettyttd(objects));
	}

	@Override
    public Object title(final Object... objects) throws OXException {
		return stringify(delegate.title(objects));
	}

	@Override
    public Object url(final Object... objects) throws OXException {
		return stringify(delegate.url(objects));
	}

	@Override
    public Object userfield01(final Object... objects) throws OXException {
		return stringify(delegate.userfield01(objects));
	}

	@Override
    public Object userfield02(final Object... objects) throws OXException {
		return stringify(delegate.userfield02(objects));
	}

	@Override
    public Object userfield03(final Object... objects) throws OXException {
		return stringify(delegate.userfield03(objects));
	}

	@Override
    public Object userfield04(final Object... objects) throws OXException {
		return stringify(delegate.userfield04(objects));
	}

	@Override
    public Object userfield05(final Object... objects) throws OXException {
		return stringify(delegate.userfield05(objects));
	}

	@Override
    public Object userfield06(final Object... objects) throws OXException {
		return stringify(delegate.userfield06(objects));
	}

	@Override
    public Object userfield07(final Object... objects) throws OXException {
		return stringify(delegate.userfield07(objects));
	}

	@Override
    public Object userfield08(final Object... objects) throws OXException {
		return stringify(delegate.userfield08(objects));
	}

	@Override
    public Object userfield09(final Object... objects) throws OXException {
		return stringify(delegate.userfield09(objects));
	}

	@Override
    public Object userfield10(final Object... objects) throws OXException {
		return stringify(delegate.userfield10(objects));
	}

	@Override
    public Object userfield11(final Object... objects) throws OXException {
		return stringify(delegate.userfield11(objects));
	}

	@Override
    public Object userfield12(final Object... objects) throws OXException {
		return stringify(delegate.userfield12(objects));
	}

	@Override
    public Object userfield13(final Object... objects) throws OXException {
		return stringify(delegate.userfield13(objects));
	}

	@Override
    public Object userfield14(final Object... objects) throws OXException {
		return stringify(delegate.userfield14(objects));
	}

	@Override
    public Object userfield15(final Object... objects) throws OXException {
		return stringify(delegate.userfield15(objects));
	}

	@Override
    public Object userfield16(final Object... objects) throws OXException {
		return stringify(delegate.userfield16(objects));
	}

	@Override
    public Object userfield17(final Object... objects) throws OXException {
		return stringify(delegate.userfield17(objects));
	}

	@Override
    public Object userfield18(final Object... objects) throws OXException {
		return stringify(delegate.userfield18(objects));
	}

	@Override
    public Object userfield19(final Object... objects) throws OXException {
		return stringify(delegate.userfield19(objects));
	}

	@Override
    public Object userfield20(final Object... objects) throws OXException {
		return stringify(delegate.userfield20(objects));
	}

    @Override
    public Object numberofimages(final Object... objects) throws OXException {
        return stringify(delegate.numberofimages(objects));
    }

    @Override
    public Object lastmodifiedofnewestattachment(final Object... objects) throws OXException {
        return stringifyTimestamp((Date)delegate.lastmodifiedofnewestattachment(objects));
    }

    @Override
    public Object usecount(final Object... objects) throws OXException {
        return stringify(delegate.usecount(objects));
    }

    @Override
    public Object markasdistributionlist(final Object[] objects) throws OXException {
        return stringify(delegate.markasdistributionlist(objects));
    }

    @Override
    public Object yomifirstname(final Object[] objects) throws OXException {
        return stringify(delegate.yomifirstname(objects));
    }

    @Override
    public Object yomilastname(final Object[] objects) throws OXException {
        return stringify(delegate.yomilastname(objects));
    }

    @Override
    public Object yomicompanyname(final Object[] objects) throws OXException {
        return stringify(delegate.yomicompanyname(objects));
    }

    @Override
    public Object image1contenttype(final Object[] objects) throws OXException {
        return stringify(delegate.image1contenttype(objects));
    }

    @Override
    public Object homeaddress(final Object[] objects) throws OXException {
        return stringify(delegate.homeaddress(objects));
    }

    @Override
    public Object businessaddress(final Object[] objects) throws OXException {
        return stringify(delegate.businessaddress(objects));
    }

    @Override
    public Object otheraddress(final Object[] objects) throws OXException {
        return stringify(delegate.otheraddress(objects));
    }

    @Override
    public Object uid(final Object[] objects) throws OXException {
        return stringify(delegate.uid(objects));
    }

    @Override
    public Object image1(final Object[] objects) throws OXException {
        return stringify(delegate.image1(objects));
    }

    @Override
    public boolean _unknownfield(final Contact contact, final String fieldname, final Object value, final Object... additionalObjects) throws OXException {
        return delegate._unknownfield(contact, fieldname, value, additionalObjects);
    }
}
