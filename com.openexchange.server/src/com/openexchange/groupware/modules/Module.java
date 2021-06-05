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

package com.openexchange.groupware.modules;

import java.util.HashMap;
import java.util.Map;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.userconfiguration.Permission;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * {@link Module} - A module known to Open-Xchange Server.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum Module {
    TASK("tasks", FolderObject.TASK, Permission.TASKS),
    CALENDAR("calendar", FolderObject.CALENDAR, Permission.CALENDAR),
    CONTACTS("contacts", FolderObject.CONTACT, Permission.CONTACTS),
    UNBOUND("unbound", FolderObject.UNBOUND, null),
    MAIL("mail", FolderObject.MAIL, Permission.WEBMAIL),
    INFOSTORE("infostore", FolderObject.INFOSTORE, Permission.INFOSTORE),
    FILES("files", FolderObject.INFOSTORE, Permission.INFOSTORE),
    SYSTEM("system", FolderObject.SYSTEM_MODULE, null);

    private final String name;
    private final int folderConstant;
    private final Permission permission;

    Module(String name, int folderConstant, Permission permission) {
        this.name = name;
        this.folderConstant = folderConstant;
        this.permission = permission;
    }

    private static final TIntObjectMap<Module> folderConstant2Module;
    private static final TObjectIntMap<String> string2FolderConstant;
    private static final Map<String, Module> name2Module;
    static {
        final Module[] values = values();
        final TIntObjectMap<Module> map1 = new TIntObjectHashMap<Module>(values.length);
        final TObjectIntMap<String> map2 = new TObjectIntHashMap<String>(values.length, 0.5f, -1);
        Map<String, Module> map3 = new HashMap<String, Module>();
        for (final Module module : values) {
            map1.put(module.folderConstant, module);
            map2.put(module.name, module.folderConstant);
            map3.put(module.name, module);
        }
        folderConstant2Module = map1;
        string2FolderConstant = map2;
        name2Module = map3;
    }

    /**
     * Gets the module for given numeric identifier.
     *
     * @param constant The numeric identifier
     * @return The module or <code>null</code>
     */
    public static Module getForFolderConstant(int constant) {
        return folderConstant2Module.get(constant);
    }

    /**
     * Gets the module for the given name.
     *
     * @param name The name, never <code>null</code>
     * @return The module or <code>null</code>, if unknown
     */
    public static Module getForName(String name) {
        return name2Module.get(name);
    }

    /**
     * Gets the module string.
     *
     * @return The module string
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the module's numeric identifier
     *
     * @return The numeric identifier
     */
    public int getFolderConstant() {
        return folderConstant;
    }

    /**
     * Gets the {@link Permission} according to this module
     *
     * @return The permission or <code>null</code> if there is none for this module
     */
    public Permission getPermission() {
        return permission;
    }

    /**
     * Gets the module string.
     *
     * @param module The module identifier
     * @param folderId The folder identifier
     * @return The module string or an empty string if unknown
     */
    public static final String getModuleString(final int module, final int folderId) {
        String moduleStr = null;
        switch (module) {
        case FolderObject.TASK:
            moduleStr = TASK.getName();
            break;
        case FolderObject.CONTACT:
            moduleStr = CONTACTS.getName();
            break;
        case FolderObject.CALENDAR:
            moduleStr = CALENDAR.getName();
            break;
        case FolderObject.UNBOUND:
            moduleStr = UNBOUND.getName();
            break;
        case FolderObject.MAIL:
            moduleStr = MAIL.getName();
            break;
        case FolderObject.INFOSTORE:
            moduleStr = INFOSTORE.getName();
            break;
        case FolderObject.SYSTEM_MODULE:
            if (folderId == FolderObject.SYSTEM_INFOSTORE_FOLDER_ID) {
                moduleStr = INFOSTORE.getName();
            } else {
                moduleStr = SYSTEM.getName();
            }
            break;
        default:
            moduleStr = "";
            break;
        }
        return moduleStr;
    }

    /**
     * Gets the module's numeric identifier.
     *
     * @param module The module string
     * @return The module's numeric identifier or <code>-1</code> if unknown
     */
    public static final int getModuleInteger(final String moduleStr) {
        return null == moduleStr ? -1 : string2FolderConstant.get(moduleStr);
    }
}
