package com.openexchange.subscribe.crawler;

import java.util.List;
import java.util.Vector;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.openexchange.groupware.container.Contact;
import com.openexchange.subscribe.xing.XingSubscriptionErrorMessage;
import com.openexchange.subscribe.xing.XingSubscriptionException;

/**
 * A crawling workflow. This holds the individual Steps and the session information (WebClient instance). 
 * 
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class Workflow {

	private List<Step> steps;
	
	public Workflow() {
		
	}
	
	public Workflow (List<Step> steps){
		this.steps = steps;
	}

	public Contact[] execute()  throws XingSubscriptionException {
		Vector<Contact> contactObjects = new Vector<Contact>();
		boolean workflowComplete = true;
		
		// emulate a known client, hopefully keeping our profile low
		final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_2);
		// Javascript needs to be disabled for security reasons
		webClient.setJavaScriptEnabled(false);
		Step previousStep = null;
		
		for (Step currentStep : steps) {
			if (previousStep != null) {
				currentStep.setInput(previousStep.getOutput());
			}
			currentStep.execute(webClient);
			previousStep = currentStep;
			if (! currentStep.executedSuccessfully()) {
				throw XingSubscriptionErrorMessage.COMMUNICATION_PROBLEM.create(); 
			}
		}
		
		webClient.closeAllWindows();
		return (Contact[]) previousStep.getOutput();
	}

	public List<Step> getSteps() {
		return steps;
	}

	public void setSteps(List<Step> steps) {
		this.steps = steps;
	}

}