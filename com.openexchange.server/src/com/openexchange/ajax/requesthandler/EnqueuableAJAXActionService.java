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

import com.openexchange.ajax.requesthandler.jobqueue.JobKey;
import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link EnqueuableAJAXActionService} - Marks an {@link AJAXActionService} as enqueue-able to AJAX job queue.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface EnqueuableAJAXActionService extends AJAXActionService {

    /**
     * Checks if this action is enqueue-able with regard to specified request data and session.
     *
     * @param request The request data
     * @param session The session
     * @return <code>true</code> if enqueue-able; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    Result isEnqueueable(AJAXRequestData request, ServerSession session) throws OXException;

    /**
     * Allows to perform certain actions as preparation prior to submitting as job into job queue.
     * <p>
     * E.g. if the action expects binary uploads (files) of possibly big size, which might not be consumed until scheduled as background
     * task. Then such uploads should be completed before enqueued. Otherwise server responds with a job identifier while client might still
     * upload data, which effectively aborts the data upload.
     *
     * @param request The request data
     * @param session The session
     * @throws OXException If preparations fails
     */
    default void prepareForEnqueue(AJAXRequestData request, ServerSession session) throws OXException {
        // Nothing;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Gets the result for specified enqueue-able flag
     *
     * @param enqueueable The enqueue-able flag
     * @return The result
     */
    public static Result resultFor(boolean enqueueable) {
        return enqueueable ? Result.TRUE : Result.FALSE;
    }

    /**
     * Gets the result for specified enqueue-able flag
     *
     * @param enqueueable The enqueue-able flag
     * @param optionalKey The key that identifies a certain job; or <code>null</code>
     * @param enqueuableAction The enqueue-able action; or <code>null</code>
     * @return The result
     */
    public static Result resultFor(boolean enqueueable, JobKey optionalKey, EnqueuableAJAXActionService enqueuableAction) {
        if (null == optionalKey && enqueuableAction == null) {
            return resultFor(enqueueable);
        }

        return new Result(enqueueable, optionalKey, enqueuableAction);
    }

    /**
     * The result for checking if an action is enqueue-able.
     */
    public static final class Result {

        static final Result TRUE = new Result(true);

        static final Result FALSE = new Result(false);

        private final boolean enqueueable;
        private final JobKey optionalKey;
        private final EnqueuableAJAXActionService enqueuableAction;

        /**
         * Initializes a new {@link Result}.
         *
         * @param enqueueable The enqueue-able flag
         */
        private Result(boolean enqueueable) {
            this(enqueueable, null, null);
        }

        /**
         * Initializes a new {@link Result}.
         *
         * @param enqueueable The enqueue-able flag
         * @param optionalKey The key that identifies a certain job; or <code>null</code>
         * @param enqueuableAction The enqueue-able action; or <code>null</code>
         */
        Result(boolean enqueueable, JobKey optionalKey, EnqueuableAJAXActionService enqueuableAction) {
            super();
            this.enqueueable = enqueueable;
            this.optionalKey = optionalKey;
            this.enqueuableAction = enqueuableAction;
        }

        /**
         * Gets the enqueue-able flag
         *
         * @return The enqueue-able flag
         */
        public boolean isEnqueueable() {
            return enqueueable;
        }

        /**
         * Gets the optional key that identifies a certain job.
         *
         * @return The key or <code>null</code>
         */
        public JobKey getOptionalKey() {
            return optionalKey;
        }

        /**
         * Gets the enqueue-able action.
         *
         * @return The enqueue-able action
         */
        public EnqueuableAJAXActionService getEnqueuableAction() {
            return enqueuableAction;
        }
    }

}
