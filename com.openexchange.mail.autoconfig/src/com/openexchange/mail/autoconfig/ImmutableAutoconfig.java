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
        private Integer mailOAuthId;
        private Integer transportOAuthId;

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

        public Builder mailOAuthId(int mailOAuthId) {
            this.mailOAuthId = Integer.valueOf(mailOAuthId);
            return this;
        }

        public Builder transportOAuthId(int transportOAuthId) {
            this.transportOAuthId = Integer.valueOf(transportOAuthId);
            return this;
        }

        public ImmutableAutoconfig build() {
            return new ImmutableAutoconfig(mailServer, transportServer, mailProtocol, transportProtocol, mailPort, transportPort, mailSecure, transportSecure, username, source, mailStartTls, transportStartTls, mailOAuthId, transportOAuthId);
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
    private final Integer mailOAuthId;
    private final Integer transportOAuthId;

    ImmutableAutoconfig(String mailServer, String transportServer, String mailProtocol, String transportProtocol, Integer mailPort, Integer transportPort, Boolean mailSecure, Boolean transportSecure, String username, String source, boolean mailStartTls, boolean transportStartTls, Integer mailOAuthId, Integer transportOAuthId) {
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
        this.mailOAuthId = mailOAuthId;
        this.transportOAuthId = transportOAuthId;
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
    public Integer getMailOAuthId() {
        return mailOAuthId;
    }

    @Override
    public Integer getTransportOAuthId() {
        return transportOAuthId;
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
