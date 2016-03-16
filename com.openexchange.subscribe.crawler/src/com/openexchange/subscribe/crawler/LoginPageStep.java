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

import java.io.IOException;
import java.net.MalformedURLException;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.openexchange.exception.OXException;
import com.openexchange.subscribe.SubscriptionErrorMessage;
import com.openexchange.subscribe.crawler.internal.AbstractStep;
import com.openexchange.subscribe.crawler.internal.HasLoginPage;
import com.openexchange.subscribe.crawler.internal.LoginStep;

/**
 * This Step logs into a website via a form requiring username and password.
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class LoginPageStep extends AbstractStep<HtmlPage, Object> implements LoginStep, HasLoginPage {

    private String url, username, password, nameOfLoginForm, nameOfUserField, nameOfPasswordField, linkAvailableAfterLogin, baseUrl;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LoginPageStep.class);

    private Page loginPage;

    public LoginPageStep() {
    }

    public LoginPageStep(final String description, final String url, final String username, final String password, final String nameOfLoginForm, final String nameOfUserField, final String nameOfPasswordField, final String linkAvailableAfterLogin, final String baseUrl) {
        this.description = description;
        this.url = url;
        this.username = username;
        this.password = password;
        this.nameOfLoginForm = nameOfLoginForm;
        this.nameOfUserField = nameOfUserField;
        this.nameOfPasswordField = nameOfPasswordField;
        this.linkAvailableAfterLogin = linkAvailableAfterLogin;
        this.baseUrl = baseUrl;
    }

    @Override
    public void execute(final WebClient webClient) throws OXException {
        HtmlPage loginPage = null;
        try {
            // Get the page, fill in the credentials and submit the login form
            loginPage = webClient.getPage(url);
            // loginPage cannot be null after this call as getPage would throw an exception otherwise
            this.loginPage = loginPage;
            final HtmlForm loginForm = loginPage.getFormByName(nameOfLoginForm);
            final HtmlTextInput userfield = loginForm.getInputByName(nameOfUserField);
            userfield.setValueAttribute(username);
            final HtmlPasswordInput passwordfield = loginForm.getInputByName(nameOfPasswordField);
            passwordfield.setValueAttribute(password);
            final HtmlPage pageAfterLogin = (HtmlPage) loginForm.submit(null);
            output = pageAfterLogin;

            // if this link is not on the page the login did not work
            boolean linkAvailable = false;
            for (final HtmlAnchor link : pageAfterLogin.getAnchors()) {
                if (link.getHrefAttribute().contains(linkAvailableAfterLogin)) {
                    linkAvailable = true;
                }
            }
            if (!linkAvailable) {
                throw SubscriptionErrorMessage.INVALID_LOGIN.create();
            }
            executedSuccessfully = true;
        } catch (final FailingHttpStatusCodeException e) {
            throw SubscriptionErrorMessage.COMMUNICATION_PROBLEM.create(e);
        } catch (final MalformedURLException e) {
            throw SubscriptionErrorMessage.COMMUNICATION_PROBLEM.create(e);
        } catch (final IOException e) {
            throw SubscriptionErrorMessage.COMMUNICATION_PROBLEM.create(e);
        } catch (final ElementNotFoundException e){
            // As this is only thrown when loginPage is already evaluated, loginPage cannot be null. This check is just to be sure in this situation
            if (null != loginPage) {
                LOG.debug("The page that does not contain the needed form : \n{}", loginPage.getWebResponse().getContentAsString());
            } else {
                LOG.debug("The page that does not contain the needed form, also loginPage is null");
            }
            throw SubscriptionErrorMessage.COMMUNICATION_PROBLEM.create(e);
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(final String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(final String password) {
        this.password = password;
    }

    public String getNameOfLoginForm() {
        return nameOfLoginForm;
    }

    public void setNameOfLoginForm(final String nameOfLoginForm) {
        this.nameOfLoginForm = nameOfLoginForm;
    }

    public String getNameOfUserField() {
        return nameOfUserField;
    }

    public void setNameOfUserField(final String nameOfUserField) {
        this.nameOfUserField = nameOfUserField;
    }

    public String getNameOfPasswordField() {
        return nameOfPasswordField;
    }

    public void setNameOfPasswordField(final String nameOfPasswordField) {
        this.nameOfPasswordField = nameOfPasswordField;
    }

    public String getPageTitleAfterLogin() {
        return linkAvailableAfterLogin;
    }

    public void setPageTitleAfterLogin(final String pageTitleAfterLogin) {
        linkAvailableAfterLogin = pageTitleAfterLogin;
    }

    public String getLinkAvailableAfterLogin() {
        return linkAvailableAfterLogin;
    }

    public void setLinkAvailableAfterLogin(final String linkAvailableAfterLogin) {
        this.linkAvailableAfterLogin = linkAvailableAfterLogin;
    }

    @Override
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(final String baseUrl) {
        this.baseUrl = baseUrl;
    }


    @Override
    public Page getLoginPage() {
        return loginPage;
    }


}
