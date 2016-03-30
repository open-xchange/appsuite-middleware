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

import static com.openexchange.twitter.internal.TwitterUtils.handleTwitterException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import twitter4j.OXTwitter;
import twitter4j.OXTwitterImpl;
import twitter4j.auth.AuthorizationFactory;
import twitter4j.auth.BasicAuthorization;
import twitter4j.auth.NullAuthorization;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.OXConfigurationBase;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.openexchange.exception.OXException;
import com.openexchange.twitter.Paging;
import com.openexchange.twitter.TwitterAccess;
import com.openexchange.twitter.TwitterAccessToken;
import com.openexchange.twitter.TwitterExceptionCodes;
import com.openexchange.twitter.TwitterService;

/**
 * {@link TwitterServiceImpl} - The twitter service implementation based on <a
 * href="http://repo1.maven.org/maven2/net/homeip/yusuke/twitter4j/">twitter4j</a>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TwitterServiceImpl implements TwitterService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TwitterServiceImpl.class);

    /**
     * Initializes a new {@link TwitterServiceImpl}.
     */
    public TwitterServiceImpl() {
        super();
    }

    @Override
    public TwitterAccess getTwitterAccess(final String twitterId, final String password) {
        return new TwitterAccessImpl(new OXTwitterImpl(OXConfigurationBase.getInstance().generateConfiguration(), new BasicAuthorization(
            twitterId,
            password)));
    }

    @Override
    public TwitterAccess getUnauthenticatedTwitterAccess() {
        return new TwitterAccessImpl(new OXTwitterImpl(
            OXConfigurationBase.getInstance().generateConfiguration(),
            NullAuthorization.getInstance()));
    }

    @Override
    public Paging newPaging() {
        return new PagingImpl(new twitter4j.Paging());
    }

    @Override
    public TwitterAccess getOAuthTwitterAccess(final String twitterToken, final String twitterTokenSecret) throws OXException {
        /*
         * Insert the appropriate consumer key and consumer secret here
         */
        final String consumerKey = TwitterConfiguration.getInstance().getConsumerKey();
        final String consumerSecret = TwitterConfiguration.getInstance().getConsumerSecret();
        if (null == consumerKey || null == consumerSecret) {
            throw TwitterExceptionCodes.MISSING_CONSUMER_KEY_SECRET.create(new Object[0]);
        }
        final Configuration configuration =
            OXConfigurationBase.getInstance().generateConfiguration(consumerKey, consumerSecret, twitterToken, twitterTokenSecret);
        final OAuthAuthorization oAuthAuthorization = new OAuthAuthorization(configuration);
        try {
            // Ensure presence of access token in authorization instance
            oAuthAuthorization.getOAuthAccessToken();
        } catch (final twitter4j.TwitterException e) {
            throw handleTwitterException(e);
        }
        return new TwitterAccessImpl(new OXTwitterImpl(configuration, oAuthAuthorization));
    }

    @Override
    public TwitterAccessToken getTwitterAccessToken(final String twitterId, final String password) throws OXException {
        try {
            /*
             * Obtain request token
             */
            final String consumerKey = TwitterConfiguration.getInstance().getConsumerKey();
            final String consumerSecret = TwitterConfiguration.getInstance().getConsumerSecret();
            if (null == consumerKey || null == consumerSecret) {
                throw TwitterExceptionCodes.MISSING_CONSUMER_KEY_SECRET.create(new Object[0]);
            }
            final Configuration configuration = OXConfigurationBase.getInstance().generateConfiguration(consumerKey, consumerSecret);
            final OXTwitter twitter = new OXTwitterImpl(configuration, AuthorizationFactory.getInstance(configuration));
            twitter.setOAuthConsumer(consumerKey, consumerSecret);
            final RequestToken requestToken;
            try {
                requestToken = twitter.getOAuthRequestToken();
            } catch (final twitter4j.TwitterException e) {
                /*
                 * Probably consumer-key/consumer-secret pair is invalid
                 */
                throw TwitterExceptionCodes.INVALID_CONSUMER_KEY_SECRET.create(e, e.getMessage());
            }
            /*
             * Crawl PIN
             */
            final String pin = crawlPINFromAuthURL(twitterId, password, requestToken);
            /*
             * Obtain & return OAuth access token
             */
            if (0 == pin.length()) {
                /*
                 * PIN could not be crawled
                 */
                LOG.warn("PIN could no be read from authorization URL: {}", requestToken.getAuthenticationURL());
                return new TwitterAccessTokenImpl(twitter.getOAuthAccessToken(requestToken));
            }
            /*
             * PIN available
             */
            return new TwitterAccessTokenImpl(twitter.getOAuthAccessToken(requestToken, pin));
        } catch (final twitter4j.TwitterException e) {
            throw handleTwitterException(e);
        }
    }

    /**
     * The pattern to match the PIN in page content.
     */
    private static final Pattern PATTERN_PIN = Pattern.compile("oauth_pin\">(?:[^0-9]*)([0-9]*)");

    /**
     * Crawls the PIN from specified request token's authorization URL.
     *
     * @param twitterId The twitter account name
     * @param password The twitter account password
     * @param requestToken The request token providing authorization URL
     * @return The PIN from authorization URL if any found; otherwise an empty string
     * @throws OXException If crawling the PIN fails
     */
    private static String crawlPINFromAuthURL(final String twitterId, final String password, final RequestToken requestToken) throws OXException {
        try {
            String pin = "";
            final BrowserVersion browser = BrowserVersion.FIREFOX_3;
            final WebClient webClient = new WebClient(browser);
            /*
             * Get page from authorization URL
             */
            final HtmlPage loginPage = webClient.getPage(requestToken.getAuthorizationURL());
            /*
             * Some constants
             */
            final String actionOfLoginForm = "https://twitter.com/oauth/authorize";
            final String idOfLoginForm = "login_form";
            final String nameOfUserField = "session[username_or_email]";
            final String nameOfPasswordField = "session[password]";
            /*
             * Iterate page's forms and look for login form
             */
            HtmlForm loginForm = null;
            {
                /*
                 * The expected form index
                 */
                final int formIndex = 0;
                final List<HtmlForm> forms = loginPage.getForms();
                final Iterator<HtmlForm> iter = forms.iterator();
                final int size = forms.size();
                for (int i = 0; null == loginForm && i < size; i++) {
                    final HtmlForm form = iter.next();
                    LOG.debug("Forms action attribute / index is : {} / {}, should be {} / {}", form.getActionAttribute(), i, actionOfLoginForm, formIndex);
                    if (formIndex == i) {
                        if ((actionOfLoginForm.equals(form.getActionAttribute()) || idOfLoginForm.equals(form.getId())) && form.getInputsByName(nameOfUserField) != null) {
                            loginForm = form;
                        }
                    }
                }
            }
            /*
             * Login form found?
             */
            if (loginForm != null) {
                /*
                 * Fill login and password into appropriate input fields.
                 */
                loginForm.<HtmlTextInput> getInputByName(nameOfUserField).setValueAttribute(twitterId);
                loginForm.<HtmlPasswordInput> getInputByName(nameOfPasswordField).setValueAttribute(password);
                /*
                 * Submit login form and get following page's content
                 */
                final String pageWithPinString = ((HtmlPage) loginForm.submit(null)).getWebResponse().getContentAsString();
                /*
                 * Find PIN in page's content
                 */
                final Matcher matcher = PATTERN_PIN.matcher(pageWithPinString);
                LOG.debug(pageWithPinString);
                if (matcher.find()) {
                    /*
                     * Assign found PIN
                     */
                    pin = matcher.group(1);
                } else {
                    LOG.warn("PIN not found in page content:\n{}", pageWithPinString);
                }
            }
            /*
             * Return PIN
             */
            return pin;
        } catch (final FailingHttpStatusCodeException e) {
            throw TwitterExceptionCodes.ACCESS_TOKEN_FAILED.create(e, twitterId);
        } catch (final MalformedURLException e) {
            throw TwitterExceptionCodes.ACCESS_TOKEN_FAILED.create(e, twitterId);
        } catch (final IOException e) {
            throw TwitterExceptionCodes.ACCESS_TOKEN_FAILED.create(e, twitterId);
        }
    }
}
