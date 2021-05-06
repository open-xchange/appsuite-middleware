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
 *    trademarks of the OX Software GmbH. group of companies.
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

import static com.openexchange.config.utils.SysEnv.getSystemEnvironment;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;
import com.openexchange.config.utils.TokenReplacingReader;
import com.openexchange.java.Charsets;
import com.openexchange.java.Reference;
import com.openexchange.java.Streams;

/**
 * {@link ConfigurationServices} - A utility class for {@link ConfigurationService}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class ConfigurationServices {

    /**
     * Initializes a new {@link ConfigurationServices}.
     */
    private ConfigurationServices() {
        super();
    }

    /**
     * Loads the properties from specified file.
     *
     * @param file The file to read from
     * @return The properties or <code>null</code> (if no such file exists)
     * @throws IOException If reading from file yields an I/O error
     * @throws IllegalArgumentException If file is invalid
     */
    public static Properties loadPropertiesFrom(File file) throws IOException {
        return loadPropertiesFrom(file, false, null);
    }

    /**
     * Loads the properties from specified file.
     *
     * @param file The file to read from
     * @param withSysEnvLookUp Whether a primary look-up in system environment variables is supposed to be performed
     * @return The properties or <code>null</code> (if no such file exists)
     * @throws IOException If reading from file yields an I/O error
     * @throws IllegalArgumentException If file is invalid
     */
    public static Properties loadPropertiesFrom(File file, boolean withSysEnvLookUp) throws IOException {
        return loadPropertiesFrom(file, withSysEnvLookUp, null);
    }

    /**
     * Loads the properties from specified file.
     *
     * @param file The file to read from
     * @param withSysEnvLookUp Whether a primary look-up in system environment variables is supposed to be performed
     * @param sysEnvPropertiesReference The reference to track properties that were replaced by a system environment variable or <code>null</code> to not track those
     * @return The properties or <code>null</code> (if no such file exists)
     * @throws IOException If reading from file yields an I/O error
     * @throws IllegalArgumentException If file is invalid
     */
    public static Properties loadPropertiesFrom(File file, boolean withSysEnvLookUp, Reference<Set<String>> sysEnvPropertiesReference) throws IOException {
        if (null == file) {
            return null;
        }

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            return loadPropertiesFrom(fis, withSysEnvLookUp, file.getName(), sysEnvPropertiesReference);
        } catch (FileNotFoundException e) {
            return null;
        } finally {
            Streams.close(fis);
        }
    }

    /**
     * Loads the properties from specified input stream.
     *
     * @param in The input stream to read from
     * @param withSysEnvLookUp Whether a primary look-up in system environment variables is supposed to be performed
     * @return The properties
     * @throws IOException If reading from input stream yields an I/O error
     * @throws IllegalArgumentException If input stream is invalid
     */
    public static Properties loadPropertiesFrom(InputStream in, boolean withSysEnvLookUp) throws IOException {
        if (null == in) {
            return null;
        }

        return loadPropertiesFrom(in, withSysEnvLookUp, null, null);
    }

    /**
     * If <code>true</code> it avoids possibly creating an unnecessary instance of {@link java.util.Properties} when checking for property
     * counterparts in system environment variables, but requires each usage of returned instance to only invoke methods that do respect
     * {@link java.util.Properties#defaults}; e.g. invoking {@link java.util.Properties#entrySet()} does not respect <code>defaults</code>.
     * <p>
     * If <code>false</code> an of {@link java.util.Properties} is created anyway, regardless if there are any counterparts in system
     * environment variables.
     */
    private static final boolean INIT_SYSENV_PROPS_WITH_DEFAULTS = false;

    /**
     * Loads the properties from specified input stream.
     *
     * @param in The input stream to read from
     * @param withSysEnvLookUp Whether a primary look-up in system environment variables is supposed to be performed
     * @param optFileName The name of the .properties file or <code>null</code> if not available
     * @param sysEnvPropertiesReference The reference to track properties that were replaced by a system environment variable or <code>null</code> to not track those
     * @return The properties
     * @throws IOException If reading from input stream yields an I/O error
     * @throws IllegalArgumentException If input stream is invalid
     */
    private static Properties loadPropertiesFrom(InputStream in, boolean withSysEnvLookUp, String optFileName, Reference<Set<String>> sysEnvPropertiesReference) throws IOException {
        InputStreamReader fr = null;
        BufferedReader br = null;
        TokenReplacingReader trr = null;
        try {
            // Initialize reader
            fr = new InputStreamReader(in, Charsets.UTF_8);
            trr = new TokenReplacingReader((br = new BufferedReader(fr, 2048)));

            // Load properties
            Properties properties = new Properties();
            properties.load(trr);

            if (false == withSysEnvLookUp) {
                // Don't check for possible system environment variables. Return as-is.
                return properties;
            }

            // Look-up in system environment variables
            Map<String, String> sysenv = getSystemEnvironment();
            if (INIT_SYSENV_PROPS_WITH_DEFAULTS) {
                // Optionally initialize dedicated java.util.Properties instance using java.util.Properties.defaults
                Properties propertiesWithSysEnv = null;
                for (Map.Entry<Object, Object> e : properties.entrySet()) {
                    String propName = e.getKey().toString().trim();
                    String value = checkForSysEnvVariable(propName, sysenv, optFileName);
                    if (value != null) {
                        // Found an environment variable for current property
                        if (propertiesWithSysEnv == null) {
                            propertiesWithSysEnv = new Properties(properties);
                        }
                        propertiesWithSysEnv.put(propName, value.trim());
                        if (sysEnvPropertiesReference != null) {
                            Set<String> sysEnvProperties = sysEnvPropertiesReference.getValue();
                            if (sysEnvProperties == null) {
                                sysEnvProperties = new HashSet<>();
                                sysEnvPropertiesReference.setValue(sysEnvProperties);
                            }
                            sysEnvProperties.add(propName);
                        }
                    }
                }
                return propertiesWithSysEnv == null ? properties : propertiesWithSysEnv;
            }

            // Initialize dedicated java.util.Properties instance not using java.util.Properties.defaults
            Properties propertiesWithSysEnv = new Properties();
            for (Map.Entry<Object, Object> e : properties.entrySet()) {
                String propName = e.getKey().toString().trim();
                String value = checkForSysEnvVariable(propName, sysenv, optFileName);
                if (value != null) {
                    // Found an environment variable for current property
                    propertiesWithSysEnv.put(propName, value.trim());
                    if (sysEnvPropertiesReference != null) {
                        Set<String> sysEnvProperties = sysEnvPropertiesReference.getValue();
                        if (sysEnvProperties == null) {
                            sysEnvProperties = new HashSet<>();
                            sysEnvPropertiesReference.setValue(sysEnvProperties);
                        }
                        sysEnvProperties.add(propName);
                    }
                } else {
                    // Found no environment variable for current property
                    propertiesWithSysEnv.put(propName, e.getValue().toString().trim());
                }
            }
            return propertiesWithSysEnv;
        } finally {
            Streams.close(trr, br, fr, in);
        }
    }

    private static final String SYSENV_PROP_NAME_DELIMITER = "__";

    /**
     * Tries to acquire the environment variable corresponding to given property name and optional file name. In case a file name is given,
     * a look-up in environment variables happens first with file name prefix and provided that first look-up yields no result a second
     * look-up is performed using only the property name portion.
     * <p>
     * For every property there is deducible name for its associated environment variable according to pattern:
     * <pre>
     *   [variable-name]    = [variable-name] | [file-name] + "__" + [variable-name]
     *
     *   [file-name]        = lower-case string of given file name with every non-digit and non-letter character replaced
     *                        with "_"; subsequent "_" characters are folded into one
     *
     *   [variable-name]    = upper-case string of given property name with every non-digit and non-letter character replaced
     *                        with "_"; subsequent "_" characters are folded into one
     * </pre>
     * <p>
     * Examples:<br>
     * <p>
     * <blockquote><table cellpadding=1 cellspacing=0 summary="Capturing group numberings">
     * <tr><th style="text-align:right; white-space:nowrap">File name&nbsp;&nbsp;&nbsp;&nbsp;</th>
     *     <td><tt><code>"attachment.properties"</code></tt></td></tr>
     * <tr><th style="text-align:right; white-space:nowrap">Property name&nbsp;&nbsp;&nbsp;&nbsp;</th>
     *     <td><tt><code>"MAX_UPLOAD_SIZE"</code></tt></td></tr>
     * <tr><th style="text-align:right; white-space:nowrap">Env. variable name&nbsp;&nbsp;&nbsp;</th>
     *     <td><tt><code>"attachment_properties__MAX_UPLOAD_SIZE"</code></tt></td></tr>
     * <tr><th style="text-align:right; white-space:nowrap">Look-up behavior&nbsp;&nbsp;&nbsp;&nbsp;</th>
     *     <td><tt>First with <code>"attachment_properties__MAX_UPLOAD_SIZE"</code>, then <code>"MAX_UPLOAD_SIZE"</code></tt></td></tr>
     * </table></blockquote>
     * <p>
     * <blockquote><table cellpadding=1 cellspacing=0 summary="Capturing group numberings">
     * <tr><th style="text-align:right; white-space:nowrap">File name&nbsp;&nbsp;&nbsp;&nbsp;</th>
     *     <td><tt><code>null</code></tt></td></tr>
     * <tr><th style="text-align:right; white-space:nowrap">Property name&nbsp;&nbsp;&nbsp;&nbsp;</th>
     *     <td><tt><code>"com.openexchange.someprop"</code></tt></td></tr>
     * <tr><th style="text-align:right; white-space:nowrap">Env. variable name&nbsp;&nbsp;&nbsp;</th>
     *     <td><tt><code>"COM_OPENEXCHANGE_SOMEPROP"</code></tt></td></tr>
     * <tr><th style="text-align:right; white-space:nowrap">Look-up behavior&nbsp;&nbsp;&nbsp;&nbsp;</th>
     *     <td><tt>With <code>"COM_OPENEXCHANGE_SOMEPROP"</code></tt></td></tr>
     * </table></blockquote>
     * <p>
     * <blockquote><table cellpadding=1 cellspacing=0 summary="Capturing group numberings">
     * <tr><th style="text-align:right; white-space:nowrap">File name&nbsp;&nbsp;&nbsp;&nbsp;</th>
     *     <td><tt><code>"mail.properties"</code></tt></td></tr>
     * <tr><th style="text-align:right; white-space:nowrap">Property name&nbsp;&nbsp;&nbsp;&nbsp;</th>
     *     <td><tt><code>"com.openexchange.mail.mailServer"</code></tt></td></tr>
     * <tr><th style="text-align:right; white-space:nowrap">Env. variable name&nbsp;&nbsp;&nbsp;</th>
     *     <td><tt><code>"mail_properties__COM_OPENEXCHANGE_MAIL_MAILSERVER"</code></tt></td></tr>
     * <tr><th style="text-align:right; white-space:nowrap">Look-up behavior&nbsp;&nbsp;&nbsp;&nbsp;</th>
     *     <td><tt>First with <code>"mail_properties__COM_OPENEXCHANGE_MAIL_MAILSERVER"</code>, then <code>"COM_OPENEXCHANGE_MAIL_MAILSERVER"</code></tt></td></tr>
     * </table></blockquote>
     *
     * @param propName The property name
     * @param sysenv The environment variables map to look-up
     * @param optFileName The optional file name
     * @return The value of the respective environment variable or <code>null</code> if there is no such environment variable
     */
    private static String checkForSysEnvVariable(String propName, Map<String, String> sysenv, String optFileName) {
        // First try with fully-qualified system environment variable name: <file-name> + "__" + <variable-name>; e.g. "attachment_properties__MAX_UPLOAD_SIZE"
        String fqnSysEnvPropName = generateFqnSysEnvNameFor(propName, optFileName);
        String value = sysenv.get(fqnSysEnvPropName);
        if (value == null) {
            // Next try with delimited system environment variable name w/o file name prefix; e.g. "COM_OPENEXCHANGE_SOMEPROP"
            String delimiter = SYSENV_PROP_NAME_DELIMITER;
            int delimPos = fqnSysEnvPropName.indexOf(delimiter);
            if (delimPos > 0) {
                String sysEnvPropName = fqnSysEnvPropName.substring(delimPos + delimiter.length());
                value = sysenv.get(sysEnvPropName);
            }
        }
        return value;
    }

    private static String generateFqnSysEnvNameFor(String propName, String optFileName) {
        int length = propName.length();

        // Initialize builder and optionally prepend file name
        StringBuilder builder;
        if (optFileName == null) {
            builder = new StringBuilder(length);
        } else {
            int fnlen = optFileName.length();
            builder = new StringBuilder(length + fnlen + 2);
            for (int i = 0; i < fnlen; i++) {
                appendUpperOrLowerCase(optFileName.charAt(i), false, builder);
            }
            builder.append(SYSENV_PROP_NAME_DELIMITER);
        }

        // Append sys-env version of property name
        for (int i = 0; i < length; i++) {
            appendUpperOrLowerCase(propName.charAt(i), true, builder);
        }

        // Return result
        return builder.toString();
    }

    /**
     * Appends the given character as either upper-case or lower-case to the given builder in case it is a letter or a digit. Otherwise
     * appends an underscore if the previous character is not a underscore, while subsequent underscore characters are folded into one.
     *
     * @param c The character to add
     * @param upperCase <code>true</code> if the character shall be transformed to upper-case, otherwise to lower-case
     * @param builder The {@link StringBuilder} to append the character to
     */
    private static void appendUpperOrLowerCase(char c, boolean upperCase, StringBuilder builder) {
        // TODO: Respect surrogate pairs?
        if (upperCase) {
            if ((c >= 'a') && (c <= 'z')) {
                builder.append((char) (c & 0x5f));
            } else if ((c >= 'A') && (c <= 'Z')) {
                builder.append(c);
            } else if (Character.isLetterOrDigit(c)) {
                builder.append(Character.toUpperCase(c));
            } else {
                if (builder.length() <= 0 || builder.charAt(builder.length() - 1) != '_') {
                    // Previously appended character is not an underscore
                    builder.append('_');
                }
            }
        } else {
            if ((c >= 'A') && (c <= 'Z')) {
                builder.append((char) (c ^ 0x20));
            } else if ((c >= 'a') && (c <= 'z')) {
                builder.append(c);
            } else if (Character.isLetterOrDigit(c)) {
                builder.append(Character.toLowerCase(c));
            } else {
                if (builder.length() <= 0 || builder.charAt(builder.length() - 1) != '_') {
                    // Previously appended character is not an underscore
                    builder.append('_');
                }
            }
        }
    }

    /**
     * Loads the YAML object from given file.
     *
     * @param file The file to read from
     * @return The YAML object or <code>null</code> (if no such file exists)
     * @throws IOException If reading from file yields an I/O error
     * @throws IllegalArgumentException If file is no valid YAML
     */
    public static Object loadYamlFrom(File file) throws IOException {
        FileInputStream fis = null;
        InputStreamReader fr = null;
        BufferedReader br = null;
        TokenReplacingReader trr = null;
        try {
            fr = new InputStreamReader((fis = new FileInputStream(file)), Charsets.UTF_8);
            trr = new TokenReplacingReader((br = new BufferedReader(fr, 2048)));
            Yaml yaml = new Yaml(new SafeConstructor());
            return yaml.load(trr);
        } catch (YAMLException e) {
            throw new IllegalArgumentException("Failed to load YAML file '" + file + ". Please fix any syntax errors in it.", e);
        } finally {
            Streams.close(trr, br, fr, fis);
        }
    }

    /**
     * Loads the YAML object from given stream.
     *
     * @param in The stream to read from
     * @return The YAML object or <code>null</code> (if no such file exists)
     * @throws IOException If reading from stream yields an I/O error
     * @throws IllegalArgumentException If stream data is no valid YAML
     */
    public static Object loadYamlFrom(InputStream in) {
        if (null == in) {
            return null;
        }

        InputStreamReader fr = null;
        BufferedReader br = null;
        TokenReplacingReader trr = null;
        try {
            fr = new InputStreamReader(in, Charsets.UTF_8);
            trr = new TokenReplacingReader((br = new BufferedReader(fr, 2048)));
            Yaml yaml = new Yaml(new SafeConstructor());
            return yaml.load(trr);
        } catch (YAMLException e) {
            throw new IllegalArgumentException("Failed to read YAML content from given input stream.", e);
        } finally {
            Streams.close(trr, br, fr, in);
        }
    }

    /**
     * Parse the only YAML document in a stream and produce the corresponding Java object.
     *
     * @param <T> The class is defined by the second argument
     * @param in The stream to read from
     * @param type The class of the object to be created
     * @return The YAML object or <code>null</code> (if no such file exists)
     * @throws IOException If reading from stream yields an I/O error
     * @throws IllegalArgumentException If stream data is no valid YAML
     */
    public static <T> T loadYamlAs(InputStream in, Class<T> type) {
        if (null == in) {
            return null;
        }

        InputStreamReader fr = null;
        BufferedReader br = null;
        TokenReplacingReader trr = null;
        try {
            fr = new InputStreamReader(in, Charsets.UTF_8);
            trr = new TokenReplacingReader((br = new BufferedReader(fr, 2048)));
            Yaml yaml = new Yaml(new SafeConstructor());
            return yaml.loadAs(trr, type);
        } catch (YAMLException e) {
            throw new IllegalArgumentException("Failed to read YAML content from given input stream.", e);
        } finally {
            Streams.close(trr, br, fr, in);
        }
    }

    /**
     * Computes the <code>"SHA-256"</code> hash from given file.
     *
     * @param file The file to compute the hash from
     * @return The computed hash
     * @throws IllegalStateException If hash cannot be computed; file is corrupt/non-existing
     */
    public static byte[] getHash(File file) {
        return getHash(file, "SHA-256");
    }

    /**
     * Computes the hash using given algorithm from given file.
     *
     * @param file The file to compute the hash from
     * @param algorithm The algorithm to use
     * @return The computed hash
     * @throws IllegalStateException If hash cannot be computed; either algorithm is unknown or file is corrupt/non-existing
     */
    public static byte[] getHash(File file, String algorithm) {
        if (null == file || null == algorithm) {
            return null;
        }

        DigestInputStream digestInputStream = null;
        try {
            digestInputStream = new DigestInputStream(new FileInputStream(file), MessageDigest.getInstance(algorithm));
            int len = 8192;
            byte[] buf = new byte[len];
            while (digestInputStream.read(buf, 0, len) > 0) {
                // Discard
            }
            return digestInputStream.getMessageDigest().digest();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No such algorithm '" + algorithm + "'.", e);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read file '" + file + "'. Reason: " + e.getMessage(), e);
        } finally {
            Streams.close(digestInputStream);
        }
    }

    /**
     * Reads the content from given file.
     *
     * @param file The file to read from
     * @return The file content or <code>null</code> (if passed file is <code>null</code>)
     * @throws IOException If an I/O error occurs
     */
    public static String readFile(File file) throws IOException {
        if (null == file) {
            return null;
        }

        FileInputStream fis = null;
        InputStreamReader fr = null;
        BufferedReader br = null;
        TokenReplacingReader trr = null;
        try {
            int length = (int) file.length();

            fr = new InputStreamReader((fis = new FileInputStream(file)), Charsets.UTF_8);
            trr = new TokenReplacingReader((br = new BufferedReader(fr, length > 16384 ? 16384 : length)));

            StringBuilder builder = new StringBuilder(length);
            int buflen = 2048;
            char[] cbuf = new char[buflen];
            for (int read; (read = trr.read(cbuf, 0, buflen)) > 0;) {
                builder.append(cbuf, 0, read);
            }
            return builder.toString();
        } finally {
            Streams.close(trr, br, fr, fis);
        }
    }

}
