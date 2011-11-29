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

package com.openexchange.filemanagement.internal;

import java.util.concurrent.atomic.AtomicBoolean;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.server.Initialization;
import com.openexchange.server.osgi.ServerActivator;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link ManagedFileInitialization} - Initialization for {@link ManagedFileManagement}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ManagedFileInitialization implements Initialization {

    private final AtomicBoolean started;

    private volatile ServiceRegistration<ManagedFileManagement> registerService;

    /**
     * Initializes a new {@link ManagedFileInitialization}
     */
    public ManagedFileInitialization() {
        super();
        started = new AtomicBoolean();
    }

    @Override
    public void start() {
        if (!started.compareAndSet(false, true)) {
            return;
        }
        // Simulate bundle registration
        final ManagedFileManagementImpl fileManagement = ManagedFileManagementImpl.getInstance();
        ServerServiceRegistry.getInstance().addService(ManagedFileManagement.class, fileManagement);
        final BundleContext context = ServerActivator.getContext();
        if (null != context) {
            registerService = context.registerService(ManagedFileManagement.class, fileManagement, null);
        }
    }

    @Override
    public void stop() {
        if (!started.compareAndSet(true, false)) {
            return;
        }
        final ServiceRegistration<ManagedFileManagement> registerService = this.registerService;
        if (null != registerService) {
            registerService.unregister();
            this.registerService = null;
        }
        // Simulate bundle shut-down
        ServerServiceRegistry.getInstance().removeService(ManagedFileManagement.class);
        ManagedFileManagementImpl.releaseInstance();
    }

}
