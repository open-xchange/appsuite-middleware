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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import org.apache.commons.logging.Log;
import com.openexchange.caching.CacheService;
import com.openexchange.contact.storage.ContactStorage;
import com.openexchange.contact.storage.ldap.config.LdapContactStorageFactory;
import com.openexchange.contact.storage.ldap.database.LdapCreateTableService;
import com.openexchange.contact.storage.ldap.database.LdapCreateTableTask;
import com.openexchange.contact.storage.ldap.database.LdapDeleteListener;
import com.openexchange.contact.storage.ldap.internal.LdapServiceLookup;
import com.openexchange.context.ContextService;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.log.LogFactory;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.timer.TimerService;
import com.openexchange.user.UserService;

/**
 * {@link LdapContactStorageActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class LdapContactStorageActivator extends HousekeepingActivator {

    private final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(LdapContactStorageActivator.class));

    /**
     * Initializes a new {@link LdapContactStorageActivator}.
     */
    public LdapContactStorageActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { DatabaseService.class, ContextService.class, UserService.class, TimerService.class, CacheService.class };
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
    
}
