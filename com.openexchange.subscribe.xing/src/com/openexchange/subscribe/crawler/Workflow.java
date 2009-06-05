package com.openexchange.subscribe.crawler;

import java.util.List;
import java.util.Vector;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.openexchange.groupware.container.ContactObject;

/**
 * A crawling workflow. This holds the individual Steps and the session information (WebClient instance). 
 * 
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class Workflow {

	private final List<Step> steps;
	
	public Workflow (List<Step> steps){
		this.steps = steps;
	}

	public ContactObject[] execute() {
		Vector<ContactObject> contactObjects = new Vector<ContactObject>();
		
		// emulate a known client, hopefully keeping our profile low
		final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_2);
		// Javascript needs to be disabled as there are errors on the start page
		webClient.setJavaScriptEnabled(false);
		Step previousStep = null;
		
		for (Step currentStep : steps) {
			if (previousStep != null) {
				currentStep.setInput(previousStep.getOutput());
			}
			currentStep.execute(webClient);
			previousStep = currentStep;
			if (! currentStep.executedSuccessfully()) break;
		}
		
		webClient.closeAllWindows();
		
		ContactObject[] contactObjectsArray = new ContactObject[contactObjects.size()];
	    for (int i=0; i<contactObjectsArray.length && i< contactObjects.size(); i++){
	    	contactObjectsArray[i] = contactObjects.get(i);
	    }
		return contactObjectsArray;
	}

}