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

package com.openexchange.mobilenotifier.utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.mobilenotifier.MobileNotifierExceptionCodes;

/**
 * {@link MobileNotifierFileUtility} - Util for file handling
 * 
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public abstract class MobileNotifierFileUtility {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MobileNotifierFileUtility.class);

    private static final String TEMPLATEPATH = "/templates/";

    /**
     * Gets a template file from the hard disk
     * 
     * @param templateFileName - The file name of the template
     * @return String - The content of the file
     */
    public static String getTemplateFileContent(final String templateFileName) throws OXException {
        final File file = new File(getTemplatePath() + templateFileName);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));

            StringBuilder sb = new StringBuilder(65532);
            String sep = System.getProperty("line.separator");
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append(sep);
            }
            return sb.toString();
        } catch (FileNotFoundException e) {
            LOG.error("Could not found file: {} ", file.toString());
            throw MobileNotifierExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw MobileNotifierExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(br);
        }
    }

    /**
     * Writes a template to a file
     * 
     * @param templateFileName the file name of the template
     * @param content the content of the template
     * @throws OXException
     */
    public static void writeTemplateFileContent(final String templateFileName, String content) throws OXException {
        try {
            final BufferedWriter isw = new BufferedWriter(new FileWriter(getTemplatePath() + templateFileName));
            isw.write(content);
            Streams.close(isw);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw MobileNotifierExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Gets the template path in the specific OS format
     * 
     * @return path to template folder
     */
    private static String getTemplatePath() {
        final char fileSep = File.separatorChar;
        final String path = TEMPLATEPATH.replace('/', fileSep);
        return System.getProperty("openexchange.propdir") + path;
    }
}
