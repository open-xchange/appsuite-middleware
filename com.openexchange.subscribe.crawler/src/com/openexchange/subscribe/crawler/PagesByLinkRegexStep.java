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
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.openexchange.subscribe.SubscriptionException;

/**
 * This step takes a page and returns all pages linked from this page via links that fulfill a regular expression
 * 
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class PagesByLinkRegexStep extends AbstractStep implements Step<List<HtmlPage>, HtmlPage> {
	
	private HtmlPage htmlPage;
	private String linkRegex;
	
	private List<HtmlPage> returnedPages = new ArrayList<HtmlPage>();
	
	public PagesByLinkRegexStep() {
		
	}
	
	public PagesByLinkRegexStep(String description, String linkRegex) {
		this.description = description;
		this.linkRegex = linkRegex;
	}

	public void execute(WebClient webClient)  throws SubscriptionException{
	    try {
	    	
	    	for (HtmlAnchor link: htmlPage.getAnchors()) {
	    		
	    		if (link.getHrefAttribute().matches(linkRegex)){
	    			HtmlPage tempPage = link.click();
	    			returnedPages.add(tempPage);
	    		}
	    	
	    	}
	    	executedSuccessfully = true;
			
		} catch (FailingHttpStatusCodeException e) {
			e.printStackTrace();
			this.exception = e;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			this.exception = e;
		} catch (IOException e) {
			e.printStackTrace();
			this.exception = e;
		}
	    
	}

	public boolean executedSuccessfully() {
		return executedSuccessfully;
	}

	public Exception getException() {
		return exception;
	}

	public String inputType() {
		return HTML_PAGE;
	}

	public String outputType() {
		return LIST_OF_HTML_PAGES;
	}

	public List<HtmlPage> getOutput() {
		return returnedPages;
	}

	public void setInput(HtmlPage input) {
		this.htmlPage = input;
	}

	public HtmlPage getHtmlPage() {
		return htmlPage;
	}

	public void setHtmlPage(HtmlPage htmlPage) {
		this.htmlPage = htmlPage;
	}

	public List<HtmlPage> getReturnedPages() {
		return returnedPages;
	}

	public void setReturnedPages(List<HtmlPage> returnedPages) {
		this.returnedPages = returnedPages;
	}

	public String getLinkRegex() {
		return linkRegex;
	}

	public void setLinkRegex(String linkRegex) {
		this.linkRegex = linkRegex;
	}
	
}
