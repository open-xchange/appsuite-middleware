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

package com.openexchange.config;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link ConfigurationService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@SingletonService
public interface ConfigurationService {

    /**
     * Gets the filter backed by given property's value.
     *
     * @param name The property name
     * @return The filter or <code>null</code> if there is no such property
     */
    public Filter getFilterFromProperty(String name);

    /**
     * Gets all properties that fulfills given filter's acceptance criteria.
     *
     * @param filter The filter
     * @return The appropriate properties
     * @throws OXException If properties cannot be returned
     */
    public Map<String, String> getProperties(PropertyFilter filter) throws OXException;

    /**
     * Searches for the property with the specified name in this property list. If the name is not found in this property list, the default
     * property list, and its defaults, recursively, are then checked. The method returns <code>null</code> if the property is not found.
     *
     * @param name The property name.
     * @return The value in this property list with the specified key value or <code>null</code>.
     */
    public String getProperty(String name);

    /**
     * Searches for the property with the specified name in this property list. If the name is not found in this property list, the default
     * property list, and its defaults, recursively, are then checked. The method returns the default value argument if the property is not
     * found.
     *
     * @param name The property name.
     * @param defaultValue The default value
     * @return The value in this property list with the specified key value or given default value argument.
     */
    public String getProperty(String name, String defaultValue);

    /**
     * Searches for the property with the specified name in this property list. If the name is not found in this property list, the default
     * property list, and its defaults, recursively, are then checked. The method returns the default value argument if the property is not
     * found. If the value can be found it will be split at the given separator and trimmed.
     * <p>
     *
     * @param name The property name.
     * @param defaultValue The default value
     * @param separator the seperator as regular expression used to split the input around this separator
     * @return The value in this property list with the specified key value or given default value argument split and trimmed at the given
     * separator
     * @throws IllegalArgumentException - if defaultValue or the seperator are missing or if the separator isn't a valid pattern
     */
    public List<String> getProperty(String name, String defaultValue, String separator);

    /**
     * Searches for the property with the specified name in this property list. If the name is not found in this property list, the default
     * property list, and its defaults, recursively, are then checked. The method returns <code>null</code> if the property is not found.
     * <p>
     * Furthermore the specified listener will be notified if any changes are noticed on specified property
     *
     * @param name The property name.
     * @param listener The property listener which is notified on property changes
     * @return The value in this property list with the specified key value or <code>null</code>.
     */
    public String getProperty(String name, PropertyListener listener);

    /**
     * Searches for the property with the specified name in this property list. If the name is not found in this property list, the default
     * property list, and its defaults, recursively, are then checked. The method returns the default value argument if the property is not
     * found.
     * <p>
     * Furthermore the specified listener will be notified if any changes are noticed on specified property
     *
     * @param name The property name.
     * @param defaultValue The default value
     * @param listener The property listener which is notified on property changes
     * @return The value in this property list with the specified key value or given default value argument.
     */
    public String getProperty(String name, String defaultValue, PropertyListener listener);

    /**
     * Searches for the property with the specified name in this property list. If the name is not found in this property list, the default
     * property list, and its defaults, recursively, are then checked. The method returns the default value argument if the property is not
     * found. If the value can be found it will be split at the given separator and trimmed.
     * <p>
     * Furthermore the specified listener will be notified if any changes are noticed on specified property
     *
     * @param name The property name.
     * @param defaultValue The default value
     * @param listener The property listener which is notified on property changes
     * @param separator the seperator as regular expression used to split the input around this separator
     * @return The value in this property list with the specified key value or given default value argument split and trimmed at the given
     * separator
     * @throws IllegalArgumentException - if defaultValue or the seperator are missing or if the separator isn't a valid pattern
     */
    public List<String> getProperty(String name, String defaultValue, PropertyListener listener, String separator);

    /**
     * Removes specified property listener previously set by {@link #getProperty(String, PropertyListener)} or
     * {@link #getProperty(String, String, PropertyListener)}.
     *
     * @param name The property name.
     * @param listener The property listener to remove
     */
    public void removePropertyListener(String name, PropertyListener listener);

    /**
     * Returns all properties defined in a specific properties file. The filename of the properties file must not contains any path
     * segments. If no such property file has been read empty properties will be returned.
     *
     * @param fileName The file name of the properties file.
     * @return the properties from that file or an empty properties if that file was not read.
     *
     * @deprecated <div style="margin-left: 0.1in; margin-right: 0.5in; background-color:#FFDDDD;">
     *             Due to "lean configuration" initiative this method might return a {@code Properties} instance that does not contain
     *             all of the properties that are expected to be contained, as the specified file might miss those properties whose
     *             value does not differ from associated default value. If the denoted file is known to exist and does contain expected
     *             content, this methods may be safely called or by invoking
     *             <code>ConfigurationServices.loadPropertiesFrom(configService.getFileByName("existing.properties"))</code>.
     *             </div>
     */
    @Deprecated
    public Properties getFile(String fileName);

