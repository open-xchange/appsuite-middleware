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

package com.openexchange.contact.storage.ldap.osgi;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import com.openexchange.caching.CacheService;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.contact.storage.ContactStorage;
import com.openexchange.contact.storage.ldap.config.LdapContactStorageFactory;
import com.openexchange.contact.storage.ldap.database.LdapCreateTableService;
import com.openexchange.contact.storage.ldap.database.LdapCreateTableTask;
import com.openexchange.contact.storage.ldap.database.LdapDeleteListener;
import com.openexchange.contact.storage.ldap.internal.LdapServiceLookup;
import com.openexchange.contact.storage.ldap.internal.Tools;
import com.openexchange.context.ContextService;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.timer.TimerService;
import com.openexchange.user.UserService;

/**
 * {@link LdapContactStorageActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class LdapContactStorageActivator extends HousekeepingActivator implements Reloadable {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(LdapContactStorageActivator.class);
    private static final String[] PROPERTIES = new String[] {"all properties in file"};
    private static File[] oldProperties;

    /**
     * Initializes a new {@link LdapContactStorageActivator}.
     */
    public LdapContactStorageActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { DatabaseService.class, ContextService.class, UserService.class, TimerService.class,
            CacheService.class, ConfigurationService.class, CapabilityService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            LOG.info("starting bundle: com.openexchange.contact.storage.ldap");
            LdapServiceLookup.set(this);
            /*
             * register update task, create table job and delete listener
             */
            registerService(CreateTableService.class, new LdapCreateTableService());
            registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new LdapCreateTableTask()));
            registerService(DeleteListener.class, new LdapDeleteListener());
            /*
             * register configured storages
             */
            for (ContactStorage storage : LdapContactStorageFactory.createAll()) {
                registerService(ContactStorage.class, storage);
            }

            // register reloadable service
            registerService(Reloadable.class, this);
        } catch (Exception e) {
            LOG.error("error starting \"com.openexchange.contact.storage.ldap\"", e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("stopping bundle: com.openexchange.contact.storage.ldap");
        LdapServiceLookup.set(null);
        super.stopBundle();
    }

    private void reinit() {
        LOG.info("Stopping bundle com.openexchange.contact.storage.ldap for reinitialisation.");
        try {
            stopBundle();
        } catch (Exception e) {
            LOG.error("Bundle com.openexchange.contact.storage.ldap could not be stopped.", e);
        }
        try {
            LOG.info("Restarting bundle: com.openexchange.contact.storage.ldap");
            LdapServiceLookup.set(this);
            /*
             * register update task, create table job and delete listener
             */
            registerService(CreateTableService.class, new LdapCreateTableService());
            registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new LdapCreateTableTask()));
            registerService(DeleteListener.class, new LdapDeleteListener());
            /*
             * register configured storages
             */
            for (ContactStorage storage : LdapContactStorageFactory.createAll()) {
                registerService(ContactStorage.class, storage);
            }

            // register reloadable service
            registerService(Reloadable.class, this);
        } catch (Exception e) {
            LOG.error("error restarting \"com.openexchange.contact.storage.ldap\"", e);
        }
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        reinit();
    }

    @Override
    public Interests getInterests() {
        Set<String> fileNames = new TreeSet<>();
        try {
            File[] newProperties = Tools.listPropertyFiles();
            Set<File> files = new HashSet<File>(Arrays.asList(newProperties));
            if (null != oldProperties && oldProperties.length > newProperties.length) {
                files.addAll(Arrays.asList(oldProperties));
            }
            oldProperties = Arrays.copyOf(newProperties, newProperties.length);
            for (File propertyFile : files) {
                if (false == fileNames.add(propertyFile.getName())) {
                    LOG.warn("Duplicate entry in set: {}", propertyFile.getName());
                }
            }
        } catch (OXException e) {
            LOG.error("error reloading config file: {}", e);
        }

        return DefaultInterests.builder().configFileNames(fileNames.toArray(new String[fileNames.size()])).build();
    }

}
