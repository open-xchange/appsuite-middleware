package com.openexchange.subscribe.crawler;

import java.util.List;
import java.util.Vector;
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