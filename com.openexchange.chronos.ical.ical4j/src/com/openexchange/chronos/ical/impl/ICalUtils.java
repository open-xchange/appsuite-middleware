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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.chronos.ical.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.CalendarParser;
import net.fortuna.ical4j.data.CalendarParserFactory;
import net.fortuna.ical4j.data.FoldingWriter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.extensions.caldav.property.Acknowledged;
import net.fortuna.ical4j.extensions.property.WrCalName;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ParameterFactoryRegistry;
import net.fortuna.ical4j.model.PropertyFactoryImpl;
import net.fortuna.ical4j.model.PropertyFactoryRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;

import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;

/**
 * {@link ICalUtils}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ICalUtils {

	static final PropertyFactoryRegistry PROPERTY_FACTORY = initPropertyFactory();
	static final ParameterFactoryRegistry PARAMETER_FACTORY = initParameterFactory();
	
    /**
     * Gets the iCal parameters, or the default parameters if passed instance is <code>null</code>.
     *
     * @param parameters The parameters as passed from the client
     * @return The parameters, or the default parameters if passed instance is <code>null</code>
     */
    static ICalParameters getParametersOrDefault(ICalParameters parameters) {
        return null != parameters ? parameters : new ICalParametersImpl();
    }
    
    static Calendar importCalendar(InputStream iCalFile, ICalParameters parameters) throws OXException {
        try {
        	return getCalendarBuilder(getParametersOrDefault(parameters)).build(iCalFile);
        } catch (IOException | ParserException e) {
        	throw new OXException(e);
        }
    }
    
    static ThresholdFileHolder exportCalendar(Calendar calendar, ICalParameters parameters) throws OXException {
        ThresholdFileHolder fileHolder = new ThresholdFileHolder();
        CalendarOutputter outputter = new CalendarOutputter(false);
    	try {
			outputter.output(calendar, fileHolder.asOutputStream());
		} catch (IOException | ValidationException e) {
			throw new OXException(e);
		}
        return fileHolder;
    }
    
    static ThresholdFileHolder exportComponent(Component component, ICalParameters parameters) throws OXException {
        ThresholdFileHolder fileHolder = new ThresholdFileHolder();
        FoldingWriter writer = null;
    	try {
    		writer = new FoldingWriter(new OutputStreamWriter(fileHolder.asOutputStream(), Charsets.UTF_8), FoldingWriter.MAX_FOLD_LENGTH);
    		writer.write(component.toString());
		} catch (IOException e) {
			throw new OXException(e);
		} finally {
			Streams.close(writer);
		}
        return fileHolder;
    }

//    static <T extends CalendarComponent> T parseComponent(Class<T> clazz, IFileHolder fileHolder, ICalParameters parameters, List<OXException> warnings) throws OXException {
//    	try (InputStream inputStream = fileHolder.getStream()) {
//    		return parseComponent(clazz, inputStream, parameters, warnings);
//    	} catch (IOException e) {
//            throw new OXException(e);
//		}
//    }

    static CalendarBuilder getCalendarBuilder(ICalParameters parameters) {
    	ICalParameters iCalParameters = getParametersOrDefault(parameters);
    	CalendarParser calendarParser = CalendarParserFactory.getInstance().createParser();
    	TimeZoneRegistry timeZoneRegistry = iCalParameters.get(ICalParameters.TIMEZONE_REGISTRY, TimeZoneRegistry.class);
    	if (null == timeZoneRegistry) {
    		timeZoneRegistry = TimeZoneRegistryFactory.getInstance().createRegistry();
    		iCalParameters.set(ICalParameters.TIMEZONE_REGISTRY, timeZoneRegistry);
    	}
    	return new CalendarBuilder(calendarParser, PROPERTY_FACTORY, PARAMETER_FACTORY, timeZoneRegistry);
    }
    
    private static final byte[] VEVENT_PROLOGUE = 
    		"BEGIN:VCALENDAR\r\n".getBytes(Charsets.UTF_8);	
    private static final byte[] VEVENT_EPILOGUE = 
    		"END:VCALENDAR\r\n".getBytes(Charsets.UTF_8);	
    private static final byte[] VALARM_PROLOGUE = 
    		"BEGIN:VCALENDAR\r\nBEGIN:VEVENT\r\n".getBytes(Charsets.UTF_8);	
    private static final byte[] VALARM_EPILOGUE = 
    		"END:VEVENT\r\nEND:VCALENDAR\r\n".getBytes(Charsets.UTF_8);	
    
