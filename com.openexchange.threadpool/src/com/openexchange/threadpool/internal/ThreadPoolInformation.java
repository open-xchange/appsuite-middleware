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

package com.openexchange.threadpool.internal;

import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import com.openexchange.threadpool.ThreadPoolInformationMBean;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * {@link ThreadPoolInformation} - The thread pool information implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ThreadPoolInformation extends StandardMBean implements ThreadPoolInformationMBean {

    private final ThreadPoolService threadPoolService;

    /**
     * Initializes a new {@link ThreadPoolInformation}.
     *
     * @param threadPoolService The thread pool service
     * @throws NotCompliantMBeanException If the MBean interface does not follow JMX design patterns for Management Interfaces, or if this
     *             does not implement the specified interface.
     */
    public ThreadPoolInformation(final ThreadPoolService threadPoolService) throws NotCompliantMBeanException {
        super(ThreadPoolInformationMBean.class);
        this.threadPoolService = threadPoolService;
    }

    @Override
    public int getActiveCount() {
        return threadPoolService.getActiveCount();
    }

    @Override
    public long getCompletedTaskCount() {
        return threadPoolService.getCompletedTaskCount();
    }

    @Override
    public int getLargestPoolSize() {
        return threadPoolService.getLargestPoolSize();
    }

    @Override
    public int getPoolSize() {
        return threadPoolService.getPoolSize();
    }

    @Override
    public long getTaskCount() {
        return threadPoolService.getTaskCount();
    }

}
