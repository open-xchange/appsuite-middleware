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

import org.apache.tools.ant.BuildException;

/**
 * Represents a CVS repository.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class CVSRepository extends Repository {

    private final String cvsRoot;
    private final String repositoryLocation;
    private final String branch;

    public CVSRepository(final String cvsRoot, final String repositoryLocation, final String projectName) {
    	this(cvsRoot, repositoryLocation, projectName, "HEAD");
    }

    public CVSRepository(final String cvsRoot, final String repositoryLocation, final String projectName, final String branch) {
        super(projectName);
        this.cvsRoot = cvsRoot;
        this.repositoryLocation = repositoryLocation;
        this.branch = branch;
    }

    @Override
    public RepositoryType getRepositoryType() {
        return RepositoryType.CVS;
    }

    public String getCvsRoot() {
        return cvsRoot;
    }

    public String getRepositoryLocation() {
        return repositoryLocation;
    }

    public String getBranch() {
    	return branch;
    }

    static CVSRepository parseCVSReference(final String reference) {
        final CVSRepository retval;
        final String[] parts = reference.split(",");
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
}
