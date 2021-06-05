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
