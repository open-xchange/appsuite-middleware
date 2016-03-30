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
        } catch (final ClassCastException e) {
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
