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

package com.openexchange.file.storage.json.actions.files;

import java.util.Map;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageConstants;
import com.openexchange.file.storage.FileStorageUtility;
import com.openexchange.file.storage.composition.IDBasedFileAccess;


/**
 * {@link GetAction}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
@Action(method = RequestMethod.GET, name = "get", description = "Get an infoitem", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "id", description = "Object ID of the requested infoitem."),
    @Parameter(name = "version", optional=true, description = "If present the infoitem data describes the given version. Otherwise the current version is returned.")
}, responseDescription = "Response with timestamp: An object containing all data of the requested infoitem. The fields of the object are listed in Common object data and Detailed infoitem data. The field id is not included.")
public class GetAction extends AbstractFileAction {

    private static final String METADATA_KEY_ENCRYPTED = FileStorageConstants.METADATA_KEY_ENCRYPTED;

    @Override
    public AJAXRequestResult handle(InfostoreRequest request) throws OXException {
        request.require(Param.ID);
        IDBasedFileAccess fileAccess = request.getFileAccess();
        File fileMetadata = fileAccess.getFileMetadata(request.getId(), request.getVersion());

        if (FileStorageUtility.isEncryptedFile(fileMetadata)) {
            Map<String, Object> meta = fileMetadata.getMeta();
            fileMetadata = new MetaDataAddingFile(fileMetadata);
            if (null == meta) {
                meta = new java.util.LinkedHashMap<>(2);
                meta.put(METADATA_KEY_ENCRYPTED, Boolean.TRUE);
                fileMetadata.setMeta(meta);
            } else if (!meta.containsKey(METADATA_KEY_ENCRYPTED)) {
                meta = new java.util.LinkedHashMap<>(meta);
                meta.put(METADATA_KEY_ENCRYPTED, Boolean.TRUE);
                fileMetadata.setMeta(meta);
            }
        }

        return result(fileMetadata, request);
    }

}
