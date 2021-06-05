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

package com.openexchange.multifactor.util;

import static com.openexchange.java.Autoboxing.I;
import com.google.common.base.CharMatcher;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.multifactor.MultifactorDevice;
import com.openexchange.multifactor.exceptions.MultifactorExceptionCodes;

/**
 * {@link DeviceNaming} - Utility class for common multifactor device naming
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public class DeviceNaming {

    private static final int MAX_NAME_LENGHT = 100;

    /**
     * {@link DefaultName} - Functional callback to obtain a default name in case {@link DeviceNaming} was not able to obtain the name from the given device.
     *
     * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
     * @since v7.10.2
     */
    @FunctionalInterface
    public interface DefaultName {

        /**
         * Gets the default name of a new multifactor device
         *
         * @return The name to use for a new multifactor device
         * @throws OXException
         */
        public String get() throws OXException;

    }

    /**
     * Applies the given name to a device by performing validation checks, or applies a default name if no device name was given.
     *
     * @param device The device to get the name from
     * @param defaultName The function to provide a default name in case the given device does not contain a name
     * @throws OXException If the device name is too long
     */
    public static void applyName(MultifactorDevice device, DefaultName defaultName) throws OXException {
        if (Strings.isEmpty(device.getName())) {
            device.setName(defaultName.get());
        } else {
            if (device.getName().length() > MAX_NAME_LENGHT) {
                throw MultifactorExceptionCodes.INVALID_ARGUMENT_LENGTH.create("name", I(device.getName().length()), I(MAX_NAME_LENGHT));
            }
            //Remove all control characters from the device name in order to prevent injecting those characters into log-files or terminal outputs
            device.setName(CharMatcher.javaIsoControl().removeFrom(device.getName()));
        }
    }
}
