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

package com.openexchange.chronos.provider.xctx;

import static com.openexchange.chronos.common.CalendarUtils.optExtendedPropertyValue;
import static com.openexchange.chronos.provider.CalendarFolderProperty.COLOR;
import static com.openexchange.chronos.provider.CalendarFolderProperty.COLOR_LITERAL;
import static com.openexchange.java.Autoboxing.B;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.ExtendedProperty;
import com.openexchange.chronos.provider.groupware.DefaultGroupwareCalendarFolder;
import com.openexchange.chronos.provider.groupware.GroupwareCalendarFolder;
import com.openexchange.chronos.provider.groupware.GroupwareFolderType;
import com.openexchange.groupware.EntityInfo;
import com.openexchange.java.Enums;

/**
 * {@link AccountConfigHelper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.5
 */
public class AccountConfigHelper {

    private static final Logger LOG = LoggerFactory.getLogger(AccountConfigHelper.class);

    private final JSONObject config;

    /**
     * Initializes a new {@link AccountConfigHelper}.
     *
     * @param config The underlying config object
     */
    public AccountConfigHelper(JSONObject config) {
        super();
        this.config = config;
    }

    public DefaultGroupwareCalendarFolder getRememberedCalendar(String folderId) {
        return deserializeFolder(getFolderConfig(folderId));
    }

    public List<DefaultGroupwareCalendarFolder> getRememberedCalendars() {
        return deserializeFolders(getFoldersConfig());
    }

    public ExtendedProperty getColor(String folderId) {
        JSONObject folderConfig = getFolderConfig(folderId);
        return COLOR(folderConfig.optString("color", null), false);
    }

    public boolean setColor(String folderId, ExtendedProperty value) {
        JSONObject folderConfig = getFolderConfig(folderId);
        if (false == Objects.equals(folderConfig.opt("color"), value.getValue())) {
            folderConfig.putSafe("color", value.getValue());
            return true;
        }
        return false;
    }

    public Boolean isSubscribed(String folderId) {
        JSONObject folderConfig = getFolderConfig(folderId);
        return folderConfig.hasAndNotNull("subscribed") ? B(folderConfig.optBoolean("subscribed")) : null;
    }

    public boolean setSubscribed(String folderId, Boolean value) {
        JSONObject folderConfig = getFolderConfig(folderId);
        if (null == value) {
            return null != folderConfig.remove("subscribed");
        }
        if (false == Objects.equals(folderConfig.opt("subscribed"), value)) {
            folderConfig.putSafe("subscribed", value);
            return true;
        }
        return false;
    }

    /**
     * Remembers the calendar folders for a certain folder type within the account configuration.
     * <p/>
     * Previously remembered folders of this type are purged implicitly, so that the passed collection of folders will effectively replace
     * the last known state for this folder type afterwards.
     *
     * @param calendarFolders The calendar folders to remember
     * @param type The folder type to remember the calendars for
     * @return <code>true</code> if the configuration was effectively changed by this operation, <code>false</code>, otherwise
     */
    public boolean rememberVisibleCalendars(List<GroupwareCalendarFolder> calendarFolders, GroupwareFolderType type) {
        /*
         * get or prepare "folders" configuration section
         */
        JSONObject foldersConfig = config.optJSONObject("folders");
        if (null == foldersConfig) {
            foldersConfig = new JSONObject();
            config.putSafe("folders", foldersConfig);
        }
        /*
         * add or update each folder in config as needed
         */
        boolean needsUpdate = false;
        Set<String> rememberedIds = new HashSet<String>(calendarFolders.size());
        for (GroupwareCalendarFolder calendarFolder : calendarFolders) {
            if (null == calendarFolder.getId()) {
                LOG.warn("Unable to remember calendar folder {} without id.", calendarFolder);
                continue;
            }
            rememberedIds.add(calendarFolder.getId());
            JSONObject folderConfig = serializeFolder(calendarFolder);
            if (false == Objects.equals(foldersConfig.optJSONObject(calendarFolder.getId()), folderConfig)) {
                foldersConfig.putSafe(calendarFolder.getId(), folderConfig);
                needsUpdate = true;
            }
        }
        /*
         * remove no longer indicated calendars of this type
         */
        List<String> idsToRemove = new ArrayList<String>();
        for (String folderId : foldersConfig.keySet()) {
            if (rememberedIds.contains(folderId)) {
                continue;
            }
            JSONObject folderConfig = foldersConfig.optJSONObject(folderId);
            if (null != folderConfig && type.equals(Enums.parse(GroupwareFolderType.class, folderConfig.optString("type", null), null))) {
                idsToRemove.add(folderId);
            }
        }
        for (String folderId : idsToRemove) {
            if (null != foldersConfig.remove(folderId)) {
                needsUpdate = true;
            }
        }
        return needsUpdate;
    }

