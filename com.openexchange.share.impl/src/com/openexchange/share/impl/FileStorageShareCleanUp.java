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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.share.impl;

import java.util.Collections;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageEventHelper;
import com.openexchange.groupware.modules.Module;
import com.openexchange.share.ShareService;
import com.openexchange.share.ShareTarget;

/**
 * {@link FileStorageShareCleanUp}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class FileStorageShareCleanUp implements EventHandler {

    private static final Logger LOG = LoggerFactory.getLogger(FileStorageShareCleanUp.class);

    private final ShareService shareService;

    /**
     * Initializes a new {@link FileStorageShareCleanUp}.
     */
    public FileStorageShareCleanUp(ShareService shareService) {
        super();
        this.shareService = shareService;
    }

    @Override
    public void handleEvent(Event event) {
        if (FileStorageEventHelper.isInfostoreEvent(event) && FileStorageEventHelper.isDeleteEvent(event)) {
            try {
                ShareTarget shareTarget = new ShareTarget(
                    Module.INFOSTORE.getFolderConstant(),
                    FileStorageEventHelper.extractFolderId(event),
                    FileStorageEventHelper.extractObjectId(event));
                shareService.deleteShareTarget(FileStorageEventHelper.extractSession(event), shareTarget, Collections.<Integer>emptyList());
            } catch (OXException e) {
                StringBuilder sb = new StringBuilder();
                for (String name : event.getPropertyNames()) {
                    sb.append(name).append(":").append(event.getProperty(name)).append(", ");
                }
                sb.setLength(sb.length() - 2);
                LOG.error("Could not delete dependent shares for file with event properties '{}'", sb, e);
            }
        }
    }

}
