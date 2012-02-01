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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * Represents a bundle in its source form like in Eclipse. Additionally to the MANIFEST.MF the .classpath file is read to understand
 * possible dependencies.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class SrcDirModule extends DirModule {

    private File dir;
    private Set<String> sourceDirs = new HashSet<String>();
    private List<String> classpathDependencies = new LinkedList<String>();
    private Set<String> exportedClasspath = new HashSet<String>();
    private Set<String> requiredClasspath = new HashSet<String>();

    public SrcDirModule(final String name) {
        super();
        this.name = name;
    }

    public SrcDirModule(final File dir) {
        super();
        this.name = dir.getName();
    }

    private void readFiles(final File manifestFile, final File classpathFile) {
        try {
            if (manifestFile.exists() && manifestFile.length() != 0) {
                this.osgiManifest = new OSGIManifest(manifestFile);
                // read Bundle-ClassPath:
                for (final String classpathEntry : osgiManifest.getListEntry(OSGIManifest.BUNDLE_CLASSPATH)) {
                    // All class files are added through the <bin> directory
                    // TODO Only libs which have a corresponding export entry.
                    if (!classpathEntry.equals(".")) {
                        exportedClasspath.add(classpathEntry);
                    }
                }
            }
            // read .classpath file
            if (classpathFile.exists() && classpathFile.length() != 0) {
                final Document d = new SAXBuilder().build(classpathFile);
                final Element root = d.getRootElement();
                final List<?> list = root.getChildren("classpathentry");
                final Iterator<?> it = list.iterator();
                while (it.hasNext()) {
                    final Element entry = (Element) it.next();
                    if (entry.getAttribute("combineaccessrules") != null) {
                        final String path = entry.getAttributeValue("path");
                        classpathDependencies.add(path.substring(1));
                    } else if (entry.getAttributeValue("kind").equals("lib")) {
                        final String path = entry.getAttributeValue("path");
                        requiredClasspath.add(path);
                        if ("true".equals(entry.getAttributeValue("exported"))) {
                            exportedClasspath.add(path);
                        }
                    } else if (entry.getAttributeValue("kind").equals("src")) {
                        sourceDirs.add(entry.getAttributeValue("path"));
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
        readFiles(new File(dir, "/META-INF/MANIFEST.MF"), new File(dir, ".classpath"));
    }

    @Override
    public void computeDependencies(final Map<String, AbstractModule> projectsByName, final Map<String, Set<AbstractModule>> projectsByPackage, final boolean strict) {
        for (final String classpathProject : classpathDependencies) {
            final AbstractModule module = projectsByName.get(classpathProject);
            if (module != null && module != this) {
                dependencies.add(module);
            }
        }
        super.computeDependencies(projectsByName, projectsByPackage, strict);
    }

    @Override
    protected Set<String> getExportedClasspath() {
        final Set<String> retval = new HashSet<String>();
        retval.add(dir.getAbsolutePath() + File.separatorChar + "<bin>");
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

    public Set<String> getSourceDirs() {
        final Set<String> retval = new HashSet<String>();
        for (final String sourceDir : sourceDirs) {
            if (!"test".equals(sourceDir) && !"sim".equals(sourceDir)) {
                retval.add(dir.getAbsolutePath() + File.separatorChar + sourceDir);
            }
        }
        return Collections.unmodifiableSet(retval);
    }
}
