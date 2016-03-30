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

package com.openexchange.group.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.groupware.i18n.Groups;
import com.openexchange.i18n.I18nService;

/**
 * {@link GroupI18nNamesService} - Provides the localized group names.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class GroupI18nNamesService {

    private static final GroupI18nNamesService INSTANCE = new GroupI18nNamesService();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static GroupI18nNamesService getInstance() {
        return INSTANCE;
    }

    // -----------------------------------------------------------------------------------------------------------------

    private final Map<String, String> i18nNames;
    private final Map<Locale, I18nService> services;
    private final Set<String> identifiers;

    /**
     * Initializes a new {@link GroupI18nNamesService}.
     */
    private GroupI18nNamesService() {
        super();
        services = new ConcurrentHashMap<Locale, I18nService>();
        identifiers = new HashSet<String>(4);
        i18nNames = new ConcurrentHashMap<String, String>(24, 0.9f, 1);
        initIdentifiers();
    }

    private void initIdentifiers() {
        // Mail strings
        identifiers.add(Groups.ALL_USERS);
        identifiers.add(Groups.STANDARD_GROUP);
    }

    /**
     * Adds specified service.
     *
     * @param service The service to add
     */
    public void addService(final I18nService service) {
        services.put(service.getLocale(), service);
        i18nNames.clear();
    }

    /**
     * Removes specified service.
     *
     * @param service The service to remove
     */
    public void removeService(final I18nService service) {
        services.remove(service.getLocale());
        i18nNames.clear();
    }

    /**
     * Gets the i18n names for groups.
     *
     * @return The i18n names for groups
     */
    public Set<String> getI18nNames() {
        if (i18nNames.isEmpty()) {
            synchronized (services) {
                if (i18nNames.isEmpty()) {
                    // Insert as-is for default locale
                    for (final String identifier : identifiers) {
                        i18nNames.put(identifier, identifier);
                    }
                    // Insert translated names
                    for (final I18nService service : services.values()) {
                        for (final String identifier : identifiers) {
                            final String translated = service.getLocalized(identifier);
                            i18nNames.put(translated, translated);
                        }
                    }
                }
            }
        }
        return Collections.unmodifiableSet(i18nNames.keySet());
    }

}
