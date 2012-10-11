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

package com.openexchange.jslob.storage.db.osgi;

import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.jslob.storage.JSlobStorage;
import com.openexchange.jslob.storage.db.DBJSlobStorage;
import com.openexchange.jslob.storage.db.groupware.DBJSlobCreateTableService;
import com.openexchange.jslob.storage.db.groupware.DBJSlobCreateTableTask;
import com.openexchange.jslob.storage.db.groupware.JSlobDBDeleteListener;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link DBJSlobStorageActivcator}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DBJSlobStorageActivcator extends HousekeepingActivator {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.LogFactory.getLog(DBJSlobStorageActivcator.class);

    /**
     * Initializes a new {@link DBJSlobStorageActivcator}.
     */
    public DBJSlobStorageActivcator() {
        super();
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("Starting bundle: com.openexchange.jslob.storage.db");
        try {
            final DBJSlobStorage dbJSlobStorage = new DBJSlobStorage(this);
            registerService(JSlobStorage.class, dbJSlobStorage);
            /*
             * Register services for table creation
             */
            registerService(CreateTableService.class, new DBJSlobCreateTableService());
            registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new DBJSlobCreateTableTask(this)));
            /*
             * Register delete listener
             */
            registerService(DeleteListener.class, new JSlobDBDeleteListener(dbJSlobStorage), null);
        } catch (final Exception e) {
            LOG.error("Starting bundle \"com.openexchange.jslob.storage.db\" failed: " + e.getMessage());
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("Stopping bundle: com.openexchange.jslob.storage.db");
        try {
            cleanUp();
        } catch (final Exception e) {
            LOG.error("Stopping bundle \"com.openexchange.jslob.storage.db\" failed: " + e.getMessage());
            throw e;
        }
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { DatabaseService.class };
    }

}
