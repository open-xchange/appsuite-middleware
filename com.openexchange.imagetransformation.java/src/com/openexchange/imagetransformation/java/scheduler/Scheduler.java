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

package com.openexchange.imagetransformation.java.scheduler;

import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.imagetransformation.java.osgi.Services;
import com.openexchange.processing.Processor;
import com.openexchange.processing.ProcessorService;
import com.openexchange.server.ServiceExceptionCode;

/**
 * {@link Scheduler}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.0
 */
public final class Scheduler {

    private static volatile Processor instance;

    /**
     * Shuts-down the processor
     */
    public static void shutDown() {
        Processor tmp = instance;
        if (null != tmp) {
            synchronized (Scheduler.class) {
                tmp = instance;
                if (null != tmp) {
                    tmp.stop();
                    instance = null;
                }
            }
        }
    }

    /**
     * Gets the processor instance; initializes it if not yet done.
     *
     * @return The instance
     */
    public static Processor getInstance() throws OXException {
        Processor tmp = instance;
        if (null == tmp) {
            synchronized (Scheduler.class) {
                tmp = instance;
                if (null == tmp) {
                    ProcessorService processorService = Services.optService(ProcessorService.class);
                    if (null == processorService) {
                        throw ServiceExceptionCode.absentService(ProcessorService.class);
                    }

                    ConfigurationService configService = Services.optService(ConfigurationService.class);
                    int defaultNumThreads = 10;
                    int numThreads = null == configService ? defaultNumThreads : configService.getIntProperty("com.openexchange.tools.images.scheduler.numThreads", defaultNumThreads);

                    tmp = processorService.newProcessor("ImageScheduler", numThreads);
                    instance = tmp;
                }
            }
        }
        return tmp;
    }

}
