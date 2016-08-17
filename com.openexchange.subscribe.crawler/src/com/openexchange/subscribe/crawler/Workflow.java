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

package com.openexchange.subscribe.crawler;

import java.security.GeneralSecurityException;
import java.util.List;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.yaml.snakeyaml.Yaml;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.BrowserVersionFeatures;
import com.gargoylesoftware.htmlunit.CrawlerCookieManager;
import com.gargoylesoftware.htmlunit.CrawlerCookieSpec;
import com.gargoylesoftware.htmlunit.CrawlerCookieSpecWithQuirkyQuotes;
import com.gargoylesoftware.htmlunit.CrawlerWebConnection;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.ThreadedRefreshHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.openexchange.exception.OXException;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionErrorMessage;
import com.openexchange.subscribe.crawler.internal.HasLoginPage;
import com.openexchange.subscribe.crawler.internal.LoginStep;
import com.openexchange.subscribe.crawler.internal.LogoutStep;
import com.openexchange.subscribe.crawler.internal.NeedsLoginStepString;
import com.openexchange.subscribe.crawler.internal.Step;
import com.openexchange.subscribe.crawler.osgi.CrawlersActivator;

/**
 * A crawling workflow. This holds the individual Steps and the session information (WebClient instance).
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class Workflow {

    private List<Step<?, ?>> steps;

    private String loginStepString;

    private Subscription subscription;

    private boolean useThreadedRefreshHandler;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Workflow.class);

    private CrawlersActivator activator;

    private boolean enableJavascript;

    private boolean debuggingEnabled = false;

    private boolean mobileUserAgent = false;

    private boolean quirkyCookieQuotes;

    public Workflow() {
        super();
    }

    public Workflow(final List<Step<?, ?>> steps) {
        super();
        this.steps = steps;
        useThreadedRefreshHandler = false;
    }

    // Convenience method for setting username and password after the workflow was created
    public Object[] execute(final String username, final String password) throws OXException {
        // Ensure username and password are not null
        if (null == username) {
            throw SubscriptionErrorMessage.MISSING_ARGUMENT.create("username");
        }
        if (null == password) {
            throw SubscriptionErrorMessage.MISSING_ARGUMENT.create("password");
        }
        for (final Step<?, ?> currentStep : steps) {
            if (debuggingEnabled) {
                currentStep.setDebuggingEnabled(true);
            }
            if (currentStep instanceof LoginStep) {
                ((LoginStep) currentStep).setUsername(username);
                ((LoginStep) currentStep).setPassword(password);
                Yaml yaml = new Yaml();
                loginStepString = yaml.dump(currentStep);
            }
            if (currentStep instanceof NeedsLoginStepString && null != loginStepString) {
                ((NeedsLoginStepString) currentStep).setLoginStepString(loginStepString);
            }
        }
        return execute();
    }

    public Object[] execute() throws OXException {

        // emulate a specific browser
        BrowserVersion browser = BrowserVersion.FIREFOX_3;
        if (mobileUserAgent) {
            browser = new BrowserVersion("Netscape", "5.0 (Windows; en-US)",
                "Mozilla/5.0 (iPhone; U; CPU iPhone OS 3_0 like Mac OS X; en-us) AppleWebKit/528.18 (KHTML, like Gecko) Version/4.0 Mobile/7A341 Safari/528.16",
                (float)1.2, new BrowserVersionFeatures[0]);
        }

        final WebClient webClient = new WebClient(browser);

        // use a custom CookiePolicy to be more lenient and thereby work with more websites
        CrawlerWebConnection crawlerConnection = new CrawlerWebConnection(webClient);
        // the same applies to SSL: Be as lenient, and thereby as compatible, as possible
        try {
            webClient.setUseInsecureSSL(true);
        } catch (GeneralSecurityException e) {
            LOG.error(e.toString());
        }
        // ... and to javascript as well
        webClient.setThrowExceptionOnScriptError(false);
        if (quirkyCookieQuotes) {crawlerConnection.setQuirkyCookieQuotes(true);}
        CookiePolicy.registerCookieSpec("crawler-special", CrawlerCookieSpec.class);
        CookiePolicy.registerCookieSpec("crawler-special-qq", CrawlerCookieSpecWithQuirkyQuotes.class);
        webClient.setCookieManager(new CrawlerCookieManager());
        // System.out.println(CookiePolicy.getCookieSpec("crawler-special"));

        webClient.setWebConnection(crawlerConnection);
        // Javascript is disabled by default for security reasons but may be activated for single crawlers
        webClient.setJavaScriptEnabled(enableJavascript);
        webClient.setTimeout(60000);
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        if (useThreadedRefreshHandler) {
            webClient.setRefreshHandler(new ThreadedRefreshHandler());
        }
        try {

            Step<?, ?> previousStep = null;
            Object result = null;

            for (final Step currentStep : steps) {
                if (previousStep != null) {
                    currentStep.setInput(previousStep.getOutput());
                }
                currentStep.setWorkflow(this);
                LOG.debug("Current Step : {}", currentStep.getClass());
                if (currentStep.isSwitchUserAgent()){
                    crawlerConnection.switchUserAgent();
                }
                currentStep.execute(webClient);
                //switch back the user agent
                if (currentStep.isSwitchUserAgent()){
                    crawlerConnection.switchUserAgent();
                }
                previousStep = currentStep;
                // if step fails try it 2 more times before crying foul
                if (!currentStep.executedSuccessfully()) {
                    LOG.error("This step did not perform as expected : {}. Repeating two more times ...", currentStep.getClass());
                    logBadInput(currentStep);
                    currentStep.execute(webClient);
                    if (!currentStep.executedSuccessfully()) {
                        LOG.error("This step failed again at repetition 1 : {}. Repeating one more time ...", currentStep.getClass());
                        logBadInput(currentStep);
                        currentStep.execute(webClient);
                        if (!currentStep.executedSuccessfully()) {
                            LOG.error("This step failed again at repetition 2 : {}. Throwing Error now.", currentStep.getClass());
                            logBadInput(currentStep);
                            throw SubscriptionErrorMessage.COMMUNICATION_PROBLEM.create();
                        }
                    }
                }
                if (!(currentStep instanceof LogoutStep)) {
                    result = currentStep.getOutput();
                }
            }

            webClient.closeAllWindows();
            return (Object[]) result;
        } catch (RuntimeException e) {
            LOG.error("User with id={} and context={} failed to subscribe source={} with display_name={}", subscription.getUserId(), subscription.getContext(), subscription.getSource().getDisplayName(), subscription.getDisplayName(), e);
            throw SubscriptionErrorMessage.COMMUNICATION_PROBLEM.create(e);
        } finally {
            /*MultiThreadedHttpConnectionManager manager = (MultiThreadedHttpConnectionManager) crawlerConnection.getHttpClient().getHttpConnectionManager();
            manager.shutdown(); */
        }
    }

    public List<Step<?, ?>> getSteps() {
        return steps;
    }

    public void setSteps(final List<Step<?, ?>> steps) {
        this.steps = steps;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(final Subscription subscription) {
        this.subscription = subscription;
    }

    public String getLoginStepString() {
        return loginStepString;
    }

    public void setLoginStepString(final String loginStepString) {
        this.loginStepString = loginStepString;
    }

    public boolean isUseThreadedRefreshHandler() {
        return useThreadedRefreshHandler;
    }

    public void setUseThreadedRefreshHandler(final boolean useThreadedRefreshHandler) {
        this.useThreadedRefreshHandler = useThreadedRefreshHandler;
    }

    public CrawlersActivator getActivator() {
        return activator;
    }

    public void setActivator(CrawlersActivator activator) {
        this.activator = activator;
    }

    public boolean isEnableJavascript() {
        return enableJavascript;
    }

    public void setEnableJavascript(boolean enableJavascript) {
        this.enableJavascript = enableJavascript;
    }

    public boolean isDebuggingEnabled() {
        return debuggingEnabled;
    }

    public void setDebuggingEnabled(boolean debuggingEnabled) {
        this.debuggingEnabled = debuggingEnabled;
    }

    public boolean isMobileUserAgent() {
        return mobileUserAgent;
    }

    public void setMobileUserAgent(boolean mobileUserAgent) {
        this.mobileUserAgent = mobileUserAgent;
    }


    public boolean isQuirkyCookieQuotes() {
        return quirkyCookieQuotes;
    }


    public void setQuirkyCookieQuotes(boolean quirkyCookieQuotes) {
        this.quirkyCookieQuotes = quirkyCookieQuotes;
    }

    // This should help to better understand why a step failed. May need to be expanded for other complex inputs without helpful toString()-Method ...
    private void logBadInput(Step<?, ?> currentStep){
        if (currentStep.getInput() != null){
            if (currentStep.getInput() instanceof Page){
                LOG.error("Bad Input causing the error at ({}) : {}", currentStep.getClass(), ((Page) currentStep.getInput()).getWebResponse().getContentAsString());
            } if (currentStep instanceof HasLoginPage){
                LOG.error("Bad Page causing the error at ({}) : {}", currentStep.getClass(), (((HasLoginPage) currentStep).getLoginPage().getWebResponse().getContentAsString()));
            } else {
                LOG.error(" Bad Input causing the error at ({}) : {}", currentStep.getClass(), currentStep.getInput());
            }
        }
    }

}
