
package com.openexchange.subscribe.xing.osgi;

import java.util.ArrayList;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.openexchange.exceptions.osgi.ComponentRegistration;
import com.openexchange.subscribe.SubscribeService;
import com.openexchange.subscribe.SubscriptionErrorMessage;
import com.openexchange.subscribe.crawler.ContactObjectsByVcardTextPagesStep;
import com.openexchange.subscribe.crawler.LoginPageStep;
import com.openexchange.subscribe.crawler.Step;
import com.openexchange.subscribe.crawler.TextPagesByLinkStep;
import com.openexchange.subscribe.crawler.Workflow;
import com.openexchange.subscribe.xing.XingSubscribeService;

public class Activator implements BundleActivator {

    private ComponentRegistration componentRegistration;

    private ServiceRegistration serviceRegistration;

    public void start(BundleContext context) throws Exception {
        componentRegistration = new ComponentRegistration(
            context,
            "XING",
            "com.openexchange.subscribe.xing",
            SubscriptionErrorMessage.EXCEPTIONS);

        ArrayList<Step> listOfSteps = new ArrayList<Step>();
		listOfSteps.add(new LoginPageStep("Login to www.xing.com", "https://www.xing.com", "", "", "loginform", "login_user_name", "login_password","Home | XING"));
		listOfSteps.add(new TextPagesByLinkStep("Get all vcards as text pages", "https://www.xing.com/app/contact?notags_filter=0;card_mode=0;search_filter=;tags_filter=;offset=", 10, "", "/app/vcard"));
		listOfSteps.add(new ContactObjectsByVcardTextPagesStep());
		Workflow xingWorkflow = new Workflow(listOfSteps);
        XingSubscribeService subscribeService = new XingSubscribeService();
        subscribeService.setXingWorkflow(xingWorkflow);

            serviceRegistration = context.registerService(SubscribeService.class.getName(), subscribeService, null);
    }

    public void stop(BundleContext context) throws Exception {
        serviceRegistration.unregister();
        componentRegistration.unregister();
    }

 

}
