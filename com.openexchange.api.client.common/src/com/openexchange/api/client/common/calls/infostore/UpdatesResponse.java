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

package com.openexchange.api.client.common.calls.infostore;

import java.util.List;
import com.openexchange.file.storage.DefaultFile;

/**
 * {@link UpdatesResponse} - The response from an "updates" call.
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class UpdatesResponse {

    private final List<DefaultFile> newFiles;
    private final List<DefaultFile> modifiedFiles;
    private final List<DefaultFile> deletedFiles;
    private final long sequenceNumber;

    /**
     * Initializes a new {@link UpdatesResponse}.
     *
     * @param newFiles A list of new files
     * @param modifiedFiles A list of modified files
     * @param deletedFiles A list of deleted files
     * @param sequenceNumber The sequence number of the item
     */
    public UpdatesResponse(List<DefaultFile> newFiles, List<DefaultFile> modifiedFiles, List<DefaultFile> deletedFiles, long sequenceNumber) {
        this.newFiles = newFiles;
        this.modifiedFiles = modifiedFiles;
        this.deletedFiles = deletedFiles;
        this.sequenceNumber = sequenceNumber;
    }

    /**
     * Gets the newFiles
     *
     * @return The newFiles
     */
    public List<DefaultFile> getNewFiles() {
        return newFiles;
    }

    /**
     * Gets the modifiedFiles
     *
     * @return The modifiedFiles
     */
    public List<DefaultFile> getModifiedFiles() {
        return modifiedFiles;
    }

    /**
     * Gets the deletedFiles
     *
     * @return The deletedFiles
     */
    public List<DefaultFile> getDeletedFiles() {
        return deletedFiles;
    }

    /**
     * Gets the sequenceNumber
     *
     * @return The sequenceNumber
     */
    public long getSequenceNumber() {
        return sequenceNumber;
    }
}
