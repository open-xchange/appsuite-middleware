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

package com.openexchange.drive.impl.sync;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.java.Strings;


/**
 * {@link RenameTools}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class RenameTools {

    /**
     * Finds an alternative filename for the supplied conflicting name, considering the given set of already used filenames.
     *
     * @param conflictingName The conflicting name
     * @param usedFilenames The used filenames that can't be used
     * @param deviceName The device name to incorporate when generating the alternative name, or <code>null</code> to ignore
     * @return An alternative name
     */
    public static String findAlternativeName(String conflictingName, Set<String> usedFilenames, String deviceName) {
        if (Strings.isEmpty(deviceName)) {
            return findAlternativeName(conflictingName, usedFilenames);
        }
        FileName fileName = new FileName(conflictingName);
        String name = fileName.getName();
        Pattern regex = Pattern.compile("\\(" + Pattern.quote(deviceName) + "(\\s\\d+)?\\)\\z");
        String alternativeName;
        do {
            Matcher matcher = regex.matcher(name);
            if (false == matcher.find()) {
                /*
                 * append new initial device name
                 */
                name += " (" + deviceName + ')';
            } else if (0 == matcher.groupCount() || 0 < matcher.groupCount() && Strings.isEmpty(matcher.group(1))) {
                /*
                 * append new initial sequence number
                 */
                name = name.substring(0, matcher.start()) + '(' + deviceName + " 1)";
            } else {
                /*
                 * incremented existing sequence number
                 */
                int number = 0;
                try {
                    number = Integer.valueOf(matcher.group(1).trim()).intValue();
                } catch (NumberFormatException e) {
                    // should not get here
                }
                name = name.substring(0, matcher.start()) + '(' + deviceName + ' ' + String.valueOf(1 + number) + ')';
            }
            alternativeName = name + fileName.getExtension();
        } while (null != usedFilenames && usedFilenames.contains(alternativeName));
        return alternativeName;
    }

    /**
     * Finds an alternative filename for the supplied conflicting name, considering the given set of already used filenames.
     *
     * @param conflictingName The conflicting name
     * @param usedFilenames The used filenames that can't be used
     * @return An alternative name
     */
    public static String findAlternativeName(String conflictingName, Set<String> usedFilenames) {
        FileName fileName = new FileName(conflictingName);
        String name = fileName.getName();
        Pattern regex = Pattern.compile("\\((\\d+)\\)\\z");
        String alternativeName;
        do {
            Matcher matcher = regex.matcher(name);
            if (false == matcher.find()) {
                /*
                 * append new initial sequence number
                 */
                name += " (1)";
            } else {
                /*
                 * incremented existing sequence number
                 */
                int number = 0;
                try {
                    number = Integer.valueOf(matcher.group(1)).intValue();
                } catch (NumberFormatException e) {
                    // should not get here
                }
                name = name.substring(0, matcher.start()) + '(' + String.valueOf(1 + number) + ')';
            }
            alternativeName = name + fileName.getExtension();
        } while (null != usedFilenames && usedFilenames.contains(alternativeName));
        return alternativeName;
    }

    /**
     * Constructs an alternative filename for the supplied conflicting name by appending the current system time.
     *
     * @param conflictingName The conflicting name
     * @return An alternative name
     */
    public static String findRandomAlternativeName(String conflictingName) {
        FileName fileName = new FileName(conflictingName);
        return fileName.getName() + ' ' + System.currentTimeMillis() + fileName.getExtension();
    }

    /**
     * Constructs an alternative filename for the supplied conflicting name by appending the current system time.
     *
     * @param conflictingName The conflicting name
     * @param deviceName The device name to incorporate when generating the alternative name, or <code>null</code> to ignore
     * @return An alternative name
     */
    public static String findRandomAlternativeName(String conflictingName, String deviceName) {
        if (Strings.isEmpty(deviceName)) {
            return findRandomAlternativeName(conflictingName);
        }
        FileName fileName = new FileName(conflictingName);
        return fileName.getName() + " (" + deviceName + ' ' + System.currentTimeMillis() + ')' + fileName.getExtension();
    }

    private static final class FileName {

        private String name;
        private String extension;

        public FileName(String fileName) {
            super();
            int extensionIndex = fileName.lastIndexOf('.');
            if (-1 == extensionIndex) {
                name = fileName;
                extension = "";
            } else {
                name = fileName.substring(0, extensionIndex);
                extension = fileName.substring(extensionIndex);
            }
        }

        /**
         * Gets the extension
         *
         * @return The extension
         */
        public String getExtension() {
            return extension;
        }

        /**
         * Gets the name
         *
         * @return The name
         */
        public String getName() {
            return name;
        }

    }

}
