package com.openexchange.ajax.framework;

import java.util.Date;
import java.util.TimeZone;

public class Parameter {
    private final String name;
    private final String value;
    public Parameter(final String name, final String value) {
        this.name = name;
        this.value = value;
    }
    public Parameter(final String name, final String[] values) {
        this(name, convert(values));
    }
    public Parameter(final String name, final int[] values) {
        this(name, convert(values));
    }
    public Parameter(final String name, final int identifier) {
        this(name, String.valueOf(identifier));
    }
    public Parameter(final String name, final long time) {
        this(name, String.valueOf(time));
    }
    public Parameter(final String name, final Date time) {
        this(name, time.getTime());
    }
    public Parameter(final String name, final Date time, final TimeZone tz) {
        this(name, time.getTime() + tz.getOffset(time.getTime()));
    }
    public Parameter(final String name, final boolean schalter) {
        this(name, String.valueOf(schalter));
    }
    public static String convert(final int[] values) {
        final StringBuilder columnSB = new StringBuilder();
        for (final int i : values) {
            columnSB.append(i);
            columnSB.append(',');
        }
        columnSB.delete(columnSB.length() - 1, columnSB.length());
        return columnSB.toString();
    }
    public static String convert(final String[] values) {
        final StringBuilder columnSB = new StringBuilder();
        for (final String i : values) {
            columnSB.append(i);
            columnSB.append(',');
        }
        columnSB.delete(columnSB.length() - 1, columnSB.length());
        return columnSB.toString();
    }
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }
    @Override
    public String toString() {
        return name + "=" + value;
    }
}
