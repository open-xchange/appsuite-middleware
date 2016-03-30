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

package com.openexchange.filestore.sproxyd.osgi;

import java.rmi.Remote;
import java.util.Dictionary;
import java.util.Hashtable;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.filestore.FileStorageProvider;
import com.openexchange.filestore.sproxyd.SproxydFileStorageFactory;
import com.openexchange.filestore.sproxyd.groupware.SproxydCreateTableService;
import com.openexchange.filestore.sproxyd.groupware.SproxydCreateTableTask;
import com.openexchange.filestore.sproxyd.groupware.SproxydDeleteListener;
import com.openexchange.filestore.sproxyd.rmi.SproxydRemoteManagement;
import com.openexchange.filestore.sproxyd.rmi.impl.SproxydRemoteImpl;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.timer.TimerService;

/**
 * {@link SproxydActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SproxydActivator extends HousekeepingActivator {

    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SproxydActivator.class);

    /**
     * Initializes a new {@link SproxydActivator}.
     */
    public SproxydActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, DatabaseService.class, TimerService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("Starting bundle: com.openexchange.filestore.sproxyd");

        // Trackers
        trackService(ContextService.class);
        openTrackers();

        // Register update task, create table job and delete listener
        registerService(CreateTableService.class, new SproxydCreateTableService());
        registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new SproxydCreateTableTask(this)));
        registerService(DeleteListener.class, new SproxydDeleteListener());

        // Register factory
        registerService(FileStorageProvider.class, new SproxydFileStorageFactory(this));

        // Register RMI
        {
            Dictionary<String, Object> props = new Hashtable<String, Object>(2);
            props.put("RMIName", SproxydRemoteManagement.RMI_NAME);
            registerService(Remote.class, new SproxydRemoteImpl(this), props);
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("Stopping bundle: com.openexchange.filestore.sproxyd");
        super.stopBundle();
    }

}
