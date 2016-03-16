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

import static com.google.common.base.Preconditions.checkNotNull;
import java.util.Locale;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.share.notification.ShareNotificationService.Transport;

/**
 * Abstract convenience implementation that provides all common fields for {@link ShareNotification}s.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public abstract class AbstractNotification<T> implements ShareNotification<T> {

    protected final Transport transport;

    protected final NotificationType type;

    protected T transportInfo;

    protected int contextID;

    protected Locale locale;

    protected HostData hostData;

    /**
     * Initializes a new {@link AbstractNotification}.
     *
     * @param transport The transport used for delivering the notification
     * @param type The type of the notification
     */
    public AbstractNotification(Transport transport, NotificationType type) {
        super();
        checkNotNull(transport);
        checkNotNull(type);
        this.type = type;
        this.transport = transport;
    }

    @Override
    public Transport getTransport() {
        return transport;
    }

    @Override
    public NotificationType getType() {
        return type;
    }

    @Override
    public T getTransportInfo() {
        return transportInfo;
    }

    @Override
    public int getContextID() {
        return contextID;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public HostData getHostData() {
        return hostData;
    }

    /**
     * Sets the transport info according to the notifications {@link Transport}
     *
     * @param transportInfo
     */
    public void setTransportInfo(T transportInfo) {
        this.transportInfo = transportInfo;
    }

    /**
     * Sets the context ID
     *
     * @param contextID
     */
    public void setContextID(int contextID) {
        this.contextID = contextID;
    }

    /**
     * Sets the locale to be used for string translations in notification messages
     *
     * @param locale
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /**
     * Sets the request context
     *
     * @param hostData
     */
    public void setRequestContext(HostData hostData) {
        this.hostData = hostData;
    }

    /**
     * Applies all common fields at once by extracting them from the given builder.
     *
     * @param builder
     */
    public void apply(AbstractNotificationBuilder<?, ?, T> builder) {
        this.transportInfo = builder.transportInfo;
        this.contextID = builder.contextID;
        this.locale = builder.locale;
        this.hostData = builder.hostData;
    }

}
