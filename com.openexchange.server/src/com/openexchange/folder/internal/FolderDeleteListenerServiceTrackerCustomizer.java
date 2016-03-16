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

package com.openexchange.folder.internal;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.folder.FolderDeleteListenerService;

/**
 * {@link FolderDeleteListenerServiceTrackerCustomizer} - The service tracker customizer for {@link FolderDeleteListenerService}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderDeleteListenerServiceTrackerCustomizer implements ServiceTrackerCustomizer<FolderDeleteListenerService,FolderDeleteListenerService> {

    private final BundleContext context;

    /**
     * Initializes a new {@link FolderDeleteListenerServiceTrackerCustomizer}.
     */
    public FolderDeleteListenerServiceTrackerCustomizer(final BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public FolderDeleteListenerService addingService(final ServiceReference<FolderDeleteListenerService> reference) {
        /*
         * Optionally expect class name with org.osgi.framework.Constants.SERVICE_DESCRIPTION property
         */
        final Object property = reference.getProperty(org.osgi.framework.Constants.SERVICE_DESCRIPTION);
        final FolderDeleteListenerRegistry registry = FolderDeleteListenerRegistry.getInstance();
        if (property != null && registry.containsByClassName(property.toString())) {
            // Nothing to track
            return null;
        }
        final FolderDeleteListenerService deleteListenerService = context.getService(reference);
        if (registry.addDeleteListenerService(deleteListenerService)) {
            return deleteListenerService;
        }
        // Nothing to track
        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(final ServiceReference<FolderDeleteListenerService> reference, final FolderDeleteListenerService service) {
        // Nothing to do
    }

    @Override
    public void removedService(final ServiceReference<FolderDeleteListenerService> reference, final FolderDeleteListenerService service) {
        if (null != service) {
            try {
                FolderDeleteListenerRegistry.getInstance().removeDeleteListenerService(service);
            } finally {
                context.ungetService(reference);
            }
        }
    }

}
