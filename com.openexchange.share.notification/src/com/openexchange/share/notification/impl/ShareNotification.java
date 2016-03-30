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

package com.openexchange.share.notification.impl;

import java.util.Locale;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.share.notification.ShareNotificationService.Transport;


/**
 * A {@link ShareNotification} encapsulates all information necessary to notify
 * the according recipient about a share and provide her a link to access that share.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public interface ShareNotification<T> {

    /**
     * Gets the transport that shall be used to deliver this notification.
     *
     * @return The {@link Transport}, never <code>null</code>
     */
    Transport getTransport();

    /**
     * Gets the type of this notification (e.g. "a share has been created").
     *
     * @return The {@link NotificationType}, never <code>null</code>
     */
    NotificationType getType();

    /**
     * Gets the transport information used to notify the recipient.
     *
     * @return The transport information, never <code>null</code>
     */
    T getTransportInfo();

    /**
     * Gets the ID of the context where the share is located.
     *
     * @return The context ID
     */
    int getContextID();

    /**
     * Gets the locale used to translate the notification message before it is sent out.
     *
     * @return The locale
     */
    Locale getLocale();

    /**
     * Gets host information of the HTTP request that led to the generation of this
     * notification.
     *
     * @return The host data
     */
    HostData getHostData();

}
