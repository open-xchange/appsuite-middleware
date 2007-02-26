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



package com.openexchange.groupware;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.event.EventConfig;
import com.openexchange.event.EventConfigImpl;
import com.openexchange.event.EventQueue;
import com.openexchange.groupware.attach.AttachmentCleaner;
import com.openexchange.groupware.infostore.webdav.PropertyCleaner;
import com.openexchange.groupware.links.LinksEventHandler;
import com.openexchange.groupware.notify.ParticipantNotify;
import com.openexchange.push.udp.PushConfigInterface;
import com.openexchange.push.udp.PushConfigInterfaceImpl;
import com.openexchange.push.udp.PushHandler;
import com.openexchange.push.udp.PushMulticastRequestTimer;
import com.openexchange.push.udp.PushMulticastSocket;
import com.openexchange.push.udp.PushOutputQueue;
import com.openexchange.push.udp.PushSocket;
import com.openexchange.server.ComfireConfig;
import com.openexchange.server.ComfireInitWorker;
import com.openexchange.server.ComfireLogger;
import com.openexchange.server.Version;
import com.openexchange.sessiond.Sessiond;
import com.openexchange.sessiond.SessiondConfigWrapper;
import com.openexchange.sessiond.SessiondConnector;
import com.openexchange.tools.conf.GlobalConfig;
import com.openexchange.webdav.InfostorePerformer;

/**
 * InitWorker
 * 
 * @author <a href="mailto:martin.kauss@open-xchange.com">Martin Kauss</a>
 * @author <a href="mailto:stefan.preuss@open-xchange.com">Stefan Preuss</a>
 */
public class InitWorker extends ComfireInitWorker {

	static {
		ComfireInitWorker.registerWorker(new InitWorker());
	}

	private static final String allowedHTMLVersions = InitWorker.getAppVersion();

	//private static final String htmlPath = ComfireConfig.properties.getProperty("HTMLPATH");

	private static HashSet allowedLanguages = new HashSet();

	private static int lll = ComfireLogger.DEBUG;

	private static String directories[] = { ComfireConfig.properties.getProperty("CONFIGPATH"),
			ComfireConfig.properties.getProperty("SETTINGSSTOREPATH") };


	private static final Log LOG = LogFactory.getLog(InitWorker.class);

	/**
	 * Constructor
	 */
	public InitWorker() {
		super();
	}

