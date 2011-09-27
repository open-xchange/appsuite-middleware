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

package com.openexchange.oauth.linkedin;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.LinkedInApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.linkedin.osgi.Activator;

/**
 * {@link LinkedInServiceImpl}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class LinkedInServiceImpl implements LinkedInService{

    private static final String PERSONAL_FIELDS = "id,first-name,last-name,phone-numbers,headline,im-accounts,twitter-accounts,date-of-birth,main-address,picture-url,positions,industry";
	private static final String PERSONAL_FIELD_QUERY = ":("+PERSONAL_FIELDS+")";
	private static final String CONNECTIONS_URL = "http://api.linkedin.com/v1/people/~/connections:(id,first-name,last-name,phone-numbers,im-accounts,twitter-accounts,date-of-birth,main-address,picture-url,positions)";
    private static final String IN_JSON = "?format=json";
    
    private Activator activator;

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(LinkedInServiceImpl.class));

    
    public LinkedInServiceImpl(Activator activator) {
        this.activator = activator;
    }

    public Activator getActivator() {
        return activator;
    }

    public void setActivator(Activator activator) {
        this.activator = activator;
    }
    
    
    public Response performRequest(String password, int user, int contextId, int accountId, Verb method, String url) {
        OAuthServiceMetaData linkedInMetaData = new OAuthServiceMetaDataLinkedInImpl(activator);

        OAuthService service = new ServiceBuilder().provider(LinkedInApi.class).apiKey(linkedInMetaData.getAPIKey()).apiSecret(
            linkedInMetaData.getAPISecret()).build();

        OAuthAccount account = null;
        try {
            com.openexchange.oauth.OAuthService oAuthService = activator.getOauthService();
            account = oAuthService.getAccount(accountId, password, user, contextId);
        } catch (OXException e) {
            LOG.error(e);
        }

        Token accessToken = new Token(account.getToken(), account.getSecret());
        OAuthRequest request = new OAuthRequest(method, url);
        service.signRequest(accessToken, request);
        return request.send();
    }
    
    
	private JSONObject extractJson(Response response) throws OXException {
		JSONObject json;
		try {
			json = new JSONObject(response.getBody());
		} catch (JSONException e) {
			throw OXException.general("Could not parse JSON: " + response.getBody()); //TODO: Different exception - wasn't this supposed to get easier with the rewrite? 
		}
        return json;
	}
	

	protected List<String> extractIds(Response response) throws OXException{
		List<String> result = new LinkedList<String>();
		try {
			JSONObject json = new JSONObject(response.getBody());
			JSONArray ids = json.getJSONArray("values");
			result = extractIds(ids);
		} catch (JSONException e) {
			
		}
		return result;
	}
	
	protected List<String> extractIds(JSONArray connections) throws OXException{
		List<String> result = new LinkedList<String>();
		try {
			for(int i = 0, max = connections.length(); i < max; i++){
				result.add(connections.getJSONObject(i).getString("id"));
			}
		} catch (JSONException e) {
			
		}
		return result;
	}
	
	
    @Override
    public String getAccountDisplayName(String password, int user, int contextId, int accountId) {
        String displayName="";
        try {
            com.openexchange.oauth.OAuthService oAuthService = activator.getOauthService();
            OAuthAccount account = oAuthService.getAccount(accountId, password, user, contextId);
            displayName = account.getDisplayName();
        } catch (OXException e) {
            LOG.error(e);
        }
        return displayName;
    }
    
    
    @Override
    public List<Contact> getContacts(String password, int user, int contextId, int accountId) {
    	Response response = performRequest(password, user, contextId, accountId, Verb.GET, CONNECTIONS_URL);
    	LinkedInXMLParser parser = new LinkedInXMLParser();
        List<Contact> contacts = parser.parseConnections(response.getBody());
        return contacts;
    }
    

	@Override
	public JSONObject getProfileForId(String id, String password, int user, int contextId, int accountId) throws OXException {
		String uri = "http://api.linkedin.com/v1/people/id="+id+PERSONAL_FIELD_QUERY;
	   	Response response = performRequest(password, user, contextId, accountId, Verb.GET, uri + IN_JSON);
    	return extractJson(response);
	}
	

	@Override
	public JSONObject getRelationToViewer(String id, String password, int user, int contextId, int accountId) throws OXException {
		String uri = "http://api.linkedin.com/v1/people/id="+id+":(relation-to-viewer)";
	   	Response response = performRequest(password, user, contextId, accountId, Verb.GET, uri + IN_JSON);
    	JSONObject relations = extractJson(response);
    	return relations;
	}
	
	@Override
	public JSONObject getConnections(String password, int user, int contextId,	int accountId) throws OXException {
		String uri = "http://api.linkedin.com/v1/people/~/connections"+PERSONAL_FIELD_QUERY;
		Response response = performRequest(password, user, contextId, accountId, Verb.GET, uri + IN_JSON);
		return extractJson(response);
	}
	
	
	@Override
	public List<String> getUsersConnectionsIds(String password, int user, int contextId, int accountId) throws OXException {
		String uri = "http://api.linkedin.com/v1/people/~/connections:(id)";
		Response response = performRequest(password, user, contextId, accountId, Verb.GET, uri + IN_JSON);
		return extractIds(response);
	}

    
    @Override
    public JSONObject getFullProfileByEMail(String email, String password, int user, int contextId, int accountId) throws OXException{
    	//Implemented as dummy, because LinkedIn has not upgraded our keys yet to do this");
    	String id = "hzFnTZPLsz";
		String uri = "http://api.linkedin.com/v1/people/id="+id+":(relation-to-viewer,"+PERSONAL_FIELDS+")";
	   	Response response = performRequest(password, user, contextId, accountId, Verb.GET, uri + IN_JSON);
    	JSONObject data = extractJson(response);
    	addFullInformationToRelation(data, password, user, contextId, accountId);
    	return data;
    }
    
    
	private void addFullInformationToRelation(JSONObject relation2, String password, int user, int contextId, int accountId) throws OXException {
		try {
			JSONObject relation = relation2.getJSONObject("relationToViewer");
			JSONArray connections = relation.getJSONObject("connections").getJSONArray("values");
			for(int i = 0, max = connections.length(); i < max; i++){
				JSONObject contact = connections.getJSONObject(i);
				if(!contact.has("person")) continue;
				
				JSONObject person = contact.getJSONObject("person");
				String id = person.getString("id");
				JSONObject fullProfile = getProfileForId(id, password, user, contextId, accountId);
				contact.put("person",fullProfile);
			}

		} catch (JSONException e) {
			throw new OXException(1).setPrefix("OAUTH-LI").setLogMessage("Could not parse JSON");
		}
		
	}

}
