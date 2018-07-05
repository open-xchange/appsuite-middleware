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

package com.openexchange.imageconverter.api;

import com.openexchange.annotation.NonNull;

/**
 * {@link MetaDataTag}
 *
 * @author <a href="mailto:kai.ahrens@open-xchange.com">Kai Ahrens</a>
 * @since v7.10
 */
public enum MetadataKey {
    // Group EXIF
    EXIF_INTEROP_INDEX(MetadataGroup.EXIF, "Interoperability Index", 0x0001),
    EXIF_INTEROP_VERSION(MetadataGroup.EXIF, "Interoperability Version", 0x0002),
    EXIF_NEW_SUBFILE_TYPE(MetadataGroup.EXIF, "New Subfile Type", 0x00FE),
    EXIF_SUBFILE_TYPE(MetadataGroup.EXIF, "Subfile Type", 0x00FF),
    EXIF_IMAGE_WIDTH(MetadataGroup.EXIF, "Image Width", 0x0100),
    EXIF_IMAGE_HEIGHT(MetadataGroup.EXIF, "Image Height", 0x0101),
    EXIF_BITS_PER_SAMPLE(MetadataGroup.EXIF, "Bits Per Sample", 0x0102),
    EXIF_COMPRESSION(MetadataGroup.EXIF, "Compression", 0x0103),
    EXIF_PHOTOMETRIC_INTERPRETATION(MetadataGroup.EXIF, "Photometric Interpretation", 0x0106),
    EXIF_THRESHOLDING(MetadataGroup.EXIF, "Thresholding", 0x0107),
    EXIF_FILL_ORDER(MetadataGroup.EXIF, "Fill Order", 0x010A),
    EXIF_DOCUMENT_NAME(MetadataGroup.EXIF, "Document Name", 0x010D),
    EXIF_IMAGE_DESCRIPTION(MetadataGroup.EXIF, "Image Description", 0x010E),
    EXIF_MAKE(MetadataGroup.EXIF, "Make", 0x010F),
    EXIF_MODEL(MetadataGroup.EXIF, "Model", 0x0110),
    EXIF_STRIP_OFFSETS(MetadataGroup.EXIF, "Strip Offsets", 0x0111),
    EXIF_ORIENTATION(MetadataGroup.EXIF, "Orientation", 0x0112),
    EXIF_SAMPLES_PER_PIXEL(MetadataGroup.EXIF, "Samples Per Pixel", 0x0115),
    EXIF_ROWS_PER_STRIP(MetadataGroup.EXIF, "Rows Per Strip", 0x0116),
    EXIF_STRIP_BYTE_COUNTS(MetadataGroup.EXIF, "Strip Byte Counts", 0x0117),
    EXIF_MIN_SAMPLE_VALUE(MetadataGroup.EXIF, "Minimum Sample Value", 0x0118),
    EXIF_MAX_SAMPLE_VALUE(MetadataGroup.EXIF, "Maximum Sample Value", 0x0119),
    EXIF_X_RESOLUTION(MetadataGroup.EXIF, "X Resolution", 0x011A),
    EXIF_Y_RESOLUTION(MetadataGroup.EXIF, "Y Resolution", 0x011B),
    EXIF_PLANAR_CONFIGURATION(MetadataGroup.EXIF, "Planar Configuration", 0x011C),
    EXIF_PAGE_NAME(MetadataGroup.EXIF, "Page Name", 0x011D),
    EXIF_RESOLUTION_UNIT(MetadataGroup.EXIF, "Resolution Unit", 0x0128),
    EXIF_PAGE_NUMBER(MetadataGroup.EXIF, "Page Number", 0x0129),
    EXIF_TRANSFER_FUNCTION(MetadataGroup.EXIF, "Transfer Function", 0x012D),
    EXIF_SOFTWARE(MetadataGroup.EXIF, "Software", 0x0131),
    EXIF_DATETIME(MetadataGroup.EXIF, "Date/Time", 0x0132),
    EXIF_ARTIST(MetadataGroup.EXIF, "Artist", 0x013B),
    EXIF_PREDICTOR(MetadataGroup.EXIF, "Predictor", 0x013D),
    EXIF_HOST_COMPUTER(MetadataGroup.EXIF, "Host Computer", 0x013C),
    EXIF_WHITE_POINT(MetadataGroup.EXIF, "White Point", 0x013E),
    EXIF_PRIMARY_CHROMATICITIES(MetadataGroup.EXIF, "Primary Chromaticities", 0x013F),
    EXIF_TILE_WIDTH(MetadataGroup.EXIF, "Tile Width", 0x0142),
    EXIF_TILE_LENGTH(MetadataGroup.EXIF, "Tile Length", 0x0143),
    EXIF_TILE_OFFSETS(MetadataGroup.EXIF, "Tile Offsets", 0x0144),
    EXIF_TILE_BYTE_COUNTS(MetadataGroup.EXIF, "Tile Byte Counts", 0x0145),
    EXIF_SUB_IFD_OFFSET(MetadataGroup.EXIF, "Sub IFD Pointer(s)", 0x014a),
    EXIF_TRANSFER_RANGE(MetadataGroup.EXIF, "Transfer Range", 0x0156),
    EXIF_JPEG_TABLES(MetadataGroup.EXIF, "JPEG Tables", 0x015B),
    EXIF_JPEG_PROC(MetadataGroup.EXIF, "JPEG Proc", 0x0200),
    EXIF_JPEG_RESTART_INTERVAL(MetadataGroup.EXIF, "JPEG Restart Interval", 0x0203),
    EXIF_JPEG_LOSSLESS_PREDICTORS(MetadataGroup.EXIF, "JPEG Lossless Predictors", 0x0205),
    EXIF_JPEG_POINT_TRANSFORMS(MetadataGroup.EXIF, "JPEG Point Transforms", 0x0206),
    EXIF_JPEG_Q_TABLES(MetadataGroup.EXIF, "JPEGQ Tables", 0x0207),
    EXIF_JPEG_DC_TABLES(MetadataGroup.EXIF, "JPEGDC Tables", 0x0208),
    EXIF_JPEG_AC_TABLES(MetadataGroup.EXIF, "JPEGAC Tables", 0x0209),
    EXIF_YCBCR_COEFFICIENTS(MetadataGroup.EXIF, "YCbCr Coefficients", 0x0211),
    EXIF_YCBCR_SUBSAMPLING(MetadataGroup.EXIF, "YCbCr Sub-Sampling", 0x0212),
    EXIF_YCBCR_POSITIONING(MetadataGroup.EXIF, "YCbCr Positioning", 0x0213),
    EXIF_REFERENCE_BLACK_WHITE(MetadataGroup.EXIF, "Reference Black/White", 0x0214),
    EXIF_STRIP_ROW_COUNTS(MetadataGroup.EXIF, "Strip Row Counts", 0x022f),
    EXIF_APPLICATION_NOTES(MetadataGroup.EXIF, "Application Notes", 0x02bc),
    EXIF_RELATED_IMAGE_FILE_FORMAT(MetadataGroup.EXIF, "Related Image File Format", 0x1000),
    EXIF_RELATED_IMAGE_WIDTH(MetadataGroup.EXIF, "Related Image Width", 0x1001),
    EXIF_RELATED_IMAGE_HEIGHT(MetadataGroup.EXIF, "Related Image Height",0x1002 ),
    EXIF_RATING(MetadataGroup.EXIF, "Rating", 0x4746),
    EXIF_CFA_REPEAT_PATTERN_DIM(MetadataGroup.EXIF, "CFA Repeat Pattern Dim", 0x828D),
    EXIF_CFA_PATTERN_2(MetadataGroup.EXIF, "CFA Pattern 2", 0x828E),
    EXIF_BATTERY_LEVEL(MetadataGroup.EXIF, "Battery Level", 0x828F),
    EXIF_COPYRIGHT(MetadataGroup.EXIF, "Copyright", 0x8298),
    EXIF_EXPOSURE_TIME(MetadataGroup.EXIF, "Exposure Time", 0x829A),
    EXIF_FNUMBER(MetadataGroup.EXIF, "F-Number", 0x829D),
    EXIF_IPTC_NAA(MetadataGroup.EXIF, "IPTC/NAA", 0x83BB),
    EXIF_INTER_COLOR_PROFILE(MetadataGroup.EXIF, "Inter Color Profile", 0x8773),
    EXIF_EXPOSURE_PROGRAM(MetadataGroup.EXIF, "Exposure Program", 0x8822),
    EXIF_SPECTRAL_SENSITIVITY(MetadataGroup.EXIF, "Spectral Sensitivity", 0x8824),
    EXIF_ISO_EQUIVALENT(MetadataGroup.EXIF, "ISO Speed Ratings", 0x8827),
    EXIF_OPTO_ELECTRIC_CONVERSION_FUNCTION(MetadataGroup.EXIF, "Opto-electric Conversion Function (OECF)", 0x8828),
    EXIF_INTERLACE(MetadataGroup.EXIF, "Interlace", 0x8829),
    EXIF_SENSITIVITY_TYPE(MetadataGroup.EXIF, "Sensitivity Type", 0x8830),
    EXIF_STANDARD_OUTPUT_SENSITIVITY(MetadataGroup.EXIF, "Standard Output Sensitivity", 0x8831),
    EXIF_RECOMMENDED_EXPOSURE_INDEX(MetadataGroup.EXIF, "Recommended Exposure Index", 0x8832),
    EXIF_TIME_ZONE_OFFSET(MetadataGroup.EXIF, "Time Zone Offset", 0x882A),
    EXIF_SELF_TIMER_MODE(MetadataGroup.EXIF, "Self Timer Mode", 0x882B),
    EXIF_EXIF_VERSION(MetadataGroup.EXIF, "Exif Version", 0x9000),
    EXIF_DATETIME_ORIGINAL(MetadataGroup.EXIF, "Date/Time Original", 0x9003),
    EXIF_DATETIME_DIGITIZED(MetadataGroup.EXIF, "Date/Time Digitized", 0x9004),
    EXIF_COMPONENTS_CONFIGURATION(MetadataGroup.EXIF, "Components Configuration", 0x9101),
    EXIF_COMPRESSED_AVERAGE_BITS_PER_PIXEL(MetadataGroup.EXIF, "Compressed Bits Per Pixel", 0x9102),
    EXIF_SHUTTER_SPEED(MetadataGroup.EXIF, "Shutter Speed Value", 0x9201),
    EXIF_APERTURE(MetadataGroup.EXIF, "Aperture Value", 0x9202),
    EXIF_BRIGHTNESS_VALUE(MetadataGroup.EXIF, "Brightness Value", 0x9203),
    EXIF_EXPOSURE_BIAS(MetadataGroup.EXIF, "Exposure Bias Value", 0x9204),
    EXIF_MAX_APERTURE(MetadataGroup.EXIF, "Max Aperture Value", 0x9205),
    EXIF_SUBJECT_DISTANCE(MetadataGroup.EXIF, "Subject Distance", 0x9206),
    EXIF_METERING_MODE(MetadataGroup.EXIF, "Metering Mode", 0x9207),
    EXIF_WHITE_BALANCE(MetadataGroup.EXIF, "White Balance", 0x9208),
    EXIF_FLASH(MetadataGroup.EXIF, "Flash", 0x9209),
    EXIF_FOCAL_LENGTH(MetadataGroup.EXIF, "Focal Length", 0x920A),
    EXIF_FLASH_ENERGY_TIFF_EP(MetadataGroup.EXIF, "Flash Energy (TIFF)", 0x920B),
    EXIF_SPATIAL_FREQ_RESPONSE_TIFF_EP(MetadataGroup.EXIF, "Spatial Frequency Response (TIFF)", 0x920C),
    EXIF_NOISE(MetadataGroup.EXIF, "Noise", 0x920D),
    EXIF_FOCAL_PLANE_X_RESOLUTION_TIFF_EP(MetadataGroup.EXIF, "Focal Plane X Resolution (TIFF)", 0x920E),
    EXIF_FOCAL_PLANE_Y_RESOLUTION_TIFF_EP(MetadataGroup.EXIF, "Focal Plane Y Resolution (TIFF)", 0x920F),
    EXIF_IMAGE_NUMBER(MetadataGroup.EXIF, "Image Number", 0x9211),
    EXIF_SECURITY_CLASSIFICATION(MetadataGroup.EXIF, "Security Classification", 0x9212),
    EXIF_IMAGE_HISTORY(MetadataGroup.EXIF, "Image History", 0x9213),
    EXIF_SUBJECT_LOCATION_TIFF_EP(MetadataGroup.EXIF, "Subject Location (TIFF)", 0x9214),
    EXIF_EXPOSURE_INDEX_TIFF_EP(MetadataGroup.EXIF, "Exposure Index (TIFF)", 0x9215),
    EXIF_STANDARD_ID_TIFF_EP(MetadataGroup.EXIF, "TIFF/EP Standard ID", 0x9216),
    EXIF_MAKERNOTE(MetadataGroup.EXIF, "Makernote", 0x927C),
    EXIF_USER_COMMENT(MetadataGroup.EXIF, "User Comment", 0x9286),
    EXIF_SUBSECOND_TIME(MetadataGroup.EXIF, "Sub-Sec Time", 0x9290),
    EXIF_SUBSECOND_TIME_ORIGINAL(MetadataGroup.EXIF, "Sub-Sec Time Original", 0x9291),
    EXIF_SUBSECOND_TIME_DIGITIZED(MetadataGroup.EXIF, "Sub-Sec Time Digitized", 0x9292),
    EXIF_WIN_TITLE(MetadataGroup.EXIF, "Windows XP Title", 0x9C9B),
    EXIF_WIN_COMMENT(MetadataGroup.EXIF, "Windows XP Comment", 0x9C9C),
    EXIF_WIN_AUTHOR(MetadataGroup.EXIF, "Windows XP Author", 0x9C9D),
    EXIF_WIN_KEYWORDS(MetadataGroup.EXIF, "Windows XP Keywords", 0x9C9E),
    EXIF_WIN_SUBJECT(MetadataGroup.EXIF, "Windows XP Subject", 0x9C9F),
    EXIF_FLASHPIX_VERSION(MetadataGroup.EXIF, "FlashPix Version", 0xA000),
    EXIF_COLOR_SPACE(MetadataGroup.EXIF, "Color Space", 0xA001),
    EXIF_EXIF_IMAGE_WIDTH(MetadataGroup.EXIF, "Exif Image Width", 0xA002),
    EXIF_EXIF_IMAGE_HEIGHT(MetadataGroup.EXIF, "Exif Image Height", 0xA003),
    EXIF_RELATED_SOUND_FILE(MetadataGroup.EXIF, "Related Sound File", 0xA004),
    EXIF_FLASH_ENERGY(MetadataGroup.EXIF, "Flash Energy", 0xA20B),
    EXIF_SPATIAL_FREQ_RESPONSE(MetadataGroup.EXIF, "Spatial Frequency Response", 0xA20C),
    EXIF_FOCAL_PLANE_X_RESOLUTION(MetadataGroup.EXIF, "Focal Plane X Resolution", 0xA20E),
    EXIF_FOCAL_PLANE_Y_RESOLUTION(MetadataGroup.EXIF, "Focal Plane Y Resolution", 0xA20F),
    EXIF_FOCAL_PLANE_RESOLUTION_UNIT(MetadataGroup.EXIF, "Focal Plane Resolution Unit", 0xA210),
    EXIF_SUBJECT_LOCATION(MetadataGroup.EXIF, "Subject Location", 0xA214),
    EXIF_EXPOSURE_INDEX(MetadataGroup.EXIF, "Exposure Index", 0xA215),
    EXIF_SENSING_METHOD(MetadataGroup.EXIF, "Sensing Method", 0xA217),
    EXIF_FILE_SOURCE(MetadataGroup.EXIF, "File Source", 0xA300),
    EXIF_SCENE_TYPE(MetadataGroup.EXIF, "Scene Type", 0xA301),
    EXIF_CFA_PATTERN(MetadataGroup.EXIF, "CFA Pattern", 0xA302),
    EXIF_CUSTOM_RENDERED(MetadataGroup.EXIF, "Custom Rendered", 0xA401),
    EXIF_EXPOSURE_MODE(MetadataGroup.EXIF, "Exposure Mode", 0xA402),
    EXIF_WHITE_BALANCE_MODE(MetadataGroup.EXIF, "White Balance Mode", 0xA403),
    EXIF_DIGITAL_ZOOM_RATIO(MetadataGroup.EXIF, "Digital Zoom Ratio", 0xA404),
    EXIF_35MM_FILM_EQUIV_FOCAL_LENGTH(MetadataGroup.EXIF, "Focal Length 35", 0xA405),
    EXIF_SCENE_CAPTURE_TYPE(MetadataGroup.EXIF, "Scene Capture Type", 0xA406),
    EXIF_GAIN_CONTROL(MetadataGroup.EXIF, "Gain Control", 0xA407),
    EXIF_CONTRAST(MetadataGroup.EXIF, "Contrast", 0xA408),
    EXIF_SATURATION(MetadataGroup.EXIF, "Saturation", 0xA409),
    EXIF_SHARPNESS(MetadataGroup.EXIF, "Sharpness", 0xA40A),
    EXIF_DEVICE_SETTING_DESCRIPTION(MetadataGroup.EXIF, "Device Setting Description", 0xA40B),
    EXIF_SUBJECT_DISTANCE_RANGE(MetadataGroup.EXIF, "Subject Distance Range", 0xA40C),
    EXIF_IMAGE_UNIQUE_ID(MetadataGroup.EXIF, "Unique Image ID", 0xA420),
    EXIF_CAMERA_OWNER_NAME(MetadataGroup.EXIF, "Camera Owner Name", 0xA430),
    EXIF_BODY_SERIAL_NUMBER(MetadataGroup.EXIF, "Body Serial Number", 0xA431),
    EXIF_LENS_SPECIFICATION(MetadataGroup.EXIF, "Lens Specification", 0xA432),
    EXIF_LENS_MAKE(MetadataGroup.EXIF, "Lens Make", 0xA433),
    EXIF_LENS_MODEL(MetadataGroup.EXIF, "Lens Model", 0xA434),
    EXIF_LENS_SERIAL_NUMBER(MetadataGroup.EXIF, "Lens Serial Number", 0xA435),
    EXIF_GAMMA(MetadataGroup.EXIF, "Gamma", 0xA500),
    EXIF_PRINT_IMAGE_MATCHING_INFO(MetadataGroup.EXIF, "Print Image Matching (PIM) Info", 0xC4A5),
    EXIF_PANASONIC_TITLE(MetadataGroup.EXIF, "Panasonic Title", 0xC6D2),
    EXIF_PANASONIC_TITLE_2(MetadataGroup.EXIF, "Panasonic Title (2)", 0xC6D3),
    EXIF_PADDING(MetadataGroup.EXIF, "Padding", 0xEA1C),
    EXIF_LENS(MetadataGroup.EXIF, "Lens", 0xFDEA),

