/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.twitter.internal;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import twitter4j.OXTwitter;
import twitter4j.OXTwitterImpl;
import twitter4j.TwitterException;
import twitter4j.auth.AuthorizationFactory;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationContext;


/**
 * {@link TwitterOAuthTest}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class TwitterOAuthTest {

    public static void main(final String[] args){
        final Configuration configuration = ConfigurationContext.getInstance();
        final OXTwitter twitter = new OXTwitterImpl(configuration, AuthorizationFactory.getInstance(configuration));

        final String twitterId = "";
        final String password = "";

        twitter.setOAuthConsumer("kZX3V4AmAWwC53yA5RjHbQ", "HvY7pVFFQSGPVu9LCoBMUhvMpS00qdtqBob99jucc");
        try {
            final RequestToken requestToken = twitter.getOAuthRequestToken();
            System.out.println(requestToken.getAuthorizationURL());


            final String url = requestToken.getAuthorizationURL();
            final String username = twitterId;
            final String nameOfUserField = "session[username_or_email]";
            final String nameOfPasswordField = "session[password]";
            final String actionOfLoginForm = "http://twitter.com/oauth/authorize";
            final Pattern patternOfPin = Pattern.compile("oauth_pin\">(?:[^0-9]*)([0-9]*)");
            final int numberOfForm = 1;

            try {
                final BrowserVersion browser = BrowserVersion.FIREFOX_3;
                final WebClient webClient = new WebClient(browser);
                // Get the page, fill in the credentials and submit the login form identified by its action
                final HtmlPage loginPage = webClient.getPage(url);
                HtmlForm loginForm = null;
                int numberOfFormCounter = 1;
                for (final HtmlForm form : loginPage.getForms()) {
                    final Pattern pattern = Pattern.compile(actionOfLoginForm);
                    final Matcher matcher = pattern.matcher(form.getActionAttribute());
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
                    final String pageWithPinString = pageAfterLogin.getWebResponse().getContentAsString();


                    final Matcher matcher = patternOfPin.matcher(pageWithPinString);
                    System.out.println(pageWithPinString);
                    String pin = "";
                    if (matcher.find()){
                        pin = matcher.group(1);
                        System.out.println("matched!");
                    }
                    System.out.println("***** PIN  : " + pin);

                }
            } catch (FailingHttpStatusCodeException e) {

            } catch (MalformedURLException e) {

            } catch (IOException e) {

            }

        } catch (TwitterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
