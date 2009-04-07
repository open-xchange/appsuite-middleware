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

package com.openexchange.mailaccount.internal;

import com.openexchange.mailaccount.MailAccount;

/**
 * {@link AbstractMailAccount} - Abstract mail account.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractMailAccount implements MailAccount {

    private static final long serialVersionUID = -641194838598605274L;

    protected int id;

    protected String login;

    protected String password;

    protected String mailServerURL;

    protected String name;

    protected String primaryAddress;

    protected String transportServerURL;

    protected int userId;

    protected String spamHandler;

    protected String trash;

    protected String sent;

    protected String drafts;

    protected String spam;

    protected String confirmedSpam;

    protected String confirmedHam;

    /**
     * Initializes a new {@link AbstractMailAccount}.
     */
    protected AbstractMailAccount() {
        super();
    }

    public int getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getMailServerURL() {
        return mailServerURL;
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

    public String getTransportServerURL() {
        return transportServerURL;
    }

    public int getUserId() {
        return userId;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public void setLogin(final String login) {
        this.login = login;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public void setMailServerURL(final String mailServerURL) {
        this.mailServerURL = mailServerURL;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setPrimaryAddress(final String primaryAddress) {
        this.primaryAddress = primaryAddress;
    }

    public void setTransportServerURL(final String transportServerURL) {
        this.transportServerURL = transportServerURL;
    }

    public void setUserId(final int userId) {
        this.userId = userId;
    }

    public String getDrafts() {
        return drafts;
    }

    public String getSent() {
        return sent;
    }

    public String getSpam() {
        return spam;
    }

    public String getTrash() {
        return trash;
    }

    public String getConfirmedHam() {
        return confirmedHam;
    }

    public String getConfirmedSpam() {
        return confirmedSpam;
    }

    public String getSpamHandler() {
        return spamHandler;
    }

    public void setTrash(final String trash) {
        this.trash = trash;
    }

    public void setSent(final String sent) {
        this.sent = sent;
    }

    public void setDrafts(final String drafts) {
        this.drafts = drafts;
    }

    public void setSpam(final String spam) {
        this.spam = spam;
    }

    public void setConfirmedSpam(final String confirmedSpam) {
        this.confirmedSpam = confirmedSpam;
    }

    public void setConfirmedHam(final String confirmedHam) {
        this.confirmedHam = confirmedHam;
    }

    public void setSpamHandler(final String spamHandler) {
        this.spamHandler = spamHandler;
    }
}
