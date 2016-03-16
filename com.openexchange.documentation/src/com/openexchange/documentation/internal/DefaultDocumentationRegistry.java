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

package com.openexchange.documentation.internal;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.documentation.DocumentationRegistry;
import com.openexchange.documentation.descriptions.ContainerDescription;
import com.openexchange.documentation.descriptions.ModuleDescription;
import com.openexchange.exception.OXException;

/**
 * {@link DefaultDocumentationRegistry} - The default documentation registry.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DefaultDocumentationRegistry implements DocumentationRegistry {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultDocumentationRegistry.class);

    private final Map<String, ModuleDescription> modules;
    private final Map<String, ContainerDescription> containers;

    /**
     * Initializes a new {@link DefaultDocumentationRegistry}.
     */
    public DefaultDocumentationRegistry() {
        super();
        this.modules = new ConcurrentHashMap<String, ModuleDescription>();
        this.containers = new ConcurrentHashMap<String, ContainerDescription>();
    }

    @Override
	public Collection<ModuleDescription> getModules() throws OXException {
        return this.modules.values();
    }

    @Override
	public ModuleDescription getModule(final String name) throws OXException {
        if (false == this.modules.containsKey(name)) {
            throw DocumentationExceptionCode.MODULE_NOT_REGISTERED.create(name);
        }
        return modules.get(name);
    }

    @Override
	public Collection<ContainerDescription> getContainers() throws OXException {
        return this.containers.values();
    }

    @Override
	public ContainerDescription getContainer(final String name) throws OXException {
        if (false == this.containers.containsKey(name)) {
            throw DocumentationExceptionCode.CONTAINER_NOT_REGISTERED.create(name);
        }
        return containers.get(name);
    }

    /**
     * Registers a new module description using the module's name.
     *
     * @param module the module description to add
     */
    public void addModule(final ModuleDescription module) throws OXException {
        if (null == module || null == module.getName()) {
            throw DocumentationExceptionCode.MODULE_NAME_MISSING.create();
        }
        if (this.modules.containsKey(module.getName())) {
            throw DocumentationExceptionCode.MODULE_ALREADY_REGISTERED.create(module.getName());
        }
        LOG.debug("Adding module: {}", module.getName());
        this.modules.put(module.getName(), module);
        final ContainerDescription[] containers = module.getContainers();
        if (null != containers) {
            for (final ContainerDescription container : containers) {
                if (null == container || null == container.getName()) {
                    throw DocumentationExceptionCode.CONTAINER_NAME_MISSING.create();
                } else if (this.containers.containsKey(container.getName())) {
                    throw DocumentationExceptionCode.CONTAINER_ALREADY_REGISTERED.create(container.getName());
                }
                this.containers.put(container.getName(), container);
            }
        }
    }

    /**
     * Removes a module description from the registry.
     *
     * @param name the name of the module description to remove
     */
    public void removeModule(final String name) throws OXException {
        if (null == name || false == modules.containsKey(name)) {
            throw DocumentationExceptionCode.MODULE_NOT_REGISTERED.create(name);
        }
        final ModuleDescription removed = modules.remove(name);
        final ContainerDescription[] containers = removed.getContainers();
        if (null != containers) {
        	for (final ContainerDescription container : containers) {
        		this.containers.remove(container.getName());
        	}
        }
    }

}
