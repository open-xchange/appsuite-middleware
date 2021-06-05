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
