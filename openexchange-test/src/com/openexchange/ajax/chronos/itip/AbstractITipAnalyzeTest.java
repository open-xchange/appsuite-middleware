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

package com.openexchange.ajax.chronos.itip;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import com.openexchange.ajax.chronos.factory.ICalFacotry;
import com.openexchange.ajax.chronos.factory.ICalFacotry.Method;
import com.openexchange.ajax.chronos.factory.ICalFacotry.ProdID;
import com.openexchange.ajax.chronos.factory.ITipMailFactory;
import com.openexchange.junit.Assert;
import com.openexchange.testing.httpclient.models.Analysis;
import com.openexchange.testing.httpclient.models.Analysis.ActionsEnum;
import com.openexchange.testing.httpclient.models.AnalyzeResponse;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.testing.httpclient.models.MailDestinationData;

/**
 * {@link AbstractITipAnalyzeTest}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public abstract class AbstractITipAnalyzeTest extends AbstractITipTest {

    protected MailDestinationData mailData;

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        try {
            if (null != mailData) {
                removeMail(mailData);
            }
        } finally {
            super.tearDown();
        }
    }

    protected void analyze(ProdID id, Method method, List<EventData> events, Consumer<Analysis> analysisValidator, Map<String, Object> values) throws Exception {
        ICalFacotry iCalFacotry = new ICalFacotry(events);
        iCalFacotry.addProdID(id);
        iCalFacotry.addMethod(method);
        if (null != values) {
            iCalFacotry.addValueMap(values);
        }
        analyze(iCalFacotry, analysisValidator, values);
    }

    protected void analyze(ICalFacotry iCalFacotry, Consumer<Analysis> analysisValidator, Map<String, Object> values) throws Exception {
        String eml = new ITipMailFactory(userResponseC2.getData().getEmail1(), userResponseC1.getData().getEmail1(), iCalFacotry.build()).build();
        mailData = createMailInInbox(eml);
        Assert.assertThat("No mail created.", mailData.getId(), notNullValue());

        AnalyzeResponse response = analyze(constructBody(mailData.getId()));
        Assert.assertThat("Should have no error", response.getErrorId(), nullValue());

        List<Analysis> data = response.getData();
        Assert.assertThat("No Analyze", data, notNullValue());
        Assert.assertThat("Only one event should have been analyzed", Integer.valueOf(data.size()), is(Integer.valueOf(1)));

        if (null == analysisValidator) {
            analysisValidator = CustomConsumers.ACTIONS.getConsumer();
        }
        analysisValidator.accept(data.get(0));
    }

    public enum CustomConsumers {
        /** Validates that the response does contain actions to set the users status */
        ACTIONS((Analysis t) -> {
            Assert.assertTrue("Missing action!", t.getActions().contains(ActionsEnum.ACCEPT));
            Assert.assertTrue("Missing action!", t.getActions().contains(ActionsEnum.TENTATIVE));
            Assert.assertTrue("Missing action!", t.getActions().contains(ActionsEnum.DECLINE));

            Assert.assertFalse("Unwanted action!", t.getActions().contains(ActionsEnum.UPDATE));
        }),
        /** Validates that the response does contain the UPDATE action */
        UPDATE((Analysis t) -> {
            Assert.assertFalse("Unwanted action!", t.getActions().contains(ActionsEnum.ACCEPT));
            Assert.assertFalse("Unwanted action!", t.getActions().contains(ActionsEnum.TENTATIVE));
            Assert.assertFalse("Unwanted action!", t.getActions().contains(ActionsEnum.DECLINE));

            Assert.assertTrue("Missing action!", t.getActions().contains(ActionsEnum.UPDATE));
        }),
        /** Validates that the response doesn't contain any action */
        EMPTY((Analysis t) -> {
            Assert.assertTrue("There should be no action!", t.getActions().isEmpty());
        }),

        ;

        private Consumer<Analysis> consumer;

        CustomConsumers(Consumer<Analysis> consumer) {
            this.consumer = consumer;
        }

        public Consumer<Analysis> getConsumer() {
            return consumer;
        }

    }
}
