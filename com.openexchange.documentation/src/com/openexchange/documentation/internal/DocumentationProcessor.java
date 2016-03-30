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

import com.openexchange.documentation.AnnotatedServices;
import com.openexchange.documentation.DescriptionFactory;
import com.openexchange.documentation.descriptions.ModuleDescription;
import com.openexchange.exception.OXException;

/**
 * {@link DocumentationProcessor} - Adds or removes the documentation from service objects to/from the registry.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DocumentationProcessor {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DocumentationProcessor.class);

	private final DefaultDocumentationRegistry registry;
	private final DescriptionFactory factory;

    /**
     * Initializes a new {@link DocumentationProcessor}.
     */
    public DocumentationProcessor(final DefaultDocumentationRegistry registry, final DescriptionFactory factory) {
        super();
        this.registry = registry;
        this.factory = factory;
    }

	public void add(final ModuleDescription description) {
		try {
            this.registry.addModule(description);
        } catch (final OXException e) {
            LOG.error("error adding module description", e);
        }
	}

    /**
     * Processes the supplied service and adds the associated description to the registry.
     *
     * @param service the service to add
     */
	public void add(final Object service) {
	    if (AnnotatedServices.class.isInstance(service)) {
	        final DocumentationBuilder builder = new DocumentationBuilder(factory);
	        builder.add(service.getClass()).add(((AnnotatedServices)service).getSupportedServices());
	        if (builder.hasModule()) {
	            this.add(builder.getModuleDescription());
	        }
	    } else if (ModuleDescription.class.isInstance(service)) {
	        this.add(((ModuleDescription)service));
	    }
	}

	/**
     * Processes the supplied service and removes the associated description from the registry.
	 *
	 * @param service the service to remove
	 */
	public void remove(final Object service) {
       if (AnnotatedServices.class.isInstance(service)) {
            final DocumentationBuilder builder = new DocumentationBuilder(factory);
            builder.add(service.getClass());
            if (builder.hasModule()) {
                this.remove(builder.getModuleDescription());
            }
        } else if (ModuleDescription.class.isInstance(service)) {
            this.remove(((ModuleDescription)service));
        } else {
            LOG.warn("Unable to get extract documentation from {}", service);
        }
	}

	public void remove(final ModuleDescription description) {
		this.remove(description.getName());
	}

	private void remove(final String module) {
		if (null != module) {
			try {
                this.registry.removeModule(module);
            } catch (final OXException e) {
                LOG.error("error removing module description", e);
            }
		}
	}

}
