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

package com.openexchange.share.limit.osgi;

import com.openexchange.ajax.requesthandler.DispatcherListener;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.share.limit.impl.FilesDownloadLimiter;
import com.openexchange.share.limit.impl.InfostoreDownloadLimiter;
import com.openexchange.share.limit.internal.Services;
import com.openexchange.share.limit.rdb.FileAccessCreateTableService;
import com.openexchange.share.limit.rdb.FileAccessCreateTableTask;
import com.openexchange.share.limit.util.LimitConfig;

/**
 *
 * {@link ShareLimitActivator}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.2
 */
public class ShareLimitActivator extends AJAXModuleActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ShareLimitActivator.class);

    /**
     * Initializes a new {@link ShareLimitActivator}.
     */
    public ShareLimitActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, ConfigViewFactory.class, DatabaseService.class };
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("starting bundle: \"com.openexchange.share.limit\"");

        Services.set(this);

        if (LimitConfig.isEnabled()) {
            // Register create table services for user schema
            registerService(CreateTableService.class, new FileAccessCreateTableService());

            // Register update tasks for user schema
            registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new FileAccessCreateTableTask(getService(DatabaseService.class))));

            final FilesDownloadLimiter filesDownloadLimiter = new FilesDownloadLimiter(getService(ConfigViewFactory.class));
            registerService(DispatcherListener.class, filesDownloadLimiter);
            final InfostoreDownloadLimiter infostoreDownloadLimiter = new InfostoreDownloadLimiter(getService(ConfigViewFactory.class));
            registerService(DispatcherListener.class, infostoreDownloadLimiter);
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("stopping bundle: \"com.openexchange.share.limit\"");
        Services.set(null);
        super.stopBundle();
    }
}
