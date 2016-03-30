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

package com.openexchange.documentation;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.openexchange.documentation.descriptions.ModuleDescription;

/**
 * {@link ModuleDescriptionRegisterer} - Abstract tracker customizer that registers a {@link ModuleDescription} service when the
 * {@link DescriptionFactory} service is available.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class ModuleDescriptionRegisterer implements ServiceTrackerCustomizer<DescriptionFactory, DescriptionFactory> {

    private final BundleContext context;
	private ServiceRegistration<ModuleDescription> serviceRegistration;

	/**
     * Initializes a new {@link ModuleDescriptionRegisterer}.
     */
    protected ModuleDescriptionRegisterer(final BundleContext context) {
        super();
		this.context = context;
    }

    /**
     * Creates a new {@link ServiceTracker} based on this customizer instance that tracks registered services under the name of the
     * {@link DescriptionFactory} class.
     *
     * @return the service tracker
     */
    public ServiceTracker<DescriptionFactory, DescriptionFactory> asTracker() {
    	return new ServiceTracker<DescriptionFactory, DescriptionFactory>(this.context, DescriptionFactory.class, this);
    }

    /**
     * Gets the module description
     *
     * @param factory a description factory instance to aid with description construction
     * @return the module description
     */
    protected abstract ModuleDescription getDescription(final DescriptionFactory factory);

	@Override
	public final DescriptionFactory addingService(final ServiceReference<DescriptionFactory> reference) {
		final DescriptionFactory factory = context.getService(reference);
		this.serviceRegistration = context.registerService(ModuleDescription.class, this.getDescription(factory), null);
		return factory;
	}

	@Override
	public final void modifiedService(final ServiceReference<DescriptionFactory> reference, final DescriptionFactory service) {
		// nothing to do
	}

	@Override
	public final void removedService(final ServiceReference<DescriptionFactory> reference, final DescriptionFactory service) {
		context.ungetService(reference);
		this.serviceRegistration.unregister();
	}

}
