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

package com.openexchange.folder.json;

import com.openexchange.ajax.customizer.folder.AdditionalFolderFieldList;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.folder.json.services.ServiceRegistry;

/**
 * {@link Constants} for the HTTP JSON interface of the folder component.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Constants {

    /**
     * The folder module identifier.
     */
    private static volatile String module = "folders";

    /**
     * The folder servlet path.
     */
    private static volatile String servletPath = DispatcherPrefixService.DEFAULT_PREFIX + module;

    /**
     * The list for additional folder fields.
     */
    public static final AdditionalFolderFieldList ADDITIONAL_FOLDER_FIELD_LIST = new AdditionalFolderFieldList();

    /**
     * No instantiation.
     */
    private Constants() {
        super();
    }

    /**
     * Gets the module string.
     *
     * @return The module string
     */
    public static String getModule() {
        return module;
    }

    /**
     * Sets the module string.
     *
     * @param module The module string
     */
    public static void setModule(final String module) {
        final String m = null == module ? Constants.module : module;
        Constants.module = m;
        Constants.servletPath = ServiceRegistry.getInstance().getService(DispatcherPrefixService.class).getPrefix() + m;
    }

    /**
     * Gets the servlet path.
     *
     * @return The servlet path
     */
    public static String getServletPath() {
        return servletPath;
    }

    /**
     * Sets the servlet path.
     *
     * @param servletPath The servlet path
     */
    public static void setServletPath(final String servletPath) {
        Constants.servletPath = servletPath;
    }

}
