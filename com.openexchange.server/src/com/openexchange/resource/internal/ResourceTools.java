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

package com.openexchange.resource.internal;

import java.util.regex.Pattern;
import javax.mail.internet.AddressException;
import com.openexchange.mail.mime.QuotedInternetAddress;

/**
 * {@link ResourceTools} - Utility methods for resource module
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ResourceTools {

    /**
     * Initializes a new {@link ResourceTools}
     */
    private ResourceTools() {
        super();
    }

    private static final Pattern PATTERN_ALLOWED_CHARS = Pattern.compile("[\\S ]+");

    /**
     * Checks if specified resource identifier contains invalid characters.
     * <p>
     * Valid characters are:<br>
     * &quot;<i>&nbsp; abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_ -+.%$@<i>&quot;
     *
     * @param identifier The resource identifier to check
     * @return <code>true</code> if specified resource identifier only consists of valid characters; otherwise <code>false</code>
     */
    public static boolean validateResourceIdentifier(final String identifier) {
        /*
         * Check for allowed chars: abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 _-+.%$@
         */
        return PATTERN_ALLOWED_CHARS.matcher(identifier).matches();
    }

    // private static final Pattern PATTERN_VALID_EMAIL =
    // Pattern.compile("[$%\\.+a-zA-Z0-9_-]+@[\\.a-zA-Z0-9_-]+");

    /**
     * Checks if specified resource email address' notation is valid
     *
     * @param emailAddress The resource email address to check
     * @return <code>true</code> if specified resource email address is valid; otherwise <code>false</code>
     */
    public static boolean validateResourceEmail(final String emailAddress) {
        /*
         * Validate e-mail with InternetAddress class from JavaMail API
         */
        try {
            new QuotedInternetAddress(emailAddress, true).validate();
            return true;
        } catch (final AddressException e) {
            return false;
        }
    }
}
