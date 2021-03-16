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

package com.openexchange.groupware.update.internal;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import com.google.common.collect.ImmutableSet;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.config.lean.Property;
import com.openexchange.java.Strings;

/**
 * {@link NamespaceAwareExcludedSet} - This class contains the list of excluded update tasks.
 * The configuration can be done by the property <code>com.openexchange.groupware.update.excludedUpdateTasks</code>.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
public class NamespaceAwareExcludedSet implements UpdateTaskSet<String> {

    /** The property providing the namespaces of the updates tasks that are supposed to be excluded */
    static final Property PROPERTY = com.openexchange.groupware.update.UpdateProperty.EXCLUDED_UPDATE_TASKS;

    private static final NamespaceAwareExcludedSet SINGLETON = new NamespaceAwareExcludedSet();

    /**
     * Gets the singleton instance
     *
     * @return The instance
     */
    public static NamespaceAwareExcludedSet getInstance() {
        return SINGLETON;
    }

    // -----------------------------------------------------------------------------------------------------------

    private final AtomicReference<Set<String>> excludedNamespacesRef = new AtomicReference<Set<String>>(Collections.emptySet());

    /**
     * Initializes a new {@link NamespaceAwareExcludedSet}.
     */
    private NamespaceAwareExcludedSet() {
        super();
    }

    /**
     * Loads & parses the <code>"com.openexchange.groupware.update.excludedUpdateTasks"</code> property.
     *
     * @param leanConfig The {@link LeanConfigurationService} to load the property
     */
    public void loadExcludedNamespaces(LeanConfigurationService leanConfig) {
        String namespaces = leanConfig.getProperty(PROPERTY);
        if (Strings.isEmpty(namespaces)) {
            return;
        }

        String[] split = Strings.splitByComma(namespaces);
        if (split == null) {
            return;
        }

        ImmutableSet.Builder<String> en = ImmutableSet.builderWithExpectedSize(split.length);
        for (String namespace : split) {
            en.add(namespace);
        }
        excludedNamespacesRef.set(en.build());
        UpdateTaskCollection.getInstance().dirtyVersion();
    }

    @Override
    public Set<String> getTaskSet() {
        return excludedNamespacesRef.get();
    }

}
