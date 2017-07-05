
package com.openexchange.chronos.storage.rdb;

import java.util.EnumMap;
import com.openexchange.chronos.CalendarFreeSlot;
import com.openexchange.chronos.service.AvailabilityField;
import com.openexchange.groupware.tools.mappings.database.DbMapping;
import com.openexchange.groupware.tools.mappings.database.DefaultDbMapper;

/**
 * {@link FreeSlotMapper}
 * 
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class FreeSlotMapper extends DefaultDbMapper<CalendarFreeSlot, AvailabilityField> {

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
    public AvailabilityField[] newArray(int size) {
        return new AvailabilityField[size];
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.groupware.tools.mappings.database.DefaultDbMapper#createMappings()
     */
    @Override
    protected EnumMap<AvailabilityField, ? extends DbMapping<? extends Object, CalendarFreeSlot>> createMappings() {
        EnumMap<AvailabilityField, DbMapping<? extends Object, CalendarFreeSlot>> mappings = new EnumMap<AvailabilityField, DbMapping<? extends Object, CalendarFreeSlot>>(AvailabilityField.class);
        //TODO: implement the mppers
        return mappings;
    }

}
