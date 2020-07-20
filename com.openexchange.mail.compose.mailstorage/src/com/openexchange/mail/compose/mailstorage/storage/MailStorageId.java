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

package com.openexchange.mail.compose.mailstorage.storage;

import java.util.Optional;
import java.util.UUID;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.compose.mailstorage.cache.CacheReference;

/**
 * {@link MailStorageId} - Basically a pair of composition space identifier and draft mail path with a session reference.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public interface MailStorageId {

    /**
     * Gets the composition space identifier
     *
     * @return The composition space identifier
     */
    UUID getCompositionSpaceId();

    /**
     * Gets the draft path
     *
     * @return The draft path
     */
    MailPath getDraftPath();

    /**
     * Gets a reference to the currently locally cached draft message
     *
     * @return The reference or empty
     */
    Optional<CacheReference> getFileCacheReference();

    /**
     * Gets the folder identifier.
     * <p>
     * Convenience method that performs:<br>
     * <pre>
     * <code>getDraftPath().getFolder()</code>
     * </pre>
     *
     * @return The folder identifier
     */
    default String getFolderId() {
        return getDraftPath().getFolder();
    }

    /**
     * Gets the mail identifier.
     * <p>
     * Convenience method that performs:<br>
     * <pre>
     * <code>getDraftPath().getMailID()</code>
     * </pre>
     *
     * @return The mail identifier
     */
    default String getMailId() {
        return getDraftPath().getMailID();
    }

    /**
     * Gets the identifier for the mail account.
     * <p>
     * Convenience method that performs:<br>
     * <pre>
     * <code>getDraftPath().getAccountId()</code>.
     * </pre>
     *
     * @return The mail account identifier
     */
    default int getAccountId() {
        return getDraftPath().getAccountId();
    }

    /**
     * Checks if this instance holds a file cache reference.
     * <p>
     * Convenience method that performs:<br>
     * <pre>
     * <code>getFileCacheReference().isPresent()</code>
     * </pre>
     *
     * @return <code>true</code> if a file cache reference is present; otherwise <code>false</code>
     */
    default boolean hasFileCacheReference() {
        return getFileCacheReference().isPresent();
    }

    /**
     * Checks if this instance holds a file cache reference AND that {@link CacheReference#isValid() the reference is valid}.
     * <p>
     * Convenience method that basically performs:<br>
     * <pre>
     * <code>getFileCacheReference().isPresent() && getFileCacheReference().get().isValid()</code>
     * </pre>
     *
     * @return <code>true</code> if a valid file cache reference is present; otherwise <code>false</code>
     */
    default boolean hasValidFileCacheReference() {
        return getFileCacheReference().map(CacheReference::isValid).orElse(Boolean.FALSE).booleanValue();
    }

}
