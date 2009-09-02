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

import java.util.List;
import org.ho.yaml.Yaml;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.openexchange.groupware.container.Contact;
import com.openexchange.subscribe.SubscriptionErrorMessage;
import com.openexchange.subscribe.SubscriptionException;
import com.openexchange.subscribe.crawler.darkside.WebClientCloser;

/**
 * A crawling workflow. This holds the individual Steps and the session information (WebClient instance). 
 * 
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class Workflow {

    private static final WebClientCloser closer = new WebClientCloser();
    
	private List<Step> steps;
	
	private String loginStepString;
	
	public Workflow() {
		
	}
	
	public Workflow (List<Step> steps){
		this.steps = steps;
	}
	
	// Convenience method for setting username and password after the workflow was created
	public Contact[] execute(String username, String password) throws SubscriptionException{
		for (Step currentStep : steps) {
			if (currentStep instanceof LoginStep){
				((LoginStep) currentStep).setUsername(username);
				((LoginStep) currentStep).setPassword(password);
				loginStepString = Yaml.dump(currentStep);
				//System.out.println("***** Dumping current step as LoginStep : " + currentStep);
			}
			if (currentStep instanceof NeedsLoginStepString && null != loginStepString){
			    ((NeedsLoginStepString) currentStep).setLoginStepString(loginStepString);
			    //System.out.println("***** Setting LoginStep for currentStep : " + currentStep);
			}
		}	
		return execute();
	}

	public Contact[] execute()  throws SubscriptionException {
		
		// emulate a known client, hopefully keeping our profile low
		final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_2);
		// Javascript needs to be disabled for security reasons
		webClient.setJavaScriptEnabled(false);
		try {
		    
		    Step previousStep = null;
	        
	        for (Step currentStep : steps) {
	            if (previousStep != null) {
	                currentStep.setInput(previousStep.getOutput());
	            }
	            //System.out.println("***** Current Step : " + currentStep);
	            currentStep.execute(webClient);
	            previousStep = currentStep;
	            if (! currentStep.executedSuccessfully()) {
	                throw SubscriptionErrorMessage.COMMUNICATION_PROBLEM.create(); 
	            }
	        }
	        
	        webClient.closeAllWindows();
	        return (Contact[]) previousStep.getOutput();
	    } finally {
	        closer.close(webClient);
	    }
	}

	public List<Step> getSteps() {
		return steps;
	}

	public void setSteps(List<Step> steps) {
		this.steps = steps;
	}

}