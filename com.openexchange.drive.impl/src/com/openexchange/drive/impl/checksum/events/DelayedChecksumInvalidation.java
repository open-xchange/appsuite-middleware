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
