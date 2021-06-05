package com.openexchange.mail.autoconfig;


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

/**
 * {@link IndividualAutoconfig}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class IndividualAutoconfig extends DefaultAutoconfig {

    private final DefaultAutoconfig delegate;
    private String individualUsername;

    public IndividualAutoconfig(DefaultAutoconfig autoconfig) {
        super();
        this.delegate = autoconfig;
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public String getMailServer() {
        return delegate.getMailServer();
    }

    @Override
    public void setMailServer(String mailServer) {
        delegate.setMailServer(mailServer);
    }

    @Override
    public String getTransportServer() {
        return delegate.getTransportServer();
    }

    @Override
    public void setTransportServer(String transportServer) {
        delegate.setTransportServer(transportServer);
    }

    @Override
    public String getMailProtocol() {
        return delegate.getMailProtocol();
    }

    @Override
    public void setMailProtocol(String mailProtocol) {
        delegate.setMailProtocol(mailProtocol);
    }

    @Override
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    @Override
    public String getTransportProtocol() {
        return delegate.getTransportProtocol();
    }

    @Override
    public void setTransportProtocol(String transportProtocol) {
        delegate.setTransportProtocol(transportProtocol);
    }

    @Override
    public Integer getMailPort() {
        return delegate.getMailPort();
    }

    @Override
    public void setMailPort(int mailPort) {
        delegate.setMailPort(mailPort);
    }

    @Override
    public Integer getTransportPort() {
        return delegate.getTransportPort();
    }

    @Override
    public void setTransportPort(int transportPort) {
        delegate.setTransportPort(transportPort);
    }

    @Override
    public Boolean isMailSecure() {
        return delegate.isMailSecure();
    }

    @Override
    public void setMailSecure(boolean mailSecure) {
        delegate.setMailSecure(mailSecure);
    }

    @Override
    public Boolean isTransportSecure() {
        return delegate.isTransportSecure();
    }

    @Override
    public void setTransportSecure(boolean transportSecure) {
        delegate.setTransportSecure(transportSecure);
    }

    @Override
    public String getUsername() {
        return individualUsername;
    }

    @Override
    public void setUsername(String username) {
        individualUsername = username;
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

}
