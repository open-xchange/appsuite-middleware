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

package com.openexchange.obs.ant.tasks;

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.httpclient.HttpException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import com.openexchange.obs.api.BuildServiceClient;
import com.openexchange.obs.api.BuildServiceException;
import com.openexchange.obs.api.PackageStatus;

/**
 * @author choeger
 *
 */
public class Wait4Project extends Task {

    private String bsprjname;

    private String bsreponame;

    private String bsuser;

    private String bspass;

    private String bsurl;

    private int bssleepdelay = 1000;

    /**
     * @return the bssleepdelay
     */
    public final int getBssleepdelay() {
        return bssleepdelay;
    }

    /**
     * @param bssleepdelay the bssleepdelay to set
     */
    public final void setBssleepdelay(int bssleepdelay) {
        this.bssleepdelay = bssleepdelay;
    }

    /**
     * @return the bsprjname
     */
    public final String getBsprjname() {
        return bsprjname;
    }

    /**
     * @param bsprjname the bsprjname to set
     */
    public final void setBsprjname(String bsprjname) {
        this.bsprjname = bsprjname;
    }

    /**
     * @return the bsreponame
     */
    public final String getBsreponame() {
        return bsreponame;
    }

    /**
     * @param bsreponame the bsreponame to set
     */
    public final void setBsreponame(String bsreponame) {
        this.bsreponame = bsreponame;
    }

    /**
     * @return the bsuser
     */
    public final String getBsuser() {
        return bsuser;
    }

    /**
     * @param bsuser the bsuser to set
     */
    public final void setBsuser(String bsuser) {
        this.bsuser = bsuser;
    }

    /**
     * @return the bspass
     */
    public final String getBspass() {
        return bspass;
    }

    /**
     * @param bspass the bspass to set
     */
    public final void setBspass(String bspass) {
        this.bspass = bspass;
    }

    /**
     * @return the bsurl
     */
    public final String getBsurl() {
        return bsurl;
    }

    /**
     * @param bsurl the bsurl to set
     */
    public final void setBsurl(String bsurl) {
        this.bsurl = bsurl;
    }


    /* (non-Javadoc)
     * @see org.apache.tools.ant.Task#execute()
     */
    @Override
    public void execute() throws BuildException {
        // check ant task arguments
        if (null == bsurl || 0 == bsurl.length()) {
            throw new BuildException("URL of build service is not defined.");
        }
        if (null == bsuser || 0 == bsuser.length()) {
            throw new BuildException("User for authenticating in build service is not defined.");
        }
        if (null == bspass || 0 == bspass.length()) {
            throw new BuildException("Password for authenticating in build service is not defined.");
        }
        if (null == bsprjname || 0 == bsprjname.length()) {
            throw new BuildException("Project in build service is not defined.");
        }
        if (null == bsreponame || 0 == bsreponame.length()) {
            throw new BuildException("Distribution in build service is not defined.");
        }
        if (bssleepdelay < 1000) {
            throw new BuildException("Sleep delay for polling build service must be greater than 1000.");
        }

        // endless loop until build finished

        final BuildServiceClient bsc; 
        try {
        	bsc = new BuildServiceClient(bsuser, bspass, bsurl);
        	PackageStatus[] statuses;
            do {
                Thread.sleep(bssleepdelay);
                statuses = bsc.checkProjectStatus(bsprjname, bsreponame);
            } while (bsc.isProjectBuilding(statuses) && !bsc.somePackageFailed(statuses));
            if (!bsc.isProjectSuccessfulBuilt(statuses)) {
                log(bsc.getProjectStatus(statuses), Project.MSG_ERR);
                throw new BuildException("Build on build service failed.");
            }
        } catch (XPathExpressionException e) {
            throw new BuildException(e.getMessage(), e);
        } catch (HttpException e) {
            throw new BuildException(e.getMessage(), e);
        } catch (IOException e) {
            throw new BuildException(e.getMessage(), e);
        } catch (BuildServiceException e) {
            throw new BuildException(e.getMessage(), e);
        } catch (InterruptedException e) {
            throw new BuildException(e.getMessage(), e);
        }
    }
}
