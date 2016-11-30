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
