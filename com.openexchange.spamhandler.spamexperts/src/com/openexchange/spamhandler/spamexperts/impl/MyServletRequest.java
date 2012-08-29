package com.openexchange.spamhandler.spamexperts.impl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.session.Session;
import com.openexchange.spamhandler.spamexperts.osgi.MyServiceRegistry;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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


/**
 * This request handler currently know these actions:
 * 
 * 
 */
public final class MyServletRequest  {

	private final Session sessionObj;
	private User user;	
	private final Context ctx;
	private final ConfigurationService configservice;
	
	private static final HttpClient HTTPCLIENT;

    static {
            final MultiThreadedHttpConnectionManager manager = new MultiThreadedHttpConnectionManager();
            final HttpConnectionManagerParams params = manager.getParams();
            params.setMaxConnectionsPerHost(HostConfiguration.ANY_HOST_CONFIGURATION, 23);
            HTTPCLIENT = new HttpClient(manager);
    }

	
	
	private static final Log LOG = com.openexchange.log.Log.loggerFor(MyServletRequest.class);
	
	public static final String ACTION_GET_NEW_PANEL_SESSION = "generate_panel_session";
	
	// config options
	private static final String PROPERTY_PANEL_API_ADMIN_USER = "com.openexchange.custom.spamexperts.panel.admin_user";
	private static final String PROPERTY_PANEL_API_ADMIN_PASSWORD = "com.openexchange.custom.spamexperts.panel.admin_password";
	private static final String PROPERTY_PANEL_API_URL = "com.openexchange.custom.spamexperts.panel.api_interface_url";
	private static final String PROPERTY_PANEL_API_AUTH_ATTRIBUTE = "com.openexchange.custom.spamexperts.panel.api_auth_attribute";
	public MyServletRequest(final Session sessionObj, final Context ctx) throws OXException {		
		this.sessionObj = sessionObj;
		this.ctx = ctx;
		try {
			this.user = UserStorage.getInstance().getUser(sessionObj.getUserId(), ctx);
		} catch (final OXException e) {
			LOG.error(e.getMessage(), e);
			throw e;
		}		
		
		// init config 
		this.configservice = MyServiceRegistry.getServiceRegistry().getService(ConfigurationService.class,true); 
	}
	
	public Object action(final String action, final JSONObject jsonObject) throws OXException, JSONException {
		Object retval = null;
		
		if(action.equalsIgnoreCase(ACTION_GET_NEW_PANEL_SESSION)){
			// create new panel session id and return it
			retval = actionGetNewPanelSession(jsonObject);
		}else{
			throw AjaxExceptionCodes.UNKNOWN_ACTION.create(action);
		}
		
		return retval;
	}
	
	/**
	 * Returns the spamexperts panel session ID 
	 * Needed by UI to redirect to Panel
	 * 
	 * @param jsonObject
	 * @return
	 * @throws AbstractOXException
	 * @throws JSONException
	 */
	private JSONObject actionGetNewPanelSession(final JSONObject jsonObject) throws OXException, JSONException {
		// our response object for the client
		final JSONObject jsonResponseObject = new JSONObject();
				
		try {
			
			String sessionid = null;
			
			LOG.debug("trying to create new spamexperts panel session for user "+getCurrentUserUsername()+" in context "+getCurrentUserContextID());
			// create complete new session id
			sessionid = createPanelSessionID();
			LOG.debug("new spamexperts panel session created for user "+getCurrentUserUsername()+" in context "+getCurrentUserContextID());
			
			
			if(sessionid==null){
				throw MyServletExceptionCode.SPAMEXPERTS_COMMUNICATION_ERROR.create("save and cache session", "invalid data for sessionid");      
			}
			
			// add valid data to response
			// UI Plugin will format session and URL and redirect
			jsonResponseObject.put("panel_session",sessionid); // send session id
			jsonResponseObject.put("panel_web_ui_url",getFromConfig("com.openexchange.custom.spamexperts.panel.web_ui_url")); // send UI URL 
			
		} catch (final URIException e) {
			LOG.error("error creating uri object of spamexperts api interface", e);
			throw MyServletExceptionCode.HTTP_COMMUNICATION_ERROR.create(e.getMessage());      
		} catch (final HttpException e) {
			LOG.error("http communication error detected with spamexperts api interface", e);
			throw MyServletExceptionCode.HTTP_COMMUNICATION_ERROR.create(e.getMessage());      
		} catch (final IOException e) {
			LOG.error("IO error occured while communicating with spamexperts api interface");
			throw MyServletExceptionCode.HTTP_COMMUNICATION_ERROR.create(e.getMessage());      
		}finally{
			// add clean up stuff for http client here	
		}

		return jsonResponseObject;
	}
	
	
	private String reponse2String(final HttpMethodBase httpmethod) throws IOException {
		 final Reader reader = new InputStreamReader(httpmethod.getResponseBodyAsStream(), httpmethod.getResponseCharSet());
		 return IOUtils.toString(reader);
   }

