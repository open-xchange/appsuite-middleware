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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.group.GroupCommand;
import com.openexchange.realtime.group.GroupDispatcher;
import com.openexchange.realtime.group.NotMember;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Stanza;


/**
 * {@link PingCommand} - A PingCommand is issued periodically by each member of a {@link GroupDispatcher} as some {@link GroupDispatcher}s
 * may have a timeout policy. When no message arrives at a {@link GroupDispatcher} for a certain time, the group dispatcher is shut down. To
 * prevent this from happening members are supposed to send a {@link Stanza} representing a {@link PingCommand} to a given group.
 * Furthermore this ping periodically checks if the sending client is still a member of the addressed {@link GroupDispatcher} as
 * {@link GroupDispatcher}s may be destroyed when a node is shut down. The next {@link Stanza} addressed at this {@link GroupDispatcher} will
 * recreate the {@link GroupDispatcher} on another backend node. This will force clients to rejoin the {@link GroupDispatcher}.
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class PingCommand implements GroupCommand {

    private static final Logger LOG = LoggerFactory.getLogger(PingCommand.class);
    
    @Override
    public void perform(Stanza stanza, GroupDispatcher groupDispatcher) throws OXException {
        ID from = stanza.getFrom();
        ID groupId = groupDispatcher.getId();
        if(!groupDispatcher.isMember(from)) {
            LOG.debug("Refusing to send to GroupDispatcher as sender {} is no member of the GroupDispatcher {}", stanza.getFrom(), groupId);
            groupDispatcher.send(new NotMember(groupId, from, stanza.getSelector()));
        }
    }

}
