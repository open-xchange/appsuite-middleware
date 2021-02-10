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

package com.openexchange.mail.compose.mailstorage.cache;

import java.io.IOException;
import java.util.Objects;
import com.openexchange.exception.OXException;
import com.openexchange.mail.compose.CompositionSpaceErrorCode;

/**
 * {@link Result} - Represents a result when attempting to cache a MIME message's content.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class Result {

    /** The error reason for a result representing a failed cache attempt */
    public static enum ErrorReason {
        /** Cache attempt failed because cache is disabled */
        DISABLED,
        /** Cache attempt failed due to an exception */
        EXCEPTION;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Creates a result for a successful cache attempt.
     *
     * @param cacheReference The cache reference
     * @return The result
     */
    public static Result successfulResultFor(CacheReference cacheReference) {
        if (cacheReference == null) {
            throw new IllegalArgumentException("CacheReference must not be null");
        }
        return new Result(cacheReference);
    }

    private static final Result RESULT_DISABLED = new Result(ErrorReason.DISABLED);

    /**
     * Gets the result for a failed cache attempt due to disabled cache.
     *
     * @return The result
     */
    public static Result disabledResult() {
        return RESULT_DISABLED;
    }

    /**
     * Create a result for a failed cache attempt including accompanying exception.
     *
     * @param exception The exception (as reason for failure)
     * @return The result
     */
    public static Result exceptionResultFor(Exception exception) {
        if (exception == null) {
            throw new IllegalArgumentException("Exception must not be null");
        }
        return new Result(ErrorReason.EXCEPTION, exception);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final CacheReference cacheReference;
    private final ErrorReason errorReason;
    private final Exception exception;

    /**
     * Initializes a new result for a successful cache attempt.
     *
     * @param cacheReference The cache reference
     */
    private Result(CacheReference cacheReference) {
        super();
        this.cacheReference = Objects.requireNonNull(cacheReference);
        this.errorReason = null;
        this.exception = null;
    }

    /**
     * Initializes a new result for a failed cache attempt providing the error reason w/o an accompanying exception.
     *
     * @param errorReason The error reason
     */
    private Result(ErrorReason errorReason) {
        this(errorReason, null);
    }

    /**
     * Initializes a new result for a failed cache attempt providing the error reason as well as an accompanying exception.
     *
     * @param errorReason The error reason
     * @param exception The accompanying exception
     */
    private Result(ErrorReason errorReason, Exception exception) {
        super();
        this.cacheReference = null;
        this.errorReason = Objects.requireNonNull(errorReason);
        this.exception = exception;
    }

    /**
     * Checks if this result represents a successful cache attempt.
     *
     * @return <code>true</code> for successful cache attempt; otherwise <code>false</code>
     */
    public boolean success() {
        return cacheReference != null;
    }

    /**
     * Gets the cache reference of this result.
     *
     * @return The cache reference or <code>null</code> in case this result does <b>not</b> represent a successful cache attempt
     */
    public CacheReference getFileCacheReference() {
        return cacheReference;
    }

    /**
     * Checks if this results represents a failed cache attempt.
     *
     * @return <code>true</code> for failed cache attempt; otherwise <code>false</code>
     */
    public boolean fail() {
        return errorReason != null;
    }

    /**
     * Gets the error reason of this result.
     *
     * @return The error reason or <code>null</code> in case this result does represent a successful cache attempt
     */
    public ErrorReason getErrorReason() {
        return errorReason;
    }

    /**
     * Checks if this results represents a failed cache attempt caused by an exception.
     *
     * @return <code>true</code> for failed cache attempt due to an exception; otherwise <code>false</code>
     */
    public boolean hasException() {
        return exception != null;
    }

    /**
     * Gets the exception that caused the cache attempt to fail.
     *
     * @return The exception or <code>null</code> in case this result does <b>not</b> represent an exception-caused failed cache attempt
     */
    public Exception getException() {
        return exception;
    }

    /**
     * Gets the causing exception as {@link OXException}.
     * <p>
     * If the cause already is of instance {@link OXException}, it is returned as is. Otherwise any other checked or runtime exception is
     * wrapped accordingly.
     *
     * @return The {@link OXException} or <code>null</code> if no exception occurred
     */
    public OXException getOXException() {
        if (exception == null) {
            return null;
        }

        if (exception instanceof OXException) {
            return (OXException) exception;
        }

        if (exception instanceof IOException) {
            return CompositionSpaceErrorCode.IO_ERROR.create(exception, exception.getMessage());
        }

        return CompositionSpaceErrorCode.ERROR.create(exception, exception.getMessage());
    }

}