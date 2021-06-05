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

package com.openexchange.drive.impl.checksum.events;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FolderID;

/**
 * {@link DelayedChecksumInvalidation}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DelayedChecksumInvalidation implements Delayed {

    /**
     * The delay for pooled invalidation messages: <code>2000ms</code>
     */
    private static final int DELAY_MSEC = 2000;

    private final FileID fileID;
    private final FolderID folderID;
    private final int contextID;
    private final String topic;
    private final long stamp;
    private final int hash;

    public DelayedChecksumInvalidation(int contextID, String topic, FolderID folderID, FileID fileID) {
        super();
        this.contextID = contextID;
        this.topic = topic;
        this.fileID = fileID;
        this.folderID = folderID;
        this.stamp = System.currentTimeMillis();
        final int prime = 31;
        int result = 1;
        result = prime * result + contextID;
        result = prime * result + ((fileID == null) ? 0 : fileID.hashCode());
        result = prime * result + ((folderID == null) ? 0 : folderID.hashCode());
        result = prime * result + ((topic == null) ? 0 : topic.hashCode());
        this.hash = result;
    }

    public DelayedChecksumInvalidation(int contextID, String topic, FileID fileID) {
        this(contextID, topic, null, fileID);
    }

    public DelayedChecksumInvalidation(int contextID, String topic, FolderID folderID) {
        this(contextID, topic, folderID, null);
    }

    public FileID getFileID() {
        return fileID;
    }

    public FolderID getFolderID() {
        return folderID;
    }

    public String getTopic() {
        return topic;
    }

    public int getContextID() {
        return contextID;
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
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof DelayedChecksumInvalidation)) {
            return false;
        }
        DelayedChecksumInvalidation other = (DelayedChecksumInvalidation) obj;
        if (contextID != other.contextID) {
            return false;
        }
        if (fileID == null) {
            if (other.fileID != null) {
                return false;
            }
        } else if (!fileID.equals(other.fileID)) {
            return false;
        }
        if (folderID == null) {
            if (other.folderID != null) {
                return false;
            }
        } else if (!folderID.equals(other.folderID)) {
            return false;
        }
        if (topic == null) {
            if (other.topic != null) {
                return false;
            }
        } else if (!topic.equals(other.topic)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(final Delayed o) {
        final long thisStamp = stamp;
        final long otherStamp = ((DelayedChecksumInvalidation) o).stamp;
        return (thisStamp < otherStamp ? -1 : (thisStamp == otherStamp ? 0 : 1));
    }

    @Override
    public long getDelay(final TimeUnit unit) {
        return unit.convert(DELAY_MSEC - (System.currentTimeMillis() - stamp), TimeUnit.MILLISECONDS);
    }

    @Override
    public String toString() {
        return "DelayedChecksumInvalidation [fileID=" + fileID + ", folderID=" + folderID + ", contextID=" + contextID + ", topic=" + topic + "]";
    }

}
