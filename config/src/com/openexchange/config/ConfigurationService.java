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

package com.openexchange.config;

import java.util.Iterator;
import java.util.Properties;

/**
 * {@link ConfigurationService}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public interface ConfigurationService {

	/**
	 * Searches for the property with the specified name in this property list.
	 * If the name is not found in this property list, the default property
	 * list, and its defaults, recursively, are then checked. The method returns
	 * <code>null</code> if the property is not found.
	 * 
	 * @param name
	 *            The property name.
	 * @return The value in this property list with the specified key value or
	 *         <code>null</code>.
	 */
	public String getProperty(String name);

	/**
	 * Searches for the property with the specified name in this property list.
	 * If the name is not found in this property list, the default property
	 * list, and its defaults, recursively, are then checked. The method returns
	 * the default value argument if the property is not found.
	 * 
	 * @param name
	 *            The property name.
	 * @param defaultValue
	 *            The default value
	 * @return The value in this property list with the specified key value or
	 *         given default value argument.
	 */
	public String getProperty(String name, String defaultValue);

	/**
	 * Searches for the property with the specified name in this property list.
	 * If the name is not found in this property list, the default property
	 * list, and its defaults, recursively, are then checked. The method returns
	 * <code>null</code> if the property is not found.
	 * <p>
	 * Furthermore the specified listener will be notified if any changes are
	 * noticed on specified property
	 * 
	 * @param name
	 *            The property name.
	 * @param listener
	 *            The property listener which is notified on property changes
	 * @return The value in this property list with the specified key value or
	 *         <code>null</code>.
	 */
	//public String getProperty(String name, PropertyListener listener);

	/**
	 * Searches for the property with the specified name in this property list.
	 * If the name is not found in this property list, the default property
	 * list, and its defaults, recursively, are then checked. The method returns
	 * the default value argument if the property is not found.
	 * <p>
	 * Furthermore the specified listener will be notified if any changes are
	 * noticed on specified property
	 * 
	 * @param name
	 *            The property name.
	 * @param defaultValue
	 *            The default value
	 * @param listener
	 *            The property listener which is notified on property changes
	 * @return The value in this property list with the specified key value or
	 *         given default value argument.
	 */
	//public String getProperty(String name, String defaultValue, PropertyListener listener);

    /**
     * Returns all properties defined in a specific properties file. The
     * filename of the properties file must not contains any path segments. If
     * no such property file has been read empty properties will be returned.
     * @param filename The filename of the properties file.
     * @return the properties from that file or an empty properties if that file
     * was not read.
     */
	public Properties getFile(String filename);

	/**
	 * Returns all properties defined in a specific properties file. The
	 * filename of the properties file must not contains any path segments. If
	 * no such property file has been read empty properties will be returned.
	 * <p>
     * Furthermore the specified listener will be notified if any changes are
     * noticed on properties of that properties file.
	 * @param filename The filename of the properties file.
	 * @param listener This property listener is notified on changes on
	 * properties of that file.
	 * @return the properties from that file or an empty properties if that file
	 * was not read.
	 */
	//public Properties getFile(String filename, PropertyListener listener);

	/**
	 * Searches for the property with the specified name in this property list.
	 * If the name is found in this property list, it is supposed to be a
	 * boolean value. If conversion fails or name is not found, the default
	 * value is returned.
	 * <p>
	 * The <code>boolean</code> returned represents the value
	 * <code>true</code> if the property is not <code>null</code> and is
	 * equal, ignoring case, to the string {@code "true"}.
	 * 
	 * @param name
	 *            The property name.
	 * @param defaultValue
	 *            The default value
	 * @return The boolean value in this property list with the specified key
	 *         value or given default value argument.
	 */
	public boolean getBoolProperty(String name, boolean defaultValue);

	/**
	 * Searches for the property with the specified name in this property list.
	 * If the name is found in this property list, it is supposed to be an
	 * integer value. If conversion fails or name is not found, the default
	 * value is returned.
	 * <p>
	 * Parses the property as a signed decimal integer. The characters in the
	 * property must all be decimal digits, except that the first character may
	 * be an ASCII minus sign <code>'-'</code> (<code>'&#92;u002D'</code>)
	 * to indicate a negative value.
	 * 
	 * @param name
	 *            The property name.
	 * @param defaultValue
	 *            The default value
	 * @return The integer value in this property list with the specified key
	 *         value or given default value argument.
	 */
	public int getIntProperty(String name, int defaultValue);

	/**
	 * Returns an iterator of all the keys in this property list.
	 * 
	 * @return The iterator of all the keys in this property list.
	 */
	public Iterator<String> propertyNames();

	/**
	 * Returns the number of properties in this property list.
	 * 
	 * @return The number of properties in this property list.
	 */
	public int size();
}
