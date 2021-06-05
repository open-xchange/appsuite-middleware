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

package com.openexchange.rest.client.httpclient;

import org.apache.http.client.HttpClient;

/**
 * {@link ManagedHttpClient} - Gets a {@link HttpClient} that is <i>managed</i> by the {@link HttpClientService}, which will take
 * care of closing the client
 * <p>
 * The client obtained by this call <b>SHOULD NOT</b> be closed.
 * <p>
 * The HTTP client is wrapped into this class, so that this class <b>CAN</b> be used as
 * a class member. It is ensured, that a caller will always receive a usable HTTP client
 * when calling this method, expect the managing service is shutting down.
 * <p>
 * All methods will throw an {@link IllegalStateException} if the managing service has been shutdown and the client is unavailable
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
public interface ManagedHttpClient extends HttpClient {
}
