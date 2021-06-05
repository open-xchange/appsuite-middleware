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

package com.openexchange.groupware.infostore.media.impl.control;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.DelayQueue;

/**
 * {@link ExtractControl} - A registry for threads currently extracting media metadata.
 * <p>
 * This registry manages a {@link DelayQueue} to track expired tasks.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class ExtractControl {

    private static final ExtractControl INSTANCE = new ExtractControl();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static ExtractControl getInstance() {
        return INSTANCE;
    }

    // ------------------------------------------------------------------------------------------------------------------------

    private final DelayQueue<ExtractAndApplyMediaMetadataTask> queue;

    /**
     * Initializes a new {@link ExtractControl}.
     */
    private ExtractControl() {
        super();
        queue = new DelayQueue<ExtractAndApplyMediaMetadataTask>();
    }

    /**
     * Adds the specified task.
     *
     * @param task The task
     * @return <tt>true</tt>
     */
    public boolean add(ExtractAndApplyMediaMetadataTask task) {
        return queue.offer(task);
    }

    /**
     * Removes the specified task.
     *
     * @param task The task to remove
     * @return <code>true</code> if such a task was removed; otherwise <code>false</code>
     */
    public boolean remove(ExtractAndApplyMediaMetadataTask task) {
        return queue.remove(task);
    }

    /**
     * Await expired extractors from this control.
     *
     * @return The expired extractors
     * @throws InterruptedException If interrupted while waiting
     */
    List<ExtractAndApplyMediaMetadataTask> awaitExpired() throws InterruptedException {
        ExtractAndApplyMediaMetadataTask expired = queue.take();
        List<ExtractAndApplyMediaMetadataTask> expirees = new LinkedList<ExtractAndApplyMediaMetadataTask>();
        expirees.add(expired);
        queue.drainTo(expirees);
        return expirees;
    }

    /**
     * Removes expired extractors from this control.
     *
     * @return The expired extractors
     */
    List<ExtractAndApplyMediaMetadataTask> removeExpired() {
        List<ExtractAndApplyMediaMetadataTask> expirees = new LinkedList<ExtractAndApplyMediaMetadataTask>();
        queue.drainTo(expirees);
        return expirees;
    }

}
