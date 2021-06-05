/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.requesthandler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link DefaultDispatcherTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
@SuppressWarnings({ "synthetic-access", "unused" })
public class DefaultDispatcherTest {

    @Test
    public void testDispatchesToActionService() throws OXException {
        final DefaultDispatcher dispatcher = new DefaultDispatcher();

        final AJAXRequestResult res = new AJAXRequestResult();

        final StaticActionService action = new StaticActionService(res);
        final StaticActionFactory factory = new StaticActionFactory(action);

        dispatcher.register("someModule", factory);

        final AJAXRequestData requestData = new AJAXRequestData();
        requestData.setModule("someModule");
        requestData.setAction("someAction");

        final AJAXRequestResult receivedResult = dispatcher.perform(requestData, null, null);

        assertSame(res, receivedResult);
    }

    @Test
    public void testChain() throws OXException {
        final DefaultDispatcher dispatcher = new DefaultDispatcher();

        final AJAXRequestResult res = new AJAXRequestResult();

        final StaticActionService action = new StaticActionService(res);
        final StaticActionFactory factory = new StaticActionFactory(action);

        final SimAJAXCustomizer c1 = new SimAJAXCustomizer("c1");
        final SimAJAXCustomizer c2 = new SimAJAXCustomizer("c2");
        final SimAJAXCustomizer c3 = new SimAJAXCustomizer("c3");

        dispatcher.addCustomizer(new StaticAJAXCustomizerFactory(c1));
        dispatcher.addCustomizer(new StaticAJAXCustomizerFactory(c2));
        dispatcher.addCustomizer(new StaticAJAXCustomizerFactory(c3));

        dispatcher.register("someModule", factory);

        final AJAXRequestData requestData = new AJAXRequestData();
        requestData.setModule("someModule");
        requestData.setAction("someAction");

        SimAJAXCustomizer.LOG.clear();

        final AJAXRequestResult receivedResult = dispatcher.perform(requestData, null, null);

        assertSame(res, receivedResult);

        assertSame(requestData, c1.getRequest());
        assertSame(requestData, c2.getRequest());
        assertSame(requestData, c3.getRequest());

        assertSame(res, c1.getResult());
        assertSame(res, c2.getResult());
        assertSame(res, c3.getResult());

        assertEquals(SimAJAXCustomizer.LOG, Arrays.asList("c1:incoming", "c2:incoming", "c3:incoming", "c3:outgoing", "c2:outgoing", "c1:outgoing"));
    }

    @Test
    public void testLaterIncoming() throws OXException {
        final DefaultDispatcher dispatcher = new DefaultDispatcher();

        final AJAXRequestResult res = new AJAXRequestResult();

        final StaticActionService action = new StaticActionService(res);
        final StaticActionFactory factory = new StaticActionFactory(action);

        final SimAJAXCustomizer c1 = new SimAJAXCustomizer("c1") {

            private boolean skipped = false;

            @Override
            public AJAXRequestData incoming(final AJAXRequestData requestData, final ServerSession session) throws OXException {
                if (!skipped) {
                    skipped = true;
                    throw new FlowControl.Later();
                }
                return super.incoming(requestData, session);
            }
        };
        final SimAJAXCustomizer c2 = new SimAJAXCustomizer("c2");
        final SimAJAXCustomizer c3 = new SimAJAXCustomizer("c3");

        dispatcher.addCustomizer(new StaticAJAXCustomizerFactory(c1));
        dispatcher.addCustomizer(new StaticAJAXCustomizerFactory(c2));
        dispatcher.addCustomizer(new StaticAJAXCustomizerFactory(c3));

        dispatcher.register("someModule", factory);

        final AJAXRequestData requestData = new AJAXRequestData();
        requestData.setModule("someModule");
        requestData.setAction("someAction");

        SimAJAXCustomizer.LOG.clear();

        dispatcher.perform(requestData, null, null);

        assertEquals(SimAJAXCustomizer.LOG, Arrays.asList("c2:incoming", "c3:incoming", "c1:incoming", "c1:outgoing", "c3:outgoing", "c2:outgoing"));
    }

