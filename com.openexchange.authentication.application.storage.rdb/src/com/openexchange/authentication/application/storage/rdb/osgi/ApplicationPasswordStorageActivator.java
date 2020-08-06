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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.authentication.application.storage.rdb.osgi;

import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import com.openexchange.authentication.application.storage.AppPasswordStorage;
import com.openexchange.authentication.application.storage.rdb.AppPasswordStorageProperty;
import com.openexchange.authentication.application.storage.rdb.passwords.AppPasswordChangeEventHandler;
import com.openexchange.authentication.application.storage.rdb.passwords.AppPasswordDeleteListener;
import com.openexchange.authentication.application.storage.rdb.passwords.AppPasswordStorageRDB;
import com.openexchange.authentication.application.storage.rdb.passwords.CreateAppHistoryUpdateTask;
import com.openexchange.authentication.application.storage.rdb.passwords.CreateAppPasswordUpdateTask;
import com.openexchange.authentication.application.storage.rdb.passwords.CreateHistoryTable;
import com.openexchange.authentication.application.storage.rdb.passwords.CreatePasswordTable;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.crypto.CryptoService;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.user.UserService;

/**
 * {@link ApplicationPasswordStorageActivator}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.4
 */
public class ApplicationPasswordStorageActivator extends HousekeepingActivator {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ApplicationPasswordStorageActivator.class);

    private static final int DEFAULT_SERVICE_RANKING = -1;  // Default ranking for services likely to be overwritten

    /**
     * Initializes a new {@link ApplicationPasswordStorageActivator}.
     */
    public ApplicationPasswordStorageActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { DatabaseService.class, ContextService.class, CryptoService.class, CapabilityService.class, LeanConfigurationService.class, UserService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("starting bundle {}", context.getBundle());
        if (false == getService(LeanConfigurationService.class).getBooleanProperty(AppPasswordStorageProperty.ENABLED)) {
            LOG.info("Database-backed application password storage is disabled by configuration.");
            return;
        }

        // Database setup
        registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new CreateAppPasswordUpdateTask(), new CreateAppHistoryUpdateTask()));
        registerService(CreateTableService.class, new CreatePasswordTable());
        registerService(CreateTableService.class, new CreateHistoryTable());

        // Register the default storage services 
        AppPasswordStorageRDB storage = new AppPasswordStorageRDB(this);
        registerService(AppPasswordStorage.class, storage, DEFAULT_SERVICE_RANKING);

        // Register password change monitor
        registerService(EventHandler.class, new AppPasswordChangeEventHandler(storage), singletonDictionary(EventConstants.EVENT_TOPIC, "com/openexchange/passwordchange"));

        // Cleanup modules
        registerService(DeleteListener.class, new AppPasswordDeleteListener(storage));
        openTrackers();
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("stopping bundle {}", context.getBundle());
        super.stopBundle();
    }

}
