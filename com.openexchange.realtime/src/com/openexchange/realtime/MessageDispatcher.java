package com.openexchange.realtime;

import com.openexchange.exception.OXException;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.tools.session.ServerSession;

/**
 * The Message dispatcher chooses an appropriate Channel to push data (aka. a Stanza) to listening clients
 *  
 * @author francisco.laguna@open-xchange.com
 *
 */
public interface MessageDispatcher {
	
	/**
	 * Push a {@link Stanza} to a given recipient. A stanza is one of its subclasses, usually a Message, Presence or Iq.
	 * @param stanza 
	 * @param session
	 * @throws OXException
	 */
	public void send(Stanza stanza, ServerSession session) throws OXException;
	
}
