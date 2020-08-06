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

package com.openexchange.authentication.application.storage.rdb;

import java.security.SecureRandom;
import java.util.regex.Pattern;
import com.openexchange.java.Strings;

/**
 * {@link AppPasswordGenerator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.4
 */
public class AppPasswordGenerator {

    private static final char[] PASSWORD_CHARS = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final char[] PASSWORD_FORMAT = "xxxx-xxxx-xxxx-xxxx".toCharArray();
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("[a-z]{4}-[a-z]{4}-[a-z]{4}-[a-z]{4}");

    private final SecureRandom random;

    /**
     * Initializes a new {@link AppPasswordGenerator}.
     */
    public AppPasswordGenerator() {
        super();
        this.random = new SecureRandom();
    }

    /**
     * Gets a new randomly generated password
     *
     * @return String of new password
     */
    public String generateRandomPassword() {
        StringBuilder stringBuilder = new StringBuilder(PASSWORD_FORMAT.length);
        for (char c : PASSWORD_FORMAT) {
            if ('x' == c) {
                stringBuilder.append(PASSWORD_CHARS[random.nextInt(PASSWORD_CHARS.length)]);
            } else {
                stringBuilder.append(c);
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Gets a value indicating whether the supplied password is in the expected format as produced by this generator or not.
     *
     * @param password The password to check
     * @return <code>true</code> if the password is in the expected format, <code>false</code>, otherwise
     */
    public boolean isInExpectedFormat(String password) {
        return Strings.isNotEmpty(password) && PASSWORD_PATTERN.matcher(password).matches();
    }

}
