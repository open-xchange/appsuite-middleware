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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.groupware.modules;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link Module} - A module known to Open-Xchange Server.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum Module {
    TASK("tasks", FolderObject.TASK),
    CALENDAR("calendar", FolderObject.CALENDAR),
    CONTACTS("contacts", FolderObject.CONTACT),
    UNBOUND("unbound", FolderObject.UNBOUND),
    MAIL("mail", FolderObject.MAIL),
    INFOSTORE("infostore", FolderObject.INFOSTORE),
    SYSTEM("system", FolderObject.SYSTEM_MODULE);

    private final String name;
    private final int folderConstant;

    Module(String name, int folderConstant) {
        this.name = name;
        this.folderConstant = folderConstant;
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
