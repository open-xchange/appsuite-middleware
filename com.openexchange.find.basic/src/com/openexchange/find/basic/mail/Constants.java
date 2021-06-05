/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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

    final static String FIELD_HAS_ATTACHMENT = "has_attachment";

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
