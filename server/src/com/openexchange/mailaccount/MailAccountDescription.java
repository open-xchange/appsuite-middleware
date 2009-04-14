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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import java.io.Serializable;

/**
 * {@link MailAccountDescription} - Container object describing a mail account to insert/update.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailAccountDescription implements Serializable {

    private static final long serialVersionUID = -2443656355399068302L;

    private int id;

    private String login;

    private String password;

    private String mailServerURL;

    private String name;

    private String primaryAddress;

    private String transportServerURL;

    private String spamHandler;

    private String trash;

    private String sent;

    private String drafts;

    private String spam;

    private String confirmedSpam;

    private String confirmedHam;

    private boolean defaultFlag;

    /**
     * Initializes a new {@link MailAccountDescription}.
     */
    public MailAccountDescription() {
        super();
    }

    /**
     * Gets the ID.
     * 
     * @return The ID
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the login.
     * 
     * @return The login
     */
    public String getLogin() {
        return login;
    }

    /**
     * Gets the mail server URL.
     * 
     * @return The mail server URL
     */
    public String getMailServerURL() {
        return mailServerURL;
    }

    /**
     * Gets the account name.
     * 
     * @return The account name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets clear, non-encrypted password.
     * 
     * @return The clear, non-encrypted password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets the primary email address.
     * 
     * @return The primary email address
     */
    public String getPrimaryAddress() {
        return primaryAddress;
    }

    /**
     * Gets the transport server URL.
     * 
     * @return The transport server URL
     */
    public String getTransportServerURL() {
        return transportServerURL;
    }

    /**
     * Gets the ID
     * 
     * @return The ID
     */
    public void setId(final int id) {
        this.id = id;
    }

    /**
     * Sets the login.
     * 
     * @param login The login
     */
    public void setLogin(final String login) {
        this.login = login;
    }

    /**
     * Sets the clear-text password (which is stored encrypted).
     * 
     * @param password The clear-text password
     */
    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * Sets the mail server URL.
     * 
     * @param mailServerURL The mail server URL
     */
    public void setMailServerURL(final String mailServerURL) {
        this.mailServerURL = mailServerURL;
    }

    /**
     * Sets the account name.
     * 
     * @param name The account name.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Sets the primary email address.
     * 
     * @param primaryAddress The primary email address
     */
    public void setPrimaryAddress(final String primaryAddress) {
        this.primaryAddress = primaryAddress;
    }

    /**
     * Sets the transport server URL.
     * 
     * @param transportServerURL The transport server URL
     */
    public void setTransportServerURL(final String transportServerURL) {
        this.transportServerURL = transportServerURL;
    }

    /**
     * Gets the name for default drafts folder.
     * 
     * @return The name for default drafts folder
     */
    public String getDrafts() {
        return drafts;
    }

    /**
     * Gets the name for default sent folder.
     * 
     * @return The name for default sent folder
     */
    public String getSent() {
        return sent;
    }

    /**
     * Gets the name for default spam folder.
     * 
     * @return The name for default spam folder
     */
    public String getSpam() {
        return spam;
    }

    /**
     * Gets the name for default trash folder.
     * 
     * @return The name for default trash folder
     */
    public String getTrash() {
        return trash;
    }

    /**
     * Gets the name for default confirmed-ham folder.
     * 
     * @return The name for default confirmed-ham folder
     */
    public String getConfirmedHam() {
        return confirmedHam;
    }

    /**
     * Gets the name for default confirmed-spam folder.
     * 
     * @return The name for default confirmed-spam folder
     */
    public String getConfirmedSpam() {
        return confirmedSpam;
    }

    /**
     * Gets the spam handler name.
     * 
     * @return The spam handler name
     */
    public String getSpamHandler() {
        return spamHandler;
    }

    /**
     * Sets the name for default trash folder.
     * 
     * @param trash The name for default trash folder
     */
    public void setTrash(final String trash) {
        this.trash = trash;
    }

    /**
     * Sets the name for default sent folder.
     * 
     * @param sent The name for default sent folder
     */
    public void setSent(final String sent) {
        this.sent = sent;
    }

    /**
     * Sets the name for default drafts folder.
     * 
     * @param drafts The name for default drafts folder
     */
    public void setDrafts(final String drafts) {
        this.drafts = drafts;
    }

    /**
     * Sets the name for default spam folder.
     * 
     * @param spam The name for spam trash folder
     */
    public void setSpam(final String spam) {
        this.spam = spam;
    }

    /**
     * Sets the name for default confirmed-spam folder.
     * 
     * @param confirmedSpam The name for default confirmed-spam folder
     */
    public void setConfirmedSpam(final String confirmedSpam) {
        this.confirmedSpam = confirmedSpam;
    }

    /**
     * Sets the name for default confirmed-ham folder.
     * 
     * @param confirmedHam The name for default confirmed-ham folder
     */
    public void setConfirmedHam(final String confirmedHam) {
        this.confirmedHam = confirmedHam;
    }

    /**
     * Sets the spam handler name.
     * 
     * @param spamHandler The spam handler name
     */
    public void setSpamHandler(final String spamHandler) {
        this.spamHandler = spamHandler;
    }

    /**
     * Checks if mail account denotes the default mail account.
     * 
     * @return <code>true</code> if mail account denotes the default mail account; otherwise <code>false</code>
     */
    public boolean isDefaultFlag() {
        return defaultFlag;
    }

    /**
     * Sets whether mail account denotes the default mail account.
     * 
     * @param defaultFlag <code>true</code> if mail account denotes the default mail account; otherwise <code>false</code>
     */
    public void setDefaultFlag(final boolean defaultFlag) {
        this.defaultFlag = defaultFlag;
    }


}
