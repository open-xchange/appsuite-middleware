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

package com.openexchange.find.basic.drive;

import java.util.Arrays;
import java.util.List;


/**
 * {@link Constants}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Constants {

    /**
     * Initializes a new {@link Constants}.
     */
    private Constants() {
        super();
    }

    /** The virtual "global" field for file name, title and description */
    public static final String FIELD_GLOBAL = "global";

    /** The field for file name */
    public static final String FIELD_FILE_NAME = "filename";

    /** The field for file description */
    public static final String FIELD_FILE_DESC = "description";

    /** The field for file content */
    public static final String FIELD_FILE_CONTENT = "content";

    /** The field for file MIME type */
    public static final String FIELD_FILE_TYPE = "file_mimetype";

    /** The field for file size */
    public static final String FIELD_FILE_SIZE = "file_size";

    /** The field for file extension */
    public static final String FIELD_FILE_EXTENSION = "file_extension";

    /** The field for folder name */
    public static final String FIELD_FOLDER_NAME = "folder_name";

    // ---------------------------------------------------------------------------------------------------------- //

    /** The fields to query for */
    public static final List<String> QUERY_FIELDS = Arrays.asList(new String[] { FIELD_FILE_NAME, FIELD_FILE_DESC });

    /** The patterns used match {@link Type#DOCUMENTS}. */
    public static final String[] FILETYPE_PATTERNS_DOCUMENTS = {
        "text/*plain*",
        "text/*rtf*",
        "application/*ms-word*",
        "application/*ms-excel*",
        "application/*ms-powerpoint*",
        "application/*msword*",
        "application/*excel*",
        "application/*powerpoint*",
        "application/*openxmlformats*",
        "application/*opendocument*",
        "application/*pdf*",
        "application/*rtf*"
    };

    /** The patterns used match {@link Type#IMAGES}. */
    public static final String[] FILETYPE_PATTERNS_IMAGES = {
        "image/*"
    };

    /** The patterns used match {@link Type#AUDIO}. */
    public static final String[] FILETYPE_PATTERNS_AUDIO = {
        "audio/*"
    };

    /** The patterns used match {@link Type#VIDEO}. */
    public static final String[] FILETYPE_PATTERNS_VIDEO = {
        "video/*"
    };
    
    /** The file extension used match {@link Type#DOC_TEXT}. */
    public static final String[] FILE_EXTENSION_TEXT = {
        "*.docx",
        "*.docm",
        "*.dotx",
        "*.dotm",
        "*.odt",
        "*.ott",
        "*.doc",
        "*.dot",
        "*.txt",
        "*.rtf"
    };
    
    /** The file extension used match {@link Type#DOC_SPREADSHEET}. */
    public static final String[] FILE_EXTENSION_SPREADSHEET = {
        "*.xlsx",
        "*.xlsm",
        "*.xltx",
        "*.xltm",
        "*.xlsb",
        "*.ods",
        "*.ots",
        "*.xls",
        "*.xlt",
        "*.xla"
    };
    
    /** The file extension used match {@link Type#DOC_PRESENTATION}. */    
    public static final String[] FILE_EXTENSION_PRESENTATION = {
        "*.pptx",
        "*.pptm",
        "*.potx",
        "*.potx",
        "*.ppsx",
        "*.ppsm",
        "*.ppam",
        "*.odp",
        "*.otp",
        "*.ppt",
        "*.pot",
        "*.pps",
        "*.ppa"
    };
    
    /** The file extension used match {@link Type#PDF}. */    
    public static final String[] FILE_EXTENSION_PDF = {
        "*.pdf"
    };
    
    /** The file extension used match {@link Type#IMAGE}. */    
    public static final String[] FILE_EXTENSION_IMAGE = {
        "*.png",
        "*.jpg",
        "*.jpeg",
        "*.gif",
        "*.tiff",
        "*.bmp",
        "*.heic",
        "*.heif"
    };   
    
    /** The file extension used match {@link Type#VIDEO}. */    
    public static final String[] FILE_EXTENSION_VIDEO = {
        "*.m4v",
        "*.ogv",
        "*.webm",
        "*.mov",
        "*.avi",
        "*.wmv",
        "*.wma",
        "*.mpg",
        "*.mpeg",
        "*.mp4",
        "*.mpg"
    };  
    
    /** The file extension used match {@link Type#MUSIC}. */    
    public static final String[] FILE_EXTENSION_AUDIO = {
        "*.mp3",
        "*.m4a",
        "*.m4b",
        "*.ogg",
        "*.aac",
        "*.wav",
        "*.wma",
        "*.mid",
        "*.ra",
        "*.ram",
        "*.rm",
        "*.m3u",
        "*.mp4a",
        "*.mpga"
    };  
}