//    static <T extends CalendarComponent> T parseComponent(Class<T> clazz, InputStream inputStream, ICalParameters parameters, List<OXException> warnings) throws OXException {
//        Calendar calendar = null;
//        CalendarBuilder calendarBuilder = getCalendarBuilder(parameters);
//        try {
//        	calendar = calendarBuilder.build(inputStream);
//        } catch (IOException e) {
//            throw new OXException(e);
//        } catch (ParserException e) {
//            throw new OXException(e);
//		}
//        ComponentList components = calendar.getComponents();
//        for (Iterator<?> iterator = components.iterator(); iterator.hasNext();) {
//            Component component = (Component) iterator.next();
//            if (clazz.isInstance(component)) {
//            	return clazz.cast(component);
//            }
//        }
//        return null;
//    }
    
    static VEvent parseVEventComponent(IFileHolder fileHolder, ICalParameters parameters, List<OXException> warnings) throws OXException {
    	try (InputStream inputStream = fileHolder.getStream()) {
    		return parseVEventComponent(inputStream, parameters, warnings);
    	} catch (IOException e) {
            throw new OXException(e);
		}
    }
    static VEvent parseVEventComponent(InputStream inputStream, ICalParameters parameters, List<OXException> warnings) throws OXException {
        Enumeration<InputStream> streamSequence = Collections.enumeration(Arrays.asList(
        		Streams.newByteArrayInputStream(VEVENT_PROLOGUE), inputStream, Streams.newByteArrayInputStream(VEVENT_EPILOGUE)));
        SequenceInputStream sequenceStream = null;
        Calendar calendar = null;
        CalendarBuilder calendarBuilder = getCalendarBuilder(parameters);
        try {
        	sequenceStream = new SequenceInputStream(streamSequence);
        	calendar = calendarBuilder.build(sequenceStream);
        } catch (IOException e) {
            throw new OXException(e);
        } catch (ParserException e) {
            throw new OXException(e);
		} finally {
			Streams.close(sequenceStream);
		}
        return (VEvent) calendar.getComponent(Component.VEVENT);
    }
    
    static VAlarm parseVAlarmComponent(IFileHolder fileHolder, ICalParameters parameters, List<OXException> warnings) throws OXException {
    	try (InputStream inputStream = fileHolder.getStream()) {
    		return parseVAlarmComponent(inputStream, parameters, warnings);
    	} catch (IOException e) {
            throw new OXException(e);
		}
    }
    static VAlarm parseVAlarmComponent(InputStream inputStream, ICalParameters parameters, List<OXException> warnings) throws OXException {
        Enumeration<InputStream> streamSequence = Collections.enumeration(Arrays.asList(
        		Streams.newByteArrayInputStream(VALARM_PROLOGUE), inputStream, Streams.newByteArrayInputStream(VALARM_EPILOGUE)));
        SequenceInputStream sequenceStream = null;
        Calendar calendar = null;
        CalendarBuilder calendarBuilder = getCalendarBuilder(parameters);
        try {
        	sequenceStream = new SequenceInputStream(streamSequence);
        	calendar = calendarBuilder.build(sequenceStream);
        } catch (IOException e) {
            throw new OXException(e);
        } catch (ParserException e) {
            throw new OXException(e);
		} finally {
			Streams.close(sequenceStream);
		}
        return (VAlarm) calendar.getComponent(Component.VALARM);
    }
    
	private static PropertyFactoryRegistry initPropertyFactory() {
		PropertyFactoryRegistry factory = new PropertyFactoryRegistry();
        factory.register(Acknowledged.PROPERTY_NAME, PropertyFactoryImpl.getInstance());
        factory.register(WrCalName.PROPERTY_NAME, WrCalName.FACTORY);
		return factory;
	}

	private static ParameterFactoryRegistry initParameterFactory() {
		ParameterFactoryRegistry factory = new ParameterFactoryRegistry();
		return factory;
	}

//
//
//    static void applyParameters(ICalWriter writer, ICalParameters parameters) {
//        if (null != parameters) {
//            TimezoneInfo tzInfo = parameters.get(ICalParameters.TIMEZONE_INFO, TimezoneInfo.class);
//            if (null != tzInfo) {
//                writer.setTimezoneInfo(tzInfo);
//            }
//        }
//    }
    
    static List<OXException> getParserWarnings(List<String> parserWarnings) {
    	if (null == parserWarnings || 0 == parserWarnings.size()) {
    		return Collections.emptyList();
    	}
    	List<OXException> warnings = new ArrayList<OXException>();
    	for (String parserWarning : parserWarnings) {
    		warnings.add(OXException.general(parserWarning));			
		}
    	return warnings;
    }

}