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
        this.unifiedName = (unifiedName.startsWith("SSL_") || unifiedName.startsWith("TLS_")) ? unifiedName.substring(4) : unifiedName;
        hash = 31 * 1 + this.unifiedName.hashCode();
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
        StringBuilder builder = new StringBuilder(64);
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