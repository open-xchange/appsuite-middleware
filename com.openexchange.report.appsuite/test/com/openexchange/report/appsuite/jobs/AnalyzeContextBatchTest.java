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

package com.openexchange.report.appsuite.jobs;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.report.appsuite.ContextReportCumulator;
import com.openexchange.report.appsuite.ReportContextHandler;
import com.openexchange.report.appsuite.ReportFinishingTouches;
import com.openexchange.report.appsuite.ReportUserHandler;
import com.openexchange.report.appsuite.UserReportCumulator;
import com.openexchange.report.appsuite.defaultHandlers.CapabilityHandler;
import com.openexchange.report.appsuite.defaultHandlers.ClientLoginCount;
import com.openexchange.report.appsuite.defaultHandlers.Total;
import com.openexchange.report.appsuite.internal.Services;


/**
 * {@link AnalyzeContextBatchTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class AnalyzeContextBatchTest {

    private AnalyzeContextBatch analyzeContextBatch;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {

        // Register the implementations for the default report
        CapabilityHandler capabilityHandler = new CapabilityHandler();

        Services.add((ReportUserHandler) capabilityHandler);
        Services.add((ReportContextHandler) capabilityHandler);
        Services.add((UserReportCumulator) capabilityHandler);
        Services.add((ContextReportCumulator) capabilityHandler);
        Services.add((ReportFinishingTouches) capabilityHandler);

        Total total = new Total();
        Services.add(total);

        ClientLoginCount clc = new ClientLoginCount();
        Services.add(clc);

    }

    @Test
    public void testRun() throws Exception {
        List<Integer> contexts = new ArrayList<>();
        contexts.add(1);
        analyzeContextBatch = new AnalyzeContextBatch(UUID.randomUUID().toString(), "default", contexts) {
            @Override
            protected User[] loadUsers(Context ctx) throws OXException {
                UserImpl user = new UserImpl();
                User[] users = new User[]{user};

                return users;
            }
        };

        analyzeContextBatch.call();
    }

}
