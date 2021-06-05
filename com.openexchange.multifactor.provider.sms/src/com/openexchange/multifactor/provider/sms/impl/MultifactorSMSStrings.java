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

package com.openexchange.multifactor.provider.sms.impl;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link MultifactorSMSStrings} contains text messages sent to the user's phone
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public class MultifactorSMSStrings implements LocalizableStrings {

    /**
     * The text message sent to the user for providing him the authentication code
     */
    public static final String MULTIFACTOR_SMS_TEXT = "Your authentication code is: ";

    /**
     * The default name of the multifactor SMS device
     */
    public static final String MULTIFACTOR_SMS_DEFAULT_DEVICE_NAME = "My Phone";

}
