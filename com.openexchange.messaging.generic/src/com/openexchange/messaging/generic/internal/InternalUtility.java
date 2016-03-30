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

package com.openexchange.messaging.generic.internal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.mail.Flags;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.MessagingMessage;
import com.openexchange.messaging.generic.Utility;

/**
 * {@link InternalUtility}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class InternalUtility {

    /**
     * Initializes a new {@link InternalUtility}.
     */
    private InternalUtility() {
        super();
    }

    /**
     * Converts specified flags bit mask to an instance of {@link Flags}.
     *
     * @param flags The flags bit mask
     * @return The corresponding instance of {@link Flags}
     */
    public static Flags convertMessagingFlags(final int flags) {
        final Flags flagsObj = new Flags();
        if ((flags & MessagingMessage.FLAG_ANSWERED) == MessagingMessage.FLAG_ANSWERED) {
            flagsObj.add(Flags.Flag.ANSWERED);
        }
        if ((flags & MessagingMessage.FLAG_DELETED) == MessagingMessage.FLAG_DELETED) {
            flagsObj.add(Flags.Flag.DELETED);
        }
        if ((flags & MessagingMessage.FLAG_DRAFT) == MessagingMessage.FLAG_DRAFT) {
            flagsObj.add(Flags.Flag.DRAFT);
        }
        if ((flags & MessagingMessage.FLAG_FLAGGED) == MessagingMessage.FLAG_FLAGGED) {
            flagsObj.add(Flags.Flag.FLAGGED);
        }
        if ((flags & MessagingMessage.FLAG_RECENT) == MessagingMessage.FLAG_RECENT) {
            flagsObj.add(Flags.Flag.RECENT);
        }
        if ((flags & MessagingMessage.FLAG_SEEN) == MessagingMessage.FLAG_SEEN) {
            flagsObj.add(Flags.Flag.SEEN);
        }
        if ((flags & MessagingMessage.FLAG_USER) == MessagingMessage.FLAG_USER) {
            flagsObj.add(Flags.Flag.USER);
        }
        if ((flags & MessagingMessage.FLAG_FORWARDED) == MessagingMessage.FLAG_FORWARDED) {
            flagsObj.add(MessagingMessage.USER_FORWARDED);
        }
        if ((flags & MessagingMessage.FLAG_READ_ACK) == MessagingMessage.FLAG_READ_ACK) {
            flagsObj.add(MessagingMessage.USER_READ_ACK);
        }
        return flagsObj;
    }

    /**
     * The parsed flags.
     */
    public static final class ParsedFlags {

        private final int flags;

        private final int colorLabel;

        private final Collection<String> userFlags;

        ParsedFlags(final int flags, final int colorLabel, final Collection<String> userFlags) {
            super();
            this.flags = flags;
            this.colorLabel = colorLabel;
            this.userFlags = userFlags;
        }

        /**
         * Gets the flags
         *
         * @return The flags
         */
        public int getFlags() {
            return flags;
        }

        /**
         * Gets the user flags
         *
         * @return The user flags
         */
        public Collection<String> getUserFlags() {
            return userFlags;
        }

        /**
         * Gets the color label.
         *
         * @return The color label
         */
        public int getColorLabel() {
            return colorLabel;
        }

    }

    /**
     * Parses specified {@link Flags flags}.
     *
     * @param flags The flags to parse
     * @return The parsed flags
     */
    public static int parseSystemFlags(final Flags flags) {
        int retval = 0;
        if (flags.contains(Flags.Flag.ANSWERED)) {
            retval |= MessagingMessage.FLAG_ANSWERED;
        }
        if (flags.contains(Flags.Flag.DELETED)) {
            retval |= MessagingMessage.FLAG_DELETED;
        }
        if (flags.contains(Flags.Flag.DRAFT)) {
            retval |= MessagingMessage.FLAG_DRAFT;
        }
        if (flags.contains(Flags.Flag.FLAGGED)) {
            retval |= MessagingMessage.FLAG_FLAGGED;
        }
        if (flags.contains(Flags.Flag.RECENT)) {
            retval |= MessagingMessage.FLAG_RECENT;
        }
        if (flags.contains(Flags.Flag.SEEN)) {
            retval |= MessagingMessage.FLAG_SEEN;
        }
        if (flags.contains(Flags.Flag.USER)) {
            retval |= MessagingMessage.FLAG_USER;
        }
        final String[] userFlags = flags.getUserFlags();
        if (userFlags != null) {
            for (final String userFlag : userFlags) {
                if (MessagingMessage.USER_FORWARDED.equalsIgnoreCase(userFlag)) {
                    retval |= MessagingMessage.FLAG_FORWARDED;
                } else if (MessagingMessage.USER_READ_ACK.equalsIgnoreCase(userFlag)) {
                    retval |= MessagingMessage.FLAG_READ_ACK;
                }
            }
        }
        /*
         * Return system flags
         */
        return retval;
    }

    /**
     * Parses specified {@link Flags flags}.
     *
     * @param flags The flags to parse
     * @return The parsed flags
     * @throws OXException If a messaging error occurs
     */
    public static ParsedFlags parseFlags(final Flags flags) throws OXException {
        int retval = 0;
        int colorLable = COLOR_LABEL_NONE;
        Collection<String> ufCol = null;
        if (flags.contains(Flags.Flag.ANSWERED)) {
            retval |= MessagingMessage.FLAG_ANSWERED;
        }
        if (flags.contains(Flags.Flag.DELETED)) {
            retval |= MessagingMessage.FLAG_DELETED;
        }
        if (flags.contains(Flags.Flag.DRAFT)) {
            retval |= MessagingMessage.FLAG_DRAFT;
        }
        if (flags.contains(Flags.Flag.FLAGGED)) {
            retval |= MessagingMessage.FLAG_FLAGGED;
        }
        if (flags.contains(Flags.Flag.RECENT)) {
            retval |= MessagingMessage.FLAG_RECENT;
        }
        if (flags.contains(Flags.Flag.SEEN)) {
            retval |= MessagingMessage.FLAG_SEEN;
        }
        if (flags.contains(Flags.Flag.USER)) {
            retval |= MessagingMessage.FLAG_USER;
        }
        final String[] userFlags = flags.getUserFlags();
        if (userFlags != null) {
            /*
             * Mark message to contain user flags
             */
            final Set<String> set = new HashSet<String>(userFlags.length);
            for (final String userFlag : userFlags) {
                if (isColorLabel(userFlag)) {
                    colorLable = getColorLabelIntValue(userFlag);
                } else if (MessagingMessage.USER_FORWARDED.equalsIgnoreCase(userFlag)) {
                    retval |= MessagingMessage.FLAG_FORWARDED;
                } else if (MessagingMessage.USER_READ_ACK.equalsIgnoreCase(userFlag)) {
                    retval |= MessagingMessage.FLAG_READ_ACK;
                } else {
                    set.add(userFlag);
                }
            }
            ufCol = set.isEmpty() ? null : set;
        }
        /*
         * Return parsed flags
         */
        return new ParsedFlags(retval, colorLable, ufCol);
    }

    /**
     * The prefix for a mail message's color labels stored as a user flag
     */
    public static final String COLOR_LABEL_PREFIX = "$cl_";

    /**
     * The deprecated prefix for a mail message's color labels stored as a user flag
     */
    public static final String COLOR_LABEL_PREFIX_OLD = "cl_";

    /**
     * The <code>int</code> value for no color label
     */
    public static final int COLOR_LABEL_NONE = 0;

    /**
     * Determines the corresponding <code>int</code> value of a given color label's string representation.
     * <p>
     * A color label's string representation matches the pattern:<br>
     * &lt;value-of-{@link #COLOR_LABEL_PREFIX}&gt;&lt;color-label-int-value&gt;
     * <p>
     * &lt;value-of-{@link #COLOR_LABEL_PREFIX_OLD} &gt;&lt;color-label-int-value&gt; is also accepted.
     *
     * @param cl The color label's string representation
     * @return The color label's <code>int</code> value
     * @throws OXException If coor label cannot be parsed
     */
    public static int getColorLabelIntValue(final String cl) throws OXException {
        if (!isColorLabel(cl)) {
            throw MessagingExceptionCodes.UNKNOWN_COLOR_LABEL.create(cl);
        }
        try {
            return Integer.parseInt(cl.substring(cl.charAt(0) == '$' ? COLOR_LABEL_PREFIX.length() : COLOR_LABEL_PREFIX_OLD.length()));
        } catch (final NumberFormatException e) {
            throw MessagingExceptionCodes.UNKNOWN_COLOR_LABEL.create(cl);
        }
    }

    /**
     * Tests if specified string matches a color label pattern.
     *
     * @param cl The string to check
     * @return <code>true</code> if specified string matches a color label pattern; otherwise <code>false</code>
     */
    public static boolean isColorLabel(final String cl) {
        return (cl != null && (cl.startsWith(COLOR_LABEL_PREFIX) || cl.startsWith(COLOR_LABEL_PREFIX_OLD)));
    }

    /**
     * Parses specified color label's string.
     * <p>
     * <b>Note</b> that this method assumes {@link #isColorLabel(String)} would return <code>true</code> for specified string.
     *
     * @param cl The color label's string
     * @param defaultValue The default value to return if parsing color label's <code>int</code> value fails
     * @return The color label's <code>int</code> value or <code>defaultValue</code> on failure.
     */
    public static int parseColorLabel(final String cl, final int defaultValue) {
        try {
            return Integer.parseInt(cl.substring('$' == cl.charAt(0) ? COLOR_LABEL_PREFIX.length() : COLOR_LABEL_PREFIX_OLD.length()));
        } catch (final NumberFormatException e) {
            final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Utility.class);
            log.debug("Inbvalid color label: {}", cl, e);
            return defaultValue;
        }
    }

    /**
     * Generates the color label's string representation from given <code>int</code> value.
     * <p>
     * A color label's string representation matches the pattern:<br>
     * &lt;value-of-{@link #COLOR_LABEL_PREFIX}&gt;&lt;color-label-int-value&gt;
     *
     * @param cl The color label's <code>int</code> value
     * @return The color abel's string representation
     */
    public static String getColorLabelStringValue(final int cl) {
        return new StringBuilder(COLOR_LABEL_PREFIX).append(cl).toString();
    }

}
