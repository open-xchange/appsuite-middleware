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

package com.openexchange.ajax.infostore;

import junit.framework.TestCase;

import com.openexchange.ajax.parser.InfostoreParser;
import com.openexchange.groupware.infostore.DocumentMetadata;

public class InfostoreParserTest extends TestCase {

    public void testParseObject() throws Exception{
        final InfostoreParser parser = new InfostoreParser();

        DocumentMetadata dm = parser.getDocumentMetadata("{\"title\" : \"The title\", \"url\" : \"http://www.open-xchange.com\" , \"filename\" : \"test.txt\", \"file_mimetype\" :\"text/plain\", \"file_size\" : 12345, \"version\" : 1, \"description\" : \"Description\", \"file_md5sum\" : \"123n12b4askfa2\", \"folder_id\" : 23, \"categories\" : \"toll, fein, hervorragend, am tollsten\"}");

        assertEquals("The title",dm.getTitle());
        assertEquals("http://www.open-xchange.com",dm.getURL());
        assertEquals("test.txt", dm.getFileName());
        assertEquals("text/plain", dm.getFileMIMEType());
        assertEquals(12345,dm.getFileSize());
        assertEquals(1,dm.getVersion());
        assertEquals("123n12b4askfa2",dm.getFileMD5Sum());
        assertEquals(23,dm.getFolderId());

        assertEquals("toll, fein, hervorragend, am tollsten", dm.getCategories());

        dm = parser.getDocumentMetadata("{\"title\" : \"The title\", \"url\" : \"http://www.open-xchange.com\" , \"filename\" : \"test.txt\", \"file_mimetype\" :\"text/plain\", \"file_size\" : 12345, \"description\" : \"Description\", \"file_md5sum\" : \"123n12b4askfa2\", \"folder_id\" : 23, \"categories\" : \"toll, fein, hervorragend, am tollsten\"}");

        assertEquals(0,dm.getVersion());

    }
}
