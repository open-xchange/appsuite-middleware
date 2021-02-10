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

package com.openexchange.folderstorage.contact.osgi;

import static org.slf4j.LoggerFactory.getLogger;
import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.contact.provider.composition.IDBasedContactsAccessFactory;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderField;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.contact.ContactsFolderStorage;
import com.openexchange.folderstorage.contact.field.ContactsAccountErrorField;
import com.openexchange.folderstorage.contact.field.ContactsConfigField;
import com.openexchange.folderstorage.contact.field.ContactsProviderField;
import com.openexchange.folderstorage.contact.field.ExtendedPropertiesField;
import com.openexchange.osgi.DependentServiceRegisterer;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.server.ServiceExceptionCode;

/**
 * {@link ContactsFolderStorageActivator}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class ContactsFolderStorageActivator extends HousekeepingActivator {

    private ServiceTracker<?, ?> dependentTracker;

    /**
     * Initializes a new {@link ContactsFolderStorageActivator}.
     */
    public ContactsFolderStorageActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            getLogger(ContactsFolderStorageActivator.class).info("Starting bundle {}", context.getBundle());
            reinit();
            openTrackers();
        } catch (Exception e) {
            getLogger(ContactsFolderStorageActivator.class).error("Error starting {}", context.getBundle(), e);
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
        Dictionary<String, String> serviceProperties = new Hashtable<>(1);
        serviceProperties.put("tree", FolderStorage.REAL_TREE_ID);
        DependentServiceRegisterer<FolderStorage> registerer = new DependentServiceRegisterer<>(context, FolderStorage.class, ContactsFolderStorage.class, serviceProperties, IDBasedContactsAccessFactory.class);
        try {
            tracker = new ServiceTracker<>(context, registerer.getFilter(), registerer);
        } catch (InvalidSyntaxException e) {
            throw ServiceExceptionCode.SERVICE_INITIALIZATION_FAILED.create(e);
        }
        this.dependentTracker = tracker;
        tracker.open();

        // Register custom fields
        registerService(FolderField.class, ExtendedPropertiesField.getInstance());
        registerService(FolderField.class, ContactsProviderField.getInstance());
        registerService(FolderField.class, ContactsConfigField.getInstance());
        registerService(FolderField.class, ContactsAccountErrorField.getInstance());
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        getLogger(ContactsFolderStorageActivator.class).info("Stopping bundle {}", context.getBundle());
        ServiceTracker<?, ?> tracker = this.dependentTracker;
        if (null != tracker) {
            this.dependentTracker = null;
            tracker.close();
            tracker = null;
        }
        super.stopBundle();
    }

}
