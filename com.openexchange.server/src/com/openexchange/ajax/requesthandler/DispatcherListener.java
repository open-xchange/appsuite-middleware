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

import com.openexchange.exception.OXException;

/**
 * {@link DispatcherListener} - A listener which receives various call-backs during a {@link Dispatcher} processing.
 * <p>
 * The call-backs happen in the following order:
 * <ol>
 * <li>{@link #onRequestInitialized(AJAXRequestData)}<br>Request has been completely parsed. but not yet performed<br>&nbsp;</li>
 * <li>{@link #onRequestPerformed(AJAXRequestData, AJAXRequestResult, Exception)}<br>Either result has been successfully created or an exception is given<br>&nbsp;</li>
 * <li>{@link #onResultReturned(AJAXRequestData, AJAXRequestResult, Exception)}<br>The result is ensured to be successfully created and is about being returned to client. If output to client failed the exception argument in non-<code>null</code></li>
 * </ol>
 * <p>
 * Example:
 * <pre>
 *  DispatcherListener listener = new ActionBoundDispatcherListener() {
 *
 *      public void onResultReturned(AJAXRequestData requestData, AJAXRequestResult requestResult, Exception e) {
 *          if (null == e) {
 *              System.out.println("User " + requestData.getSession().getUserId() + " successfully loaded " + requestData.getParameter("id"));
 *          } else {
 *              System.out.println("User " + requestData.getSession().getUserId() + " failed to download " + requestData.getParameter("id") + " with HTTP error code " + DispatcherListeners.getHttpError(e));
 *          }
 *      }
 *
 *      public void onRequestPerformed(AJAXRequestData requestData, AJAXRequestResult requestResult, Exception e) {
 *          // Don't care
 *      }
 *
 *      public void onRequestInitialized(AJAXRequestData requestData) {
 *          System.out.println("User " + requestData.getSession().getUserId() + " wants to download " + requestData.getParameter("id"));
 *      }
 *
 *      public String getModule() {
 *          return "files";
 *      }
 *
 *      public Set<String> getActions() {
 *          return Collections.singleton("document");
 *      }
 *  };
 *
 *  // Register the dispatcher listener
 *  registerService(DispatcherListener.class, listener);
 * </pre>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 * @see DispatcherListeners
 */
public interface DispatcherListener {

    /**
     * Checks whether this dispatcher listener wants to receive call-backs for given request data.
     *
     * @param requestData
     * @return <code>true</code> if applicable; otherwise <code>false</code>
     */
    boolean applicable(AJAXRequestData requestData);

    /**
     * Called when a request is about being performed.
     *
     * @param requestData The associated request data
     * @throws OXException If this listener signals to abort further processing
     */
    void onRequestInitialized(AJAXRequestData requestData) throws OXException;

    /**
     * Called when a result was supposed to be created, but not yet returned to requesting client (by responsible {@link ResponseRenderer renderer}).
     *
     * @param requestData The associated request data
     * @param requestResult The request result that has been created or <code>null</code> if creation failed (in that case an exception is passed)
     * @param e The exception that caused termination, or <code>null</code> if execution completed normally (and a viable <code>requestResult</code> is given)
     * @throws OXException If this listener signals to abort further processing
     */
    void onRequestPerformed(AJAXRequestData requestData, AJAXRequestResult requestResult, Exception e) throws OXException;

    /**
     * Called when a result has been successfully created and an attempt was made returning it to requesting client (by responsible {@link ResponseRenderer renderer}).
     *
     * @param requestData The associated request data
     * @param requestResult The request result that has been returned
     * @param e The exception (or <code>HttpErrorCodeException</code>) that caused termination, or <code>null</code> if execution completed normally
     */
    void onResultReturned(AJAXRequestData requestData, AJAXRequestResult requestResult, Exception e);
}
