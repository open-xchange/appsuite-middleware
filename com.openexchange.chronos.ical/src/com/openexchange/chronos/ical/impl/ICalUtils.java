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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import biweekly.Biweekly;
import biweekly.ICalVersion;
import biweekly.ICalendar;
import biweekly.component.ICalComponent;
import biweekly.io.TimezoneInfo;
import biweekly.io.text.ICalWriter;

import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;

/**
 * {@link ICalUtils}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ICalUtils {

    static ThresholdFileHolder exportComponent(ICalComponent component, ICalParameters parameters) throws OXException {
        ICalendar iCalendar = new ICalendar();
        iCalendar.addComponent(component);
        return exportICalendar(iCalendar, parameters);
    }

    static <T extends ICalComponent> T parseComponent(Class<T> clazz, IFileHolder fileHolder, List<OXException> warnings) throws OXException {
    	try (InputStream inputStream = fileHolder.getStream()) {
    		return parseComponent(clazz, inputStream, warnings);
    	} catch (IOException e) {
            throw new OXException(e);
		}
    }

    static <T extends ICalComponent> T parseComponent(Class<T> clazz, InputStream inputStream, List<OXException> warnings) throws OXException {
        ICalendar iCalendar = null;
        try {
        	List<List<String>> parserWarnings = new ArrayList<List<String>>();
            iCalendar = Biweekly.parse(inputStream).warnings(parserWarnings).first();
            if (0 < parserWarnings.size()) {
            	warnings.addAll(getParserWarnings(parserWarnings.get(0)));
            }
        } catch (IOException e) {
            throw new OXException(e);
        }
        return null != iCalendar ? iCalendar.getComponent(clazz) : null;
    }

    static ThresholdFileHolder exportICalendar(ICalendar iCalendar, ICalParameters parameters) throws OXException {
        ThresholdFileHolder fileHolder = new ThresholdFileHolder();
        ICalWriter iCalWriter = null;
        try {
            iCalWriter = new ICalWriter(fileHolder.asOutputStream(), ICalVersion.V2_0);
            applyParameters(iCalWriter, parameters);
            iCalWriter.write(iCalendar);
            iCalWriter.flush();
        } catch (IOException e) {
            throw new OXException(e);
        } finally {
            Streams.close(iCalWriter);
        }
        return fileHolder;
    }

    /**
     * Gets the iCal parameters, or the default parameters if passed instance is <code>null</code>.
     *
     * @param parameters The parameters as passed from the client
     * @return The parameters, or the default parameters if passed instance is <code>null</code>
     */
    static ICalParameters getParametersOrDefault(ICalParameters parameters) {
        return null != parameters ? parameters : new ICalParametersImpl();
    }

    static void applyParameters(ICalWriter writer, ICalParameters parameters) {
        if (null != parameters) {
            TimezoneInfo tzInfo = parameters.get(ICalParameters.TIMEZONE_INFO, TimezoneInfo.class);
            if (null != tzInfo) {
                writer.setTimezoneInfo(tzInfo);
            }
        }
    }
    
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