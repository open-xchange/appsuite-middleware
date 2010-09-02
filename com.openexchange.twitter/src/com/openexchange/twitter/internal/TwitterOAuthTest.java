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

package com.openexchange.twitter.internal;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.http.RequestToken;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;


/**
 * {@link TwitterOAuthTest}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class TwitterOAuthTest {
    
    public static void main(String[] args){
        final Twitter twitter = new Twitter();
        
        String twitterId = "";
        String password = "";
        
        twitter.setOAuthConsumer("kZX3V4AmAWwC53yA5RjHbQ", "HvY7pVFFQSGPVu9LCoBMUhvMpS00qdtqBob99jucc");
        try {
            final RequestToken requestToken = twitter.getOAuthRequestToken();
            System.out.println(requestToken.getAuthorizationURL());
            
            
            String url = requestToken.getAuthorizationURL();
            String username = twitterId;            
            String nameOfUserField = "session[username_or_email]";
            String nameOfPasswordField = "session[password]";
            String actionOfLoginForm = "http://twitter.com/oauth/authorize";
            Pattern patternOfPin = Pattern.compile("oauth_pin\">(?:[^0-9]*)([0-9]*)");
            int numberOfForm = 1;
            
            try {
                BrowserVersion browser = BrowserVersion.FIREFOX_3;
                final WebClient webClient = new WebClient(browser);
                // Get the page, fill in the credentials and submit the login form identified by its action
                HtmlPage loginPage = webClient.getPage(url);
                HtmlForm loginForm = null;
                int numberOfFormCounter = 1;
                for (final HtmlForm form : loginPage.getForms()) {
                    Pattern pattern = Pattern.compile(actionOfLoginForm);
                    Matcher matcher = pattern.matcher(form.getActionAttribute());
                    System.out.println("Forms action attribute / number is : " + form.getActionAttribute() + " / " + numberOfFormCounter + ", should be " + actionOfLoginForm + " / "+numberOfForm);
                    if (matcher.matches() && numberOfForm == numberOfFormCounter && form.getInputsByName(nameOfUserField) != null) {
                        loginForm = form;
                    }
                    numberOfFormCounter++;
                }
                if (loginForm != null) {
                    final HtmlTextInput userfield = loginForm.getInputByName(nameOfUserField);
                    userfield.setValueAttribute(username);
                    final HtmlPasswordInput passwordfield = loginForm.getInputByName(nameOfPasswordField);
                    passwordfield.setValueAttribute(password);
                    final HtmlPage pageAfterLogin = (HtmlPage) loginForm.submit(null);                    
                    String pageWithPinString = pageAfterLogin.getWebResponse().getContentAsString();
                    
                    
                    Matcher matcher = patternOfPin.matcher(pageWithPinString);
                    System.out.println(pageWithPinString);
                    String pin = "";
                    if (matcher.find()){
                        pin = matcher.group(1);
                        System.out.println("matched!");
                    }
                    System.out.println("***** PIN  : " + pin);
                    
                }
            } catch (final FailingHttpStatusCodeException e) {
                
            } catch (final MalformedURLException e) {
                
            } catch (final IOException e) {
                
            }            
            
        } catch (TwitterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
