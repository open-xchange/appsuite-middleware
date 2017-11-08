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
