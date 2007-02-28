package com.openexchange.admin;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    private PluginStarter starter = null;

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        final ClassLoader oldloader = Thread.currentThread().getContextClassLoader();
        final ClassLoader newloader = this.getClass().getClassLoader();
        Thread.currentThread().setContextClassLoader(newloader);
        this.starter = new PluginStarter(newloader);
        this.starter.start(context);
        Thread.currentThread().setContextClassLoader(oldloader);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        this.starter.stop();
    }

}