    @Test
    public void testLaterIncomingTwice() throws OXException {
        final DefaultDispatcher dispatcher = new DefaultDispatcher();

        final AJAXRequestResult res = new AJAXRequestResult();

        final StaticActionService action = new StaticActionService(res);
        final StaticActionFactory factory = new StaticActionFactory(action);

        final SimAJAXCustomizer c1 = new SimAJAXCustomizer("c1") {

            private int skipCount = 0;

            @Override
            public AJAXRequestData incoming(final AJAXRequestData requestData, final ServerSession session) throws OXException {
                if (skipCount < 2) {
                    skipCount++;
                    throw new FlowControl.Later();
                }
                return super.incoming(requestData, session);
            }
        };
        final SimAJAXCustomizer c2 = new SimAJAXCustomizer("c2");
        final SimAJAXCustomizer c3 = new SimAJAXCustomizer("c3");

        dispatcher.addCustomizer(new StaticAJAXCustomizerFactory(c1));
        dispatcher.addCustomizer(new StaticAJAXCustomizerFactory(c2));
        dispatcher.addCustomizer(new StaticAJAXCustomizerFactory(c3));

        dispatcher.register("someModule", factory);

        final AJAXRequestData requestData = new AJAXRequestData();
        requestData.setModule("someModule");
        requestData.setAction("someAction");

        SimAJAXCustomizer.LOG.clear();

        dispatcher.perform(requestData, null, null);

        assertEquals(SimAJAXCustomizer.LOG, Arrays.asList("c2:incoming", "c3:incoming", "c1:incoming", "c1:outgoing", "c3:outgoing", "c2:outgoing"));
    }

    @Test
    public void testLaterOutgoing() throws OXException {
        final DefaultDispatcher dispatcher = new DefaultDispatcher();

        final AJAXRequestResult res = new AJAXRequestResult();

        final StaticActionService action = new StaticActionService(res);
        final StaticActionFactory factory = new StaticActionFactory(action);

        final SimAJAXCustomizer c1 = new SimAJAXCustomizer("c1");
        final SimAJAXCustomizer c2 = new SimAJAXCustomizer("c2");
        final SimAJAXCustomizer c3 = new SimAJAXCustomizer("c3") {

            private boolean skipped = false;

            @Override
            public AJAXRequestResult outgoing(final AJAXRequestData requestData, final AJAXRequestResult res, final ServerSession session) throws OXException {
                if (!skipped) {
                    skipped = true;
                    throw new FlowControl.Later();
                }
                return super.outgoing(requestData, res, session);
            }
        };

        dispatcher.addCustomizer(new StaticAJAXCustomizerFactory(c1));
        dispatcher.addCustomizer(new StaticAJAXCustomizerFactory(c2));
        dispatcher.addCustomizer(new StaticAJAXCustomizerFactory(c3));

        dispatcher.register("someModule", factory);

        final AJAXRequestData requestData = new AJAXRequestData();
        requestData.setModule("someModule");
        requestData.setAction("someAction");

        SimAJAXCustomizer.LOG.clear();

        dispatcher.perform(requestData, null, null);

        assertEquals(SimAJAXCustomizer.LOG, Arrays.asList("c1:incoming", "c2:incoming", "c3:incoming", "c2:outgoing", "c1:outgoing", "c3:outgoing"));
    }

    @Test
    public void testLaterOutgoingTwice() throws OXException {
        final DefaultDispatcher dispatcher = new DefaultDispatcher();

        final AJAXRequestResult res = new AJAXRequestResult();

        final StaticActionService action = new StaticActionService(res);
        final StaticActionFactory factory = new StaticActionFactory(action);

        final SimAJAXCustomizer c1 = new SimAJAXCustomizer("c1");
        final SimAJAXCustomizer c2 = new SimAJAXCustomizer("c2");
        final SimAJAXCustomizer c3 = new SimAJAXCustomizer("c3") {

            private int skipCount = 0;

            @Override
            public AJAXRequestResult outgoing(final AJAXRequestData requestData, final AJAXRequestResult res, final ServerSession session) throws OXException {
                if (skipCount < 2) {
                    skipCount++;
                    throw new FlowControl.Later();
                }
                return super.outgoing(requestData, res, session);
            }
        };

        dispatcher.addCustomizer(new StaticAJAXCustomizerFactory(c1));
        dispatcher.addCustomizer(new StaticAJAXCustomizerFactory(c2));
        dispatcher.addCustomizer(new StaticAJAXCustomizerFactory(c3));

        dispatcher.register("someModule", factory);

        final AJAXRequestData requestData = new AJAXRequestData();
        requestData.setModule("someModule");
        requestData.setAction("someAction");

        SimAJAXCustomizer.LOG.clear();

        dispatcher.perform(requestData, null, null);

        assertEquals(SimAJAXCustomizer.LOG, Arrays.asList("c1:incoming", "c2:incoming", "c3:incoming", "c2:outgoing", "c1:outgoing", "c3:outgoing"));
    }

