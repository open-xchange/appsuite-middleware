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

package com.openexchange.groupware.attach;

import java.util.UUID;

/**
 * {@link AttachmentBatch}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.0
 */
public class AttachmentBatch {

    private UUID batchId;

    private boolean finalElement;

    public AttachmentBatch(UUID batchId, boolean finalElement) {
        this.batchId = batchId;
        this.finalElement = finalElement;
    }

    /**
     * Gets the batchId
     *
     * @return The batchId
     */
    public UUID getBatchId() {
        return batchId;
    }

    /**
     * Sets the batchId
     *
     * @param batchId The batchId to set
     */
    public void setBatchId(UUID batchId) {
        this.batchId = batchId;
    }

    /**
     * Gets the finalElement
     *
     * @return The finalElement
     */
    public boolean isFinalElement() {
        return finalElement;
    }

    /**
     * Sets the finalElement
     *
     * @param finalElement The finalElement to set
     */
    public void setFinalElement(boolean finalElement) {
        this.finalElement = finalElement;
    }
}
