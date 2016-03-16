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

package com.openexchange.mail.api;

public interface IMailProperties {

    /**
     * Signals whether secure connections to external accounts are mandatory.
     *
     * @return <code>true</code> if secure connections are enforced; otherwise <code>false</code>
     */
    public boolean isEnforceSecureConnection();

    /**
     * Sets whether secure connections to external accounts are mandatory.
     *
     * @param enforceSecureConnection <code>true</code> to signal secure connections to external accounts are mandatory; otherwise <code>false</code>
     */
    public void setEnforceSecureConnection(boolean enforceSecureConnection);

    /**
     * Checks if default folders (e.g. "Sent Mail", "Drafts") are supposed to be created below personal namespace folder (INBOX) even though
     * mail server indicates to create them on the same level as personal namespace folder.
     * <p>
     * <b>Note</b> that personal namespace folder must allow subfolder creation.
     *
     * @return <code>true</code> if default folders are supposed to be created below personal namespace folder; otherwise <code>false</code>
     */
    public boolean isAllowNestedDefaultFolderOnAltNamespace();

    /**
     * Gets the max. allowed size (in bytes) for attachment for being displayed.
     *
     * @return The max. allowed size (in bytes) for attachment for being displayed
     */
    public int getAttachDisplaySize();

    /**
     * Gets the default separator character.
     *
     * @return The default separator character
     */
    public char getDefaultSeparator();

    /**
     * Indicates whether subscription shall be ignored or not.
     *
     * @return <code>true</code> if subscription shall be ignored; otherwise <code>false</code>
     */
    public boolean isIgnoreSubscription();

    /**
     * Indicates whether subscription is supported or not.
     *
     * @return <code>true</code> if subscription is supported; otherwise <code>false</code>
     */
    public boolean isSupportSubscription();

    /**
     * Gets the mail fetch limit.
     *
     * @return The mail fetch limit
     */
    public int getMailFetchLimit();

    /**
     * Indicates if user flags are enabled.
     *
     * @return <code>true</code> if user flags are enabled; otherwise <code>false</code>
     */
    public boolean isUserFlagsEnabled();

    /**
     * Indicates if watcher is enabled.
     *
     * @return <code>true</code> if watcher is enabled; otherwise <code>false</code>
     */
    public boolean isWatcherEnabled();

    /**
     * Gets the watcher frequency.
     *
     * @return The watcher frequency
     */
    public int getWatcherFrequency();

    /**
     * Indicates if watcher is allowed to close exceeded connections.
     *
     * @return <code>true</code> if watcher is allowed to close exceeded connections; otherwise <code>false</code>
     */
    public boolean isWatcherShallClose();

    /**
     * Gets the watcher time.
     *
     * @return The watcher time
     */
    public int getWatcherTime();

    /**
     * Gets the mail access cache shrinker-interval seconds.
     *
     * @return The mail access cache shrinker-interval seconds
     */
    public int getMailAccessCacheShrinkerSeconds();

    /**
     * Gets the mail access cache idle seconds.
     *
     * @return The mail access cache idle seconds.
     */
    public int getMailAccessCacheIdleSeconds();

    /**
     * Waits for loading this properties.
     *
     * @throws InterruptedException If another thread interrupted the current thread before or while the current thread was waiting for
     *             loading the properties.
     */
    public void waitForLoading() throws InterruptedException;
}
