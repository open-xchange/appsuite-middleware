
package com.openexchange.realtime.atmosphere.stanza;

import com.openexchange.realtime.packet.Stanza;

public interface StanzaInitializer<T extends Stanza> {

    /**
     * Visit the Stanza's default payloads and initialize its fields based on the found payloads.
     */
    T initialize();

}
