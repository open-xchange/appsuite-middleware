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

package com.openexchange.realtime.json.protocol;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.exception.RealtimeException;
import com.openexchange.realtime.packet.GenericError;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.util.StanzaSequenceGate;

/**
 * {@link RTProtocol} contains the logic to handle all protocol events of the RT protocol like pings and acknowledgements.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public interface RTProtocol {

    /**
     * Called when a GET request is received from the client.
     * 
     * @param state - the client state
     * @param transmitter - the client transmitter
     */
    public abstract void getReceived(RTClientState state, StanzaTransmitter transmitter);

    /**
     * Called when a Ping is received from the client. If the ping asks for a commit, a Pong message is generated, enqueJSONExceptionued and the buffer
     * is emptied, otherwise only the states timestamp is touched.
     */
    public abstract void ping(ID from, boolean commit, RTClientState state, StanzaTransmitter transmitter);

    /**
     * A 'received' message was received from the client
     */
    public abstract void acknowledgementReceived(long seq, RTClientState state);

    /**
     * Enqueus a stanza and empties the buffer
     * 
     * @throws OXException
     */
    public abstract void send(Stanza stanza, RTClientState state, StanzaTransmitter transmitter);

    /**
     * A message was received from the client
     * 
     * @param newState
     * @throws RealtimeException 
     * @throws OXException
     */
    public abstract void receivedMessage(Stanza stanza, StanzaSequenceGate gate, RTClientState state, boolean newState, StanzaTransmitter transmitter) throws RealtimeException;

    /**
     * A message was received from the client. Instead of sending acknlowledgements, they will be collected in the passed acknowledgements
     * list.
     * @throws RealtimeException 
     */
    public abstract void receivedMessage(Stanza stanza, StanzaSequenceGate gate, RTClientState state, boolean b, StanzaTransmitter transmitter, List<Long> acknowledgements) throws RealtimeException;

    /**
     * Empties the buffer, if there are messages to be sent
     */
    public abstract void emptyBuffer(RTClientState state, StanzaTransmitter transmitter);

    /**
     * Handle RealtimeExceptions that occured during protocol processing. This generates an error Stanza and asynchronously sends it via the
     * MessageDispatcher to the client that should be informed about the error situation. If the causing Stanza is null a
     * {@link GenericError} Stanza will be sent to the client.
     * 
     * @param recipient The client that you want to inform about the error. If null the exception will only appear in the server logs.
     * @param exception The exception that occurred
     * @param stanza The stanza that caused the exception. This helps the client to relate the error to one of his actions. May be null
     */
    public abstract void handleRealtimeException(ID recipient, RealtimeException exception, Stanza stanza);

    /**
     * The client wishes to reset its sequence numbering. This causes the states that are associated with the client to be reset.
     * 
     * @param id The ID of the client
     * @param newSequence The new Sequence the clients would like to use.
     * @param gate The StanzaSequenceGate that has to be reset
     * @param clientState The RTClientState that has to be reset
     */
    void nextSequence(ID id, int newSequence, StanzaSequenceGate gate, RTClientState clientState) throws RealtimeException;

}
