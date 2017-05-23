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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.ajax.framework.config.util.PropertyHelper;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.configuration.TestConfig;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.test.pool.TestContext;
import com.openexchange.test.pool.TestContextPool;
import com.openexchange.test.pool.TestUser;

/**
 * {@link ProvisioningSetup}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.3
 */
public class ProvisioningSetup {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ProvisioningSetup.class);

    private static final TestConfig.Property KEY = TestConfig.Property.PROV_PROPS;

    private static AtomicBoolean initialized = new AtomicBoolean();

    private static final String PASSWORD_IDENTIFIER = "password";
    private static final String ADMIN_IDENTIFIER = "oxadmin";
    private static final String MASTER_IDENTIFIER = "oxadminmaster";
    private static final String MASTER_PWD_IDENTIFIER = "oxadminmaster_password";
    private static final String REST_IDENTIFIER = "restUser";
    private static final String REST_PWD_IDENTIFIER = "restPwd";

    private static final String CONTEXT_IDENTIFIER = "context";
    private static final String USER1_IDENTIFIER = "user1";
    private static final String USER2_IDENTIFIER = "user2";
    private static final String USER3_IDENTIFIER = "user3";
    private static final String USER4_IDENTIFIER = "user4";
    private static final String USER5_IDENTIFIER = "user5";
    private static final String NO_REPLY_IDENTIFIER = "noreply";
    private static final String PARTICIPANT1_IDENTIFIER = "participant1";
    private static final String PARTICIPANT2_IDENTIFIER = "participant2";
    private static final String PARTICIPANT3_IDENTIFIER = "participant3";
    private static final String GROUP_PARTICIPANT_IDENTIFIER = "group_participant";
    private static final String RESOURCE_PARTICIPANT = "resource_participant";
    private static final String RESOURCE_PARTICIPANT1 = "resource_participant1";
    private static final String RESOURCE_PARTICIPANT2 = "resource_participant2";
    private static final String RESOURCE_PARTICIPANT3 = "resource_participant3";

    public static void init() throws OXException {
        synchronized (ProvisioningSetup.class) {
            if (!initialized.get()) {
                LOG.info("Starting initialization of contexts.");
                AJAXConfig.init();
                Properties contextsAndUsers = getProperties();

                createProvisionedContext(contextsAndUsers);
                createOXAdminMaster(contextsAndUsers);
                createRestUser(contextsAndUsers);

                TestContextPool.startWatcher();

                initialized.compareAndSet(false, true);
                LOG.info("Finished initialization for {} contexts.", TestContextPool.getAllTimeAvailableContexts().size());
            }
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

    private static void createProvisionedContext(Properties contextsAndUsers) {
        String password = contextsAndUsers.getProperty(PASSWORD_IDENTIFIER);
        String oxadmin = contextsAndUsers.getProperty(ADMIN_IDENTIFIER);

        Map<String, Object> map = new HashMap<String, Object>();
        for (final String name : contextsAndUsers.stringPropertyNames()) {
            map.put(name, contextsAndUsers.getProperty(name));
        }

        for (int i = 1;; i++) {
            String prefix = Integer.toString(i) + ".";
            Map<String, Object> filter = PropertyHelper.filter(map, prefix, true, false);

            if ((filter == null) || (filter.size() == 0)) {
                break;
            }

            String contextName = filter.get(prefix + CONTEXT_IDENTIFIER).toString();
            try {
                TestContext context = new TestContext(contextName);
                context.setAdmin(new TestUser(oxadmin, contextName, password));

                String userId1 = filter.get(prefix + USER1_IDENTIFIER).toString();
                TestUser testUser = new TestUser(userId1, contextName, password);
                context.addUser(testUser);

                String userId2 = filter.get(prefix + USER2_IDENTIFIER).toString();
                TestUser testUser2 = new TestUser(userId2, contextName, password);
                context.addUser(testUser2);

                String userId3 = filter.get(prefix + USER3_IDENTIFIER).toString();
                TestUser testUser3 = new TestUser(userId3, contextName, password);
                context.addUser(testUser3);

                String userId4 = filter.get(prefix + USER4_IDENTIFIER).toString();
                TestUser testUser4 = new TestUser(userId4, contextName, password);
                context.addUser(testUser4);

                String userId5 = filter.get(prefix + USER5_IDENTIFIER).toString();
                TestUser testUser5 = new TestUser(userId5, contextName, password);
                context.addUser(testUser5);

                String noReply = filter.get(prefix + NO_REPLY_IDENTIFIER).toString();
                TestUser noReplyUser = new TestUser(noReply, contextName, password);
                context.setNoReplyUser(noReplyUser);

                context.addUserParticipants(filter.get(prefix + PARTICIPANT1_IDENTIFIER).toString());
                context.addUserParticipants(filter.get(prefix + PARTICIPANT2_IDENTIFIER).toString());
                context.addUserParticipants(filter.get(prefix + PARTICIPANT3_IDENTIFIER).toString());
                context.addGroupParticipant(filter.get(prefix + GROUP_PARTICIPANT_IDENTIFIER).toString());
                context.addResourceParticipants(filter.get(prefix + RESOURCE_PARTICIPANT).toString());
                context.addResourceParticipants(filter.get(prefix + RESOURCE_PARTICIPANT1).toString());
                context.addResourceParticipants(filter.get(prefix + RESOURCE_PARTICIPANT2).toString());
                context.addResourceParticipants(filter.get(prefix + RESOURCE_PARTICIPANT3).toString());

                TestContextPool.addContext(context);
            } catch (Exception e) {
                LOG.warn("Unable to add context {} to context registry.", contextName, e);
                // TODO: handle exception
            }
        }
    }

    private static Properties getProperties() throws OXException {
        File propFile = getPropFile();

        Properties contextsAndUserProps = readPropFile(propFile);

        return contextsAndUserProps;
    }

    private static File getPropFile() throws OXException {
        String propertyFileName = getPropertyFileName();
        if (null == propertyFileName) {
            throw ConfigurationExceptionCodes.NO_FILENAME.create();
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
        return props;
    }
}
