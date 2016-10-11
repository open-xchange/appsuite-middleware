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

package com.openexchange.mail.autoconfig;

/**
 * {@link ImmutableAutoconfig} - The default auto-configuration implementation.
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ImmutableAutoconfig implements Autoconfig {

    /**
     * Creates a new builder instance
     *
     * @return The new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The builder to create an instance of <code>ImmutableAutoconfig</code> */
    public static final class Builder {

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
        private boolean mailOAuth;
        private boolean transportOAuth;

        Builder() {
            super();
        }

        public Builder mailServer(String mailServer) {
            this.mailServer = mailServer;
            return this;
        }

        public Builder transportServer(String transportServer) {
            this.transportServer = transportServer;
            return this;
        }

        public Builder mailProtocol(String mailProtocol) {
            this.mailProtocol = mailProtocol;
            return this;
        }

        public Builder transportProtocol(String transportProtocol) {
            this.transportProtocol = transportProtocol;
            return this;
        }

        public Builder mailPort(Integer mailPort) {
            this.mailPort = mailPort;
            return this;
        }

        public Builder transportPort(Integer transportPort) {
            this.transportPort = transportPort;
            return this;
        }

        public Builder mailSecure(Boolean mailSecure) {
            this.mailSecure = mailSecure;
            return this;
        }

        public Builder transportSecure(Boolean transportSecure) {
            this.transportSecure = transportSecure;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder source(String source) {
            this.source = source;
            return this;
        }

        public Builder mailStartTls(boolean mailStartTls) {
            this.mailStartTls = mailStartTls;
            return this;
        }

        public Builder transportStartTls(boolean transportStartTls) {
            this.transportStartTls = transportStartTls;
            return this;
        }

        public Builder mailOAuth(boolean mailOAuth) {
            this.mailOAuth = mailOAuth;
            return this;
        }

        public Builder transportOAuth(boolean transportOAuth) {
            this.transportOAuth = transportOAuth;
            return this;
        }

        public ImmutableAutoconfig build() {
            return new ImmutableAutoconfig(mailServer, transportServer, mailProtocol, transportProtocol, mailPort, transportPort, mailSecure, transportSecure, username, source, mailStartTls, transportStartTls, mailOAuth, transportOAuth);
        }
    }

    // ---------------------------------------------------------------------------

    private final String mailServer;
    private final String transportServer;
    private final String mailProtocol;
    private final String transportProtocol;
    private final Integer mailPort;
    private final Integer transportPort;
    private final Boolean mailSecure;
    private final Boolean transportSecure;
    private final String username;
    private final String source;
    private final boolean mailStartTls;
    private final boolean transportStartTls;
    private final boolean mailOAuth;
    private final boolean transportOAuth;

    ImmutableAutoconfig(String mailServer, String transportServer, String mailProtocol, String transportProtocol, Integer mailPort, Integer transportPort, Boolean mailSecure, Boolean transportSecure, String username, String source, boolean mailStartTls, boolean transportStartTls, boolean mailOAuth, boolean transportOAuth) {
        super();
        this.mailServer = mailServer;
        this.transportServer = transportServer;
        this.mailProtocol = mailProtocol;
        this.transportProtocol = transportProtocol;
        this.mailPort = mailPort;
        this.transportPort = transportPort;
        this.mailSecure = mailSecure;
        this.transportSecure = transportSecure;
        this.username = username;
        this.source = source;
        this.mailStartTls = mailStartTls;
        this.transportStartTls = transportStartTls;
        this.mailOAuth = mailOAuth;
        this.transportOAuth = transportOAuth;
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public String getMailServer() {
        return mailServer;
    }

    @Override
    public String getTransportServer() {
        return transportServer;
    }

    @Override
    public String getMailProtocol() {
        return mailProtocol;
    }

    @Override
    public String getTransportProtocol() {
        return transportProtocol;
    }

    @Override
    public Integer getMailPort() {
        return mailPort;
    }

    @Override
    public Integer getTransportPort() {
        return transportPort;
    }

    @Override
    public Boolean isMailSecure() {
        return mailSecure;
    }

    @Override
    public Boolean isTransportSecure() {
        return transportSecure;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isMailStartTls() {
        return mailStartTls;
    }

    @Override
    public boolean isTransportStartTls() {
        return transportStartTls;
    }

    @Override
    public boolean isMailOAuth() {
        return mailOAuth;
    }

    @Override
    public boolean isTransportOAuth() {
        return transportOAuth;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(256);
        builder.append("ImmutableAutoconfig [");
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
        builder.append("mailPort=").append(mailPort).append(", transportPort=").append(transportPort).append(", mailSecure=").append(mailSecure).append(", transportSecure=").append(transportSecure).append(", mailStartTls=").append(mailStartTls).append(", transportStartTls=").append(transportStartTls);
        if (username != null) {
            builder.append(", username=").append(username);
        }
        builder.append("]");
        return builder.toString();
    }

}
