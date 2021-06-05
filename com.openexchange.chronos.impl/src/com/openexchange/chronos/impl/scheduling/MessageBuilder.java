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

import static com.openexchange.java.Autoboxing.I;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.annotation.NonNull;
import com.openexchange.annotation.Nullable;
import com.openexchange.chronos.CalendarObjectResource;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.Check;
import com.openexchange.chronos.scheduling.RecipientSettings;
import com.openexchange.chronos.scheduling.SchedulingMessage;
import com.openexchange.chronos.scheduling.SchedulingMethod;
import com.openexchange.chronos.scheduling.changes.ScheduleChange;
import com.openexchange.exception.OXException;

/**
 * {@link MessageBuilder}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public class MessageBuilder {

    protected SchedulingMethod method;
    protected CalendarUser originator;
    protected CalendarUser recipient;
    protected CalendarObjectResource resource;
    protected ScheduleChange scheduleChange;
    protected AttachmentDataProvider attachmentDataProvider;
    protected RecipientSettings recipientSettings;
    protected Map<String, Object> additionals = new HashMap<>();

    /**
     * Initializes a new {@link MessageBuilder}.
     */
    public MessageBuilder() {
        super();
    }

    /**
     * Sets the method
     *
     * @param method The method to set
     * @return This {@link MessageBuilder} instance
     */
    public MessageBuilder setMethod(SchedulingMethod method) {
        this.method = method;
        return this;
    }

    /**
     * Sets the originator
     *
     * @param originator The originator to set
     * @return This {@link MessageBuilder} instance
     */
    public MessageBuilder setOriginator(CalendarUser originator) {
        this.originator = originator;
        return this;
    }

    /**
     * Sets the recipient
     *
     * @param recipient The recipient to set
     * @return This {@link MessageBuilder} instance
     */
    public MessageBuilder setRecipient(CalendarUser recipient) {
        this.recipient = recipient;
        return this;
    }

    /**
     * Sets the resource
     *
     * @param resource The resource to set
     * @return This {@link MessageBuilder} instance
     */
    public MessageBuilder setResource(CalendarObjectResource resource) {
        this.resource = resource;
        return this;
    }

    /**
     * Set the description for the message
     *
     * @param scheduleChange The {@link ScheduleChange}
     * @return This {@link MessageBuilder} instance
     */
    public MessageBuilder setScheduleChange(ScheduleChange scheduleChange) {
        this.scheduleChange = scheduleChange;
        return this;
    }

    /**
     * Set the attachment data provider for the message
     *
     * @param attachmentDataProvider The attachment data provider
     * @return This {@link MessageBuilder} instance
     */
    public MessageBuilder setAttachmentDataProvider(AttachmentDataProvider attachmentDataProvider) {
        this.attachmentDataProvider = attachmentDataProvider;
        return this;
    }

    /**
     * Set the recipient settings for the message.
     *
     * @param recipientSettings The recipient settings
     * @return This {@link MessageBuilder} instance
     */
    public MessageBuilder setRecipientSettings(RecipientSettings recipientSettings) {
        this.recipientSettings = recipientSettings;
        return this;
    }

    /**
     * Add an additional information
     *
     * @param key The key to identify
     * @param value The value to add
     * @return This {@link MessageBuilder} instance
     */
    public MessageBuilder addAdditionals(String key, Object value) {
        additionals.put(key, value);
        return this;
    }

    /**
     * Adds multiple additional informations
     *
     * @param additionals A {@link Map} with additional information
     * @return This {@link MessageBuilder} instance
     */
    public MessageBuilder setAdditionals(Map<String, Object> additionals) {
        this.additionals.putAll(additionals);
        return this;
    }

    /**
     * Builds a new {@link SchedulingMessage}
     *
     * @return The {@link SchedulingMessage}
     * @throws OXException In case data is incomplete
     */
    public SchedulingMessage build() throws OXException {
        return new Message(this);
    }
}

class Message implements SchedulingMessage {

    private final @NonNull SchedulingMethod method;
    private final @NonNull CalendarUser originator;
    private final @NonNull CalendarUser recipient;
    private final @NonNull CalendarObjectResource resource;
    private final @NonNull ScheduleChange scheduleChange;
    private final AttachmentDataProvider attachmentDataProvider;
    private final RecipientSettings recipientSettings;
    private final Map<String, Object> additionals;

    /**
     * Initializes a new {@link Message}.
     * 
     * @param builder The {@link MessageBuilder}
     * @throws OXException In case data is incomplete
     */
    public Message(MessageBuilder builder) throws OXException {
        super();
        this.method = notNull(builder.method);
        this.originator = notNull(builder.originator);
        this.recipient = notNull(builder.recipient);
        this.resource = notNull(builder.resource);
        this.scheduleChange = notNull(builder.scheduleChange);
        this.attachmentDataProvider = builder.attachmentDataProvider;
        this.recipientSettings = builder.recipientSettings;
        this.additionals = builder.additionals;
    }

    @Override
    @NonNull
    public SchedulingMethod getMethod() {
        return method;
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
    public InputStream getAttachmentData(int managedId) throws OXException {
        List<Event> calendarObject = getResource().getEvents();
        Check.containsAttachment(calendarObject, managedId);
        if (null == attachmentDataProvider) {
            String id = null;
            if (null != calendarObject.get(0)) {
                id = calendarObject.get(0).getId();
            }
            throw CalendarExceptionCodes.ATTACHMENT_NOT_FOUND.create(I(managedId), id);
        }
        return attachmentDataProvider.getAttachmentData(managedId);
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
        return "SchedulingMessage:[method:" + getMethod() + ", recipient:" + getRecipient().toString() + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 97;
        int result = 1;
        result = prime * result + ((additionals == null) ? 0 : additionals.hashCode());
        result = prime * result + scheduleChange.hashCode();
        result = prime * result + method.hashCode();
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
        if (!(obj instanceof Message)) {
            return false;
        }
        Message other = (Message) obj;
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
        if (method != other.method) {
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
