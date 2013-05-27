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

package com.openexchange.oauth.msn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import junit.framework.TestCase;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.oauth.DefaultOAuthToken;
import com.openexchange.oauth.OAuthConstants;
import com.openexchange.oauth.msn.osgi.MSNOAuthActivator;

/**
 * {@link MSNConnectionTest}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class MSNConnectionTest extends TestCase{

    public void testMSNServiceImpl(){
       final String apiKey = "0000000040052F00";
       final String apiSecret = "zCH5gYyMcZz6blXGM5M44kC6N98OQ1Uc";
       final MSNOAuthActivator activator = new MSNOAuthActivator();
       OAuthServiceMetaDataMSNImpl serviceMetadata = new OAuthServiceMetaDataMSNImpl(null);
       activator.setOAuthMetadata(serviceMetadata);
       activator.setOauthService(new MockOAuthService(serviceMetadata));
       final MSNServiceImpl service = new MSNServiceImpl(activator);
       final List<Contact> contacts = service.getContacts(null, 0, 0, 0);
       System.out.println(contacts.size());
       //assertTrue("No. of contacts found should be > 0 ", contacts.size() > 0);
       for (final Contact contact : contacts){
           System.out.println("first name : " + contact.getGivenName());
           System.out.println("last name : " + contact.getSurName());
       }
    }

    /**
     * Deactivated because it needs manual interaction.
     */
    public void _testGetAccessTokenViaOAuthServiceMetaDataMSNImpl(){
        final String clientID = "0000000040052F00";
        final String clientSecret = "zCH5gYyMcZz6blXGM5M44kC6N98OQ1Uc";
        final String callbackURL = "http://www.open-xchange.com";
        final Scanner in = new Scanner(System.in);

        final String authURL = "https://consent.live.com/connect.aspx?wrap_client_id=" + clientID + "&wrap_callback=" + callbackURL + "&wrap_client_state=js_close_window&mkt=en-us&wrap_scope=WL_Profiles.View,WL_Contacts.View,Messenger.SignIn";
        System.out.println("Authorization URL (paste this into browser) : ");
        System.out.println(authURL);

        // Now lets try to get an access token with this wrap_verification_code ...
        System.out.println("paste the wrap_verification_code here");
        System.out.print(">>");
        final String wrap_verification_code = in.nextLine();

        final OAuthServiceMetaDataMSNImpl metadata = new OAuthServiceMetaDataMSNImpl(null);
        final HashMap<String, Object> arguments = new HashMap<String, Object>();
        arguments.put(OAuthConstants.ARGUMENT_PIN, wrap_verification_code);
        arguments.put(OAuthConstants.ARGUMENT_CALLBACK, callbackURL);
        try {
            metadata.getOAuthToken(arguments);
        } catch (final OXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void testURLLength(){
        final String longURL = "";
        final String shortURL = "";

       System.out.println("Length of long URL : " + longURL.length());
       System.out.println("Length of short URL : " + shortURL.length());
    }

    public void testHandlingOfInvalidSSLCertificate(){
        OutputStreamWriter writer = null;
        BufferedReader reader = null;
        try {


            final URL url = new URL("https://consent.live.com/AccessToken.aspx");
            final URLConnection connection = url.openConnection();
            connection.setConnectTimeout(2500);
            connection.setReadTimeout(2500);
            connection.setDoOutput(true);

            writer = new OutputStreamWriter(connection.getOutputStream());

            writer.flush();

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line = null;

            final DefaultOAuthToken token = new DefaultOAuthToken();
            token.setSecret("");

            while((line = reader.readLine()) != null) {
                final String[] keyValuePairs = line.split("&");
                for (final String keyValuePair : keyValuePairs) {
                    System.out.println(keyValuePair);
                }
            }

        } catch (final UnsupportedEncodingException x) {
            System.out.println(x.getMessage());
        } catch (final IOException e) {
            System.out.println(e.getMessage());
        }
    }

}
