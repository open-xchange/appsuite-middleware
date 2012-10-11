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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.ajp13.coyote;

import java.util.Set;
import javax.servlet.http.Cookie;

/**
 * {@link CookieWrapper} - {@link Set}-capable CookieWrapper
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CookieWrapper {

    /**
     * Creates a new {@link CookieWrapper} for specified cookie.
     * 
     * @param cookie The cookie
     * @return The new wrapper
     * @throws IllegalArgumentException If passed {@link Cookie} instance is <code>null</code>
     */
    public static CookieWrapper wrapper(final Cookie cookie) {
        return new CookieWrapper(cookie);
    }

    private final Cookie cookie;

    private final int hash;

    /**
     * Initializes a new {@link CookieWrapper}.
     * 
     * @throws IllegalArgumentException If passed {@link Cookie} instance is <code>null</code>
     */
    public CookieWrapper(final Cookie cookie) {
        super();
        if (null == cookie) {
            throw new IllegalArgumentException("Cookie is null.");
        }
        this.cookie = cookie;
        final int prime = 31;
        int result = 1;
        for (final String s : new String[] { cookie.getName(), cookie.getValue(), cookie.getDomain(), cookie.getPath() }) {
            result = prime * result + ((s == null) ? 0 : s.hashCode());
        }
        // Max-age
        result = prime * result + cookie.getMaxAge();
        // Secure
        result = prime * result + (cookie.getSecure() ? 1 : 0);
        // Version
        result = prime * result + cookie.getVersion();
        this.hash = result;
    }

    /**
     * Gets the assigned cookie.
     * 
     * @return The cookie
     */
    public Cookie getCookie() {
        return cookie;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CookieWrapper)) {
            return false;
        }
        final Cookie thisCookie = this.cookie;
        final Cookie otherCookie = ((CookieWrapper) obj).cookie;
        if (thisCookie == otherCookie) {
            return true;
        }
        if (thisCookie == null) {
            return (otherCookie == null);
        }
        // Compare by Cookie arguments
        if (!strEquals(thisCookie.getName(), otherCookie.getName(), false)) {
            return false;
        }
        if (!strEquals(thisCookie.getValue(), otherCookie.getValue(), false)) {
            return false;
        }
        if (!strEquals(thisCookie.getDomain(), otherCookie.getDomain(), false)) {
            return false;
        }
        if (!strEquals(thisCookie.getPath(), otherCookie.getPath(), false)) {
            return false;
        }
        if (thisCookie.getMaxAge() != otherCookie.getMaxAge()) {
            return false;
        }
        if (thisCookie.getSecure() != otherCookie.getSecure()) {
            return false;
        }
        if (thisCookie.getVersion() != otherCookie.getVersion()) {
            return false;
        }
        return true;
    }

    private static boolean strEquals(final String str1, final String str2, final boolean ignoreCase) {
        return (null == str1) ? (null == str2) : (ignoreCase ? str1.equalsIgnoreCase(str2) : str1.equals(str2));
    }

}
