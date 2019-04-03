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

package com.openexchange.imageconverter.api;

import static com.openexchange.java.Strings.isNotEmpty;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.imageconverter.api.ElementLock.LockMode;

/**
 * {@link ElementLocker}
 *
 * @author <a href="mailto:kai.ahrens@open-xchange.com">Kai Ahrens</a>
 * @since v7.10.0
 */
public class ElementLocker {

    /**
     * @param element
     */
    public static boolean lock(final String element) {
        return lock(element, LockMode.STANDARD);
    }

    /**
     * @param element
     * @param lockMode
     * @return
     */
    public static boolean lock(final String element, final LockMode lockMode) {
        boolean ret = false;

        if (isNotEmpty(element)) {
            ElementLock elementLock = null;

            synchronized (m_elementLockMap) {
                elementLock = m_elementLockMap.get(element);

                if (null == elementLock) {
                    m_elementLockMap.put(element, elementLock = new ElementLock());
                }
            }

            ret = elementLock.lock(lockMode);

            // cleaning up in case of unsuccessful try lock
            if (!ret && (LockMode.TRY_LOCK == lockMode)) {
                // remove element only from map, if no one else
                // holds the lock and no processing is happening
                synchronized (m_elementLockMap) {
                    if ((0 == elementLock.getUseCount()) && !elementLock.isProcessing()) {
                        m_elementLockMap.remove(element);
                    }
                }
            }

        }

        return ret;
    }

    /**
     * @param element
     */
    public static void unlock(final String element) {
        unlock(element, false);
    }

    /**
     * @param element
     * @param finishProcessing
     */
    public static void unlock(final String element, final boolean finishProcessing) {
        if (isNotEmpty(element)) {
            synchronized (m_elementLockMap) {
                final ElementLock keyLock = m_elementLockMap.get(element);

                if (null != keyLock) {
                    if ((0 == keyLock.unlockAndGetUseCount(finishProcessing)) && !keyLock.isProcessing()) {
                        m_elementLockMap.remove(element);
                    }
                }
            }
        }
    }

    // - Members ---------------------------------------------------------------

    private static Map<String, ElementLock> m_elementLockMap = new HashMap<>();
}
