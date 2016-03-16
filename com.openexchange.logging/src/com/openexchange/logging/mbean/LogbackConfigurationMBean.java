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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.logging.mbean;

import java.util.List;
import java.util.Map;
import java.util.Set;
import ch.qos.logback.classic.Level;
import com.openexchange.management.MBeanMethodAnnotation;

/**
 * {@link LogbackConfigurationMBean}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface LogbackConfigurationMBean {

    public static final String DOMAIN = "com.openexchange.logging";

    public static final String KEY = "name";

    public static final String VALUE = "Logging Configuration";

    static final String DESCRIPTION = "Management Bean for the Logback Configuration";

    /**
     * Filter context
     * @param contextID
     * @return response object
     */
    @MBeanMethodAnnotation (description="Create a filter for the specified contextID", parameters={"contextID", "loggers"}, parameterDescriptions={"The contextID for which to apply the filter", "A Map of logger/level pairs"})
    public LogbackMBeanResponse filterContext(int contextID, Map<String, Level> loggers);

    /**
     * Filter user
     * @param userID
     * @param contextID
     */
    @MBeanMethodAnnotation (description="Create a filter for the specified userID contextID combo", parameters={"userID", "contextID", "loggers"}, parameterDescriptions={"The userID for which to apply the filter", "The contextID for which to apply the filter", "A Map of logger/level pairs"})
    public LogbackMBeanResponse filterUser(int userID, int contextID, Map<String, Level> loggers);

    /**
     * Filter session
     * @param sessionID
     */
    @MBeanMethodAnnotation (description="Create a filter for the specified sessionID", parameters={"sessionID", "loggers"}, parameterDescriptions={"The sessionID for which to apply the filter", "A Map of logger/level pairs"})
    public LogbackMBeanResponse filterSession(String sessionID, Map<String, Level> loggers);

    /**
     * Modifies the specified level for the specified loggers
     * @param loggers
     */
    @MBeanMethodAnnotation (description="Set the log levels for the specified set of loggers", parameters={"loggers"}, parameterDescriptions={"A Map of logger/level pairs"})
    public LogbackMBeanResponse modifyLogLevels(Map<String, Level> loggers);

    /**
     * Overrides Exception categories to be suppressed (comma separated).
     * @param categories
     */
    @MBeanMethodAnnotation (description="Overrides Exception categories to be suppressed (comma separated).", parameters={"categories"}, parameterDescriptions={"The categories to be suppressed when logging."})
    public LogbackMBeanResponse overrideExceptionCategories(String categories);

    /**
     * Returns the Exception categories to be suppressed (comma separated).
     * @return
     */
    @MBeanMethodAnnotation (description="Returns the Exception categories to be suppressed.", parameters={}, parameterDescriptions={})
    public Set<String> listExceptionCategories();

    /**
     * Remove the context filter
     * @param contextID
     * @param loggers loggers to remove from the filter (if the list is empty, then the entire filter is removed)
     */
    @MBeanMethodAnnotation (description="Remove the context filter for the specified contextID", parameters={"contextID", "loggers"}, parameterDescriptions={"The contextID for which to remove the logging filter", "Loggers to remove from the filter"})
    public LogbackMBeanResponse removeContextFilter(int contextID, List<String> loggers);

    /**
     * Remove the user filter
     * @param userID
     * @param contextID
     * @param loggers loggers to remove from the filter (if the list is empty, then the entire filter is removed)
     */
    @MBeanMethodAnnotation (description="Remove the user filter for the specified userID, contextID combo", parameters={"userID", "contextID", "loggers"}, parameterDescriptions={"The userID for which to remove the logging filter", "The contextID for which to remove the logging filter", "Loggers to remove from the filter"})
    public LogbackMBeanResponse removeUserFilter(int userID, int contextID, List<String> loggers);

    /**
     * Remove the session filter
     * @param sessionID
     * @param loggers loggers to remove from the filter (if the list is empty, then the entire filter is removed)
     */
    @MBeanMethodAnnotation (description="Remove the session filter for the specified sessionID", parameters={"sessionID", "loggers"}, parameterDescriptions={"The sessionID for which to remove the logging filter", "Loggers to remove from the filter"})
    public LogbackMBeanResponse removeSessionFilter(String sessionID, List<String> loggers);

    /**
     * Returns a list with all loggers of the system along with their log level.
     *
     * @return
     */
    @MBeanMethodAnnotation (description="Return a list with all system's loggers along with their assigned log level", parameters={}, parameterDescriptions={})
    public Set<String> listAllLoggers();

    /**
     * Return a list with all loggers that were dynamically modified along with their assigned log level
     *
     * @return
     */
    @MBeanMethodAnnotation (description="Return a list with all loggers that were dynamically modified along with their assigned log level", parameters={}, parameterDescriptions={})
    public Set<String> listDynamicallyModifiedLoggers();

    /**
     * Return a list with only the specified loggers
     *
     * @param loggers
     * @return
     */
    @MBeanMethodAnnotation (description="Return a list with the specified system's loggers along with their assigned log level", parameters={"loggers"}, parameterDescriptions={"Specified loggers to return"})
    public Set<String> getLevelForLoggers(String[] loggers);

    /**
     * Returns a list with all logging filters
     *
     * @return
     */
    @MBeanMethodAnnotation (description="Return a list with all logging filters", parameters={}, parameterDescriptions={})
    public Set<String> listFilters();

    /**
     * Removes all filters
     */
    @MBeanMethodAnnotation (description="Remove all logging filters", parameters={}, parameterDescriptions={})
    public LogbackMBeanResponse clearFilters();

    /**
     * Filter user
     * @param userID
     * @param contextID
     */
    @MBeanMethodAnnotation (description="Sets whether to include stack traces in HTTP-API JSON responses for userID contextID combo", parameters={"userID", "contextID", "enable"}, parameterDescriptions={"The userID for which to apply the setting", "The contextID for which to apply the setting", "Whether to enable or disable to include stack traces in HTTP-API JSON responses"})
    public void includeStackTraceForUser(int userID, int contextID, boolean enable);

    /**
     * Get root appender statistics
     */
    @MBeanMethodAnnotation (description="Gets an info string about all root appenders", parameterDescriptions={}, parameters={})
    public String getRootAppenderStats();

}
