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
 * {@link MailAccount} - Provides all necessary information for a user's mail account like server, port, login, password, etc.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface MailAccount extends Account {

    /**
     * Gets the ID of the user belonging to this mail account.
     *
     * @return The ID of the user
     */
    public int getUserId();

    /**
     * Generates the mail server URL; e.g. <code>&quot;imap://imap.somewhere.com:4143&quot;</code>.
     *
     * @return The generated mail server URL
     */
    public String generateMailServerURL();

    /**
     * Gets the mail server name.
     * <p>
     * The mail server name can either be a machine name, such as "<code>java.sun.com</code>", or a textual representation of its IP
     * address.
     *
     * @return The mail server name
     */
    public String getMailServer();

    /**
     * Gets the mail server port.
     *
     * @return The mail server port
     */
    public int getMailPort();

    /**
     * Gets the mail server protocol.
     *
     * @return The mail server protocol
     */
    public String getMailProtocol();

    /**
     * Checks if a secure connection to mail server shall be established.
     *
     * @return <code>true</code> if a secure connection to mail server shall be established; otherwise <code>false</code>
     */
    public boolean isMailSecure();

    /**
     * Gets the transport authentication information
     *
     * @return The transport authentication information
     */
    @Override
    public TransportAuth getTransportAuth();

    /**
     * Checks if a secure connection to transport server shall be established.
     *
     * @return <code>true</code> if a secure connection to transport server shall be established; otherwise <code>false</code>
     */
    public boolean isTransportSecure();

    /**
     * Gets the optional transport login.
     * <p>
     * <b>NOTE</b>:&nbsp;{@link #getLogin()} is returned if no separate transport login is available.
     *
     * @return The optional transport login
     */
    @Override
    public String getTransportLogin();

    /**
     * Gets the optional transport password.
     * <p>
     * <b>NOTE</b>:&nbsp;{@link #getPassword()} is returned if no separate transport password is available.
     *
     * @return The optional transport password
     */
    @Override
    public String getTransportPassword();

    /**
     * Gets the personal part of primary email address; e.g.<br>
     * <code>Jane Doe &lt;jane.doe@somewhere.com&gt;</code>
     *
     * @return The personal
     */
    public String getPersonal();

    /**
     * Gets the reply-to address.
     *
     * @return The reply-to address
     */
    public String getReplyTo();

    /**
     * Gets the spam handler name for this mail account.
     *
     * @return The spam handler name
     */
    public String getSpamHandler();

    /**
     * Gets the name of the draft folder.
     * <p>
     * <b>Note</b>: This is only the name, not its full name.
     *
     * @return The name of the draft folder
     */
    public String getDrafts();

    /**
     * Gets the name of the sent folder.
     * <p>
     * <b>Note</b>: This is only the name, not its full name.
     *
     * @return The name of the sent folder
     */
    public String getSent();

    /**
     * Gets the name of the spam folder.
     * <p>
     * <b>Note</b>: This is only the name, not its full name.
     *
     * @return The name of the spam folder
     */
    public String getSpam();

    /**
     * Gets the name of the trash folder.
     * <p>
     * <b>Note</b>: This is only the name, not its full name.
     *
     * @return The name of the trash folder
     */
    public String getTrash();

    /**
     * Gets the name of the archive folder.
     * <p>
     * <b>Note</b>: This is only the name, not its full name.
     *
     * @return The name of the archive folder
     */
    public String getArchive();

    /**
     * Gets the name of the confirmed ham folder.
     * <p>
     * <b>Note</b>: This is only the name, not its full name.
     *
     * @return The name of the confirmed ham folder
     */
    public String getConfirmedHam();

    /**
     * Gets the name of the confirmed spam folder.
     * <p>
     * <b>Note</b>: This is only the name, not its full name.
     *
     * @return The name of the confirmed spam folder
     */
    public String getConfirmedSpam();

    /**
     * Checks if this mail account is enabled for Unified Mail.
     *
     * @return <code>true</code> if this mail account is enabled for Unified Mail; otherwise <code>false</code>
     */
    public boolean isUnifiedINBOXEnabled();

    /**
     * Gets the trash full name
     *
     * @return The trash full name
     */
    public String getTrashFullname();

    /**
     * Gets the archive full name
     *
     * @return The archive full name
     */
    public String getArchiveFullname();

    /**
     * Gets the sent full name
     *
     * @return The sent full name
     */
    public String getSentFullname();

    /**
     * Gets the drafts full name
     *
     * @return The drafts full name
     */
    public String getDraftsFullname();

    /**
     * Gets the spam full name
     *
     * @return The spam full name
     */
    public String getSpamFullname();

    /**
     * Gets the confirmed-spam full name
     *
     * @return The confirmed-spam full name
     */
    public String getConfirmedSpamFullname();

    /**
     * Gets the confirmed-ham full name
     *
     * @return The confirmed-ham full name
     */
    public String getConfirmedHamFullname();

    /**
     * Gets this account's properties.
     *
     * @return Account's properties
     */
    public Map<String, String> getProperties();

    /**
     * Adds specified name-value-pair to properties.
     *
     * @param name The property name
     * @param value The property value
     */
    public void addProperty(String name, String value);

    /**
     * Gets this account's transport properties.
     *
     * @return Account's transport properties
     */
    public Map<String, String> getTransportProperties();

    /**
     * Adds specified name-value-pair to transport properties.
     *
     * @param name The transport property name
     * @param value The transport property value
     */
    public void addTransportProperty(String name, String value);

    /**
     * Checks if STARTTLS should be used to connect to mail server
     * 
     * @return
     */
    public boolean isMailStartTls();

}
