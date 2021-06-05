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

package com.openexchange.admin.taskmanagement;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * {@link TaskManagerThreadFactory} - The thread factory for task manager.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.8.0
 */
 final class TaskManagerThreadFactory implements ThreadFactory {

    private final AtomicInteger threadNumber;
    private final String namePrefix;

    /**
     * Initializes a new {@link TaskManagerThreadFactory}.
     */
    TaskManagerThreadFactory() {
        super();
        threadNumber = new AtomicInteger();
        this.namePrefix = "TaskManager-";
    }

    @Override
    public Thread newThread(Runnable r) {
        final Thread t = new Thread(r, getThreadName(threadNumber.incrementAndGet(), namePrefix));
        t.setUncaughtExceptionHandler(TaskManagerUncaughtExceptionhandler.getInstance());
        return t;
    }

    private static String getThreadName(int threadNumber, String namePrefix) {
        StringBuilder retval = new StringBuilder(namePrefix.length() + 7);
        retval.append(namePrefix);
        for (int i = threadNumber; i < 1000000; i *= 10) {
            retval.append('0');
        }
        retval.append(threadNumber);
        return retval.toString();
    }

}
