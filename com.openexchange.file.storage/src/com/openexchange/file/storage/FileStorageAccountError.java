
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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.file.storage;

import com.openexchange.annotation.NonNull;

import com.openexchange.annotation.Nullable;
import com.openexchange.exception.OXException;
import java.util.Date;
import java.util.Objects;

/**
 * {@link FileStorageAccountError}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class FileStorageAccountError {

    private String errorCode;
    private Date timeStamp;

    /**
     * Initializes a new {@link FileStorageAccountError}.
     */
    public FileStorageAccountError() {
        this(null, null);
    }

    /**
     * Initializes a new {@link FileStorageAccountError} with the current time as time stamp
     *
     * @param errorCode The error code
     */
    public FileStorageAccountError(@NonNull String errorCode) {
        this(Objects.requireNonNull(errorCode, "errorCode must not be null"), null);
    }

    /**
     * Initializes a new {@link FileStorageAccountError}.
     *
     * @param errorCode The error code, might be <code>null</code>
     * @param timeStamp The time stamp of when the error occurred, might be <code>null</code>
     */
    public FileStorageAccountError(@Nullable String errorCode, @Nullable Date timeStamp) {
        this.errorCode = errorCode;
        this.timeStamp = timeStamp != null ? timeStamp : new Date();
    }

    /**
     * Gets the error code
     *
     * @return The errorCode
     */
    public @Nullable String getErrorCode() {
        return errorCode;
    }

    /**
     * Sets the error code
     *
     * @param errorCode The errorCode to set, might be <code>null</code>
     * @return this
     */
    public FileStorageAccountError setErrorCode(@Nullable String errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    /**
     * Sets the error code from the given {@link OXException}
     *
     * @param e the {@link OXException} to set the error code from
     * @return this
     */
    public FileStorageAccountError setErrorCode(@NonNull OXException e) {
        this.errorCode = e.getErrorCode();
        return this;
    }

    /**
     * Gets the time stamp
     *
     * @return The time stamp of when the error occurred, or <code>null</code>
     */
    public @Nullable Date getTimeStamp() {
        return timeStamp;
    }

    /**
     * Sets the time stamp
     *
     * @param timeStamp The timeStamp to set, might be <code>null</code>
     * @return this
     */
    public FileStorageAccountError setTimeStamp(@Nullable Date timeStamp) {
        this.timeStamp = timeStamp;
        return this;
    }
}
