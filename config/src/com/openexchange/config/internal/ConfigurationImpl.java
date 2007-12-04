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

package com.openexchange.config.internal;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.config.Configuration;
import com.openexchange.config.PropertyListener;
import com.openexchange.config.internal.filewatcher.FileWatcher;

/**
 * {@link ConfigurationImpl}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class ConfigurationImpl implements Configuration {

	private static final Log LOG = LogFactory.getLog(ConfigurationImpl.class);

	private static final String EXT = ".properties";

	private static final class PropertyFileFilter implements FileFilter {

		public boolean accept(final File pathname) {
			return pathname.isDirectory() || pathname.getName().toLowerCase().endsWith(EXT);
		}

	}

	private final Map<String, String> properties;

	private final Map<String, String> propertiesFiles;

	/**
	 * Initializes a new configuration. The properties directory is determined
	 * by system property "<code>openexchange.propdir</code>"
	 */
	public ConfigurationImpl() {
		this(System.getProperty("openexchange.propdir"));
	}

	/**
	 * Initializes a new configuration
	 * 
	 * @param directory
	 *            The directory where property files are located
	 */
	public ConfigurationImpl(final String directory) {
		super();
		if (null == directory) {
			throw new IllegalArgumentException("directory is null. Missing system property \"openexchange.propdir\".");
		}
		final File dir = new File(directory);
		if (!dir.exists()) {
			throw new IllegalArgumentException("Not found: " + directory);
		} else if (!dir.isDirectory()) {
			throw new IllegalArgumentException("Not a directory: " + directory);
		}
		properties = new HashMap<String, String>();
		propertiesFiles = new HashMap<String, String>();
		final FileFilter fileFilter = new PropertyFileFilter();
		processDirectory(dir, fileFilter, properties, propertiesFiles);
	}

	private static void processDirectory(final File dir, final FileFilter fileFilter,
			final Map<String, String> properties, final Map<String, String> propertiesFiles) {
		final File[] files = dir.listFiles(fileFilter);
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				processDirectory(files[i], fileFilter, properties, propertiesFiles);
			} else {
				processPropertiesFile(files[i], properties, propertiesFiles);
			}
		}
	}

	private static void processPropertiesFile(final File propFile, final Map<String, String> properties,
			final Map<String, String> propertiesFiles) {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(propFile);
			final Properties tmp = new Properties();
			tmp.load(fis);
			final int size = tmp.size();
			final Iterator<Entry<Object, Object>> iter = tmp.entrySet().iterator();
			for (int i = 0; i < size; i++) {
				final Entry<Object, Object> e = iter.next();
				final String propName = e.getKey().toString().trim();
				properties.put(propName, e.getValue().toString().trim());
				propertiesFiles.put(propName, propFile.getPath());
			}
		} catch (final FileNotFoundException e) {
			LOG.error(e.getLocalizedMessage(), e);
		} catch (final IOException e) {
			LOG.error(e.getLocalizedMessage(), e);
		} finally {
			if (null != fis) {
				try {
					fis.close();
				} catch (final IOException e) {
					LOG.error(e.getLocalizedMessage(), e);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.config.Configuration#getProperty(java.lang.String)
	 */
	public String getProperty(final String name) {
		return properties.get(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.config.Configuration#getProperty(java.lang.String,
	 *      java.lang.Object)
	 */
	public String getProperty(final String name, final String defaultValue) {
		return properties.containsKey(name) ? properties.get(name) : defaultValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.config.Configuration#getProperty(java.lang.String,
	 *      com.openexchange.config.PropertyListener)
	 */
	public String getProperty(final String name, final PropertyListener listener) {
		if (properties.containsKey(name)) {
			final PropertyWatcher pw = PropertyWatcher.addPropertyWatcher(name, properties.get(name), true);
			pw.addPropertyListener(listener);
			final FileWatcher fileWatcher = FileWatcher.getFileWatcher(new File(propertiesFiles.get(name)));
			fileWatcher.addFileListener(pw);
			fileWatcher.startFileWatcher(10000);
			properties.get(name);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.config.Configuration#getProperty(java.lang.String,
	 *      java.lang.Object, com.openexchange.config.PropertyListener)
	 */
	public String getProperty(final String name, final String defaultValue, final PropertyListener listener) {
		if (properties.containsKey(name)) {
			final PropertyWatcher pw = PropertyWatcher.addPropertyWatcher(name, properties.get(name), true);
			pw.addPropertyListener(listener);
			final FileWatcher fileWatcher = FileWatcher.getFileWatcher(new File(propertiesFiles.get(name)));
			fileWatcher.addFileListener(pw);
			fileWatcher.startFileWatcher(10000);
			properties.get(name);
		}
		return defaultValue;
	}

	/**
     * {@inheritDoc}
     */
    @Override
    public Properties getFile(final String filename) {
        return getFile(filename, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Properties getFile(final String filename,
        final PropertyListener listener) {
        final Properties retval = new Properties();
        final Iterator<Entry<String,String>> iter = propertiesFiles.entrySet().iterator();
        while (iter.hasNext()) {
            final Entry<String,String> entry = iter.next();
            if (entry.getValue().endsWith(filename)) {
                final String value;
                if (null == listener) {
                    value = getProperty(entry.getKey());
                } else {
                    value = getProperty(entry.getKey(), listener);
                }
                retval.put(entry.getKey(), value);
            }
        }
        return retval;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.config.Configuration#getProperty(java.lang.String,
	 *      boolean)
	 */
	public boolean getBoolProperty(String name, boolean defaultValue) {
		if (properties.containsKey(name)) {
			return Boolean.parseBoolean(properties.get(name));
		}
		return defaultValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.config.Configuration#getProperty(java.lang.String,
	 *      int)
	 */
	public int getIntProperty(String name, int defaultValue) {
		if (properties.containsKey(name)) {
			try {
				return Integer.parseInt(properties.get(name));
			} catch (final NumberFormatException e) {
				if (LOG.isTraceEnabled()) {
					LOG.trace(e.getLocalizedMessage(), e);
				}
			}
		}
		return defaultValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.config.Configuration#propertyNames()
	 */
	public Iterator<String> propertyNames() {
		return properties.keySet().iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.config.Configuration#size()
	 */
	public int size() {
		return properties.size();
	}
}
