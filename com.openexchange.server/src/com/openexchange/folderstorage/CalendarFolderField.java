///*
// * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
// * @license AGPL-3.0
// *
// * This code is free software: you can redistribute it and/or modify
// * it under the terms of the GNU Affero General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU Affero General Public License for more details.
// *
// * You should have received a copy of the GNU Affero General Public License
// * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
// *
// * Any use of the work other than as authorized under this license or copyright law is prohibited.
// *
// */
//
//package com.openexchange.folderstorage;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.Map;
//import java.util.Map.Entry;
//import com.openexchange.chronos.Transp;
//import com.openexchange.chronos.common.DataHandlers;
//import com.openexchange.conversion.ConversionResult;
//import com.openexchange.conversion.ConversionService;
//import com.openexchange.conversion.DataArguments;
//import com.openexchange.conversion.DataHandler;
//import com.openexchange.conversion.SimpleData;
//import com.openexchange.server.services.ServerServiceRegistry;
//
///**
// * {@link CalendarFolderField}
// *
// * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
// * @since v7.10.0
// */
//public enum CalendarFolderField {
//    ;
//
//    public static final FolderField EXTENDED_PROPERTIES = new FolderField(3201, "cal.xprops", null) {
//
//        /**
//         * serialVersionUID
//         */
//        private static final long serialVersionUID = -6859701869502421711L;
//
//        /**
//         * Deserializes a folder property from its serialized representation.
//         *
//         * @param value The value to parse
//         * @return The parsed folder property
//         */
//        @Override
//        public FolderProperty parse(Object value) {
//
//            try {
//                DataHandler dataHandler = ServerServiceRegistry.getServize(ConversionService.class).getDataHandler(DataHandlers.JSON2XPROPERTIES);
//                ConversionResult result = dataHandler.processData(new SimpleData<Object>(value), new DataArguments(), null);
//                if (null != result && null != result.getData()) {
//                    return new FolderProperty(getName(), result.getData());
//                }
//            } catch (Exception e) {
//                //                LOG.warn("Error converting extended properties \"{}\": {}", folderProperty.getValue(), e.getMessage(), e);
//            }
//            return null;
//        }
//
//        /**
//         * Serializes a folder property.
//         *
//         * @param property The folder property to write
//         * @return The serialized value
//         */
//        @Override
//        public Object write(FolderProperty property) {
//            try {
//                DataHandler dataHandler = ServerServiceRegistry.getServize(ConversionService.class).getDataHandler(DataHandlers.XPROPERTIES2JSON);
//                ConversionResult result = dataHandler.processData(new SimpleData<Object>(property.getValue()), new DataArguments(), null);
//                if (null != result && null != result.getData()) {
//                    return result.getData();
//                }
//            } catch (Exception e) {
//                //
//                //                LOG.warn("Error converting extended properties \"{}\": {}", extendedProperties, e.getMessage(), e);
//            }
//            return null;
//        }
//
//    };
//
//    /**
//     * {@link String}
//     * <p/>
//     * Specifies the color of the calendar (as a <code>CSS3</code> color value).
//     */
//    public static final FolderField COLOR = new FolderField(3201, "cal.color", null);
//
//    /**
//     * {@link Boolean}
//     * <p/>
//     * Indicates whether a calendar should be considered for synchronization with external clients or not.
//     */
//    public static final FolderField USED_FOR_SYNC = new FolderField(3202, "cal.usedForSync", Boolean.TRUE);
//
//    /**
//     * {@link String}
//     * <p/>
//     * Determines whether the calendar object resources in a calendar collection will affect the owner's busy time information.
//     *
//     * @see <a href="https://tools.ietf.org/html/rfc6638#section-9.1">RFC 6638, section 9.1</a>
//     */
//    public static final FolderField SCHEDULE_TRANSP = new FolderField(3203, "cal.scheduleTransp", Transp.OPAQUE);
//
//    /**
//     * {@link String}
//     * <p/>
//     * Specifies the description of the calendar folder.
//     *
//     * @see <a href="https://tools.ietf.org/html/rfc7986#section-5.2">RFC 7986, section 5.2</a>
//     */
//    public static final FolderField DESCRIPTION = new FolderField(3204, "cal.scheduleTransp", null);
//
//    /**
//     * Gets all known calendar folder fields.
//     *
//     * @return A list of all calendar folder fields
//     */
//    public static List<FolderField> getValues() {
//        //        return Arrays.asList(new FolderField[] { COLOR, USED_FOR_SYNC, SCHEDULE_TRANSP, DESCRIPTION, EXTENDED_PROPERTIES });
//        return Arrays.asList(new FolderField[] { EXTENDED_PROPERTIES });
//    }
//
//    /**
//     * Optionally gets a folder field value from a folder properties collection.
//     *
//     * @param properties The folder properties
//     * @param field The field to get the value for
//     * @param clazz The value's target type
//     * @return The parameter value, or <code>null</code> if not set
//     */
//    public static <T> T optValue(Map<FolderField, FolderProperty> properties, FolderField field, Class<T> clazz) {
//        return optValue(properties, field, clazz, null);
//    }
//
//    /**
//     * Optionally gets a folder field value from a folder properties collection.
//     *
//     * @param properties The folder properties
//     * @param field The field to get the value for
//     * @param clazz The value's target type
//     * @param defaultValue The default value to use as fallback if the parameter is not set
//     * @return The parameter value, or the passed default value if not set
//     */
//    public static <T> T optValue(Map<FolderField, FolderProperty> properties, FolderField field, Class<T> clazz, T defaultValue) {
//        if (null != properties && 0 < properties.size()) {
//            FolderProperty property = properties.get(field);
//            if (null != property && null != property.getValue() && clazz.isInstance(property.getValue())) {
//                return clazz.cast(property.getValue());
//            }
//        }
//        return defaultValue;
//    }
//
//    /**
//     * Applies a collection of folder properties for a specific folder.
//     *
//     * @param properties The properties to set
//     * @param folder The folder to set the properties for
//     */
//    public static void setProperties(Map<FolderField, FolderProperty> properties, ParameterizedFolder folder) {
//        if (null != properties && 0 < properties.size()) {
//            for (Entry<FolderField, FolderProperty> entry : properties.entrySet()) {
//                folder.setProperty(entry.getKey(), null != entry.getValue() ? entry.getValue().getValue() : null);
//            }
//        }
//    }
//
//}
