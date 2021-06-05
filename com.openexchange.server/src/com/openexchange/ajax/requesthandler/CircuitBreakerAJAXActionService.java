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

import java.util.concurrent.Callable;
import org.fishwife.jrugged.CircuitBreaker;
import org.fishwife.jrugged.CircuitBreakerException;
import com.openexchange.exception.OXException;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.ratelimit.RateLimitedException;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link CircuitBreakerAJAXActionService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class CircuitBreakerAJAXActionService implements AJAXActionService {

    private final AJAXActionService actionService;
    private final CircuitBreaker circuitBreaker;

    /**
     * Initializes a new {@link CircuitBreakerAJAXActionService}.
     */
    public CircuitBreakerAJAXActionService(CircuitBreaker circuitBreaker, AJAXActionService actionService) {
        super();
        this.circuitBreaker = circuitBreaker;
        this.actionService = actionService;

    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        try {
            return circuitBreaker.invoke(new AJAXRequestResultCallable(requestData, session, actionService));
        } catch (OXException e) {
            throw e;
        } catch (CircuitBreakerException e) {
            throw new RateLimitedException("429 Too Many Requests", 0, e);
        } catch (Exception e) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }

    }

    // -----------------------------------------------------------------------------------------------------------------

    private static final class AJAXRequestResultCallable implements Callable<AJAXRequestResult> {

        private final AJAXRequestData requestData;
        private final ServerSession session;
        private final AJAXActionService actionServize;

        /**
         * Initializes a new {@link CircuitBreakerAJAXActionService.AJAXRequestResultCallable}.
         */
        AJAXRequestResultCallable(AJAXRequestData requestData, ServerSession session, AJAXActionService actionService) {
            super();
            this.requestData = requestData;
            this.session = session;
            actionServize = actionService;
        }

        @Override
        public AJAXRequestResult call() throws Exception {
            return actionServize.perform(requestData, session);
        }

    }

}
