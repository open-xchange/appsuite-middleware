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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.service.indexing.old;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * {@link StandardIndexingJob} - The standard <code style="color: red;">abstract</code> {@link IndexingJob} to extend from.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class StandardIndexingJob implements IndexingJob {

    private static final long serialVersionUID = -603520334844563670L;

    /**
     * The job's priority; initially <code>4</code> (default).
     */
    protected volatile int priority;

    /**
     * The job's behavior; initially {@link Behavior#CONSUMER_RUNS}.
     * 
     * @see #setBehavior(com.openexchange.service.indexing.IndexingJob.Behavior)
     */
    protected volatile Behavior behavior;

    /**
     * The job' origin.
     */
    protected volatile Origin origin;

    /**
     * The time stamp.
     */
    protected final long timeStamp;

    /**
     * The properties.
     */
    protected final ConcurrentMap<String, ?> properties;

    /**
     * Initializes a new {@link StandardIndexingJob}.
     */
    protected StandardIndexingJob() {
        super();
        properties = new ConcurrentHashMap<String, Object>();
        timeStamp = System.currentTimeMillis();
        priority = DEFAULT_PRIORITY;
        behavior = DEFAULT_BEHAVIOR;
        origin = DEFAULT_ORIGIN;
    }

    @Override
    public Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

    @Override
    public Map<String, ?> getProperties() {
        return properties;
    }

    @Override
    public long getTimeStamp() {
        return timeStamp;
    }

    @Override
    public Origin getOrigin() {
        return origin;
    }

    @Override
    public boolean isDurable() {
        return true;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void setPriority(final int priority) {
        this.priority = priority;
    }

    @Override
    public Behavior getBehavior() {
        return behavior;
    }

    
    /**
     * Sets the origin
     *
     * @param origin The origin to set
     */
    public void setOrigin(final Origin origin) {
        this.origin = origin;
    }

    /**
     * Sets the behavior
     * 
     * @param behavior The behavior to set
     */
    public void setBehavior(final Behavior behavior) {
        this.behavior = behavior;
    }

    @Override
    public void beforeExecute() {
        // Nothing to do
    }

    @Override
    public void afterExecute(final Throwable t) {
        // Nothing to do
    }

}
