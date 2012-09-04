
package com.openexchange.realtime.presence.hazelcast.impl;

import com.openexchange.exception.OXException;
import com.openexchange.realtime.example.presence.PresenceService;
import com.openexchange.realtime.example.presence.PresenceStatus;
import com.openexchange.realtime.packet.ID;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link HazelCastPresenceService}
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class HazelCastPresenceService implements PresenceService {

    @Override
    public PresenceStatus getPresence(ID id) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public void changeState(ID id, PresenceState state, String statusMessage, ServerSession session) throws OXException {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

}
