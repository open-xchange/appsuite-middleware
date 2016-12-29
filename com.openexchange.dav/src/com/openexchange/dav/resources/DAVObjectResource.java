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

package com.openexchange.dav.resources;

import java.io.InputStream;
import com.openexchange.dav.DAVFactory;
import com.openexchange.exception.OXException;
import com.openexchange.webdav.protocol.WebdavPath;

/**
 * {@link DAVObjectResource}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class DAVObjectResource extends DAVResource {

    protected static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DAVObjectResource.class);

    /**
     * Initializes a new {@link DAVObjectResource}.
     *
     * @param parent The parent folder collection
     * @param url The resource url
     */
    protected DAVObjectResource(DAVFactory factory, WebdavPath url) {
        super(factory, url);
    }

    /**
     * Adds an attachment to the underlying groupware object.
     *
     * @param inputStream The binary attachment data
     * @param contentType The attachment's content type
     * @param fileName The target filename
     * @param size The attachment size
     * @param recurrenceIDs The targeted recurrence ids, or <code>null</code> if not applicable or to apply to the master instance only
     * @return The managed identifiers of the added attachments
     */
    public abstract int[] addAttachment(InputStream inputStream, String contentType, String fileName, long size, String[] recurrenceIDs) throws OXException;

    /**
     * Replaces an existing attachment with an updated one.
     *
     * @param attachmentId The identifier of the attachment to update
     * @param inputStream The binary attachment data
     * @param contentType The attachment's content type
     * @param fileName The target filename
     * @param size The attachment size
     * @return The managed identifier of the updated attachment
     */
    public abstract int updateAttachment(int attachmentId, InputStream inputStream, String contentType, String fileName, long size) throws OXException;

    /**
     * Removes an attachment from the underlying groupware object.
     *
     * @param attachmentId The identifier of the attachment to remove
     * @param recurrenceIDs The targeted recurrence ids, or <code>null</code> if not applicable or to apply to the master instance only
     */
    public abstract void removeAttachment(int attachmentId, String[] recurrenceIDs) throws OXException;

}
