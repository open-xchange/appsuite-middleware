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

package com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.guipref;

import java.sql.Connection;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.AbstractAttributeChangers;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.EmptyAttribute;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.settings.impl.ConfigTree;
import com.openexchange.groupware.settings.impl.SettingStorage;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link GuiPreferenceUserAttributeChangers}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class GuiPreferenceUserAttributeChangers extends AbstractAttributeChangers {

    private static final Logger LOG = LoggerFactory.getLogger(GuiPreferenceUserAttributeChangers.class);

    /**
     * Initialises a new {@link GuiPreferenceUserAttributeChangers}.
     */
    public GuiPreferenceUserAttributeChangers() {
        super(EnumSet.noneOf(EmptyAttribute.class));
    }

    @Override
    public Set<String> change(User userData, int userId, int contextId, Connection connection, Collection<Runnable> pendingInvocations) throws StorageException {
        Map<String, String> guiPreferences = userData.getGuiPreferences();
        if (guiPreferences == null) {
            return EMPTY_SET;
        }

        // If administrator sets GUI configuration existing GUI configuration is overwritten
        Set<String> changedAttributes = new HashSet<>();
        Iterator<Entry<String, String>> iter = guiPreferences.entrySet().iterator();

        SettingStorage settingStorage = getSettingStorage(userId, contextId);
        while (iter.hasNext()) {
            Entry<String, String> entry = iter.next();
            String key = entry.getKey();
            String value = entry.getValue();
            if (null != key && null != value) {
                try {
                    Setting setting = ConfigTree.getInstance().getSettingByPath(key);
                    setting.setSingleValue(value);
                    settingStorage.save(connection, setting);
                    changedAttributes.add(key);
                } catch (OXException e) {
                    LOG.error("Problem while storing GUI preferences.", e);
                }
            }
        }
        return changedAttributes;
    }

    /**
     * Returns the {@link SettingStorage} instance for the specified user in the specified context
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return the {@link SettingStorage} instance for the specified user in the specified context
     * @throws StorageException if the {@link SettingStorage} instance cannot be initialised
     */
    private SettingStorage getSettingStorage(int userId, int contextId) throws StorageException {
        try {
            return SettingStorage.getInstance(ServerSessionAdapter.valueOf(userId, contextId));
        } catch (OXException e) {
            throw StorageException.wrapForRMI("Cannot initialise the SettingStorage for user '" + userId + "' in context '" + contextId + "'", e);
        }
    }
}
