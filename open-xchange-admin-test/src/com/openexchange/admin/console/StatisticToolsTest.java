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

package com.openexchange.admin.console;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.openexchange.admin.rmi.AbstractRMITest;

/**
 * @author d7
 */
public class StatisticToolsTest extends AbstractRMITest {

    private int returnCodeXchange;

    private int returnCodeAll;

    private int returnCodeThreadpool;

    private int returnCodeRuntime;

    private int returnCodeOs;

    private int returnCodeThreading;

    private int returnCodeShowOperations;

    private int returnCodeMemory;

    private int returnCodeGc;

    private int returnCodeMemoryFull;

    private int returnCodeDocumentconverter;

    private int returnCodeOffice;

    private int returnEventAdmin;

    private int returnCache;

    private int returnCluster;

    private int returnGeneral;

    private int returnMailinterface;

    private int returnPooling;

    private int returnCallMonitor;

    private int returnMisc;

    private int returnOverview;

    private int returnMemoryPool;

    @Test
    public void testGetXchangeStats() {
        final StatisticTools statisticTools = new StatisticTools() {

            @Override
            protected void sysexit(int exitCode) {
                StatisticToolsTest.this.returnCodeXchange = exitCode;
            }
        };
        statisticTools.start(new String[] { "-x", "-H", getRMIHost() }, "showruntimestats");
        assertEquals("Expected 0 as return code!", 0, this.returnCodeXchange);
    }

    @Test
    public void testGetAllStats() {
        final StatisticTools statisticTools = new StatisticTools() {

            @Override
            protected void sysexit(int exitCode) {
                StatisticToolsTest.this.returnCodeAll = exitCode;
            }
        };
        statisticTools.start(new String[] { "-a", "-H", getRMIHost() }, "showruntimestats");
        assertEquals("Expected 0 as return code!", 0, this.returnCodeAll);
    }

    @Test
    public void testGetThreadpoolstats() {
        final StatisticTools statisticTools = new StatisticTools() {

            @Override
            protected void sysexit(int exitCode) {
                StatisticToolsTest.this.returnCodeThreadpool = exitCode;
            }
        };
        statisticTools.start(new String[] { "-p", "-H", getRMIHost() }, "showruntimestats");
        assertEquals("Expected 0 as return code!", 0, this.returnCodeThreadpool);
    }

    @Test
    public void testGetRuntimestats() {
        final StatisticTools statisticTools = new StatisticTools() {

            @Override
            protected void sysexit(int exitCode) {
                StatisticToolsTest.this.returnCodeRuntime = exitCode;
            }
        };
        statisticTools.start(new String[] { "-r", "-H", getRMIHost() }, "showruntimestats");
        assertEquals("Expected 0 as return code!", 0, this.returnCodeRuntime);
    }

    @Test
    public void testGetOsstats() {
        final StatisticTools statisticTools = new StatisticTools() {

            @Override
            protected void sysexit(int exitCode) {
                StatisticToolsTest.this.returnCodeOs = exitCode;
            }
        };
        statisticTools.start(new String[] { "-o", "-H", getRMIHost() }, "showruntimestats");
        assertEquals("Expected 0 as return code!", 0, this.returnCodeOs);
    }

    @Test
    public void testGetThreadingstats() {
        final StatisticTools statisticTools = new StatisticTools() {

            @Override
            protected void sysexit(int exitCode) {
                StatisticToolsTest.this.returnCodeThreading = exitCode;
            }
        };
        statisticTools.start(new String[] { "-t", "-H", getRMIHost() }, "showruntimestats");
        assertEquals("Expected 0 as return code!", 0, this.returnCodeThreading);
    }

    @Test
    public void testGetShowoperationsstats() {
        final StatisticTools statisticTools = new StatisticTools() {

            @Override
            protected void sysexit(int exitCode) {
                StatisticToolsTest.this.returnCodeShowOperations = exitCode;
            }
        };
        statisticTools.start(new String[] { "-s", "-H", getRMIHost() }, "showruntimestats");
        assertEquals("Expected 0 as return code!", 0, this.returnCodeShowOperations);
    }

    @Test
    public void testGetMemory() {
        final StatisticTools statisticTools = new StatisticTools() {

            @Override
            protected void sysexit(int exitCode) {
                StatisticToolsTest.this.returnCodeMemory = exitCode;
            }
        };
        statisticTools.start(new String[] { "-m", "-H", getRMIHost() }, "showruntimestats");
        assertEquals("Expected 0 as return code!", 0, this.returnCodeMemory);
    }

    @Test
    public void testGetGcstats() {
        final StatisticTools statisticTools = new StatisticTools() {

            @Override
            protected void sysexit(int exitCode) {
                StatisticToolsTest.this.returnCodeGc = exitCode;
            }
        };
        statisticTools.start(new String[] { "-z", "-H", getRMIHost() }, "showruntimestats");
        assertEquals("Expected 0 as return code!", 0, this.returnCodeGc);
    }

    @Test
    public void testGetMemoryFullstats() {
        final StatisticTools statisticTools = new StatisticTools() {

            @Override
            protected void sysexit(int exitCode) {
                StatisticToolsTest.this.returnCodeMemoryFull = exitCode;
            }
        };
        statisticTools.start(new String[] { "-M", "-H", getRMIHost() }, "showruntimestats");
        assertEquals("Expected 0 as return code!", 0, this.returnCodeMemoryFull);
    }