    private JSONObject getFoldersConfig() {
        JSONObject foldersConfig = config.optJSONObject("folders");
        if (null == foldersConfig) {
            foldersConfig = new JSONObject();
            config.putSafe("folders", foldersConfig);
        }
        return foldersConfig;
    }

    private JSONObject getFolderConfig(String folderId) {
        JSONObject foldersConfig = getFoldersConfig();
        JSONObject folderConfig = foldersConfig.optJSONObject(folderId);
        if (null == folderConfig) {
            folderConfig = new JSONObject();
            foldersConfig.putSafe(folderId, folderConfig);
        }
        return folderConfig;
    }

    private static List<DefaultGroupwareCalendarFolder> deserializeFolders(JSONObject foldersConfig) {
        if (null == foldersConfig || foldersConfig.isEmpty()) {
            return Collections.emptyList();
        }
        List<DefaultGroupwareCalendarFolder> calendarFolders = new ArrayList<DefaultGroupwareCalendarFolder>(foldersConfig.length());
        for (String folderId : foldersConfig.keySet()) {
            JSONObject folderConfig = foldersConfig.optJSONObject(folderId);
            if (null != folderConfig) {
                calendarFolders.add(deserializeFolder(folderConfig));
            }
        }
        return calendarFolders;
    }

    private static DefaultGroupwareCalendarFolder deserializeFolder(JSONObject folderConfig) {
        if (null == folderConfig) {
            return null;
        }
        DefaultGroupwareCalendarFolder calendarFolder = new DefaultGroupwareCalendarFolder();
        calendarFolder.setId(folderConfig.optString("id", null));
        calendarFolder.setParentId(folderConfig.optString("parentId", null));
        calendarFolder.setName(folderConfig.optString("name", null));
        calendarFolder.setFolderType(Enums.parse(GroupwareFolderType.class, folderConfig.optString("type", null), null));
        calendarFolder.setSubscribed(folderConfig.hasAndNotNull("subscribed") ? B(folderConfig.optBoolean("subscribed")) : null);
        ExtendedProperties extendedProperties = new ExtendedProperties();
        extendedProperties.add(COLOR(folderConfig.optString("color", null), false));
        calendarFolder.setExtendedProperties(extendedProperties);
        calendarFolder.setCreatedFrom(folderConfig.hasAndNotNull("createdFrom") ? EntityInfo.parseJSON(folderConfig.optJSONObject("createdFrom")) : null);
        return calendarFolder;
    }

    private static JSONObject serializeFolder(GroupwareCalendarFolder calendarFolder) {
        if (null == calendarFolder) {
            return null;
        }
        return new JSONObject(8)
            .putSafe("id", calendarFolder.getId())
            .putSafe("parentId", calendarFolder.getParentId())
            .putSafe("name", calendarFolder.getName())
            .putSafe("type", null != calendarFolder.getType() ? calendarFolder.getType().name() : null)
            .putSafe("subscribed", calendarFolder.isSubscribed())
            .putSafe("color", optExtendedPropertyValue(calendarFolder.getExtendedProperties(), COLOR_LITERAL, String.class))
            .putSafe("createdFrom", null != calendarFolder.getCreatedFrom() ? calendarFolder.getCreatedFrom().toJSON() : null)
        ;
    }

}
