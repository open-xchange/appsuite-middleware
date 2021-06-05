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

package com.openexchange.groupware.infostore.webdav;

import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.tx.TransactionAware;
import com.openexchange.webdav.protocol.WebdavProperty;

public interface PropertyStore extends TransactionAware{
	public void saveProperties(int entity, List<WebdavProperty> properties, Context ctx) throws OXException;
	public Map<Integer, List<WebdavProperty>> loadProperties(List<Integer> entities, List<WebdavProperty> properties, Context ctx) throws OXException;
	public List<WebdavProperty> loadProperties(int entity, List<WebdavProperty> properties, Context ctx) throws OXException;
	public void removeAll(int entity, Context ctx) throws OXException;
	public List<WebdavProperty> loadAllProperties(int entity, Context ctx) throws OXException;
	public Map<Integer, List<WebdavProperty>> loadAllProperties(List<Integer> ids, Context ctx) throws OXException;
	public void removeProperties(int entity, List<WebdavProperty> properties, Context ctx) throws OXException;
	public void removeAll(List<Integer> clean, Context ctx) throws OXException;
}
