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

package com.openexchange.filemanagement.internal;

import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.DistributedFileUtils;
import com.openexchange.mail.utils.MailPasswordUtil;


/**
 * {@link DistributedFileUtilsImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class DistributedFileUtilsImpl implements DistributedFileUtils {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(DistributedFileUtilsImpl.class);
    }

    /**
     * Initializes a new {@link DistributedFileUtilsImpl}.
     */
    public DistributedFileUtilsImpl() {
        super();
    }

    private static final java.security.Key DFM_KEY = MailPasswordUtil.generateSecretKey("open-xchange");

    @Override
    public String encodeId(String rawId) throws OXException {
        if (rawId == null) {
            return null;
        }

        try {
            String encrypted = MailPasswordUtil.encrypt(rawId, DFM_KEY);
            return com.openexchange.ajax.AJAXUtility.encodeUrl(encrypted);
        } catch (java.security.GeneralSecurityException x) {
            LoggerHolder.LOG.debug("Failed to encode identifier", x);
            return rawId;
        }
    }

    @Override
    public String decodeId(String encodedId) throws OXException {
        if (encodedId == null) {
            return null;
        }

        try {
            String urlDecoded = com.openexchange.ajax.AJAXUtility.decodeUrl(encodedId, "UTF-8");
            return MailPasswordUtil.decrypt(urlDecoded, DFM_KEY);
        } catch (java.security.GeneralSecurityException x) {
            LoggerHolder.LOG.debug("Failed to decode identifier", x);
            return encodedId;
        }
    }

}
