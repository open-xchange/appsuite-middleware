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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import com.openexchange.ant.data.CVSRepository;
import com.openexchange.ant.data.GitRepository;
import com.openexchange.ant.data.ProjectSetFileReader;
import com.openexchange.ant.data.Repository;

/**
 * Parses ProjectSet files, outputs that to the named property and sets project specific properties to fetch the projects from a repository.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class CreateProjectList extends Task {

    private Path projectSets;
    private String name;

    public CreateProjectList() {
        super();
    }

    public final void setName(final String name) {
        this.name = name;
    }

    public final Path createProjectSets() {
        projectSets = new Path(getProject());
        return projectSets;
    }

    @Override
    public final void execute() throws BuildException {
        // Parse the PSF files.
        final String[] projectSetFiles = projectSets.list();
        final Map<String, Repository> projects = new HashMap<String, Repository>();
        for (final String projectSetFile : projectSetFiles) {
            log("Parsing " + projectSetFile, Project.MSG_INFO);
            projects.putAll(ProjectSetFileReader.parse(projectSetFile));
        }

        // Log the project list and output the property for it.
        final String projectNamesList = joinCommaSeparated(projects.keySet());
        log(name + "=" + projectNamesList, Project.MSG_INFO);
        getProject().setInheritedProperty(name, projectNamesList);

        // Set the project specific properties for accessing the repository for that project.
        for (final Map.Entry<String, Repository> entry : projects.entrySet()) {
            final String projectName = entry.getKey();
            final Repository repository = entry.getValue();
            switch (repository.getRepositoryType()) {
            case CVS:
                final CVSRepository cvsRepo = (CVSRepository) repository;
                getProject().setInheritedProperty(projectName + ".cvsRoot", cvsRepo.getCvsRoot());
                getProject().setInheritedProperty(projectName + ".repositoryLocation", cvsRepo.getRepositoryLocation());
                getProject().setInheritedProperty(projectName + ".branch", cvsRepo.getBranch());
                break;
            case Git:
                final GitRepository gitRepo = (GitRepository) repository;
                getProject().setInheritedProperty(projectName + ".remote", gitRepo.getRemote());
                getProject().setInheritedProperty(projectName + ".branch", gitRepo.getBranch());
                break;
            default:
                throw new BuildException("Unknown repository type " + repository.getRepositoryType() + ".");
            }
        }
    }

    /**
     * Generates a comma separated list of the projects.
     *
     * @param projects Projects to put into that list.
     * @return
     */
    public String joinCommaSeparated(final Set<String> projects) {
        final StringBuffer buffer = new StringBuffer();
        for (final String project : projects) {
            buffer.append(project);
            buffer.append(',');
        }
        if (buffer.length() > 0) {
            buffer.setLength(buffer.length() - 1);
        }
        return buffer.toString();
    }
}
