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

package com.openexchange.exception.interception;

import java.util.LinkedList;
import java.util.List;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.exception.interception.internal.OXExceptionInterceptorRegistration;
import com.openexchange.test.mock.MockUtils;


/**
 * {@link OXExceptionInterceptorRegistrationTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class OXExceptionInterceptorRegistrationTest extends TestCase {

    /**
     * @throws java.lang.Exception
     */
    @Override
    public void setUp() throws Exception {
        OXExceptionInterceptorRegistration.initInstance();
    }

    @Test
    public void testIsResponsibleInterceptorRegistered_completelySameInterceptor_returnFalse() {
        // PREPARATION
        List<OXExceptionInterceptor> interceptors = new LinkedList<OXExceptionInterceptor>();
        AbstractOXExceptionInterceptor oxExceptionInterceptor = new AbstractOXExceptionInterceptor(1) {

            @Override
            public OXExceptionArguments intercept(OXException oxException) {
                return null;
            }
        };
        oxExceptionInterceptor.addResponsibility(new Responsibility("module", "action"));
        interceptors.add(oxExceptionInterceptor);
        MockUtils.injectValueIntoPrivateField(OXExceptionInterceptorRegistration.getInstance(), "interceptors", interceptors);

        // TEST-PREPARATION
        AbstractOXExceptionInterceptor testInterceptor = new AbstractOXExceptionInterceptor(1) {

            @Override
            public OXExceptionArguments intercept(OXException oxException) {
                return null;
            }
        };
        testInterceptor.addResponsibility(new Responsibility("module", "action"));

        boolean responsibleInterceptorRegistered = OXExceptionInterceptorRegistration.getInstance().isResponsibleInterceptorRegistered(testInterceptor);

        Assert.assertTrue("A responsible interceptor might already be registered but cannot be found", responsibleInterceptorRegistered);
    }

    @Test
    public void testIsResponsibleInterceptorRegistered_differentRanking_returnFalse() {
        // PREPARATION
        List<OXExceptionInterceptor> interceptors = new LinkedList<OXExceptionInterceptor>();
        AbstractOXExceptionInterceptor oxExceptionInterceptor = new AbstractOXExceptionInterceptor(1) {

            @Override
            public OXExceptionArguments intercept(OXException oxException) {
                return null;
            }
        };
        oxExceptionInterceptor.addResponsibility(new Responsibility("module", "action"));
        interceptors.add(oxExceptionInterceptor);
        MockUtils.injectValueIntoPrivateField(OXExceptionInterceptorRegistration.getInstance(), "interceptors", interceptors);

        // TEST-PREPARATION
        AbstractOXExceptionInterceptor testInterceptor = new AbstractOXExceptionInterceptor(11) {

            @Override
            public OXExceptionArguments intercept(OXException oxException) {
                return null;
            }
        };
        testInterceptor.addResponsibility(new Responsibility("module", "action"));

        boolean responsibleInterceptorRegistered = OXExceptionInterceptorRegistration.getInstance().isResponsibleInterceptorRegistered(testInterceptor);

        Assert.assertFalse("A responsible interceptor might NOT be registered but can be found", responsibleInterceptorRegistered);
    }

    @Test
    public void testIsResponsibleInterceptorRegistered_compareManyInterceptorsWithManyResponsibilities_returnFalse() {
        // PREPARATION
        List<OXExceptionInterceptor> interceptors = new LinkedList<OXExceptionInterceptor>();
        AbstractOXExceptionInterceptor oxExceptionInterceptor = new AbstractOXExceptionInterceptor(1) {

            @Override
            public OXExceptionArguments intercept(OXException oxException) {
                return null;
            }
        };
        oxExceptionInterceptor.addResponsibility(new Responsibility("module", "action"));
        interceptors.add(oxExceptionInterceptor);

        AbstractOXExceptionInterceptor registeredInterceptor1 = new AbstractOXExceptionInterceptor(1) {
            @Override
            public OXExceptionArguments intercept(OXException oxException) {
                return null;
            }
        };
        registeredInterceptor1.addResponsibility(new Responsibility("testModul", "textAction"));
        registeredInterceptor1.addResponsibility(new Responsibility("testModul1", "textAction1"));
        registeredInterceptor1.addResponsibility(new Responsibility("testModul2", "textAction2"));
        AbstractOXExceptionInterceptor registeredInterceptor2 = new AbstractOXExceptionInterceptor(2) {
            @Override
            public OXExceptionArguments intercept(OXException oxException) {
                return null;
            }
        };
        registeredInterceptor2.addResponsibility(new Responsibility("testModul", "textAction"));
        registeredInterceptor2.addResponsibility(new Responsibility("testModul1", "textAction1"));
        registeredInterceptor2.addResponsibility(new Responsibility("testModul2", "textAction2"));
        AbstractOXExceptionInterceptor registeredInterceptor3 = new AbstractOXExceptionInterceptor(3) {
            @Override
            public OXExceptionArguments intercept(OXException oxException) {
                return null;
            }
        };
        registeredInterceptor3.addResponsibility(new Responsibility("testModul", "textAction"));
        registeredInterceptor3.addResponsibility(new Responsibility("testModul1", "textAction1"));
        registeredInterceptor3.addResponsibility(new Responsibility("testModul2", "textAction2"));
        interceptors.add(registeredInterceptor1);
        interceptors.add(registeredInterceptor2);
        interceptors.add(registeredInterceptor3);

        MockUtils.injectValueIntoPrivateField(OXExceptionInterceptorRegistration.getInstance(), "interceptors", interceptors);

        // TEST-PREPARATION
        AbstractOXExceptionInterceptor testInterceptor = new AbstractOXExceptionInterceptor(1) {

            @Override
            public OXExceptionArguments intercept(OXException oxException) {
                return null;
            }
        };
        testInterceptor.addResponsibility(new Responsibility("moduleNotAvailable", "actionNotAvailable"));

        boolean responsibleInterceptorRegistered = OXExceptionInterceptorRegistration.getInstance().isResponsibleInterceptorRegistered(testInterceptor);

        Assert.assertFalse("A responsible interceptor might NOT be registered but can be found", responsibleInterceptorRegistered);
    }

    @Test
    public void testIsResponsibleInterceptorRegistered_compareManyInterceptorsWithManyResponsibilitiesWithTestInterceptorWithManyResponsibilities_returnFalse() {
        // PREPARATION
        List<OXExceptionInterceptor> interceptors = new LinkedList<OXExceptionInterceptor>();
        AbstractOXExceptionInterceptor oxExceptionInterceptor = new AbstractOXExceptionInterceptor(1) {

            @Override
            public OXExceptionArguments intercept(OXException oxException) {
                return null;
            }
        };
        oxExceptionInterceptor.addResponsibility(new Responsibility("module", "action"));
        interceptors.add(oxExceptionInterceptor);

        AbstractOXExceptionInterceptor registeredInterceptor1 = new AbstractOXExceptionInterceptor(1) {

            @Override
            public OXExceptionArguments intercept(OXException oxException) {
                return null;
            }
        };
        registeredInterceptor1.addResponsibility(new Responsibility("testModul", "textAction"));
        registeredInterceptor1.addResponsibility(new Responsibility("testModul1", "textAction1"));
        registeredInterceptor1.addResponsibility(new Responsibility("testModul2", "textAction2"));
        AbstractOXExceptionInterceptor registeredInterceptor2 = new AbstractOXExceptionInterceptor(2) {

            @Override
            public OXExceptionArguments intercept(OXException oxException) {
                return null;
            }
        };
        registeredInterceptor2.addResponsibility(new Responsibility("testModul", "textAction"));
        registeredInterceptor2.addResponsibility(new Responsibility("testModul1", "textAction1"));
        registeredInterceptor2.addResponsibility(new Responsibility("testModul2", "textAction2"));
        AbstractOXExceptionInterceptor registeredInterceptor3 = new AbstractOXExceptionInterceptor(3) {

            @Override
            public OXExceptionArguments intercept(OXException oxException) {
                return null;
            }
        };
        registeredInterceptor3.addResponsibility(new Responsibility("testModul", "textAction"));
        registeredInterceptor3.addResponsibility(new Responsibility("testModul1", "textAction1"));
        registeredInterceptor3.addResponsibility(new Responsibility("testModul2", "textAction2"));

        interceptors.add(registeredInterceptor1);
        interceptors.add(registeredInterceptor2);
        interceptors.add(registeredInterceptor3);
        MockUtils.injectValueIntoPrivateField(OXExceptionInterceptorRegistration.getInstance(), "interceptors", interceptors);

        // TEST-PREPARATION
        AbstractOXExceptionInterceptor testInterceptor = new AbstractOXExceptionInterceptor(1) {

            @Override
            public OXExceptionArguments intercept(OXException oxException) {
                return null;
            }
        };
        testInterceptor.addResponsibility(new Responsibility("moduleNotAvailable", "actionNotAvailable"));
        testInterceptor.addResponsibility(new Responsibility("moduleNotAvailable1", "actionNotAvailable1"));
        testInterceptor.addResponsibility(new Responsibility("moduleNotAvailable2", "actionNotAvailable2"));
        testInterceptor.addResponsibility(new Responsibility("moduleNotAvailable3", "actionNotAvailable3"));

        boolean responsibleInterceptorRegistered = OXExceptionInterceptorRegistration.getInstance().isResponsibleInterceptorRegistered(testInterceptor);

        Assert.assertFalse("A responsible interceptor might NOT be registered but can be found", responsibleInterceptorRegistered);
    }

    @Test
    public void testIsResponsibleInterceptorRegistered_sameResponsibilitiesButdifferentRanking_returnFalse() {
        // PREPARATION
        List<OXExceptionInterceptor> interceptors = new LinkedList<OXExceptionInterceptor>();
        AbstractOXExceptionInterceptor oxExceptionInterceptor = new AbstractOXExceptionInterceptor(1) {

            @Override
            public OXExceptionArguments intercept(OXException oxException) {
                return null;
            }
        };
        oxExceptionInterceptor.addResponsibility(new Responsibility("module", "action"));
        interceptors.add(oxExceptionInterceptor);

        AbstractOXExceptionInterceptor registeredInterceptor1 = new AbstractOXExceptionInterceptor(1) {

            @Override
            public OXExceptionArguments intercept(OXException oxException) {
                return null;
            }
        };
        registeredInterceptor1.addResponsibility(new Responsibility("testModul", "textAction"));
        registeredInterceptor1.addResponsibility(new Responsibility("testModul1", "textAction1"));
        registeredInterceptor1.addResponsibility(new Responsibility("testModul2", "textAction2"));
        AbstractOXExceptionInterceptor registeredInterceptor2 = new AbstractOXExceptionInterceptor(2) {

            @Override
            public OXExceptionArguments intercept(OXException oxException) {
                return null;
            }
        };
        registeredInterceptor2.addResponsibility(new Responsibility("testModul", "textAction"));
        registeredInterceptor2.addResponsibility(new Responsibility("testModul1", "textAction1"));
        registeredInterceptor2.addResponsibility(new Responsibility("testModul2", "textAction2"));
        AbstractOXExceptionInterceptor registeredInterceptor3 = new AbstractOXExceptionInterceptor(3) {

            @Override
            public OXExceptionArguments intercept(OXException oxException) {
                return null;
            }
        };
        registeredInterceptor3.addResponsibility(new Responsibility("testModul", "textAction"));
        registeredInterceptor3.addResponsibility(new Responsibility("testModul1", "textAction1"));
        registeredInterceptor3.addResponsibility(new Responsibility("module", "action"));

        interceptors.add(registeredInterceptor1);
        interceptors.add(registeredInterceptor2);
        interceptors.add(registeredInterceptor3);
        MockUtils.injectValueIntoPrivateField(OXExceptionInterceptorRegistration.getInstance(), "interceptors", interceptors);

        // TEST-PREPARATION
        AbstractOXExceptionInterceptor testInterceptor = new AbstractOXExceptionInterceptor(3) {

            @Override
            public OXExceptionArguments intercept(OXException oxException) {
                return null;
            }
        };
        testInterceptor.addResponsibility(new Responsibility("moduleNotAvailable", "actionNotAvailable"));
        testInterceptor.addResponsibility(new Responsibility("moduleNotAvailable1", "actionNotAvailable1"));
        testInterceptor.addResponsibility(new Responsibility("moduleNotAvailable2", "actionNotAvailable2"));
        testInterceptor.addResponsibility(new Responsibility("module", "action"));

        boolean responsibleInterceptorRegistered = OXExceptionInterceptorRegistration.getInstance().isResponsibleInterceptorRegistered(testInterceptor);

        Assert.assertTrue("A responsible interceptor might already be registered but cannot be found", responsibleInterceptorRegistered);
    }

    @Test
    public void testGetResponsibleInterceptors_noResponsibleRegisteredBecauseNoOneRegistered_returnEmptyList() {
        List<OXExceptionInterceptor> responsibleInterceptors = OXExceptionInterceptorRegistration.getInstance().getResponsibleInterceptors("moduleNotAvailable", "actionNotAvailable");

        Assert.assertEquals("Wrong number of responsible interceptor found", 0, responsibleInterceptors.size());
    }

    @Test
    public void testGetResponsibleInterceptors_noResponsibleRegistered_returnEmptyList() {
        // PREPARATION
        List<OXExceptionInterceptor> interceptors = new LinkedList<OXExceptionInterceptor>();
        AbstractOXExceptionInterceptor oxExceptionInterceptor = new AbstractOXExceptionInterceptor(1) {

            @Override
            public OXExceptionArguments intercept(OXException oxException) {
                return null;
            }
        };
        oxExceptionInterceptor.addResponsibility(new Responsibility("module", "action"));
        oxExceptionInterceptor.addResponsibility(new Responsibility("module1", "action1"));
        oxExceptionInterceptor.addResponsibility(new Responsibility("module2", "action2"));
        interceptors.add(oxExceptionInterceptor);
        MockUtils.injectValueIntoPrivateField(OXExceptionInterceptorRegistration.getInstance(), "interceptors", interceptors);

        // TEST-PREPARATION
        List<OXExceptionInterceptor> responsibleInterceptors = OXExceptionInterceptorRegistration.getInstance().getResponsibleInterceptors("moduleNotAvailable", "actionNotAvailable");

        Assert.assertEquals("Wrong number of responsible interceptor found", 0, responsibleInterceptors.size());
    }

    @Test
    public void testGetResponsibleInterceptors_oneResponsibleRegistered_returnOne() {
        // PREPARATION
        List<OXExceptionInterceptor> interceptors = new LinkedList<OXExceptionInterceptor>();
        AbstractOXExceptionInterceptor oxExceptionInterceptor = new AbstractOXExceptionInterceptor(1) {

            @Override
            public OXExceptionArguments intercept(OXException oxException) {
                return null;
            }
        };
        oxExceptionInterceptor.addResponsibility(new Responsibility("module", "action"));
        oxExceptionInterceptor.addResponsibility(new Responsibility("module1", "action1"));
        oxExceptionInterceptor.addResponsibility(new Responsibility("module2", "action2"));
        interceptors.add(oxExceptionInterceptor);
        MockUtils.injectValueIntoPrivateField(OXExceptionInterceptorRegistration.getInstance(), "interceptors", interceptors);

        // TEST-PREPARATION
        List<OXExceptionInterceptor> responsibleInterceptors = OXExceptionInterceptorRegistration.getInstance().getResponsibleInterceptors("module2", "action2");

        Assert.assertEquals("Wrong number of responsible interceptor found", 1, responsibleInterceptors.size());
    }

    @Test
    public void testGetResponsibleInterceptors_wrongAction_returnEmptyList() {
        // PREPARATION
        List<OXExceptionInterceptor> interceptors = new LinkedList<OXExceptionInterceptor>();
        AbstractOXExceptionInterceptor oxExceptionInterceptor = new AbstractOXExceptionInterceptor(1) {

            @Override
            public OXExceptionArguments intercept(OXException oxException) {
                return null;
            }
        };
        oxExceptionInterceptor.addResponsibility(new Responsibility("module", "action"));
        oxExceptionInterceptor.addResponsibility(new Responsibility("module1", "action1"));
        oxExceptionInterceptor.addResponsibility(new Responsibility("module2", "action2"));
        interceptors.add(oxExceptionInterceptor);
        MockUtils.injectValueIntoPrivateField(OXExceptionInterceptorRegistration.getInstance(), "interceptors", interceptors);

        // TEST-PREPARATION
        List<OXExceptionInterceptor> responsibleInterceptors = OXExceptionInterceptorRegistration.getInstance().getResponsibleInterceptors("module2", "action7");

        Assert.assertEquals("Wrong number of responsible interceptor found", 0, responsibleInterceptors.size());
    }

    @Test
    public void testGetResponsibleInterceptors_wrongModule_returnEmptyList() {
        // PREPARATION
        List<OXExceptionInterceptor> interceptors = new LinkedList<OXExceptionInterceptor>();
        AbstractOXExceptionInterceptor oxExceptionInterceptor = new AbstractOXExceptionInterceptor(1) {

            @Override
            public OXExceptionArguments intercept(OXException oxException) {
                return null;
            }
        };
        oxExceptionInterceptor.addResponsibility(new Responsibility("module", "action"));
        oxExceptionInterceptor.addResponsibility(new Responsibility("module1", "action1"));
        oxExceptionInterceptor.addResponsibility(new Responsibility("module2", "action2"));
        interceptors.add(oxExceptionInterceptor);
        MockUtils.injectValueIntoPrivateField(OXExceptionInterceptorRegistration.getInstance(), "interceptors", interceptors);

        // TEST-PREPARATION
        List<OXExceptionInterceptor> responsibleInterceptors = OXExceptionInterceptorRegistration.getInstance().getResponsibleInterceptors("module8", "action1");

        Assert.assertEquals("Wrong number of responsible interceptor found", 0, responsibleInterceptors.size());
    }

    @Test
    public void testGetResponsibleInterceptors_twoResponsibleRegistered_returnTwo() {
        // PREPARATION
        List<OXExceptionInterceptor> interceptors = new LinkedList<OXExceptionInterceptor>();
        AbstractOXExceptionInterceptor oxExceptionInterceptor = new AbstractOXExceptionInterceptor(1) {

            @Override
            public OXExceptionArguments intercept(OXException oxException) {
                return null;
            }
        };
        oxExceptionInterceptor.addResponsibility(new Responsibility("module", "action"));
        oxExceptionInterceptor.addResponsibility(new Responsibility("module1", "action1"));
        oxExceptionInterceptor.addResponsibility(new Responsibility("module2", "action2"));
        interceptors.add(oxExceptionInterceptor);

        AbstractOXExceptionInterceptor oxExceptionInterceptor1 = new AbstractOXExceptionInterceptor(1) {

            @Override
            public OXExceptionArguments intercept(OXException oxException) {
                return null;
            }
        };
        oxExceptionInterceptor1.addResponsibility(new Responsibility("module7", "action7"));
        oxExceptionInterceptor1.addResponsibility(new Responsibility("module1", "action1"));
        oxExceptionInterceptor1.addResponsibility(new Responsibility("module8", "action8"));
        interceptors.add(oxExceptionInterceptor1);

        MockUtils.injectValueIntoPrivateField(OXExceptionInterceptorRegistration.getInstance(), "interceptors", interceptors);

        // TEST-PREPARATION
        List<OXExceptionInterceptor> responsibleInterceptors = OXExceptionInterceptorRegistration.getInstance().getResponsibleInterceptors("module1", "action1");

        Assert.assertEquals("Wrong number of responsible interceptor found", 2, responsibleInterceptors.size());
    }

    @Test
    public void testGetResponsibleInterceptors_ThreeRegisteredTwoResponsible_returnTwo() {
        // PREPARATION
        List<OXExceptionInterceptor> interceptors = new LinkedList<OXExceptionInterceptor>();
        AbstractOXExceptionInterceptor oxExceptionInterceptor = new AbstractOXExceptionInterceptor(1) {

            @Override
            public OXExceptionArguments intercept(OXException oxException) {
                return null;
            }
        };
        oxExceptionInterceptor.addResponsibility(new Responsibility("module", "action"));
        oxExceptionInterceptor.addResponsibility(new Responsibility("module11", "action12"));
        oxExceptionInterceptor.addResponsibility(new Responsibility("module2", "action2"));
        interceptors.add(oxExceptionInterceptor);

        AbstractOXExceptionInterceptor oxExceptionInterceptor1 = new AbstractOXExceptionInterceptor(1) {

            @Override
            public OXExceptionArguments intercept(OXException oxException) {
                return null;
            }
        };
        oxExceptionInterceptor1.addResponsibility(new Responsibility("module7", "action7"));
        oxExceptionInterceptor1.addResponsibility(new Responsibility("module1", "action1"));
        oxExceptionInterceptor1.addResponsibility(new Responsibility("module8", "action8"));
        interceptors.add(oxExceptionInterceptor1);

        AbstractOXExceptionInterceptor oxExceptionInterceptor2 = new AbstractOXExceptionInterceptor(1) {

            @Override
            public OXExceptionArguments intercept(OXException oxException) {
                return null;
            }
        };
        oxExceptionInterceptor2.addResponsibility(new Responsibility("module7", "action7"));
        oxExceptionInterceptor2.addResponsibility(new Responsibility("module1", "action1"));
        oxExceptionInterceptor2.addResponsibility(new Responsibility("module8", "action8"));
        interceptors.add(oxExceptionInterceptor2);

        MockUtils.injectValueIntoPrivateField(OXExceptionInterceptorRegistration.getInstance(), "interceptors", interceptors);

        // TEST-PREPARATION
        List<OXExceptionInterceptor> responsibleInterceptors = OXExceptionInterceptorRegistration.getInstance().getResponsibleInterceptors("module1", "action1");

        Assert.assertEquals("Wrong number of responsible interceptor found", 2, responsibleInterceptors.size());
    }


    @Test
    public void testGetResponsibleInterceptors_fourRegistered_returnInCorrectOrder() {
        // PREPARATION
        List<OXExceptionInterceptor> interceptors = new LinkedList<OXExceptionInterceptor>();
        AbstractOXExceptionInterceptor oxExceptionInterceptor = new AbstractOXExceptionInterceptor(Integer.MIN_VALUE) {

            @Override
            public OXExceptionArguments intercept(OXException oxException) {
                return null;
            }
        };
        oxExceptionInterceptor.addResponsibility(new Responsibility("module", "action"));
        oxExceptionInterceptor.addResponsibility(new Responsibility("module11", "action12"));
        oxExceptionInterceptor.addResponsibility(new Responsibility("module2", "action2"));
        interceptors.add(oxExceptionInterceptor);

        AbstractOXExceptionInterceptor oxExceptionInterceptor1 = new AbstractOXExceptionInterceptor(Integer.MAX_VALUE) {

            @Override
            public OXExceptionArguments intercept(OXException oxException) {
                return null;
            }
        };
        oxExceptionInterceptor1.addResponsibility(new Responsibility("module7", "action7"));
        oxExceptionInterceptor1.addResponsibility(new Responsibility("module1", "action1"));
        oxExceptionInterceptor1.addResponsibility(new Responsibility("module8", "action8"));
        interceptors.add(oxExceptionInterceptor1);

        AbstractOXExceptionInterceptor oxExceptionInterceptor2 = new AbstractOXExceptionInterceptor(1111) {

            @Override
            public OXExceptionArguments intercept(OXException oxException) {
                return null;
            }
        };
        oxExceptionInterceptor2.addResponsibility(new Responsibility("module7", "action7"));
        oxExceptionInterceptor2.addResponsibility(new Responsibility("module1", "action1"));
        oxExceptionInterceptor2.addResponsibility(new Responsibility("module8", "action8"));
        interceptors.add(oxExceptionInterceptor2);

        AbstractOXExceptionInterceptor oxExceptionInterceptor3 = new AbstractOXExceptionInterceptor(444) {

            @Override
            public OXExceptionArguments intercept(OXException oxException) {
                return null;
            }
        };
        oxExceptionInterceptor3.addResponsibility(new Responsibility("module7", "action7"));
        oxExceptionInterceptor3.addResponsibility(new Responsibility("module1ignore", "action1ignore"));
        oxExceptionInterceptor3.addResponsibility(new Responsibility("module8", "action8"));
        interceptors.add(oxExceptionInterceptor3);

        AbstractOXExceptionInterceptor oxExceptionInterceptor4 = new AbstractOXExceptionInterceptor(55) {

            @Override
            public OXExceptionArguments intercept(OXException oxException) {
                return null;
            }
        };
        oxExceptionInterceptor4.addResponsibility(new Responsibility("module7", "action7"));
        oxExceptionInterceptor4.addResponsibility(new Responsibility("module1", "action1"));
        oxExceptionInterceptor4.addResponsibility(new Responsibility("module8", "action8"));
        interceptors.add(oxExceptionInterceptor4);

        AbstractOXExceptionInterceptor oxExceptionInterceptor5 = new AbstractOXExceptionInterceptor(333) {

            @Override
            public OXExceptionArguments intercept(OXException oxException) {
                return null;
            }
        };
        oxExceptionInterceptor5.addResponsibility(new Responsibility("module7", "action7"));
        oxExceptionInterceptor5.addResponsibility(new Responsibility("module1", "action1"));
        oxExceptionInterceptor5.addResponsibility(new Responsibility("module8", "action8"));
        interceptors.add(oxExceptionInterceptor5);

        MockUtils.injectValueIntoPrivateField(OXExceptionInterceptorRegistration.getInstance(), "interceptors", interceptors);

        // TEST-PREPARATION
        List<OXExceptionInterceptor> responsibleInterceptors = OXExceptionInterceptorRegistration.getInstance().getResponsibleInterceptors("module1", "action1");

        Assert.assertEquals("Wrong number of responsible interceptor found", 4, responsibleInterceptors.size());
        int lastRanking = Integer.MIN_VALUE;
        for (OXExceptionInterceptor interceptor : responsibleInterceptors) {
            Assert.assertTrue("Wrong order of interceptors!", lastRanking <= interceptor.getRanking());
            lastRanking = interceptor.getRanking();
        }
    }
}
