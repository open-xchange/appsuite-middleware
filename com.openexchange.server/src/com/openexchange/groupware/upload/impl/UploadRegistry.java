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

package com.openexchange.groupware.upload.impl;

import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * An interface that defines a method to register instances of <code>com.openexchange.groupware.upload.UploadListener</code>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface UploadRegistry {

    /**
     * Fires the upload event by delegating this event to all registered listeners. Finally the <code>UploadEvent.cleanUp()</code> method is
     * invoked to delete temporary files from disk.
     *
     * @param uploadEvent The upload event
     * @param uploadListeners The upload listeners for current upload event
     * @throws OXException If an error like over quota occurs
     */
    void fireUploadEvent(UploadEvent uploadEvent, Collection<UploadListener> uploadListeners) throws OXException;

    /**
     * Create an <code>UpdateEvent</code> object from incoming multipart form data
     *
     * @param req The corresponding instance of <code>HttpServletRequest</code>
     * @return An <code>UpdateEvent</code> object from incoming multipart form data
     * @throws OXException If an error like over quota occurs
     * @deprecated Use {@link #processUpload(HttpServletRequest, long, long, Session)}
     */
    @Deprecated
    UploadEvent processUpload(HttpServletRequest req) throws OXException;

    /**
     * Create an <code>UpdateEvent</code> object from incoming multipart form data
     *
     * @param req The corresponding instance of <code>HttpServletRequest</code>
     * @param maxFileSize The maximum allowed size of a single uploaded file or <code>-1</code>
     * @param maxOverallSize The maximum allowed size of a complete request or <code>-1</code>
     * @param session The associated session or <code>null</code>
     * @return An <code>UpdateEvent</code> object from incoming multipart form data
     * @throws OXException If an error like over quota occurs
     */
    UploadEvent processUpload(HttpServletRequest req, long maxFileSize, long maxOverallSize, Session session) throws OXException;
}
