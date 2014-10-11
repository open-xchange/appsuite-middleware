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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.tx;

import java.util.HashMap;
import java.util.Map;


/**
 * {@link TransactionState}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class TransactionState {

    private static final ThreadLocal<Map<Object, Object>> PARAMETERS = new ThreadLocal<Map<Object, Object>>();

    public static final String WRITE_CON = "dbcon_writeable";

    public static final String READ_CON = "dbcon_readonly";

    public static void init() {
        Map<Object, Object> existing = PARAMETERS.get();
        if (existing != null) {
            throw new IllegalStateException("TransactionState is already initialized!");
        }

        PARAMETERS.set(new HashMap<Object, Object>());
    }

    public static boolean isInitialized() {
        Map<Object, Object> existing = PARAMETERS.get();
        return existing != null;
    }

    public static void put(Object key, Object value) {
        checkAndGetMap().put(key, value);
    }

    public static Object get(Object key) {
        return checkAndGetMap().get(key);
    }

    public static boolean containsKey(Object key) {
        return checkAndGetMap().containsKey(key);
    }

    public static void remove(Object key) {
        checkAndGetMap().remove(key);
    }

    public static void cleanUp() {
        Map<Object, Object> existing = PARAMETERS.get();
        if (existing == null) {
            throw new IllegalStateException("TransactionState was not initialized!");
        }

        PARAMETERS.remove();
    }

    private static Map<Object, Object> checkAndGetMap() {
        Map<Object, Object> existing = PARAMETERS.get();
        if (existing == null) {
            throw new IllegalStateException("TransactionState was not initialized!");
        }

        return existing;
    }

}
