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

package com.openexchange.groupware.infostore.mbean;

import java.util.List;
import javax.management.MBeanException;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.QuotaFileStorageService;
import com.openexchange.groupware.infostore.utils.FileMD5SumHelper;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link FileChecksumsMBeanImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class FileChecksumsMBeanImpl extends StandardMBean implements FileChecksumsMBean {

    /**
     * Initializes a new {@link FileChecksumsMBeanImpl}.
     */
    public FileChecksumsMBeanImpl() throws NotCompliantMBeanException {
        super(FileChecksumsMBean.class);
    }

    @Override
    public List<String> listFilesWithoutChecksumInContext(int contextId) throws MBeanException {
        try {
            return FileMD5SumHelper.toString(contextId, getMD5SumHelper().listMissingInContext(contextId));
        } catch (OXException e) {
            throw new MBeanException(e, e.getMessage());
        }
    }

    @Override
    public List<String> listFilesWithoutChecksumInDatabase(int databaseId) throws MBeanException {
        try {
            return FileMD5SumHelper.toString(getMD5SumHelper().listMissingInDatabase(databaseId));
        } catch (OXException e) {
            throw new MBeanException(e, e.getMessage());
        }
    }

    @Override
    public List<String> listAllFilesWithoutChecksum() throws MBeanException {
        try {
            return FileMD5SumHelper.toString(getMD5SumHelper().listAllMissing());
        } catch (OXException e) {
            throw new MBeanException(e, e.getMessage());
        }
    }

    @Override
    public List<String> calculateMissingChecksumsInContext(int contextId) throws MBeanException {
        try {
            return FileMD5SumHelper.toString(contextId, getMD5SumHelper().calculateMissingInContext(contextId));
        } catch (OXException e) {
            throw new MBeanException(e, e.getMessage());
        }
    }

    @Override
    public List<String> calculateMissingChecksumsInDatabase(int databaseId) throws MBeanException {
        try {
            return FileMD5SumHelper.toString(getMD5SumHelper().calculateMissingInDatabase(databaseId));
        } catch (OXException e) {
            throw new MBeanException(e, e.getMessage());
        }
    }

    @Override
    public List<String> calculateAllMissingChecksums() throws MBeanException {
        try {
            return FileMD5SumHelper.toString(getMD5SumHelper().calculateAllMissing());
        } catch (OXException e) {
            throw new MBeanException(e, e.getMessage());
        }
    }

    private static FileMD5SumHelper getMD5SumHelper() throws OXException {
        DatabaseService dbService = ServerServiceRegistry.getServize(DatabaseService.class, true);
        QuotaFileStorageService fsService = ServerServiceRegistry.getServize(QuotaFileStorageService.class, true);
        return new FileMD5SumHelper(dbService, fsService);
    }

}
