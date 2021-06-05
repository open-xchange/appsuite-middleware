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
    public SMSMultifactorDevice(String id, String name, String phoneNumber, boolean backup) {
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
       if (Strings.isNotEmpty(phoneNumber) && phoneNumber.length() >= tailLength) {
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