    // Group FILE
    FILE_NAME(MetadataGroup.FILE, "File Name", 0x0001),
    FILE_SIZE(MetadataGroup.FILE, "File Size", 0x0002),
    FILE_MODIFIED_DATE(MetadataGroup.FILE, "File Modified Date", 0x0003),

    // Group GPS
    GPS_VERSION_ID(MetadataGroup.GPS, "GPS Version ID", 0x0000),
    GPS_LATITUDE_REF(MetadataGroup.GPS, "GPS Latitude Ref", 0x0001),
    GPS_LATITUDE(MetadataGroup.GPS, "GPS Latitude", 0x0002),
    GPS_LONGITUDE_REF(MetadataGroup.GPS, "GPS Longitude Ref", 0x0003),
    GPS_LONGITUDE(MetadataGroup.GPS, "GPS Longitude", 0x0004),
    GPS_ALTITUDE_REF(MetadataGroup.GPS, "GPS Altitude Ref", 0x0005),
    GPS_ALTITUDE(MetadataGroup.GPS, "GPS Altitude", 0x0006),
    GPS_TIME_STAMP(MetadataGroup.GPS, "GPS Time-Stamp", 0x0007),
    GPS_SATELLITES(MetadataGroup.GPS, "GPS Satellites", 0x0008),
    GPS_STATUS(MetadataGroup.GPS, "GPS Status", 0x0009),
    GPS_MEASURE_MODE(MetadataGroup.GPS, "GPS Measure Mode", 0x000A),
    GPS_DOP(MetadataGroup.GPS, "GPS DOP", 0x000B),
    GPS_SPEED_REF(MetadataGroup.GPS, "GPS Speed Ref", 0x000C),
    GPS_SPEED(MetadataGroup.GPS, "GPS Speed", 0x000D),
    GPS_TRACK_REF(MetadataGroup.GPS, "GPS Track Ref", 0x000E),
    GPS_TRACK(MetadataGroup.GPS, "GPS Track", 0x000F),
    GPS_IMG_DIRECTION_REF(MetadataGroup.GPS, "GPS Img Direction Ref", 0x0010),
    GPS_IMG_DIRECTION(MetadataGroup.GPS, "GPS Img Direction", 0x0011),
    GPS_MAP_DATUM(MetadataGroup.GPS, "GPS Map Datum", 0x0012),
    GPS_DEST_LATITUDE_REF(MetadataGroup.GPS, "GPS Dest Latitude Ref", 0x0013),
    GPS_DEST_LATITUDE(MetadataGroup.GPS, "GPS Dest Latitude", 0x0014 ),
    GPS_DEST_LONGITUDE_REF(MetadataGroup.GPS, "GPS Dest Longitude Ref", 0x0015),
    GPS_DEST_LONGITUDE(MetadataGroup.GPS, "GPS Dest Longitude", 0x0016),
    GPS_DEST_BEARING_REF(MetadataGroup.GPS, "GPS Dest Bearing Ref", 0x0017),
    GPS_DEST_BEARING(MetadataGroup.GPS, "GPS Dest Bearing", 0x0018),
    GPS_DEST_DISTANCE_REF(MetadataGroup.GPS, "GPS Dest Distance Ref", 0x0019),
    GPS_DEST_DISTANCE(MetadataGroup.GPS, "GPS Dest Distance", 0x001A),
    GPS_PROCESSING_METHOD(MetadataGroup.GPS, "GPS Processing Method", 0x001B),
    GPS_AREA_INFORMATION(MetadataGroup.GPS, "GPS Area Information", 0x001C),
    GPS_DATE_STAMP(MetadataGroup.GPS, "GPS Date Stamp", 0x001D),
    GPS_DIFFERENTIAL(MetadataGroup.GPS, "GPS Differential", 0x001E),

