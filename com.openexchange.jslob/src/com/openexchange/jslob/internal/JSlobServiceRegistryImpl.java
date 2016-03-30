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

package com.openexchange.jslob.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.exception.OXException;
import com.openexchange.jslob.JSlobExceptionCodes;
import com.openexchange.jslob.JSlobService;
import com.openexchange.jslob.registry.JSlobServiceRegistry;

/**
 * {@link JSlobServiceRegistryImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class JSlobServiceRegistryImpl implements JSlobServiceRegistry {

    private static final JSlobServiceRegistryImpl INSTANCE = new JSlobServiceRegistryImpl();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static JSlobServiceRegistryImpl getInstance() {
        return INSTANCE;
    }

    private final ConcurrentMap<String, JSlobService> registry;

    /**
     * Initializes a new {@link JSlobServiceRegistryImpl}.
     */
    private JSlobServiceRegistryImpl() {
        super();
        registry = new ConcurrentHashMap<String, JSlobService>(2, 0.9f, 1);
    }

    @Override
    public JSlobService getJSlobService(final String serviceId) throws OXException {
        final JSlobService service = registry.get(serviceId);
        if (null == service) {
            throw JSlobExceptionCodes.NOT_FOUND.create(serviceId);
        }
        return service;
    }

    @Override
    public JSlobService optJSlobService(final String serviceId) throws OXException {
        return registry.get(serviceId);
    }

    @Override
    public Collection<JSlobService> getJSlobServices() throws OXException {
        final List<JSlobService> list = new ArrayList<JSlobService>(8);
        list.addAll(registry.values());
        return list;
    }

    @Override
    public boolean putJSlobService(final JSlobService jslobService) {
        final Set<String> keys = new HashSet<String>();
        if (null != registry.putIfAbsent(jslobService.getIdentifier(), jslobService)) {
            return false;
        }
        /*
         * Add aliases, too
         */
        keys.add(jslobService.getIdentifier());
        final List<String> aliases = jslobService.getAliases();
        if (null != aliases && !aliases.isEmpty()) {
            for (final String alias : aliases) {
                if (null != registry.putIfAbsent(alias, jslobService)) {
                    /*
                     * Clean-up all keys
                     */
                    for (final String key : keys) {
                        registry.remove(key);
                    }
                    return false;
                }
                keys.add(alias);
            }
        }
        return true;
    }

    @Override
    public void removeJSlobService(final JSlobService jslobService) throws OXException {
        registry.remove(jslobService.getIdentifier());
        /*
         * Remove aliases, too
         */
        final List<String> aliases = jslobService.getAliases();
        if (null != aliases && !aliases.isEmpty()) {
            for (final String alias : aliases) {
                registry.remove(alias);
            }
        }
    }

}
