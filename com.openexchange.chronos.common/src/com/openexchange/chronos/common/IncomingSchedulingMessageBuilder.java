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

package com.openexchange.chronos.common;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import com.openexchange.annotation.NonNull;
import com.openexchange.chronos.CalendarObjectResource;
import com.openexchange.chronos.scheduling.IncomingSchedulingMessage;
import com.openexchange.chronos.scheduling.IncomingSchedulingObject;
import com.openexchange.chronos.scheduling.SchedulingMethod;

/**
 * {@link IncomingSchedulingMessageBuilder}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
public class IncomingSchedulingMessageBuilder {

    protected SchedulingMethod method;
    protected int targetUser;
    protected IncomingSchedulingObject schedulingObject;
    protected CalendarObjectResource resource;
    protected Date timestamp;

    @NonNull
    protected Map<String, Object> additionals = new HashMap<>(5);

    /**
     * Initializes a new {@link IncomingSchedulingMessageBuilder}.
     */
    private IncomingSchedulingMessageBuilder() {}

    public static IncomingSchedulingMessageBuilder newBuilder() {
        return new IncomingSchedulingMessageBuilder();
    }

    /**
     * Sets the method
     *
     * @param method The method to set
     * @return This instance for chaining
     */
    public IncomingSchedulingMessageBuilder setMethod(SchedulingMethod method) {
        this.method = method;
        return this;
    }

    /**
     * Sets the object that triggered the scheduling
     *
     * @param schedulingObject The object to set
     * @return This instance for chaining
     */
    public IncomingSchedulingMessageBuilder setSchedulingObject(IncomingSchedulingObject schedulingObject) {
        this.schedulingObject = schedulingObject;
        return this;
    }

    /**
     * Sets the targetUser
     *
     * @param targetUser The targetUser to set
     * @return This instance for chaining
     */
    public IncomingSchedulingMessageBuilder setTargetUser(int targetUser) {
        this.targetUser = targetUser;
        return this;
    }

    /**
     * Sets the resource
     *
     * @param resource The resource to set
     * @return This instance for chaining
     */
    public IncomingSchedulingMessageBuilder setResource(CalendarObjectResource resource) {
        this.resource = resource;
        return this;
    }

    /**
     * Set the timestamp to apply during update operations
     *
     * @param timestamp The timestamp to set
     * @return This instance for chaining
     */
    public IncomingSchedulingMessageBuilder setTimeStamp(Date timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    /**
     * Sets an additionals value
     * 
     * @param key The key
     * @param value The value to set
     * @return This instance for chaining
     */
    public IncomingSchedulingMessageBuilder addAdditionals(String key, Object value) {
        this.additionals.put(key, value);
        return this;
    }

    /**
     * Builds a new {@link IncomingSchedulingMessage}
     *
     * @return A new {@link IncomingSchedulingMessage}
     * @throws IllegalStateException In case data is not set
     */
    public IncomingSchedulingMessage build() throws IllegalStateException {
        return new Incoming(this);

    }
}

class Incoming implements IncomingSchedulingMessage {

    private final @NonNull SchedulingMethod method;
    private final int targetUser;
    private final @NonNull IncomingSchedulingObject schedulingObject;
    private final @NonNull CalendarObjectResource resource;
    private final @NonNull Date timestamp;
    private final @NonNull Map<String, Object> additionals;

    /**
     * Initializes a new {@link Incoming}.
     * 
     * @param builder The builder
     * @throws IllegalStateException In case data is not set
     *
     */
    @SuppressWarnings("null")
    public Incoming(IncomingSchedulingMessageBuilder builder) throws IllegalStateException {
        this.method = notNull(builder.method);
        this.schedulingObject = notNull(builder.schedulingObject);
        this.targetUser = builder.targetUser;
        if (targetUser <= 0) {
            throw new IllegalStateException("Target user must be set");
        }
        this.resource = notNull(builder.resource);
        this.timestamp = null == builder.timestamp ? new Date() : builder.timestamp;
        this.additionals = builder.additionals;
    }

    @Override
    public @NonNull SchedulingMethod getMethod() {
        return method;
    }

    @Override
    public int getTargetUser() {
        return targetUser;
    }

    @Override
    @NonNull
    public IncomingSchedulingObject getSchedulingObject() {
        return schedulingObject;
    }

    @Override
    public @NonNull CalendarObjectResource getResource() {
        return resource;
    }

    @Override
    @NonNull
    public Date getTimeStamp() {
        return timestamp;
    }

    @Override
    public <T> Optional<T> getAdditional(String key, Class<T> clazz) {
        Object object = additionals.get(key);
        if (null != object && clazz.isAssignableFrom(object.getClass())) {
            return Optional.of(clazz.cast(object));
        }
        return Optional.empty();
    }

    private @NonNull <T> T notNull(T object) throws IllegalStateException {
        if (null == object) {
            throw new IllegalStateException("Object most not be null");
        }
        return object;
    }

}
