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

package com.openexchange.xing;

import org.json.JSONObject;

/**
 * {@link Address} - Represents an address.
 * <p>
 * 
 * <pre>
 *     "private_address": {
 *       "city": "Hamburg",
 *       "country": "DE",
 *       "zip_code": "20357",
 *       "street": "Privatstra\u00dfe 1",
 *       "phone": "+49|40|1234560",
 *       "fax": null,
 *       "province": "Hamburg",
 *       "email": "max@mustermann.de",
 *       "mobile_phone": "+49|0155|1234567"
 *     }
 * </pre>
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Address {

    private final String city;
    private final String country;
    private final String zipCode;
    private final String street;
    private final String phone;
    private final String fax;
    private final String province;
    private final String email;
    private final String mobilePhone;

    /**
     * Initializes a new {@link Address}.
     */
    public Address(final JSONObject addressInformation) {
        super();
        this.city = addressInformation.optString("city", null);
        this.country = addressInformation.optString("country", null);
        this.zipCode = addressInformation.optString("zip_code", null);
        this.street = addressInformation.optString("street", null);
        this.phone = sanitizePhoneNumber(addressInformation.optString("phone", null));
        this.fax = sanitizePhoneNumber(addressInformation.optString("fax", null));
        this.province = addressInformation.optString("province", null);
        this.email = addressInformation.optString("email", null);
        this.mobilePhone = sanitizePhoneNumber(addressInformation.optString("mobile_phone", null));
    }

    /**
     * Sanitizes the phone number so that it starts with a '+' or '00'
     * 
     * @param phoneNumber - the number to sanitize
     * @return String with the sanitized number or 'null' if provided
     */
    private String sanitizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return phoneNumber;
        }

        String toSanitize = phoneNumber;

        if (toSanitize.equalsIgnoreCase("null")) {
            return toSanitize;
        } else if ((toSanitize.startsWith("+") || (toSanitize.startsWith("00")))) {
            return toSanitize;
        } else {
            toSanitize = "+" + toSanitize;
        }

        return toSanitize;
    }

    /**
     * Gets the city
     *
     * @return The city
     */
    public String getCity() {
        return city;
    }

    /**
     * Gets the country
     *
     * @return The country
     */
    public String getCountry() {
        return country;
    }

    /**
     * Gets the ZIP code
     *
     * @return The ZIP code
     */
    public String getZipCode() {
        return zipCode;
    }

    /**
     * Gets the street
     *
     * @return The street
     */
    public String getStreet() {
        return street;
    }

    /**
     * Gets the phone
     *
     * @return The phone
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Gets the fax
     *
     * @return The fax
     */
    public String getFax() {
        return fax;
    }

    /**
     * Gets the province
     *
     * @return The province
     */
    public String getProvince() {
        return province;
    }

    /**
     * Gets the email
     *
     * @return The email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Gets the mobile phone
     *
     * @return The mobile phone
     */
    public String getMobilePhone() {
        return mobilePhone;
    }

}
