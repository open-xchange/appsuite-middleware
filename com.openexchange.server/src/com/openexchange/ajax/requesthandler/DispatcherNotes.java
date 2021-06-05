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

package com.openexchange.ajax.requesthandler;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * {@link DispatcherNotes} - The action annotation provides the default format for an {@link AJAXActionService}.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DispatcherNotes {

    /**
     * Gets the default format.
     *
     * @return The default format
     */
    String defaultFormat() default "apiResponse";

    /**
     * Indicates whether this action allows falling back to the public session cookie for session retrieval. This is useful
     * if you don't want varying URLs between sessions. The trade-off is less stability for your requests in problematic infrastructures.
     * @return Whether to allow access using the fallback session or not
     */
    boolean allowPublicSession() default false;

    /**
     * Indicates whether this action allows authentication via public session identifier.
     * @return Whether to allow authentication via public session identifier or not
     */
    boolean publicSessionAuth() default false;

    /**
     * Indicates that this action may be called without a session
     * @return whether to allow access to this action without a session
     */
	boolean noSession() default false;

	/**
     * Indicates whether this action is allowed to miss the associated secret cookie, because it is meant as a callback.
     * @return Whether to allow access without secret
     */
	boolean noSecretCallback() default false;

	/**
     * Indicates whether this action prefers reading/parsing request body stream by itself.
     * @return Whether to prefer reading/parsing request body stream by itself
     */
    boolean preferStream() default false;

    /**
     * Signals whether the performed action is allowed for being enqueued in job queue in case its processing exceeds the threshold
     *
     * @return <code>true</code> if enqueue-able; otherwise <code>false</code>
     */
    boolean enqueueable() default false;
}
