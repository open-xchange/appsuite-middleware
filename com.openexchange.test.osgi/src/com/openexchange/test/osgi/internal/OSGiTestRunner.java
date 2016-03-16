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

package com.openexchange.test.osgi.internal;

import java.io.File;
import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.schmant.task.junit4.launcher.AntXmlRunListener;
import com.openexchange.test.osgi.OSGiTest;


/**
 * {@link OSGiTestRunner}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class OSGiTestRunner extends Thread {

    private final OSGiTest test;


    public OSGiTestRunner(OSGiTest test) {
        super();
        this.test = test;
    }

    @Override
    public void run() {
        if (test == null) {
            System.out.println("No test classes found.");
            exitOsgi();
            return;
        }
        Class<?>[] testClasses = test.getTestClasses();
        if (testClasses == null) {
            System.out.println("No tests  found.");
            exitOsgi();
            return;
        }

        for (Class<?> clazz : testClasses) {
            String className = clazz.getName();
            System.out.println("Running test " + className);
            TextListener listener = new TextListener(System.out);
            String resultDir = System.getProperty("com.openexchange.test.osgi.resultDir", "/tmp");
            System.setProperty("org.schmant.task.junit4.target", resultDir + File.separatorChar + "TEST-" + className + ".xml");
            AntXmlRunListener xmlListener = new AntXmlRunListener();
            JUnitCore core = new JUnitCore();
            core.addListener(listener);
            core.addListener(xmlListener);
            Result result = core.run(clazz);
            int successful = result.getRunCount() - result.getFailureCount();
            System.out.println("Successful: " + successful + " (" + result.getRunCount() + ").");
        }

        exitOsgi();
    }

    private void exitOsgi() {
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {

        }

        System.exit(0);
    }

}
