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

import java.util.Collection;
import java.util.Locale;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.java.Strings;
import com.openexchange.share.notification.ShareNotificationService.Transport;


/**
 * An abstract superclass for builders of {@link ShareNotification}s.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
@SuppressWarnings("unchecked")
public abstract class AbstractNotificationBuilder<B extends AbstractNotificationBuilder<B, N, T>, N extends ShareNotification<T>, T> {

    protected final NotificationType type;

    protected T transportInfo;

    protected int contextID;

    protected int userID;

    protected Locale locale;

    protected HostData hostData;

    protected AbstractNotificationBuilder(NotificationType type) {
        super();
        this.type = type;
    }

    /**
     * Sets the transport info according to the notifications {@link Transport}
     *
     * @param transportInfo
     */
    public B setTransportInfo(T transportInfo) {
        this.transportInfo = transportInfo;
        return (B) this;
    }

    /**
     * Sets the ID of the context where the share is located.
     *
     * @param contextID The context ID
     */
    public B setContextID(int contextID) {
        this.contextID = contextID;
        return (B) this;
    }

    /**
     * Sets the ID of the recipient user.
     *
     * @param userID
     */
    public B setUserID(int userID) {
        this.userID = userID;
        return (B) this;
    }

    /**
     * Sets the locale to be used for string translations in notification messages
     *
     * @param locale
     */
    public B setLocale(Locale locale) {
        this.locale = locale;
        return (B) this;
    }

    /**
     * Sets the request context
     *
     * @param hostData
     */
    public B setHostData(HostData hostData) {
        this.hostData = hostData;
        return (B) this;
    }

    /**
     * Builds the {@link ShareNotification} with all the values set for this builder.
     *
     * @return The share notification
     * @throws IllegalStateException if a necessary field has not been initialized or an invalid value was set
     */
    public N build() {
        checkNotNull(transportInfo, "transportInfo");
        checkNotZero(contextID, "contextID");
        checkNotZero(userID, "guestID");
        checkNotNull(locale, "locale");
        checkNotNull(hostData, "hostData");
        return doBuild();
    }

    /**
     * Builds the final {@link ShareNotification}. Must be implemented by all inheritors.
     *
     * @return The ShareNotification
     * @throws IllegalStateException if a necessary field has not been initialized or an invalid value was set
     */
    protected abstract N doBuild();

    protected static final void checkNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalStateException("Field '" + fieldName + "' must be set before calling build()!");
        }
    }

    protected static final void checkNotZero(int value, String fieldName) {
        if (value == 0) {
            throw new IllegalStateException("Field '" + fieldName + "' must be set before calling build()!");
        }
    }

    protected static final void checkGreaterZero(int value, String fieldName) {
        if (value <= 0) {
            throw new IllegalStateException("Field '" + fieldName + "' must be set before calling build()!");
        }
    }

    protected static final void checkNotEmpty(Object value, String fieldName) {
        if (value instanceof String && Strings.isEmpty((String) value)) {
            throw new IllegalStateException("Field '" + fieldName + "' must be set to a valid String before calling build()!");
        } else if (value instanceof Collection<?> && ((Collection<?>)value).isEmpty()) {
            throw new IllegalStateException("Collection '" + fieldName + "' must contain at least one element before calling build()!");
        }
    }
}
