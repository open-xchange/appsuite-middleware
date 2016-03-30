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

package com.openexchange.realtime.group.commands;

import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.dispatch.MessageDispatcher;
import com.openexchange.realtime.exception.RealtimeException;
import com.openexchange.realtime.exception.RealtimeExceptionCodes;
import com.openexchange.realtime.group.GroupCommand;
import com.openexchange.realtime.group.GroupDispatcher;
import com.openexchange.realtime.group.NotMember;
import com.openexchange.realtime.group.osgi.GroupServiceRegistry;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.util.ActionHandler;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;


/**
 * {@link LeaveCommand}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class LeaveCommand implements GroupCommand {

    private static final Logger LOG = LoggerFactory.getLogger(LeaveCommand.class);

    @Override
    public void perform(final Stanza stanza, final GroupDispatcher groupDispatcher) throws RealtimeException {
        try {
            if (isSynchronous(stanza)) { // call:// surrogate sender in from, use getOnBehalf for real sender
                if (shouldExecuteAsynchronously(groupDispatcher)) {
                    try {
                        GroupServiceRegistry.getInstance().getService(ThreadPoolService.class).submit(new AbstractTask<Void>() {

                            @Override
                            public Void call() throws OXException {
                                doSignOff(stanza, groupDispatcher);
                                return null;
                            }
                        }).get();
                    } catch (InterruptedException e) {
                        throw e;
                    } catch (ExecutionException e) {
                        throw e.getCause();
                    }
                } else {
                    doSignOff(stanza, groupDispatcher);
                }
            } else { // real sender in from
                groupDispatcher.leave(stanza.getFrom(), stanza);
            }
        } catch(RealtimeException re) {
            throw re;
        } catch (Throwable t) {
            throw RealtimeExceptionCodes.LEAVE_FAILED.create(t, stanza.getFrom().toString());
        }
    }

    protected void doSignOff(final Stanza stanza, final GroupDispatcher groupDispatcher) throws OXException {
        Stanza signOffMessage = groupDispatcher.getSignOffMessage(stanza.getOnBehalfOf());
        if (null == signOffMessage) {
            // Apparently requester was no member of the group dispatcher
            return;
        }
        signOffMessage.setFrom(groupDispatcher.getId());
        signOffMessage.setTo(stanza.getFrom());
        groupDispatcher.leave(stanza.getOnBehalfOf(), stanza);
        GroupServiceRegistry.getInstance().getService(MessageDispatcher.class).send(signOffMessage);
    }

    private boolean shouldExecuteAsynchronously(GroupDispatcher groupDispatcher) {
        try {
            return ActionHandler.isAsynchronous(groupDispatcher.getClass().getMethod("getSignOffMessage", ID.class));
        } catch (SecurityException e) {
            return false;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private boolean isSynchronous(Stanza stanza) {
        return stanza.getFrom().getProtocol().equals("call");
    }

    /**
     * During synchronous calls the real sender gets moved to Stanza's onBehalfOf and is replaced by a surrogate sender, a UUID (see
     * com.openexchange.realtime.hazelcast.impl.ResponseChannel.setUp(String, Stanza)). We have to find out who actually wants to leave the
     * GroupDispatcher.
     *
     * @param stanza the {@link Stanza} representing the LeaveCommand.
     * @return the real sender
     */
    private ID getRealSender(Stanza stanza) {
        if(isSynchronous(stanza)) {
            return stanza.getOnBehalfOf();
        }
        return stanza.getFrom();
    }

    /**
     * Notify the sender of the {@link Stanza} that we refuse to execute the {@link LeaveCommand} as he is no member of the addressed
     * {@link GroupDispatcher}
     *
     * @param leaveCommand
     * @throws OXException
     */
    private void doNotifyNotMember(Stanza leaveCommand, GroupDispatcher groupDispatcher) throws OXException {
        Stanza notMemberMessage = new NotMember(groupDispatcher.getId(), leaveCommand.getFrom(), leaveCommand.getSelector());
        GroupServiceRegistry.getInstance().getService(MessageDispatcher.class).send(notMemberMessage);
    }

}
