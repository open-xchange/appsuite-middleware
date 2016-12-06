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

package com.openexchange.configuration.v2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;

/**
 * {@link TestConfig}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.3
 */
public class TestConfig {

    /**
     * Singleton.
     */
    private static TestConfig INSTANCE;

    /**
     * Prevent instantiation
     */
    private TestConfig() {
        super();
    }

    /**
     * Key of the system property that contains the file name of the
     * system.properties configuration file.
     */
    private static final String KEY = "test.propfile";

    /**
     * {@inheritDoc}
     */
    protected String getPropertyFileName() throws OXException {
        String fileName = System.getProperty(KEY);
        if (null == fileName) {
            fileName = "conf/test.properties";
        }
        return fileName;
    }

    /**
     * Reads the configuration.
     * 
     * @throws OXException if reading configuration fails.
     */
    public static void init() throws OXException {
        if (null == INSTANCE) {
            synchronized (TestConfig.class) {
                if (null == INSTANCE) {
                    INSTANCE = new TestConfig();
                    INSTANCE.loadTestConfigFiles();
                }
            }
        }
    }

    private void loadTestConfigFiles() throws OXException {
        loadProperties(getPropertyFileName());
    }

    private final void loadProperties(final String propFileName) throws OXException {
        if (null == propFileName) {
            throw ConfigurationExceptionCodes.NO_FILENAME.create();
        }
        final File propFile = new File(propFileName);
        if (!propFile.exists()) {
            throw ConfigurationExceptionCodes.FILE_NOT_FOUND.create(propFile.getAbsoluteFile());
        }
        if (!propFile.canRead()) {
            throw ConfigurationExceptionCodes.NOT_READABLE.create(propFile.getAbsoluteFile());
        }

        Properties props = new Properties();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(propFile);
            props.load(fis);
        } catch (final FileNotFoundException e) {
            throw ConfigurationExceptionCodes.FILE_NOT_FOUND.create(propFile.getAbsolutePath(), e);
        } catch (final IOException e) {
            throw ConfigurationExceptionCodes.READ_ERROR.create(propFile.getAbsolutePath(), e);
        } finally {
            Streams.close(fis);
        }
    }
}
