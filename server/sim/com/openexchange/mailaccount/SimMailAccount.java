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

package com.openexchange.mailaccount;

import java.util.Map;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class SimMailAccount implements MailAccount {

    private String login;

    private int mailPort;

    private String mailProtocol;

    private boolean transportSecure;

    private boolean mailSecure;

    private String transportServer;

    private String transportProtocol;

    private int transportPort;

    private String transportPassword;

    private String transportLogin;

    private String primaryAddress;

    private String personal;

    private String password;

    private String name;

    private String mailServer;

    private Map<String, String> properties;
    
    public void setProperties(final Map<String, String> properties) {
        this.properties = properties;
    }

    public void setLogin(final String login) {
        this.login = login;
    }

    public void setMailPort(final int mailPort) {
        this.mailPort = mailPort;
    }

    public void setMailProtocol(final String mailProtocol) {
        this.mailProtocol = mailProtocol;
    }

    public void setTransportSecure(final boolean transportSecure) {
        this.transportSecure = transportSecure;
    }

    public void setMailSecure(final boolean mailSecure) {
        this.mailSecure = mailSecure;
    }

    public void setTransportServer(final String transportServer) {
        this.transportServer = transportServer;
    }

    public void setTransportProtocol(final String transportProtocol) {
        this.transportProtocol = transportProtocol;
    }

    public void setTransportPort(final int transportPort) {
        this.transportPort = transportPort;
    }

    public void setTransportPassword(final String transportPassword) {
        this.transportPassword = transportPassword;
    }

    public void setTransportLogin(final String transportLogin) {
        this.transportLogin = transportLogin;
    }

    public void setPrimaryAddress(final String primaryAddress) {
        this.primaryAddress = primaryAddress;
    }

    public void setPersonal(final String personal) {
        this.personal = personal;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setMailServer(final String mailServer) {
        this.mailServer = mailServer;
    }

    public void addProperty(final String name, final String value) {
        // TODO Auto-generated method stub

    }

    public String generateMailServerURL() {
        // TODO Auto-generated method stub
        return null;
    }

    public String generateTransportServerURL() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getConfirmedHam() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getConfirmedHamFullname() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getConfirmedSpam() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getConfirmedSpamFullname() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getDrafts() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getDraftsFullname() {
        // TODO Auto-generated method stub
        return null;
    }

    public int getId() {
        // TODO Auto-generated method stub
        return 0;
    }

    public String getLogin() {
        return login;
    }

    public int getMailPort() {
        return mailPort;
    }

    public String getMailProtocol() {
        return mailProtocol;
    }

    public String getMailServer() {
        return mailServer;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getPrimaryAddress() {
        return primaryAddress;
    }

    public String getPersonal() {
        return personal;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public String getSent() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getSentFullname() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getSpam() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getSpamFullname() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getSpamHandler() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getTransportLogin() {
        return transportLogin;
    }

    public String getTransportPassword() {
        return transportPassword;
    }

    public int getTransportPort() {
        return transportPort;
    }

    public String getTransportProtocol() {
        return transportProtocol;
    }

    public String getTransportServer() {
        return transportServer;
    }

    public String getTrash() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getTrashFullname() {
        // TODO Auto-generated method stub
        return null;
    }

    public int getUserId() {
        // TODO Auto-generated method stub
        return 0;
    }

    public boolean isDefaultAccount() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isMailSecure() {
        return mailSecure;
    }

    public boolean isTransportSecure() {
        return transportSecure;
    }

    public boolean isUnifiedINBOXEnabled() {
        // TODO Auto-generated method stub
        return false;
    }

}
