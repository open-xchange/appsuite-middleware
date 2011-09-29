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
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 * A bundle already containing the compiled .class files. This class currently only works if all .class files are contained in a JAR listed
 * in the MANIFEST.MF.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class BinDirModule extends DirModule {

    private File dir;
    private final List<String> classpathDependencies = new LinkedList<String>();
    private final Set<String> exportedClasspath = new HashSet<String>();
    private final Set<String> requiredClasspath = new HashSet<String>();

    public BinDirModule(final String name) {
        super();
        this.name = name;
    }

    public BinDirModule(final File dir) {
        this.name = dir.getName();
    }

    private void readFiles(final File manifestFile) {
        try {
            if (manifestFile.exists() && manifestFile.length() != 0) {
                this.osgiManifest = new OSGIManifest(manifestFile);
                // read Bundle-ClassPath:
                for (String classpathEntry : osgiManifest.getListEntry(OSGIManifest.BUNDLE_CLASSPATH)) {
                    if (!classpathEntry.equals(".")) {
                        if (isExported(new File(dir, classpathEntry))) {
                            exportedClasspath.add(classpathEntry);
                        }
                    } else {
                        // TODO scan for .class files
                    }
                }
            }
        } catch (final Exception e) {
            throw new BuildException(e);
        }
    }

    @Override
    public void readLocalFiles(final Project project, final File rootDir) {
        dir = new File(rootDir, name);
        readFiles(new File(dir, "/META-INF/MANIFEST.MF"));
    }

    @Override
    public void computeDependencies(final Map<String, AbstractModule> projectsByName, final Map<String, Set<AbstractModule>> projectsByPackage) {
        for (final String classpathProject : classpathDependencies) {
            final AbstractModule module = projectsByName.get(classpathProject);
            if (module != null && module != this) {
                dependencies.add(module);
            }
        }
        super.computeDependencies(projectsByName, projectsByPackage);
    }

    @Override
    public Set<String> getExportedClasspath() {
        final Set<String> retval = new HashSet<String>();
        for (final String classpathEntry : exportedClasspath) {
            retval.add(dir.getAbsolutePath() + File.separatorChar + classpathEntry);
        }
        return Collections.unmodifiableSet(retval);
    }

    @Override
    public Set<String> getRequiredClasspath() {
        Set<String> retval = super.getRequiredClasspath();
        if (!requiredClasspath.isEmpty()) {
            final Set<String> tmp = new HashSet<String>();
            tmp.addAll(retval);
            for (final String classpathEntry : requiredClasspath) {
                tmp.add(dir.getAbsolutePath() + File.separatorChar + classpathEntry);
            }
            retval = Collections.unmodifiableSet(tmp); 
        }
        return retval;
    }
}
