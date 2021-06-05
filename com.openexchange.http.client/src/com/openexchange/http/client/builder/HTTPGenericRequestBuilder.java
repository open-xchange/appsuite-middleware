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

package com.openexchange.http.client.builder;

import java.util.Map;
import com.openexchange.exception.OXException;

public interface HTTPGenericRequestBuilder<T extends HTTPGenericRequestBuilder<T>> {
	public T url(String url);
	public T verbatimURL(String url);
	
	public T parameter(String parameter, String value);
	public T parameters(Map<String, String> parameters);
		
	public T header(String header, String value);
	public T headers(Map<String, String> cookies);
	
	public HTTPRequest build() throws OXException;
}
