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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.drive.client.windows.files;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Pattern;
import com.openexchange.exception.OXException;

/**
 * {@link UpdateFilesProvider} provides all setup files for the update
 * 
 * Initially checks for existing setup files and provides them afterwards
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public interface UpdateFilesProvider {

    /**
     * Forget all knows configuration's and setup files and search them under the last used path.
     * 
     * @throws OXException
     */
    public void reload() throws OXException;

    /**
     * Forget all knows configuration's and setup files and search them under given path.
     * 
     * @throws OXException
     */
    void reload(String path) throws OXException;

    /**
     * Returns the given setup file as a stream
     * 
     * @param category The file category
     * @param branding The branding of the file. Default: "generic"
     * @param name The filename
     * @return the given file as a stream
     * @throws OXException
     */
    public InputStream getFile(String branding, String name) throws OXException;

    /**
     * Checks if the provider knows the given file.
     * 
     * @param category
     * @param branding
     * @param name
     * @return
     * @throws OXException
     */
    public boolean contains(String branding, String name) throws OXException;

    /**
     * Retrieves the size of the given file
     * 
     * @param categoryDrive
     * @param fileName
     * @return
     * @throws OXException
     */
    public long getSize(String branding, String name) throws OXException;

    /**
     * Retrieves the first filename which matches the given regex expression.
     * 
     * @param category
     * @param branding
     * @param regex
     * @return the name or null
     * @throws OXException
     */
    public String getFileName(String branding, Pattern regex) throws OXException;

    /**
     * Retrieves the md5 checksum for the given file.
     * 
     * @param categoryDrive
     * @param exeFileName
     * @return
     * @throws IOException
     * @throws OXException
     */
    public String getMD5(String branding, String name) throws IOException, OXException;

    /**
     * Retrieves the icon for the specific branding.
     * 
     * @param branding
     * @return
     * @throws IOException
     */
    public String getIcon(String branding) throws IOException;

    /**
     * Retrieves the branding identifiers of all available branding's.
     * 
     * @return A list of identifiers.
     */
    public List<String> getAvailableBrandings();

    /**
     * Checks if the given branding identifier is valid.
     * 
     * @param branding
     * @return true if the UpdateFilesProvider knows the branding.
     */
    public boolean isValid(String branding);

}
