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

package com.openexchange.realtime.atmosphere.http;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.logging.Log;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.ExceptionUtils;
import com.openexchange.realtime.atmosphere.impl.stanza.builder.StanzaBuilderSelector;
import com.openexchange.realtime.atmosphere.impl.stanza.writer.StanzaWriter;
import com.openexchange.realtime.atmosphere.stanza.StanzaBuilder;
import com.openexchange.realtime.dispatch.MessageDispatcher;
import com.openexchange.realtime.exception.RealtimeException;
import com.openexchange.realtime.exception.RealtimeExceptionCodes;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Message;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.payload.PayloadTree;
import com.openexchange.realtime.payload.PayloadTreeNode;
import com.openexchange.realtime.util.CustomGateAction;
import com.openexchange.realtime.util.StanzaSequenceGate;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;


/**
 * The {@link QueryAction} delivers a message, waits for a response and sends that back to the client.
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
 */
public class QueryAction extends RTAction {

    private final static Log LOG = com.openexchange.log.Log.loggerFor(QueryAction.class);
    private final ServiceLookup services;
    private final StanzaSequenceGate gate;
    //how long to wait until: EITHER a stanza was answered synchronously OR a valid sequence was constructed from incoming Stanzas
    private final long TIMEOUT = 50;

    /**
     * Initializes a new {@link QueryAction}.
     * @param services
     */
    public QueryAction(ServiceLookup services, StanzaSequenceGate gate) {
        super();
        this.services = services;
        this.gate = gate;
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData request, ServerSession session) throws OXException {
        ID id = constructID(request, session);

        StanzaBuilder<? extends Stanza> stanzaBuilder = StanzaBuilderSelector.getBuilder(id, session, (JSONObject) request.requireData());

        Stanza stanza = stanzaBuilder.build();
        if (stanza.traceEnabled()) {
            stanza.trace("received in backend");
        }

        stanza.setOnBehalfOf(id);
        stanza.setFrom(id);

        stanza.transformPayloadsToInternal();
        stanza.initializeDefaults();

        final Map<String, Object> values = new HashMap<String, Object>();

        final Lock sendLock = new ReentrantLock();
        try {
            LOG.debug(Thread.currentThread()+ ": Trying to lock");
            sendLock.lock();
            LOG.debug(Thread.currentThread()+ ": Got lock");
            final Condition handled = sendLock.newCondition();
            if(gate.handle(stanza, stanza.getTo(), new CustomGateAction() {

                @Override
                public void handle(final Stanza stanza, ID recipient) {
                    if(LOG.isDebugEnabled()) {
                        LOG.debug("Handling stanza: " + stanza);
                    }
                    try {
                        values.put("answer", services.getService(MessageDispatcher.class).sendSynchronously(stanza, request.isSet("timeout") ? request.getIntParameter("timeout") : TIMEOUT, TimeUnit.SECONDS));
                    } catch (OXException e) {
                        values.put("exception", e);
                    } catch (Throwable t) {
                        ExceptionUtils.handleThrowable(t);
                        LOG.error(t.getMessage(), t);
                        values.put("exception", new OXException(t));
                    }
                    values.put("done", Boolean.TRUE);
                    try {
                        sendLock.lock();
                        handled.signal();
                    } finally {
                        sendLock.unlock();
                    }
                    if(LOG.isDebugEnabled()) {
                        LOG.debug("Done handling Stanza");
                    }
                }

            })) {
                /*
                 *  Stanza was handled by the gate. Return ack asynchronously via the MessageDispatcher so clients can synchronously receive
                 *  the welcomemessage
                 */
                acknowledgeReceipt(stanza, id);
            }

            // If the sequence number isn't correct, wait for a given time until a valid sequence was constructed from incoming Stanzas
            if (!values.containsKey("done")) {
                if(!handled.await(request.isSet("timeout") ? request.getIntParameter("timeout") : TIMEOUT, TimeUnit.SECONDS)) {
                    LOG.debug("Timeout while waiting for correct sequence/handling Stanza:" + new StanzaWriter().write(stanza));
                }
            }

            OXException exception = (OXException) values.get("exception");
            if (exception != null) {
                throw RealtimeExceptionCodes.STANZA_INTERNAL_SERVER_ERROR.create(exception, exception.getMessage());
            }
        } catch (OXException e) {
            LOG.error(e.getMessage(), e);
            RealtimeException re = RealtimeExceptionCodes.STANZA_INTERNAL_SERVER_ERROR.create(e, e.getMessage());
            throw re;
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
            RealtimeException re = RealtimeExceptionCodes.STANZA_INTERNAL_SERVER_ERROR.create(e, e.getMessage());
            throw re;
        } catch (Throwable e) {
            LOG.error(e);
            RealtimeException re = RealtimeExceptionCodes.STANZA_INTERNAL_SERVER_ERROR.create(e, e.getMessage());
            throw re;
        } finally {
            sendLock.unlock();
        }

        Object answer = values.get("answer");

        if (answer == null || !Stanza.class.isInstance(answer)) {
            RealtimeException realtimeException = RealtimeExceptionCodes.STANZA_INTERNAL_SERVER_ERROR.create("Request didn't yield any response.");
            LOG.error(realtimeException.getMessage(), realtimeException);
            stanza.setError(realtimeException);
            stanza.transformPayloads("json");
            stanza.setSequenceNumber(-1L);
            return new AJAXRequestResult(new StanzaWriter().write(stanza), "json");
        }
        Stanza answerStanza = (Stanza)answer;
        //Set the recipient to the client that originally sent the request
        answerStanza.setTo(id);
        return new AJAXRequestResult(new StanzaWriter().write(answerStanza), "json");
    }

    /**
     * Asynchronously send an acknowledgement for the received Stanza
     * @param stanza The received Stanza
     * @param id The ID of the recipient
     * @throws OXException
     */
    private void acknowledgeReceipt(Stanza stanza, ID id) throws OXException {
        long sequenceNumber = stanza.getSequenceNumber();
        if (sequenceNumber >= 0) {
        Stanza s = new Message();
        s.setFrom(stanza.getTo());
        s.setTo(id);
        s.addPayload(new PayloadTree(PayloadTreeNode.builder().withPayload(
            sequenceNumber,
            "json",
            "atmosphere",
            "received").build()));

        if(LOG.isDebugEnabled()) {
            LOG.debug("Acknowledging Stanza: " +new StanzaWriter().write(s));
        }

        services.getService(MessageDispatcher.class).send(s);
        }

    }

}
