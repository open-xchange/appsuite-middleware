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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.sun.mail.imap;

import javax.mail.MessagingException;
import com.sun.mail.imap.IMAPFolder.SnippetFetchProfileItem;

/**
 * {@link IMAPTextPreviewProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface IMAPTextPreviewProvider {
    
    public static enum Mode {
        /** Requires to generate a text preview for a message */
        REQUIRE("FUZZY"),
        /**Only generate a text preview for a message if it is already available/cached */
        ONLY_IF_AVAILABLE("LAZY=FUZZY"),
        ;
        
        private final String algorithmName;

        private Mode(String algorithmName) {
            this.algorithmName = Utility.toUpperCase(algorithmName);
        }

        /**
         * Gets the algorithm name
         *
         * @return The algorithm name
         */
        public String getAlgorithmName() {
            return algorithmName;
        }
        
        /**
         * Gets the mode for given algorithm name
         *
         * @param algorithmName The algorithm name
         * @return The associated mode or <code>null</code>
         */
        public static Mode modeFor(String algorithmName) {
            if (null == algorithmName) {
                return null;
            }
            
            for (Mode mode : Mode.values()) {
                if (mode.algorithmName.equals(algorithmName)) {
                    return mode;
                }
            }
            return null;
        }
    }

    /**
     * Gets the text preview for specified message according to given mode.
     *
     * @param message The message
     * @param mode The mode to obey
     * @return The text preview for specified message
     * @throws MessagingException If text preview cannot be returned
     */
    String getTextPreview(IMAPMessage message, Mode mode) throws MessagingException;
    
    /**
     * Gets the text preview for referenced message according to given mode.
     *
     * @param uid The UID of the message
     * @param mode The mode to obey
     * @return The text preview for specified message
     * @throws MessagingException If text preview cannot be returned
     */
    String getTextPreview(long uid, Mode mode) throws MessagingException;
}
