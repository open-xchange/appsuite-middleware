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
    protected AbstractAddressTerm(final String pattern) {
        super();
        String addr;
        try {
            addr = new QuotedInternetAddress(pattern, false).getUnicodeAddress();
        } catch (final AddressException e) {
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
