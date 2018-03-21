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

package com.openexchange.folderstorage.calendar.osgi;

import static org.slf4j.LoggerFactory.getLogger;
import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccessFactory;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderField;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.calendar.CalendarAccountErrorField;
import com.openexchange.folderstorage.calendar.CalendarConfigField;
import com.openexchange.folderstorage.calendar.CalendarFolderStorage;
import com.openexchange.folderstorage.calendar.CalendarProviderField;
import com.openexchange.folderstorage.calendar.ExtendedPropertiesField;
import com.openexchange.osgi.DependentServiceRegisterer;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.server.ServiceExceptionCode;

/**
 * {@link CalendarFolderStorageActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public final class CalendarFolderStorageActivator extends HousekeepingActivator {

    private ServiceTracker<?, ?> dependentTracker;

    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            getLogger(CalendarFolderStorageActivator.class).info("starting bundle {}", context.getBundle());
            /*
             * initialize folder storage
             */
            reinit();
            openTrackers();
        } catch (Exception e) {
            getLogger(CalendarFolderStorageActivator.class).error("error starting {}", context.getBundle(), e);
            throw e;
        }
    }

    private synchronized void reinit() throws OXException {
        ServiceTracker<?, ?> tracker = this.dependentTracker;
        if (null != tracker) {
            this.dependentTracker = null;
            tracker.close();
            tracker = null;
        }
        /*
         * register calendar folder storage once IDBasedCalendarAccessFactory service becomes available
         */
        Dictionary<String, String> serviceProperties = new Hashtable<String, String>(1);
        serviceProperties.put("tree", FolderStorage.REAL_TREE_ID);
        DependentServiceRegisterer<FolderStorage> registerer = new DependentServiceRegisterer<FolderStorage>(
            context, FolderStorage.class, CalendarFolderStorage.class, serviceProperties, IDBasedCalendarAccessFactory.class);
        try {
            tracker = new ServiceTracker<>(context, registerer.getFilter(), registerer);
        } catch (InvalidSyntaxException e) {
            throw ServiceExceptionCode.SERVICE_INITIALIZATION_FAILED.create(e);
        }
        this.dependentTracker = tracker;
        tracker.open();
        /*
         * register custom calendar folder fields
         */
        registerService(FolderField.class, ExtendedPropertiesField.getInstance());
        registerService(FolderField.class, CalendarProviderField.getInstance());
        registerService(FolderField.class, CalendarConfigField.getInstance());
        registerService(FolderField.class, CalendarAccountErrorField.getInstance());
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        getLogger(CalendarFolderStorageActivator.class).info("stopping bundle {}", context.getBundle());
        ServiceTracker<?, ?> tracker = this.dependentTracker;
        if (null != tracker) {
            this.dependentTracker = null;
            tracker.close();
            tracker = null;
        }
        super.stopBundle();
    }

}
