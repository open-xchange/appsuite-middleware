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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.osgi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.List;
import org.osgi.framework.BundleContext;


/**
 * {@link ServiceDependentRegistration}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class ServiceDependentRegistration<T> extends ConditionalRegistration implements DynamicServiceStateListener {

    private Whiteboard whiteboard;
    private final List<Object> services = new ArrayList<Object>();

    public ServiceDependentRegistration(final BundleContext context, final String serviceName, final T service, final Dictionary dict, final Whiteboard whiteboard) {
        super(context, serviceName, service, dict);
        this.whiteboard = whiteboard;
        this.service = configure(service);
    }

    public ServiceDependentRegistration(final BundleContext context, final String serviceName, final T service, final Whiteboard whiteboard) {
        this(context, serviceName, service, null, whiteboard);
    }

    public ServiceDependentRegistration(final BundleContext context, final String serviceName, final Whiteboard whiteboard) {
        this(context, serviceName, (T) null, whiteboard);
    }

    public ServiceDependentRegistration(final BundleContext context, final String serviceName, final Dictionary dict, final Whiteboard whiteboard) {
        this(context, serviceName, null, dict, whiteboard);
    }

    /**
     * Override to configure the service
     * @param service
     */
    public T configure(final T service) {
        return service;
    }

    public void addDependency(final Object...services) {
        this.services.addAll(Arrays.asList(services));
    }

    public <T> T get(final Class<T> clazz) {
        return getAndDependOn(clazz);
    }

    public <T> T getAndDependOn(final Class<T> clazz) {
        final T service = whiteboard.getService(clazz, this);
        addDependency(service);
        return service;
    }

    @Override
    protected boolean mustRegister() {
        for(final Object service : services) {
            if (! whiteboard.isActive(service)) {
                LOG.info("Missing service. Proxy is not active: "+service+ " needed by "+this.service);
                return false;
            }
        }
        final boolean validateServices = validateServices();
        if(validateServices) {
            LOG.info("All is fine, registering service "+service+".");
            return true;
        }
        return false;
    }

    /**
     * Override to add additional constraint requirements to the service
     * @return
     */
    public boolean validateServices() {
        return true;
    }

    @Override
    public void stateChanged() {
        check();
    }


}
