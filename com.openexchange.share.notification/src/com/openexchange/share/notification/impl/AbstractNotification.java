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