	private static String AUTH_ID_MAIL ="mail";
	private static String AUTH_ID_LOGIN ="login";
	private static String AUTH_ID_IMAP_LOGIN ="imaplogin";
	private static String AUTH_ID_USERNAME ="username";
	
	private String createPanelSessionID() throws OXException, JSONException, IOException {
		
		String authid = null; // FALLBACK IS MAIL
		
		String authid_attribute = getAuthIDAttribute();
		if(authid_attribute==null || authid_attribute.trim().length()==0){
			authid_attribute = AUTH_ID_MAIL;
			if(LOG.isDebugEnabled()){
				LOG.debug("Using "+authid_attribute+" from user "+getCurrentUserUsername()+" in context "+getCurrentUserContextID()+" as authentication attribute against panel API");
			}
		}
		
		if(authid_attribute.equals(AUTH_ID_IMAP_LOGIN)){
			authid = this.user.getImapLogin();
		}else if(authid_attribute.equals(AUTH_ID_LOGIN)){
			authid = this.sessionObj.getLogin();
		}else if (authid_attribute.equals(AUTH_ID_MAIL)){
			authid = this.user.getMail();
		}else if (authid_attribute.equals(AUTH_ID_USERNAME)){
			authid = this.sessionObj.getUserlogin();
		}else {
			authid = this.user.getMail();
		}
		
		if(LOG.isDebugEnabled()){
			LOG.debug("Using "+authid+" as authID string from user "+getCurrentUserUsername()+" in context "+getCurrentUserContextID()+" to authenticate against panel API");
		}
		
		// call the API to retrieve the URL to access panel
        final GetMethod GET = new GetMethod(getPanelApiURL()+authid);
        // send request
        
        HTTPCLIENT.getState().setCredentials(
                        new AuthScope(GET.getURI().getHost(), 80, "API user authentication"),
                        new UsernamePasswordCredentials(getPanelApiAdminUser(), getPanelApiAdminPassword())
        );
		
		try {
			
			final int statusCode = HTTPCLIENT.executeMethod(GET);
			
			if (statusCode != HttpStatus.SC_OK) {
				LOG.error("HTTP request to create new spamexperts panel session failed with status: " +GET.getStatusLine());
				throw MyServletExceptionCode.SPAMEXPERTS_COMMUNICATION_ERROR.create("create panel authticket", GET.getStatusLine());      
			}
			
			final String resp = reponse2String(GET);
			if(LOG.isDebugEnabled()){
				LOG.debug("Got response for user "+getCurrentUserUsername()+" in context "+getCurrentUserContextID()+" from  panel API: \n"+resp);
			}
			
			if(resp.indexOf("ERROR")!=-1){
				// ERROR DETECTED
				throw MyServletExceptionCode.SPAMEXPERTS_COMMUNICATION_ERROR.create("create panel authticket", resp);      
			}
			
			return resp;	
		} catch (final IllegalArgumentException e){
			LOG.error("error in http communication", e);
			throw e;
		} catch (final HttpException e) {
			LOG.error("error in http communication", e);
			throw e;
		} catch (final IOException e) {
			LOG.error("IO error in http communication", e);
			throw e;
        } finally {
            if (GET != null) {
                GET.releaseConnection();
            }
        }
        
	}
	
	
	private String getPanelApiURL() throws OXException{
		return getFromConfig(PROPERTY_PANEL_API_URL);
	}
	
	private String getAuthIDAttribute() throws OXException{
		return getFromConfig(PROPERTY_PANEL_API_AUTH_ATTRIBUTE);
	}
	
	private String getPanelApiAdminUser() throws OXException{
		return getFromConfig(PROPERTY_PANEL_API_ADMIN_USER);
	}
	
	private String getPanelApiAdminPassword() throws OXException{
		return getFromConfig(PROPERTY_PANEL_API_ADMIN_PASSWORD);
	}

	private String getFromConfig(final String key) throws OXException{		
		return this.configservice.getProperty(key); 
	}
	
	private String getCurrentUserUsername(){		
		return this.sessionObj.getLogin();
	}
	
	private int getCurrentUserContextID(){
		return this.sessionObj.getContextId();
	}	
	
	private String getCurrentUserPassword(){
		return this.sessionObj.getPassword();
	}

}
