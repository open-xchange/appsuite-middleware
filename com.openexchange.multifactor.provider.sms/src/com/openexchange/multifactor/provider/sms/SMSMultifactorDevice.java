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

package com.openexchange.multifactor.provider.sms;

import com.openexchange.java.Strings;
import com.openexchange.multifactor.AbstractMultifactorDevice;
import com.openexchange.multifactor.MultifactorDevice;
import com.openexchange.multifactor.provider.sms.impl.MultifactorSMSProvider;

/**
 * {@link SMSMultifactorDevice}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public class SMSMultifactorDevice extends AbstractMultifactorDevice {

    public static final String PHONE_NUMBER_PARAMETER = "phoneNumber";
    public static final String PHONE_NUMBER_TAIL_PARAMETER = "phoneNumberTail";
    public static final String SECRET_CODE_PARAMETER         = "secret_code";

    private static final int TAIL_LENGTH = 4;

    /**
     * Initializes a new {@link SMSMultifactorDevice}.
     *
     * @param id The id of the device
     * @param name The name of the device
     * @param phoneNumber The phone number of this device
     * @param backup whether this device is a backup device or not
     */
    public SMSMultifactorDevice(String id, String name, String phoneNumber, Boolean backup) {
        super(id, MultifactorSMSProvider.NAME, name);
        setBackup(backup);
        setPhoneNumber(phoneNumber);
        setPhoneNumberTail(createPhoneNumberTail(phoneNumber,TAIL_LENGTH));
    }

    /**
     * Extracts the tail of a given phone number
     *
     * @param phoneNumber the number
     * @param tailLength the length of the tail to extract
     * @return The tail of the number with the given length, or null if the number is empty or not long enough
     */
    private String createPhoneNumberTail(String phoneNumber, int tailLength) {
       if(Strings.isNotEmpty(phoneNumber) && phoneNumber.length() >= tailLength) {
         return phoneNumber.substring(phoneNumber.length() - tailLength);
       }
       return null;
    }

    /**
     * Initializes a new {@link SMSMultifactorDevice} on base of an existing {@link MultifactorDevice}.
     *
     * @param source The {@link MultifactorDevice} to create the new device from
     */
    public SMSMultifactorDevice(MultifactorDevice source) {
        super(source.getId(),
              source.getProviderName(),
              source.getName(),
              source.getParameters());
        setBackup(source.isBackup());
    }

    /**
     * Adds the tail of the phone-number to the devices parameters.
     *
     * This gives a hint to the client to which phone device a challenge token was sent, without revealing the complete number.
     *
     * @param tail The tail to set
     */
    private void setPhoneNumberTail(String tail) {
        setParameter(PHONE_NUMBER_TAIL_PARAMETER, tail);
    }

    /**
     * Sets the device's phone number
     *
     * @param phoneNumber The number to set
     */
    private void setPhoneNumber(String phoneNumber) {
        setParameter(PHONE_NUMBER_PARAMETER, phoneNumber);
    }

    /**
     * Gets the phone number of the device
     *
     * @return The phone number
     */
    public String getPhoneNumber() {
        return getParameter(PHONE_NUMBER_PARAMETER);
    }

    /**
     * Convenience method to remove the phone number from the device's paramters
     */
    public void removePhoneNumber() {
        removeParamter(PHONE_NUMBER_PARAMETER);
    }

    /**
     * Returns the last 4 digits of the phone number
     *
     * @return The last four digits of the phone number
     */
    public String getPhoneNumberTail() {
        return getParameter(PHONE_NUMBER_TAIL_PARAMETER);
    }

    /**
     * Gets the secret authentication code
     *
     * @return The secret authentication code
     */
    public String getSecretCode() {
        return getParameter(SECRET_CODE_PARAMETER);
    }
}