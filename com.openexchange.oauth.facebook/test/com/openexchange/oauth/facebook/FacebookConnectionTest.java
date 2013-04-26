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

package com.openexchange.oauth.facebook;

import java.net.URLEncoder;
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

import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.oauth.facebook.osgi.Activator;


/**
 * {@link FacebookConnectionTest}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class FacebookConnectionTest  extends TestCase {

    private FacebookServiceImpl facebook;

    @Override
    public void setUp(){
        final Activator activator = new Activator();
        facebook = new FacebookServiceImpl(new MockOAuthService(), new OAuthServiceMetaDataFacebookImpl(null));
    }

    private static final String NETWORK_NAME = "Facebook";
    private static final String PROTECTED_RESOURCE_URL = "https://graph.facebook.com/me/friends";
    private static final String NO_SECRET_NEEDED = "";
    private static final Token EMPTY_TOKEN = null;

    public void testAccountCreation(){
        // This is basically scribes example
        final String apiKey = "842495cb6b4d939cf998fd9239b5fe6f";
        final String apiSecret = "9876bb887fac4be7518055b487eda4d9";
        final OAuthService service = new ServiceBuilder()
                                      .provider(FacebookApi.class)
                                      .apiKey(apiKey)
                                      .apiSecret(apiSecret)
                                      .callback("http://localhost/~karstenwill/")
                                      .build();
        final Scanner in = new Scanner(System.in);

        System.out.println("=== " + NETWORK_NAME + "'s OAuth Workflow ===");
        System.out.println();

        // Obtain the Authorization URL
        System.out.println("Fetching the Authorization URL...");
        final String authorizationUrl = service.getAuthorizationUrl(EMPTY_TOKEN);
        System.out.println("Got the Authorization URL!");
        System.out.println("Now go and authorize Scribe here:");
        System.out.println(authorizationUrl+"&scope=friends_birthday,friends_work_history,friends_about_me,friends_hometown,offline_access");
        System.out.println("And paste the access token here");
        System.out.print(">>");
        final Token accessToken = new Token(in.nextLine(), NO_SECRET_NEEDED);
        System.out.println();

        // Now let's go and ask for a protected resource!
        System.out.println("Now we're going to access a protected resource...");
        final OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL);
        service.signRequest(accessToken, request);
        final Response response = request.send();
        System.out.println("Got it! Lets see what we found...");
        System.out.println();
        System.out.println(response.getCode());
        System.out.println(response.getBody());

        try {
            final JSONObject jsonObject = new JSONObject(response.getBody());
            final JSONArray array = jsonObject.getJSONArray("data");

            for (int i = 0; i < array.length() ; i++){
                final JSONObject object = array.getJSONObject(i);
                System.out.println(object.get("id"));
            }
        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        final OAuthRequest request2 = new OAuthRequest(Verb.GET, "https://graph.facebook.com/me");
        service.signRequest(accessToken, request2);
        final Response response2 = request2.send();
        System.out.println("Got it! Lets see what we found...");
        System.out.println();
        System.out.println(response2.getCode());
        System.out.println(response2.getBody());

        String myuid = "";
        try {
            final JSONObject object = new JSONObject(response2.getBody());
            myuid = object.getString("id");
            System.out.println("My uid : " + myuid);
        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        final OAuthRequest request3 = new OAuthRequest(Verb.GET, "https://api.facebook.com/method/fql.query?query=SELECT%20name,first_name,last_name,email,birthday_date,pic_big,hometown_location%20from%20user%20where%20uid%20in%20%28SELECT%20uid2%20from%20friend%20where%20uid1="+myuid+"%29&format=JSON");
        service.signRequest(accessToken, request3);
        final Response response3 = request3.send();
        System.out.println("Got it! Lets see what we found...");
        System.out.println();
        System.out.println(response3.getCode());
        System.out.println(response3.getBody());

        try {
            final JSONArray allConnections = new JSONArray(response3.getBody());
            for (int i=0; i < allConnections.length(); i++){
                final JSONObject connection = allConnections.getJSONObject(i);
                System.out.println("---");
                System.out.println(connection.get("first_name"));
                System.out.println(connection.get("last_name"));
                System.out.println(connection.get("email"));
                System.out.println(connection.get("birthday_date"));
                System.out.println(connection.get("pic_big"));
                System.out.println(connection.get("hometown_location"));
            }
        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        System.out.println();
        System.out.println("Thats it man! Go and build something awesome with Scribe! :)");
    }

    public void testUsageOfExistingAccount() throws OXException{
        final List<Contact> contacts = facebook.getContacts(null,1,1,1);
        for (final Contact contact : contacts){
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
        final String apiKey = "842495cb6b4d939cf998fd9239b5fe6f";
        final String apiSecret = "9876bb887fac4be7518055b487eda4d9";
        final OAuthService service = new ServiceBuilder()
                                      .provider(FacebookApi.class)
                                      .apiKey(apiKey)
                                      .apiSecret(apiSecret)
                                      .callback("http://localhost/~karstenwill/?parameter=value")
                                      .build();
        final Scanner in = new Scanner(System.in);

        System.out.println("=== " + NETWORK_NAME + "'s OAuth Workflow ===");
        System.out.println();

        // Obtain the Authorization URL
        System.out.println("Fetching the Authorization URL...");
        final String authorizationUrl = "https://graph.facebook.com/oauth/authorize?client_id=842495cb6b4d939cf998fd9239b5fe6f&redirect_uri=http://localhost/~karstenwill/?parameter=value";
        System.out.println("Got the Authorization URL!");
        System.out.println("Now go and authorize Scribe here:");
        System.out.println(authorizationUrl+"&scope=friends_birthday,friends_work_history,friends_about_me,friends_hometown,offline_access");
        System.out.println("And paste the code here");
        System.out.print(">>");
        final String code = in.nextLine();

        System.out.println("Now enter this in your browser:");
        final String string = "https://graph.facebook.com/oauth/access_token?client_id="+apiKey+"&redirect_uri="+URLEncoder.encode("http://localhost/~karstenwill/?parameter=value")+"&client_secret="+apiSecret+"&code="+code;
        System.out.println(string);
        System.out.println("And enter the access token here:");
        System.out.print(">>");
        final Token accessToken = new Token(in.nextLine(), NO_SECRET_NEEDED);
        // Now let's go and ask for a protected resource!
        System.out.println("Now we're going to access a protected resource...");
        final OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL);

        service.signRequest(accessToken, request);
        final Response response = request.send();
        System.out.println("Got it! Lets see what we found...");
        System.out.println();
        System.out.println(response.getCode());
        System.out.println(response.getBody());
    }

    // for more detailed information please see http://developers.facebook.com/docs/reference/api/
    public void testGetWall(){
        final String apiKey = "842495cb6b4d939cf998fd9239b5fe6f";
        final String apiSecret = "9876bb887fac4be7518055b487eda4d9";
        final OAuthService service = new ServiceBuilder()
                                      .provider(FacebookApi.class)
                                      .apiKey(apiKey)
                                      .apiSecret(apiSecret)
                                      .callback("http://localhost/~karstenwill/")
                                      .build();
        final MockOAuthService mock = new MockOAuthService();
        // get the wall
        final Token accessToken = new Token(mock.getAccount(1, null, 1, 1).getToken(),"");

        //find out the uid of the user
        final OAuthRequest request2 = new OAuthRequest(Verb.GET, "https://graph.facebook.com/me");
        service.signRequest(accessToken, request2);
        final Response response2 = request2.send();
        System.out.println("Got it! Lets see what we found...");
        System.out.println();
        System.out.println(response2.getCode());
        System.out.println(response2.getBody());
        String myuid = "";
        try {
            final JSONObject object = new JSONObject(response2.getBody());
            myuid = object.getString("id");
            System.out.println("My uid : " + myuid);
        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Now let's go and ask for a protected resource!
        System.out.println("Now we're going to access the feed");
        final OAuthRequest request = new OAuthRequest(Verb.GET, "https://graph.facebook.com/"+myuid+"/feed");

        service.signRequest(accessToken, request);
        final Response response = request.send();
        System.out.println("This is what we get back");
        System.out.println(response.getBody());
    }

    // for more detailed information please see http://developers.facebook.com/docs/reference/api/
    public void testPostToWall(){
//        You can publish to the Facebook graph by issuing HTTP POST requests to the appropriate connection URLs, using an user access token or an app access token (for Open Graph Pages). For example, you can post a new wall post on Arjun's wall by issuing a POST request to https://graph.facebook.com/arjun/feed:
//
//        curl -F 'access_token=...' \
//             -F 'message=Hello, Arjun. I like this new API.' \
//             https://graph.facebook.com/arjun/feed
        final String apiKey = "842495cb6b4d939cf998fd9239b5fe6f";
        final String apiSecret = "9876bb887fac4be7518055b487eda4d9";
        final OAuthService service = new ServiceBuilder()
                                      .provider(FacebookApi.class)
                                      .apiKey(apiKey)
                                      .apiSecret(apiSecret)
                                      .callback("http://localhost/~karstenwill/")
                                      .build();
        final MockOAuthService mock = new MockOAuthService();
        // get the wall
        final Token accessToken = new Token(mock.getAccount(1, null, 1, 1).getToken(),"");

        //find out the uid of the user
        final OAuthRequest request2 = new OAuthRequest(Verb.GET, "https://graph.facebook.com/me");
        service.signRequest(accessToken, request2);
        final Response response2 = request2.send();
        System.out.println("Got it! Lets see what we found...");
        System.out.println();
        System.out.println(response2.getCode());
        System.out.println(response2.getBody());
        String myuid = "";
        try {
            final JSONObject object = new JSONObject(response2.getBody());
            myuid = object.getString("id");
            System.out.println("My uid : " + myuid);
        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Now let's go and ask for a protected resource!
        System.out.println("Now we're going to post something to the wall feed");
        final String message = "This is my new, dynamic test-post. Enjoy!";
        final OAuthRequest request = new OAuthRequest(Verb.POST, "https://graph.facebook.com/"+myuid+"/feed"+"?message="+URLEncoder.encode(message));

        service.signRequest(accessToken, request);
        final Response response = request.send();
        System.out.println("This is what we get back");
        System.out.println(response.getBody());
    }

    public void testFQLQuery(){
        final String apiKey = "842495cb6b4d939cf998fd9239b5fe6f";
        final String apiSecret = "9876bb887fac4be7518055b487eda4d9";
        final OAuthService service = new ServiceBuilder()
                                      .provider(FacebookApi.class)
                                      .apiKey(apiKey)
                                      .apiSecret(apiSecret)
                                      .build();
        final MockOAuthService mock = new MockOAuthService();
        // get the wall
        final Token accessToken = new Token(mock.getAccount(1, null, 1, 1).getToken(),"");

        //find out the uid of the user
        final OAuthRequest request2 = new OAuthRequest(Verb.GET, "https://graph.facebook.com/me");
        service.signRequest(accessToken, request2);
        final Response response2 = request2.send();
        System.out.println("Got it! Lets see what we found...");
        System.out.println();
        System.out.println(response2.getCode());
        System.out.println(response2.getBody());
        String myuid = "";
        try {
            final JSONObject object = new JSONObject(response2.getBody());
            myuid = object.getString("id");
            System.out.println("My uid : " + myuid);
        } catch (final JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Now let's go and ask for a protected resource!
        System.out.println("Now we're going to execute a FQL query");
        final OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.facebook.com/method/fql.query?format=XML&query=" + URLEncoder.encode(("SELECT wall_count FROM user WHERE uid = " + myuid)));
        service.signRequest(accessToken, request);
        final Response response = request.send();
        System.out.println("This is what we get back");
        System.out.println(response.getBody());
    }

}
