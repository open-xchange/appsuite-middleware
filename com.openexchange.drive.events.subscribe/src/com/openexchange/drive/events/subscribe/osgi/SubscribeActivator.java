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

package com.openexchange.drive.events.subscribe.osgi;

import com.openexchange.context.ContextService;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.drive.events.subscribe.DriveSubscriptionStore;
import com.openexchange.drive.events.subscribe.internal.SubscribeServiceLookup;
import com.openexchange.drive.events.subscribe.rdb.DriveEventSubscriptionsAddUuidColumnTask;
import com.openexchange.drive.events.subscribe.rdb.DriveEventSubscriptionsCreateTableService;
import com.openexchange.drive.events.subscribe.rdb.DriveEventSubscriptionsCreateTableTask;
import com.openexchange.drive.events.subscribe.rdb.DriveEventSubscriptionsDeleteListener;
import com.openexchange.drive.events.subscribe.rdb.DriveEventSubscriptionsMakeUuidPrimaryTask;
import com.openexchange.drive.events.subscribe.rdb.RdbSubscriptionStore;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link SubscribeActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SubscribeActivator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SubscribeActivator.class);

    /**
     * Initializes a new {@link SubscribeActivator}.
     */
    public SubscribeActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { DatabaseService.class, ContextService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("starting bundle: {}", context.getBundle().getSymbolicName());
        SubscribeServiceLookup.set(this);
        registerService(CreateTableService.class, new DriveEventSubscriptionsCreateTableService());
        registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new DriveEventSubscriptionsCreateTableTask()));
        registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new DriveEventSubscriptionsAddUuidColumnTask()));
        registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new DriveEventSubscriptionsMakeUuidPrimaryTask()));
        registerService(DeleteListener.class, new DriveEventSubscriptionsDeleteListener());
        registerService(DriveSubscriptionStore.class, new RdbSubscriptionStore());
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("stopping bundle: {}", context.getBundle().getSymbolicName());
        SubscribeServiceLookup.set(null);
        super.stopBundle();
    }

}
