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

package com.openexchange.file.storage.infostore.internal;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.utils.Metadata;


/**
 * {@link FieldMapping}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FieldMapping {

    private static final Map<File.Field, Metadata> field2metadata = new EnumMap<File.Field, Metadata>(File.Field.class);

    static {
        field2metadata.put(File.Field.LAST_MODIFIED, Metadata.LAST_MODIFIED_LITERAL);
        field2metadata.put(File.Field.CREATED, Metadata.CREATION_DATE_LITERAL);
        field2metadata.put(File.Field.MODIFIED_BY, Metadata.MODIFIED_BY_LITERAL);
        field2metadata.put(File.Field.FOLDER_ID, Metadata.FOLDER_ID_LITERAL);
        field2metadata.put(File.Field.TITLE, Metadata.TITLE_LITERAL);
        field2metadata.put(File.Field.VERSION, Metadata.VERSION_LITERAL);
        field2metadata.put(File.Field.CONTENT, Metadata.CONTENT_LITERAL);
        field2metadata.put(File.Field.ID, Metadata.ID_LITERAL);
        field2metadata.put(File.Field.FILE_SIZE, Metadata.FILE_SIZE_LITERAL);
        field2metadata.put(File.Field.DESCRIPTION, Metadata.DESCRIPTION_LITERAL);
        field2metadata.put(File.Field.URL, Metadata.URL_LITERAL);
        field2metadata.put(File.Field.CREATED_BY, Metadata.CREATED_BY_LITERAL);
        field2metadata.put(File.Field.FILENAME, Metadata.FILENAME_LITERAL);
        field2metadata.put(File.Field.FILE_MIMETYPE, Metadata.FILE_MIMETYPE_LITERAL);
        field2metadata.put(File.Field.SEQUENCE_NUMBER, Metadata.SEQUENCE_NUMBER_LITERAL);
        field2metadata.put(File.Field.CATEGORIES, Metadata.CATEGORIES_LITERAL);
        field2metadata.put(File.Field.LOCKED_UNTIL, Metadata.LOCKED_UNTIL_LITERAL);
        field2metadata.put(File.Field.FILE_MD5SUM, Metadata.FILE_MD5SUM_LITERAL);
        field2metadata.put(File.Field.VERSION_COMMENT, Metadata.VERSION_COMMENT_LITERAL);
        field2metadata.put(File.Field.CURRENT_VERSION, Metadata.CURRENT_VERSION_LITERAL);
        field2metadata.put(File.Field.COLOR_LABEL, Metadata.COLOR_LABEL_LITERAL);
        field2metadata.put(File.Field.LAST_MODIFIED_UTC, Metadata.LAST_MODIFIED_UTC_LITERAL);
        field2metadata.put(File.Field.NUMBER_OF_VERSIONS, Metadata.NUMBER_OF_VERSIONS_LITERAL);
        field2metadata.put(File.Field.META, Metadata.META_LITERAL);
        field2metadata.put(File.Field.OBJECT_PERMISSIONS, Metadata.OBJECT_PERMISSIONS_LITERAL);
        field2metadata.put(File.Field.SHAREABLE, Metadata.SHAREABLE_LITERAL);
        field2metadata.put(File.Field.ORIGIN, Metadata.ORIGIN_LITERAL);
        field2metadata.put(File.Field.CAPTURE_DATE, Metadata.CAPTURE_DATE_LITERAL);
        field2metadata.put(File.Field.WIDTH, Metadata.WIDTH_LITERAL);
        field2metadata.put(File.Field.HEIGHT, Metadata.HEIGHT_LITERAL);
        field2metadata.put(File.Field.CAMERA_MAKE, Metadata.CAMERA_MAKE_LITERAL);
        field2metadata.put(File.Field.CAMERA_MODEL, Metadata.CAMERA_MODEL_LITERAL);
        field2metadata.put(File.Field.CAMERA_ISO_SPEED, Metadata.CAMERA_ISO_SPEED_LITERAL);
        field2metadata.put(File.Field.CAMERA_APERTURE, Metadata.CAMERA_APERTURE_LITERAL);
        field2metadata.put(File.Field.CAMERA_EXPOSURE_TIME, Metadata.CAMERA_EXPOSURE_TIME_LITERAL);
        field2metadata.put(File.Field.CAMERA_FOCAL_LENGTH, Metadata.CAMERA_FOCAL_LENGTH_LITERAL);
        field2metadata.put(File.Field.GEOLOCATION, Metadata.GEOLOCATION_LITERAL);
        field2metadata.put(File.Field.MEDIA_META, Metadata.MEDIA_META_LITERAL);
        field2metadata.put(File.Field.MEDIA_STATUS, Metadata.MEDIA_STATUS_LITERAL);
        field2metadata.put(File.Field.MEDIA_DATE, Metadata.MEDIA_DATE_LITERAL);
        field2metadata.put(File.Field.CREATED_FROM, Metadata.CREATED_FROM_LITERAL);
        field2metadata.put(File.Field.MODIFIED_FROM, Metadata.MODIFIED_FROM_LITERAL);
        field2metadata.put(File.Field.UNIQUE_ID, Metadata.UNIQUE_ID_LITERAL);
    }

    public static Metadata getMatching(File.Field field) {
        return field2metadata.get(field);
    }

    public static Metadata[] getMatching(List<File.Field> fields) {
        Metadata[] retval = new Metadata[fields.size()];
        for(int i = 0; i < retval.length; i++) {
            retval[i] = getMatching(fields.get(i));
        }
        return removeNullElements(retval);
    }

    public static int getSortDirection(SortDirection order) {
        return SortDirection.DESC.equals(order) ? InfostoreFacade.DESC : InfostoreFacade.ASC;
    }

    private static Metadata[] removeNullElements(Metadata[] source) {
        List<Metadata> tmp = null;
        for (int i = 0; i < source.length; i++) {
            Metadata metadata = source[i];
            if (null == metadata) {
                tmp = Lists.newArrayList(
                    Iterables.filter(Arrays.asList(source), Predicates.notNull()));
                break;
            }
        }
        return null == tmp ? source : tmp.toArray(new Metadata[tmp.size()]);
    }

}
