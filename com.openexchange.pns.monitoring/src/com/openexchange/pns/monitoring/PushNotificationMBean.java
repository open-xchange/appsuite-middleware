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

package com.openexchange.pns.monitoring;

import javax.management.MBeanException;
import com.openexchange.management.MBeanMethodAnnotation;


/**
 * {@link PushNotificationMBean} - The MBean for Push Notification Service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public interface PushNotificationMBean {

    /** The MBean's domain */
    public static final String DOMAIN = "com.openexchange.pns";

    /**
     * Gets the number of buffered notifications that are supposed to be transported.
     *
     * @return The number of buffered notifications
     * @throws MBeanException If number of buffered notifications cannot be returned
     */
    @MBeanMethodAnnotation (description="Gets the number of buffered notifications that are supposed to be transported", parameters={}, parameterDescriptions={})
    long getNumberOfBufferedNotifications() throws MBeanException;

    /**
     * Gets the number of submitted notifications per minute.
     * <p>
     * A notification is in submitted state if fetched from buffer and submitted for being transported, but not yet done.
     *
     * @return The number of submitted notifications per minute
     * @throws MBeanException If number of submitted notifications cannot be returned
     */
    @MBeanMethodAnnotation (description="Gets the number of submitted notifications per minute", parameters={}, parameterDescriptions={})
    long getNotificationsPerMinute() throws MBeanException;

    /**
     * Gets the total number of submitted notifications so far.
     * <p>
     * A notification is in submitted state if fetched from buffer and submitted for being transported, but not yet done.
     *
     * @return The total number of submitted notifications
     * @throws MBeanException If number of submitted notifications cannot be returned
     */
    @MBeanMethodAnnotation (description="Gets the total number of submitted notifications so far.", parameters={}, parameterDescriptions={})
    long getTotalNumberOfSubmittedNotifications() throws MBeanException;

    /**
     * Gets the total number of processed/distributed notifications so far.
     *
     * @return The total number of processed/distributed notifications
     * @throws MBeanException If number of processed/distributed notifications cannot be returned
     */
    @MBeanMethodAnnotation (description="Gets the total number of processed/distributed notifications so far.", parameters={}, parameterDescriptions={})
    long getTotalNumberOfProcessedNotifications() throws MBeanException;

    /**
     * Gets the number of notifications that are currently submitted, but not yet processed.
     *
     * @return The number of enqueued notifications
     * @throws MBeanException If number of enqueued notifications cannot be returned
     */
    @MBeanMethodAnnotation (description="Gets the number of notifications that are currently submitted, but not yet processed", parameters={}, parameterDescriptions={})
    long getEnqueuedNotifications() throws MBeanException;

}
