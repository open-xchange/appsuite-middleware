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

package com.openexchange.contacts.ldap.contacts;

import java.util.Comparator;
import java.util.Date;

import com.davekoelle.AlphanumComparator;
import com.openexchange.groupware.container.ContactObject;

public class ContactComparator implements Comparator<ContactObject> {

    private final int orderfield;
    
    private final AlphanumComparator alphanumComparator = new AlphanumComparator();

    public ContactComparator(final int orderfield) {
        this.orderfield = orderfield;
    }

    public int compare(ContactObject o1, ContactObject o2) {
        switch (this.orderfield) {
        case -1:
            // Default sort
            final String s1 = getFirstNotNull(o1);
            final String s2 = getFirstNotNull(o2);
            return alphanumComparator.compare(s1, s2);
        case ContactObject.ANNIVERSARY:
            return compareDate(o1.getAnniversary(), o2.getAnniversary());
        case ContactObject.ASSISTANT_NAME:
            return compareString(o1.getAssistantName(), o2.getAssistantName());
        case ContactObject.BIRTHDAY:
            return compareDate(o1.getBirthday(), o2.getBirthday());
        case ContactObject.BRANCHES:
            return compareString(o1.getBranches(), o2.getBranches());
        case ContactObject.BUSINESS_CATEGORY:
            return compareString(o1.getBusinessCategory(), o2.getBusinessCategory());
        case ContactObject.CELLULAR_TELEPHONE1:
            return compareString(o1.getCellularTelephone1(), o2.getCellularTelephone1());
        case ContactObject.CELLULAR_TELEPHONE2:
            return compareString(o1.getCellularTelephone2(), o2.getCellularTelephone2());
        case ContactObject.CITY_BUSINESS:
            return compareString(o1.getCityBusiness(), o2.getCityBusiness());
        case ContactObject.CITY_HOME:
            return compareString(o1.getCityHome(), o2.getCityHome());
        case ContactObject.CITY_OTHER:
            return compareString(o1.getCityOther(), o2.getCityOther());
        case ContactObject.COMMERCIAL_REGISTER:
            return compareString(o1.getCommercialRegister(), o2.getCommercialRegister());
        case ContactObject.COMPANY:
            return compareString(o1.getCompany(), o2.getCompany());
        case ContactObject.CONTEXTID:
            return intcompare(o1.getContextId(), o2.getContextId());
        case ContactObject.COUNTRY_BUSINESS:
            return compareString(o1.getCountryBusiness(), o2.getCountryBusiness());
        case ContactObject.COUNTRY_HOME:
            return compareString(o1.getCountryHome(), o2.getCountryHome());
        case ContactObject.COUNTRY_OTHER:
            return compareString(o1.getCountryOther(), o2.getCountryOther());
        case ContactObject.DEFAULT_ADDRESS:
            return intcompare(o1.getDefaultAddress(), o2.getDefaultAddress());
        case ContactObject.DEPARTMENT:
            return compareString(o1.getDepartment(), o2.getDepartment());
        case ContactObject.DISPLAY_NAME:
            return compareString(o1.getDisplayName(), o2.getDisplayName());
        case ContactObject.EMAIL1:
            return compareString(o1.getEmail1(), o2.getEmail1());
        case ContactObject.EMAIL2:
            return compareString(o1.getEmail2(), o2.getEmail2());
        case ContactObject.EMAIL3:
            return compareString(o1.getEmail3(), o2.getEmail3());
        case ContactObject.EMPLOYEE_TYPE:
            return compareString(o1.getEmployeeType(), o2.getEmployeeType());
        case ContactObject.FAX_BUSINESS:
            return compareString(o1.getFaxBusiness(), o2.getFaxBusiness());
        case ContactObject.FAX_HOME:
            return compareString(o1.getFaxHome(), o2.getFaxHome());
        case ContactObject.FAX_OTHER:
            return compareString(o1.getFaxOther(), o2.getFaxOther());
        case ContactObject.FILE_AS:
            return compareString(o1.getFileAs(), o2.getFileAs());
        case ContactObject.GIVEN_NAME:
            return compareString(o1.getGivenName(), o2.getGivenName());
        case ContactObject.INFO:
            return compareString(o1.getInfo(), o2.getInfo());
        case ContactObject.INSTANT_MESSENGER1:
            return compareString(o1.getInstantMessenger1(), o2.getInstantMessenger1());
        case ContactObject.INSTANT_MESSENGER2:
            return compareString(o1.getInstantMessenger2(), o2.getInstantMessenger2());
        case ContactObject.INTERNAL_USERID:
            return intcompare(o1.getInternalUserId(), o2.getInternalUserId());
        case ContactObject.MANAGER_NAME:
            return compareString(o1.getManagerName(), o2.getManagerName());
        case ContactObject.MARITAL_STATUS:
            return compareString(o1.getMaritalStatus(), o2.getMaritalStatus());
        case ContactObject.MIDDLE_NAME:
            return compareString(o1.getMiddleName(), o2.getMiddleName());
        case ContactObject.NICKNAME:
            return compareString(o1.getNickname(), o2.getNickname());
        case ContactObject.NOTE:
            return compareString(o1.getNote(), o2.getNote());
        case ContactObject.NUMBER_OF_CHILDREN:
            return compareString(o1.getNumberOfChildren(), o2.getNumberOfChildren());
        case ContactObject.NUMBER_OF_EMPLOYEE:
            return compareString(o1.getNumberOfEmployee(), o2.getNumberOfEmployee());
        case ContactObject.POSITION:
            return compareString(o1.getPosition(), o2.getPosition());
        case ContactObject.POSTAL_CODE_BUSINESS:
            return compareString(o1.getPostalCodeBusiness(), o2.getPostalCodeBusiness());
        case ContactObject.POSTAL_CODE_HOME:
            return compareString(o1.getPostalCodeHome(), o2.getPostalCodeHome());
        case ContactObject.POSTAL_CODE_OTHER:
            return compareString(o1.getPostalCodeOther(), o2.getPostalCodeOther());
        case ContactObject.PROFESSION:
            return compareString(o1.getProfession(), o2.getProfession());
        case ContactObject.ROOM_NUMBER:
            return compareString(o1.getRoomNumber(), o2.getRoomNumber());
        case ContactObject.SALES_VOLUME:
            return compareString(o1.getSalesVolume(), o2.getSalesVolume());
        case ContactObject.SPOUSE_NAME:
            return compareString(o1.getSpouseName(), o2.getSpouseName());
        case ContactObject.STATE_BUSINESS:
            return compareString(o1.getStateBusiness(), o2.getStateBusiness());
        case ContactObject.STATE_HOME:
            return compareString(o1.getStateHome(), o2.getStateHome());
        case ContactObject.STATE_OTHER:
            return compareString(o1.getStateOther(), o2.getStateOther());
        case ContactObject.SUFFIX:
            return compareString(o1.getSuffix(), o2.getSuffix());
        case ContactObject.SUR_NAME:
            return compareString(o1.getSurName(), o2.getSurName());
        case ContactObject.TAX_ID:
            return compareString(o1.getTaxID(), o2.getTaxID());
        case ContactObject.TELEPHONE_ASSISTANT:
            return compareString(o1.getTelephoneAssistant(), o2.getTelephoneAssistant());
        case ContactObject.TELEPHONE_BUSINESS1:
            return compareString(o1.getTelephoneBusiness1(), o2.getTelephoneBusiness1());
        case ContactObject.TELEPHONE_BUSINESS2:
            return compareString(o1.getTelephoneBusiness2(), o2.getTelephoneBusiness2());
        case ContactObject.TELEPHONE_CALLBACK:
            return compareString(o1.getTelephoneCallback(), o2.getTelephoneCallback());
        case ContactObject.TELEPHONE_CAR:
            return compareString(o1.getTelephoneCar(), o2.getTelephoneCar());
        case ContactObject.TELEPHONE_COMPANY:
            return compareString(o1.getTelephoneCompany(), o2.getTelephoneCompany());
        case ContactObject.TELEPHONE_HOME1:
            return compareString(o1.getTelephoneHome1(), o2.getTelephoneHome1());
        case ContactObject.TELEPHONE_HOME2:
            return compareString(o1.getTelephoneHome2(), o2.getTelephoneHome2());
        case ContactObject.TELEPHONE_IP:
            return compareString(o1.getTelephoneIP(), o2.getTelephoneIP());
        case ContactObject.TELEPHONE_ISDN:
            return compareString(o1.getTelephoneISDN(), o2.getTelephoneISDN());
        case ContactObject.TELEPHONE_OTHER:
            return compareString(o1.getTelephoneOther(), o2.getTelephoneOther());
        case ContactObject.TELEPHONE_PAGER:
            return compareString(o1.getTelephonePager(), o2.getTelephonePager());
        case ContactObject.TELEPHONE_PRIMARY:
            return compareString(o1.getTelephonePrimary(), o2.getTelephonePrimary());
        case ContactObject.TELEPHONE_RADIO:
            return compareString(o1.getTelephoneRadio(), o2.getTelephoneRadio());
        case ContactObject.TELEPHONE_TELEX:
            return compareString(o1.getTelephoneTelex(), o2.getTelephoneTelex());
        case ContactObject.TELEPHONE_TTYTDD:
            return compareString(o1.getTelephoneTTYTTD(), o2.getTelephoneTTYTTD());
        case ContactObject.TITLE:
            return compareString(o1.getTitle(), o2.getTitle());
        case ContactObject.URL:
            return compareString(o1.getURL(), o2.getURL());
        case ContactObject.USERFIELD01:
            return compareString(o1.getUserField01(), o2.getUserField01());
        case ContactObject.USERFIELD02:
            return compareString(o1.getUserField02(), o2.getUserField02());
        case ContactObject.USERFIELD03:
            return compareString(o1.getUserField03(), o2.getUserField03());
        case ContactObject.USERFIELD04:
            return compareString(o1.getUserField04(), o2.getUserField04());
        case ContactObject.USERFIELD05:
            return compareString(o1.getUserField05(), o2.getUserField05());
        case ContactObject.USERFIELD06:
            return compareString(o1.getUserField06(), o2.getUserField06());
        case ContactObject.USERFIELD07:
            return compareString(o1.getUserField07(), o2.getUserField07());
        case ContactObject.USERFIELD08:
            return compareString(o1.getUserField08(), o2.getUserField08());
        case ContactObject.USERFIELD09:
            return compareString(o1.getUserField09(), o2.getUserField09());
        case ContactObject.USERFIELD10:
            return compareString(o1.getUserField10(), o2.getUserField10());
        case ContactObject.USERFIELD11:
            return compareString(o1.getUserField11(), o2.getUserField11());
        case ContactObject.USERFIELD12:
            return compareString(o1.getUserField12(), o2.getUserField12());
        case ContactObject.USERFIELD13:
            return compareString(o1.getUserField13(), o2.getUserField13());
        case ContactObject.USERFIELD14:
            return compareString(o1.getUserField14(), o2.getUserField14());
        case ContactObject.USERFIELD15:
            return compareString(o1.getUserField15(), o2.getUserField15());
        case ContactObject.USERFIELD16:
            return compareString(o1.getUserField16(), o2.getUserField16());
        case ContactObject.USERFIELD17:
            return compareString(o1.getUserField17(), o2.getUserField17());
        case ContactObject.USERFIELD18:
            return compareString(o1.getUserField18(), o2.getUserField18());
        case ContactObject.USERFIELD19:
            return compareString(o1.getUserField19(), o2.getUserField19());
        case ContactObject.USERFIELD20:
            return compareString(o1.getUserField20(), o2.getUserField20());
        default:
            throw new UnsupportedOperationException("Unknown sort column value " + this.orderfield);
        }
    }

    private int compareString(final String string1, final String string2) {
        if (null == string1) {
            if (null == string2) {
                return 0;
            } else {
                return 1;
            }
        } else {
            return this.alphanumComparator.compare(string1, string2);
        }
    }

    private int compareDate(final Date date1, final Date date2) {
        if (null == date1) {
            if (null == date2) {
                return 0;
            } else {
                return 1;
            }
        } else {
            return date1.compareTo(date2);
        }
    }

    private int intcompare(final int o1, final int o2) {
        return o1 - o2;
    }

    private String getFirstNotNull(final ContactObject contact) {
        final String retval;
        if (contact.containsSurName()) {
            retval = contact.getSurName();
        } else if (contact.containsDisplayName()) {
            retval = contact.getDisplayName();
        } else if (contact.containsCompany()) {
            retval = contact.getCompany();
        } else if (contact.containsEmail1()) {
            retval = contact.getEmail1();
        } else if (contact.containsEmail2()) {
            retval = contact.getEmail2();
        } else {
            retval = "";
        }
        return retval;
    }

}
