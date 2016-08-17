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

import static com.openexchange.java.Strings.isEmpty;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.AJAXUtility;
import com.openexchange.ajax.requesthandler.responseRenderers.FileResponseRenderer;
import com.openexchange.ajax.requesthandler.responseRenderers.FileResponseRenderer.FileResponseRendererActionException;
import com.openexchange.java.Strings;

/**
 * {@link CheckParametersAction} Check certain parameters
 *
 * Influence the following IDataWrapper attributes:
 * <ul>
 * <li>delivery
 * <li>contentType
 * <li>contentTypeByParameter
 * <li>contentDisposition
 * </ul>
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.0
 */
public class CheckParametersAction implements IFileResponseRendererAction {

    private static final String DELIVERY = AJAXServlet.PARAMETER_DELIVERY;

    @Override
    public void call(IDataWrapper data) throws IOException, FileResponseRendererActionException {
        // Check certain parameters
        data.setDelivery(AJAXUtility.sanitizeParam(data.getRequest().getParameter(DELIVERY)));
        if (data.getDelivery() == null) {
            data.setDelivery(data.getFile().getDelivery());
        }
        data.setContentType(AJAXUtility.encodeUrl(data.getRequest().getParameter(IDataWrapper.PARAMETER_CONTENT_TYPE), true));
        data.setContentTypeByParameter(false);
        if (Strings.isEmpty(data.getContentType())) {
            if (IDataWrapper.DOWNLOAD.equalsIgnoreCase(data.getDelivery())) {
                data.setContentType(IDataWrapper.SAVE_AS_TYPE);
            } else {
                data.setContentType(data.getFileContentType());
            }
        } else {
            data.setContentTypeByParameter(true);
        }
        data.setContentType(unquote(data.getContentType()));
        // Delivery is set to "view", but Content-Type is indicated as application/octet-stream
        if (IDataWrapper.VIEW.equalsIgnoreCase(data.getDelivery()) && (null != data.getContentType() && data.getContentType().startsWith(IDataWrapper.SAVE_AS_TYPE))) {
            data.setContentType(FileResponseRenderer.getContentTypeByFileName(data.getFileName()));
            if (null == data.getContentType()) {
                // Not known
                data.setContentType(IDataWrapper.SAVE_AS_TYPE);
            }
        }
        data.setContentDisposition(AJAXUtility.encodeUrl(data.getRequest().getParameter(IDataWrapper.PARAMETER_CONTENT_DISPOSITION)));
        if (Strings.isEmpty(data.getContentDisposition())) {
            if (IDataWrapper.VIEW.equalsIgnoreCase(data.getDelivery())) {
                data.setContentDisposition("inline");
            } else if (IDataWrapper.DOWNLOAD.equalsIgnoreCase(data.getDelivery())) {
                data.setContentDisposition("attachment");
            } else {
                data.setContentDisposition(data.getFile().getDisposition());
            }
        }
        data.setContentDisposition(unquote(data.getContentDisposition()));

        if (null == data.getFile()) {
            // Quit with 404
            throw new FileResponseRenderer.FileResponseRendererActionException(HttpServletResponse.SC_NOT_FOUND, "File not found.");
        }
    }

    /**
     * Removes single or double quotes from a string if its quoted.
     *
     * @param s The value to be unquoted
     * @return The unquoted value or <code>null</code>
     */
    private String unquote(final String s) {
        if (!isEmpty(s) && ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'")))) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

}
