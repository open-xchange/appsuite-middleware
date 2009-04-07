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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.pop3.uid;

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
     * @param uid The UID string
     * @return The Rabin hash value
     */
    public static long uid2long(final String uid) {
        return RabinHashFunction64.DEFAULT_HASH_FUNCTION.hash(uid);
    }

    public static String long2uid(final long uid) {
        return "";
    }
    
    public static String[] longs2uids(final long[] uids) {
        final String[] retval = new String[uids.length];
        for (int i = 0; i < retval.length; i++) {
            retval[i] = long2uid(uids[i]);
        }
        return retval;
    }

    public static void main(final String[] args) {
        System.out.println(uid2long("ab"));
        System.out.println(uid2long("aba"));

        System.out.println(uid2long("a"));
        System.out.println(uid2long("z"));
    }
}
