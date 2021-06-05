/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.mailaccount;

import java.util.Map;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class SimMailAccount implements MailAccount {

    private static final long serialVersionUID = -8723018808153206584L;

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

    @SuppressWarnings("unused")
    private int failedAuthCount;

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

    /**
     * Sets the number of failed authentication attempts
     *
     * @param failedAuthCount The number of failed authentication attempts
     */
    public void setFailedAuthCount(int failedAuthCount) {
        this.failedAuthCount = failedAuthCount;
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

    @Override
    public boolean isMailDisabled() {
        return false;
    }

    @Override
    public boolean isTransportDisabled() {
        return false;
    }

}
