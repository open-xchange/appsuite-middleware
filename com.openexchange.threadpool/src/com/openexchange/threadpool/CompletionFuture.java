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

package com.openexchange.threadpool;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * {@link CompletionFuture} - Consumers <tt>take</tt> completed tasks and process their results in the order they complete. A
 * <tt>CompletionFuture</tt> can for example be used to manage asynchronous IO, in which tasks that perform reads are submitted in one part
 * of a program or system, and then acted upon in a different part of the program when the reads complete, possibly in a different order
 * than they were requested..
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface CompletionFuture<V> {

    /**
     * Retrieves and removes the Future representing the next completed task, waiting if none are yet present.
     *
     * @return The Future representing the next completed task
     * @throws InterruptedException If interrupted while waiting.
     */
    Future<V> take() throws InterruptedException;

    /**
     * Retrieves and removes the Future representing the next completed task or <tt>null</tt> if none are present.
     *
     * @return The Future representing the next completed task, or <tt>null</tt> if none are present.
     */
    Future<V> poll();

    /**
     * Retrieves and removes the Future representing the next completed task, waiting if necessary up to the specified wait time if none are
     * yet present.
     *
     * @param timeout How long to wait before giving up, in units of <tt>unit</tt>
     * @param unit A <tt>TimeUnit</tt> determining how to interpret the <tt>timeout</tt> parameter
     * @return The Future representing the next completed task or <tt>null</tt> if the specified waiting time elapses before one is present.
     * @throws InterruptedException If interrupted while waiting.
     */
    Future<V> poll(long timeout, TimeUnit unit) throws InterruptedException;

}
