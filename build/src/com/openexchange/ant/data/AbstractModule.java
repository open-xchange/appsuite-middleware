/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.ant.data;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import org.apache.tools.ant.BuildException;

/**
 * Abstract super class for all supported types of presentation of a bundle. Contains the general methods for handling imports and exports
 * and for generating the dependencies and the required class path.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public abstract class AbstractModule {

    protected String name;
    protected OSGIManifest osgiManifest;
    protected AbstractModule fragmentHost;
    protected Set<AbstractModule> dependencies = new HashSet<AbstractModule>();

    protected AbstractModule() {
        super();
    }

    public final Set<String> getExportedPackages() {
        final Set<String> retval;
        if (null == osgiManifest) {
            retval = Collections.emptySet();
        } else {
            retval = osgiManifest.getListEntry(OSGIManifest.EXPORT_PACKAGE);
        }
        return retval;
    }

    public void computeDependencies(final Map<String, AbstractModule> modulesByName, final Map<String, Set<AbstractModule>> modulesByPackage) {
        if (osgiManifest != null) {
            for (final String importedPackage : osgiManifest.getListEntry(OSGIManifest.IMPORT_PACKAGE)) {
                final Set<AbstractModule> exportingModules = modulesByPackage.get(importedPackage);
                if (exportingModules != null) {
                    for (final AbstractModule module : exportingModules) {
                        if (module != this) {
                            dependencies.add(module);
                        }
                    }
                } else {
                    throw new BuildException("Can not find bundle that exports \"" + importedPackage + "\"");
                }
            }
            for (final String requiredBundle : osgiManifest.getListEntry(OSGIManifest.REQUIRE_BUNDLE)) {
                final AbstractModule requiredModule = modulesByName.get(requiredBundle);
                if (requiredModule != null && requiredModule != this) {
                    dependencies.add(requiredModule);
                }
            }
            final String fragmentHostName = osgiManifest.getEntry(OSGIManifest.FRAGMENT_HOST);
            if (fragmentHostName != null) {
                fragmentHost = modulesByName.get(fragmentHostName);
                if (null == fragmentHost) {
                    throw new BuildException("Can not find bundle for fragment host \"" + fragmentHostName + "\".");
                }
                dependencies.add(fragmentHost);
            }
        }
    }

    public void computeDependenciesForFragments() {
        if (fragmentHost != null) {
            dependencies.addAll(fragmentHost.getDependencies());
            dependencies.remove(this); // just in case the fragment host "requires" the fragment
        }
    }

    public Set<AbstractModule> getDependencies() {
        return dependencies;
    }

    public Set<String> getRequiredClasspath() {
        final Set<String> retval = new HashSet<String>();
        for (final AbstractModule dependency : dependencies) {
            retval.addAll(dependency.getExportedClasspath());
        }
        return Collections.unmodifiableSet(retval);
    }

    public Set<String> getDeepRequiredClasspath() {
        final Stack<AbstractModule> seenModules = new Stack<AbstractModule>();
        return getDeepRequiredClasspath(seenModules, this);
    }

    private static Set<String> getDeepRequiredClasspath(final Stack<AbstractModule> seenModules, final AbstractModule module) {
        seenModules.push(module);
        final Set<String> retval = new HashSet<String>();
        for (final AbstractModule dependency : module.getDependencies()) {
            if (!seenModules.contains(dependency)) {
                retval.addAll(getDeepRequiredClasspath(seenModules, dependency));
            }
            retval.addAll(dependency.getExportedClasspath());
        }
        seenModules.pop();
        return retval;
    }

    protected final boolean isExported(final File classpathEntry) {
        if (!classpathEntry.exists()) {
            return false;
        }
        try {
            final JarFile jarFile = new JarFile(classpathEntry);
            for (final String exportedPackage : getExportedPackages()) {
                String expected = exportedPackage.replace('.', '/') + '/';
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (entry.getName().startsWith(expected)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            throw new BuildException(e);
        }
        return false;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (o.getClass() != this.getClass()) {
            return false;
        }
        final AbstractModule other = (AbstractModule) o;
        return this.name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public abstract Set<String> getExportedClasspath();
}
