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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package org.quartz.service;

import org.quartz.CronScheduleBuilder;
import org.quartz.Scheduler;

/**
 * {@link QuartzService} - An OSGi service wrapped around <a href="http://quartz-scheduler.org/">Quartz</a> scheduler.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface QuartzService {

    /**
     * The property name for a Cron expression ({@link CronScheduleBuilder#cronSchedule(String)}).
     * <p>
     * <table border="0" cellpadding="1" cellspacing="0">
     * <tr align="left">
     * <th bgcolor="#CCCCFF" align="left" id="construct">Example</th>
     * <th bgcolor="#CCCCFF" align="left" id="matches">Description</th>
     * </tr>
     * <tr>
     * <td valign="top"><tt>"0/20 * * * * ?"</tt></td>
     * <td headers="matches">Run every 20 seconds</td>
     * </tr>
     * <tr>
     * <td valign="top"><tt>"15 0/2 * * * ?"</tt></td>
     * <td headers="matches">Run every other minute (at 15 seconds past the minute)</td>
     * </tr>
     * <tr>
     * <td valign="top"><tt>"0 0/2 8-17 * * ?"</tt></td>
     * <td headers="matches">Run every other minute but only between 8am and 5pm</td>
     * </tr>
     * <tr>
     * <td valign="top"><tt>"0 0/3 17-23 * * ?"</tt></td>
     * <td headers="matches">Run every three minutes but only between 5pm and 11pm</td>
     * </tr>
     * <tr>
     * <td valign="top"><tt>"0 0 10am 1,15 * ?"</tt></td>
     * <td headers="matches">Run at 10am on the 1st and 15th days of the month</td>
     * </tr>
     * <tr>
     * <td valign="top"><tt>"0,30 * * ? * MON-FRI"</tt></td>
     * <td headers="matches">Run every 30 seconds but only on Weekdays (Monday through Friday)</td>
     * </tr>
     * <tr>
     * <td valign="top"><tt>"0,30 * * ? * SAT,SUN"</tt></td>
     * <td headers="matches">Run every 30 seconds but only on Weekends (Saturday and Sunday)</td>
     * </tr>
     * </table>
     */
    public static final String PROPERTY_CRON_EXPRESSION = "quartz.cronExpression";

    /**
     * Gets the main scheduler started on bundle start-up.
     * 
     * @return The main scheduler
     */
    Scheduler getScheduler();

}
