package com.openexchange.data.conversion.ical.ical4j.internal;

import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ConversionError;

import java.util.List;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public interface ObjectVerifier<T> {

    public void verify(int index, T object, List<ConversionWarning> warnings) throws ConversionError;
}
