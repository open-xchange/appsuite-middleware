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
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.openexchange.subscribe.SubscriptionErrorMessage;
import com.openexchange.subscribe.SubscriptionException;

/**
 * This Step gets a page reachable via Url in the current context (WebClient) 
 * 
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class PageByUrlStep extends AbstractStep implements Step<HtmlPage, Object>{

	private String url;
	private HtmlPage currentPage;
	private Exception exception;
	private boolean executedSuccessfully;
	
	public PageByUrlStep(){
		
	}
	
	public PageByUrlStep(String description, String url){
		this.description = description;
		this.url = url;
	}
	
	public void execute(WebClient webClient)  throws SubscriptionException{
		try {
			HtmlPage pageByUrl = webClient.getPage(this.url);
			this.currentPage = pageByUrl;
			executedSuccessfully = true;
		} catch (FailingHttpStatusCodeException e) {
			throw SubscriptionErrorMessage.COMMUNICATION_PROBLEM.create(e);
		} catch (MalformedURLException e) {
			throw SubscriptionErrorMessage.COMMUNICATION_PROBLEM.create(e);
		} catch (IOException e) {
			throw SubscriptionErrorMessage.COMMUNICATION_PROBLEM.create(e);
		}
	}

	public boolean executedSuccessfully() {
		return this.executedSuccessfully;
	}

	public Exception getException() {
		return this.exception;
	}

	public String inputType() {
		return URL_STRING;
	}

	public String outputType() {
		return HTML_PAGE;
	}

	public HtmlPage getOutput() {
		return currentPage;
	}

	public void setInput(Object input) {
		// this needs to do nothing
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public HtmlPage getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(HtmlPage currentPage) {
		this.currentPage = currentPage;
	}

	public boolean isExecutedSuccessfully() {
		return executedSuccessfully;
	}

	public void setExecutedSuccessfully(boolean executedSuccessfully) {
		this.executedSuccessfully = executedSuccessfully;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}
	
	

}
