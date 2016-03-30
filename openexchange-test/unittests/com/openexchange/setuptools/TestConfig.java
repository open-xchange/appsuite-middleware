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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
package com.openexchange.setuptools;

import com.openexchange.exception.OXException;
import com.openexchange.configuration.AJAXConfig;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class TestConfig {
    private String user;
    private String secondUser;
    private String thirdUser;
    private String fourthUser;

    private String participant1, participant2, participant3;
    private String resource1, resource2, resource3;

    private final String oxAdminMaster, oxAdminMasterPwd;

    private String group;

    private final String contextName;

    public TestConfig() throws OXException {
        AJAXConfig.init();
        user = AJAXConfig.getProperty(AJAXConfig.Property.LOGIN);
        secondUser = AJAXConfig.getProperty(AJAXConfig.Property.SECONDUSER);
        thirdUser = AJAXConfig.getProperty(AJAXConfig.Property.THIRDLOGIN);
        fourthUser = AJAXConfig.getProperty(AJAXConfig.Property.FOURTHLOGIN);

        participant1 = AJAXConfig.getProperty(AJAXConfig.Property.USER_PARTICIPANT1);
        participant2 = AJAXConfig.getProperty(AJAXConfig.Property.USER_PARTICIPANT2);
        participant3 = AJAXConfig.getProperty(AJAXConfig.Property.USER_PARTICIPANT3);

        resource1 = AJAXConfig.getProperty(AJAXConfig.Property.RESOURCE_PARTICIPANT1);
        resource2 = AJAXConfig.getProperty(AJAXConfig.Property.RESOURCE_PARTICIPANT2);
        resource3 = AJAXConfig.getProperty(AJAXConfig.Property.RESOURCE_PARTICIPANT3);

        oxAdminMaster = AJAXConfig.getProperty(AJAXConfig.Property.OX_ADMIN_MASTER);
        oxAdminMasterPwd = AJAXConfig.getProperty(AJAXConfig.Property.OX_ADMIN_MASTER_PWD);

        group = AJAXConfig.getProperty(AJAXConfig.Property.GROUP_PARTICIPANT);

        contextName = AJAXConfig.getProperty(AJAXConfig.Property.CONTEXTNAME);
    }

    public String getContextName() {
        return contextName;
    }

    public String getUser() {
        return user;
    }

    public void setUser(final String user) {
        this.user = user;
    }

    public String getSecondUser() {
        return secondUser;
    }

    public void setSecondUser(final String secondUser) {
        this.secondUser = secondUser;
    }

    public String getThirdUser() {
        return thirdUser;
    }

    public void setThirdUser(final String thirdUser) {
        this.thirdUser = thirdUser;
    }

    public String getFourthUser() {
        return fourthUser;
    }

    public void setFourthUser(final String fourthUser) {
        this.fourthUser = fourthUser;
    }

    public String getParticipant1() {
        return participant1;
    }

    public void setParticipant1(final String participant1) {
        this.participant1 = participant1;
    }

    public String getParticipant2() {
        return participant2;
    }

    public void setParticipant2(final String participant2) {
        this.participant2 = participant2;
    }

    public String getParticipant3() {
        return participant3;
    }

    public void setParticipant3(final String participant3) {
        this.participant3 = participant3;
    }

    public String getResource1() {
        return resource1;
    }

    public void setResource1(final String resource1) {
        this.resource1 = resource1;
    }

    public String getResource2() {
        return resource2;
    }

    public void setResource2(final String resource2) {
        this.resource2 = resource2;
    }

    public String getResource3() {
        return resource3;
    }

    public void setResource3(final String resource3) {
        this.resource3 = resource3;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(final String group) {
        this.group = group;
    }

    public String getOxAdminMaster() {
        return oxAdminMaster;
    }

    public String getOxAdminMasterPwd() {
        return oxAdminMasterPwd;
    }
}
