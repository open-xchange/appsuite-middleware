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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
import twitter4j.OXTwitter;
import twitter4j.OXTwitterImpl;
import twitter4j.TwitterException;
import twitter4j.auth.AuthorizationFactory;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationContext;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
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
            } catch (final FailingHttpStatusCodeException e) {

            } catch (final MalformedURLException e) {

            } catch (final IOException e) {

            }

        } catch (final TwitterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
