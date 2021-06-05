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

package com.openexchange.chronos.alarm.message.impl;

/**
 * {@link Key} is a identifying key for a {@link SingleMessageDeliveryTask}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
class Key {

    private final int cid, account, id;
    private final String eventId;

    /**
     * Initializes a new {@link MessageAlarmDeliveryWorker.key}.
     */
    public Key(int cid, int account, String eventId, int id) {
        this.cid = cid;
        this.account = account;
        this.id = id;
        this.eventId = eventId;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = hash * 31 + cid;
        hash = hash * 31 + account;
        hash = hash * 31 + eventId.hashCode();
        hash = hash * 31 + id;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Key) {
            return obj.hashCode() == this.hashCode();
        }
        return false;
    }

    /**
     * Gets the eventId
     *
     * @return The eventId
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Gets the cid
     *
     * @return The cid
     */
    public int getCid() {
        return cid;
    }

    /**
     * Gets the account
     *
     * @return The account
     */
    public int getAccount() {
        return account;
    }

    @Override
    public String toString() {
        return "Key [cid=" + cid + "|account=" + account + "|eventId=" + eventId + "|alarmId=" + id + "]";
    }

    /**
     * Gets the id
     *
     * @return The id
     */
    public int getId() {
        return id;
    }
}