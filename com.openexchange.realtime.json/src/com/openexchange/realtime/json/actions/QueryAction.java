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

package com.openexchange.realtime.json.actions;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.ExceptionUtils;
import com.openexchange.realtime.dispatch.MessageDispatcher;
import com.openexchange.realtime.exception.RealtimeException;
import com.openexchange.realtime.exception.RealtimeExceptionCodes;
import com.openexchange.realtime.json.impl.StateEntry;
import com.openexchange.realtime.json.impl.StateManager;
import com.openexchange.realtime.json.impl.stanza.builder.StanzaBuilderSelector;
import com.openexchange.realtime.json.impl.stanza.writer.StanzaWriter;
import com.openexchange.realtime.json.stanza.StanzaBuilder;
import com.openexchange.realtime.json.util.RTResultFormatter;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.util.CustomGateAction;
import com.openexchange.realtime.util.StanzaSequenceGate;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;


/**
 * The {@link QueryAction} delivers a message, waits for a result and sends the result back to the client along with the acks and Stanzas
 * addressed to the client.
 *
 *     <li> Query action examples:
 *       <ol>
 *         <li> Join a room via PUT
 *           <pre>
 *           {
 *              "payloads": [
 *               {
 *                 "namespace": "group",
 *                 "data": "join",
 *                 "element": "command"
 *               }
 *             ],
 *             "element": "message",
 *             "selector": "chineseRoomSelector",
 *             "to": "synthetic.china://room1"
 *           }
 *           </pre>
 *         <li> Leave a room via PUT
 *         <pre>
 *         {
 *           "payloads": [
 *             {
 *               "namespace": "group",
 *               "data": "leave",
 *               "element": "command"
 *             }
 *           ],
 *           "element": "message",
 *           "to": "synthetic.china://room1"
 *         }
 *         </pre>
 *       </ol>
 *     </li>
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class QueryAction extends RTAction {

    private static final String CARESULT_ANSWER = "answer";
    private static final String CARESULT_DONE = "done";
    private static final String CARESULT_EXCEPTION = "exception";
    
    protected final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(QueryAction.class);
    private final ServiceLookup services;
    private final StanzaSequenceGate gate;
    private final StateManager stateManager;
    //how long to wait until: EITHER a stanza was answered synchronously OR a valid sequence was constructed from incoming Stanzas
    private final long TIMEOUT = 50;

    /**
     * Initializes a new {@link QueryAction}.
     * @param services
     */
    public QueryAction(ServiceLookup services, StanzaSequenceGate gate, StateManager stateManager) {
        super();
        this.services = services;
        this.gate = gate;
        this.stateManager = stateManager;
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData request, ServerSession session) throws OXException {
        ID id = constructID(request, session);

        if(!stateManager.isConnected(id)) {
            RealtimeException stateMissingException = RealtimeExceptionCodes.STATE_MISSING.create();
            LOG.debug("", stateMissingException);
            Map<String, Object> errorMap = getErrorMap(stateMissingException, session);
            return new AJAXRequestResult(errorMap, "native");
        }
        StateEntry stateEntry = stateManager.retrieveState(id);

        StanzaBuilder<? extends Stanza> stanzaBuilder = StanzaBuilderSelector.getBuilder(id, session, (JSONObject) request.requireData());

        Stanza stanza = stanzaBuilder.build();
        if (stanza.traceEnabled()) {
            stanza.trace("received in backend");
        }

        stanza.setOnBehalfOf(id);
        stanza.setFrom(id);

        stanza.transformPayloadsToInternal();
        stanza.initializeDefaults();

        //Remember the original sequence as it might get changed for local or remote delivery
        long sequenceNumber = stanza.getSequenceNumber();

        final Map<String, Object> customActionResults = new HashMap<String, Object>();
        final Map<String, Object> queryActionResults = new HashMap<String, Object>();

        final Lock sendLock = new ReentrantLock();
        LOG.debug("{}: Trying to lock", Thread.currentThread());
        sendLock.lock();
        try {
            LOG.debug("{}: Got lock", Thread.currentThread());
            final Condition handled = sendLock.newCondition();
            
            
            
            if(gate.handle(stanza, stanza.getTo(), new CustomGateAction() {

                @Override
                public void handle(final Stanza stanza, ID recipient) {
                    LOG.debug("Handling stanza: {}", stanza);
                    try {
                        customActionResults.put(CARESULT_ANSWER, services.getService(MessageDispatcher.class).sendSynchronously(stanza, request.isSet("timeout") ? request.getIntParameter("timeout") : TIMEOUT, TimeUnit.SECONDS));
                    } catch (RealtimeException re) {
                        customActionResults.put(CARESULT_EXCEPTION, re);
                    } catch (Throwable t) {
                        ExceptionUtils.handleThrowable(t);
                        customActionResults.put(CARESULT_EXCEPTION, RealtimeExceptionCodes.RESULT_MISSING.create(t));
                    }
                    customActionResults.put(CARESULT_DONE, Boolean.TRUE);
                    sendLock.lock();
                    try {
                        handled.signal();
                    } finally {
                        sendLock.unlock();
                    }
                    LOG.debug("Done handling Stanza");
                }

            })) {
                /*
                 *  Stanza was handled by the gate. We have to return an ack if the Stanza carried a sequence number
                 */
                if (sequenceNumber >= 0) {
                    List<Long> ackList = Collections.singletonList(sequenceNumber);
                    queryActionResults.put(ACKS, ackList);
                }
            }

            // If the sequence number isn't correct, wait for a given time until a valid sequence was constructed from incoming Stanzas
            if (!customActionResults.containsKey(CARESULT_DONE)) {
                try {
                    if(!handled.await(request.isSet("timeout") ? request.getIntParameter("timeout") : TIMEOUT, TimeUnit.SECONDS)) {
                        LOG.debug("Timeout while waiting for handling Stanza:{} \n CustomActionResults contains: {}", new StanzaWriter().write(stanza), customActionResults);
                        customActionResults.put(CARESULT_EXCEPTION, RealtimeExceptionCodes.RESULT_MISSING.create());
                    }
                } catch (InterruptedException e) {
                    customActionResults.put(CARESULT_EXCEPTION, RealtimeExceptionCodes.RESULT_MISSING.create(e));
                }
            }

        }
        catch (Throwable t) {
            LOG.error("", t);
            throw RealtimeExceptionCodes.STANZA_INTERNAL_SERVER_ERROR.create(t, t.getMessage());
        }
        finally {
            sendLock.unlock();
        }

        //Add the result Object to the response data
        Object answer = customActionResults.get(CARESULT_ANSWER);
        if (answer == null || !Stanza.class.isInstance(answer)) {
            Throwable cause = (Throwable) customActionResults.get(CARESULT_EXCEPTION);
            /*
             * If the answer contains an exception that isn't a specific RealtimeException we have to wrap it into generic server error so
             * the client gets something that he knows to handle 
             */
            RealtimeException noResultException = null;
            if(cause != null) {
                if(!RealtimeException.class.isInstance(cause)) {
                    noResultException = RealtimeExceptionCodes.RESULT_MISSING.create(cause);
                } else {
                    noResultException = RealtimeException.class.cast(cause);
                }
            } else {
                noResultException = RealtimeExceptionCodes.RESULT_MISSING.create();
            }
            queryActionResults.put(ERROR, exceptionToJSON(noResultException, session));
            stanza.trace(noResultException.getMessage(), noResultException);
            LOG.error("", noResultException);
        } else {
            Stanza answerStanza = (Stanza)answer;
            //Set the recipient to the client that originally sent the request
            answerStanza.setTo(id);
            queryActionResults.put(RESULT, stanzaToJSON(answerStanza));
        }

        //additionally check for Stanzas that are addressed to the client and add them to the response
        List<JSONObject> stanzas = pollStanzas(stateEntry.state);
        queryActionResults.put(STANZAS, stanzas);
        LOG.debug("{}", new Object() { @Override public String toString() { return RTResultFormatter.format(queryActionResults);}});
        return new AJAXRequestResult(queryActionResults, "native");
    }

}
