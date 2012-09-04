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

package com.openexchange.realtime.example.atmosphere.chat;

import java.io.IOException;
import java.util.Date;
import org.atmosphere.cpr.AtmosphereHandler;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResponse;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.websocket.WebSocketEventListenerAdapter;
import com.openexchange.log.Log;
import com.openexchange.log.LogFactory;


/**
 * {@link ChatHandler} as Example of Atmosphere integration via
 * AtmosphereService into OX,
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class ChatHandler implements AtmosphereHandler {

    private static final org.apache.commons.logging.Log LOG = Log.valueOf(LogFactory.getLog(ChatHandler.class));
    
    @Override
    public void destroy() {
        if(LOG.isInfoEnabled()) {
            LOG.info("Stopping atmosphere framework");
        }
    }

    /*
     * The AtmosphereHandler.onRequest is invoked every time a new connection 
     * is made to an application. An application must take action and decide 
     * what to do with the AtmosphereResource, e.g. suspend, resume or 
     * broadcast events. You can also write String or bytes back to the client
     * from that method.
     */
    @Override
    public void onRequest(AtmosphereResource resource) throws IOException {

        // Log all events on the console, including WebSocket events.
        resource.addEventListener(new WebSocketEventListenerAdapter());
        
        AtmosphereRequest request = resource.getRequest();
        String method = request.getMethod();
        AtmosphereResponse response = resource.getResponse();
        
        if(method.equalsIgnoreCase("GET")) {
            /*
             * GET requests can be handled via Continuations. Suspend the request 
             * and use it for bidirectional communication.
             * "negotiating" header is used to list all supported transports
             */
            
            
            if(request.getHeader("negotiating") == null) {
                LOG.info(">>>> Going to suspend request: "+ request);
                resource.suspend();
            } else {
                response.getWriter().write("OK");
            }
            
            
        } else if (method.equalsIgnoreCase("POST")) {
            /*
             * Use POST request to synchronously send data over the server 
             */
            String message = request.getReader().readLine().trim();
            LOG.info(">>>> Got message: "+ message);
            
            /*
             * The default Broadcaster of an AtmosphereResource is always "/*"
             * so if you broadcast a message it will reach all
             * AtmosphereResources (connected Clients) and they have the chance
             * to handle the broadcast event.
             * More infos at https://github.com/Atmosphere/atmosphere/wiki/Understanding-Broadcaster
             */
            Broadcaster defaultBroadcaster = resource.getBroadcaster();
            defaultBroadcaster.broadcast(message);
        }
        
    }

    /*
     * We received a broadcast message, now handle it.
     */
    @Override
    public void onStateChange(AtmosphereResourceEvent event) throws IOException {
        AtmosphereResource resource = event.getResource();
        AtmosphereResponse response = resource.getResponse();
        
        //Did we suspend the AtmosphereResource earlier?
        if(event.isSuspended()) {
            String body = event.getMessage().toString();
            LOG.info(">>>> Going to write message body: " + body);

            // Simple JSON -- Use Jackson for more complex structure
            // Message looks like { "author" : "foo", "message" : "bar" }
            String author = body.substring(body.indexOf(":") + 2, body.indexOf(",") - 1);
            String message = body.substring(body.lastIndexOf(":") + 2, body.length() - 2);

            response.getWriter().write(new Data(author, message).toString());
            switch (resource.transport()) {
                case JSONP:
                case AJAX:
                case LONG_POLLING:
                    event.getResource().resume();
                    break;
                default:
                    response.getWriter().flush();
                    break;
            }   
        } else if (!event.isResuming()) {// remote connection got closed by proxy or browser
            LOG.info(">>>> Event wasn't resuming.");
            Data message = new Data("Someone","say bye bye!");
            event.broadcaster().broadcast(message);
        }
    }
    
    private final static class Data {

        private final String text;
        private final String author;

        public Data(String author, String text) {
            this.author = author;
            this.text = text;
        }

        @Override
        public String toString() {
            return "{ \"text\" : \"" + text + "\", \"author\" : \"" + author + "\" , \"time\" : " + new Date().getTime() + "}";
        }
    }

}
