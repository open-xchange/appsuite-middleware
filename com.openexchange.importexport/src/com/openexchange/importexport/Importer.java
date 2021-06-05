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

package com.openexchange.importexport;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.importexport.importers.ImportResults;
import com.openexchange.tools.session.ServerSession;

/**
 * This interface defines an importer, meaning a class able to
 * import one or more data formats into the OX.
 *
 * @author Tobias Prinz, mailto:tobias.prinz@open-xchange.com
 *
 */
public interface Importer {

	/**
	 *
	 * @param sessObj: Session object enabling us to check write access.
	 * @param format: Format of the data that is meant to be imported
	 * @param folders: Those folders the data is meant to be imported int
	 * @param optionalParams: Params that might be needed by a specific implementor of this interface. Note: The format was chosen to be congruent with HTTP-GET
	 * @return true, if this importer can import this format for this module; false otherwise
	 * @see com.openexchange.groupware.Types
	 */
	boolean canImport(ServerSession sessObj, Format format, List<String> folders, Map<String, String[]> optionalParams) throws OXException;

	/**
	 *
	 * @param sessObj: session object enabling us to check access rights (write rights needed)
	 * @param format: Format of the data to be imported
	 * @param is: InputStream containing data to be imported
	 * @param folders: Identifiers for folders (plus their type as int) - usually only one, but iCal may need two and future extensions might need even more (remember: Folders can have only one type, so type is not a necessary argument)
	 * @param optionalParams: Params that might be needed by a specific implementor of this interface. Note: The format was chosen to be congruent with HTTP-GET
	 * @return
	 * @throws OXException
	 * @see com.openexchange.groupware.Types
	 */
	ImportResults importData(ServerSession sessObj, Format format, InputStream is, List<String> folders, Map<String, String[]> optionalParams) throws OXException;

}
