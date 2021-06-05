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

package com.openexchange.processing.internal;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import com.openexchange.exception.OXException;
import com.openexchange.processing.Processor;
import com.openexchange.processing.ProcessorService;


/**
 * {@link ProcessorServiceImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class ProcessorServiceImpl implements ProcessorService {

    private final AtomicInteger count;
    private final Queue<Processor> processors;

    /**
     * Initializes a new {@link ProcessorServiceImpl}.
     */
    public ProcessorServiceImpl() {
        super();
        count = new AtomicInteger();
        processors = new ConcurrentLinkedQueue<Processor>();
    }

    @Override
    public Processor newProcessor(String name, int numThreads) throws OXException {
        try {
            int id = count.incrementAndGet();
            Processor processor = new RoundRobinProcessor(null == name ? "Processor" + id : name, numThreads);
            processors.offer(processor);
            return processor;
        } catch (RuntimeException e) {
            throw OXException.general(e.getMessage(), e);
        }
    }

    @Override
    public Processor newBoundedProcessor(String name, int numThreads, int maxTasks) throws OXException {
        try {
            int id = count.incrementAndGet();
            Processor processor = new BoundedRoundRobinProcessor(null == name ? "Processor" + id : name, numThreads, maxTasks);
            processors.offer(processor);
            return processor;
        } catch (RuntimeException e) {
            throw OXException.general(e.getMessage(), e);
        }
    }

    /**
     * Shuts-down all processors
     */
    public void shutDownAll() {
        for (Processor processor; (processor = processors.poll()) != null;) {
            processor.stop();
        }
    }

}
