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
 * {@link MailAccount} - Provides all necessary information for a user's mail account like server, port, login, password, etc.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface MailAccount extends TransportAccount {

    /**
     * Gets the ID of the user belonging to this mail account.
     *
     * @return The ID of the user
     */
    int getUserId();

    /**
     * Generates the mail server URL; e.g. <code>&quot;imap://imap.somewhere.com:4143&quot;</code>.
     *
     * @return The generated mail server URL
     */
    String generateMailServerURL();

    /**
     * Gets the mail server name.
     * <p>
     * The mail server name can either be a machine name, such as "<code>java.sun.com</code>", or a textual representation of its IP
     * address.
     *
     * @return The mail server name
     */
    String getMailServer();

    /**
     * Gets the mail server port.
     *
     * @return The mail server port
     */
    int getMailPort();

    /**
     * Gets the mail server protocol.
     *
     * @return The mail server protocol
     */
    String getMailProtocol();

    /**
     * Checks if a secure connection to mail server shall be established.
     *
     * @return <code>true</code> if a secure connection to mail server shall be established; otherwise <code>false</code>
     */
    boolean isMailSecure();

    /**
     * Checks if mail server expects to authenticate via OAuth or not.
     *
     * @return <code>true</code> for OAuth authentication, otherwise <code>false</code>.
     */
    boolean isMailOAuthAble();

    /**
     * Gets the identifier of the associated OAuth account (if any) to authenticate against mail server.
     *
     * @return The OAuth account identifier or <code>-1</code> if there is no associated OAuth account
     */
    int getMailOAuthId();

    /**
     * Checks whether mail access is disabled
     *
     * @return <code>true</code> if disabled; otherwise <code>false</code>
     */
    boolean isMailDisabled();

    /**
     * Gets the transport authentication information
     *
     * @return The transport authentication information
     */
    @Override
    TransportAuth getTransportAuth();

    /**
     * Gets the optional transport login.
     * <p>
     * <b>NOTE</b>:&nbsp;{@link #getLogin()} is returned if no separate transport login is available.
     *
     * @return The optional transport login
     */
    @Override
    String getTransportLogin();

    /**
     * Gets the optional transport password.
     * <p>
     * <b>NOTE</b>:&nbsp;{@link #getPassword()} is returned if no separate transport password is available.
     *
     * @return The optional transport password
     */
    @Override
    String getTransportPassword();

    /**
     * Checks if transport server expects to authenticate via OAuth or not.
     *
     * @return <code>true</code> for OAuth authentication, otherwise <code>false</code>.
     */
    @Override
    boolean isTransportOAuthAble();

    /**
     * Gets the identifier of the associated OAuth account (if any) to authenticate against transport server.
     *
     * @return The OAuth account identifier or <code>-1</code> if there is no associated OAuth account
     */
    @Override
    int getTransportOAuthId();

    /**
     * Gets the spam handler name for this mail account.
     *
     * @return The spam handler name
     */
    String getSpamHandler();

    /**
     * Gets the name of the draft folder.
     * <p>
     * <b>Note</b>: This is only the name, not its full name.
     *
     * @return The name of the draft folder
     */
    String getDrafts();

    /**
     * Gets the name of the sent folder.
     * <p>
     * <b>Note</b>: This is only the name, not its full name.
     *
     * @return The name of the sent folder
     */
    String getSent();

    /**
     * Gets the name of the spam folder.
     * <p>
     * <b>Note</b>: This is only the name, not its full name.
     *
     * @return The name of the spam folder
     */
    String getSpam();

    /**
     * Gets the name of the trash folder.
     * <p>
     * <b>Note</b>: This is only the name, not its full name.
     *
     * @return The name of the trash folder
     */
    String getTrash();

    /**
     * Gets the name of the archive folder.
     * <p>
     * <b>Note</b>: This is only the name, not its full name.
     *
     * @return The name of the archive folder
     */
    String getArchive();

    /**
     * Gets the name of the confirmed ham folder.
     * <p>
     * <b>Note</b>: This is only the name, not its full name.
     *
     * @return The name of the confirmed ham folder
     */
    String getConfirmedHam();

    /**
     * Gets the name of the confirmed spam folder.
     * <p>
     * <b>Note</b>: This is only the name, not its full name.
     *
     * @return The name of the confirmed spam folder
     */
    String getConfirmedSpam();

    /**
     * Checks if this mail account is enabled for Unified Mail.
     *
     * @return <code>true</code> if this mail account is enabled for Unified Mail; otherwise <code>false</code>
     */
    boolean isUnifiedINBOXEnabled();

    /**
     * Gets the trash full name
     *
     * @return The trash full name
     */
    String getTrashFullname();

    /**
     * Gets the archive full name
     *
     * @return The archive full name
     */
    String getArchiveFullname();

    /**
     * Gets the sent full name
     *
     * @return The sent full name
     */
    String getSentFullname();

    /**
     * Gets the drafts full name
     *
     * @return The drafts full name
     */
    String getDraftsFullname();

    /**
     * Gets the spam full name
     *
     * @return The spam full name
     */
    String getSpamFullname();

    /**
     * Gets the confirmed-spam full name
     *
     * @return The confirmed-spam full name
     */
    String getConfirmedSpamFullname();

    /**
     * Gets the confirmed-ham full name
     *
     * @return The confirmed-ham full name
     */
    String getConfirmedHamFullname();

    /**
     * Gets this account's properties.
     *
     * @return Account's properties
     */
    Map<String, String> getProperties();

    /**
     * Adds specified name-value-pair to properties.
     *
     * @param name The property name
     * @param value The property value
     */
    void addProperty(String name, String value);

    /**
     * Gets this account's transport properties.
     *
     * @return Account's transport properties
     */
    Map<String, String> getTransportProperties();

    /**
     * Adds specified name-value-pair to transport properties.
     *
     * @param name The transport property name
     * @param value The transport property value
     */
    void addTransportProperty(String name, String value);

    /**
     * Checks if STARTTLS should be used to connect to mail server
     *
     * @return <code>true</code> if STARTTLS should be used; otherwise <code>false</code>
     */
    boolean isMailStartTls();

    /**
     * Gets the identifier of the account's root folder.
     *
     * @return The root folder identifier
     */
    String getRootFolder();

}
