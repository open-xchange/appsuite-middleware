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

package com.openexchange.drive.json.json;

import java.util.Date;
import java.util.EnumMap;
import com.openexchange.drive.DriveAction;
import com.openexchange.drive.DriveFileField;
import com.openexchange.drive.DriveFileMetadata;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.json.DateMapping;
import com.openexchange.groupware.tools.mappings.json.DefaultJsonMapper;
import com.openexchange.groupware.tools.mappings.json.JsonMapping;
import com.openexchange.groupware.tools.mappings.json.StringMapping;

/**
 * {@link DriveFieldMapper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveFieldMapper extends DefaultJsonMapper<DriveFileMetadata, DriveFileField> {

    static final int COLUMN_CREATED = 4;
    static final int COLUMN_MODIFIED = 5;
    static final int COLUMN_NAME = 702;
    static final int COLUMN_CONTENT_TYPE = 703;
    static final int COLUMN_CHECKSUM = 708;
    static final int COLUMN_PREVIEW_LINK = 750;
    static final int COLUMN_DIRECT_LINK_FRAGMENTS = 751;
    static final int COLUMN_DIRECT_LINK = 752;
    static final int COLUMN_THUMBNAIL_LINK = 753;

    private static final DriveFieldMapper INSTANCE = new DriveFieldMapper();

    /**
     * Gets the DriveFieldMapper instance.
     *
     * @return The DriveFieldMapper instance.
     */
    public static DriveFieldMapper getInstance() {
        return INSTANCE;
    }

    @Override
    public DriveFileMetadata newInstance() {
        return new DriveFileMetadata();
    }

    @Override
    public DriveFileField[] newArray(int size) {
        return new DriveFileField[size];
    }

    @Override
    protected EnumMap<DriveFileField, ? extends JsonMapping<? extends Object, DriveFileMetadata>> createMappings() {
        EnumMap<DriveFileField, JsonMapping<? extends Object, DriveFileMetadata>> mappings = new
            EnumMap<DriveFileField, JsonMapping<? extends Object, DriveFileMetadata>>(DriveFileField.class);

        mappings.put(DriveFileField.CHECKSUM, new StringMapping<DriveFileMetadata>("checksum", COLUMN_CHECKSUM) {

            @Override
            public boolean isSet(DriveFileMetadata object) {
                return null != object.getChecksum();
            }

            @Override
            public void set(DriveFileMetadata object, String value) throws OXException {
                object.setChecksum(value);
            }

            @Override
            public String get(DriveFileMetadata object) {
                return object.getChecksum();
            }

            @Override
            public void remove(DriveFileMetadata object) {
                object.setChecksum(null);
            }
        });

        mappings.put(DriveFileField.NAME, new StringMapping<DriveFileMetadata>("name", COLUMN_NAME) {

            @Override
            public boolean isSet(DriveFileMetadata object) {
                return null != object.getFileName();
            }

            @Override
            public void set(DriveFileMetadata object, String value) throws OXException {
                object.setFileName(value);
            }

            @Override
            public String get(DriveFileMetadata object) {
                return object.getFileName();
            }

            @Override
            public void remove(DriveFileMetadata object) {
                object.setFileName(null);
            }
        });

        mappings.put(DriveFileField.CONTENT_TYPE, new StringMapping<DriveFileMetadata>(DriveAction.PARAMETER_CONTENT_TYPE, COLUMN_CONTENT_TYPE) {

            @Override
            public boolean isSet(DriveFileMetadata object) {
                return null != object.getMimeType();
            }

            @Override
            public void set(DriveFileMetadata object, String value) throws OXException {
                object.setMimeType(value);
            }

            @Override
            public String get(DriveFileMetadata object) {
                return object.getMimeType();
            }

            @Override
            public void remove(DriveFileMetadata object) {
                object.setMimeType(null);
            }
        });

        mappings.put(DriveFileField.MODIFIED, new DateMapping<DriveFileMetadata>(DriveAction.PARAMETER_MODIFIED, COLUMN_MODIFIED) {

            @Override
            public boolean isSet(DriveFileMetadata object) {
                return null != object.getModified();
            }

            @Override
            public void set(DriveFileMetadata object, Date value) throws OXException {
                object.setModified(value);
            }

            @Override
            public Date get(DriveFileMetadata object) {
                return object.getModified();
            }

            @Override
            public void remove(DriveFileMetadata object) {
                object.setModified(null);
            }
        });

        mappings.put(DriveFileField.CREATED, new DateMapping<DriveFileMetadata>(DriveAction.PARAMETER_CREATED, COLUMN_CREATED) {

            @Override
            public boolean isSet(DriveFileMetadata object) {
                return null != object.getCreated();
            }

            @Override
            public void set(DriveFileMetadata object, Date value) throws OXException {
                object.setCreated(value);
            }

            @Override
            public Date get(DriveFileMetadata object) {
                return object.getCreated();
            }

            @Override
            public void remove(DriveFileMetadata object) {
                object.setCreated(null);
            }
        });

        mappings.put(DriveFileField.DIRECT_LINK, new StringMapping<DriveFileMetadata>(DriveAction.PARAMETER_DIRECT_LINK, COLUMN_DIRECT_LINK) {

            @Override
            public boolean isSet(DriveFileMetadata object) {
                return null != object.getDirectLink();
            }

            @Override
            public void set(DriveFileMetadata object, String value) throws OXException {
                object.setDirectLink(value);
            }

            @Override
            public String get(DriveFileMetadata object) {
                return object.getDirectLink();
            }

            @Override
            public void remove(DriveFileMetadata object) {
                object.setDirectLink(null);
            }
        });

        mappings.put(DriveFileField.DIRECT_LINK_FRAGMENTS, new StringMapping<DriveFileMetadata>(DriveAction.PARAMETER_DIRECT_LINK_FRAGMENTS, COLUMN_DIRECT_LINK_FRAGMENTS) {

            @Override
            public boolean isSet(DriveFileMetadata object) {
                return null != object.getDirectLinkFragments();
            }

            @Override
            public void set(DriveFileMetadata object, String value) throws OXException {
                object.setDirectLinkFragments(value);
            }

            @Override
            public String get(DriveFileMetadata object) {
                return object.getDirectLinkFragments();
            }

            @Override
            public void remove(DriveFileMetadata object) {
                object.setDirectLinkFragments(null);
            }
        });

        mappings.put(DriveFileField.PREVIEW_LINK, new StringMapping<DriveFileMetadata>(DriveAction.PARAMETER_PREVIEW_LINK, COLUMN_PREVIEW_LINK) {

            @Override
            public boolean isSet(DriveFileMetadata object) {
                return null != object.getPreviewLink();
            }

            @Override
            public void set(DriveFileMetadata object, String value) throws OXException {
                object.setPreviewLink(value);
            }

            @Override
            public String get(DriveFileMetadata object) {
                return object.getPreviewLink();
            }

            @Override
            public void remove(DriveFileMetadata object) {
                object.setPreviewLink(null);
            }
        });

        mappings.put(DriveFileField.THUMBNAIL_LINK, new StringMapping<DriveFileMetadata>(DriveAction.PARAMETER_THUMBNAIL_LINK, COLUMN_THUMBNAIL_LINK) {

            @Override
            public boolean isSet(DriveFileMetadata object) {
                return null != object.getThumbnailLink();
            }

            @Override
            public void set(DriveFileMetadata object, String value) throws OXException {
                object.setThumbnailLink(value);
            }

            @Override
            public String get(DriveFileMetadata object) {
                return object.getThumbnailLink();
            }

            @Override
            public void remove(DriveFileMetadata object) {
                object.setThumbnailLink(null);
            }
        });

        return mappings;
    }

}
