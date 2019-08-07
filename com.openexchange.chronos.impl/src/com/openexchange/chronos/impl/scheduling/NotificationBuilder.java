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

package com.openexchange.chronos.impl.scheduling;

import java.util.HashMap;
import java.util.Map;
import com.openexchange.annotation.NonNull;
import com.openexchange.annotation.Nullable;
import com.openexchange.chronos.CalendarObjectResource;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.scheduling.ChangeNotification;
import com.openexchange.chronos.scheduling.RecipientSettings;
import com.openexchange.chronos.scheduling.changes.ChangeAction;
import com.openexchange.chronos.scheduling.changes.ScheduleChange;
import com.openexchange.exception.OXException;

/**
 * {@link NotificationBuilder}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public class NotificationBuilder {

    protected ChangeAction action;
    protected CalendarUser originator;
    protected CalendarUser recipient;
    protected CalendarObjectResource resource;
    protected ScheduleChange scheduleChange;
    protected RecipientSettings recipientSettings;
    protected Map<String, Object> additionals = new HashMap<>();

    /**
     * Initializes a new {@link NotificationBuilder}.
     */
    public NotificationBuilder() {
        super();
    }

    /**
     * Sets the method
     *
     * @param action The action to set
     * @return This {@link NotificationBuilder} instance
     */
    public NotificationBuilder setMethod(ChangeAction action) {
        this.action = action;
        return this;
    }

    /**
     * Sets the originator
     *
     * @param originator The originator to set
     * @return This {@link NotificationBuilder} instance
     */
    public NotificationBuilder setOriginator(CalendarUser originator) {
        this.originator = originator;
        return this;
    }

    /**
     * Sets the recipient
     *
     * @param recipient The recipient to set
     * @return This {@link NotificationBuilder} instance
     */
    public NotificationBuilder setRecipient(CalendarUser recipient) {
        this.recipient = recipient;
        return this;
    }

    /**
     * Sets the resource
     *
     * @param resource The resource to set
     * @return This {@link NotificationBuilder} instance
     */
    public NotificationBuilder setResource(CalendarObjectResource resource) {
        this.resource = resource;
        return this;
    }

    /**
     * Set the schedule change for the message
     *
     * @param scheduleChange The change
     * @return This {@link NotificationBuilder} instance
     */
    public NotificationBuilder setScheduleChange(ScheduleChange scheduleChange) {
        this.scheduleChange = scheduleChange;
        return this;
    }

    /**
     * Set the recipient settings for the message.
     *
     * @param recipientSettings The recipient settings
     * @return This {@link NotificationBuilder} instance
     */
    public NotificationBuilder setRecipientSettings(RecipientSettings recipientSettings) {
        this.recipientSettings = recipientSettings;
        return this;
    }

    /**
     * Add an additional information
     *
     * @param key The key to identify
     * @param value The value to add
     * @return This {@link NotificationBuilder} instance
     */
    public NotificationBuilder addAdditionals(String key, Object value) {
        additionals.put(key, value);
        return this;
    }

    /**
     * Adds multiple additional informations
     *
     * @param additionals A {@link Map} with additional information
     * @return This {@link NotificationBuilder} instance
     */
    public NotificationBuilder setAdditionals(Map<String, Object> additionals) {
        this.additionals.putAll(additionals);
        return this;
    }

    /**
     * Builds a new {@link ChangeNotification}
     *
     * @return The {@link ChangeNotification}
     * @throws OXException In case data is incomplete
     */
    public ChangeNotification build() throws OXException {
        return new Notification(this);
    }

}

class Notification implements ChangeNotification {

    private final @NonNull ChangeAction action;
    private final @NonNull CalendarUser originator;
    private final @NonNull CalendarUser recipient;
    private final @NonNull CalendarObjectResource resource;
    private final @NonNull ScheduleChange scheduleChange;
    private final RecipientSettings recipientSettings;
    private final Map<String, Object> additionals;

    /**
     * Initializes a new {@link Notification}.
     * 
     * @param builder The {@link NotificationBuilder}
     * @throws OXException In case data is incomplete
     */
    public Notification(NotificationBuilder builder) throws OXException {
        super();
        this.action = notNull(builder.action);
        this.originator = notNull(builder.originator);
        this.recipient = notNull(builder.recipient);
        this.resource = notNull(builder.resource);
        this.scheduleChange = notNull(builder.scheduleChange);
        this.additionals = builder.additionals;
        this.recipientSettings = builder.recipientSettings;
    }

    @Override
    @NonNull
    public ChangeAction getAction() {
        return action;
    }

    @Override
    @NonNull
    public CalendarUser getOriginator() {
        return originator;
    }

    @Override
    @NonNull
    public CalendarUser getRecipient() {
        return recipient;
    }

    @Override
    @NonNull
    public CalendarObjectResource getResource() {
        return resource;
    }

    @Override
    @NonNull
    public ScheduleChange getScheduleChange() {
        return scheduleChange;
    }

    @Override
    public RecipientSettings getRecipientSettings() {
        return recipientSettings;
    }

    @Override
    @Nullable
    public <T> T getAdditional(String key, Class<T> clazz) {
        if (null == additionals || additionals.isEmpty()) {
            return null;
        }
        if (additionals.containsKey(key)) {
            Object object = additionals.get(key);
            if (null != object && clazz.isAssignableFrom(object.getClass())) {
                return clazz.cast(object);
            }
        }
        return null;
    }

    @NonNull
    private <T> T notNull(T object) throws OXException {
        if (null == object) {
            throw new OXException(999);
        }
        return object;
    }

    @Override
    public String toString() {
        return "ChangeNotification:[action:" + getAction() + ", recipient:" + getRecipient().toString() + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 97;
        int result = 1;
        result = prime * result + ((additionals == null) ? 0 : additionals.hashCode());
        result = prime * result + scheduleChange.hashCode();
        result = prime * result + action.hashCode();
        result = prime * result + originator.hashCode();
        result = prime * result + recipient.hashCode();
        result = prime * result + resource.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Notification)) {
            return false;
        }
        Notification other = (Notification) obj;
        if (additionals == null) {
            if (other.additionals != null) {
                return false;
            }
        } else if (!additionals.equals(other.additionals)) {
            return false;
        }
        if (!scheduleChange.equals(other.scheduleChange)) {
            return false;
        }
        if (action != other.action) {
            return false;
        }
        if (!originator.equals(other.originator)) {
            return false;
        }
        if (!recipient.equals(other.recipient)) {
            return false;
        }
        if (!resource.equals(other.resource)) {
            return false;
        }
        return true;
    }

}
