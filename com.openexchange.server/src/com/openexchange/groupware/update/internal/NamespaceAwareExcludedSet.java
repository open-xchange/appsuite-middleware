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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.groupware.update.internal;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import com.google.common.collect.ImmutableSet;
import com.openexchange.config.lean.DefaultProperty;
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
    static final Property PROPERTY = DefaultProperty.valueOf("com.openexchange.groupware.update.excludedUpdateTasks", "");

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
