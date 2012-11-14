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

package com.openexchange.realtime.presence;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.MessageDispatcher;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Presence;
import com.openexchange.realtime.util.IDMap;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link DummyPresenceService}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class DummyPresenceService implements PresenceStatusService {

    private final ServiceLookup services;

    private final IDMap<PresenceData> statusMap = new IDMap<PresenceData>();

    private final IDMap<List<ID>> subscriptions = new IDMap<List<ID>>();

    public DummyPresenceService(ServiceLookup services) {
        super();
        this.services = services;

        // Set up some subscriptions. These would be stored in the database and managed by a PresenceSubscription service or additional
        // methods on this service

        ID mah = new ID("martin.herfurth@premium");
        ID top = new ID("tobias.prinz@premium");
        ID fla = new ID("francisco.laguna@premium");
        ID marens = new ID("marc.arens@premium");

        subscriptions.put(mah, Arrays.asList(fla, top));
        subscriptions.put(top, Arrays.asList(mah, fla));
        subscriptions.put(fla, Arrays.asList(mah, top));
        subscriptions.put(marens, Arrays.asList(mah, top, fla));

    }

    @Override
    public void changePresenceStatus(ID id, PresenceData status, ServerSession session) throws OXException {
        statusMap.put(id.toGeneralForm(), status);

        MessageDispatcher dispatcher = services.getService(MessageDispatcher.class);

        for (ID subscriber : subscriptions.get(id.toGeneralForm())) {
            Presence presence = new Presence();

            presence.setFrom(id);
            presence.setTo(subscriber);
            presence.setState(status.getState());
            presence.setMessage(status.getMessage());
            dispatcher.send(presence, session);
        }

    }

    @Override
    public PresenceData getPresenceStatus(ID id) {
        return statusMap.get(id.toGeneralForm());
    }

    /* (non-Javadoc)
     * @see com.openexchange.realtime.presence.PresenceStatusService#getPresenceStatus(java.util.Collection)
     */
    @Override
    public IDMap<PresenceData> getPresenceStatus(Collection<ID> ids) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.realtime.presence.PresenceStatusService#registerPresenceChangeListener(com.openexchange.realtime.presence.PresenceChangeListener)
     */
    @Override
    public void registerPresenceChangeListener(PresenceChangeListener presenceChangeListener) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.realtime.presence.PresenceStatusService#unregisterPresenceChangeListener(com.openexchange.realtime.presence.PresenceChangeListener)
     */
    @Override
    public void unregisterPresenceChangeListener(PresenceChangeListener presenceChangeListener) {
        // TODO Auto-generated method stub
        
    }

}
