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

package com.openexchange.file.storage.composition;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.java.Strings;

/**
 * {@link FilenameValidationUtils}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.1
 */
public final class FilenameValidationUtils {

    /** An enumeration of possible file name violations */
    public static enum ValidityViolation {
        /** No file name violation */
        NONE,
        /** The file name matches a reserved name */
        RESERVED_NAME,
        /** The file name consists only of dots (<code>"."</code> or <code>".."</code>) */
        ONLY_DOTS,
        /** The file name ends with a dot or white-space */
        OTHER_ILLEGAL,
        ;
    }

    /** Represents a validity result */
    public static final class ValidityResult {

        private final ValidityViolation violation;
        private final String info;

        ValidityResult(ValidityViolation violation) {
            this(violation, null);
        }

        ValidityResult(ValidityViolation violation, String info) {
            super();
            this.violation = violation;
            this.info = info;
        }

        /**
         * Checks if this validity result rated the checked file name to be valid.
         *
         * @return <code>true</code> if valid; otherwise <code>false</code>
         */
        public boolean isValid() {
            return violation == ValidityViolation.NONE;
        }

        /**
         * Gets the violation
         *
         * @return The violation
         */
        public ValidityViolation getViolation() {
            return violation;
        }

        /**
         * Gets optional additional information
         *
         * @return The information string or <code>null</code>
         */
        public String getInfo() {
            return info;
        }
    }

    private static final ValidityResult VALID = new ValidityResult(ValidityViolation.NONE);

    // ---------------------------------------------------------------------------------------------------------------------------------

    private FilenameValidationUtils() {
        super();
    }

    public static final int MAX_PATH_SEGMENT_LENGTH = 255;

    public static final Pattern FILENAME_VALIDATION_PATTERN = Pattern.compile(
        "^                                # Anchor to start of string.        \n" +
        "(?!                              # Assert filename is not: CON, PRN, \n" +
        "  (?:                            # AUX, NUL, COM1, COM2, COM3, COM4, \n" +
        "    CON|PRN|AUX|NUL|             # COM5, COM6, COM7, COM8, COM9,     \n" +
        "    COM[1-9]|LPT[1-9]            # LPT1, LPT2, LPT3, LPT4, LPT5,     \n" +
        "  )                              # LPT6, LPT7, LPT8, and LPT9...     \n" +
        "  (?:\\.[^.]*)?                  # followed by optional extension    \n" +
        "  $                              # and end of string                 \n" +
        ")                                # End negative lookahead assertion. \n" +
        "[^<>:/?*\"\\\\|\\x00-\\x1F]*     # Zero or more valid filename chars.\n" +
        "[^<>:/?*\"\\\\|\\x00-\\x1F\\ .]  # Last char is not a space or dot.  \n" +
        "$                                # Anchor to end of string.            ",
        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.COMMENTS);

    public static final Pattern ILLEGAL_CHARACTER_PATTERN = Pattern.compile("(<)|(>)|(:)|(\\/)|(\\?)|(\\*)|(\\\")|(\\\\)|(\\|)");

    public static final Pattern RESERVED_NAME_PATTERN = Pattern.compile("(^CON$)|(^PRN$)|(^AUX$)|(^NUL$)|(^COM[1-9]$)|(^LPT[1-9]$)", Pattern.CASE_INSENSITIVE);

    public static final Pattern ONLY_DOTS_PATTERN = Pattern.compile("(^\\.$)|(^\\.\\.$)");

    public static final Pattern OTHER_ILLEGAL_PATTERN = Pattern.compile("(.*\\.$)|(.*\\s$)");

    /**
     * Checks specified file name for illegal characters
     *
     * @param filename The file name
     * @throws OXException If file name contains illegal characters
     * @see #getIllegalCharacters(String)
     */
    public static void checkCharacters(String filename) throws OXException {
        String illegalCharacters = getIllegalCharacters(filename);
        if (Strings.isNotEmpty(illegalCharacters)) {
            throw FileStorageExceptionCodes.ILLEGAL_CHARACTERS.create(illegalCharacters);
        }
    }

    /**
     * Gets the illegal characters from specified file name
     *
     * @param filename The file name
     * @return The illegal characters or <code>null</code>
     */
    public static String getIllegalCharacters(String filename) {
        StringBuilder sb = null;
        for (Matcher matcher = ILLEGAL_CHARACTER_PATTERN.matcher(filename); matcher.find();) {
            String group = matcher.group();
            if (Strings.isNotEmpty(group)) {
                if (null == sb) {
                    sb = new StringBuilder();
                    sb.append(group);
                } else {
                    if (sb.lastIndexOf(group) == -1) {
                        sb.append(group);
                    }
                }
            }
        }

        return null == sb || sb.length() == 0 ? null : sb.toString();
    }

    /**
     * Checks specified file name's validity.
     *
     * @param filename The file name
     * @throws OXException If file name matches a reserved name, consists only of dots (<code>"."</code> or <code>".."</code>) or ends with a dot or white-space
     */
    public static void checkName(String filename) throws OXException {
        ValidityResult validity = getValidityFor(filename);
        switch (validity.getViolation()) {
            case ONLY_DOTS:
                throw FileStorageExceptionCodes.ONLY_DOTS_NAME.create();
            case OTHER_ILLEGAL:
                throw FileStorageExceptionCodes.WHITESPACE_END.create();
            case RESERVED_NAME:
                throw FileStorageExceptionCodes.RESERVED_NAME.create(validity.getInfo());
            default:
                break;
        }
    }

    /**
     * Determines the validity of specified file name.
     *
     * @param filename The file name
     * @return The validity result
     */
    public static ValidityResult getValidityFor(String filename) {
        Matcher matcher = RESERVED_NAME_PATTERN.matcher(filename);
        if (matcher.find()) {
            return new ValidityResult(ValidityViolation.RESERVED_NAME, matcher.group());
        }
        Matcher dots = ONLY_DOTS_PATTERN.matcher(filename);
        if (dots.find()) {
            return new ValidityResult(ValidityViolation.ONLY_DOTS);
        }
        Matcher other = OTHER_ILLEGAL_PATTERN.matcher(filename);
        if (other.find()) {
            return new ValidityResult(ValidityViolation.OTHER_ILLEGAL);
        }
        return VALID;
    }

    /**
     * Gets a value indicating whether the supplied file name is invalid, i.e. it contains illegal characters or is not supported for
     * other reasons.
     *
     * @param fileName The file name to check
     * @return <code>true</code> if the filename is considered invalid, <code>false</code>, otherwise
     */
    public static boolean isInvalidFileName(String fileName) {
        if (Strings.isEmpty(fileName)) {
            return true; // no empty filenames
        }
        if (false == FILENAME_VALIDATION_PATTERN.matcher(fileName).matches()) {
            return true; // no invalid filenames
        }
        if (MAX_PATH_SEGMENT_LENGTH < fileName.length()) {
            return true; // no too long filenames
        }
        return false;
    }

    /**
     * Gets a value indicating whether the supplied folder name is invalid, i.e. it contains illegal characters or is not supported for
     * other reasons.
     *
     * @param name The folder name to check
     * @return <code>true</code> if the name is considered invalid, <code>false</code>, otherwise
     * @throws OXException
     */
    public static boolean isInvalidFolderName(String name) throws OXException {
        // same check as for filenames for now
        return isInvalidFileName(name);
    }

}
