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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * 
 * @author francisco.laguna@open-xchange.com
 * 
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
 */
public class ServletConfigLoader {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(ServletConfigLoader.class);

	// Minimize IO and remember failed lookups
	private final transient Set<String> clazzGuardian = new HashSet<String>();

	private final transient Set<String> pathGuardian = new HashSet<String>();

	// Minimize IO and cache results
	private final transient Map<String, Map<String, String>> clazzProps = new HashMap<String, Map<String, String>>();

	private final transient Map<String, Map<String, String>> pathProps = new HashMap<String, Map<String, String>>();

	private transient ServletConfig defaultConfig;

	private transient ServletContext defaultContext;

	private File directory;

	public ServletConfigLoader(File directory) {
		this.directory = directory;
	}

	public ServletConfigLoader() {
		super();
	}

	public ServletConfig getConfig(final String clazz) {
		final ServletConfig conf = lookupByClass(clazz);
		if (conf != null) {
			return conf;
		}
		return defaultConfig;
	}

	public ServletContext getContext(final String clazz) {
		final ServletConfig conf = lookupByClass(clazz);
		if (conf != null) {
			return conf.getServletContext();
		}
		return defaultContext;
	}

	public ServletConfig getConfig(final String clazz, final String pathArg) {
		final String path = ignoreWildcards(pathArg);
		final ServletConfig conf = lookupByClassAndPath(clazz, path);
		if (conf != null) {
			return conf;
		}
		return defaultConfig;
	}

	public ServletContext getContext(final String clazz, final String pathArg) {
		final String path = ignoreWildcards(pathArg);
		final ServletConfig conf = lookupByClassAndPath(clazz, path);
		if (conf != null) {
			return conf.getServletContext();
		}
		return defaultContext;
	}

	public void setDefaultConfig(final ServletConfig config) {
		this.defaultConfig = config;
	}

	public void setDefaultContext(final ServletContext context) {
		this.defaultContext = context;
	}

	protected ServletConfig lookupByClass(final String clazz) {
		if (clazzGuardian.contains(clazz)) {
			return null;
		}
		Map<String, String> props = clazzProps.get(clazz);
		if (props != null) {
			return buildConfig(props);
		}
		final File propFile = new File(directory, clazz + ".properties");
		props = loadProperties(propFile);
		if (props == null) {
			clazzGuardian.add(clazz);
			return null;
		}
		clazzProps.put(clazz, props);
		return buildConfig(props);
	}

	protected ServletConfig lookupByClassAndPath(final String clazz, final String path) {
		if (clazzGuardian.contains(clazz) && pathGuardian.contains(path)) {
			return null;
		}
		final File propFile = new File(directory, clazz + ".properties");
		final File propDir = new File(directory, path);

		Map<String, String> props = clazzProps.get(clazz);
		Map<String, String> dirProps = pathProps.get(path);

		if (props == null && !clazzGuardian.contains(clazz)) {
			props = loadProperties(propFile);
			if (null == props) {
				clazzGuardian.add(clazz);
			}
			clazzProps.put(clazz, props);
		}

		if (dirProps == null && !pathGuardian.contains(path)) {
			dirProps = loadDirProps(propDir);
			if (dirProps == null) {
				pathGuardian.add(path);
			}
			pathProps.put(path, dirProps);
		}

		if (dirProps == null && props == null) {
			return null;
		}

		if (props == null) {
			props = new HashMap<String, String>();
		}

		if (dirProps != null) {
			props.putAll(dirProps);
		}

		return buildConfig(props);

	}

	protected ServletConfig buildConfig(final Map<String, String> props) {
		final ServletConfigWrapper config = new ServletConfigWrapper();
		config.setInitParameter(props);
		config.setServletContextWrapper(new ServletContextWrapper(config));
		return config;
	}

	protected Map<String, String> loadProperties(final File propFile) {
		if (!propFile.exists()) {
			return null;
		}
		final Properties props = new Properties();
		InputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(propFile));
			props.load(in);
		} catch (IOException x) {
			return null;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}

		final Map<String, String> m = new HashMap<String, String>();
		addProps(m, props);

		return m;
	}

	protected Map<String, String> loadDirProps(final File dir) {
		final Map<String, String> m = new HashMap<String, String>();

		if (dir.exists() && dir.isDirectory()) {
			for (File f : dir.listFiles()) {
				if (f.isFile() && f.getName().endsWith(".properties")) {
					final Map<String, String> props = loadProperties(f);
					m.putAll(props);
				}
			}
			return m;
		}
		return null;
	}

	private void addProps(final Map<String, String> m, final Properties props) {
		for (Map.Entry<Object, Object> entry : props.entrySet()) {
			m.put((String) entry.getKey(), (String) entry.getValue());
		}
	}

	protected String ignoreWildcards(final String path) {
		return path.replaceAll("\\*", "");
	}

	public File getDirectory() {
		return directory;
	}

	public void setDirectory(final File directory) {
		this.directory = directory;
	}

}