    // Group Header
    HEADER_PIXEL_WIDTH(MetadataGroup.HEADER, "Header Pixel Width", 0x9001),
    HEADER_PIXEL_HEIGHT(MetadataGroup.HEADER, "Header Pixel Height", 0x9002),
    HEADER_FORMAT_NAME(MetadataGroup.HEADER, "Header Format Name", 0x9010),

    // Group Thumbnail
    THUMBNAIL_OFFSET(MetadataGroup.THUMBNAIL, "Thumbnail Offset", 0x0201),
    THUMBNAIL_LENGTH(MetadataGroup.THUMBNAIL,  "Thumbnail Length", 0x0202);

    /**
     * Initializes a new {@link Key}.
     * @param group
     * @param name
     * @param id
     */
    private MetadataKey(@NonNull final MetadataGroup metadataGroup, @NonNull final String name, final int id) {
        m_dataGroup = metadataGroup;
        m_name = name;
        m_id = id;
    }

    // - public API -----------------------------------------------------------

    /**
     * @return
     */
    public MetadataGroup getDataGroup() {
        return m_dataGroup;
    }

    /**
     * @return
     */
    public String getName() {
        return m_name;
    }

    /**
     * @return
     */
    public int getId() {
        return m_id;
    }

    // - Members -----------------------------------------------------------

    private final MetadataGroup m_dataGroup;

    private final String m_name ;

    private final int m_id;
}
