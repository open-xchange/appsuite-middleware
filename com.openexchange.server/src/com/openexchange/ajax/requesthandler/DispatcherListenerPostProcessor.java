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

import java.util.Collection;
import java.util.Collections;

/**
 * {@link DispatcherListenerPostProcessor} - The special post-processor triggering dispatcher listeners for final call-back.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class DispatcherListenerPostProcessor implements AJAXRequestResultPostProcessor {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DispatcherListenerPostProcessor.class);

    private final Collection<DispatcherListener> dispatcherListeners;

    /**
     * Initializes a new {@link DispatcherListenerPostProcessor}.
     */
    public DispatcherListenerPostProcessor(Collection<DispatcherListener> dispatcherListeners) {
        super();
        this.dispatcherListeners = null == dispatcherListeners ? Collections.<DispatcherListener> emptyList() : dispatcherListeners;
    }

    @Override
    public void doPostProcessing(AJAXRequestData requestData, AJAXRequestResult requestResult, Exception e) {
        for (DispatcherListener dispatcherListener : dispatcherListeners) {
            try {
                dispatcherListener.onResultReturned(requestData, requestResult, e);
            } catch (Exception x) {
                LOG.error("Failed to execute dispatcher listener {}", dispatcherListener.getClass().getSimpleName(), x);
            }
        }
    }

}
