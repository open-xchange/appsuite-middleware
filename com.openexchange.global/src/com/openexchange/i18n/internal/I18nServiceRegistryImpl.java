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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.i18n.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.exception.OXException;
import com.openexchange.i18n.I18nService;
import com.openexchange.i18n.I18nServiceRegistry;


/**
 * {@link I18nServiceRegistryImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class I18nServiceRegistryImpl implements I18nServiceRegistry {

    private final ConcurrentMap<Locale, I18nService> services;

    /**
     * Initializes a new {@link I18nServiceRegistryImpl}.
     */
    public I18nServiceRegistryImpl() {
        super();
        services = new ConcurrentHashMap<Locale, I18nService>(32, 0.9F, 1);
    }

    /**
     * Adds specified i18n service to this registry.
     *
     * @param service The service to add
     * @return <code>true</code> if successfully added; otherwise <code>false</code>
     */
    public boolean addI18nService(I18nService service) {
        return null != service && null == services.putIfAbsent(service.getLocale(), service);
    }

    /**
     * Removes specified i18n service from this registry.
     *
     * @param service The service to remove
     * @return <code>true</code> if successfully removed; otherwise <code>false</code>
     */
    public boolean removeI18nService(I18nService service) {
        return null != service && null != services.remove(service.getLocale());
    }

    @Override
    public Collection<I18nService> getI18nServices() throws OXException {
        return Collections.unmodifiableCollection(services.values());
    }

    @Override
    public I18nService getI18nService(Locale locale) throws OXException {
        return null == locale ? null : services.get(locale);
    }

    @Override
    public I18nService getBestFittingI18nService(Locale locale) throws OXException {
        if (null == locale) {
            // Garbage in, garbage out...
            return null;
        }

        Map<Locale, I18nService> services = new HashMap<>(this.services);
        I18nService service = services.get(locale);
        if (null != service) {
            return service;
        }

        String language = locale.getLanguage();
        if (null == language) {
            // Huh...?
            return null;
        }

        I18nService firstMatch = null;
        for (Map.Entry<Locale, I18nService> entry : services.entrySet()) {
            Locale loc = entry.getKey();
            if (language.equals(loc.getLanguage())) {
                // Does language match country part?
                if (language.equalsIgnoreCase(loc.getCountry())) {
                    return entry.getValue();
                }

                // Is it the first candidate with equal language?
                if (null == firstMatch) {
                    firstMatch = entry.getValue();
                }
            }
        }
        return firstMatch;
    }

}
