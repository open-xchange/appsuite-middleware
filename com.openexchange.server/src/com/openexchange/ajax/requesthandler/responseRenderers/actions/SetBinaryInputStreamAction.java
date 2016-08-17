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

package com.openexchange.ajax.requesthandler.responseRenderers.actions;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.fileholder.InputStreamReadable;
import com.openexchange.ajax.fileholder.IFileHolder.RandomAccess;
import com.openexchange.ajax.requesthandler.responseRenderers.FileResponseRenderer;

/**
 * {@link SetBinaryInputStreamAction} set the documentData as an binary InputStream
 *
 * Influence the following IDataWrapper attributes:
 * <ul>
 * <li>documentData
 * </ul>
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.0
 */
public class SetBinaryInputStreamAction implements IFileResponseRendererAction {

    @Override
    public void call(IDataWrapper data) throws Exception {
        // Set binary input stream
        RandomAccess randomAccess = data.getFile().getRandomAccess();
        if (null == randomAccess) {
            InputStream stream = data.getFile().getStream();
            if (null != stream) {
                if ((stream instanceof ByteArrayInputStream) || (stream instanceof BufferedInputStream)) {
                    data.setDocumentData(new InputStreamReadable(stream));
                } else {
                    data.setDocumentData(new InputStreamReadable(new BufferedInputStream(stream, 65536)));
                }
            }
        } else {
            data.setDocumentData(randomAccess);
        }

        if (null == data.getDocumentData()) {
            // Quit with 404
            throw new FileResponseRenderer.FileResponseRendererActionException(HttpServletResponse.SC_NOT_FOUND, "File not found.");
        }
    }
}
