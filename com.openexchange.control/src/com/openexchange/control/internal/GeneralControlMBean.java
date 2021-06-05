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
    
    public static final String DOMAIN = "com.openexchange.control";
    public static final String MBEAN_NAME = "Control";

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
