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

package com.openexchange.filestore.swift.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * {@link EndpointFactory} - A factory for {@link Endpoint} instances, which manages associated tenant-scoped locks.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class EndpointFactory {

    private static final EndpointFactory INSTANCE = new EndpointFactory();

    /**
     * Gets the singleton instance
     *
     * @return The singleton instance
     */
    public static EndpointFactory getInstance() {
        return INSTANCE;
    }

    // ----------------------------------------------------------------------------------------------------------------

    private final ConcurrentMap<String, Object> locks;

    private EndpointFactory() {
        super();
        locks = new ConcurrentHashMap<>(16, 0.9F, 1);
    }

    /**
     * Creates an end-point for specified URI; including version, tenant and container, e.g. <code>"https://swift.store.invalid/v1/CloudFS_123456/MyContainer"</code>.
     *
     * @param endpointUri The end-point URI
     * @return The created end-point instance
     */
    public Endpoint createEndpointFor(String endpointUri) {
        // Extract base URI
        int pos = endpointUri.lastIndexOf('/');
        String baseUri = endpointUri.substring(0, pos);

        // Get tenant-associated lock for this JVM
        Object lock = locks.get(baseUri);
        if (null == lock) {
            Object newLock = new Object();
            lock = locks.putIfAbsent(baseUri, newLock);
            if (null == lock) {
                lock = newLock;
            }
        }

        // Extract container name
        String containerName = endpointUri.substring(pos + 1);

        // Create & return end-point
        return new Endpoint(baseUri, containerName, lock);
    }

}
