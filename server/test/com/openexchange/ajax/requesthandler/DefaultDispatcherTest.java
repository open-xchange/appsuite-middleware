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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.ajax.requesthandler;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import junit.framework.TestCase;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link DefaultDispatcherTest}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class DefaultDispatcherTest extends TestCase {

    public void testDispatchesToActionService() throws AbstractOXException {
        DefaultDispatcher dispatcher = new DefaultDispatcher();

        AJAXRequestResult res = new AJAXRequestResult();

        StaticActionService action = new StaticActionService(res);
        StaticActionFactory factory = new StaticActionFactory(action);

        dispatcher.register("someModule", factory);

        AJAXRequestData requestData = new AJAXRequestData();
        requestData.setModule("someModule");
        requestData.setAction("someAction");

        AJAXRequestResult receivedResult = dispatcher.perform(requestData, null);

        assertSame(res, receivedResult);
    }

    public void testChain() throws AbstractOXException {
        DefaultDispatcher dispatcher = new DefaultDispatcher();

        AJAXRequestResult res = new AJAXRequestResult();

        StaticActionService action = new StaticActionService(res);
        StaticActionFactory factory = new StaticActionFactory(action);

        SimAJAXCustomizer c1 = new SimAJAXCustomizer("c1");
        SimAJAXCustomizer c2 = new SimAJAXCustomizer("c2");
        SimAJAXCustomizer c3 = new SimAJAXCustomizer("c3");
        
        dispatcher.addCustomizer(new StaticAJAXCustomizerFactory(c1));
        dispatcher.addCustomizer(new StaticAJAXCustomizerFactory(c2));
        dispatcher.addCustomizer(new StaticAJAXCustomizerFactory(c3));
        
        dispatcher.register("someModule", factory);
        
        AJAXRequestData requestData = new AJAXRequestData();
        requestData.setModule("someModule");
        requestData.setAction("someAction");

        SimAJAXCustomizer.LOG.clear();
        
        AJAXRequestResult receivedResult = dispatcher.perform(requestData, null);

        assertSame(res, receivedResult);
        
        assertSame(requestData, c1.getRequest());
        assertSame(requestData, c2.getRequest());
        assertSame(requestData, c3.getRequest());
        
        assertSame(res, c1.getResult());
        assertSame(res, c2.getResult());
        assertSame(res, c3.getResult());
        
        assertEquals(SimAJAXCustomizer.LOG, Arrays.asList("c1:incoming", "c2:incoming", "c3:incoming", "c3:outgoing", "c2:outgoing", "c1:outgoing"));
    }
    
    public void testLaterIncoming() throws AbstractOXException {
        DefaultDispatcher dispatcher = new DefaultDispatcher();

        AJAXRequestResult res = new AJAXRequestResult();

        StaticActionService action = new StaticActionService(res);
        StaticActionFactory factory = new StaticActionFactory(action);

        SimAJAXCustomizer c1 = new SimAJAXCustomizer("c1") {
            
            private boolean skipped = false;
            
            @Override
            public AJAXRequestData incoming(AJAXRequestData request, ServerSession session) throws AbstractOXException {
                if (!skipped) {
                    skipped = true;
                    throw new FlowControl.Later();
                }
                return super.incoming(request, session);
            }
        };
        SimAJAXCustomizer c2 = new SimAJAXCustomizer("c2");
        SimAJAXCustomizer c3 = new SimAJAXCustomizer("c3");
        
        dispatcher.addCustomizer(new StaticAJAXCustomizerFactory(c1));
        dispatcher.addCustomizer(new StaticAJAXCustomizerFactory(c2));
        dispatcher.addCustomizer(new StaticAJAXCustomizerFactory(c3));
        
        dispatcher.register("someModule", factory);
        
        AJAXRequestData requestData = new AJAXRequestData();
        requestData.setModule("someModule");
        requestData.setAction("someAction");

        SimAJAXCustomizer.LOG.clear();
        
        dispatcher.perform(requestData, null);

        assertEquals(SimAJAXCustomizer.LOG, Arrays.asList("c2:incoming", "c3:incoming", "c1:incoming", "c1:outgoing", "c3:outgoing", "c2:outgoing"));
    }
    
    public void testLaterIncomingTwice() throws AbstractOXException {
        DefaultDispatcher dispatcher = new DefaultDispatcher();

        AJAXRequestResult res = new AJAXRequestResult();

        StaticActionService action = new StaticActionService(res);
        StaticActionFactory factory = new StaticActionFactory(action);

        SimAJAXCustomizer c1 = new SimAJAXCustomizer("c1") {
            
            private int skipCount = 0;
            
            @Override
            public AJAXRequestData incoming(AJAXRequestData request, ServerSession session) throws AbstractOXException {
                if (skipCount < 2) {
                    skipCount++;
                    throw new FlowControl.Later();
                }
                return super.incoming(request, session);
            }
        };
        SimAJAXCustomizer c2 = new SimAJAXCustomizer("c2");
        SimAJAXCustomizer c3 = new SimAJAXCustomizer("c3");
        
        dispatcher.addCustomizer(new StaticAJAXCustomizerFactory(c1));
        dispatcher.addCustomizer(new StaticAJAXCustomizerFactory(c2));
        dispatcher.addCustomizer(new StaticAJAXCustomizerFactory(c3));
        
        dispatcher.register("someModule", factory);
        
        AJAXRequestData requestData = new AJAXRequestData();
        requestData.setModule("someModule");
        requestData.setAction("someAction");

        SimAJAXCustomizer.LOG.clear();
        
        dispatcher.perform(requestData, null);

        assertEquals(SimAJAXCustomizer.LOG, Arrays.asList("c2:incoming", "c3:incoming", "c1:incoming", "c1:outgoing", "c3:outgoing", "c2:outgoing"));
    }
    
    public void testLaterOutgoing() throws AbstractOXException {
        DefaultDispatcher dispatcher = new DefaultDispatcher();

        AJAXRequestResult res = new AJAXRequestResult();

        StaticActionService action = new StaticActionService(res);
        StaticActionFactory factory = new StaticActionFactory(action);

        SimAJAXCustomizer c1 = new SimAJAXCustomizer("c1"); 
        SimAJAXCustomizer c2 = new SimAJAXCustomizer("c2");
        SimAJAXCustomizer c3 = new SimAJAXCustomizer("c3"){
            
            private boolean skipped = false;
            
            @Override
            public AJAXRequestResult outgoing(AJAXRequestData request,AJAXRequestResult res, ServerSession session) throws AbstractOXException {
                if (!skipped) {
                    skipped = true;
                    throw new FlowControl.Later();
                }
                return super.outgoing(request, res, session);
            }
        };
        
        dispatcher.addCustomizer(new StaticAJAXCustomizerFactory(c1));
        dispatcher.addCustomizer(new StaticAJAXCustomizerFactory(c2));
        dispatcher.addCustomizer(new StaticAJAXCustomizerFactory(c3));
        
        dispatcher.register("someModule", factory);
        
        AJAXRequestData requestData = new AJAXRequestData();
        requestData.setModule("someModule");
        requestData.setAction("someAction");

        SimAJAXCustomizer.LOG.clear();
        
        dispatcher.perform(requestData, null);

        assertEquals(SimAJAXCustomizer.LOG, Arrays.asList("c1:incoming", "c2:incoming", "c3:incoming", "c2:outgoing", "c1:outgoing", "c3:outgoing"));
    }
    
    public void testLaterOutgoingTwice() throws AbstractOXException {
        DefaultDispatcher dispatcher = new DefaultDispatcher();

        AJAXRequestResult res = new AJAXRequestResult();

        StaticActionService action = new StaticActionService(res);
        StaticActionFactory factory = new StaticActionFactory(action);

        SimAJAXCustomizer c1 = new SimAJAXCustomizer("c1"); 
        SimAJAXCustomizer c2 = new SimAJAXCustomizer("c2");
        SimAJAXCustomizer c3 = new SimAJAXCustomizer("c3"){
            
            private int skipCount = 0;
            
            @Override
            public AJAXRequestResult outgoing(AJAXRequestData request,AJAXRequestResult res, ServerSession session) throws AbstractOXException {
                if (skipCount < 2) {
                    skipCount++;
                    throw new FlowControl.Later();
                }
                return super.outgoing(request, res, session);
            }
        };
        
        dispatcher.addCustomizer(new StaticAJAXCustomizerFactory(c1));
        dispatcher.addCustomizer(new StaticAJAXCustomizerFactory(c2));
        dispatcher.addCustomizer(new StaticAJAXCustomizerFactory(c3));
        
        dispatcher.register("someModule", factory);
        
        AJAXRequestData requestData = new AJAXRequestData();
        requestData.setModule("someModule");
        requestData.setAction("someAction");

        SimAJAXCustomizer.LOG.clear();
        
        dispatcher.perform(requestData, null);

        assertEquals(SimAJAXCustomizer.LOG, Arrays.asList("c1:incoming", "c2:incoming", "c3:incoming", "c2:outgoing", "c1:outgoing", "c3:outgoing"));
    }
    
    // Error Cases
    
    public void testUnknownModule() throws AbstractOXException {
        DefaultDispatcher dispatcher = new DefaultDispatcher();
        AJAXRequestData requestData = new AJAXRequestData();
        requestData.setModule("someModule");
        requestData.setAction("someAction");

        try {
            dispatcher.perform(requestData, null);
            fail("Should have produced an AjaxException");
        } catch (AjaxException x) {
            // All Done
        }
        
    }
    
    public void testUnknownAction() throws AbstractOXException {
        DefaultDispatcher dispatcher = new DefaultDispatcher();

        StaticActionFactory factory = new StaticActionFactory(null);

        dispatcher.register("someModule", factory);

        AJAXRequestData requestData = new AJAXRequestData();
        requestData.setModule("someModule");
        requestData.setAction("someAction");

        try {
            dispatcher.perform(requestData, null);
            fail("Should have produced an AjaxException");
        } catch (AjaxException x) {
            // All Done
        }
    }
    
    public void testNullCustomizer() throws AbstractOXException {
        DefaultDispatcher dispatcher = new DefaultDispatcher();
        AJAXRequestResult res = new AJAXRequestResult();

        StaticActionService action = new StaticActionService(res);
        StaticActionFactory factory = new StaticActionFactory(action);

        dispatcher.register("someModule", factory);

        dispatcher.addCustomizer(new StaticAJAXCustomizerFactory(null));
        
        AJAXRequestData requestData = new AJAXRequestData();
        requestData.setModule("someModule");
        requestData.setAction("someAction");

        dispatcher.perform(requestData, null);
    }

    private static class SimAJAXCustomizer implements AJAXActionCustomizer {
        public static List<String> LOG = new LinkedList<String>();
        
        private AJAXRequestData request;
        private AJAXRequestResult result;
        private String name;
        
        private SimAJAXCustomizer(String name) {
            this.name = name;
        }

        public AJAXRequestData incoming(AJAXRequestData request, ServerSession session) throws AbstractOXException {
            this.request = request;
            LOG.add(name+":incoming");
            return request;
        }

        public AJAXRequestResult outgoing(AJAXRequestData request, AJAXRequestResult result, ServerSession session) throws AbstractOXException {
            this.result = result;
            LOG.add(name+":outgoing");
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

        private AJAXActionCustomizer customizer;

        public StaticAJAXCustomizerFactory(AJAXActionCustomizer customizer) {
            super();
            this.customizer = customizer;
        }

        public AJAXActionCustomizer createCustomizer(AJAXRequestData request, ServerSession session) {
            return customizer;
        }
        
    }

    private static final class StaticActionService implements AJAXActionService {

        private AJAXRequestResult result;

        private ServerSession session;

        private AJAXRequestData request;

        public StaticActionService(AJAXRequestResult result) {
            this.result = result;
        }

        public AJAXRequestResult perform(AJAXRequestData request, ServerSession session) throws AbstractOXException {
            this.request = request;
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

        private AJAXActionService actionService;

        private String action;

        public StaticActionFactory(AJAXActionService actionService) {
            this.actionService = actionService;
        }

        public AJAXActionService createActionService(String action) throws AjaxException {
            this.action = action;
            return actionService;
        }

        public String getAction() {
            return action;
        }

    }

}
