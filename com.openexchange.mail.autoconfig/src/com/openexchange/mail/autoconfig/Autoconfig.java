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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
 * {@link Autoconfig}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Autoconfig {

    private String mailServer;

    private String transportServer;

    private String mailProtocol;

    private String transportProtocol;

    private int mailPort;

    private int transportPort;

    private boolean mailSecure;

    private boolean transportSecure;

    private String username;

    /**
     * Gets the mailServer
     *
     * @return The mailServer
     */
    public String getMailServer() {
        return mailServer;
    }

    /**
     * Sets the mailServer
     *
     * @param mailServer The mailServer to set
     */
    public void setMailServer(String mailServer) {
        this.mailServer = mailServer;
    }

    /**
     * Gets the transportServer
     *
     * @return The transportServer
     */
    public String getTransportServer() {
        return transportServer;
    }

    /**
     * Sets the transportServer
     *
     * @param transportServer The transportServer to set
     */
    public void setTransportServer(String transportServer) {
        this.transportServer = transportServer;
    }

    /**
     * Gets the mailProtocol
     *
     * @return The mailProtocol
     */
    public String getMailProtocol() {
        return mailProtocol;
    }

    /**
     * Sets the mailProtocol
     *
     * @param mailProtocol The mailProtocol to set
     */
    public void setMailProtocol(String mailProtocol) {
        this.mailProtocol = mailProtocol;
    }

    /**
     * Gets the transportProtocol
     *
     * @return The transportProtocol
     */
    public String getTransportProtocol() {
        return transportProtocol;
    }

    /**
     * Sets the transportProtocol
     *
     * @param transportProtocol The transportProtocol to set
     */
    public void setTransportProtocol(String transportProtocol) {
        this.transportProtocol = transportProtocol;
    }

    /**
     * Gets the mailPort
     *
     * @return The mailPort
     */
    public int getMailPort() {
        return mailPort;
    }

    /**
     * Sets the mailPort
     *
     * @param mailPort The mailPort to set
     */
    public void setMailPort(int mailPort) {
        this.mailPort = mailPort;
    }

    /**
     * Gets the transportPort
     *
     * @return The transportPort
     */
    public int getTransportPort() {
        return transportPort;
    }

    /**
     * Sets the transportPort
     *
     * @param transportPort The transportPort to set
     */
    public void setTransportPort(int transportPort) {
        this.transportPort = transportPort;
    }

    /**
     * Gets the mailSecure
     *
     * @return The mailSecure
     */
    public boolean isMailSecure() {
        return mailSecure;
    }

    /**
     * Sets the mailSecure
     *
     * @param mailSecure The mailSecure to set
     */
    public void setMailSecure(boolean mailSecure) {
        this.mailSecure = mailSecure;
    }

    /**
     * Gets the transportSecure
     *
     * @return The transportSecure
     */
    public boolean isTransportSecure() {
        return transportSecure;
    }

    /**
     * Sets the transportSecure
     *
     * @param transportSecure The transportSecure to set
     */
    public void setTransportSecure(boolean transportSecure) {
        this.transportSecure = transportSecure;
    }

    /**
     * Gets the user name
     *
     * @return The user name
     */
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
    public String toString() {
        StringBuilder builder = new StringBuilder(256);
        builder.append("Autoconfig [");
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
            transportSecure).append(", ");
        if (username != null) {
            builder.append("username=").append(username);
        }
        builder.append("]");
        return builder.toString();
    }

}
