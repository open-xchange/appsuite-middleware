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

package com.openexchange.share.impl.groupware;

import java.util.Collection;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.ServiceSet;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.groupware.spi.ModuleExtension;

/**
 * {@link ModuleExtensionRegistry}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ModuleExtensionRegistry<T extends ModuleExtension> {

    private final ServiceSet<T> extensions;

    /**
     * Initializes a new {@link ModuleExtensionRegistry}.
     *
     * @param extensions A service set holding the known module extensions
     */
    public ModuleExtensionRegistry(ServiceSet<T> extensions) {
        super();
        this.extensions = extensions;
    }

    /**
     * Gets the extension being responsible for a specific module, throwing an exception if there is none registered.
     *
     * @param module The module to get the extension for
     * @return The module extension
     */
    public T get(int module) throws OXException {
        T handler = opt(module);
        if (null == handler) {
            String name = ShareModuleMapping.moduleMapping2String(module);
            throw ShareExceptionCodes.SHARING_NOT_SUPPORTED.create(null == name ? Integer.toString(module) : name);
        }
        return handler;
    }

    /**
     * Optionally gets the extension being responsible for a specific module.
     *
     * @param module The module to get the extension for
     * @return The module extension, or <code>null</code> if there is none registered
     */
    public T opt(int module) {
        for (T moduleExtension : extensions) {
            if (supports(moduleExtension, module)) {
                return moduleExtension;
            }
        }
        return null;
    }

    private boolean supports(T moduleExtension, int module) {
        Collection<String> modules = moduleExtension.getModules();
        String name = ShareModuleMapping.moduleMapping2String(module);
        return null != name && null != modules && modules.contains(name);
    }

}