    /**
     * Gets the directory denoted by given directory name.
     *
     * @param directoryName The directory name
     * @return The directory or <code>null</code>
     */
    File getDirectory(String directoryName);

    /**
     * Gets the file denoted by given file name.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; background-color:#FFDDDD;">
     * Due to "lean configuration" initiative this method the referenced file might be absent or return an incomplete content
     * in case a <code>.properties</code> file is supposed to be fetched, as the specified file might miss those properties whose
     * value does not differ from associated default value.
     * If the denoted file is known to exist and does contain expected content, this methods may be safely called.
     * </div>
     *
     * @param fileName The file name
     * @return The file or <code>null</code>
     */
    File getFileByName(String fileName);

    /**
     * If no property format is used for configuration data, the text content of a file can be retrieved with this call.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; background-color:#FFDDDD;">
     * Due to "lean configuration" initiative this method the referenced file might be absent or return an incomplete content
     * in case a <code>.properties</code> file is supposed to be fetched, as the specified file might miss those properties whose
     * value does not differ from associated default value.
     * If the denoted file is known to exist and does contain expected content, this methods may be safely called.
     * </div>
     *
     * @param fileName The logical file name of the file to be retrieved.
     * @return The text content of the configuration
     */
    public String getText(String fileName);

    /**
     * Returns all properties defined in a specific properties file. The filename of the properties file must not contains any path
     * segments. If no such property file has been read empty properties will be returned.
     * <p>
     * Furthermore the specified listener will be notified if any changes are noticed on properties of that properties file.
     *
     * @param filename The filename of the properties file.
     * @param listener This property listener is notified on changes on properties of that file.
     * @return the properties from that file or an empty properties if that file was not read.
     */
    // public Properties getFile(String filename, PropertyListener listener);
    /**
     * Retrieves and merges all properties files in below the given folder name and its subfolders (recursively). All properties discovered
     * this way are aggregated in the returned properties object.
     *
     * @param folderName
     * @return Aggregated properties of all properties files below this folder.
     */
    public Properties getPropertiesInFolder(String folderName);

    /**
     * Searches for the property with the specified name in this property list. If the name is found in this property list, it is supposed
     * to be a boolean value. If conversion fails or name is not found, the default value is returned.
     * <p>
     * The <code>boolean</code> returned represents the value <code>true</code> if the property is not <code>null</code> and is equal,
     * ignoring case, to the string {@code "true"}.
     *
     * @param name The property name.
     * @param defaultValue The default value
     * @return The boolean value in this property list with the specified key value or given default value argument.
     */
    public boolean getBoolProperty(String name, boolean defaultValue);

    /**
     * Searches for the property with the specified name in this property list. If the name is found in this property list, it is supposed
     * to be an integer value. If conversion fails or name is not found, the default value is returned.
     * <p>
     * Parses the property as a signed decimal integer. The characters in the property must all be decimal digits, except that the first
     * character may be an ASCII minus sign <code>'-'</code> (<code>'&#92;u002D'</code>) to indicate a negative value.
     *
     * @param name The property name.
     * @param defaultValue The default value
     * @return The integer value in this property list with the specified key value or given default value argument.
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

    /**
     * Loads a file and parses it with a YAML parser. The type of object returned depends on the layout of the yaml file and should be known
     * to clients of this service.
     *
     * @param filename
     * @return The parsed data
     * @throws IllegalStateException If .yml file cannot be loaded
     */
    public Object getYaml(String filename);

    /**
     * Loads all files in a directory and parses them with a YAML parser. The type of the objects returned depends on the layout of the yaml
     * files.
     *
     * @param dirName
     * @return A map mapping filename to the object that was parsed.
     * @throws IllegalStateException If .yml file cannot be loaded
     */
    public Map<String, Object> getYamlInFolder(String dirName);

    /**
     * Searches for the property with the specified name in this property list. If the name is not found in this property list, the default
     * property list, and its defaults, recursively, are then checked. The method returns the default value argument if the property is not
     * found.
     * <p>
     * Furthermore the specified listener will be notified if any changes are noticed on specified property
     *
     * @param name The property name.
     * @param defaultValue The default value
     * @param listener The property listener which is notified on property changes
     * @return The value in this property list with the specified key value or given default value argument.
     */
    boolean getBoolProperty(String name, boolean defaultValue, PropertyListener propertyListener);

    /**
     * Searches for the property with the specified name in this property list. If the name is not found in this property list, the default
     * property list, and its defaults, recursively, are then checked. The method returns the default value argument if the property is not
     * found.
     * <p>
     * Furthermore the specified listener will be notified if any changes are noticed on specified property
     *
     * @param name The property name.
     * @param defaultValue The default value
     * @param listener The property listener which is notified on property changes
     * @return The value in this property list with the specified key value or given default value argument.
     */
    int getIntProperty(String name, int defaultValue, PropertyListener propertyListener);

}
