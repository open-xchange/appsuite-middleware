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

package com.openexchange.pop3.util;

import com.planetj.math.rabinhash.RabinHashFunction64;

/**
 * {@link UIDUtil} - UID utility class for POP3 bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UIDUtil {

    /**
     * Initializes a new {@link UIDUtil}.
     */
    private UIDUtil() {
        super();
    }

    /**
     * Computes the Rabin hash value of specified UID string.
     *
     * @param uidl The UID string as received from POP3 UIDL command
     * @return The Rabin hash value
     */
    public static long uid2long(final String uidl) {
        return RabinHashFunction64.DEFAULT_HASH_FUNCTION.hash(uidl);
    }

    /**
     * Computes the Rabin hash value of specified UID strings.
     *
     * @param uidls The UID strings as received from POP3 UIDL command
     * @return The Rabin hash values
     */
    public static long[] uid2long(final String[] uidls) {
        if (null == uidls) {
            return null;
        }
        final long[] longs = new long[uidls.length];
        for (int i = 0; i < longs.length; i++) {
            longs[i] = uid2long(uidls[i]);
        }
        return longs;
    }

}
