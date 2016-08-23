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

    private TransportAuth transportAuth;

    private String transportServer;

    private String transportProtocol;

    private int transportPort;

    private String transportPassword;

    private String transportLogin;

    private String primaryAddress;

    private String personal;

    private String replyTo;

    private String password;

    private String name;

    private String mailServer;

    private Map<String, String> properties;
    private Map<String, String> transportProperties;

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

    public void setTransportAuth(TransportAuth transportAuth) {
        this.transportAuth = transportAuth;
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

    public void setReplyTo(final String replyTo) {
        this.replyTo = replyTo;
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

    @Override
    public void addProperty(final String name, final String value) {
        // Nothing to do

    }

    @Override
    public String generateMailServerURL() {
        // Nothing to do
        return null;
    }

    @Override
    public String generateTransportServerURL() {
        // Nothing to do
        return null;
    }

    @Override
    public String getConfirmedHam() {
        // Nothing to do
        return null;
    }

    @Override
    public String getConfirmedHamFullname() {
        // Nothing to do
        return null;
    }

    @Override
    public String getConfirmedSpam() {
        // Nothing to do
        return null;
    }

    @Override
    public String getConfirmedSpamFullname() {
        // Nothing to do
        return null;
    }

    @Override
    public String getDrafts() {
        // Nothing to do
        return null;
    }

    @Override
    public String getDraftsFullname() {
        // Nothing to do
        return null;
    }

    @Override
    public int getId() {
        // Nothing to do
        return 0;
    }

    @Override
    public String getLogin() {
        return login;
    }

    @Override
    public int getMailPort() {
        return mailPort;
    }

    @Override
    public String getMailProtocol() {
        return mailProtocol;
    }

    @Override
    public String getMailServer() {
        return mailServer;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getPrimaryAddress() {
        return primaryAddress;
    }

    @Override
    public String getPersonal() {
        return personal;
    }

    @Override
    public String getReplyTo() {
        return replyTo;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public String getSent() {
        // Nothing to do
        return null;
    }

    @Override
    public String getSentFullname() {
        // Nothing to do
        return null;
    }

    @Override
    public String getSpam() {
        // Nothing to do
        return null;
    }

    @Override
    public String getSpamFullname() {
        // Nothing to do
        return null;
    }

    @Override
    public String getSpamHandler() {
        // Nothing to do
        return null;
    }

    @Override
    public String getTransportLogin() {
        return transportLogin;
    }

    @Override
    public String getTransportPassword() {
        return transportPassword;
    }

    @Override
    public int getTransportPort() {
        return transportPort;
    }

    @Override
    public String getTransportProtocol() {
        return transportProtocol;
    }

    @Override
    public String getTransportServer() {
        return transportServer;
    }

    @Override
    public TransportAuth getTransportAuth() {
        return transportAuth;
    }

    @Override
    public String getTrash() {
        // Nothing to do
        return null;
    }

    @Override
    public String getTrashFullname() {
        // Nothing to do
        return null;
    }

    @Override
    public int getUserId() {
        // Nothing to do
        return 0;
    }

    @Override
    public boolean isDefaultAccount() {
        // Nothing to do
        return false;
    }

    @Override
    public boolean isMailSecure() {
        return mailSecure;
    }

    @Override
    public boolean isTransportSecure() {
        return transportSecure;
    }

    @Override
    public boolean isUnifiedINBOXEnabled() {
        // Nothing to do
        return false;
    }

    @Override
    public String getArchive() {
        return null;
    }

    @Override
    public String getArchiveFullname() {
        return null;
    }

    @Override
    public Map<String, String> getTransportProperties() {
        return transportProperties;
    }

    @Override
    public void addTransportProperty(String name, String value) {
        // nothing to do
    }

    @Override
    public boolean isMailStartTls() {
        return false;
    }

    @Override
    public boolean isTransportStartTls() {
        return false;
    }

    @Override
    public int getMailOAuthId() {
        return -1;
    }

    @Override
    public int getTransportOAuthId() {
        return -1;
    }

    @Override
    public boolean isMailOAuthAble() {
        return false;
    }

    @Override
    public boolean isTransportOAuthAble() {
        return false;
    }

    @Override
    public String getRootFolder() {
        return null;
    }

}
