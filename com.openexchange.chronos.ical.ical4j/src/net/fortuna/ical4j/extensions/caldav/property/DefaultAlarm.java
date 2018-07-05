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

import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyFactory;
import net.fortuna.ical4j.model.PropertyFactoryImpl;
import net.fortuna.ical4j.model.ValidationException;

/**
 * New property as defined in a RFC draft
 * 
 * @see <a href="http://tools.ietf.org/html/draft-daboo-valarm-extensions-04">draft-daboo-valarm-extensions-04</a>
 * 
 * @author probert
 *
 */
public class DefaultAlarm extends Property {

  private static final long serialVersionUID = 2182103734645261668L;
  
  public static final String PROPERTY_NAME = "DEFAULT-ALARM";
  
  private String value;

  public static final PropertyFactory FACTORY = new Factory();
  
  public static final DefaultAlarm TRUE = new ImmutableDefaultAlarm("TRUE", null);
  
  public static final DefaultAlarm FALSE = new ImmutableDefaultAlarm("FALSE", null);
    
  public DefaultAlarm(PropertyFactory factory) {
    super(PROPERTY_NAME, factory);
  }

  public DefaultAlarm(ParameterList aList, PropertyFactory factory, String value) {
    super(PROPERTY_NAME, aList, factory);
    setValue(value);
  }

  public DefaultAlarm(ParameterList aList, String aValue) {
    super(PROPERTY_NAME, aList, PropertyFactoryImpl.getInstance());
    value = aValue;
  }

  @Override
  public void setValue(String aValue) {
    this.value = aValue;
  }

  @Override
  public void validate() throws ValidationException {
  }

  @Override
  public String getValue() {
    return value;
  }
  
  private static final class ImmutableDefaultAlarm extends DefaultAlarm {
    private static final long serialVersionUID = -2054338254L;

    private ImmutableDefaultAlarm(String value) {
      super(new ParameterList(true), value);
    }
    
    public void setValue(String aValue) {
      throw new UnsupportedOperationException("Cannot modify constant instances");
    }
    
    ImmutableDefaultAlarm(String s, ImmutableDefaultAlarm immutableclazz) {
      this(s);
    }
  }

  private static class Factory implements PropertyFactory {

    private static final long serialVersionUID = 2099427445505899578L;

    public Property createProperty(String name) {
      return new DefaultAlarm(this);
    }

    public Property createProperty(String name, ParameterList parameters, String value) {
      DefaultAlarm property = null;
      if (FALSE.getValue().equals(value)) {
        property = FALSE;
      }
      else {
        property = new DefaultAlarm(parameters, this, value);
      }
      return property;
    }
  }

}
