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

package com.openexchange.mail.search;

import javax.mail.internet.AddressException;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.QuotedInternetAddress;

/**
 * {@link AbstractAddressTerm}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractAddressTerm extends SearchTerm<String> {

    private static final long serialVersionUID = -1266840509034246826L;

    /** The address to search for */
    protected final String addr;
    private String lowerCaseAddr;

    /**
     * Initializes a new {@link AbstractAddressTerm}
     */
    protected AbstractAddressTerm(String pattern) {
        super();
        String addr;
        try {
            addr = new QuotedInternetAddress(pattern, false).getUnicodeAddress();
        } catch (AddressException e) {
            addr = pattern;
        }
        this.addr = addr;
    }

    /**
     * Gets the ASCII lower-case representation of the pattern.
     *
     * @return The ASCII lower-case representation of the pattern
     */
    protected String getLowerCaseAddr() {
        String s = lowerCaseAddr;
        if (null == s) {
            s = Strings.asciiLowerCase(addr);
            lowerCaseAddr = s;
        }
        return s;
    }

    @Override
    public String getPattern() {
        return addr;
    }

    @Override
    public boolean isAscii() {
        return isAscii(addr);
    }

    @Override
    public boolean containsWildcard() {
        return null == addr ? false : addr.indexOf('*') >= 0 || addr.indexOf('?') >= 0;
    }
}
