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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.json.JSONException;
import com.openexchange.ajax.smtptest.actions.SMTPInitResponse;
import com.openexchange.ajax.smtptest.actions.StartSMTPRequest;
import com.openexchange.ajax.smtptest.actions.StopSMTPRequest;
import com.openexchange.ajax.smtptest.actions.UpdateMailAccountRequest;
import com.openexchange.exception.OXException;
import com.openexchange.test.pool.TestContext;
import com.openexchange.test.pool.TestContextPool;
import com.openexchange.test.pool.TestUser;

/**
 * {@link SmtpMockSetup}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.4
 */
public class SmtpMockSetup {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SmtpMockSetup.class);

    private static AtomicBoolean initialized = new AtomicBoolean();

    private static List<TestContext> contextsWithStartedMock;

    public static void init() throws OXException {
        synchronized (SmtpMockSetup.class) {
            if (!initialized.get()) {
                ProvisioningSetup.init();

                startSmtpMockForAllContexts();

                initialized.compareAndSet(false, true);
            }
        }
    }

    private static void startSmtpMockForAllContexts() {
        contextsWithStartedMock = TestContextPool.getCopyOfCurrentlyAvailableContexts();

        for (TestContext testContext : contextsWithStartedMock) {
            TestUser noReplyUser = testContext.getNoReplyUser();
            SmtpMockConfig smtpMockConfig = startSmtpMockServerAndSetNoReply(noReplyUser);
            
            List<TestUser> allUsers = testContext.getCopyOfAll();
            for (TestUser user : allUsers) {
                rewriteMailAccountConfig(user, smtpMockConfig);
            }
        }
    }

    private static SmtpMockConfig startSmtpMockServerAndSetNoReply(TestUser userNoReply) {
        try {
            AJAXClient client = new AJAXClient(userNoReply);
            StartSMTPRequest request = new StartSMTPRequest(true, client.getValues().getContextId(), userNoReply.getLogin());

            SMTPInitResponse response = client.execute(request);
            return new SmtpMockConfig(response.getHostname(), response.getPort());
        } catch (OXException | IOException | JSONException e) {
            LOG.error("", e);
        }
        return null;
    }

    private static void rewriteMailAccountConfig(TestUser user, SmtpMockConfig config) {
        try {
            AJAXClient client = new AJAXClient(user);
            SMTPInitResponse response = client.execute(new UpdateMailAccountRequest(config.getHostname(), config.getPort()));
        } catch (OXException | IOException | JSONException e) {
            LOG.error("", e);
        }
    }

    public static void restore() {
        synchronized (SmtpMockSetup.class) {
            if (initialized.get()) {
                stopSMTPMockForAllContexts();

                initialized.compareAndSet(true, false);
            }
        }
    }

    private static void stopSMTPMockForAllContexts() {
        List<TestContext> testContexts = contextsWithStartedMock;

        for (TestContext testContext : testContexts) {
            List<TestUser> allUsers = testContext.getCopyOfAll();
            for (TestUser user : allUsers) {
                stopSMTPMockServer(user);
            }
        }
    }

    private static void stopSMTPMockServer(TestUser user) {
        try {
            AJAXClient client = new AJAXClient(user);
            StopSMTPRequest request = new StopSMTPRequest(true);
            SMTPInitResponse response = client.execute(request);
        } catch (OXException | IOException | JSONException e) {
            LOG.error("", e);
        }
    }

    private static class SmtpMockConfig {

        private final int port;
        private final String hostname;

        public SmtpMockConfig(String hostname, int port) {
            this.port = port;
            this.hostname = hostname;
        }

        public int getPort() {
            return port;
        }

        public String getHostname() {
            return hostname;
        }
    }
}
