package com.openexchange.database.liquibase.osgi;

import java.io.IOException;
import java.net.URL;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import liquibase.servicelocator.DefaultPackageScanClassResolver;
import liquibase.servicelocator.PackageScanFilter;
import org.osgi.framework.Bundle;

/**
 * Package scan resolver that works with OSGI frameworks (in theory all of them)
 */
public class OSGIPackageScanClassResolver2 extends
DefaultPackageScanClassResolver {

    private final Bundle bundle;

    public OSGIPackageScanClassResolver2(Bundle bundle) {
        this.bundle = bundle;
    }

    @Override
    protected void find(PackageScanFilter test, String packageName,
        Set<Class<?>> classes) {
        // FIXME Only scan the jar file once.
        // FIXME Copy this to the v3 project.
        packageName = packageName.replace('.', '/');

        URL url = bundle.getEntry("/lib/liquibase.jar");
        try {
            JarInputStream jis = new JarInputStream(url.openStream());
            JarEntry next;
            while ((next = jis.getNextJarEntry()) != null) {
                String name = next.getName();
                if (name.startsWith(packageName)) {
                    String remaining = name.substring(packageName.length());
                    if (remaining.startsWith("/")) {
                        remaining = remaining.substring(1);
                    }
                    if (remaining.endsWith(".class")) {
                        String fixedName = name.substring(0, name.indexOf('.'))
                            .replace('/', '.');
                        try {
                            Class<?> klass = bundle.loadClass(fixedName);
                            if (test.matches(klass)) {
                                System.out.println("Class matched: "
                                    + fixedName);
                                classes.add(klass);
                            }
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
            jis.close();
        } catch (IOException e) {

        }

    }
}

