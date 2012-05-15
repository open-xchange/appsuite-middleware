
package com.openexchange.spamsettings.generic.osgi;

import org.osgi.framework.BundleActivator;
import com.openexchange.osgi.CompositeBundleActivator;

/**
 * {@link Activator}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Activator extends CompositeBundleActivator {

    private static final BundleActivator[] ACTIVATORS = { new PreferencesActivator(), new ServletActivator() };

    @Override
    protected BundleActivator[] getActivators() {
        return ACTIVATORS;
    }

}
