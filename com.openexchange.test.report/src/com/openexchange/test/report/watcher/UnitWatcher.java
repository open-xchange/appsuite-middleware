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

package com.openexchange.test.report.watcher;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import com.openexchange.test.report.TestResult;
import com.openexchange.test.report.annotations.TaggedTestCase;
import com.openexchange.test.report.protocol.ConversationProtocol;
import com.openexchange.test.report.protocol.ConversationProtocolAware;

/**
 * {@link UnitWatcher}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class UnitWatcher extends TestWatcher {

    private ClassWatcher watcher;
    private ConversationProtocolAware client;

    /**
     * Initializes a new {@link UnitWatcher}.
     * 
     * @param watcher The ClassWatcher for this UnitWatcher
     * @param client The IN8TestClient
     */
    public UnitWatcher(ClassWatcher watcher, ConversationProtocolAware client) {
        this.watcher = watcher;
        this.client = client;
    }

    @Override
    protected void failed(Throwable e, Description description) {
        watcher.incrementFailures();
        evaluate(description, false, e);
    }

    @Override
    protected void succeeded(Description description) {
        evaluate(description, true, null);
    }

    private void evaluate(Description description, boolean succeeded, Throwable e) {
        if (description.getAnnotation(TaggedTestCase.class) != null) {

            TaggedTestCase vtc = description.getAnnotation(TaggedTestCase.class);
            String[] actions = vtc.actions();
            List<ConversationProtocol> protocol = new ArrayList<ConversationProtocol>(client.getConversationProtocol());

            for (String action : actions) {
                int count = 0;
                Iterator<ConversationProtocol> iterator = protocol.iterator();
                while (iterator.hasNext()) {
                    ConversationProtocol p = iterator.next();
                    if (action.equals(p.getAction())) {
                        TestResult result = new TestResult();
                        result.setTestClass(description.getClassName());
                        result.setTestMethod(description.getMethodName());
                        result.setDescription(vtc.description());
                        result.setAction(p.getAction());
                        result.setParameters(p.getRequestParameters());
                        result.setDuration(p.getRequestDuration());
                        result.setAssertationStatus(succeeded);
                        if (p.getBody() != null) {
                            result.setBody(p.getBody());
                        }
                        if (e != null) {
                            result.setFailureReason(e.getMessage());
                            result.setThrowable(e);
                        }

                        watcher.getReportWriter().addTestResult(result);
                        break;
                    }
                    count++;
                }
                protocol.remove(count);
            }
        }
    }
}