    // Error Cases

    @Test
    public void testUnknownModule() {
        final DefaultDispatcher dispatcher = new DefaultDispatcher();
        final AJAXRequestData requestData = new AJAXRequestData();
        requestData.setModule("someModule");
        requestData.setAction("someAction");

        try {
            dispatcher.perform(requestData, null, null);
            fail("Should have produced an OXException");
        } catch (OXException x) {
            // All Done
        }

    }

    @Test
    public void testUnknownAction() {
        final DefaultDispatcher dispatcher = new DefaultDispatcher();

        final StaticActionFactory factory = new StaticActionFactory(null);

        dispatcher.register("someModule", factory);

        final AJAXRequestData requestData = new AJAXRequestData();
        requestData.setModule("someModule");
        requestData.setAction("someAction");

        try {
            dispatcher.perform(requestData, null, null);
            fail("Should have produced an OXException");
        } catch (OXException x) {
            // All Done
        }
    }

    @Test
    public void testNullCustomizer() throws OXException {
        final DefaultDispatcher dispatcher = new DefaultDispatcher();
        final AJAXRequestResult res = new AJAXRequestResult();

        final StaticActionService action = new StaticActionService(res);
        final StaticActionFactory factory = new StaticActionFactory(action);

        dispatcher.register("someModule", factory);

        dispatcher.addCustomizer(new StaticAJAXCustomizerFactory(null));

        final AJAXRequestData requestData = new AJAXRequestData();
        requestData.setModule("someModule");
        requestData.setAction("someAction");

        dispatcher.perform(requestData, null, null);
    }

    private static class SimAJAXCustomizer implements AJAXActionCustomizer {

        public static List<String> LOG = new LinkedList<String>();

        private AJAXRequestData request;
        private AJAXRequestResult result;
        private final String name;

        private SimAJAXCustomizer(final String name) {
            this.name = name;
        }

        @Override
        public AJAXRequestData incoming(final AJAXRequestData requestData, final ServerSession session) throws OXException {
            this.request = requestData;
            LOG.add(name + ":incoming");
            return requestData;
        }

        @Override
        public AJAXRequestResult outgoing(final AJAXRequestData requestData, final AJAXRequestResult result, final ServerSession session) throws OXException {
            this.result = result;
            LOG.add(name + ":outgoing");
            return result;
        }

        public AJAXRequestData getRequest() {
            return request;
        }

        public AJAXRequestResult getResult() {
            return result;
        }

    }

    private static final class StaticAJAXCustomizerFactory implements AJAXActionCustomizerFactory {

        private final AJAXActionCustomizer customizer;

        public StaticAJAXCustomizerFactory(final AJAXActionCustomizer customizer) {
            super();
            this.customizer = customizer;
        }

        @Override
        public AJAXActionCustomizer createCustomizer(final AJAXRequestData request, final ServerSession session) {
            return customizer;
        }

    }

    private static final class StaticActionService implements AJAXActionService {

        private final AJAXRequestResult result;

        private ServerSession session;

        private AJAXRequestData request;

        public StaticActionService(final AJAXRequestResult result) {
            this.result = result;
        }

        @Override
        public AJAXRequestResult perform(final AJAXRequestData requestData, final ServerSession session) throws OXException {
            this.request = requestData;
            this.session = session;
            return result;
        }

        public AJAXRequestData getRequest() {
            return request;

        }

        public ServerSession getSession() {
            return session;
        }

    }

    private static final class StaticActionFactory implements AJAXActionServiceFactory {

        private final AJAXActionService actionService;

        private String action;

        public StaticActionFactory(final AJAXActionService actionService) {
            this.actionService = actionService;
        }

        @Override
        public AJAXActionService createActionService(final String action) throws OXException {
            this.action = action;
            return actionService;
        }

        public String getAction() {
            return action;
        }

    }

}
