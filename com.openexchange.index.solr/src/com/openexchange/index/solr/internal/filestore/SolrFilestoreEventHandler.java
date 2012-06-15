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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.index.solr.internal.filestore;

import java.io.InputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageEventHelper;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.groupware.Types;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexFacadeService;
import com.openexchange.index.StandardIndexDocument;
import com.openexchange.index.IndexDocument.Type;
import com.openexchange.index.solr.internal.Services;
import com.openexchange.session.Session;


/**
 * {@link SolrFilestoreEventHandler}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SolrFilestoreEventHandler implements EventHandler {
    
    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(SolrFilestoreEventHandler.class));

    @Override
    public void handleEvent(Event event) {
        if (FileStorageEventHelper.isInfostoreEvent(event)) {
            try {
                Session session = FileStorageEventHelper.extractSession(event);
                if (FileStorageEventHelper.isCreateEvent(event)) {
                    IDBasedFileAccessFactory accessFactory = Services.getService(IDBasedFileAccessFactory.class);
                    IndexFacadeService indexService = Services.getService(IndexFacadeService.class);                    
                    IDBasedFileAccess access = accessFactory.createAccess(session);
                    String id = FileStorageEventHelper.extractObjectId(event);
                    File fileMetadata = access.getFileMetadata(id, FileStorageFileAccess.CURRENT_VERSION);
                    InputStream is = access.getDocument(id, FileStorageFileAccess.CURRENT_VERSION);
                    IndexAccess<File> indexAccess = indexService.acquireIndexAccess(Types.INFOSTORE, session);
                    StandardIndexDocument<File> document = new StandardIndexDocument<File>(fileMetadata, Type.INFOSTORE_DOCUMENT);
                    document.addProperty("attachment", is);
                    //TODO: evt. FileID öffentlich machen?
//                    document.addProperty("account", )
                    indexAccess.addAttachments(document, true);
                    
                    LOG.info(FileStorageEventHelper.createDebugMessage("CreateEvent", event));
                } else if (FileStorageEventHelper.isUpdateEvent(event)) {
                    LOG.info(FileStorageEventHelper.createDebugMessage("UpdateEvent", event));
                } else if (FileStorageEventHelper.isDeleteEvent(event)) {
                    LOG.info(FileStorageEventHelper.createDebugMessage("DeleteEvent", event));
                }
            } catch (OXException e) {
                // TODO: handle exception
            }
        }        
    }

}
