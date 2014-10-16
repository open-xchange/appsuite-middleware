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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

package com.openexchange.drive;


/**
 * {@link DriveSettings}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveSettings {

    private String helpLink;
    private String serverVersion;
    private DriveQuota quota;
    private String supportedApiVersion;
    private String minApiVersion;

    /**
     * Initializes a new {@link DriveSettings}.
     */
    public DriveSettings() {
        super();
    }

    /**
     * Gets the helpLink
     *
     * @return The helpLink
     */
    public String getHelpLink() {
        return helpLink;
    }

    /**
     * Sets the helpLink
     *
     * @param helpLink The helpLink to set
     */
    public void setHelpLink(String helpLink) {
        this.helpLink = helpLink;
    }

    /**
     * Gets the quota
     *
     * @return The quota
     */
    public DriveQuota getQuota() {
        return quota;
    }

    /**
     * Sets the quota
     *
     * @param quota The quota to set
     */
    public void setQuota(DriveQuota quota) {
        this.quota = quota;
    }

    /**
     * Sets the server version
     *
     * @param serverVersion The server version to set
     */
    public void setServerVersion(String serverVersion) {
        this.serverVersion = serverVersion;
    }

    /**
     * Gets the server version
     *
     * @return The server version
     */
    public String getServerVersion() {
        return serverVersion;
    }

    /**
     * Gets the supportedApiVersion
     *
     * @return The supportedApiVersion
     */
    public String getSupportedApiVersion() {
        return supportedApiVersion;
    }

    /**
     * Sets the supportedApiVersion
     *
     * @param supportedApiVersion The supportedApiVersion to set
     */
    public void setSupportedApiVersion(String supportedApiVersion) {
        this.supportedApiVersion = supportedApiVersion;
    }

    /**
     * Gets the minApiVersion
     *
     * @return The minApiVersion
     */
    public String getMinApiVersion() {
        return minApiVersion;
    }

    /**
     * Sets the minApiVersion
     *
     * @param minApiVersion The minApiVersion to set
     */
    public void setMinApiVersion(String minApiVersion) {
        this.minApiVersion = minApiVersion;
    }

}
