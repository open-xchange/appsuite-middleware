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

package com.openexchange.mail.event;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import com.openexchange.push.PushEventConstants;
import com.openexchange.session.Session;

/**
 * {@link PooledEvent} - A pooled event.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PooledEvent implements Delayed {

    private volatile long stamp;
    private final int contextId;
    private final int userId;
    private final int accountId;
    private final String topic;
    private final String fullname;
    private final Session session;
    private final boolean contentRelated;
    private final boolean immediateDelivery;
    private final boolean remote;
    private final int hash;
    private boolean async;
    private final Map<String, Object> properties;

    /**
     * Initializes a new {@link PooledEvent} with {@link PushEventConstants#TOPIC default topic}.
     *
     * @param contextId The context ID
     * @param userId The user ID
     * @param accountId The account ID
     * @param fullname The folder fullname
     * @param contentRelated <code>true</code> for a content-related event; otherwise <code>false</code>
     * @param immediateDelivery <code>true</code> for immediate delivery; otherwise <code>false</code>
     * @param session The session
     */
    public PooledEvent(int contextId, int userId, int accountId, String fullname, boolean contentRelated, boolean immediateDelivery, boolean remote, Session session) {
        this(PushEventConstants.TOPIC, contextId, userId, accountId, fullname, contentRelated, immediateDelivery, remote, session);
    }

    /**
     * Initializes a new {@link PooledEvent}.
     *
     * @param contextId The context ID
     * @param userId The user ID
     * @param accountId The account ID
     * @param fullname The folder fullname
     * @param contentRelated <code>true</code> for a content-related event; otherwise <code>false</code>
     * @param immediateDelivery <code>true</code> for immediate delivery; otherwise <code>false</code>
     * @param session The session
     */
    public PooledEvent(String topic, int contextId, int userId, int accountId, String fullname, boolean contentRelated, boolean immediateDelivery, boolean remote, Session session) {
        super();
        properties = new HashMap<String, Object>(4);
        async = true;
        this.topic = topic;
        stamp = System.currentTimeMillis();
        this.contextId = contextId;
        this.userId = userId;
        this.accountId = accountId;
        this.fullname = fullname;
        this.contentRelated = contentRelated;
        this.immediateDelivery = immediateDelivery;
        this.session = session;
        this.remote = remote;
        // Hash code
        final int prime = 31;
        int result = 1;
        result = prime * result + ((topic == null) ? 0 : topic.hashCode());
        result = prime * result + accountId;
        result = prime * result + contextId;
        result = prime * result + ((fullname == null) ? 0 : fullname.hashCode());
        result = prime * result + userId;
        result = prime * result + (contentRelated ? 1 : 0);
        result = prime * result + (immediateDelivery ? 1 : 0);
        hash = result;
    }

    /**
     * Puts given property.
     *
     * @param name The property name
     * @param value The property value
     * @return This pooled event with property applied
     */
    public PooledEvent putProperty(String name, Object value) {
        properties.put(name, value);
        return this;
    }

    /**
     * Gets given property.
     *
     * @param name The property name
     */
    public Object getProperty(String name) {
        return properties.get(name);
    }

    /**
     * Removes given property.
     *
     * @param name The property name
     */
    public void removesProperty(String name) {
        properties.remove(name);
    }

    /**
     * Gets the properties.
     *
     * @return The properties
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * Sets whether the event should be delivered asynchronously (default behavior). If <code>false</code>
     * {@link EventPool#put(PooledEvent)} does not return to the caller until delivery of the event is completed.
     * <p>
     * <b>Note</b>: Works only if this pooled event is considered for immediate delivery.
     *
     * @param async <code>true</code> to deliver asynchronously; otherwise <code>false</code>
     * @return This pooled event with new behavior applied
     */
    public PooledEvent setAsync(boolean async) {
        this.async = async;
        return this;
    }

    /**
     * Checks whether the event should be delivered asynchronously.
     *
     * @return <code>true</code> to deliver asynchronously; otherwise <code>false</code>
     */
    public boolean isAsync() {
        /*
         * Asynchronous delivery if either non-immediate delivery or async
         */
        return !immediateDelivery || async;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return immediateDelivery ? 0L : unit.convert(EventPool.MSEC_DELAY - (System.currentTimeMillis() - stamp), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        final long thisStamp = stamp;
        final long otherStamp = ((PooledEvent) o).stamp;
        return (thisStamp < otherStamp ? -1 : (thisStamp == otherStamp ? 0 : 1));
    }

    /**
     * Gets the topic for this event.
     *
     * @return The topic
     */
    public String getTopic() {
        return topic;
    }

    /**
     * Touches this pooled notification; meaning its last-accessed time stamp is set to now.
     */
    public void touch() {
        stamp = System.currentTimeMillis();
    }

    /**
     * Gets this pooled notification's last-accessed time stamp.
     *
     * @return The last-accessed time stamp.
     */
    public long lastAccessed() {
        return stamp;
    }

    /**
     * Checks if this pooled events matches specified user ID and context ID.
     *
     * @param userId The user ID
     * @param contextId The context ID
     * @return <code>true</code> this pooled events matches; otherwise <code>false</code>
     */
    public boolean equalsByUser(int userId, int contextId) {
        return this.userId == userId && this.contextId == contextId;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PooledEvent)) {
            return false;
        }
        final PooledEvent other = (PooledEvent) obj;
        if (accountId != other.accountId) {
            return false;
        }
        if (contextId != other.contextId) {
            return false;
        }
        if (userId != other.userId) {
            return false;
        }
        if (contentRelated != other.contentRelated) {
            return false;
        }
        if (topic == null) {
            if (other.topic != null) {
                return false;
            }
        } else if (!topic.equals(other.topic)) {
            return false;
        }
        if (fullname == null) {
            if (other.fullname != null) {
                return false;
            }
        } else if (!fullname.equals(other.fullname)) {
            return false;
        }
        return true;
    }

    /**
     * Gets the context ID.
     *
     * @return The context ID
     */
    public int getContextId() {
        return contextId;
    }

    /**
     * Gets the user ID.
     *
     * @return The user ID
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Gets the account ID.
     *
     * @return The account ID
     */
    public int getAccountId() {
        return accountId;
    }

    /**
     * Gets the full name
     *
     * @return The full name
     */
    public String getFullname() {
        return fullname;
    }

    /**
     * Gets the session
     *
     * @return The session
     */
    public Session getSession() {
        return session;
    }

    /**
     * Checks for a content-related event.
     *
     * @return <code>true</code> for a content-related event; otherwise <code>false</code>
     */
    public boolean isContentRelated() {
        return contentRelated;
    }

    /**
     * Checks if this event is supposed to be distributed remotely.
     *
     * @return <code>true</code> for remote distribution; otherwise <code>false</code>
     */
    public boolean isRemote() {
        return remote;
    }

}
