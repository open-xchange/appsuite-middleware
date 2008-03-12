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

package com.openexchange.mail.mime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.openexchange.configuration.SystemConfig;

/**
 * {@link MIMEType2ExtMap} - Maps MIME types to file extensions and vice versa.
 * <p>
 * This class looks in various places for MIME types file entries. When requests
 * are made to look up MIME types or file extensions, it searches MIME types
 * files in the following order:
 * <ol>
 * <li>The file <i>.mime.types</i> in the user's home directory.</li>
 * <li>The file <i>&lt;java.home&gt;/lib/mime.types</i>.</li>
 * <li>The file or resources named <i>META-INF/mime.types</i>.</li>
 * <li>The file or resource named <i>META-INF/mimetypes.default</i>.</li>
 * </ol>
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MIMEType2ExtMap {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(MIMEType2ExtMap.class);

	private static final AtomicBoolean initialized = new AtomicBoolean();

	private static final Map<String, String> type_hash = new HashMap<String, String>();

	private static final Map<String, List<String>> ext_hash = new HashMap<String, List<String>>();

	/**
	 * No instance
	 */
	private MIMEType2ExtMap() {
		super();
	}

	/**
	 * Resets MIME type file map
	 */
	public static void reset() {
		if (initialized.get()) {
			synchronized (initialized) {
				if (!initialized.get()) {
					return;
				}
				type_hash.clear();
				ext_hash.clear();
				initialized.set(false);
			}
		}
	}

	/**
	 * Initializes MIME type file map
	 */
	public static void init() {
		if (!initialized.get()) {
			synchronized (initialized) {
				try {
					if (initialized.get()) {
						return;
					}
					final StringBuilder sb = new StringBuilder(128);
					{
						final String homeDir = System.getProperty("user.home");
						if (homeDir != null) {
							final File file = new File(sb.append(homeDir).append(File.separatorChar).append(
									".mime.types").toString());
							if (file.exists()) {
								if (LOG.isInfoEnabled()) {
									sb.setLength(0);
									LOG.info(sb.append("Loading MIME type file \"").append(file.getPath()).append('"')
											.toString());
								}
								loadInternal(file);
							}
						}
					}
					{
						final String javaHome = System.getProperty("java.home");
						if (javaHome != null) {
							sb.setLength(0);
							final File file = new File(sb.append(javaHome).append(File.separatorChar).append("lib")
									.append(File.separator).append("mime.types").toString());
							if (file.exists()) {
								if (LOG.isInfoEnabled()) {
									sb.setLength(0);
									LOG.info(sb.append("Loading MIME type file \"").append(file.getPath()).append('"')
											.toString());
								}
								loadInternal(file);
							}
						}
					}
					{
						for (final Enumeration<URL> e = ClassLoader.getSystemResources("META-INF/mime.types"); e
								.hasMoreElements();) {
							final URL url = e.nextElement();
							if (LOG.isInfoEnabled()) {
								sb.setLength(0);
								LOG.info(sb.append("Loading MIME type file \"").append(url.getFile()).append('"')
										.toString());
							}
							loadInternal(url);
						}
					}
					{
						for (final Enumeration<URL> e = ClassLoader.getSystemResources("META-INF/mimetypes.default"); e
								.hasMoreElements();) {
							final URL url = e.nextElement();
							if (LOG.isInfoEnabled()) {
								sb.setLength(0);
								LOG.info(sb.append("Loading MIME type file \"").append(url.getFile()).append('"')
										.toString());
							}
							loadInternal(url);
						}
					}
					{
						String mimeTypesFile = SystemConfig.getProperty(SystemConfig.Property.MimeTypeFile);
						if (mimeTypesFile != null && (mimeTypesFile = mimeTypesFile.trim()).length() > 0) {
							final File file = new File(mimeTypesFile);
							if (file.exists()) {
								if (LOG.isInfoEnabled()) {
									sb.setLength(0);
									LOG.info(sb.append("Loading MIME type file \"").append(file.getPath()).append('"')
											.toString());
								}
								loadInternal(file);
							}
						}

					}
					initialized.set(true);
					if (LOG.isInfoEnabled()) {
						LOG.info("MIMEType2ExtMap successfully initialized");
					}
				} catch (final IOException e) {
					LOG.error(e.getLocalizedMessage(), e);
				}
			}
		}
	}

	/**
	 * Gets the MIME type associated with given file
	 * 
	 * @param file
	 *            The file
	 * @return The MIME type associated with given file extension or
	 *         <code>application/octet-stream</code> if none found
	 */
	public static String getContentType(final File file) {
		return getContentType(file.getName());
	}

	/**
	 * Gets the MIME type associated with given file name
	 * 
	 * @param fileName
	 *            The file name
	 * @return The MIME type associated with given file extension or
	 *         <code>application/octet-stream</code> if none found
	 */
	public static String getContentType(final String fileName) {
		if (!initialized.get()) {
			init();
		}
		final int pos = fileName.lastIndexOf('.');
		if (pos < 0) {
			return MIMETypes.MIME_APPL_OCTET;
		}
		final String s1 = fileName.substring(pos + 1);
		if (s1.length() == 0) {
			return MIMETypes.MIME_APPL_OCTET;
		}
		final String type = type_hash.get(s1.toLowerCase());
		if (null == type) {
			return MIMETypes.MIME_APPL_OCTET;
		}
		return type;
	}

	private static final List<String> DEFAULT_EXT;

	static {
		final List<String> l = new ArrayList<String>(1);
		l.add("dat");
		DEFAULT_EXT = Collections.unmodifiableList(l);
	}

	/**
	 * Gets the file extension for given MIME type
	 * 
	 * @param mimeType
	 *            The MIME type
	 * @return The file extension for given MIME type or <code>dat</code> if
	 *         none found
	 */
	public static List<String> getFileExtensions(final String mimeType) {
		if (!initialized.get()) {
			init();
		}
		return ext_hash.containsKey(mimeType.toLowerCase()) ? Collections.unmodifiableList(ext_hash.get(mimeType))
				: DEFAULT_EXT;
	}

	/**
	 * Loads the MIME type file specified through <code>fileStr</code>
	 * 
	 * @param fileStr
	 *            The MIME type file to load
	 */
	public static void load(final String fileStr) {
		if (!initialized.get()) {
			init();
		}
		load(new File(fileStr));
	}

	/**
	 * Loads the MIME type file specified through given file
	 * 
	 * @param file
	 *            The MIME type file to load
	 */
	public static void load(final File file) {
		if (!initialized.get()) {
			init();
		}
		loadInternal(file);
	}

	/**
	 * Loads the MIME type file specified through given file
	 * 
	 * @param file
	 *            The MIME type file to load
	 */
	private static void loadInternal(final File file) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "iso-8859-1"));
			parse(reader);
		} catch (final UnsupportedEncodingException e) {
			LOG.error(e.getLocalizedMessage(), e);
		} catch (final FileNotFoundException e) {
			LOG.error(e.getLocalizedMessage(), e);
		} catch (final IOException e) {
			LOG.error(e.getLocalizedMessage(), e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (final IOException e) {
					LOG.error(e.getLocalizedMessage(), e);
				}
			}
		}
	}

	/**
	 * Loads the MIME type file specified through given URL
	 * 
	 * @param url
	 *            The URL to a MIME type file
	 */
	private static void loadInternal(final URL url) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(url.openStream(), "iso-8859-1"));
			parse(reader);
		} catch (final UnsupportedEncodingException e) {
			LOG.error(e.getLocalizedMessage(), e);
		} catch (final FileNotFoundException e) {
			LOG.error(e.getLocalizedMessage(), e);
		} catch (final IOException e) {
			LOG.error(e.getLocalizedMessage(), e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (final IOException e) {
					LOG.error(e.getLocalizedMessage(), e);
				}
			}
		}
	}

	private static void parse(final BufferedReader reader) throws IOException {
		String line = null;
		final StringBuilder strBuilder = new StringBuilder(64);
		while ((line = reader.readLine()) != null) {
			final int i = strBuilder.length();
			strBuilder.append(line);
			if (i > 0 && strBuilder.charAt(i - 1) == '\\') {
				strBuilder.delete(0, i - 1);
			} else {
				parseEntry(strBuilder.toString().trim());
				strBuilder.setLength(0);
			}
		}
		if (strBuilder.length() > 0) {
			parseEntry(strBuilder.toString().trim());
		}
	}

	private static void parseEntry(final String entry) {
		if (entry.length() == 0) {
			return;
		} else if (entry.charAt(0) == '#') {
			return;
		}
		if (entry.indexOf('=') > 0) {
			final MimeTypeFileLineParser parser = new MimeTypeFileLineParser(entry);
			final String type = parser.getType();
			final List<String> exts = parser.getExtensions();
			if (type != null && exts != null) {
				for (String ext : exts) {
					type_hash.put(ext, type);
				}
				if (ext_hash.containsKey(type)) {
					ext_hash.get(type).addAll(exts);
				} else {
					ext_hash.put(type, exts);
				}
			}
		} else {
			final String[] tokens = entry.split("[ \t\n\r\f]+");
			if (tokens.length > 1) {
				final String type = tokens[0].toLowerCase();
				final List<String> set = new ArrayList<String>();
				for (int i = 1; i < tokens.length; i++) {
					final String ext = tokens[i].toLowerCase();
					set.add(ext);
					type_hash.put(ext, type);
				}
				if (ext_hash.containsKey(type)) {
					ext_hash.get(type).addAll(set);
				} else {
					ext_hash.put(type, set);
				}
			}
		}
	}

}
