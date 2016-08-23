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

package com.openexchange.find.basic.mail;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * {@link Constants}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class Constants {

    final static String FIELD_FROM = "from";

    final static String FIELD_TO = "to";

    final static String FIELD_CC = "cc";

    final static String FIELD_BCC = "bcc";

    final static String FIELD_SUBJECT = "subject";

    final static String FIELD_BODY = "body";

    final static String FIELD_FOLDER = "folder";

    final static String FIELD_FILENAME_NAME = "filename";

    static final List<String> FROM_FIELDS = asList(FIELD_FROM);

    static final List<String> TO_FIELDS = Arrays.asList(new String[] { FIELD_TO, FIELD_CC, FIELD_BCC });

    static final List<String> FROM_AND_TO_FIELDS = Arrays.asList(new String[] { FIELD_FROM, FIELD_TO, FIELD_CC, FIELD_BCC });

    static final List<String> FOLDERS_FIELDS = Arrays.asList(new String[] { FIELD_FOLDER });

    static final List<String> QUERY_FIELDS = Arrays.asList(new String[] { FIELD_SUBJECT, FIELD_FROM, FIELD_TO, FIELD_CC, FIELD_BCC });

    static final List<String> QUERY_FIELDS_BODY = Arrays.asList(new String[] { FIELD_SUBJECT, FIELD_FROM, FIELD_TO, FIELD_CC, FIELD_BCC, FIELD_BODY });

    static List<String> asList(String str) {
        return Collections.singletonList(str);
    }

}
