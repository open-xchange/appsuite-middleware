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

package com.openexchange.service.indexing;

import java.util.List;
import java.util.Map;
import javax.management.MBeanException;
import javax.management.ObjectName;

/**
 * {@link JobMonitoringMBean} - A MBean for monitoring indexing jobs.
 * The {@link ObjectName} under that the instance of this MBean is registered is created like this:<br>
 * <code>new ObjectName(JobMonitoringMBean.DOMAIN, JobMonitoringMBean.KEY, JobMonitoringMBean.VALUE);</code>.
 * Any JMX client should instantiate it in the same way to avoid naming errors.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public interface JobMonitoringMBean {
    
    static final String DOMAIN = "com.openexchange.service.indexing";

    static final String KEY = "type";

    static final String VALUE = "indexingServiceMonitoring";

    /**
     * @return The number of job infos stored in the cluster.
     * @throws MBeanException
     */
    int countStoredJobInfos() throws MBeanException;
    
    /**
     * @return A list of ids of all job infos stored in the cluster.
     * @throws MBeanException
     */
    List<String> getStoredJobInfos() throws MBeanException;

    /**
     * @return The number of job triggers that are stored locally on this node.
     * @throws MBeanException
     */
    int countLocalTriggers() throws MBeanException;
    
    /**
     * @return A list of trigger names that are stored locally on this node.
     * @throws MBeanException
     */
    List<String> getLocalTriggers() throws MBeanException;

    /**
     * @return The number of currently running jobs on this node.
     * @throws MBeanException
     */
    int countRunningJobs() throws MBeanException;

    /**
     * Returns the names of all currently running jobs on this node.
     * The maps key is the name of the jobs trigger. The value is the name of the job itself.
     *
     * @return The job names.
     * @throws MBeanException
     */
    Map<String, String> getRunningJobs() throws MBeanException;
    
    /**
     * Returns a human-readable String that contains detailed information about the
     * locally stored trigger with the given name. If no trigger exists for the given name,
     * <code>null</code> is returned.
     * 
     * @param triggerName The triggers name (as returned by {@link #getLocalTriggers()}).
     * @return
     * @throws MBeanException
     */
    String getTriggerDetails(String triggerName) throws MBeanException;
    
    /**
     * Returns a given jobs recurrence details as a human-readable String. 
     * If no job exists for the given key, <code>null</code> is returned.
     * 
     * @param jobKey The jobs key (as returned by {@link #getStoredJobInfos()}).
     * @return
     * @throws MBeanException
     */
    String getRecurrenceDetails(String jobKey) throws MBeanException;

}
