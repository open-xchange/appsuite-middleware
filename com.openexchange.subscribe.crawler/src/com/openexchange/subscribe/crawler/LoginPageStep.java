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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.openexchange.subscribe.SubscriptionErrorMessage;
import com.openexchange.subscribe.SubscriptionException;

/**
 * This Step logs into a website via a form requiring username and password. 
 * 
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class LoginPageStep extends AbstractStep implements Step<HtmlPage, Object>, LoginStep{

	private String url, username, password, nameOfLoginForm, nameOfUserField, nameOfPasswordField, linkAvailableAfterLogin, baseUrl;
	private HtmlPage currentPage;
	
	public LoginPageStep(){
		
	}
	
	public LoginPageStep (String description, String url, String username, String password, String nameOfLoginForm, String nameOfUserField, String nameOfPasswordField, String linkAvailableAfterLogin, String baseUrl) {
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
	
	public void execute(WebClient webClient) throws SubscriptionException{
		HtmlPage loginPage;
		try {
			// Get the page, fill in the credentials and submit the login form
			loginPage = webClient.getPage(this.url);
		    HtmlForm loginForm = loginPage.getFormByName(this.nameOfLoginForm);
		    HtmlTextInput userfield = loginForm.getInputByName(this.nameOfUserField);
		    userfield.setValueAttribute(this.username);
		    HtmlPasswordInput passwordfield = loginForm.getInputByName(this.nameOfPasswordField);
		    passwordfield.setValueAttribute(this.password);
		    final HtmlPage pageAfterLogin = (HtmlPage)loginForm.submit(null);
		    this.currentPage = pageAfterLogin;
		    
		    // if this link is not on the page the login did not work
		    boolean linkAvailable = false;
		    for (HtmlAnchor link : pageAfterLogin.getAnchors()){
		    	if (link.getHrefAttribute().contains(linkAvailableAfterLogin)){
		    		linkAvailable = true;
		    	}
		    }
		    if (! linkAvailable){
		    	throw SubscriptionErrorMessage.INVALID_LOGIN.create();
		    }
		    executedSuccessfully = true;
		} catch (FailingHttpStatusCodeException e) {	
			throw SubscriptionErrorMessage.COMMUNICATION_PROBLEM.create(e);
		} catch (MalformedURLException e) {
			throw SubscriptionErrorMessage.COMMUNICATION_PROBLEM.create(e);
		} catch (IOException e) {
			throw SubscriptionErrorMessage.COMMUNICATION_PROBLEM.create(e);
		}		
	}

	public HtmlPage getCurrentPage() {
		return currentPage;
	}
	
	public String inputType() {
		return null;
	}

	public String outputType() {
		return HTML_PAGE;
	}

	public HtmlPage getOutput() {
		return currentPage;
	}

	public void setInput(Object input) {
		// this does nothing
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getNameOfLoginForm() {
		return nameOfLoginForm;
	}

	public void setNameOfLoginForm(String nameOfLoginForm) {
		this.nameOfLoginForm = nameOfLoginForm;
	}

	public String getNameOfUserField() {
		return nameOfUserField;
	}

	public void setNameOfUserField(String nameOfUserField) {
		this.nameOfUserField = nameOfUserField;
	}

	public String getNameOfPasswordField() {
		return nameOfPasswordField;
	}

	public void setNameOfPasswordField(String nameOfPasswordField) {
		this.nameOfPasswordField = nameOfPasswordField;
	}

	public void setCurrentPage(HtmlPage currentPage) {
		this.currentPage = currentPage;
	}

	public String getPageTitleAfterLogin() {
		return linkAvailableAfterLogin;
	}

	public void setPageTitleAfterLogin(String pageTitleAfterLogin) {
		this.linkAvailableAfterLogin = pageTitleAfterLogin;
	}

    
    public String getLinkAvailableAfterLogin() {
        return linkAvailableAfterLogin;
    }

    
    public void setLinkAvailableAfterLogin(String linkAvailableAfterLogin) {
        this.linkAvailableAfterLogin = linkAvailableAfterLogin;
    }

    
    public String getBaseUrl() {
        return baseUrl;
    }

    
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
	
	

}
