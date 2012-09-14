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

package com.openexchange.realtime.atmosphere.impl.stanza;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.atmosphere.AtmosphereExceptionCode;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Presence;
import com.openexchange.realtime.packet.Presence.Type;
import com.openexchange.realtime.packet.Presence.Type.*;


/**
 * {@link PresenceParser} - Parese an atmosphere client's presence message and build a Presence Stanza from it by adding the recipients ID.
 * 
 * Modeled after http://tools.ietf.org/html/rfc3921#page-16
 * 
 * A valid Presence message contains at least 
 * <pre>
 * {
     kind: 'presence'
     to:   'myuser@mycontext',
     data: {
       state:    'online',
       message:  'i am here',
       [priority: 0],
       [type: none]
     }
  };
 * </pre>
 * and is transformed into a Presence stanza in the end
 * <pre>
 * {
     from: 'usera@context',
     to:   'myuser@mycontext',
     namespace. 'default',
     payload : {
       format: 'presence'
       data: {
           state:    'online',
           message:  'i am here',
           [priority: 0]
           [type: none]
       }
     }
  };
 * </pre>
 * 
 * Presence Stanza with data object of type JSONObject ( later transformed into PresenceStatus)
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class PresenceBuilder extends StanzaBuilder<Presence> {
    
    /**
     * Create a new PresenceBuilder
     * Initializes a new {@link PresenceBuilder}.
     * @param from the sender's ID, must not be null
     * @param json the sender's message, must not be null
     * @throws IllegalArgumentException if from or json are null
     */
    public PresenceBuilder(ID from, JSONObject json) {
        if(from == null || json == null) {
            throw new IllegalArgumentException();
        }
        this.from = from;
        this.json = json;
        this.stanza = new Presence();
    }
    
    @Override
    protected Presence build() throws OXException {
        basics();
        type();
        return stanza;
    }
    
    
    private void type() throws OXException {
        if(json.has("type")) {
            String type = json.optString("type");
            if (type.equalsIgnoreCase("UNAVAILABLE")) {
                this.stanza.setState(Type.UNAVAILABLE);
            } else if (type.equalsIgnoreCase("SUBSCRIBE")) {
                this.stanza.setState(Type.SUBSCRIBE);
            } else if (type.equalsIgnoreCase("SUBSCRIBED")) {
                this.stanza.setState(Type.SUBSCRIBED);
            } else if (type.equalsIgnoreCase("UNSUBSCRIBE")) {
                this.stanza.setState(Type.UNSUBSCRIBE);
            } else if (type.equalsIgnoreCase("UNSUBSCRIBED")) {
                this.stanza.setState(Type.UNSUBSCRIBED);
            } else if (type.equalsIgnoreCase("ERROR")) {
                this.stanza.setState(Type.ERROR);
            } else if (type.equalsIgnoreCase("NONE")) {
                this.stanza.setState(Type.NONE);
            } else if (type.equalsIgnoreCase("PENDING")) {
                this.stanza.setState(Type.PENDING);
            } else {
                throw AtmosphereExceptionCode.ERROR_WHILE_BUILDING.create("Malformed Presence type");
            }
        }
    }
    
    @Override
    protected void validate() throws OXException {
        switch(this.stanza.getState()) {
        case UNAVAILABLE: ;
            break;
        case SUBSCRIBE: ;
            break;
        case SUBSCRIBED: ;
            break;
        case UNSUBSCRIBE:;
            break;
        case UNSUBSCRIBED:;
            break;
        case ERROR:;
            break;
        case NONE:;
            break;
        case PENDING:;
            break;
        }
    }

}
