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

package com.openexchange.mail.api;

/**
 * {@link IMailProperties} - The properties associated with mail access.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface IMailProperties {

    /**
     * Checks if default folders (e.g. "Sent Mail", "Drafts") are supposed to be created below personal namespace folder (INBOX) even though
     * mail server indicates to create them on the same level as personal namespace folder.
     * <p>
     * <b>Note</b> that personal namespace folder must allow subfolder creation.
     *
     * @return <code>true</code> if default folders are supposed to be created below personal namespace folder; otherwise <code>false</code>
     */
    boolean isAllowNestedDefaultFolderOnAltNamespace();

    /**
     * Indicates whether subscription shall be ignored or not.
     *
     * @return <code>true</code> if subscription shall be ignored; otherwise <code>false</code>
     */
    boolean isIgnoreSubscription();

    /**
     * Indicates whether subscription is supported or not.
     *
     * @return <code>true</code> if subscription is supported; otherwise <code>false</code>
     */
    boolean isSupportSubscription();

    /**
     * Gets the mail fetch limit.
     *
     * @return The mail fetch limit
     */
    int getMailFetchLimit();

    /**
     * Indicates if user flags are enabled.
     *
     * @return <code>true</code> if user flags are enabled; otherwise <code>false</code>
     */
    boolean isUserFlagsEnabled();

    /**
     * Checks whether inline images are supposed to be hidden when outputting the display version for a mail message.
     * <p>
     * An image is considered as inline if Content-Disposition header is simply set to <tt>"inline"</tt> and headers provide no file name information.
     *
     * @return <code>true</code> to hide; otherwise <code>false</code>
     */
    boolean hideInlineImages();

    /**
     * Waits for loading this properties.
     *
     * @throws InterruptedException If another thread interrupted the current thread before or while the current thread was waiting for loading the properties.
     */
    void waitForLoading() throws InterruptedException;

}
