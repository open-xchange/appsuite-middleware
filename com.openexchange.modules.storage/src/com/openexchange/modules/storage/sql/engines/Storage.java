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

package com.openexchange.modules.storage.sql.engines;

import java.sql.SQLException;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.modules.model.Attribute;
import com.openexchange.modules.model.Model;


/**
 * {@link Storage}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public interface Storage<T extends Model<T>> {

    public void create(T thing) throws SQLException, OXException;

    public void update(T thing, List<? extends Attribute<T>> updatedAttributes) throws SQLException, OXException;

    public T load(Object id) throws SQLException, OXException;

    public List<T> load() throws SQLException, OXException;

    public void delete(Object id) throws SQLException, OXException;

}
