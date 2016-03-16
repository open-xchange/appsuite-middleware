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
 * {@link LoginPageByFormIDStep}
 * This step identifies the login-form via its id. The same method is used for the fields for username and password.
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class LoginPageByFormIDStep extends AbstractStep<HtmlPage, Object> implements LoginStep, HasLoginPage {

    private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LoginPageByFormActionStep.class);

    private String url, username, password, idOfLoginForm, idOfUserField, idOfPasswordField, linkAvailableAfterLogin;

    private Page loginPage;


    public LoginPageByFormIDStep() {

    }

    public LoginPageByFormIDStep(final String description, final String url, final String username, final String password, final String idOfLoginForm, final String iddOfUserField, final String idOfPasswordField, final String linkAvailableAfterLogin) {
        this.description = description;
        this.url = url;
        this.username = username;
        this.password = password;
        this.idOfLoginForm = idOfLoginForm;
        this.idOfUserField = iddOfUserField;
        this.idOfPasswordField = idOfPasswordField;
        this.linkAvailableAfterLogin = linkAvailableAfterLogin;
    }

    @Override
    public void execute(final WebClient webClient) throws OXException {
        HtmlPage loginPage = null;
        try {
            // Get the page, fill in the credentials and submit the login form identified by its action
            loginPage = webClient.getPage(url);

            this.loginPage = loginPage;
            HtmlForm loginForm = (HtmlForm) loginPage.getElementById(idOfLoginForm);

            if (loginForm != null) {
                final HtmlTextInput userfield = (HtmlTextInput) loginForm.getInputByName("TextfieldEmail");
                userfield.setValueAttribute(username);
                final HtmlPasswordInput passwordfield = (HtmlPasswordInput) loginForm.getInputByName("TextfieldPassword");
                passwordfield.setValueAttribute(password);
                final HtmlPage pageAfterLogin = (HtmlPage) loginForm.submit(null);
                output = pageAfterLogin;

                openPageInBrowser(pageAfterLogin);

                executedSuccessfully = true;
            }
        } catch (final FailingHttpStatusCodeException e) {
            throw SubscriptionErrorMessage.COMMUNICATION_PROBLEM.create(e);
        } catch (final MalformedURLException e) {
            throw SubscriptionErrorMessage.COMMUNICATION_PROBLEM.create(e);
        } catch (final IOException e) {
            throw SubscriptionErrorMessage.COMMUNICATION_PROBLEM.create(e);
        } catch (final ElementNotFoundException e) {
            openPageInBrowser(loginPage);
            throw SubscriptionErrorMessage.COMMUNICATION_PROBLEM.create(e);
        }
    }

    @Override
    public void setInput(final Object input) {
        // this does nothing
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

    public String getActionOfLoginForm() {
        return idOfLoginForm;
    }

    public void setActionOfLoginForm(final String nameOfLoginForm) {
        idOfLoginForm = nameOfLoginForm;
    }

    public String getNameOfUserField() {
        return idOfUserField;
    }

    public void setNameOfUserField(final String nameOfUserField) {
        this.idOfUserField = nameOfUserField;
    }

    public String getNameOfPasswordField() {
        return idOfPasswordField;
    }

    public void setNameOfPasswordField(final String nameOfPasswordField) {
        this.idOfPasswordField = nameOfPasswordField;
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

    /* (non-Javadoc)
     * @see com.openexchange.subscribe.crawler.internal.LoginStep#getBaseUrl()
     */
    @Override
    public String getBaseUrl() {
        return "";
    }


    public String getIdOfLoginForm() {
        return idOfLoginForm;
    }


    public void setIdOfLoginForm(String idOfLoginForm) {
        this.idOfLoginForm = idOfLoginForm;
    }


    public String getIdOfUserField() {
        return idOfUserField;
    }


    public void setIdOfUserField(String idOfUserField) {
        this.idOfUserField = idOfUserField;
    }


    public String getIdOfPasswordField() {
        return idOfPasswordField;
    }


    public void setIdOfPasswordField(String idOfPasswordField) {
        this.idOfPasswordField = idOfPasswordField;
    }

    @Override
    public Page getLoginPage() {
        return loginPage;
    }


}
