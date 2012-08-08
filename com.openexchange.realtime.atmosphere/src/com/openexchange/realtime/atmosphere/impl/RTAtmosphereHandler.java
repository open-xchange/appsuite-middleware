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

package com.openexchange.realtime.atmosphere.impl;

import java.io.IOException;
import org.atmosphere.cpr.AtmosphereHandler;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResponse;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.cpr.MetaBroadcaster;
import org.atmosphere.websocket.WebSocketEventListenerAdapter;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.log.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.realtime.atmosphere.OXRTHandler;
import com.openexchange.realtime.atmosphere.StanzaSender;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.util.IDMap;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link RTAtmosphereHandler} - Handler that gets associated with the
 * {@link RTAtmosphereChannel} and does the main work of handling incoming and
 * outgoing Stanzas. Transformation of Stanzas is handed over to the proper
 * OXRTHandler 
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> JavaDoc
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class RTAtmosphereHandler implements AtmosphereHandler, StanzaSender {
	// TODO: Figure Out JSONP and Long-Polling State management. Hot-Swap the
	// AtmosphereResource.
	// TODO: Close connections and get rid of em
    // TODO: investigate why grizzly's responseimls are suddenly invalidated
    
    private static final org.apache.commons.logging.Log LOG = Log.valueOf(LogFactory.getLog(RTAtmosphereHandler.class));
	private final ServiceLookup services;
	private final HandlerLibrary library;
	// Keep track of ID <-> RTAtmosphereState associations
	private final IDMap<RTAtmosphereState> id2State;
	

	/**
	 * Initializes a new {@link RTAtmosphereHandler}.
	 * 
	 * @param library  The library to use for OXRTHandler lookups needed for
	 *                  transformations of incoming and outgoing stanzas
	 * @param services The service-lookup providing needed services
	 */
	public RTAtmosphereHandler(HandlerLibrary library, ServiceLookup services) {
	    super();
	    id2State = new IDMap<RTAtmosphereState>();
		this.library = library;
		this.services = services;
	}

	@Override
    public void destroy() {
	    // Ignore for now
	}
	
	@Override
	public void onStateChange(AtmosphereResourceEvent event) throws IOException {
	    AtmosphereResource resource = event.getResource();
        AtmosphereResponse response = resource.getResponse();
        
        //Did we suspend the AtmosphereResource earlier?
        if(event.isSuspended()) {
            response.getWriter().write(event.getMessage().toString());
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
        } else if (!event.isResuming()) {
            LOG.info("Event wasn't resuming remote connection got closed by proxy or browser.");
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
        // Log all events on the console, including WebSocket events for debugging
        resource.addEventListener(new WebSocketEventListenerAdapter());
        AtmosphereRequest request = resource.getRequest();
        AtmosphereResponse response = resource.getResponse();
        String method = request.getMethod();
        /*
         * GET requests can be handled via Continuations. Suspend the request 
         * and use it for bidirectional communication.
         * "negotiating" header is used to list all supported transports
         */
        if(method.equalsIgnoreCase("GET")) {
            //check headers and params for session infos, fail fast if invalid
            try {
                getServerSession(getSessionFromHeader(request), getSessionFromParameters(request));
            } catch (OXException e) {
                LOG.error(e);
                writeExceptionToResource(e, resource);
                return;
            }
            if(request.getHeader("negotiating") == null) {
                resource.suspend();
            } else {
                response.getWriter().write("OK");
            }
        /*
         * Use POST request to synchronously send data over the server.
         * First Post should contain handshake information -> getState 
         */
        } else if (method.equalsIgnoreCase("POST")) {
            String postData = request.getReader().readLine();
            if(postData != null) {
                try {
                    ServerSession serverSession = getServerSession(getSessionFromHeader(request), getSessionFromParameters(request), getSessionFromPostData(postData));
                    RTAtmosphereState atmosphereState = getState(resource, serverSession);
                    handleIncoming(StanzaParser.parse(postData), atmosphereState);
                } catch (OXException e) {
                    LOG.error(e);
                    writeExceptionToResource(e, resource);
                }
            }
        }
    }
    
    /**
     * Write a simple JSON error to the client.
     * @param exception the exception to send.
     * @param resource the resource representing the client
     * @throws IOException 
     */
    private void writeExceptionToResource(Exception exception, AtmosphereResource resource) throws IOException {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("error", exception.toString());
            resource.getResponse().getWriter().write(jsonObject.toString());
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

	/**
	 * Convert the session info into a ServerSession Object.   
     * @param sessionInfo The sessionInfo to convert
     * @return The ServerSession objectmatching the session infos
     * @throws IllegalArgumentException if the sessionInfo is null or empty 
	 * @throws OXException if an error happens while trying to build the
	 *                      ServerSession from the session infos  
     */
    private ServerSession getServerSessionFromInfo(String sessionInfo) throws OXException {
        if(sessionInfo == null || sessionInfo.isEmpty()) {
            throw new IllegalArgumentException("Invalid parameter: sessionInfo");
        }
        
        Session session = null;
        ServerSession serverSession = null;

        SessiondService sessiondService = services.getService(SessiondService.class);
        if(sessiondService == null) {
            throw OXExceptionFactory.getInstance().create(ServiceExceptionCode.SERVICE_UNAVAILABLE, SessiondService.class);
        }
        session = sessiondService.getSession(sessionInfo);
        if(session == null) {
            throw OXExceptionFactory.getInstance().create(SessionExceptionCodes.SESSION_EXPIRED, sessionInfo);
        }
        serverSession = new ServerSessionAdapter(session);
        return serverSession;
    }
    
    /**
     * Inspect the request headers for session information and return it.
     * @param request the request to inspect
     * @return null if no session info can be found, the session info otherwise
     */
    private String getSessionFromHeader(AtmosphereRequest request) {
        return request.getHeader("session");
    }
    
    /**
     * Inspect the request parameters for session information and return it.
     * @param request the request to inspect
     * @return null if no session info can be found, the session info otherwise
     */
    private String getSessionFromParameters(AtmosphereRequest request) {
        return request.getParameter("session");
    }
    
    /**
     * Inspect the request's post data for session information and return it.
     * @param postData the JSON String to inspect
     * @return null if no session info can be found, the session info otherwise
     * @throws OXException if the postData isn't valid JSON
     */
    private String getSessionFromPostData(String postData) throws OXException {
        JSONObject requestData;
        String sessionInfo = null;
        try {
            requestData = new JSONObject(postData);
            sessionInfo = requestData.optString("session");
        } catch (JSONException e) {
            throw OXExceptionFactory.getInstance().create(SessionExceptionCodes.SESSION_PARAMETER_MISSING);
        }
        return sessionInfo;
    }
    
    /**
     * Get a ServerSession object from a list of session infos submitted in the request.
     * Fails on the first non-null sessionInfo parameter that is invalid.
     * @param sessionInfo a list of session infos, nulls are allowed
     * @return The Serversession that matches the first given session info
     * @throws OXException if no matching ServerSession can be found
     */
    private ServerSession getServerSession(String... sessionInfo) throws OXException {
        ServerSession serverSession = null;
            for (int i=0; serverSession == null && i < sessionInfo.length; i++) {
                String currentSessionInfo = sessionInfo[i];
                if( currentSessionInfo != null && !currentSessionInfo.isEmpty()) {
                    serverSession = getServerSessionFromInfo(sessionInfo[i]);
                }
            }
        return serverSession;
    }

    /**
     * Check if an entity is connected to the associated Channel.
     * @param id the ID of the entity you are looking for.
     * @return true if the entity is connected, false otherwise
     */
    public boolean isConnected(ID id) {
        return id2State.containsKey(id.toGeneralForm());
    }
    
    @Override
    public void send(Stanza stanza) throws OXException {
        ID generalForm = stanza.getTo().toGeneralForm();
        RTAtmosphereState rtAtmosphereState = id2State.get(generalForm);
        try {
            rtAtmosphereState.atmosphereResource.getResponse().getWriter().write(StanzaWriter.write(stanza).toString());
            AtmosphereResource ar = rtAtmosphereState.atmosphereResource;
            if(ar.isSuspended()) {
                ar.resume();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
//        String stanzaAsJSON = StanzaWriter.write(stanza).toString();
//        String contextAndUser = generateBroadcasterId(stanza.getTo().toGeneralForm());
//        /*
//         * Broadcast stanza to all entities matching the user@context by
//         * using a wildcard for the resource: /user@context/*
//         */
//        MetaBroadcaster.getDefault().broadcastTo(contextAndUser+"/*", stanzaAsJSON);
        
    }
    
 /**
  * Check the AtmosphereResource for the session header/parameter and add it
  * to the uuid2state map that tracks Serversession <-> RTAtmosphereState
  * @param atmosphereResource the AtmosphereResource
  * @return null if the AtmosphereResource doesn't contain session
  * informations or an RTAtmosphereState that assembles the AtmosphereResource,
  * Serversession and ID
  * @throws OXException if the server session is missing from the
  * AtmosphereResource 
  */
 private RTAtmosphereState getState(AtmosphereResource atmosphereResource, ServerSession serverSession) throws OXException {
     RTAtmosphereState state = new RTAtmosphereState();
     state.session = serverSession;
     state.atmosphereResource = atmosphereResource;
     state.id = constructId(atmosphereResource, serverSession);
     return state;
 }
 
/**
 * Build an {@link ID} from the infos given by the AtmosphereResource and ServerSession 
 * @param atmosphereResource
 * @param serverSession
 * @return the constructed ID
 */
private ID constructId(AtmosphereResource atmosphereResource, ServerSession serverSession) {
    String userLogin = serverSession.getUserlogin();
    String contextName = getContextName(serverSession.getLogin());
    
    AtmosphereRequest request = atmosphereResource.getRequest();
    String resource = request.getHeader("resource");
    if (resource == null) {
        resource = request.getParameter("resource");
    }
    //TODO: think about proper unique resources later  
    if (resource == null) {
        resource = serverSession.getSessionID();
    }
    
    return new ID(RTAtmosphereChannel.PROTOCOL, userLogin, contextName, resource);
}

/**
 * Get context string from login string 
 * @param login the login string 
 * @return an empty string if no context can be found, the context oterwise
 */
private String getContextName(String login) {
    int index = login.indexOf('@');
    if (index < 0) {
        return "";
    }
    return login.substring(index + 1);
}
    
    /**
     * Handle incoming Stanza and decide if they have an internal namespace
     * and need to be handles by the Channel/Handler or if they should be 
     * dispatched iow. transformed to POJOs and handed over to the
     * MessageDispatcher.
     * @param stanza The Stanza to handle
     * @param atmosphereState The associated state
     * @throws OXException
     */
    protected void handleIncoming(Stanza stanza, RTAtmosphereState atmosphereState)
            throws OXException {
        if (isInternal(stanza)) {
            handleInternally(stanza, atmosphereState);
        } else {
            stampStanza(stanza, atmosphereState);
            dispatchStanza(stanza, atmosphereState);
        }
    }
    
	private boolean isInternal(Stanza stanza) {
		return stanza.getNamespace().startsWith("ox:");
	}

	/**
	 * Handle the Stanza internally instead of handing it over to the message
	 * dispatcher. Handshaking and other internal stuff happens here.
	 * @param stanza the incoming stanza
	 * @param serverSession the associated serverSession
	 * @throws OXException
	 */
	private void handleInternally(Stanza stanza, RTAtmosphereState atmosphereState) {
		if ("ox:handshake".equals(stanza.getNamespace())) {
		    // create new broadcaster for resource 
		    Broadcaster broadcaster = BroadcasterFactory.getDefault().get(generateBroadcasterId(atmosphereState.id));
			// add resource (connected client) to broadcaster
//		    broadcaster.addAtmosphereResource(atmosphereState.atmosphereResource);
		    atmosphereState.atmosphereResource.resumeOnBroadcast(true);
		    atmosphereState.atmosphereResource.setBroadcaster(broadcaster);
		   
		    //TODO: track connected user@context only basic representation for lookups, broadcast to all clients when available?
		    id2State.put(atmosphereState.id.toGeneralForm(), atmosphereState);
			/*
			 * TODO:
			 * configure broadcaster lifetime, schedule keepalive messages etc.  
			 */
		}
	}
	
	/**
	 * Create a broadcaster id from a given {@link ID}.
	 *
	 * Creates broadcaster ids like : /context/user or /context/user/resource
	 * depending if the given ID contains a resource.
	 * 
	 * This way we can address:
	 *  - single clients of a user identified by resource: /context/user/resource
	 *  - all clients of a user: /context/user/*
	 *  - all clients of all users in a context: /context/*
	 *  - all clients in all contexts: /*  
	 *
	 * @param id the id to use for generating the broadcaster id 
	 * @return the generated broadcaster id 
	 */
	private String generateBroadcasterId(ID id) {
	    StringBuilder sb = new StringBuilder();
        sb.append("/").append(id.getContext()).append("/").append(id.getUser());
        if(id.getResource() != null) {
            sb.append("/").append(id.getResource());
        }
	    return sb.toString();
	}

	/**
	 * Stamp the stanza, iow. set the sender of the stanza.  
	 * @param stanza the stanza to stamp
	 * @param state the associated atmosphereState
	 */
	private void stampStanza(Stanza stanza, RTAtmosphereState state) {
	    stanza.setFrom(state.id);
	}

	private void dispatchStanza(Stanza stanza, RTAtmosphereState atmosphereState)
			throws OXException {
		OXRTHandler transformer = library.getHandlerFor(stanza.getNamespace());
		if (transformer == null) {
			throw OXException.general("No transformer for namespace "
					+ stanza.getNamespace());
		}
		transformer.incoming(stanza, atmosphereState.session);
	}

	/**
	 * Handle outgoing Stanzas by transforming it into the proper representation
	 * and sending it to the addressed entity. 
	 * @param stanza the Stanza to send
	 * @param serverSession the associated ServerSession
	 * @throws OXException if no transformer for the given Stanza can be found
	 */
	public void handleOutgoing(Stanza stanza, ServerSession serverSession) throws OXException {
		OXRTHandler transformer = library.getHandlerFor(stanza.getNamespace());
		if (transformer == null) {
			throw OXException.general("No transformer for namespace "
					+ stanza.getNamespace());
		}
		/*
		 * Let the transformer handle the processing of the stanza.
		 * hand over this as reference for sending after transforming
		 */
		transformer.outgoing(stanza, serverSession, this);
	}

}
