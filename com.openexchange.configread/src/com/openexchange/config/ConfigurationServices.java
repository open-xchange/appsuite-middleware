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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import com.openexchange.java.Streams;

/**
 * {@link ConfigurationServices} - A utility class for {@link ConfigurationService}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class ConfigurationServices {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ConfigurationServices.class);

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
        if (null == file) {
            return null;
        }

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            Properties properties = new Properties();
            properties.load(fis);
            return properties;
        } catch (FileNotFoundException e) {
            return null;
        } finally {
            Streams.close(fis);
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
        FileReader reader = null;
        try {
            reader = new FileReader(file);
            Yaml yaml = new Yaml();
            return yaml.load(reader);
        } catch (YAMLException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        } finally {
            Streams.close(reader);
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
    public static Object loadYamlFrom(InputStream in) throws IOException {
        if (null == in) {
            return null;
        }

        try {
            Yaml yaml = new Yaml();
            return yaml.load(in);
        } catch (YAMLException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        } finally {
            Streams.close(in);
        }
    }

    /**
     * Computes the <code>"SHA-256"</code> hash from given file.
     *
     * @param file The file to compute the hash from
     * @return The computed hash
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

}
