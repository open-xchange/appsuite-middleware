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

package com.openexchange.oauth.facebook;

import java.util.List;
import java.util.Scanner;
import junit.framework.TestCase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.FacebookApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;
import com.openexchange.groupware.container.Contact;
import com.openexchange.oauth.facebook.osgi.FacebookOAuthActivator;


/**
 * {@link FacebookConnectionTest}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class FacebookConnectionTest  extends TestCase {
    
    private FacebookServiceImpl facebook;
    
    public void setUp(){
        FacebookOAuthActivator activator = new FacebookOAuthActivator();
//        OAuthServiceMetaDataLinkedInImpl linkedInMetadata = new OAuthServiceMetaDataLinkedInImpl();
//        activator.setLinkedInMetadata(linkedInMetadata);
        facebook = new FacebookServiceImpl(activator);
        activator.setOauthService(new MockOAuthService());
    }
    
    private static final String NETWORK_NAME = "Facebook";
    private static final String PROTECTED_RESOURCE_URL = "https://graph.facebook.com/me/friends";
    private static final String NO_SECRET_NEEDED = "";
    private static final Token EMPTY_TOKEN = null;
    
    public void testAccountCreation(){
        // This is basically scribes example
        String apiKey = "842495cb6b4d939cf998fd9239b5fe6f";
        String apiSecret = "9876bb887fac4be7518055b487eda4d9";
        OAuthService service = new ServiceBuilder()
                                      .provider(FacebookApi.class)
                                      .apiKey(apiKey)
                                      .apiSecret(apiSecret)
                                      .callback("http://localhost/~karstenwill/")
                                      .build();
        Scanner in = new Scanner(System.in);

        System.out.println("=== " + NETWORK_NAME + "'s OAuth Workflow ===");
        System.out.println();

        // Obtain the Authorization URL
        System.out.println("Fetching the Authorization URL...");
        String authorizationUrl = service.getAuthorizationUrl(EMPTY_TOKEN);
        System.out.println("Got the Authorization URL!");
        System.out.println("Now go and authorize Scribe here:");
        System.out.println(authorizationUrl+"&scope=friends_birthday,friends_work_history,friends_about_me,friends_hometown");
        System.out.println("And paste the access token here");
        System.out.print(">>");
        Token accessToken = new Token(in.nextLine(), NO_SECRET_NEEDED);
        System.out.println();

        // Now let's go and ask for a protected resource!
        System.out.println("Now we're going to access a protected resource...");
        OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL);
        service.signRequest(accessToken, request);
        Response response = request.send();
        System.out.println("Got it! Lets see what we found...");
        System.out.println();
        System.out.println(response.getCode());
        System.out.println(response.getBody());
        
        try {
            JSONObject jsonObject = new JSONObject(response.getBody());
            JSONArray array = jsonObject.getJSONArray("data");
            
            for (int i = 0; i < array.length() ; i++){
                JSONObject object = array.getJSONObject(i);
                System.out.println(object.get("id"));
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        OAuthRequest request2 = new OAuthRequest(Verb.GET, "https://graph.facebook.com/me");
        service.signRequest(accessToken, request2);
        Response response2 = request2.send();
        System.out.println("Got it! Lets see what we found...");
        System.out.println();
        System.out.println(response2.getCode());
        System.out.println(response2.getBody());
        
        String myuid = "";
        try {
            JSONObject object = new JSONObject(response2.getBody());
            myuid = object.getString("id");
            System.out.println("My uid : " + myuid);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
        OAuthRequest request3 = new OAuthRequest(Verb.GET, "https://api.facebook.com/method/fql.query?query=SELECT%20name,first_name,last_name,email,birthday_date,pic_big,hometown_location%20from%20user%20where%20uid%20in%20%28SELECT%20uid2%20from%20friend%20where%20uid1="+myuid+"%29&format=JSON");
        service.signRequest(accessToken, request3);
        Response response3 = request3.send();
        System.out.println("Got it! Lets see what we found...");
        System.out.println();
        System.out.println(response3.getCode());
        System.out.println(response3.getBody());
        
        try {
            JSONArray allConnections = new JSONArray(response3.getBody());
            for (int i=0; i < allConnections.length(); i++){
                JSONObject connection = allConnections.getJSONObject(i);
                System.out.println("---");
                System.out.println(connection.get("first_name"));
                System.out.println(connection.get("last_name"));
                System.out.println(connection.get("email"));
                System.out.println(connection.get("birthday_date"));
                System.out.println(connection.get("pic_big"));
                System.out.println(connection.get("hometown_location"));
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        

        System.out.println();
        System.out.println("Thats it man! Go and build something awesome with Scribe! :)");
    }

    public void testUsageOfExistingAccount(){
        List<Contact> contacts = facebook.getContacts(1,1,1);
        for (Contact contact : contacts){
            System.out.println(contact.getGivenName() + " " + contact.getSurName());
            System.out.println(contact.getBirthday());
            System.out.println(contact.getPostalCodeHome());
            System.out.println(contact.getCityHome());
            System.out.println(contact.getCountryHome());
            System.out.println(contact.getImageContentType());
            System.out.println("---");
        }
    }
    
    public void testNewWorkflow(){
     // This is basically scribes example
        String apiKey = "842495cb6b4d939cf998fd9239b5fe6f";
        String apiSecret = "9876bb887fac4be7518055b487eda4d9";
        OAuthService service = new ServiceBuilder()
                                      .provider(FacebookApi.class)
                                      .apiKey(apiKey)
                                      .apiSecret(apiSecret)
                                      .callback("http://localhost/~karstenwill/")
                                      .build();
        Scanner in = new Scanner(System.in);

        System.out.println("=== " + NETWORK_NAME + "'s OAuth Workflow ===");
        System.out.println();

        // Obtain the Authorization URL
        System.out.println("Fetching the Authorization URL...");
        String authorizationUrl = "https://graph.facebook.com/oauth/authorize?client_id=842495cb6b4d939cf998fd9239b5fe6f&redirect_uri=http://localhost/~karstenwill/";
        System.out.println("Got the Authorization URL!");
        System.out.println("Now go and authorize Scribe here:");
        System.out.println(authorizationUrl+"&scope=friends_birthday,friends_work_history,friends_about_me,friends_hometown");
        System.out.println("And paste the code here");
        System.out.print(">>");
        String code = in.nextLine();
        
        System.out.println("Now enter this in your browser:");
        
        System.out.println("https://graph.facebook.com/oauth/access_token?client_id="+apiKey+"&redirect_uri=http://localhost/~karstenwill/&client_secret="+apiSecret+"&code=" + code);
        System.out.println("And enter the access token here:");
        System.out.print(">>");
        Token accessToken = new Token(in.nextLine(), NO_SECRET_NEEDED);
        // Now let's go and ask for a protected resource!
        System.out.println("Now we're going to access a protected resource...");
        OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL);
        
        service.signRequest(accessToken, request);
        Response response = request.send();
        System.out.println("Got it! Lets see what we found...");
        System.out.println();
        System.out.println(response.getCode());
        System.out.println(response.getBody());
    }
}
