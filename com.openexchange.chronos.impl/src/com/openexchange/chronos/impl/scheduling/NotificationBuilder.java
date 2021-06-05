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
