
package com.openexchange.chronos.storage.rdb;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import com.openexchange.chronos.CalendarFreeSlot;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.service.FreeSlotField;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.database.BigIntMapping;
import com.openexchange.groupware.tools.mappings.database.DateMapping;
import com.openexchange.groupware.tools.mappings.database.DbMapping;
import com.openexchange.groupware.tools.mappings.database.DefaultDbMapper;
import com.openexchange.groupware.tools.mappings.database.DefaultDbMapping;
import com.openexchange.groupware.tools.mappings.database.IntegerMapping;
import com.openexchange.groupware.tools.mappings.database.VarCharMapping;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;

/**
 * {@link FreeSlotMapper}
 * 
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class FreeSlotMapper extends DefaultDbMapper<CalendarFreeSlot, FreeSlotField> {

    private static final FreeSlotMapper INSTANCE = new FreeSlotMapper();

    /**
     * Gets the mapper instance
     * 
     * @return The mapper instance
     */
    public static FreeSlotMapper getInstance() {
        return INSTANCE;
    }

    /**
     * Initialises a new {@link FreeSlotMapper}
     */
    public FreeSlotMapper() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.groupware.tools.mappings.Factory#newInstance()
     */
    @Override
    public CalendarFreeSlot newInstance() {
        return new CalendarFreeSlot();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.groupware.tools.mappings.ArrayFactory#newArray(int)
     */
    @Override
    public FreeSlotField[] newArray(int size) {
        return new FreeSlotField[size];
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.groupware.tools.mappings.database.DefaultDbMapper#createMappings()
     */
    @Override
    protected EnumMap<FreeSlotField, ? extends DbMapping<? extends Object, CalendarFreeSlot>> createMappings() {
        EnumMap<FreeSlotField, DbMapping<? extends Object, CalendarFreeSlot>> mappings = new EnumMap<FreeSlotField, DbMapping<? extends Object, CalendarFreeSlot>>(FreeSlotField.class);
        mappings.put(FreeSlotField.uid, new IntegerMapping<CalendarFreeSlot>("id", "Availability ID") {

            @Override
            public boolean isSet(CalendarFreeSlot object) {
                return object.contains(FreeSlotField.uid);
            }

            @Override
            public void set(CalendarFreeSlot object, Integer value) throws OXException {
                object.setUid(Integer.toString(value));
            }

            @Override
            public Integer get(CalendarFreeSlot object) {
                return Integer.valueOf(object.getUid());
            }

            @Override
            public void remove(CalendarFreeSlot object) {
                object.removeUid();
            }

        });
        mappings.put(FreeSlotField.dtstart, new DateMapping<CalendarFreeSlot>("start", "Start DateTime") {

            @Override
            public boolean isSet(CalendarFreeSlot object) {
                return object.contains(FreeSlotField.dtstart);
            }

            @Override
            public void set(CalendarFreeSlot object, Date value) throws OXException {
                object.setStartTime(value);
            }

            @Override
            public Date get(CalendarFreeSlot object) {
                return object.getStartTime();
            }

            @Override
            public void remove(CalendarFreeSlot object) {
                object.removeStartTime();
            }

        });
        mappings.put(FreeSlotField.dtend, new DateMapping<CalendarFreeSlot>("end", "End DateTime") {

            @Override
            public boolean isSet(CalendarFreeSlot object) {
                return object.contains(FreeSlotField.dtend);
            }

            @Override
            public void set(CalendarFreeSlot object, Date value) throws OXException {
                object.setEndTime(value);
            }

            @Override
            public Date get(CalendarFreeSlot object) {
                return object.getEndTime();
            }

            @Override
            public void remove(CalendarFreeSlot object) {
                object.removeEndTime();
            }
        });
        mappings.put(FreeSlotField.created, new BigIntMapping<CalendarFreeSlot>("created", "Created") {

            @Override
            public boolean isSet(CalendarFreeSlot object) {
                return object.contains(FreeSlotField.created);
            }

            @Override
            public void set(CalendarFreeSlot object, Long value) throws OXException {
                object.setCreated(value == null ? null : new Date(value.longValue()));
            }

            @Override
            public Long get(CalendarFreeSlot object) {
                Date created = object.getCreated();
                return created == null ? null : created.getTime();
            }

            @Override
            public void remove(CalendarFreeSlot object) {
                object.removeCreated();
            }
        });
        mappings.put(FreeSlotField.lastModified, new BigIntMapping<CalendarFreeSlot>("modified", "Last Modified") {

            @Override
            public boolean isSet(CalendarFreeSlot object) {
                return object.contains(FreeSlotField.lastModified);
            }

            @Override
            public void set(CalendarFreeSlot object, Long value) throws OXException {
                object.setLastModified(value == null ? null : new Date(value));
            }

            @Override
            public Long get(CalendarFreeSlot object) {
                return object.getLastModified().getTime();
            }

            @Override
            public void remove(CalendarFreeSlot object) {
                object.removeLastModified();
            }
        });
        mappings.put(FreeSlotField.description, new VarCharMapping<CalendarFreeSlot>("description", "Description") {

            @Override
            public boolean isSet(CalendarFreeSlot object) {
                return object.contains(FreeSlotField.description);
            }

            @Override
            public void set(CalendarFreeSlot object, String value) throws OXException {
                object.setDescription(value);
                ;
            }

            @Override
            public String get(CalendarFreeSlot object) {
                return object.getDescription();
            }

            @Override
            public void remove(CalendarFreeSlot object) {
                object.removeDescription();
            }
        });
        mappings.put(FreeSlotField.summary, new VarCharMapping<CalendarFreeSlot>("summary", "Summary") {

            @Override
            public boolean isSet(CalendarFreeSlot object) {
                return object.contains(FreeSlotField.summary);
            }

            @Override
            public void set(CalendarFreeSlot object, String value) throws OXException {
                object.setSummary(value);
            }

            @Override
            public String get(CalendarFreeSlot object) {
                return object.getSummary();
            }

            @Override
            public void remove(CalendarFreeSlot object) {
                object.removeSummary();
            }
        });
        mappings.put(FreeSlotField.categories, new VarCharMapping<CalendarFreeSlot>("categories", "Categories") {

            @Override
            public boolean isSet(CalendarFreeSlot object) {
                return object.contains(FreeSlotField.categories);
            }

            @Override
            public void set(CalendarFreeSlot object, String value) throws OXException {
                String[] split = Strings.splitByCommaNotInQuotes(value);
                object.setCategories(split == null ? null : Arrays.asList(split));
            }

            @Override
            public String get(CalendarFreeSlot object) {
                List<String> categories = object.getCategories();
                if (categories == null || categories.size() == 0) {
                    return null;
                }

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(categories.get(0));
                for (String category : categories) {
                    stringBuilder.append(category);
                }
                stringBuilder.setLength(stringBuilder.length() - 1);

                return stringBuilder.toString();
            }

            @Override
            public void remove(CalendarFreeSlot object) {
                object.removeCategories();
            }
        });
        mappings.put(FreeSlotField.extendedProperties, new DefaultDbMapping<ExtendedProperties, CalendarFreeSlot>("extendedProperties", "Extended Properties", Types.BLOB) {

            @Override
            public int set(PreparedStatement statement, int parameterIndex, CalendarFreeSlot object) throws SQLException {
                ExtendedProperties value = get(object);
                if (null == value) {
                    statement.setNull(parameterIndex, getSqlType());
                } else {
                    try {
                        byte[] data = ExtendedPropertiesCodec.encode(value);
                        statement.setBinaryStream(parameterIndex, Streams.newByteArrayInputStream(data), data.length);
                    } catch (IOException e) {
                        throw new SQLException(e);
                    }
                }
                return 1;
            }

            @Override
            public ExtendedProperties get(ResultSet resultSet, String columnLabel) throws SQLException {
                InputStream inputStream = null;
                try {
                    inputStream = resultSet.getBinaryStream(columnLabel);
                    return ExtendedPropertiesCodec.decode(inputStream);
                } catch (IOException e) {
                    throw new SQLException(e);
                } finally {
                    Streams.close(inputStream);
                }
            }

            @Override
            public boolean isSet(CalendarFreeSlot object) {
                return object.contains(FreeSlotField.extendedProperties);
            }

            @Override
            public void set(CalendarFreeSlot object, ExtendedProperties value) throws OXException {
                object.setExtendedProperties(value);
            }

            @Override
            public ExtendedProperties get(CalendarFreeSlot object) {
                return object.getExtendedProperties();
            }

            @Override
            public void remove(CalendarFreeSlot object) {
                object.removeExtendedProperties();
            }

        });
        return mappings;
    }

}
