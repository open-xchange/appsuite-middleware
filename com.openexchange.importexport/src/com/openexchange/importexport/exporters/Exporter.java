/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.importexport.exporters;

import java.nio.charset.Charset;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.importexport.formats.Format;
import com.openexchange.importexport.helpers.SizedInputStream;
import com.openexchange.tools.session.ServerSession;

/**
 * Defines a class able to export a certain type of OX folder as a certain format
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 * @see com.openexchange.groupware.Types
 */
public interface Exporter {

    /**
     * The default character set used to generate output.
     */
    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

	/**
	 *
	 * @param sessObj: The session object to be able to check permissions.
	 * @param format: Format the exported data is supposed to be in
	 * @param folder: Folder that should be exported. Note: A folder can only contain data of one type
	 * @param optionalParams: Params that might be needed by a specific implementor of this interface. Note: The format was chosen to be congruent with HTTP-GET
	 * @return true, if the given folders can be exported in the given format; false otherwise
	 */
	boolean canExport(ServerSession sessObj, Format format, String folder, Map<String, Object> optionalParams) throws OXException;

	/**
	 *
	 * @param sessObj: The session object to be able to check permissions.
	 * @param format: Format the returned InputStream should be in.
	 * @param folder: Folder that should be exported. Note: A folder can only contain data of one type.
	 * @param fieldsToBeExported: A list of fields of that folder that should be exported. Convention: If the list is empty, all fields are exported.
	 * @param optionalParams: Params that might be needed by a specific implementor of this interface. Note: The format was chosen to be congruent with HTTP-GET
	 * @return InputStream in requested format.
	 * @throws OXException
	 */
	SizedInputStream exportData(ServerSession sessObj, Format format, String folder, int[] fieldsToBeExported, Map<String, Object> optionalParams) throws OXException;

	/**
	 *
	 * @param sessObj: The session object to be able to check permissions.
	 * @param format: Format the returned InputStream should be in.
	 * @param folder: Folder that should be exported. Note: A folder can only contain data of one type.
	 * @param objectId: Id of an entry in that folder that is to be exported.
	 * @param fieldsToBeExported: A list of fields of that folder that should be exported. Convention: If the list is empty, all fields are exported.
	 * @param optionalParams: Params that might be needed by a specific implementor of this interface. Note: The format was chosen to be congruent with HTTP-GET
	 * @return InputStream in requested format.
	 * @throws OXException
	 */
	SizedInputStream exportData(ServerSession sessObj, Format format, String folder, int objectId, int[] fieldsToBeExported, Map<String, Object> optionalParams) throws OXException;

}
