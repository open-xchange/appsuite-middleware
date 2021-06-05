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

package com.openexchange.processing;

import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link ProcessorService} - Appropriate for generating certain processors that are supposed to work-off certain tasks/jobs.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
@SingletonService
public interface ProcessorService {

    /**
     * Creates a new processor having given name and specified number of threads to process scheduled tasks.
     *
     * @param name The unique processor name or <code>null</code> to auto-generate a unique name; e.g. "ImageProcessor"
     * @param numThreads The number of threads to use
     * @return A new processor
     * @throws OXException If processor cannot be returned
     */
    Processor newProcessor(String name, int numThreads) throws OXException;

    /**
     * Creates a new bounded processor having given name and specified number of threads to process scheduled tasks.
     *
     * @param name The unique processor name or <code>null</code> to auto-generate a unique name; e.g. "ImageProcessor"
     * @param numThreads The number of threads to use
     * @param maxTasks The max. number of tasks that may reside in queue awaiting being processed
     * @return A new processor
     * @throws OXException If processor cannot be returned
     */
    Processor newBoundedProcessor(String name, int numThreads, int maxTasks) throws OXException;

}
