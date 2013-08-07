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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.Order;

public class ContactComparator implements Comparator<Contact> {

    private final AlphanumComparator alphanumComparator = new AlphanumComparator();

    private final int orderfield;
    private final Order order;

    public ContactComparator(final int orderfield, Order order) {
        super();
        this.orderfield = orderfield;
        this.order = order;
    }

    @Override
    public int compare(Contact o1, Contact o2) {
        int retval;
        switch (this.orderfield) {
        case -1:
            // Default sort
            final String s1 = getFirstNotNull(o1);
            final String s2 = getFirstNotNull(o2);
            retval = alphanumComparator.compare(s1, s2);
            break;
        case Contact.ANNIVERSARY:
            retval = compareDate(o1.getAnniversary(), o2.getAnniversary());
            break;
        case Contact.ASSISTANT_NAME:
            retval = compareString(o1.getAssistantName(), o2.getAssistantName());
            break;
        case Contact.BIRTHDAY:
            retval = compareDate(o1.getBirthday(), o2.getBirthday());
            break;
        case Contact.BRANCHES:
            retval = compareString(o1.getBranches(), o2.getBranches());
            break;
        case Contact.BUSINESS_CATEGORY:
            retval = compareString(o1.getBusinessCategory(), o2.getBusinessCategory());
            break;
        case Contact.CELLULAR_TELEPHONE1:
            retval = compareString(o1.getCellularTelephone1(), o2.getCellularTelephone1());
            break;
        case Contact.CELLULAR_TELEPHONE2:
            retval = compareString(o1.getCellularTelephone2(), o2.getCellularTelephone2());
            break;
        case Contact.CITY_BUSINESS:
            retval = compareString(o1.getCityBusiness(), o2.getCityBusiness());
            break;
        case Contact.CITY_HOME:
            retval = compareString(o1.getCityHome(), o2.getCityHome());
            break;
        case Contact.CITY_OTHER:
            retval = compareString(o1.getCityOther(), o2.getCityOther());
            break;
        case Contact.COMMERCIAL_REGISTER:
            retval = compareString(o1.getCommercialRegister(), o2.getCommercialRegister());
            break;
        case Contact.COMPANY:
            retval = compareString(o1.getCompany(), o2.getCompany());
            break;
        case Contact.CONTEXTID:
            retval = intcompare(o1.getContextId(), o2.getContextId());
            break;
        case Contact.COUNTRY_BUSINESS:
            retval = compareString(o1.getCountryBusiness(), o2.getCountryBusiness());
            break;
        case Contact.COUNTRY_HOME:
            retval = compareString(o1.getCountryHome(), o2.getCountryHome());
            break;
        case Contact.COUNTRY_OTHER:
            retval = compareString(o1.getCountryOther(), o2.getCountryOther());
            break;
        case Contact.DEFAULT_ADDRESS:
            retval = intcompare(o1.getDefaultAddress(), o2.getDefaultAddress());
            break;
        case Contact.DEPARTMENT:
            retval = compareString(o1.getDepartment(), o2.getDepartment());
            break;
        case Contact.DISPLAY_NAME:
            retval = compareString(o1.getDisplayName(), o2.getDisplayName());
            break;
        case Contact.EMAIL1:
            retval = compareString(o1.getEmail1(), o2.getEmail1());
            break;
        case Contact.EMAIL2:
            retval = compareString(o1.getEmail2(), o2.getEmail2());
            break;
        case Contact.EMAIL3:
            retval = compareString(o1.getEmail3(), o2.getEmail3());
            break;
        case Contact.EMPLOYEE_TYPE:
            retval = compareString(o1.getEmployeeType(), o2.getEmployeeType());
            break;
        case Contact.FAX_BUSINESS:
            retval = compareString(o1.getFaxBusiness(), o2.getFaxBusiness());
            break;
        case Contact.FAX_HOME:
            retval = compareString(o1.getFaxHome(), o2.getFaxHome());
            break;
        case Contact.FAX_OTHER:
            retval = compareString(o1.getFaxOther(), o2.getFaxOther());
            break;
        case Contact.FILE_AS:
            retval = compareString(o1.getFileAs(), o2.getFileAs());
            break;
        case Contact.GIVEN_NAME:
            retval = compareString(o1.getGivenName(), o2.getGivenName());
            break;
        case Contact.INFO:
            retval = compareString(o1.getInfo(), o2.getInfo());
            break;
        case Contact.INSTANT_MESSENGER1:
            retval = compareString(o1.getInstantMessenger1(), o2.getInstantMessenger1());
            break;
        case Contact.INSTANT_MESSENGER2:
            retval = compareString(o1.getInstantMessenger2(), o2.getInstantMessenger2());
            break;
        case Contact.INTERNAL_USERID:
            retval = intcompare(o1.getInternalUserId(), o2.getInternalUserId());
            break;
        case Contact.MANAGER_NAME:
            retval = compareString(o1.getManagerName(), o2.getManagerName());
            break;
        case Contact.MARITAL_STATUS:
            retval = compareString(o1.getMaritalStatus(), o2.getMaritalStatus());
            break;
        case Contact.MIDDLE_NAME:
            retval = compareString(o1.getMiddleName(), o2.getMiddleName());
            break;
        case Contact.NICKNAME:
            retval = compareString(o1.getNickname(), o2.getNickname());
            break;
        case Contact.NOTE:
            retval = compareString(o1.getNote(), o2.getNote());
            break;
        case Contact.NUMBER_OF_CHILDREN:
            retval = compareString(o1.getNumberOfChildren(), o2.getNumberOfChildren());
            break;
        case Contact.NUMBER_OF_EMPLOYEE:
            retval = compareString(o1.getNumberOfEmployee(), o2.getNumberOfEmployee());
            break;
        case Contact.POSITION:
            retval = compareString(o1.getPosition(), o2.getPosition());
            break;
        case Contact.POSTAL_CODE_BUSINESS:
            retval = compareString(o1.getPostalCodeBusiness(), o2.getPostalCodeBusiness());
            break;
        case Contact.POSTAL_CODE_HOME:
            retval = compareString(o1.getPostalCodeHome(), o2.getPostalCodeHome());
            break;
        case Contact.POSTAL_CODE_OTHER:
            retval = compareString(o1.getPostalCodeOther(), o2.getPostalCodeOther());
            break;
        case Contact.PROFESSION:
            retval = compareString(o1.getProfession(), o2.getProfession());
            break;
        case Contact.ROOM_NUMBER:
            retval = compareString(o1.getRoomNumber(), o2.getRoomNumber());
            break;
        case Contact.SALES_VOLUME:
            retval = compareString(o1.getSalesVolume(), o2.getSalesVolume());
            break;
        case Contact.SPOUSE_NAME:
            retval = compareString(o1.getSpouseName(), o2.getSpouseName());
            break;
        case Contact.STATE_BUSINESS:
            retval = compareString(o1.getStateBusiness(), o2.getStateBusiness());
            break;
        case Contact.STATE_HOME:
            retval = compareString(o1.getStateHome(), o2.getStateHome());
            break;
        case Contact.STATE_OTHER:
            retval = compareString(o1.getStateOther(), o2.getStateOther());
            break;
        case Contact.SUFFIX:
            retval = compareString(o1.getSuffix(), o2.getSuffix());
            break;
        case Contact.SUR_NAME:
            retval = compareString(o1.getSurName(), o2.getSurName());
            break;
        case Contact.TAX_ID:
            retval = compareString(o1.getTaxID(), o2.getTaxID());
            break;
        case Contact.TELEPHONE_ASSISTANT:
            retval = compareString(o1.getTelephoneAssistant(), o2.getTelephoneAssistant());
            break;
        case Contact.TELEPHONE_BUSINESS1:
            retval = compareString(o1.getTelephoneBusiness1(), o2.getTelephoneBusiness1());
            break;
        case Contact.TELEPHONE_BUSINESS2:
            retval = compareString(o1.getTelephoneBusiness2(), o2.getTelephoneBusiness2());
            break;
        case Contact.TELEPHONE_CALLBACK:
            retval = compareString(o1.getTelephoneCallback(), o2.getTelephoneCallback());
            break;
        case Contact.TELEPHONE_CAR:
            retval = compareString(o1.getTelephoneCar(), o2.getTelephoneCar());
            break;
        case Contact.TELEPHONE_COMPANY:
            retval = compareString(o1.getTelephoneCompany(), o2.getTelephoneCompany());
            break;
        case Contact.TELEPHONE_HOME1:
            retval = compareString(o1.getTelephoneHome1(), o2.getTelephoneHome1());
            break;
        case Contact.TELEPHONE_HOME2:
            retval = compareString(o1.getTelephoneHome2(), o2.getTelephoneHome2());
            break;
        case Contact.TELEPHONE_IP:
            retval = compareString(o1.getTelephoneIP(), o2.getTelephoneIP());
            break;
        case Contact.TELEPHONE_ISDN:
            retval = compareString(o1.getTelephoneISDN(), o2.getTelephoneISDN());
            break;
        case Contact.TELEPHONE_OTHER:
            retval = compareString(o1.getTelephoneOther(), o2.getTelephoneOther());
            break;
        case Contact.TELEPHONE_PAGER:
            retval = compareString(o1.getTelephonePager(), o2.getTelephonePager());
            break;
        case Contact.TELEPHONE_PRIMARY:
            retval = compareString(o1.getTelephonePrimary(), o2.getTelephonePrimary());
            break;
        case Contact.TELEPHONE_RADIO:
            retval = compareString(o1.getTelephoneRadio(), o2.getTelephoneRadio());
            break;
        case Contact.TELEPHONE_TELEX:
            retval = compareString(o1.getTelephoneTelex(), o2.getTelephoneTelex());
            break;
        case Contact.TELEPHONE_TTYTDD:
            retval = compareString(o1.getTelephoneTTYTTD(), o2.getTelephoneTTYTTD());
            break;
        case Contact.TITLE:
            retval = compareString(o1.getTitle(), o2.getTitle());
            break;
        case Contact.URL:
            retval = compareString(o1.getURL(), o2.getURL());
            break;
        case Contact.USERFIELD01:
            retval = compareString(o1.getUserField01(), o2.getUserField01());
            break;
        case Contact.USERFIELD02:
            retval = compareString(o1.getUserField02(), o2.getUserField02());
            break;
        case Contact.USERFIELD03:
            retval = compareString(o1.getUserField03(), o2.getUserField03());
            break;
        case Contact.USERFIELD04:
            retval = compareString(o1.getUserField04(), o2.getUserField04());
            break;
        case Contact.USERFIELD05:
            retval = compareString(o1.getUserField05(), o2.getUserField05());
            break;
        case Contact.USERFIELD06:
            retval = compareString(o1.getUserField06(), o2.getUserField06());
            break;
        case Contact.USERFIELD07:
            retval = compareString(o1.getUserField07(), o2.getUserField07());
            break;
        case Contact.USERFIELD08:
            retval = compareString(o1.getUserField08(), o2.getUserField08());
            break;
        case Contact.USERFIELD09:
            retval = compareString(o1.getUserField09(), o2.getUserField09());
            break;
        case Contact.USERFIELD10:
            retval = compareString(o1.getUserField10(), o2.getUserField10());
            break;
        case Contact.USERFIELD11:
            retval = compareString(o1.getUserField11(), o2.getUserField11());
            break;
        case Contact.USERFIELD12:
            retval = compareString(o1.getUserField12(), o2.getUserField12());
            break;
        case Contact.USERFIELD13:
            retval = compareString(o1.getUserField13(), o2.getUserField13());
            break;
        case Contact.USERFIELD14:
            retval = compareString(o1.getUserField14(), o2.getUserField14());
            break;
        case Contact.USERFIELD15:
            retval = compareString(o1.getUserField15(), o2.getUserField15());
            break;
        case Contact.USERFIELD16:
            retval = compareString(o1.getUserField16(), o2.getUserField16());
            break;
        case Contact.USERFIELD17:
            retval = compareString(o1.getUserField17(), o2.getUserField17());
            break;
        case Contact.USERFIELD18:
            retval = compareString(o1.getUserField18(), o2.getUserField18());
            break;
        case Contact.USERFIELD19:
            retval = compareString(o1.getUserField19(), o2.getUserField19());
            break;
        case Contact.USERFIELD20:
            retval = compareString(o1.getUserField20(), o2.getUserField20());
            break;
        case Contact.FOLDER_ID:
            retval = intcompare(o1.getParentFolderID(), o2.getParentFolderID());
            break;
        default:
            throw new UnsupportedOperationException("Unknown sort column value " + this.orderfield);
        }
        switch (order) {
        case ASCENDING:
            break;
        case DESCENDING:
            retval = -retval;
            break;
        case NO_ORDER:
            retval = 0;
            break;
        }
        return retval;
    }

    private int compareString(final String string1, final String string2) {
        if (null == string1) {
            if (null == string2) {
                return 0;
            }
            return 1;
        }
        if (null == string2) {
            return -1;
        }
        return this.alphanumComparator.compare(string1, string2);
    }

    private int compareDate(final Date date1, final Date date2) {
        if (null == date1) {
            if (null == date2) {
                return 0;
            }
            return 1;
        }
        if (null == date2) {
            return -1;
        }
        return date1.compareTo(date2);
    }

    private int intcompare(final int o1, final int o2) {
        return o1 - o2;
    }

    private String getFirstNotNull(final Contact contact) {
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
