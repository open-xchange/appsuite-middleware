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

    private List<S> list = null;
    
    /**
     * Initializes a new {@link ServiceLoader}.
     * @param list
     */
    private ServiceLoader(final List<S> list) {
        super();
        this.list = list;
    }

    public static <S> ServiceLoader<S> load(final Class<S> service) throws IllegalAccessException, InstantiationException, ClassNotFoundException, IOException {
        return load(service, Thread.currentThread().getContextClassLoader());
    }
    
    public static <S> ServiceLoader<S> load(final Class<S> service, final ClassLoader loader) throws IllegalAccessException, InstantiationException, ClassNotFoundException, IOException {
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
                    throw e;
                } catch (final InstantiationException e) {
                    throw e;
                } catch (final IllegalAccessException e) {
                    throw e;
                } finally {
                    stringReader.close();
                }
            }
        } catch (final IOException e1) {
            throw e1;
        }
        return new ServiceLoader<S>(retval);
    }
    
    @Override
    public Iterator<S> iterator() {
        return this.list.iterator();
    }

}
