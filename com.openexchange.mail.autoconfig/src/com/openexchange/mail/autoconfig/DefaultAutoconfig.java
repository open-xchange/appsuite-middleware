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

package com.openexchange.mail.autoconfig;


/**
 * {@link DefaultAutoconfig} - The default auto-configuration implementation.
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DefaultAutoconfig implements Autoconfig {

    private String mailServer;
    private String transportServer;
    private String mailProtocol;
    private String transportProtocol;
    private Integer mailPort;
    private Integer transportPort;
    private Boolean mailSecure;
    private Boolean transportSecure;
    private String username;
    private String source;
    private boolean mailStartTls;
    private boolean transportStartTls;
    private Integer mailOAuthId;
    private Integer transportOAuthId;

    /**
     * Initializes a new ranked {@link DefaultAutoconfig}.
     */
    public DefaultAutoconfig() {
        super();
    }

    @Override
    public String getSource() {
        return source;
    }

    /**
     * Sets the source
     *
     * @param source The source to set
     */
    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public String getMailServer() {
        return mailServer;
    }

    /**
     * Sets the mail server
     *
     * @param mailServer The mail server to set
     */
    public void setMailServer(String mailServer) {
        this.mailServer = mailServer;
    }

    @Override
    public String getTransportServer() {
        return transportServer;
    }

    /**
     * Sets the transport server
     *
     * @param transportServer The transport server to set
     */
    public void setTransportServer(String transportServer) {
        this.transportServer = transportServer;
    }

    @Override
    public String getMailProtocol() {
        return mailProtocol;
    }

    /**
     * Sets the mail protocol
     *
     * @param mailProtocol The mail protocol to set
     */
    public void setMailProtocol(String mailProtocol) {
        this.mailProtocol = mailProtocol;
    }

    @Override
    public String getTransportProtocol() {
        return transportProtocol;
    }

    /**
     * Sets the transport protocol
     *
     * @param transportProtocol The transport protocol to set
     */
    public void setTransportProtocol(String transportProtocol) {
        this.transportProtocol = transportProtocol;
    }

    @Override
    public Integer getMailPort() {
        return mailPort;
    }

    /**
     * Sets the mail port
     *
     * @param mailPort The mail port to set
     */
    public void setMailPort(int mailPort) {
        this.mailPort = Integer.valueOf(mailPort);
    }

    @Override
    public Integer getTransportPort() {
        return transportPort;
    }

    /**
     * Sets the transport port
     *
     * @param transportPort The transport port to set
     */
    public void setTransportPort(int transportPort) {
        this.transportPort = Integer.valueOf(transportPort);
    }

    @Override
    public Boolean isMailSecure() {
        return mailSecure;
    }

    /**
     * Sets the mail secure flag
     *
     * @param mailSecure The mail secure flag to set
     */
    public void setMailSecure(boolean mailSecure) {
        this.mailSecure = Boolean.valueOf(mailSecure);
    }

    @Override
    public Boolean isTransportSecure() {
        return transportSecure;
    }

    /**
     * Sets the transport secure flag
     *
     * @param transportSecure The transport secure flag to set
     */
    public void setTransportSecure(boolean transportSecure) {
        this.transportSecure = Boolean.valueOf(transportSecure);
    }

    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Sets the user name
     *
     * @param username The user name to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public boolean isMailStartTls() {
        return mailStartTls;
    }

    /**
     * Sets if STARTTLS is required for mail access.
     *
     * @param mailStartTls <code>true</code> if STARTTLS is required; otherwise <code>false</code>
     */
    public void setMailStartTls(boolean mailStartTls) {
        this.mailStartTls = mailStartTls;
    }

    @Override
    public boolean isTransportStartTls() {
        return transportStartTls;
    }

    /**
     * Sets if STARTTLS is required for mail transport.
     *
     * @param transportStartTls <code>true</code> if STARTTLS is required; otherwise <code>false</code>
     */
    public void setTransportStartTls(boolean transportStartTls) {
        this.transportStartTls = transportStartTls;
    }

    @Override
    public Integer getMailOAuthId() {
        return mailOAuthId;
    }

    /**
     * Sets the identifier of the OAuth account needed for mail access.
     *
     * @param mailOAuthId The OAuth account identifier
     */
    public void setMailOAuthId(int mailOAuthId) {
        this.mailOAuthId = Integer.valueOf(mailOAuthId);
    }

    @Override
    public Integer getTransportOAuthId() {
        return transportOAuthId;
    }

    /**
     * Sets the identifier of the OAuth account needed for mail transport.
     *
     * @param transportOAuthId The OAuth account identifier
     */
    public void setTransportOAuthId(int transportOAuthId) {
        this.transportOAuthId = Integer.valueOf(transportOAuthId);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(256);
        builder.append("DefaultAutoconfig [");
        if (mailServer != null) {
            builder.append("mailServer=").append(mailServer).append(", ");
        }
        if (transportServer != null) {
            builder.append("transportServer=").append(transportServer).append(", ");
        }
        if (mailProtocol != null) {
            builder.append("mailProtocol=").append(mailProtocol).append(", ");
        }
        if (transportProtocol != null) {
            builder.append("transportProtocol=").append(transportProtocol).append(", ");
        }
        builder.append("mailPort=").append(mailPort).append(", transportPort=").append(transportPort).append(", mailSecure=").append(mailSecure).append(", transportSecure=").append(
            transportSecure).append(", mailStartTls=").append(mailStartTls).append(", transportStartTls=").append(transportStartTls);
        if (mailOAuthId != null) {
            builder.append("mailOAuthId=").append(mailOAuthId).append(", ");
        }
        if (transportOAuthId != null) {
            builder.append("transportOAuthId=").append(transportOAuthId).append(", ");
        }
        if (username != null) {
            builder.append(", username=").append(username);
        }
        builder.append("]");
        return builder.toString();
    }

}
