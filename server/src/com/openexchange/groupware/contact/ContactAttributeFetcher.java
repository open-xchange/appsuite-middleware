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

package com.openexchange.groupware.contact;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.ajax.fields.ContactFields;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.search.SearchAttributeFetcher;

/**
 * {@link ContactAttributeFetcher}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ContactAttributeFetcher implements SearchAttributeFetcher<ContactObject> {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(AttributeGetter.class);

    private static interface AttributeGetter {

        public Object getObject(ContactObject candidate);
    }

    private static final Map<String, AttributeGetter> GETTERS;

    static {
        final Map<String, AttributeGetter> m = new HashMap<String, AttributeGetter>(25);

        m.put(ContactFields.ANNIVERSARY, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getAnniversary();
            }
        });

        m.put(ContactFields.ASSISTANT_NAME, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getAssistantName();
            }
        });

        m.put(ContactFields.BIRTHDAY, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getBirthday();
            }
        });

        m.put(ContactFields.BRANCHES, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getBranches();
            }
        });

        m.put(ContactFields.BUSINESS_CATEGORY, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getBusinessCategory();
            }
        });

        m.put(ContactFields.CELLULAR_TELEPHONE1, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getCellularTelephone1();
            }
        });

        m.put(ContactFields.CELLULAR_TELEPHONE2, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getCellularTelephone2();
            }
        });

        m.put(ContactFields.CITY_BUSINESS, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getCityBusiness();
            }
        });

        m.put(ContactFields.CITY_HOME, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getCityHome();
            }
        });

        m.put(ContactFields.CITY_OTHER, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getCityOther();
            }
        });

        m.put(ContactFields.COMMERCIAL_REGISTER, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getCommercialRegister();
            }
        });

        m.put(ContactFields.COMPANY, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getCompany();
            }
        });

        m.put(ContactFields.COUNTRY_BUSINESS, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getCountryBusiness();
            }
        });

        m.put(ContactFields.COUNTRY_HOME, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getCountryHome();
            }
        });

        m.put(ContactFields.COUNTRY_OTHER, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getCountryOther();
            }
        });

        m.put(ContactFields.DEPARTMENT, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getDepartment();
            }
        });

        m.put(ContactFields.DISPLAY_NAME, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getDisplayName();
            }
        });

        m.put(ContactFields.DISTRIBUTIONLIST, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getDistributionList();
            }
        });

        m.put(ContactFields.EMAIL1, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getEmail1();
            }
        });

        m.put(ContactFields.EMAIL2, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getEmail2();
            }
        });

        m.put(ContactFields.EMAIL3, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getEmail3();
            }
        });

        m.put(ContactFields.EMPLOYEE_TYPE, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getEmployeeType();
            }
        });

        m.put(ContactFields.FAX_BUSINESS, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getFaxBusiness();
            }
        });

        m.put(ContactFields.FAX_HOME, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getFaxHome();
            }
        });

        m.put(ContactFields.FAX_OTHER, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getFaxOther();
            }
        });

        m.put(ContactFields.FIRST_NAME, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getGivenName();
            }
        });

        m.put(ContactFields.INFO, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getInfo();
            }
        });

        m.put(ContactFields.INSTANT_MESSENGER1, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getInstantMessenger1();
            }
        });

        m.put(ContactFields.INSTANT_MESSENGER2, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getInstantMessenger2();
            }
        });

        m.put(ContactFields.LAST_NAME, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getSurName();
            }
        });

        m.put(ContactFields.LINKS, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getLinks();
            }
        });

        m.put(ContactFields.MANAGER_NAME, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getManagerName();
            }
        });

        m.put(ContactFields.MARITAL_STATUS, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getMaritalStatus();
            }
        });

        m.put(ContactFields.NICKNAME, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getNickname();
            }
        });

        m.put(ContactFields.NOTE, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getNote();
            }
        });

        m.put(ContactFields.NUMBER_OF_CHILDREN, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getNumberOfChildren();
            }
        });

        m.put(ContactFields.NUMBER_OF_EMPLOYEE, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getNumberOfEmployee();
            }
        });

        m.put(ContactFields.POSITION, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getPosition();
            }
        });

        m.put(ContactFields.POSTAL_CODE_BUSINESS, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getPostalCodeBusiness();
            }
        });

        m.put(ContactFields.POSTAL_CODE_HOME, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getPostalCodeHome();
            }
        });

        m.put(ContactFields.POSTAL_CODE_OTHER, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getPostalCodeOther();
            }
        });

        m.put(ContactFields.PROFESSION, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getProfession();
            }
        });

        m.put(ContactFields.ROOM_NUMBER, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getRoomNumber();
            }
        });

        m.put(ContactFields.SALES_VOLUME, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getSalesVolume();
            }
        });

        m.put(ContactFields.SECOND_NAME, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getMiddleName();
            }
        });

        m.put(ContactFields.SPOUSE_NAME, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getSpouseName();
            }
        });

        m.put(ContactFields.STATE_BUSINESS, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getStateBusiness();
            }
        });

        m.put(ContactFields.STATE_HOME, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getStateHome();
            }
        });

        m.put(ContactFields.STATE_OTHER, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getStreetOther();
            }
        });

        m.put(ContactFields.STREET_BUSINESS, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getStreetBusiness();
            }
        });

        m.put(ContactFields.STREET_HOME, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getStreetHome();
            }
        });

        m.put(ContactFields.STREET_OTHER, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getStreetOther();
            }
        });

        m.put(ContactFields.SUFFIX, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getSuffix();
            }
        });

        m.put(ContactFields.TAX_ID, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getTaxID();
            }
        });

        m.put(ContactFields.TELEPHONE_ASSISTANT, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getTelephoneAssistant();
            }
        });

        m.put(ContactFields.TELEPHONE_BUSINESS1, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getTelephoneBusiness1();
            }
        });

        m.put(ContactFields.TELEPHONE_BUSINESS2, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getTelephoneBusiness2();
            }
        });

        m.put(ContactFields.TELEPHONE_CALLBACK, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getTelephoneCallback();
            }
        });

        m.put(ContactFields.TELEPHONE_CAR, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getTelephoneCar();
            }
        });

        m.put(ContactFields.TELEPHONE_COMPANY, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getTelephoneCompany();
            }
        });

        m.put(ContactFields.TELEPHONE_HOME1, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getTelephoneHome1();
            }
        });

        m.put(ContactFields.TELEPHONE_HOME2, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getTelephoneHome2();
            }
        });

        m.put(ContactFields.TELEPHONE_IP, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getTelephoneIP();
            }
        });

        m.put(ContactFields.TELEPHONE_ISDN, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getTelephoneISDN();
            }
        });

        m.put(ContactFields.TELEPHONE_OTHER, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getTelephoneOther();
            }
        });

        m.put(ContactFields.TELEPHONE_PAGER, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getTelephonePager();
            }
        });

        m.put(ContactFields.TELEPHONE_PRIMARY, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getTelephonePrimary();
            }
        });

        m.put(ContactFields.TELEPHONE_RADIO, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getTelephoneRadio();
            }
        });

        m.put(ContactFields.TELEPHONE_TELEX, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getTelephoneTelex();
            }
        });

        m.put(ContactFields.TELEPHONE_TTYTDD, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getTelephoneTTYTTD();
            }
        });

        m.put(ContactFields.TITLE, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getTitle();
            }
        });

        m.put(ContactFields.URL, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getURL();
            }
        });

        m.put(ContactFields.USER_ID, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return Integer.valueOf(candidate.getInternalUserId());
            }
        });

        // TODO: Userfield01 - Userfield20

        /*-
         * Common fields
         */

        m.put(ContactFields.CATEGORIES, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getCategories();
            }
        });

        m.put(ContactFields.COLORLABEL, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return Integer.valueOf(candidate.getLabel());
            }
        });

        m.put(ContactFields.CREATED_BY, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return Integer.valueOf(candidate.getCreatedBy());
            }
        });

        m.put(ContactFields.CREATION_DATE, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getCreationDate();
            }
        });

        m.put(ContactFields.FOLDER_ID, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return Integer.valueOf(candidate.getParentFolderID());
            }
        });

        m.put(ContactFields.ID, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return Integer.valueOf(candidate.getObjectID());
            }
        });

        m.put(ContactFields.LAST_MODIFIED, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return candidate.getLastModified();
            }
        });

        m.put(ContactFields.MODIFIED_BY, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return Integer.valueOf(candidate.getModifiedBy());
            }
        });

        m.put(ContactFields.PRIVATE_FLAG, new AttributeGetter() {

            public Object getObject(final ContactObject candidate) {
                return Boolean.valueOf(candidate.getPrivateFlag());
            }
        });

        GETTERS = Collections.unmodifiableMap(m);
    }

    private static final ContactAttributeFetcher instance = new ContactAttributeFetcher();

    /**
     * Gets the contact attribute fetcher instance.
     * 
     * @return The contact attribute fetcher instance.
     */
    public static ContactAttributeFetcher getInstance() {
        return instance;
    }

    /**
     * Initializes a new {@link ContactAttributeFetcher}.
     */
    private ContactAttributeFetcher() {
        super();
    }

    public <T> T getAttribute(final String attributeName, final ContactObject candidate) {
        final AttributeGetter getter = GETTERS.get(attributeName);
        if (null == getter) {
            if (LOG.isInfoEnabled()) {
                LOG.info("No getter for field: " + attributeName);
            }
            return null;
        }
        return (T) getter.getObject(candidate);
    }

}
