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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.report.appsuite.management;

import com.openexchange.exception.OXException;
import com.openexchange.report.appsuite.Report;
import com.openexchange.report.appsuite.jobs.Orchestration;


/**
 * The {@link ReportMXBeanImpl} implements the MXBean interface by delegating all calls to
 * the {@link Orchestration} singleton and wrapping reports as {@link JMXReport} instances
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ReportMXBeanImpl implements ReportMXBean {

    @Override
    public String run() throws Exception {
        try {
            return Orchestration.getInstance().run();
        } catch (OXException e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public String run(String reportType) throws Exception {
        try {
            return Orchestration.getInstance().run();
        } catch (OXException e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public JMXReport retrieveLastReport(String reportType) throws Exception {
        return new JMXReport(Orchestration.getInstance().getLastReport(reportType));
    }
    
    @Override
    public JMXReport retrieveLastReport() throws Exception {
        return new JMXReport(Orchestration.getInstance().getLastReport());
    }

    @Override
    public JMXReport[] retrievePendingReports(String reportType) throws Exception {
        return JMXReport.wrap(Orchestration.getInstance().getPendingReports(reportType));
    }
    
    @Override
    public JMXReport[] retrievePendingReports() throws Exception {
        return JMXReport.wrap(Orchestration.getInstance().getPendingReports());
    }

    @Override
    public void flushPending(String uuid, String reportType) {
        Orchestration.getInstance().flushPending(uuid, reportType);
    }

    @Override
    public void flushPending(String uuid) {
        Orchestration.getInstance().flushPending(uuid);
    }

}
