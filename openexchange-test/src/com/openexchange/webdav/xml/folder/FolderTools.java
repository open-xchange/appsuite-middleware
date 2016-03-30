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

package com.openexchange.webdav.xml.folder;

import java.io.IOException;
import java.util.Date;
import org.jdom2.JDOMException;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.test.TestException;
import com.openexchange.webdav.xml.folder.actions.ListRequest;
import com.openexchange.webdav.xml.folder.actions.ListResponse;
import com.openexchange.webdav.xml.framework.WebDAVClient;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class FolderTools {

    private final WebDAVClient client;

    private FolderObject defaultAppointmentFolder;
    private FolderObject defaultContactFolder;

    /**
     * Default constructor.
     */
    public FolderTools(final WebDAVClient client) {
        super();
        this.client = client;
    }

    public FolderObject getDefaultAppointmentFolder() throws IOException, JDOMException, OXException, OXException {
        return getDefaultAppointmentFolder(null);
    }

    public FolderObject getDefaultAppointmentFolder(final String host) throws IOException, JDOMException, OXException, OXException {
        if (null == defaultAppointmentFolder) {
            final ListRequest request = new ListRequest(new Date(0));
            final ListResponse response = client.execute(host, request);
            final int userId = client.getGroupUserTools().getUserId(host);
            for (final FolderObject folder : response) {
                if (folder.isDefaultFolder() && folder.getModule() == FolderObject.CALENDAR && folder.getCreatedBy() == userId) {
                    defaultAppointmentFolder = folder;
                    break;
                }
            }
            if (null == defaultAppointmentFolder) {
                throw new TestException("Unable to find default appointment folder.");
            }
        }
        return defaultAppointmentFolder;
    }

    public FolderObject getDefaultContactFolder() throws OXException, IOException, JDOMException, OXException {
        return getDefaultContactFolder(null);
    }

    public FolderObject getDefaultContactFolder(String host) throws OXException, IOException, JDOMException, OXException {
        if (null == defaultContactFolder) {
            final ListRequest request = new ListRequest(new Date(0));
            final ListResponse response = client.execute(host, request);
            final int userId = client.getGroupUserTools().getUserId(host);
            for (final FolderObject folder : response) {
                if (folder.isDefaultFolder() && folder.getModule() == FolderObject.CONTACT && folder.getCreatedBy() == userId) {
                    defaultContactFolder = folder;
                    break;
                }
            }
            if (null == defaultContactFolder) {
                throw new TestException("Unable to find default contact folder.");
            }
        }
        return defaultContactFolder;
    }
}
