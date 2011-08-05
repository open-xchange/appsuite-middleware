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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.tools.ant.BuildException;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * Parser for ProjectSet files.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ProjectSetFileReader {

    public static Map<String, Repository> parse(String projectSetFileName) {
        try {
            Document document = new SAXBuilder().build(new File(projectSetFileName));
            Element psf = document.getRootElement();
            if (!"psf".equals(psf.getName())) {
                throw new BuildException("Root element is not a PSF but a " + psf.toString());
            }
            final Map<String, Repository> projects = new HashMap<String, Repository>();
            for (Object tmp : psf.getChildren()) {
                if (!(tmp instanceof Element)) {
                    throw new BuildException("PSF child is not an element but a " + tmp.getClass().getName());
                }
                for (Repository repository : parseProvider((Element) tmp)) {
                    projects.put(repository.getProjectName(), repository);
                }
            }
            return projects;
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            throw new BuildException("lala" + sw.toString());
        }
    }

    private static Set<Repository> parseProvider(Element providerElement) {
        if (!"provider".equals(providerElement.getName())) {
            throw new BuildException("PSF child is not a provider but a " + providerElement.getName());
        }
        Attribute id = providerElement.getAttribute("id");
        if (null == id) {
            throw new BuildException("Attribute id for tag provider is missing");
        }
        String providerName = id.getValue();
        RepositoryType type = RepositoryType.byProvider(providerName);
        if (null == type) {
            throw new BuildException("Unknown provider " + providerName);
        }
        final Set<Repository> repositories = new HashSet<Repository>();
        for (Object tmp : providerElement.getChildren()) {
            if (!(tmp instanceof Element)) {
                throw new BuildException("Provider child is not an element but a " + tmp.getClass().getName());
            }
            repositories.add(parseProject((Element) tmp, type));
        }
        return repositories;
    }

    private static Repository parseProject(Element projectElement, RepositoryType type) {
        if (!"project".equals(projectElement.getName())) {
            throw new BuildException("Provider child is not a project but a " + projectElement.getName());
        }
        Attribute reference = projectElement.getAttribute("reference");
        if (null == reference) {
            throw new BuildException("Attribute reference for tag project is missing");
        }
        String projectReference = reference.getValue();
        final Repository repository;
        switch (type) {
        case CVS:
            repository = parseCVSReference(projectReference);
            break;
        case SVN:
            repository = parseSVNReference(projectReference);
            break;
        case Git:
            repository = parseGitReference(projectReference);
            break;
        default:
            throw new BuildException("Unknown repository type " + type.name());
        }
        return repository;
    }

    private static CVSRepository parseCVSReference(String reference) {
        final CVSRepository retval;
        String[] parts = reference.split(",");
        if (!"1.0".equals(parts[0])) {
            throw new BuildException("Unknown CVS reference version " + parts[0]);
        }
        switch (parts.length) {
        case 4:
            retval = new CVSRepository(parts[1], parts[2], parts[3]);
            break;
        case 5:
            retval = new CVSRepository(parts[1], parts[2], parts[3], parts[4]);
            break;
        default:
            throw new BuildException("Unknown number of CVS reference definition parts.");
        }
        return retval;
    }

    private static Repository parseSVNReference(String reference) {
        throw new BuildException("Parsing a SVN repository reference is currently not implemented.");
    }

    private static Repository parseGitReference(String reference) {
        // 1.0,https://git.open-xchange.com/git/wd/backend,master,build
        String[] parts = reference.split(",");
        if (4 != parts.length) {
            throw new BuildException("Unknown number of Git reference definition parts.");
        }
        if (!"1.0".equals(parts[0])) {
            throw new BuildException("Unknown CVS reference version " + parts[0]);
        }
        return new GitRepository(parts[1], parts[2], parts[3]);
    }
}