    @Test
    public void testGetDocumentconverterstats() {
        final StatisticTools statisticTools = new StatisticTools() {

            @Override
            protected void sysexit(int exitCode) {
                StatisticToolsTest.this.returnCodeDocumentconverter = exitCode;
            }
        };
        statisticTools.start(new String[] { "-y", "-H", getRMIHost() }, "showruntimestats");
        assertEquals("Expected 0 as return code!", 0, this.returnCodeDocumentconverter);
    }

    @Test
    public void testGetOfficestats() {
        final StatisticTools statisticTools = new StatisticTools() {

            @Override
            protected void sysexit(int exitCode) {
                StatisticToolsTest.this.returnCodeOffice = exitCode;
            }
        };
        statisticTools.start(new String[] { "-f", "-H", getRMIHost() }, "showruntimestats");
        assertEquals("Expected 0 as return code!", 0, this.returnCodeOffice);
    }

    @Test
    public void testEventAdminStats() {
        final StatisticTools statisticTools = new StatisticTools() {

            @Override
            protected void sysexit(int exitCode) {
                StatisticToolsTest.this.returnEventAdmin = exitCode;
            }
        };
        statisticTools.start(new String[] { "-e", "-H", getRMIHost() }, "showruntimestats");
        assertEquals("Expected 0 as return code!", 0, this.returnEventAdmin);
    }

    @Test
    public void testCachestats() {
        final StatisticTools statisticTools = new StatisticTools() {

            @Override
            protected void sysexit(int exitCode) {
                StatisticToolsTest.this.returnCache = exitCode;
            }
        };
        statisticTools.start(new String[] { "-j", "-H", getRMIHost() }, "showruntimestats");
        assertEquals("Expected 0 as return code!", 0, this.returnCache);
    }

    @Test
    public void testClusterstats() {
        final StatisticTools statisticTools = new StatisticTools() {

            @Override
            protected void sysexit(int exitCode) {
                StatisticToolsTest.this.returnCluster = exitCode;
            }
        };
        statisticTools.start(new String[] { "-c", "-H", getRMIHost() }, "showruntimestats");
        assertEquals("Expected 0 as return code!", 0, this.returnCluster);
    }

    @Test
    public void testGeneralstats() {
        final StatisticTools statisticTools = new StatisticTools() {

            @Override
            protected void sysexit(int exitCode) {
                StatisticToolsTest.this.returnGeneral = exitCode;
            }
        };
        statisticTools.start(new String[] { "--generalstats", "-H", getRMIHost() }, "showruntimestats");
        assertEquals("Expected 0 as return code!", 0, this.returnGeneral);
    }

    @Test
    public void testMailinterfacestats() {
        final StatisticTools statisticTools = new StatisticTools() {

            @Override
            protected void sysexit(int exitCode) {
                StatisticToolsTest.this.returnMailinterface = exitCode;
            }
        };
        statisticTools.start(new String[] { "--mailinterfacestats", "-H", getRMIHost() }, "showruntimestats");
        assertEquals("Expected 0 as return code!", 0, this.returnMailinterface);
    }

    @Test
    public void testPoolingstats() {
        final StatisticTools statisticTools = new StatisticTools() {

            @Override
            protected void sysexit(int exitCode) {
                StatisticToolsTest.this.returnPooling = exitCode;
            }
        };
        statisticTools.start(new String[] { "--poolingstats", "-H", getRMIHost() }, "showruntimestats");
        assertEquals("Expected 0 as return code!", 0, this.returnPooling);
    }

    @Test
    public void testCallMonitorstats() {
        final StatisticTools statisticTools = new StatisticTools() {

            @Override
            protected void sysexit(int exitCode) {
                StatisticToolsTest.this.returnCallMonitor = exitCode;
            }
        };
        statisticTools.start(new String[] { "--callmonitorstats", "-H", getRMIHost() }, "showruntimestats");
        assertEquals("Expected 0 as return code!", 0, this.returnCallMonitor);
    }

    @Test
    public void testMiscstats() {
        final StatisticTools statisticTools = new StatisticTools() {

            @Override
            protected void sysexit(int exitCode) {
                StatisticToolsTest.this.returnMisc = exitCode;
            }
        };
        statisticTools.start(new String[] { "--misc", "-H", getRMIHost() }, "showruntimestats");
        assertEquals("Expected 0 as return code!", 0, this.returnMisc);
    }

    @Test
    public void testOverviewstats() {
        final StatisticTools statisticTools = new StatisticTools() {

            @Override
            protected void sysexit(int exitCode) {
                StatisticToolsTest.this.returnOverview = exitCode;
            }
        };
        statisticTools.start(new String[] { "--overview", "-H", getRMIHost() }, "showruntimestats");
        assertEquals("Expected 0 as return code!", 0, this.returnOverview);
    }

    @Test
    public void testMemoryPoolstats() {
        final StatisticTools statisticTools = new StatisticTools() {

            @Override
            protected void sysexit(int exitCode) {
                StatisticToolsTest.this.returnMemoryPool = exitCode;
            }
        };
        statisticTools.start(new String[] { "--memorypool", "-H", getRMIHost() }, "showruntimestats");
        assertEquals("Expected 0 as return code!", 0, this.returnMemoryPool);
    }
}