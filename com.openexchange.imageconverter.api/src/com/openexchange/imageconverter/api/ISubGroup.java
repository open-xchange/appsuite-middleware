package com.openexchange.imageconverter.api;

import java.util.Date;

public interface ISubGroup {

    /**
     * Gets the SubGroup id.
     *
     * @return The subGroupId.
     */
    public String getSubGroupId();

    /**
     * Retrieving the earliest create {@link Date} of the SubGroup.
     *
     * @return The earliest create {@link Date} of the SubGroup as Gregorian calendar date.
     *
     */
    public Date getCreateDate();

    /**
     * Retrieving the latest access {@link Date} of the SubGroup.
     *
     * @return The latest access {@link Date} of the SubGroup as Gregorian calendar date.
     *
     */
    public Date getModificationDate();

    /**
     * Retrieving the summed up length of the SubGroup.
     *
     * @return The summed up length of all {@link IFileItem} lengths within the SubGroup.
     */
    public long getLength();
}
