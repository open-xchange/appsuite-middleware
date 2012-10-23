package com.openexchange.test.osgi.test;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestSuite;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.config.ConfigurationService;
import com.openexchange.osgi.HousekeepingActivator;


public class Activator extends HousekeepingActivator {

    private static Activator instance;
//    private BundleContext context;

    private List<ServiceRegistration> regs;

    public Activator()  {
        instance = this;
    }

    @Override
    public void startBundle(){
//        this.context = context;
        regs = new ArrayList<ServiceRegistration>();
        regs.add(context.registerService(TestSuite.class.getName(), new TestSuite(OSGITest.class), null));

        System.out.println(this.getClass().getName() + " added " + regs.size() + " suites for OSGi testing.");
    }

    public static synchronized Activator getDefault() {
        return instance;
    }

    public BundleContext getContext() {
        return context;
    }

    public void stop(BundleContext context) throws Exception {
        for (ServiceRegistration sr: regs)
            sr.unregister();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class };
    }
}