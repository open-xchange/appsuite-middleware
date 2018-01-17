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

package com.openexchange.multifactor.util;

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
                throw MultifactorExceptionCodes.INVALID_ARGUMENT_LENGTH.create("name", device.getName().length(), MAX_NAME_LENGHT);
            }
            //Remove all control characters from the device name in order to prevent injecting those characters into log-files or terminal outputs
            device.setName(CharMatcher.javaIsoControl().removeFrom(device.getName()));
        }
    }
}
