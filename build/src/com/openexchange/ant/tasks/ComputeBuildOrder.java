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

package com.openexchange.ant.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import com.openexchange.ant.data.AbstractModule;
import com.openexchange.ant.data.BinDirModule;
import com.openexchange.ant.data.DependenciesSorter;
import com.openexchange.ant.data.DirModule;
import com.openexchange.ant.data.JarModule;
import com.openexchange.ant.data.SrcDirModule;

/**
 * {@link ComputeBuildOrder}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ComputeBuildOrder extends Task {

    private File dir;
    private String propertyName;
    private String projectList;
    private Path classpath;

    public ComputeBuildOrder() {
        super();
    }

    public final void setProjectList(final String projectList) {
        this.projectList = projectList;
    }

    public final void setPropertyName(final String propertyName) {
        this.propertyName = propertyName;
    }

    public final void setDir(final File dir) {
        this.dir = dir;
    }

    public final Path createClasspath() {
        classpath = new Path(getProject());
        return classpath;
    }

    @Override
    public void execute() throws BuildException {
        // Parse ClassPath-Jars
        log("using classpath: " + classpath, Project.MSG_INFO);
        final String[] classpathFiles = classpath.list();
        final List<AbstractModule> classpathModules = new ArrayList<AbstractModule>(classpathFiles.length);
        for (final String classpathFilename : classpathFiles) {
            final File classpathFile = new File(classpathFilename);
            if (classpathFile.isFile()) {
            	classpathModules.add(new JarModule(classpathFile));
            } else {
                DirModule classpathModule = new BinDirModule(classpathFile);
                classpathModule.readLocalFiles(getProject(), classpathFile.getParentFile());
                classpathModules.add(classpathModule);
            }
        }

        // All bundles from projectList need to be source bundles.
        List<AbstractModule> appModules = new ArrayList<AbstractModule>();
        final String[] split = projectList.split(",");
        for (int i = 0; i < split.length; i++) {
            DirModule module = new SrcDirModule(split[i]);
            module.readLocalFiles(getProject(), dir);
            appModules.add(module);
        }

        final List<AbstractModule> allModules = new ArrayList<AbstractModule>(classpathModules.size() + appModules.size());
        allModules.addAll(classpathModules);
        allModules.addAll(appModules);
        log("all modules: " + allModules, Project.MSG_INFO);

        // Build structures to help bundles resolving their dependencies.
        final Map<String, AbstractModule> modulesByName = new HashMap<String, AbstractModule>();
        for (final AbstractModule module : allModules) {
            modulesByName.put(module.getName(), module);
        }
        final Map<String, Set<AbstractModule>> modulesByPackage = new HashMap<String, Set<AbstractModule>>();
        for (AbstractModule module : allModules) {
            for (String exportedPackage : module.getExportedPackages()) {
                Set<AbstractModule> exportingModules = modulesByPackage.get(exportedPackage);
                if (exportingModules == null) {
                    exportingModules = new HashSet<AbstractModule>();
                    modulesByPackage.put(exportedPackage, exportingModules);
                }
                exportingModules.add(module);
            }
        }

        // Compute dependencies in the bundles.
        for (AbstractModule module : allModules) {
            module.computeDependencies(modulesByName, modulesByPackage);
        }
        for (AbstractModule module : allModules) {
            module.computeDependenciesForFragments();
        }

        // Generate a sorted list of bundles according to their dependencies.
        List<AbstractModule> sortedModules = new DependenciesSorter().sortDependencies(classpathModules, appModules);
        log("sorted modules: " + sortedModules, Project.MSG_INFO);

        sortedModules.removeAll(classpathModules); // only keep the app modules

        String moduleNamesList = getModuleNamesList(sortedModules, ',');
        getProject().setInheritedProperty(propertyName, moduleNamesList);

        // Set properties for each bundle.
        for (AbstractModule module : sortedModules) {
            // direct dependencies for compiling the classes.
            final StringBuilder requiredClasspath = new StringBuilder();
            for (final String classpathEntry : module.getRequiredClasspath()) {
                requiredClasspath.append(classpathEntry);
                requiredClasspath.append(',');
            }
            if (requiredClasspath.length() > 0) {
                // first package will not have dependencies.
                requiredClasspath.setLength(requiredClasspath.length() - 1);
            }
            log(module.getName() + ".requiredClasspath: " + requiredClasspath, Project.MSG_DEBUG);
            getProject().setInheritedProperty(module.getName() + ".requiredClasspath", requiredClasspath.toString());
            // deep dependencies for executing the classes.
            final StringBuilder deepClasspath = new StringBuilder();
            for (final String classpathEntry : module.getDeepRequiredClasspath()) {
                deepClasspath.append(classpathEntry);
                deepClasspath.append(',');
            }
            if (deepClasspath.length() > 0) {
                // first package will not have dependencies.
                deepClasspath.setLength(deepClasspath.length() - 1);
            }
            log(module.getName() + ".deepClasspath: " + deepClasspath, Project.MSG_DEBUG);
            getProject().setInheritedProperty(module.getName() + ".deepClasspath", deepClasspath.toString());
        }
    }

    private String getModuleNamesList(final Collection<AbstractModule> modules, final char delimiter) {
        final StringBuffer buffer = new StringBuffer();
        for (AbstractModule module : modules) {
            buffer.append(module.getName());
            buffer.append(delimiter);
        }
        if (buffer.length() > 0) {
            buffer.setLength(buffer.length() - 1);
        }
        return buffer.toString();
    }
}
