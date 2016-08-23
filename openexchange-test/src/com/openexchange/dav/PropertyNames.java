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

package com.openexchange.dav;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.xml.Namespace;

/**
 * {@link PropertyNames}
 * 
 * Contains {@link DavPropertyName} definitions.
 * 
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class PropertyNames {

	/**
	 * xmlns:x0="DAV:"
	 */
	public final static Namespace NS_DAV = DavConstants.NAMESPACE;
//	public final static Namespace NS_DAV = Namespace.getNamespace("x0", "DAV:");
	
	/**
	 * xmlns:x1="urn:ietf:params:xml:ns:caldav"
	 */
	public final static Namespace NS_CALDAV = Namespace.getNamespace("x1", "urn:ietf:params:xml:ns:caldav");
	
	/**
	 * xmlns:x2="http://calendarserver.org/ns/"
	 */
	public final static Namespace NS_CALENDARSERVER = Namespace.getNamespace("x2", "http://calendarserver.org/ns/");;
	
	/**
	 * xmlns:x3="http://apple.com/ns/ical/"
	 */
	public final static Namespace NS_APPLE_ICAL = Namespace.getNamespace("x3", "http://apple.com/ns/ical/");;

	/**
	 * xmlns:x4="urn:ietf:params:xml:ns:carddav"
	 */
	public final static Namespace NS_CARDDAV = Namespace.getNamespace("x4", "urn:ietf:params:xml:ns:carddav");;

	/**
	 * xmlns:x5="http://me.com/_namespace/"
	 */
	public final static Namespace NS_ME_COM = Namespace.getNamespace("x5", "http://me.com/_namespace/");;


	/*---------------------------------------------------------------*/
	
	/**
	 * propfind xmlns="DAV:"
	 */
	public static final DavPropertyName PROPFIND = DavPropertyName.create("propfind", NS_DAV);

	/**
	 * prop xmlns="DAV:"
	 */
	public static final DavPropertyName PROP = DavPropertyName.create("prop", NS_DAV);

	/**
	 * principal-collection-set xmlns="DAV:"
	 */
	public static final DavPropertyName PRINCIPAL_COLLECTION_SET = DavPropertyName.create("principal-collection-set", NS_DAV);

	/**
	 * multistatus xmlns="DAV:"
	 */
	public static final DavPropertyName MULTISTATUS = DavPropertyName.create("multistatus", NS_DAV);

	/**
	 * response xmlns="DAV:"
	 */
	public static final DavPropertyName RESPONSE = DavPropertyName.create("response", NS_DAV);

	/**
	 * href xmlns="DAV:"
	 */
	public static final DavPropertyName HREF = DavPropertyName.create("href", NS_DAV);

	/**
	 * propstat xmlns="DAV:"
	 */
	public static final DavPropertyName PROPSTAT = DavPropertyName.create("propstat", NS_DAV);

	/**
	 * status xmlns="DAV:"
	 */
	public static final DavPropertyName STATUS = DavPropertyName.create("status", NS_DAV);

	/**
	 * current-user-principal xmlns="DAV:"
	 */
	public static final DavPropertyName CURRENT_USER_PRINCIPAL = DavPropertyName.create("current-user-principal", NS_DAV);

	/**
	 * displayname xmlns="DAV:"
	 */
	public static final DavPropertyName DISPLAYNAME = DavPropertyName.create("displayname", NS_DAV);

	/**
	 * principal-URL xmlns="DAV:"
	 */
	public static final DavPropertyName PRINCIPAL_URL = DavPropertyName.create("principal-URL", NS_DAV);

	/**
	 * supported-report-set xmlns="DAV:"
	 */
	public static final DavPropertyName SUPPORTED_REPORT_SET = DavPropertyName.create("supported-report-set", NS_DAV);

	/**
	 * resourcetype xmlns="DAV:"
	 */
	public static final DavPropertyName RESOURCETYPE = DavPropertyName.create("resourcetype", NS_DAV);

	/**
	 * owner xmlns="DAV:"
	 */
	public static final DavPropertyName OWNER = DavPropertyName.create("owner", NS_DAV);

	/**
	 * quota-available-bytes xmlns="DAV:"
	 */
	public static final DavPropertyName QUOTA_AVAILABLE_BYTES = DavPropertyName.create("quota-available-bytes", NS_DAV);

	/**
	 * quota-used-bytes xmlns="DAV:"
	 */
	public static final DavPropertyName QUOTA_USED_BYTES = DavPropertyName.create("quota-used-bytes", NS_DAV);

	/**
	 * current-user-privilege-set xmlns="DAV:"
	 */
	public static final DavPropertyName CURRENT_USER_PRIVILEGE_SET = DavPropertyName.create("current-user-privilege-set", NS_DAV);

	/**
	 * collection xmlns="DAV:"
	 */
	public static final DavPropertyName COLLECTION = DavPropertyName.create("collection", NS_DAV);

	/**
	 * privilege xmlns="DAV:"
	 */
	public static final DavPropertyName PRIVILEGE = DavPropertyName.create("privilege", NS_DAV);

	/**
	 * read-acl xmlns="DAV:"
	 */
	public static final DavPropertyName READ_ACL = DavPropertyName.create("read-acl", NS_DAV);

	/**
	 * read-current-user-privilege-set xmlns="DAV:"
	 */
	public static final DavPropertyName READ_CURRENT_USER_PRIVILEGE_SET = DavPropertyName.create("read-current-user-privilege-set", NS_DAV);

	/**
	 * read xmlns="DAV:"
	 */
	public static final DavPropertyName READ = DavPropertyName.create("read", NS_DAV);

	/**
	 * write xmlns="DAV:"
	 */
	public static final DavPropertyName WRITE = DavPropertyName.create("write", NS_DAV);

	/**
	 * write-properties xmlns="DAV:"
	 */
	public static final DavPropertyName WRITE_PROPERTIES = DavPropertyName.create("write-properties", NS_DAV);

	/**
	 * write-content xmlns="DAV:"
	 */
	public static final DavPropertyName WRITE_CONTENT = DavPropertyName.create("write-content", NS_DAV);

	/**
	 * write-acl xmlns="DAV:"
	 */
	public static final DavPropertyName WRITE_ACL = DavPropertyName.create("write-acl", NS_DAV);

	/**
	 * bind xmlns="DAV:"
	 */
	public static final DavPropertyName BIND = DavPropertyName.create("bind", NS_DAV);

	/**
	 * unbind xmlns="DAV:"
	 */
	public static final DavPropertyName UNBIND = DavPropertyName.create("unbind", NS_DAV);

	/**
	 * propertyupdate xmlns="DAV:"
	 */
	public static final DavPropertyName PROPERTYUPDATE = DavPropertyName.create("propertyupdate", NS_DAV);

	/**
	 * set xmlns="DAV:"
	 */
	public static final DavPropertyName SET = DavPropertyName.create("set", NS_DAV);

	/**
	 * getetag xmlns="DAV:"
	 */
	public static final DavPropertyName GETETAG = DavPropertyName.create("getetag", NS_DAV);

	/**
	 * resource-id xmlns="DAV:"
	 */
	public static final DavPropertyName RESOURCE_ID = DavPropertyName.create("resource-id", NS_DAV);

	/**
	 * add-member xmlns="DAV:"
	 */
	public static final DavPropertyName ADD_MEMBER = DavPropertyName.create("add-member", NS_DAV);

	/**
	 * sync-token xmlns="DAV:"
	 */
	public static final DavPropertyName SYNC_TOKEN = DavPropertyName.create("sync-token", NS_DAV);

	/**
	 * supported-report xmlns="DAV:"
	 */
	public static final DavPropertyName SUPPORTED_REPORT = DavPropertyName.create("supported-report", NS_DAV);

	/**
	 * report xmlns="DAV:"
	 */
	public static final DavPropertyName REPORT = DavPropertyName.create("report", NS_DAV);

	/**
	 * sync-collection xmlns="DAV:"
	 */
	public static final DavPropertyName SYNC_COLLECTION = DavPropertyName.create("sync-collection", NS_DAV);

	/**
	 * getcontenttype xmlns="DAV:"
	 */
	public static final DavPropertyName GETCONTENTTYPE = DavPropertyName.create("getcontenttype", NS_DAV);


	/**
	 * calendar-home-set xmlns="urn:ietf:params:xml:ns:caldav"
	 */
	public static final DavPropertyName CALENDAR_HOME_SET = DavPropertyName.create("calendar-home-set", NS_CALDAV);

	/**
	 * calendar-user-address-set xmlns="urn:ietf:params:xml:ns:caldav"
	 */
	public static final DavPropertyName CALENDAR_USER_ADDRESS_SET = DavPropertyName.create("calendar-user-address-set", NS_CALDAV);

	/**
	 * schedule-inbox-URL xmlns="urn:ietf:params:xml:ns:caldav"
	 */
	public static final DavPropertyName SCHEDULE_INBOX_URL = DavPropertyName.create("schedule-inbox-URL", NS_CALDAV);

	/**
	 * schedule-outbox-URL xmlns="urn:ietf:params:xml:ns:caldav"
	 */
	public static final DavPropertyName SCHEDULE_OUTBOX_URL = DavPropertyName.create("schedule-outbox-URL", NS_CALDAV);

	/**
	 * calendar-description xmlns="urn:ietf:params:xml:ns:caldav"
	 */
	public static final DavPropertyName CALENDAR_DESCRIPTION = DavPropertyName.create("calendar-description", NS_CALDAV);

	/**
	 * supported-calendar-component-set xmlns="urn:ietf:params:xml:ns:caldav"
	 */
	public static final DavPropertyName SUPPORTED_CALENDAR_COMPONENT_SET = DavPropertyName.create("supported-calendar-component-set", NS_CALDAV);

	/**
	 * calendar-free-busy-set xmlns="urn:ietf:params:xml:ns:caldav"
	 */
	public static final DavPropertyName CALENDAR_FREE_BUSY_SET = DavPropertyName.create("calendar-free-busy-set", NS_CALDAV);

	/**
	 * schedule-calendar-transp xmlns="urn:ietf:params:xml:ns:caldav"
	 */
	public static final DavPropertyName SCHEDULE_CALENDAR_TRANSP = DavPropertyName.create("schedule-calendar-transp", NS_CALDAV);

	/**
	 * schedule-default-calendar-URL xmlns="urn:ietf:params:xml:ns:caldav"
	 */
	public static final DavPropertyName SCHEDULE_DEFAULT_CALENDAR_URL = DavPropertyName.create("schedule-default-calendar-URL", NS_CALDAV);

	/**
	 * calendar-timezone xmlns="urn:ietf:params:xml:ns:caldav"
	 */
	public static final DavPropertyName CALENDAR_TIMEZONE = DavPropertyName.create("calendar-timezone", NS_CALDAV);

	/**
	 * comp xmlns="urn:ietf:params:xml:ns:caldav"
	 */
	public static final DavPropertyName COMP = DavPropertyName.create("comp", NS_CALDAV);

	/**
	 * calendar xmlns="urn:ietf:params:xml:ns:caldav"
	 */
	public static final DavPropertyName CALENDAR = DavPropertyName.create("calendar", NS_CALDAV);

	/**
	 * calendar-multiget xmlns="urn:ietf:params:xml:ns:caldav"
	 */
	public static final DavPropertyName CALENDAR_MULTIGET = DavPropertyName.create("calendar-multiget", NS_CALDAV);

	/**
	 * calendar-data xmlns="urn:ietf:params:xml:ns:caldav"
	 */
	public static final DavPropertyName CALENDAR_DATA = DavPropertyName.create("calendar-data", NS_CALDAV);

	/**
	 * calendar-query xmlns="urn:ietf:params:xml:ns:caldav"
	 */
	public static final DavPropertyName CALENDAR_QUERY = DavPropertyName.create("calendar-query", NS_CALDAV);


	/**
	 * dropbox-home-URL xmlns="http://calendarserver.org/ns/"
	 */
	public static final DavPropertyName DROPBOX_HOME_URL = DavPropertyName.create("dropbox-home-URL", NS_CALENDARSERVER);

	/**
	 * xmpp-uri xmlns="http://calendarserver.org/ns/"
	 */
	public static final DavPropertyName XMPP_URI = DavPropertyName.create("xmpp-uri", NS_CALENDARSERVER);

	/**
	 * notification-URL xmlns="http://calendarserver.org/ns/"
	 */
	public static final DavPropertyName NOTIFICATION_URL = DavPropertyName.create("notification-URL", NS_CALENDARSERVER);

	/**
	 * xmpp-server xmlns="http://calendarserver.org/ns/"
	 */
	public static final DavPropertyName XMPP_SERVER = DavPropertyName.create("xmpp-server", NS_CALENDARSERVER);

	/**
	 * getctag xmlns="http://calendarserver.org/ns/"
	 */
	public static final DavPropertyName GETCTAG = DavPropertyName.create("getctag", NS_CALENDARSERVER);

	/**
	 * source xmlns="http://calendarserver.org/ns/"
	 */
	public static final DavPropertyName SOURCE = DavPropertyName.create("source", NS_CALENDARSERVER);

	/**
	 * subscribed-strip-alarms xmlns="http://calendarserver.org/ns/"
	 */
	public static final DavPropertyName SUBSCRIBED_STRIP_ALARMS = DavPropertyName.create("subscribed-strip-alarms", NS_CALENDARSERVER);

	/**
	 * subscribed-strip-attachments xmlns="http://calendarserver.org/ns/"
	 */
	public static final DavPropertyName SUBSCRIBED_STRIP_ATTACHMENTS = DavPropertyName.create("subscribed-strip-attachments", NS_CALENDARSERVER);

	/**
	 * subscribed-strip-todos xmlns="http://calendarserver.org/ns/"
	 */
	public static final DavPropertyName SUBSCRIBED_STRIP_TODOS = DavPropertyName.create("subscribed-strip-todos", NS_CALENDARSERVER);

	/**
	 * push-transports xmlns="http://calendarserver.org/ns/"
	 */
	public static final DavPropertyName PUSH_TRANSPORTS = DavPropertyName.create("push-transports", NS_CALENDARSERVER);

	/**
	 * pushkey xmlns="http://calendarserver.org/ns/"
	 */
	public static final DavPropertyName PUSHKEY = DavPropertyName.create("pushkey", NS_CALENDARSERVER);

	/**
	 * publish-url xmlns="http://calendarserver.org/ns/"
	 */
	public static final DavPropertyName PUBLISH_URL = DavPropertyName.create("publish-url", NS_CALENDARSERVER);

	/**
	 * email-address-set xmlns="http://calendarserver.org/ns/"
	 */
	public static final DavPropertyName EMAIL_ADDRESS_SET = DavPropertyName.create("email-address-set", NS_CALENDARSERVER);

	/**
	 * notificationtype xmlns="http://calendarserver.org/ns/"
	 */
	public static final DavPropertyName NOTIFICATIONTYPE = DavPropertyName.create("notificationtype", NS_CALENDARSERVER);

	/**
	 * allowed-sharing-modes xmlns="http://calendarserver.org/ns/"
	 */
	public static final DavPropertyName ALLOWED_SHARING_MODES = DavPropertyName.create("allowed-sharing-modes", NS_CALENDARSERVER);


	/**
	 * calendar-color xmlns="http://apple.com/ns/ical/"
	 */
	public static final DavPropertyName CALENDAR_COLOR = DavPropertyName.create("calendar-color", NS_APPLE_ICAL);

	/**
	 * calendar-order xmlns="http://apple.com/ns/ical/"
	 */
	public static final DavPropertyName CALENDAR_ORDER = DavPropertyName.create("calendar-order", NS_APPLE_ICAL);

	/**
	 * refreshrate xmlns="http://apple.com/ns/ical/"
	 */
	public static final DavPropertyName REFRESHRATE = DavPropertyName.create("refreshrate", NS_APPLE_ICAL);


	/**
	 * bulk-requests xmlns="http://me.com/_namespace/"
	 */
	public static final DavPropertyName BULK_REQUESTS = DavPropertyName.create("bulk-requests", NS_ME_COM);


	/**
	 * max-image-size xmlns="urn:ietf:params:xml:ns:carddav"
	 */
	public static final DavPropertyName MAX_IMAGE_SIZE = DavPropertyName.create("max-image-size", NS_CARDDAV);

	/**
	 * max-resource-size xmlns="urn:ietf:params:xml:ns:carddav"
	 */
	public static final DavPropertyName MAX_RESOURCE_SIZE = DavPropertyName.create("max-resource-size", NS_CARDDAV);

	/**
	 * addressbook-home-set xmlns="urn:ietf:params:xml:ns:carddav"
	 */
	public static final DavPropertyName ADDRESSBOOK_HOME_SET = DavPropertyName.create("addressbook-home-set", NS_CARDDAV);

	/**
	 * addressbook xmlns="urn:ietf:params:xml:ns:carddav"
	 */
	public static final DavPropertyName ADDRESSBOOK = DavPropertyName.create("addressbook", NS_CARDDAV);

	/**
	 * directory-gateway xmlns="urn:ietf:params:xml:ns:carddav"
	 */
	public static final DavPropertyName DIRECTORY_GATEWAY = DavPropertyName.create("directory-gateway", NS_CARDDAV);

    /**
     * addressbook-multiget xmlns="urn:ietf:params:xml:ns:carddav"
     */
    public static final DavPropertyName ADDRESSBOOK_MULTIGET = DavPropertyName.create("addressbook-multiget", NS_CARDDAV);

    /**
     * addressbook-query xmlns="urn:ietf:params:xml:ns:carddav"
     */
    public static final DavPropertyName ADDRESSBOOK_QUERY = DavPropertyName.create("addressbook-query", NS_CARDDAV);

	/**
	 * address-data xmlns="urn:ietf:params:xml:ns:carddav"
	 */
	public static final DavPropertyName ADDRESS_DATA = DavPropertyName.create("address-data", NS_CARDDAV);

	/**
	 * me-card xmlns="http://calendarserver.org/ns/"
	 */
	public static final DavPropertyName ME_CARD = DavPropertyName.create("me-card", NS_CALENDARSERVER);

	/**
	 * response xmlns="urn:ietf:params:xml:ns:caldav"
	 */
	public static final DavPropertyName RESPONSE_CALDAV = DavPropertyName.create("response", NS_CALDAV);

	/**
	 * recipient xmlns="urn:ietf:params:xml:ns:caldav"
	 */
	public static final DavPropertyName RECIPIENT = DavPropertyName.create("recipient", NS_CALDAV);

	/**
	 * request-status xmlns="urn:ietf:params:xml:ns:caldav"
	 */
	public static final DavPropertyName REQUEST_STATUS = DavPropertyName.create("request-status", NS_CALDAV);

    /**
     * responsedescription xmlns="DAV:"
     */
    public static final DavPropertyName RESPONSEDESCRIPTION = DavPropertyName.create("responsedescription", NS_DAV);

    /**
     * default-alarm-vevent-date xmlns="urn:ietf:params:xml:ns:caldav"
     */
    public static final DavPropertyName DEFAULT_ALARM_VEVENT_DATE = DavPropertyName.create("default-alarm-vevent-date", NS_CALDAV);

    /**
     * default-alarm-vevent-datetime xmlns="urn:ietf:params:xml:ns:caldav"
     */
    public static final DavPropertyName DEFAULT_ALARM_VEVENT_DATETIME = DavPropertyName.create("default-alarm-vevent-datetime", NS_CALDAV);

    /**
     * mkcalendar xmlns="urn:ietf:params:xml:ns:caldav"
     */
    public static final DavPropertyName MKCALENDAR = DavPropertyName.create("mkcalendar", NS_CALDAV);

    /**
     * pre-publish-url xmlns="http://calendarserver.org/ns/"
     */
    public static final DavPropertyName PRE_PUBLISH_URL = DavPropertyName.create("pre-publish-url", NS_CALENDARSERVER);

    /**
     * supported-calendar-component-sets xmlns="urn:ietf:params:xml:ns:caldav"
     */
    public static final DavPropertyName SUPPORTED_CALENDAR_COMPONENT_SETS = DavPropertyName.create("supported-calendar-component-sets", NS_CALDAV);
    
    
	private PropertyNames() {
		// prevent instantiation
	}	
}
