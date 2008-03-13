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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.tools.servlet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * The ServletConfigLoader is used to discover init parameters for servlets.
 * Some 3rd party servlets prefer to be configured via init parameters in the
 * web.xml. Our curstom Servlet Container doesn't implement any handling for the
 * web.xml, so this class tries to improve the situation for 3rd party servlets
 * a bit. This class discovers property files for init parameters in the
 * following fashion:
 * 
 * Recall the format of the servletmapping.properties:
 * /some/path:com.openexchange.servlets.MyServlet
 * 
 * The ServletConfigLoader will look for properties files to provide the init
 * parameters below the directory given in the constructor. It will try to find
 * a File com.openexchange.servlets.MyServlet.properties for the parameters.
 * Additionally it will load all .properties files in some/path below the given
 * directory. Properties in some/path will override properties in the base file.
 * The discovery mechanism will ignore all wildcards in the paths (/some/path*
 * will be regarded as /some/path).
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com>Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class ServletConfigLoader {

	private static ServletConfigLoader defaultInstance;

	/**
	 * The file extension for property files
	 */
	private static final String FILEEXT_PROPERTIES = ".properties";

	private static final AtomicBoolean initialized = new AtomicBoolean();

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(ServletConfigLoader.class);

	/**
	 * Puts properties into map
	 * 
	 * @param m -
	 *            the destination map
	 * @param props -
	 *            the source properties
	 */
	private static void addProps(final Map<String, String> m, final Properties props) {
		for (final Map.Entry<Object, Object> entry : props.entrySet()) {
			m.put((String) entry.getKey(), (String) entry.getValue());
		}
	}

	/**
	 * Constructs a <code>ServletConfig</code> instance from given property
	 * map
	 * 
	 * @param props -
	 *            the property map
	 * @return a <code>ServletConfig</code> instance containing all elements
	 *         of given property map
	 */
	private static ServletConfigWrapper buildConfig(final Map<String, String> props, final String clazz) {
		final ServletConfigWrapper config = new ServletConfigWrapper();
		config.setInitParameter(props);
		config.setServletContextWrapper(new ServletContextWrapper(config));
		config.setServletName(getClassName(clazz));
		return config;
	}

	/**
	 * Gets the sole class name
	 * 
	 * @param fullyQualifiedName
	 *            The fully-qualified class name
	 * @return The sole class name
	 */
	private static String getClassName(final String fullyQualifiedName) {
		final int pos;
		if ((pos = fullyQualifiedName.lastIndexOf('.')) > 0) {
			return fullyQualifiedName.substring(pos + 1);
		}
		return fullyQualifiedName;
	}

	/**
	 * Gets the default instance
	 * 
	 * @return The default instance
	 */
	public static ServletConfigLoader getDefaultInstance() {
		return defaultInstance;
	}

	/**
	 * Removes all wildcard characters (<code>*</code> and <code>?</code>)
	 * from specified path
	 * 
	 * @param path
	 *            The path
	 * @return The path with wildcard characters stripped off
	 */
	private static String ignoreWildcards(final String path) {
		return path.replaceAll("\\*|\\?", "");
	}

	/**
	 * Initializes the default instance
	 * 
	 * @param servletConfigDir
	 *            The servlet config directory
	 */
	public static void initDefaultInstance(final String servletConfigDir) {
		if (!initialized.get()) {
			synchronized (initialized) {
				if (initialized.get()) {
					return;
				}
				final ServletConfigWrapper servletConfig = new ServletConfigWrapper();
				final ServletContextWrapper servletContext = new ServletContextWrapper(servletConfig);
				servletConfig.setServletContextWrapper(servletContext);
				defaultInstance = new ServletConfigLoader(servletConfig, servletContext, new File(servletConfigDir));
				initialized.set(true);
			}
		}
	}

	/**
	 * Loads all properties files from specified directory
	 * 
	 * @param dir
	 *            The directory containing properties files
	 * @return A map containing all properties
	 */
	private static Map<String, String> loadDirProps(final File dir) {
		final Map<String, String> m = new HashMap<String, String>();
		if (dir.exists() && dir.isDirectory()) {
			for (final File f : dir.listFiles()) {
				if (f.isFile() && f.getName().toLowerCase().endsWith(FILEEXT_PROPERTIES)) {
					m.putAll(loadProperties(f));
				}
			}
			return m;
		}
		return null;
	}

	/**
	 * Loads the properties file denoted by specified argument
	 * <code>propFile</code>
	 * 
	 * @param propFile
	 *            The properties file
	 * @return A map containing the properties
	 */
	private static Map<String, String> loadProperties(final File propFile) {
		if (!propFile.exists()) {
			return null;
		}
		final Properties props = new Properties();
		InputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(propFile));
			props.load(in);
		} catch (final IOException x) {
			return null;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (final IOException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
		final Map<String, String> m = new HashMap<String, String>();
		addProps(m, props);
		return m;
	}

	/**
	 * Resets the default instance
	 */
	public static void resetDefaultInstance() {
		if (initialized.get()) {
			synchronized (initialized) {
				if (!initialized.get()) {
					return;
				}
				defaultInstance = null;
				initialized.set(false);
			}
		}
	}

	/**
	 * Remembers failed lookups on class property files
	 */
	private final transient Set<String> clazzGuardian = new HashSet<String>();

	/**
	 * Cache class properties
	 */
	private final transient Map<String, Map<String, String>> clazzProps = new HashMap<String, Map<String, String>>();

	private transient ServletConfig defaultConfig;

	private transient ServletContext defaultContext;

	private File directory;

	private Map<String, String> globalProps;

	/**
	 * Remembers failed lookups on path property files
	 */
	private final transient Set<String> pathGuardian = new HashSet<String>();

	/**
	 * Cache path properties
	 */
	private final transient Map<String, Map<String, String>> pathProps = new HashMap<String, Map<String, String>>();

	/**
	 * Initializes a new servlet config loader
	 * 
	 * @param directory
	 *            The directory containing properties files
	 */
	public ServletConfigLoader(final File directory) {
		this.directory = directory;
		globalProps = loadDirProps(this.directory);
	}

	/**
	 * Initializes a new servlet config loader
	 * 
	 * @param servletConfig
	 *            The default servlet config
	 * @param servletContext
	 *            The default servlet context
	 * @param directory
	 *            The directory containing global property files
	 */
	private ServletConfigLoader(final ServletConfigWrapper servletConfig, final ServletContextWrapper servletContext,
			final File directory) {
		super();
		this.defaultConfig = servletConfig;
		this.defaultContext = servletContext;
		this.directory = directory;
		globalProps = loadDirProps(this.directory);
	}

	/**
	 * Gets the configuration for given servlet class and path. The returned
	 * configuration contains ONLY the single servlet's class properties.
	 * 
	 * @param clazz
	 *            The servlet canonical class name
	 * @return The servlet configuration
	 */
	public ServletConfig getConfig(final String clazz) {
		final ServletConfigWrapper conf = lookupByClass(clazz);
		if (conf != null) {
			return conf;
		}
		return defaultConfig;
	}

	/**
	 * Gets the configuration for given servlet class and path. The returned
	 * configuration contains both single servlet's class properties and path
	 * properties.
	 * 
	 * @param clazz
	 *            The servlet canonical class name
	 * @param pathArg
	 *            The servlet's path without leading '/' character
	 * @return The servlet configuration
	 */
	public ServletConfig getConfig(final String clazz, final String pathArg) {
		final String path = ignoreWildcards(pathArg);
		final ServletConfigWrapper conf = lookupByClassAndPath(clazz, path);
		if (conf != null) {
			return conf;
		}
		return defaultConfig;
	}

	/**
	 * Gets servlet's context
	 * 
	 * @param clazz
	 *            The servlet canonical class name
	 * @return The servlet context
	 */
	public ServletContext getContext(final String clazz) {
		final ServletConfigWrapper conf = lookupByClass(clazz);
		if (conf != null) {
			return conf.getServletContext();
		}
		return defaultContext;
	}

	/**
	 * Gets servlet's context whose servlet configuration contains both single
	 * servlet's class properties and path properties.
	 * 
	 * @param clazz
	 *            the servlet canonical class name
	 * @param path
	 *            The servlet's path without leading '/' character
	 * @return The servlet context
	 */
	public ServletContext getContext(final String clazz, final String path) {
		final String pathStr = ignoreWildcards(path);
		final ServletConfigWrapper conf = lookupByClassAndPath(clazz, pathStr);
		if (conf != null) {
			return conf.getServletContext();
		}
		return defaultContext;
	}

	/**
	 * Gets the directory in which all servlet configurations are kept
	 * 
	 * @return The configurations' directory
	 */
	public File getDirectory() {
		return directory;
	}

	private ServletConfigWrapper lookupByClass(final String clazz) {
		if (clazzGuardian.contains(clazz) && (globalProps == null)) {
			return null;
		}
		/*
		 * Property containers
		 */
		Map<String, String> props = clazzProps.get(clazz);
		/*
		 * Lookup class-specific properties
		 */
		if (props == null) {
			if (!clazzGuardian.contains(clazz)) {
				props = loadProperties(new File(directory, new StringBuilder(32).append(clazz).append('.').append(
						FILEEXT_PROPERTIES).toString()));
				if (null == props) {
					clazzGuardian.add(clazz);
				} else {
					clazzProps.put(clazz, new HashMap<String, String>(props)); // Clone
				}
			}
		} else {
			props = new HashMap<String, String>(props); // Clone
		}
		/*
		 * No properties present at all
		 */
		if ((props == null) && (globalProps == null)) {
			return null;
		}
		/*
		 * Compose properties for servlet configuration
		 */
		if (props == null) {
			props = new HashMap<String, String>();
		}
		if (globalProps != null) {
			props.putAll(globalProps);
		}
		return buildConfig(props, clazz);
	}

	private ServletConfigWrapper lookupByClassAndPath(final String clazz, final String path) {
		if (clazzGuardian.contains(clazz) && pathGuardian.contains(path) && (globalProps == null)) {
			return null;
		}
		/*
		 * Property containers
		 */
		Map<String, String> props = clazzProps.get(clazz);
		Map<String, String> dirProps = pathProps.get(path);
		/*
		 * Lookup class-specific properties
		 */
		if (props == null) {
			if (!clazzGuardian.contains(clazz)) {
				props = loadProperties(new File(directory, new StringBuilder(32).append(clazz).append('.').append(
						FILEEXT_PROPERTIES).toString()));
				if (null == props) {
					clazzGuardian.add(clazz);
				} else {
					clazzProps.put(clazz, new HashMap<String, String>(props)); // Clone
				}
			}
		} else {
			props = new HashMap<String, String>(props); // Clone
		}
		if ((dirProps == null) && !pathGuardian.contains(path)) {
			dirProps = loadDirProps(new File(directory, path));
			if (dirProps == null) {
				pathGuardian.add(path);
			} else {
				pathProps.put(path, dirProps);
			}
		}
		/*
		 * No properties present at all
		 */
		if ((dirProps == null) && (props == null) && (globalProps == null)) {
			return null;
		}
		/*
		 * Compose properties for servlet configuration
		 */
		if (props == null) {
			props = new HashMap<String, String>();
		}
		if (dirProps != null) {
			props.putAll(dirProps);
		}
		if (globalProps != null) {
			props.putAll(globalProps);
		}
		return buildConfig(props, clazz);
	}

	/**
	 * Removes the configuration for given servlet class
	 * 
	 * @param clazz
	 *            The servlet canonical class name
	 */
	public void removeConfig(final String clazz) {
		clazzProps.remove(clazz);
	}

	/**
	 * Sets the configuration for given servlet class
	 * 
	 * @param clazz
	 *            The servlet canonical class name
	 * @param initParams
	 *            The servlet's init parameters
	 */
	public void setConfig(final String clazz, final Dictionary<String, String> initParams) {
		final int size = initParams.size();
		final Map<String, String> map = new HashMap<String, String>(size);
		final Enumeration<String> e = initParams.keys();
		for (int i = 0; i < size; i++) {
			final String key = e.nextElement();
			map.put(key, initParams.get(key));
		}
		clazzProps.put(clazz, map);
	}

	/**
	 * Sets the default servlet configuration
	 * 
	 * @param config
	 *            The default configuration
	 * @throws UnsupportedOperationException
	 *             If this method is invoked on default instance obtained via
	 *             {@link #getDefaultInstance()}
	 */
	public void setDefaultConfig(final ServletConfig config) {
		if (this == defaultInstance) {
			throw new UnsupportedOperationException("Default servlet config must not be changed for default instance");
		}
		this.defaultConfig = config;
	}

	/**
	 * Sets the default servlet context
	 * 
	 * @param context
	 *            The default context
	 * @throws UnsupportedOperationException
	 *             If this method is invoked on default instance obtained via
	 *             {@link #getDefaultInstance()}
	 */
	public void setDefaultContext(final ServletContext context) {
		if (this == defaultInstance) {
			throw new UnsupportedOperationException("Default servlet context must not be changed for default instance");
		}
		this.defaultContext = context;
	}

	/**
	 * Sets the directory in which all servlet configurations are kept
	 * 
	 * @param directory
	 *            The configurations' directory
	 * @throws UnsupportedOperationException
	 *             If this method is invoked on default instance obtained via
	 *             {@link #getDefaultInstance()}
	 */
	public void setDirectory(final File directory) {
		if (this == defaultInstance) {
			throw new UnsupportedOperationException("Default servlet context must not be changed for default instance");
		}
		this.directory = directory;
		globalProps = loadDirProps(this.directory);
	}
}
