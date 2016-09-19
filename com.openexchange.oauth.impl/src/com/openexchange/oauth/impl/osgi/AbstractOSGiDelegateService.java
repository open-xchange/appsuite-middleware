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

package com.openexchange.oauth.impl.osgi;

import java.util.concurrent.atomic.AtomicReference;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;

/**
 * {@link AbstractOSGiDelegateService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractOSGiDelegateService<S> {

    private final Class<S> clazz;

    private final AtomicReference<S> service;

    private volatile ServiceTracker<?, ?> tracker;

    /**
     * Initializes a new {@link AbstractOSGiDelegateService}.
     */
    protected AbstractOSGiDelegateService(final Class<S> clazz) {
        super();
        this.clazz = clazz;
        service = new AtomicReference<>();
    }

    /**
     * Starts tracking the delegate service.
     *
     * @param bundleContext The bundle context
     * @return This instance for method chaining
     */
    @SuppressWarnings("unchecked")
    public final <I extends AbstractOSGiDelegateService<S>> I start(final BundleContext bundleContext) {
        if (null == tracker) {
            synchronized (this) {
                ServiceTracker<?, ?> tmp = tracker;
                if (null == tracker) {
                    tracker = tmp = new ServiceTracker<>(bundleContext, clazz.getName(), new Customizer<>(service, bundleContext));
                    tmp.open();
                }
            }
        }
        return (I) this;
    }

    /**
     * Stops tracking the delegate service.
     */
    public final void stop() {
        final ServiceTracker<?, ?> tmp = tracker;
        if (null != tmp) {
            tmp.close();
        }
    }

    /**
     * Gets the service from service reference.
     *
     * @return The service
     * @throws OXException If service reference returned <code>null</code>
     */
    protected S getService() throws OXException {
        final S serviceInst = service.get();
        if (null == serviceInst) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(clazz.getName());
        }
        return serviceInst;
    }

    /**
     * Gets the service from service reference.
     *
     * @return The service or <code>null</code> if absent
     */
    protected S optService() {
        return service.get();
    }

}
