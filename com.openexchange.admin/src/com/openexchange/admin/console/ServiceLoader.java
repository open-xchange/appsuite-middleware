package com.openexchange.admin.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;


/**
 * This class aims to be a compensation for the missing ServiceLoader in JDK 5.
 * Note that the instantiation isn't done lazily compared to the ServiceLoader in
 * JDK 6
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 * @param <S>
 */
// TODO: Remove this class by the ServiceLoader class of JDK 6 if JDK 5 is no more
// supported by OX
public class ServiceLoader<S> implements Iterable<S> {

    private Iterator<S> iter = null;
    
    /**
     * Initializes a new {@link ServiceLoader}.
     * @param iter
     */
    private ServiceLoader(final Iterator<S> iter) {
        super();
        this.iter = iter;
    }

    public static <S> ServiceLoader<S> load(final Class<S> service) {
        return load(service, Thread.currentThread().getContextClassLoader());
    }
    
    // TODO: This method can be moved to a more general class, and afterwards this abstract class can 
    // be converted to an interface
    public static <S> ServiceLoader<S> load(final Class<S> service, final ClassLoader loader) {
        final List<S> retval = new ArrayList<S>();
        try {
            final Enumeration<URL> resources = loader.getResources("META-INF/services/" + service.getName());
            while (resources.hasMoreElements()) {
                final URL nextElement = resources.nextElement();
                final BufferedReader stringReader = new BufferedReader(new InputStreamReader(nextElement.openStream(), "UTF-8"));
                try {
                    // We only read the first line...
                    final String readLine = stringReader.readLine();
                    final Class<?> forName = Class.forName(readLine);
                    if (!service.isAssignableFrom(forName)) {
                        // TODO: Use error logging from console tools here
                        System.err.println("The class given in the META-INF/services directory is no subclass of ContextConsoleInterface");
                        break;
                    }
                    final Class<? extends S> asSubclass = forName.asSubclass(service);
                    retval.add(asSubclass.newInstance());
                } catch (final ClassNotFoundException e) {
                    // We don't handle this exception, if there no corresponding path nothing will be returned here
                } catch (final InstantiationException e) {
                    // We don't handle this exception, if there no corresponding path nothing will be returned here
                } catch (final IllegalAccessException e) {
                    // We don't handle this exception, if there no corresponding path nothing will be returned here
                } finally {
                    stringReader.close();
                }
            }
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        return new ServiceLoader<S>(retval.iterator());
    }
    
    public Iterator<S> iterator() {
        return this.iter;
    }

}
