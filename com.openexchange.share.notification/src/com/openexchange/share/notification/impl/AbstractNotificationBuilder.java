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
