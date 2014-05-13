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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.find.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import com.openexchange.find.SearchService;
import com.openexchange.find.internal.AvailableModules;
import com.openexchange.find.internal.MandatoryFolders;
import com.openexchange.find.internal.SearchDriverManager;
import com.openexchange.find.internal.SearchServiceImpl;
import com.openexchange.find.spi.ModuleSearchDriver;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.jslob.ConfigTreeEquivalent;

/**
 * The activator for find bundle.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> Exception handling + logging
 * @since 7.6.0
 */
public class FindActivator implements BundleActivator {

    private volatile ServiceTracker<ModuleSearchDriver, ModuleSearchDriver> driverTracker;
    private volatile ServiceRegistration<SearchService> searchServiceRegistration;
    private volatile ServiceRegistration<PreferencesItemService> mandatoryFoldersRegistration;
    private volatile ServiceRegistration<ConfigTreeEquivalent> availableFoldersJSLob;
    private volatile ServiceRegistration<PreferencesItemService> availableModulesRegistration;
    private volatile ServiceRegistration<ConfigTreeEquivalent> availableModulesJSLob;

    /**
     * Initializes a new {@link FindActivator}.
     */
    public FindActivator() {
        super();
    }

    @Override
    public void start(final BundleContext context) throws Exception {
        final Logger logger = org.slf4j.LoggerFactory.getLogger(FindActivator.class);
        logger.info("Starting bundle: com.openexchange.find");
        try {
            final SearchDriverManager driverManager = new SearchDriverManager(context);
            final SearchServiceImpl searchService = new SearchServiceImpl(driverManager);

            final ServiceTracker<ModuleSearchDriver, ModuleSearchDriver> driverTracker = new ServiceTracker<ModuleSearchDriver, ModuleSearchDriver>(context, ModuleSearchDriver.class, driverManager);
            this.driverTracker = driverTracker;
            driverTracker.open();

            final MandatoryFolders mandatoryFolders = new MandatoryFolders(driverManager);
            searchServiceRegistration = context.registerService(SearchService.class, searchService, null);
            mandatoryFoldersRegistration = context.registerService(PreferencesItemService.class, mandatoryFolders, null);
            availableFoldersJSLob = context.registerService(ConfigTreeEquivalent.class, mandatoryFolders, null);

            final AvailableModules availableModules = new AvailableModules(driverManager);
            availableModulesRegistration = context.registerService(PreferencesItemService.class, availableModules, null);
            availableModulesJSLob = context.registerService(ConfigTreeEquivalent.class, availableModules, null);

            logger.info("Bundle successfully started: com.openexchange.find");
        } catch (final Exception e) {
            logger.error("Error while starting bundle: com.openexchange.find", e);
            throw e;
        }
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        final Logger logger = org.slf4j.LoggerFactory.getLogger(FindActivator.class);
        logger.info("Stopping bundle: com.openexchange.find");
        try {
            final ServiceRegistration<SearchService> searchServiceRegistration = this.searchServiceRegistration;
            if (searchServiceRegistration != null) {
                searchServiceRegistration.unregister();
                this.searchServiceRegistration = null;
            }

            final ServiceRegistration<PreferencesItemService> mandatoryFoldersRegistration = this.mandatoryFoldersRegistration;
            if (mandatoryFoldersRegistration != null) {
                mandatoryFoldersRegistration.unregister();
                this.mandatoryFoldersRegistration = null;
            }

            final ServiceRegistration<ConfigTreeEquivalent> availableFoldersJSLob = this.availableFoldersJSLob;
            if (availableFoldersJSLob != null) {
                availableFoldersJSLob.unregister();
                this.availableFoldersJSLob = null;
            }

            final ServiceRegistration<PreferencesItemService> availableModulesRegistration = this.availableModulesRegistration;
            if (availableModulesRegistration != null) {
                availableModulesRegistration.unregister();
                this.availableModulesRegistration = null;
            }

            final ServiceRegistration<ConfigTreeEquivalent> availableModulesJSLob = this.availableModulesJSLob;
            if (availableModulesJSLob != null) {
                availableModulesJSLob.unregister();
                this.availableModulesJSLob = null;
            }

            final ServiceTracker<ModuleSearchDriver, ModuleSearchDriver> driverTracker = this.driverTracker;
            if (driverTracker != null) {
                driverTracker.close();
                this.driverTracker = null;
            }

            logger.info("Bundle successfully stopped: com.openexchange.find");
        } catch (final Exception e) {
            logger.error("Error while stopping bundle: com.openexchange.find", e);
            throw e;
        }
    }

}
