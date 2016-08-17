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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.dav.caldav.ical;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang.StringEscapeUtils;
import com.openexchange.dav.caldav.ICalResource;


/**
 * {@link SimpleICal}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SimpleICal {

	private static final String CRLF = "\r\n";

    public static Component parse(String iCal) throws IOException, SimpleICalException {
        return parse(iCal, "VCALENDAR");
    }

    public static Component parse(String iCal, String name) throws IOException, SimpleICalException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new StringReader(ICalUtils.unfold(iCal)));
            String line = reader.readLine();
            if (null == line || false == line.startsWith("BEGIN:" + name)) {
                throw new SimpleICalException(name + " component expected");
            }
            return new Component(name, reader);
        } finally {
            reader.close();
        }
    }

    public static final class Component {

		private final String name;
		private final List<Property> properties;
		private final List<Component> components;

        public Component(String name, BufferedReader reader) throws SimpleICalException, IOException {
            this(name);
            parse(reader);
        }

        public Component(String name) {
            super();
            this.name = name;
            this.properties = new ArrayList<SimpleICal.Property>();
            this.components = new ArrayList<SimpleICal.Component>();
        }

        public Component getVAlarm() {
            List<Component> components = getVAlarms();
            return 0 < components.size() ? components.get(0) : null;
        }

        public List<Component> getVAlarms() {
            return getComponents(ICalResource.VALARM);
        }

    	public String getUID() {
	        return this.getPropertyValue("UID");
	    }

        public String getSummary() {
            return this.getPropertyValue("SUMMARY");
        }

        public String getDescription() {
            return this.getPropertyValue("DESCRIPTION");
        }

        public Property getAttendee(String email) {
            List<Property> properties = this.getProperties("ATTENDEE");
            if (null != properties) {
                for (Property property : properties) {
                    String value = property.getValue();
                    if (null != value && value.contains("mailto:" + email)) {
                        return property;
                    }
                }
            }
            return null;
        }

        public List<Date> getExDates() throws ParseException {
            List<Date> exDates = new ArrayList<Date>();
            List<Property> properties = this.getProperties("EXDATE");
            for (Property property : properties) {
                exDates.add(ICalUtils.parseDate(property));
            }
            return exDates;
        }

        public void setSummary(String summary) throws ParseException {
            this.setProperty("SUMMARY", summary);
        }

        public void setLocation(String location) throws ParseException {
            this.setProperty("LOCATION", location);
        }

        public Date getRecurrenceID() throws ParseException {
            return ICalUtils.parseDate(this.getProperty("RECURRENCE-ID"));
        }

        public Date getDTStart() throws ParseException {
            return ICalUtils.parseDate(this.getProperty("DTSTART"));
        }

	    public void setDTStart(Date start) throws ParseException {
	        this.setProperty("DTSTART", ICalUtils.formatAsUTC(start));
	    }

	    public Date getDTEnd() throws ParseException {
	        return ICalUtils.parseDate(this.getProperty("DTEND"));
	    }

	    public void setDTEnd(Date start) throws ParseException {
	        this.setProperty("DTEND", ICalUtils.formatAsUTC(start));
	    }

        public String getLocation() {
            return this.getPropertyValue("LOCATION");
        }

        public String getTransp() {
            return this.getPropertyValue("TRANSP");
        }

        public void setTransp(String transp) {
            this.setProperty("TRANSP", transp);
        }

    	public List<Component> getComponents() {
    		return components;
    	}

    	public List<Component> getComponents(String type) {
    		List<Component> filteredComponents = new ArrayList<SimpleICal.Component>();
    		for (Component component : this.components) {
    			if (type.equals(component.getName())) {
    				filteredComponents.add(component);
    			}
    		}
    		return filteredComponents;
    	}

    	private void parse(BufferedReader reader) throws SimpleICalException, IOException {
    		String line = null;
    		while (null != (line = reader.readLine())) {
    			if (line.startsWith("END:" + name)) {
    				// finished
    				break;
    			} else if (line.startsWith("BEGIN:")) {
					// subcomponent
    				String name = line.substring(6).trim();
    				this.components.add(new Component(name, reader));
    			} else {
					// property
					this.properties.add(new Property(line));
    			}
    		}
    	}

    	public String getName() {
    		return name;
    	}

        public Property getProperty(String name) {
            for (Property property : this.properties) {
                if (name.equals(property.name)) {
                    return property;
                }
            }
            return null;
        }

        public void setProperty(String name, String value, Map<String, String> attributes) {
            for (Property property : this.properties) {
                if (name.equals(property.name)) {
                    property.value = value;
                    property.attributes = attributes;
                    return;
                }
            }
            this.properties.add(new Property(name, value, attributes));
        }

        public void setProperty(String name, String value) {
            this.setProperty(name, value, new HashMap<String, String>());
        }

        public void removeProperties(String name) {
            for (Property property : getProperties(name)) {
                this.properties.remove(property);
            }
        }

    	public List<Property> getProperties(String name) {
    		List<Property> properties = new ArrayList<SimpleICal.Property>();
    		for (Property property : this.properties) {
    			if (name.equals(property.name)) {
    				properties.add(property);
    			}
			}
    		return properties;
    	}

    	public List<Property> getProperties() {
    		return properties;
    	}

    	public String getPropertyValue(String propertyName) {
    		Property property = getProperty(propertyName);
    		return null != property ? property.value : null;
    	}

    	@Override
    	public String toString() {
    		StringBuilder stringBuilder = new StringBuilder();
    		stringBuilder.append("BEGIN:").append(name).append(CRLF);
    		for (Property property : this.properties) {
    			stringBuilder.append(property).append(CRLF);
    		}
    		for (Component component : this.components) {
    			stringBuilder.append(component).append(CRLF);
    		}
    		stringBuilder.append("END:").append(name);
    		return stringBuilder.toString();
    	}

    }

    public static final class Property {

    	private String name;
    	private String value;
    	private Map<String, String> attributes;

    	public Property(String line) throws SimpleICalException {
    	    super();
            this.attributes = new HashMap<String, String>();
    		this.parse(line);
    	}

    	public Property(String name, String value, Map<String, String> attributes) {
            super();
            this.name = name;
            this.value = value;
            this.attributes = attributes;
        }

    	private void parse(String line) throws SimpleICalException {
    		int index = line.indexOf(':');
    		if (0 > index) {
    			throw new SimpleICalException("No ':' character found in " + line);
    		}
    		//this.value = line.substring(1 + index).trim();
    		this.value = StringEscapeUtils.unescapeJava(line.substring(1 + index));
    		String nameAndAttributes = line.substring(0, index);
    		index = nameAndAttributes.indexOf(';');
    		if (0 > index) {
    			// no attributes
    			this.name = nameAndAttributes.toUpperCase();
    		} else {
    			this.name = nameAndAttributes.substring(0, index).toUpperCase();
    			StringBuilder attributeNameBuilder = new StringBuilder();
    			StringBuilder attributeValueBuilder = new StringBuilder();
    			boolean inQuote = false;
    			boolean inAttributeName = true;
    			for (int i = index; i < nameAndAttributes.length (); i++) {
    				char c = nameAndAttributes.charAt(i);
    				if (';' == c && false == inQuote) {
    					if (0 < attributeNameBuilder.length ()) {
    						attributes.put(attributeNameBuilder.toString(), attributeValueBuilder.toString());
    						attributeNameBuilder.setLength(0);
    						attributeValueBuilder.setLength(0);
    						inAttributeName = true;
    					}
    				} else if ('=' == c && false == inQuote) {
    					inAttributeName = false;
    				} else if (',' == c && false == inQuote) {
    					// ',' should be quoted in attribute values
    					throw new SimpleICalException("Unquoted comma in attribute value: " + nameAndAttributes);
    				} else if ('"' == c) {
    					inQuote = false == inQuote;
    				} else {
    					if (inAttributeName) {
    						attributeNameBuilder.append(c);
    					} else {
    						attributeValueBuilder.append(c);
    					}
    				}
    			}
    			if (0 < attributeNameBuilder.length()) {
    				attributes.put(attributeNameBuilder.toString(), attributeValueBuilder.toString());
    			}
    		}
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

    	/**
    	 * @return the attributes
    	 */
    	public Map<String, String> getAttributes() {
    		return attributes;
    	}

    	public String getAttribute(String attributeName) {
    		return attributes.get(attributeName);
    	}

    	@Override
    	public String toString() {
    		StringBuilder stringBuilder = new StringBuilder();
    		stringBuilder.append(name);
    		for (Entry<String, String> entry : attributes.entrySet()) {
    			stringBuilder.append(";").append(entry.getKey()).append("=");
    			if (0 < entry.getValue().indexOf(',')) {
    				stringBuilder.append('"').append(entry.getValue()).append('"');
    			} else {
    				stringBuilder.append(entry.getValue());
    			}
			}
    		stringBuilder.append(":").append(value);
    		return stringBuilder.toString();
    	}
    }

    public static final class SimpleICalException extends Exception {

    	private static final long serialVersionUID = -24367328086610189L;

    	public SimpleICalException(String msg) {
    		super(msg);
    	}

    }

}
