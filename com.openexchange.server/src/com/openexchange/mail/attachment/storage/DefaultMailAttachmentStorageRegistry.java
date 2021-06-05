/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.mail.attachment.storage;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * {@link DefaultMailAttachmentStorageRegistry}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
public class DefaultMailAttachmentStorageRegistry implements MailAttachmentStorageRegistry {

    private static volatile DefaultMailAttachmentStorageRegistry instance;

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static MailAttachmentStorageRegistry getInstance() {
        return instance;
    }

    /**
     * Initializes the instance
     *
     * @param context The OSGi bundle context
     */
    public static void initInstance(BundleContext context) {
        ServiceTracker<MailAttachmentStorage, MailAttachmentStorage> tracker = new ServiceTracker<MailAttachmentStorage, MailAttachmentStorage>(context, MailAttachmentStorage.class, null);
        tracker.open();
        instance = new DefaultMailAttachmentStorageRegistry(tracker);
    }

    /**
     * Drops the instance
     */
    public static void dropInstance() {
        DefaultMailAttachmentStorageRegistry tmp = instance;
        if (null != tmp) {
            tmp.tracker.close();
            instance = null;
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------- //

    private final ServiceTracker<MailAttachmentStorage, MailAttachmentStorage> tracker;

    /**
     * Initializes a new {@link DefaultMailAttachmentStorageRegistry}.
     *
     * @param context The bundle context
     */
    private DefaultMailAttachmentStorageRegistry(ServiceTracker<MailAttachmentStorage, MailAttachmentStorage> tracker) {
        super();
        this.tracker = tracker;
    }

    @Override
    public MailAttachmentStorage getMailAttachmentStorage() {
        return tracker.getService();
    }

}
