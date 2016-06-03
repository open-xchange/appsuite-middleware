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
package net.fortuna.ical4j.extensions.caldav.property;

import net.fortuna.ical4j.extensions.caldav.parameter.CalendarServerAttendeeRef;
import net.fortuna.ical4j.extensions.caldav.parameter.CalendarServerDtStamp;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyFactory;
import net.fortuna.ical4j.model.PropertyFactoryImpl;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.util.ParameterValidator;

/**
 * This property is a non-standard property for iCal Server/Calendar Server
 * 
 * @see <a href="http://svn.calendarserver.org/repository/calendarserver/CalendarServer/trunk/doc/Extensions/caldav-privatecomments.txt">caldav-privatecomments.txt</a>
 * 
 * @author probert
 *
 */
public class CalendarServerAttendeeComment extends Property {

  private static final long serialVersionUID = 2182103734645261668L;
  
  public static final String PROPERTY_NAME = "X-CALENDARSERVER-ATTENDEE-COMMENT";
  
  private String value;

  public static final PropertyFactory FACTORY = new Factory();
    
  public CalendarServerAttendeeComment(PropertyFactory factory) {
    super(PROPERTY_NAME, factory);
  }

  public CalendarServerAttendeeComment(ParameterList aList, PropertyFactory factory, String value) {
    super(PROPERTY_NAME, aList, factory);
    setValue(value);
  }

  public CalendarServerAttendeeComment(ParameterList aList, String aValue) {
    super(PROPERTY_NAME, aList, PropertyFactoryImpl.getInstance());
    value = aValue;
  }

  @Override
  public void setValue(String aValue) {
    this.value = aValue;
  }

  @Override
  public void validate() throws ValidationException {
    /*
     * ; the following are REQUIRED,
     * ; but MUST occur only once
     */
    ParameterValidator.getInstance().assertOne(CalendarServerDtStamp.PARAMETER_NAME,
        getParameters());

    /*
     * ; the following are REQUIRED,
     * ; but MUST occur only once
     */
    ParameterValidator.getInstance().assertOne(CalendarServerAttendeeRef.PARAMETER_NAME,
        getParameters());
  }

  @Override
  public String getValue() {
    return value;
  }

  private static class Factory implements PropertyFactory {

    private static final long serialVersionUID = 2099427445505899578L;

    public Property createProperty(String name) {
      return new CalendarServerAttendeeComment(this);
    }

    public Property createProperty(String name, ParameterList parameters, String value) {
      CalendarServerAttendeeComment property = null;
      property = new CalendarServerAttendeeComment(parameters, this, value);
      return property;
    }
  }

}
