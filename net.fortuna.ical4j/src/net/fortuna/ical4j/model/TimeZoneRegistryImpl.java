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
package net.fortuna.ical4j.model;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.TzUrl;
import net.fortuna.ical4j.util.CompatibilityHints;
import net.fortuna.ical4j.util.Configurator;
import net.fortuna.ical4j.util.ResourceLoader;

/**
 * $Id$
 *
 * Created on 18/09/2005
 *
 * The default implementation of a <code>TimeZoneRegistry</code>. This implementation will search the classpath for
 * applicable VTimeZone definitions used to back the provided TimeZone instances.
 * @author Ben Fortuna
 */
public class TimeZoneRegistryImpl implements TimeZoneRegistry {

    private static final String DEFAULT_RESOURCE_PREFIX = "zoneinfo/";

    private static final Pattern TZ_ID_SUFFIX = Pattern.compile("(?<=/)[^/]*/[^/]*$");

    private static final String UPDATE_ENABLED = "net.fortuna.ical4j.timezone.update.enabled";

    private static final Map DEFAULT_TIMEZONES = new ConcurrentHashMap();

    private static final Properties ALIASES = new Properties();
    static {
        try {
            ALIASES.load(ResourceLoader.getResourceAsStream("net/fortuna/ical4j/model/tz.alias"));
        }
        catch (IOException ioe) {
            LogFactory.getLog(TimeZoneRegistryImpl.class).warn(
                    "Error loading timezone aliases: " + ioe.getMessage());
        }

        InputStream resourceStream = null;
        try {
        	resourceStream = ResourceLoader.getResourceAsStream("tz.alias");
            ALIASES.load(resourceStream);
        }
        catch (Exception e) {
        	LogFactory.getLog(TimeZoneRegistryImpl.class).debug(
        			"Error loading custom timezone aliases: " + e.getMessage());
        }
        finally {
            if (null != resourceStream) {
                try { resourceStream.close(); } catch (Exception x) { /*ignore*/ }
            }
        }
    }

    private final Map timezones;

    private final String resourcePrefix;

    /**
     * Default constructor.
     */
    public TimeZoneRegistryImpl() {
        this(DEFAULT_RESOURCE_PREFIX);
    }

    /**
     * Creates a new instance using the specified resource prefix.
     * @param resourcePrefix a prefix prepended to classpath resource lookups for default timezones
     */
    public TimeZoneRegistryImpl(final String resourcePrefix) {
        this.resourcePrefix = resourcePrefix;
        timezones = new ConcurrentHashMap();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void register(final TimeZone timezone) {
    	// for now we only apply updates to included definitions by default..
    	register(timezone, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void register(final TimeZone timezone, boolean update) {
    	if (update) {
            // load any available updates for the timezone..
            timezones.put(timezone.getID(), new TimeZone(updateDefinition(timezone.getVTimeZone())));
    	}
    	else {
            timezones.put(timezone.getID(), timezone);
    	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void clear() {
        timezones.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final TimeZone getTimeZone(final String id) {
        TimeZone timezone = (TimeZone) timezones.get(id);
        if (timezone == null) {
            Map cachedTimezones;
            synchronized (DEFAULT_TIMEZONES) {
                cachedTimezones = (Map) DEFAULT_TIMEZONES.get(resourcePrefix);
                if (null == cachedTimezones) {
                    cachedTimezones = new ConcurrentHashMap();
                    DEFAULT_TIMEZONES.put(resourcePrefix, cachedTimezones);
                }
            }
            synchronized (cachedTimezones) {
                timezone = (TimeZone) cachedTimezones.get(id);
                if (timezone == null) {
                    // if timezone not found with identifier, try loading an alias..
                    final String alias = ALIASES.getProperty(id);
                    if (alias != null) {
                        return getTimeZone(alias);
                    }
                    try {
                        final VTimeZone vTimeZone = loadVTimeZone(id);
                        if (vTimeZone != null) {
                            // XXX: temporary kludge..
                            // ((TzId) vTimeZone.getProperties().getProperty(Property.TZID)).setValue(id);
                            timezone = new TimeZone(vTimeZone);
                            cachedTimezones.put(timezone.getID(), timezone);
                        }
                        else if (CompatibilityHints.isHintEnabled(CompatibilityHints.KEY_RELAXED_PARSING)) {
                            // strip global part of id and match on default tz..
                            Matcher matcher = TZ_ID_SUFFIX.matcher(id);
                            if (matcher.find()) {
                                return getTimeZone(matcher.group());
                            }
                        }
                    } catch (Exception e) {
                        Log log = LogFactory.getLog(TimeZoneRegistryImpl.class);
                        log.warn("Error occurred loading VTimeZone", e);
                    }
                }
            }
        }
        return timezone;
    }

    /**
     * Loads an existing VTimeZone from the classpath corresponding to the specified Java timezone.
     */
    private VTimeZone loadVTimeZone(final String id) throws IOException, ParserException {
        final URL resource = ResourceLoader.getResource(resourcePrefix + id + ".ics");
        if (resource != null) {
            final CalendarBuilder builder = new CalendarBuilder();
            final Calendar calendar = builder.build(resource.openStream());
            final VTimeZone vTimeZone = (VTimeZone) calendar.getComponent(Component.VTIMEZONE);
            // load any available updates for the timezone.. can be explicility disabled via configuration
            if (!"false".equals(Configurator.getProperty(UPDATE_ENABLED))) {
                return updateDefinition(vTimeZone);
            }
            return vTimeZone;
        }
        return null;
    }

    /**
     * @param vTimeZone
     * @return
     */
    private VTimeZone updateDefinition(VTimeZone vTimeZone) {
        final TzUrl tzUrl = vTimeZone.getTimeZoneUrl();
        if (tzUrl != null) {
            InputStream inputStream = null;
            try {
                URLConnection connection = tzUrl.getUri().toURL().openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                inputStream = connection.getInputStream();
                final CalendarBuilder builder = new CalendarBuilder();
                final Calendar calendar = builder.build(inputStream);
                final VTimeZone updatedVTimeZone = (VTimeZone) calendar.getComponent(Component.VTIMEZONE);
                if (updatedVTimeZone != null) {
                    return updatedVTimeZone;
                }
            }
            catch (Exception e) {
                Log log = LogFactory.getLog(TimeZoneRegistryImpl.class);
                log.warn("Unable to retrieve updates for timezone: " + vTimeZone.getTimeZoneId().getValue(), e);
            }
            finally {
                if (null != inputStream) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        LogFactory.getLog(TimeZoneRegistryImpl.class).debug("Error closing stream", e);
                    }
                }
            }
        }
        return vTimeZone;
    }
}
