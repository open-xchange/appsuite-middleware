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

package com.openexchange.pop3.storage.mailaccount;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.exception.OXException;
import com.openexchange.pop3.POP3Access;
import com.openexchange.pop3.storage.CASPOP3StorageProperties;
import com.openexchange.pop3.storage.POP3StorageProperties;
import com.openexchange.session.Session;

/**
 * {@link CASSessionPOP3StorageProperties} - Session-backed implementation of {@link POP3StorageProperties}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CASSessionPOP3StorageProperties implements CASPOP3StorageProperties {

    /**
     * Gets the storage properties bound to specified POP3 access.
     *
     * @param pop3Access The POP3 access
     * @return The storage properties bound to specified POP3 access
     */
    public static CASSessionPOP3StorageProperties getInstance(final POP3Access pop3Access) {
        final Session session = pop3Access.getSession();
        final String key = SessionParameterNames.getStorageProperties(pop3Access.getAccountId());
        CASSessionPOP3StorageProperties cached;
        try {
            cached = (CASSessionPOP3StorageProperties) session.getParameter(key);
        } catch (ClassCastException e) {
            cached = null;
        }
        if (null == cached) {
            cached = new CASSessionPOP3StorageProperties(new RdbPOP3StorageProperties(pop3Access));
            session.setParameter(key, cached);
        }
        return cached;
    }

    /*-
     * Member section
     */

    private final ConcurrentMap<String, AtomicReference<String>> map;

    private final POP3StorageProperties delegatee;

    /**
     * Initializes a new {@link CASSessionPOP3StorageProperties}.
     */
    private CASSessionPOP3StorageProperties(final POP3StorageProperties delegatee) {
        super();
        this.delegatee = delegatee;
        map = new ConcurrentHashMap<String, AtomicReference<String>>();
    }

    @Override
    public void addProperty(final String propertyName, final String propertyValue) throws OXException {
        final AtomicReference<String> ref = putIfAbsent(propertyName);
        ref.set(propertyValue);
        delegatee.addProperty(propertyName, propertyValue);
    }

    @Override
    public boolean compareAndSetProperty(final String propertyName, final String expectedPropertyValue, final String newPropertyValue) throws OXException {
        final AtomicReference<String> ref = putIfAbsent(propertyName);
        final boolean success = ref.compareAndSet(expectedPropertyValue, newPropertyValue);
        if (success) {
            delegatee.addProperty(propertyName, newPropertyValue);
        }
        return success;
    }

    private AtomicReference<String> putIfAbsent(final String propertyName) {
        AtomicReference<String> ref = map.get(propertyName);
        if (null == ref) {
            ref = new AtomicReference<String>();
            final AtomicReference<String> prev = map.putIfAbsent(propertyName, ref);
            if (prev != null) {
                ref = prev;
            }
        }
        return ref;
    }

    @Override
    public String getProperty(final String propertyName) throws OXException {
        if (map.containsKey(propertyName)) {
            return map.get(propertyName).get();
        }
        final String value = delegatee.getProperty(propertyName);
        if (null != value) {
            final AtomicReference<String> ref = putIfAbsent(propertyName);
            ref.set(value);
        }
        return value;
    }

    @Override
    public void removeProperty(final String propertyName) throws OXException {
        map.remove(propertyName);
        delegatee.removeProperty(propertyName);
    }

}
