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

package com.openexchange.chronos.service;

import com.openexchange.chronos.RecurrenceId;

/**
 * {@link EventID}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class EventID {

    private final String folderID;
    private final String objectID;
    private final RecurrenceId recurrenceID;

    /**
     * Initializes a new {@link EventID}.
     *
     * @param folderID The folder ID
     * @param objectID The object ID
     */
    public EventID(String folderID, String objectID) {
        this(folderID, objectID, null);
    }

    /**
     * Initializes a new {@link EventID}.
     *
     * @param folderID The folder ID
     * @param objectID The object ID
     * @param recurrenceID The recurrence ID
     */
    public EventID(String folderID, String objectID, RecurrenceId recurrenceID) {
        super();
        this.folderID = folderID;
        this.objectID = objectID;
        this.recurrenceID = recurrenceID;
    }

    public String getFolderID() {
        return folderID;
    }

    public String getObjectID() {
        return objectID;
    }

    public RecurrenceId getRecurrenceID() {
        return recurrenceID;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((folderID == null) ? 0 : folderID.hashCode());
        result = prime * result + ((objectID == null) ? 0 : objectID.hashCode());
        result = prime * result + ((recurrenceID == null) ? 0 : recurrenceID.hashCode());
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        EventID other = (EventID) obj;
        if (folderID == null) {
            if (other.folderID != null) {
                return false;
            }
        } else if (!folderID.equals(other.folderID)) {
            return false;
        }
        if (objectID == null) {
            if (other.objectID != null) {
                return false;
            }
        } else if (!objectID.equals(other.objectID)) {
            return false;
        }
        if (recurrenceID == null) {
            if (other.recurrenceID != null) {
                return false;
            }
        } else if (!recurrenceID.equals(other.recurrenceID)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return null == recurrenceID ? "EventID [folderID=" + folderID + ", objectID=" + objectID + "]" : "EventID [folderID=" + folderID + ", objectID=" + objectID + ", recurrenceID=" + recurrenceID + "]";
    }

}
