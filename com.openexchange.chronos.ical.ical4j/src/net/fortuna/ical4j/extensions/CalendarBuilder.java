/**
 * Copyright (c) 2012, Ben Fortuna
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  o Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 *  o Neither the name of Ben Fortuna nor the names of any other contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.fortuna.ical4j.extensions;

import net.fortuna.ical4j.data.CalendarParser;
import net.fortuna.ical4j.data.CalendarParserFactory;
import net.fortuna.ical4j.extensions.outlook.OriginalEnd;
import net.fortuna.ical4j.extensions.outlook.OriginalStart;
import net.fortuna.ical4j.extensions.property.CalStart;
import net.fortuna.ical4j.model.ParameterFactoryRegistry;
import net.fortuna.ical4j.model.PropertyFactoryRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;

public class CalendarBuilder extends net.fortuna.ical4j.data.CalendarBuilder {

	private static final PropertyFactoryRegistry PROPERTY_FACTORY_REGISTRY = new PropertyFactoryRegistry();

	private static final ParameterFactoryRegistry PARAMETER_FACTORY_REGISTRY = new ParameterFactoryRegistry();
	
	static {
		PROPERTY_FACTORY_REGISTRY.register(CalStart.PROPERTY_NAME, CalStart.FACTORY);
		PROPERTY_FACTORY_REGISTRY.register(OriginalStart.PROPERTY_NAME, OriginalStart.FACTORY);
		PROPERTY_FACTORY_REGISTRY.register(OriginalEnd.PROPERTY_NAME, OriginalEnd.FACTORY);
	}
	
	public CalendarBuilder() {
        this(CalendarParserFactory.getInstance().createParser());
	}

	public CalendarBuilder(CalendarParser parser) {
        this(parser, TimeZoneRegistryFactory.getInstance().createRegistry());
	}

	public CalendarBuilder(TimeZoneRegistry tzRegistry) {
        this(CalendarParserFactory.getInstance().createParser(), tzRegistry);
	}

	public CalendarBuilder(CalendarParser parser, TimeZoneRegistry tzRegistry) {
        super(parser, PROPERTY_FACTORY_REGISTRY, PARAMETER_FACTORY_REGISTRY, tzRegistry);
	}

}
