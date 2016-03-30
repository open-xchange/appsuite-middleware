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

package com.openexchange.control.internal;

import java.util.List;
import java.util.Map;
import javax.management.MBeanException;

/**
 * {@link GeneralControlMBean} - MBean interface for calls to OSGi framework.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface GeneralControlMBean {

    /**
     * Lists all active bundles while mapping bundle name to the bundle's current state which is one of UNINSTALLED, INSTALLED, RESOLVED,
     * STARTING, STOPPING, or ACTIVE.
     *
     * @return A bundle list
     */
    public List<Map<String, String>> list();

    /**
     * Starts the bundle denoted by specified bundle name.
     *
     * @param name The bundle name
     * @throws MBeanException If an appropriate bundle could not be found for given name
     */
    public void start(final String name) throws MBeanException;

    /**
     * Stops the bundle denoted by specified bundle name.
     *
     * @param name The bundle name
     * @throws MBeanException If an appropriate bundle could not be found for given name
     */
    public void stop(final String name) throws MBeanException;

    /**
     * This a convenience method that just invokes {@link #stop(String)} followed by {@link #start(String)} on the bundle denoted by
     * specified bundle name.
     *
     * @param name The bundle name
     * @throws MBeanException If an appropriate bundle could not be found for given name
     */
    public void restart(final String name) throws MBeanException;

    /**
     * Installs a bundle from the specified location string. A bundle is obtained from location as interpreted by the OSGi framework in an
     * implementation dependent manner. Every installed bundle is uniquely identified by its location string, typically in the form of a
     * URL.
     *
     * @param local The location identifier of the bundle to install.
     * @throws MBeanException If bundle installation fails
     */
    public void install(final String local) throws MBeanException;

    /**
     * Uninstalls the bundle denoted by specified bundle name.
     *
     * @param name The bundle name
     * @throws MBeanException If an appropriate bundle could not be found for given name
     */
    public void uninstall(final String name) throws MBeanException;

    /**
     * Updates the bundle denoted by specified bundle name.
     *
     * @param name The bundle name
     * @param autorefresh <code>true</code> to automatically refresh bundles for immediate usage; otherwise <code>false</code>
     * @throws MBeanException If an appropriate bundle could not be found for given name
     */
    public void update(final String name, final boolean autorefresh) throws MBeanException;

    /**
     * Refreshes all bundles.
     */
    public void refresh();

    /**
     * Shuts down the OSGi framework through invoking closure of top-level system bundle.
     */
    boolean shutdown();

    /**
     * Shuts down the OSGi framework through invoking closure of top-level system bundle.
     * @param waitForExit <code>true</code> to wait for the OSGi framework being shut down completely; otherwise <code>false</code>.
     */
    boolean shutdown(boolean waitForExit);

    /**
     * Lists all available registered services.
     * <p>
     * For each registered service its name, the bundle that registered the service and the bundles that are using the service are contained
     * in the map:
     *
     * <pre>
     * &quot;service&quot; -&gt; &lt;service-name&gt;
     * &quot;registered_by&quot; -&gt; &lt;name-of-the-bundle-that-registered-the-service&gt;
     * &quot;bundles&quot; -&gt; &lt;names-of-the-bundles-that-are-using-the-service&gt;
     * </pre>
     *
     * @return A list of available registered services.
     */
    public List<Map<String, Object>> services();

    /**
     * Gets the version of the OX groupware.
     *
     * @return The version number.
     */
    public String version();

    /**
     * Returns whether this node is currently executing any update tasks.
     * If so, you must not call <code>shutdown()</code> until this method
     * returns <code>false</code>.
     *
     * @return <code>true</code> if update tasks are currently running on
     * this node.
     */
    public boolean updateTasksRunning();

}
