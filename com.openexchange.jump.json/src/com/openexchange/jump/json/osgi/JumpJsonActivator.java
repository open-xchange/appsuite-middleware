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

package com.openexchange.jump.json.osgi;

import java.util.concurrent.atomic.AtomicReference;
import org.osgi.framework.BundleContext;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.jump.EndpointHandler;
import com.openexchange.jump.json.EndpointHandlerRegistry;
import com.openexchange.jump.json.JumpActionFactory;
import com.openexchange.jump.json.actions.AbstractJumpAction;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;

/**
 * {@link JumpJsonActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JumpJsonActivator extends AJAXModuleActivator {

    private static final AtomicReference<BundleContext> START_BUNDLE_CONTEXT_REF = new AtomicReference<BundleContext>();

    /**
     * Gets the bundle context passed to this activator on bundle start-up.
     *
     * @return The bundle context or <code>null</code> if absent
     */
    public static BundleContext getBundleContextFromStartUp() {
        return START_BUNDLE_CONTEXT_REF.get();
    }

    // -------------------------------------------------------------------------------------------- //

    /**
     * Initializes a new {@link JumpJsonActivator}.
     */
    public JumpJsonActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

    @Override
    protected void startBundle() throws Exception {
        final BundleContext context = this.context;
        START_BUNDLE_CONTEXT_REF.set(context);

        final RankingAwareNearRegistryServiceTracker<EndpointHandler> handlers = new RankingAwareNearRegistryServiceTracker<EndpointHandler>(context, EndpointHandler.class);
        rememberTracker(handlers);
        openTrackers();
        AbstractJumpAction.setEndpointHandlerRegistry(new EndpointHandlerRegistry(handlers));

        registerModule(new JumpActionFactory(this), "jump");
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
        AbstractJumpAction.setEndpointHandlerRegistry(null);
        START_BUNDLE_CONTEXT_REF.set(null);
    }

}