	public void doInit() {

		ComfireLogger.log("DEBUG: JAVA default file.encoding: " + System.getProperty("file.encoding"),
				ComfireLogger.DEBUG);
		ComfireLogger.log("DEBUG: Template deps: " + allowedHTMLVersions, ComfireLogger.DEBUG);
		ComfireLogger.log("DEBUG: Using html path: " + ComfireConfig.properties.getProperty("HTMLPATH"),
				ComfireLogger.DEBUG);
		ComfireLogger.log("DEBUG: Using config path: " + ComfireConfig.properties.getProperty("CONFIGPATH"),
				ComfireLogger.DEBUG);
		ComfireLogger.log("DEBUG: Using settings path: " + ComfireConfig.properties.getProperty("SETTINGSSTOREPATH"),
				ComfireLogger.DEBUG);
		ComfireLogger.log("DEBUG: Using filespool path: " + ComfireConfig.properties.getProperty("FILESPOOLPATH"),
				ComfireLogger.DEBUG);
		ComfireLogger.log("DEBUG: Using ldap properties file: "
				+ ComfireConfig.properties.getProperty("LDAPPROPERTIES"), ComfireLogger.DEBUG);

		GlobalConfig.loadConf();
		// findAllowedLanguages();

		GlobalConfig.setParameter("nasName", InitWorker.getAppName());
		GlobalConfig.setParameter("nasVersion", InitWorker.getAppVersion());

		if (ComfireConfig.properties.getProperty("LOGIN_LOG_LEVEL") != null) {
			final String check = ComfireConfig.properties.getProperty("LOGIN_LOG_LEVEL");
			try {
				lll = Integer.valueOf(check).intValue();
			} catch (NumberFormatException nfe) {
			}
			System.out.println("LLL = " + lll);
		}
		LOG.info("Parse Event properties");
		final EventConfig eventConfig = new EventConfigImpl(ComfireConfig.properties.getProperty("EVENTPROPERTIES"));
		final EventQueue eventQueue = new EventQueue(eventConfig);

		LOG.info("Parse Push properties");
		final PushConfigInterface pushConfigInterface = new PushConfigInterfaceImpl(ComfireConfig.properties
				.getProperty("PUSHPROPERTIES"));
		final PushSocket pushSocket = new PushSocket(pushConfigInterface);
		final PushOutputQueue pushOutputQueue = new PushOutputQueue(pushConfigInterface);

		if (pushConfigInterface.isPushEnabled()) {
			final PushHandler pushHandler = new PushHandler();
			EventQueue.addAppointmentEvent(pushHandler);
			EventQueue.addTaskEvent(pushHandler);
			EventQueue.addContactEvent(pushHandler);
			EventQueue.addFolderEvent(pushHandler);
		}
		
		final PushMulticastSocket pushMultiCastRequest = new PushMulticastSocket(pushConfigInterface);
		final PushMulticastRequestTimer multicastRequest = new PushMulticastRequestTimer(pushConfigInterface);

		LOG.info("Adding Notification Listener");
		final ParticipantNotify notify = new ParticipantNotify();
		EventQueue.addAppointmentEvent(notify);
		EventQueue.addTaskEvent(notify);

		LOG.info("Adding LinkEventHandler");
		final LinksEventHandler linkHandler = new LinksEventHandler();
		EventQueue.addAppointmentEvent(linkHandler);
		EventQueue.addContactEvent(linkHandler);
		EventQueue.addTaskEvent(linkHandler);
		EventQueue.addInfostoreEvent(linkHandler);

		LOG.info("Adding AttachmentCleaner");
		final AttachmentCleaner attCleaner = new AttachmentCleaner();
		EventQueue.addAppointmentEvent(attCleaner);
		EventQueue.addContactEvent(attCleaner);
		EventQueue.addTaskEvent(attCleaner);
		
		LOG.info("Adding PropertiesCleaner");
		PropertyCleaner propertyCleaner = new PropertyCleaner(InfostorePerformer.getInstance().getFactory().getFolderProperties(), InfostorePerformer.getInstance().getFactory().getInfoProperties());
		EventQueue.addFolderEvent(propertyCleaner);
		EventQueue.addInfostoreEvent(propertyCleaner);
		
//		ComfireLogger.log("Starting AJP-Server...", ComfireLogger.INFO);
//		AJPv13Server.startAJPServer();
//		ComfireLogger.log("\tDONE", ComfireLogger.INFO);
//
//		ComfireLogger.log("Starting JMX Monitor Agent...", ComfireLogger.INFO);
//		MonitorAgent.startMonitorAgent();
//		ComfireLogger.log("\tDONE", ComfireLogger.INFO);

		LOG.info("Parse Sessiond properties");
		final SessiondConfigWrapper config = new SessiondConfigWrapper(ComfireConfig.properties
				.getProperty("SESSIONDPROPERTIES"));
		SessiondConnector.setConfig(config);

		LOG.info("Starting Sessiond");
		new Sessiond(config);

	}

	public static void log(final String user, final String msg, final Exception e) {
		final Date d = new Date();

		System.err.println("------------------------------------------------------------------");
		System.err.println(new SimpleDateFormat("MMM dd HH:mm:ss").format(d) + " - " + user + " - "
				+ InitWorker.getAppVersion());
		System.err.println(msg);
		if (null != e) {
			System.err.println("StackTrace:");
			e.printStackTrace();
		}
		System.err.println("------------------------------------------------------------------");
	}

	public static HashSet getAllowedLanguages() {
		return ((HashSet) allowedLanguages.clone());
	}

	public static String getAppVersion() {
		return Version.BUILDNUMBER;
	}

	public static String getAppName() {
		return Version.NAME;
	}

	public static String getAppCodename() {
		return Version.CODENAME;
	}
}
