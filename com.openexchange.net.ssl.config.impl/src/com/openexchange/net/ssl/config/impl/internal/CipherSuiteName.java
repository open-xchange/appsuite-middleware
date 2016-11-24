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

package com.openexchange.net.ssl.config.impl.internal;

import com.openexchange.java.Strings;

/**
 * {@link CipherSuiteName} - A cipher suite name holding a unified and JRE-specific name.
 * <p>
 * Example for <code>DHE-RSA with AES 128 CBC SHA</code>:
 * <p>
 * <table>
 * <tr><th>Java vendor</th><th>Specific name</th><th>Unified name</th></tr>
 * <tr><td>Oracle Java:&nbsp;</td><td><code>"TLS_DHE_RSA_WITH_AES_128_CBC_SHA"</code></td><td><code>"DHE_RSA_WITH_AES_128_CBC_SHA"</code></td></tr>
 * <tr><td>IBM Java:&nbsp;</td><td><code>"SSL_DHE_RSA_WITH_AES_128_CBC_SHA"</code></td><td><code>"DHE_RSA_WITH_AES_128_CBC_SHA"</code></td></tr>
 * </table>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
final class CipherSuiteName {

    /**
     * Gets the <code>CipherSuiteName</code> for given cipher suite name string.
     *
     * @param cipherSuiteName The cipher suite name string
     * @return The <code>CipherSuiteName</code> instance
     */
    static CipherSuiteName nameFor(String cipherSuiteName) {
        return null == cipherSuiteName ? null : new CipherSuiteName(cipherSuiteName);
    }

    // -----------------------------------------------------------------------------------------

    private final String originalName;
    private final String unifiedName;
    private final int hash;

    CipherSuiteName(String originalName) {
        super();
        this.originalName = originalName;
        String unifiedName = Strings.toUpperCase(originalName);
        if (unifiedName.startsWith("SSL_") || unifiedName.startsWith("TLS_")) {
            this.unifiedName = unifiedName.substring(4);
        } else {
            this.unifiedName = unifiedName;
        }
        hash = 31 * 1 + unifiedName.hashCode();
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CipherSuiteName other = (CipherSuiteName) obj;
        if (unifiedName == null) {
            if (other.unifiedName != null) {
                return false;
            }
        } else if (!unifiedName.equals(other.unifiedName)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        if (unifiedName != null) {
            builder.append(unifiedName).append(", ");
        }
        if (originalName != null) {
            builder.append(originalName);
        }
        builder.append("]");
        return builder.toString();
    }
}