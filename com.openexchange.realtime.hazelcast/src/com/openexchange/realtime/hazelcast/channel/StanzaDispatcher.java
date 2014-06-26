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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.realtime.hazelcast.channel;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.cleanup.GlobalRealtimeCleanup;
import com.openexchange.realtime.dispatch.LocalMessageDispatcher;
import com.openexchange.realtime.dispatch.MessageDispatcher;
import com.openexchange.realtime.exception.RealtimeExceptionCodes;
import com.openexchange.realtime.hazelcast.Utils;
import com.openexchange.realtime.hazelcast.osgi.Services;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Stanza;

/**
 * {@link StanzaDispatcher}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class StanzaDispatcher implements Callable<Map<ID, OXException>>, Serializable {

    private static final long serialVersionUID = 7824598922472715144L;
    private static final Logger LOG = LoggerFactory.getLogger(StanzaDispatcher.class);
    private final Stanza stanza;
    private final Set<ID> targets;

    /**
     * Initializes a new {@link StanzaDispatcher}.
     * @throws OXException
     */
    public StanzaDispatcher() throws OXException {
        this(null, null);
    }

    /**
     * Initializes a new {@link StanzaDispatcher}.
     *
     * @param stanza The stanza to dispatch
     * @throws OXException
     */
    public StanzaDispatcher(Stanza stanza, Set<ID> targets) throws OXException {
        super();
        this.targets = targets;
        this.stanza = stanza;
        if (stanza != null) {
            stanza.transformPayloads("native");
        }
    }

    @Override
    public Map<ID, OXException> call() throws Exception {
        stanza.trace("Received remote delivery. Dispatching locally");
        LocalMessageDispatcher dispatcher = Services.getService(LocalMessageDispatcher.class);
        Map<ID, OXException> exceptions = dispatcher.send(stanza, targets);
        /*
         * The Stanza was delivered to this node because the ResourceDirectory listed this node in the routing info. If the Resource isn't
         * available anylonger we remove it from the ResourceDirectory and try to send the Stanza again via the GlobalMessageDispatcher
         * service. This will succeed if the Channel can conjure the Resource.
         */
        if (Utils.shouldResend(exceptions, stanza)) {
            //Can't resend without incrementing but incrementing will mess up client sequences and further communication, so set to -1
            stanza.setSequenceNumber(-1);
            final GlobalRealtimeCleanup cleanup = Services.optService(GlobalRealtimeCleanup.class);
            final MessageDispatcher messageDispatcher = Services.optService(MessageDispatcher.class);
            if (cleanup == null || messageDispatcher == null) {
                LOG.error(
                    "Error while trying to resend.",
                    RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(cleanup == null ? GlobalRealtimeCleanup.class : MessageDispatcher.class));
            } else {
                cleanup.cleanForId(stanza.getTo());
                messageDispatcher.send(stanza);
                // remove the exception that triggered the resend.
                exceptions.remove(stanza.getTo());
            }
        }
        return exceptions;
    }

}
