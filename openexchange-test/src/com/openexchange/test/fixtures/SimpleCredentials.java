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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
package com.openexchange.test.fixtures;

import java.util.Calendar;
import java.util.TimeZone;

import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.groupware.container.Contact;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 * @author Stefan Preuss <stefan.preuss@open-xchange.com>
 */
public class SimpleCredentials implements Cloneable {
    private String login;
    private String imapLogin;
    private String password;
    private Contact contact;
    private TestUserConfig config;
    private TestUserConfigFactory userConfigFactory = null;
	private ContactFinder contactFinder;

    public SimpleCredentials(TestUserConfigFactory userConfigFactory, ContactFinder contactFinder) {
    	super();
    	this.userConfigFactory = userConfigFactory;
    	this.contactFinder = contactFinder;
    }
    
    public String getLogin() {
        return login;
    }

    public void setLogin(final String login) {
        this.login = login;
    }
    
    public String getIMAPLogin() {
        return null == this.imapLogin ? this.login : imapLogin;
    }
    
    public void setIMAPLogin(final String imapLogin) {
        this.imapLogin = imapLogin;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public Contact asContact() {
    	if (null == this.contact) {
    		this.contact = contactFinder.getContact(this);
    	}
        return contact;
    }
    
    public TestUserConfig getConfig() {
    	if (null == config) {
    		config = userConfigFactory.create(this);
    	}
    	return config;
    }
    
    public int getUserId() {
    	return getConfig().getInt(Tree.Identifier);
    }

    public TimeZone getTimeZone() {
    	return TimeZone.getTimeZone(getConfig().getString(Tree.TimeZone));
    }
    
    public Calendar getCalendar() {
    	final Calendar calendar = Calendar.getInstance(getTimeZone());
    	calendar.setMinimalDaysInFirstWeek(4);
    	calendar.setFirstDayOfWeek(Calendar.MONDAY);
    	return calendar;
    }

    public boolean equals(final Object o) {
        if (this == o) { 
        	return true; 
        }
        if (o == null || getClass() != o.getClass()) { 
        	return false; 
        }
        final SimpleCredentials that = (SimpleCredentials) o;
        return (null != this.login ? this.login.equals(that.login) : null == that.login) &&
	    	(null != this.password ? this.password.equals(that.password) : null == that.password);
    }

    public int hashCode() {
        int result;
        result = (login != null ? login.hashCode() : 0);
        return result;
    }
    
	public String toString() {
		return String.format("SimpleCredentials[%s]", null != this.getLogin() ? this.getLogin() : "");
	}
	
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
}
