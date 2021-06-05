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


package com.openexchange.messaging.sms.service;

import java.util.List;

/**
 * An interface describing what information a user configuration has to provide
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public interface MessagingUserConfigurationInterface {

    /**
     * Get the list of allowed sender addresses
     * @return
     */
    public List<String> getAddresses();

    /**
     * TODO
     * @return
     */
    public String getDisplayString();

    /**
     * Get the maximum allowed length of the message
     * @return
     */
    public int getLength();

    /**
     * If the messaging service is enabled for the user or not
     *
     * @return
     */
    public boolean isEnabled();

    /**
     * If the message service uses captchas
     * @return
     */
    public boolean isCaptcha();

    /**
     * If the backend is allowed to send multiple SMS, if yes, the GUI shows a counter for the number of SMS messages to be sent
     * @return
     */
    public boolean getMultiSMS();

    /**
     * If the backend is allowed to send MMS messages, if yes, the GUI allows to upload images
     * @return
     */
    public boolean isMMS();

    /**
     * Returns an optional Upsell link, if the user has no SMS enabled.
     * @return
     */
    public String getUpsellLink();

    /**
     * If the user should have the option to append a signature to the outgoing SMS
     * @return
     */
    public boolean isSignatureOption();

    /**
     * Returns the max. number of of recipients, use 0 for unlimited
     * @return
     */
    public int getRecipientLimit();

    /**
     * Returns the max. number of of sms, use 0 for unlimited
     * @return
     */
    public int getSmsLimit();

    /**
     * Return the RegEx which will be used to clean numbers in the GUI. Can be null if the default should be used
     * @return
     */
    public String getNumCleanRegEx();
}
