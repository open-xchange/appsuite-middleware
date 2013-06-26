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

package com.openexchange.realtime.hazelcast.channel;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.directory.Resource;
import com.openexchange.realtime.directory.ResourceDirectory;
import com.openexchange.realtime.dispatch.DispatchExceptionCode;
import com.openexchange.realtime.dispatch.LocalMessageDispatcher;
import com.openexchange.realtime.dispatch.MessageDispatcher;
import com.openexchange.realtime.hazelcast.Services;
import com.openexchange.realtime.hazelcast.Utils;
import com.openexchange.realtime.hazelcast.impl.GlobalMessageDispatcherImpl;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.util.IDMap;

/**
 * {@link StanzaDispatcher}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class StanzaDispatcher implements Callable<Map<ID, OXException>>, Serializable {

    private static final long serialVersionUID = 7824598922472715144L;
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
        stanza.trace("Received remove delivery. Dispatching locally");
        LocalMessageDispatcher dispatcher = Services.getService(LocalMessageDispatcher.class);
        Map<ID, OXException> exceptions = dispatcher.send(stanza, targets);
        if (Utils.shouldResend(exceptions, stanza)) {
            ResourceDirectory directory = Services.optService(ResourceDirectory.class);
            directory.remove(stanza.getTo());
            Services.getService(MessageDispatcher.class).send(stanza);
        }
        return exceptions;
            
    }

}
