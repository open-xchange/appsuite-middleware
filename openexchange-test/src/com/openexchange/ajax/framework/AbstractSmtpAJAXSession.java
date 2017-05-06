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

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.smtptest.actions.ClearMailsRequest;
import com.openexchange.test.pool.TestUser;

public abstract class AbstractSmtpAJAXSession extends AbstractAJAXSession {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractSmtpAJAXSession.class);

    private static final AtomicInteger counter = new AtomicInteger();

    protected TestUser noReplyUser;
    private AJAXClient noReplyClient;

    @BeforeClass
    public static void before() throws Exception {
        counter.incrementAndGet();
        SmtpMockSetup.init();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        List<TestUser> copyOfAll = testContext.getCopyOfAll();
        for (TestUser user : copyOfAll) {
            AJAXClient currentClient =  new AJAXClient(user);
            currentClient.execute(new ClearMailsRequest());
            currentClient.logout();
        }
        noReplyUser = testContext.getNoReplyUser();
        noReplyClient = new AJAXClient(noReplyUser);
        getNoReplyClient().execute(new ClearMailsRequest());
    }

    @Override
    public void tearDown() throws Exception {
        try {
            if (noReplyClient != null) {
                // Client can be null if setUp() fails
                noReplyClient.logout();
                noReplyClient = null;
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(AbstractSmtpAJAXSession.class).error("Unable to correctly tear down test setup.", e);
        } finally {
            super.tearDown();
        }
    }

    @AfterClass
    public static void afterClass() throws Exception {
        synchronized (AbstractSmtpAJAXSession.class) {
            int get = counter.decrementAndGet();
            if (get == 0) {
                LOG.info("No more test running. Going to restore SMTP config.", new Throwable());
                SmtpMockSetup.restore();
            }
        }
    }

    public AJAXClient getNoReplyClient() {
        return noReplyClient;
    }
}
