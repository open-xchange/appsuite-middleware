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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.database.migration.internal;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import liquibase.resource.ResourceAccessor;


/**
 * {@link ScheduledExecution}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class ScheduledExecution {

    private final Lock lock = new ReentrantLock();

    private final Condition wasExecuted = lock.newCondition();

    private final String fileLocation;

    private final ResourceAccessor resourceAccessor;

    private boolean done = false;

    /**
     * Initializes a new {@link ScheduledExecution}.
     * @param fileLocation
     * @param resourceAccessor
     */
    public ScheduledExecution(String fileLocation, ResourceAccessor resourceAccessor) {
        super();
        this.fileLocation = fileLocation;
        this.resourceAccessor = resourceAccessor;
    }

    public String getFileLocation() {
        return fileLocation;
    }

    public ResourceAccessor getResourceAccessor() {
        return resourceAccessor;
    }

    public void setExecuted() {
        lock.lock();
        try {
            done = true;
            wasExecuted.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void waitForExecution() throws InterruptedException {
        while (!done) {
            lock.lock();
            try {
                wasExecuted.await();
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fileLocation == null) ? 0 : fileLocation.hashCode());
        result = prime * result + ((resourceAccessor == null) ? 0 : resourceAccessor.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ScheduledExecution other = (ScheduledExecution) obj;
        if (fileLocation == null) {
            if (other.fileLocation != null)
                return false;
        } else if (!fileLocation.equals(other.fileLocation))
            return false;
        if (resourceAccessor == null) {
            if (other.resourceAccessor != null)
                return false;
        } else if (!resourceAccessor.equals(other.resourceAccessor))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ScheduledExecution [fileLocation=" + fileLocation + ", resourceAccessor=" + resourceAccessor + "]";
    }

}
