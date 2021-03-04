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

package com.openexchange.ajax.framework;

import static com.openexchange.java.Autoboxing.I;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.test.common.configuration.AJAXConfig;
import com.openexchange.test.common.configuration.TestConfig;
import com.openexchange.test.common.test.pool.TestContextPool;
import com.openexchange.test.common.test.pool.TestUser;

/**
 * {@link ProvisioningSetup}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.3
 */
public class ProvisioningSetup {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ProvisioningSetup.class);

    private static final TestConfig.Property KEY = TestConfig.Property.PROV_PROPS;

    private static AtomicInteger initialized = new AtomicInteger(0);

    private static final String MASTER_IDENTIFIER = "oxadminmaster";
    private static final String MASTER_PWD_IDENTIFIER = "oxadminmaster_password";
    private static final String REST_IDENTIFIER = "restUser";
    private static final String REST_PWD_IDENTIFIER = "restPwd";

    
    /**
     * Initializes configuration etc.
     *
     * @throws OXException In case it fails
     */
    public static void init() throws OXException {
        synchronized (ProvisioningSetup.class) {
            if (initialized.get() <= 0) {
                LOG.info("Starting initialization of contexts.");
                AJAXConfig.init();
                Properties contextsAndUsers = getProperties();

                createOXAdminMaster(contextsAndUsers);
                createRestUser(contextsAndUsers);

                TestContextPool.init();

                LOG.info("Finished initialization for {} contexts.", I(TestContextPool.getAllTimeAvailableContexts().size()));
            } else {
                LOG.debug("Pool already initialized! Please do not try to remember users/pools multiple times as this will cause unexpected behavior within test execution.");
            }
            initialized.getAndIncrement();
        }
    }

    /**
     * Tears down all unused resources
     * <p>
     * Only removes resource if this is the last test class that
     * calls the clean up
     */
    public static void down() {
        if (initialized.decrementAndGet() <= 0) {
            TestContextPool.down();
        }
    }

    private static void createOXAdminMaster(Properties contextsAndUsers) {
        String user = contextsAndUsers.get(MASTER_IDENTIFIER).toString();
        String password = contextsAndUsers.get(MASTER_PWD_IDENTIFIER).toString();
        TestUser oxadminMaster = new TestUser(user, "", password);

        TestContextPool.setOxAdminMaster(oxadminMaster);
    }

    private static void createRestUser(Properties contextsAndUsers) {
        String user = contextsAndUsers.get(REST_IDENTIFIER).toString();
        String password = contextsAndUsers.get(REST_PWD_IDENTIFIER).toString();
        TestUser restUser = new TestUser(user, "", password);

        TestContextPool.setRestUser(restUser);
    }

    private static Properties getProperties() throws OXException {
        File propFile = getPropFile();

        return readPropFile(propFile);
    }

    private static File getPropFile() throws OXException {
        String propertyFileName = getPropertyFileName();

        //Check for custom provisioning properties
        final File customPropFile = new File(propertyFileName.replace(".properties", "-custom.properties"));
        if (customPropFile.exists() && customPropFile.canRead()) {
            return customPropFile;
        }

        final File propFile = new File(propertyFileName);
        if (!propFile.exists()) {
            throw ConfigurationExceptionCodes.FILE_NOT_FOUND.create(propFile.getAbsoluteFile());
        }
        if (!propFile.canRead()) {
            throw ConfigurationExceptionCodes.NOT_READABLE.create(propFile.getAbsoluteFile());
        }
        return propFile;
    }

    protected static String getPropertyFileName() throws OXException {
        final String fileName = TestConfig.getProperty(KEY);
        if (null == fileName) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(KEY.getPropertyName());
        }
        return fileName;
    }

    private static Properties readPropFile(File propFile) throws OXException {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(propFile)) {
            props.load(fis);
        } catch (FileNotFoundException e) {
            throw ConfigurationExceptionCodes.FILE_NOT_FOUND.create(propFile.getAbsolutePath(), e);
        } catch (IOException e) {
            throw ConfigurationExceptionCodes.READ_ERROR.create(propFile.getAbsolutePath(), e);
        }
        return props;
    }

}
