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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.documentation.annotations.Module;
import com.openexchange.exception.OXException;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link FileActionFactory}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
@Module(name = "infostore", description = "The infostore module combines the knowledge database, bookmarks and documents.")
public class FileActionFactory implements AJAXActionServiceFactory {

    /**
     * The singleton instance of {@link FileActionFactory}
     */
    public static FileActionFactory INSTANCE = new FileActionFactory();

    private final Map<String, AJAXActionService> actions;

    /**
     * Initializes a new {@link FileActionFactory}.
     */
    protected FileActionFactory() {
        super();
        final Map<String, AJAXActionService> actions = new ConcurrentHashMap<String, AJAXActionService>(24, 0.9f, 1);
        actions.put("new", new NewAction());
        actions.put("update", new UpdateAction());
        actions.put("delete", new DeleteAction());
        actions.put("detach", new DetachAction());
        actions.put("revert", new RevertAction());
        actions.put("lock", new LockAction());
        actions.put("unlock", new UnlockAction());
        actions.put("copy", new CopyAction());
        actions.put("move", new MoveAction());

        actions.put("all", new AllAction());
        actions.put("updates", new UpdatesAction());
        actions.put("list", new ListAction());
        actions.put("versions", new VersionsAction());
        actions.put("get", new GetAction());
        actions.put("search", new SearchAction());
        actions.put("shares", new SharesAction());
        actions.put("notify", new NotifyAction());

        actions.put("saveAs", new SaveAsAction());

        actions.put("document", new DocumentAction());
        actions.put("zipdocuments", new ZipDocumentsAction());
        actions.put("zipfolder", new ZipFolderAction());

        actions.put("documentdelta", new DocumentDeltaAction());
        actions.put("documentsig", new DocumentSigAction());
        actions.put("documentpatch", new DocumentPatchAction());

        actions.put("checkname", new CheckNameAction());
        this.actions = Collections.unmodifiableMap(actions);
    }

    @Override
    public Collection<? extends AJAXActionService> getSupportedServices() {
        return java.util.Collections.unmodifiableCollection(actions.values());
    }

    @Override
    public AJAXActionService createActionService(final String action) throws OXException {
        final AJAXActionService handler = actions.get(action);
        if (handler == null) {
            throw AjaxExceptionCodes.UNKNOWN_ACTION.create(action);
        }
        return handler;
    }

}
