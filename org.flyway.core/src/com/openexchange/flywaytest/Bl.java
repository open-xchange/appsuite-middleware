package com.openexchange.flywaytest;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleContext;
import com.openexchange.flywaytest.osgi.OXResolver;


public class Bl implements CommandProvider {
    
    private BundleContext b;
    private OXResolver resolver;
    
    public Bl(BundleContext b, OXResolver resolver) {
        this.b = b;
        this.resolver = resolver;
    }

    @Override
    public String getHelp() {
        return null;
    }
    
    public Object _bla(CommandInterpreter in) {
        new FlywayTest(b, resolver);
        return null;
    }

}
