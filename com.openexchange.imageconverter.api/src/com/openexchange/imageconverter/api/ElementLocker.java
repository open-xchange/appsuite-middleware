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

package com.openexchange.imageconverter.api;

import java.util.HashMap;
import java.util.Map;
import com.openexchange.imageconverter.api.ElementLock.LockMode;
import com.openexchange.imageconverter.api.ElementLock.UnlockMode;

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

        if (null != element) {
            ElementLock elementLock = null;

            synchronized (m_elementLockMap) {
                elementLock = m_elementLockMap.get(element);

                if (null == elementLock) {
                    m_elementLockMap.put(element, elementLock = new ElementLock());
                } else {
                    elementLock.incrementUseCount();
                }
            }

            ret = elementLock.lock(lockMode);

            // cleaning up in case of unsuccessful try lock
            if (!ret) {
                // remove element only from map, if no one else
                // holds the lock and no processing is happening
                synchronized (m_elementLockMap) {
                    if ((0 == elementLock.decrementUseCount()) && !elementLock.isProcessing()) {
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
        unlock(element, UnlockMode.STANDARD);
    }

    /**
     * @param element
     * @param finishProcessing
     */
    public static void unlock(final String element, final UnlockMode unlockMode) {
        if (null != element) {
            synchronized (m_elementLockMap) {
                final ElementLock elementLock = m_elementLockMap.get(element);

                if (null != elementLock) {
                    elementLock.unlock(unlockMode);

                    if ((0 == elementLock.decrementUseCount()) && !elementLock.isProcessing()) {
                        m_elementLockMap.remove(element);
                    }
                }
            }
        }
    }

    // - Members ---------------------------------------------------------------

    // m_elementLockMap needs to be synchronized with every access (intended)
    private static Map<String, ElementLock> m_elementLockMap = new HashMap<>();
}
