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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.logging.tracking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.NoOpLog;

import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.log.LogPropertyName.LogLevel;
import com.openexchange.session.Session;
import com.openexchange.user.UserService;

public class TrackingConfiguration implements TrackingConfigurationMBean {
	
	private final ConcurrentMap<String, ConcurrentMap<String, LogLevel>> sessionLevels = new ConcurrentHashMap<String, ConcurrentMap<String, LogLevel>>();
	
	private final ConcurrentMap<Integer, ConcurrentMap<String, LogLevel>> cidLevels = new ConcurrentHashMap<Integer, ConcurrentMap<String, LogLevel>>();
	
	private final ConcurrentMap<Integer, ConcurrentMap<Integer, ConcurrentMap<String, LogLevel>>> userLevels = new ConcurrentHashMap<Integer, ConcurrentMap<Integer, ConcurrentMap<String, LogLevel>>>();
	
	private final UserService users;
	private final ContextService contexts;
	
	public TrackingConfiguration(UserService users, ContextService contexts) {
		this.users = users;
		this.contexts = contexts;
	}
	
	public void setLogLevel(String className, String sessionId, String lvl) {
		LogLevel level = LogLevel.valueOf(lvl.toUpperCase());
		ConcurrentMap<String, LogLevel> clazzMap = sessionLevels.putIfAbsent(sessionId, new ConcurrentHashMap<String, LogLevel>());
		if (level == null) {
			clazzMap.remove(className);
		} else {
			clazzMap.put(className, level);
		}
	}
	
	public void setLogLevel(String className, int cid, int uid, String lvl) {
		LogLevel level = LogLevel.valueOf(lvl.toUpperCase());

		ConcurrentMap<Integer, ConcurrentMap<String, LogLevel>> userMap = userLevels.putIfAbsent(Integer.valueOf(cid), new ConcurrentHashMap<Integer, ConcurrentMap<String, LogLevel>>());
		ConcurrentMap<String, LogLevel> clazzMap = userMap.putIfAbsent(Integer.valueOf(uid), new ConcurrentHashMap<String, LogLevel>());
		if (level == null) {
			clazzMap.remove(className);
		} else {
			clazzMap.put(className, level);
		}
	}
	
	public void setLogLevel(String className, int cid, String lvl) {
		LogLevel level = LogLevel.valueOf(lvl.toUpperCase());

		ConcurrentMap<String, LogLevel> clazzMap = cidLevels.putIfAbsent(Integer.valueOf(cid), new ConcurrentHashMap<String, LogLevel>());
		if (level == null) {
			clazzMap.remove(className);
		} else {
			clazzMap.put(className, level);
		}
	}
	
	private int getUserId(String userName, int cid) throws OXException {
		Context context;
		context = contexts.getContext(cid);
		User[] candidates = users.searchUserByName(userName, context, UserService.SEARCH_LOGIN_NAME);
		if (candidates.length == 0) {
			candidates = users.searchUserByName(userName, context, UserService.SEARCH_DISPLAY_NAME);
		}
		if (candidates.length == 0) {
			throw OXException.general("Could not resolve user: "+userName);
		}
		return candidates[0].getId();
	}
	
	public boolean setLogLevel(String className, int cid, String userName, String level) {
		
		try {
			setLogLevel(className, cid, getUserId(userName, cid), level);
		} catch (OXException e) {
			return false;
		}
		return true;
	}
	
	public void clearTracking(int cid, int uid) {
		ConcurrentMap<Integer, ConcurrentMap<String, LogLevel>> userMap = userLevels.get(Integer.valueOf(cid));
		if (userMap != null) {
			userMap.remove(Integer.valueOf(uid));
			if (userMap.isEmpty()) {
				userLevels.remove(Integer.valueOf(cid));
			}
		}
	}

	public void clearTracking(String sessionId) {
		sessionLevels.remove(sessionId);
	}

	public void clearTracking(int cid) {
		cidLevels.remove(Integer.valueOf(cid));
	}

	public boolean clearTracking(int cid, String userName) {
		try {
			clearTracking(cid, getUserId(userName, cid));
		} catch (OXException x) {
			return false;
		}
		return true;
	}
	

		
	public boolean isDebugEnabled(String[] className, Session sessionObject) {
		return getLogLevel(className, sessionObject).includes(LogLevel.DEBUG);
	}

	public boolean isErrorEnabled(String[] className, Session sessionObject) {
		return getLogLevel(className, sessionObject).includes(LogLevel.ERROR);
	}

	public boolean isFatalEnabled(String[] className, Session sessionObject) {
		return getLogLevel(className, sessionObject).includes(LogLevel.FATAL);
	}

	public boolean isInfoEnabled(String[] className, Session sessionObject) {
		return getLogLevel(className, sessionObject).includes(LogLevel.INFO);
	}

	public boolean isTraceEnabled(String[] className, Session sessionObject) {
		return getLogLevel(className, sessionObject).includes(LogLevel.TRACE);
	}

	public boolean isWarnEnabled(String[] className, Session sessionObject) {
		return getLogLevel(className, sessionObject).includes(LogLevel.WARNING);
	}

	public Log getLog(Session session) {
		if (session == null) {
			return new NoOpLog();
		}
		final Log[] logs = new Log[]{
			LogFactory.getLog("com.openexchange.logging.tracking.session."+session.getSessionID()),
			LogFactory.getLog("com.openexchange.logging.tracking.ctx."+session.getContextId()),
			LogFactory.getLog("com.openexchange.logging.tracking.uid."+session.getContextId()+"."+session.getUserId()),
			
		};
		return new Log() {

			public void debug(Object arg0) {
				for(Log l: logs) {
					l.debug(arg0);
				}
			}

			public void debug(Object arg0, Throwable arg1) {
				for(Log l: logs) {
					l.debug(arg0, arg1);
				}
			}

			public void error(Object arg0) {
				for(Log l: logs) {
					l.error(arg0);
				}
				
			}

			public void error(Object arg0, Throwable arg1) {
				for(Log l: logs) {
					l.error(arg0, arg1);
				}
				
			}

			public void fatal(Object arg0) {
				for(Log l: logs) {
					l.fatal(arg0);
				}
				
			}

			public void fatal(Object arg0, Throwable arg1) {
				for(Log l: logs) {
					l.fatal(arg0, arg1);
				}
			}

			public void info(Object arg0) {
				for(Log l: logs) {
					l.info(arg0);
				}				
			}

			public void info(Object arg0, Throwable arg1) {
				for(Log l: logs) {
					l.info(arg0, arg1);
				}	
			}
			
			public void trace(Object arg0) {
				for(Log l: logs) {
					l.trace(arg0);
				}	
			}

			public void trace(Object arg0, Throwable arg1) {
				for(Log l: logs) {
					l.trace(arg0, arg1);
				}	
			}

			public void warn(Object arg0) {
				for(Log l: logs) {
					l.warn(arg0);
				}	
			}

			public void warn(Object arg0, Throwable arg1) {
				for(Log l: logs) {
					l.warn(arg0, arg1);
				}	
			}


			public boolean isDebugEnabled() {
				return true;
			}

			public boolean isErrorEnabled() {
				return true;
			}

			public boolean isFatalEnabled() {
				return true;
			}

			public boolean isInfoEnabled() {
				return true;
			}

			public boolean isTraceEnabled() {
				return true;
			}

			public boolean isWarnEnabled() {
				return true;
			}
			
			
		};
	}
	
	private LogLevel getLogLevel(String[] className, Session sessionObject) {
		if (sessionObject == null) {
			return LogLevel.OFF;
		}
		ConcurrentMap<String, LogLevel> clazzMap = sessionLevels.get(sessionObject.getSessionID());
		LogLevel sessionLevel = getLowestLevel(clazzMap, className);

		clazzMap = cidLevels.get(Integer.valueOf(sessionObject.getContextId()));
		LogLevel cidLevel = getLowestLevel(clazzMap, className);

		LogLevel userLevel = LogLevel.OFF;
		ConcurrentMap<Integer,ConcurrentMap<String,LogLevel>> map = userLevels.get(Integer.valueOf(sessionObject.getContextId()));
		if (map != null) {
			clazzMap = map.get(Integer.valueOf(sessionObject.getUserId()));
			userLevel = getLowestLevel(clazzMap, className);
		}
		return getLowestLevel(sessionLevel, cidLevel, userLevel);
	}

	private LogLevel getLowestLevel(LogLevel...logLevel) {
		Arrays.sort(logLevel, LogLevel.getComparator());
		return logLevel[0];
	}

	private LogLevel getLowestLevel(
			ConcurrentMap<String, LogLevel> clazzMap, String[] className) {
		if (clazzMap == null) {
			return LogLevel.OFF;
		}
		int length = className.length;
		
		Set<LogLevel> allLevels = new HashSet<LogLevel>();
		
		while(length > 0) {
			StringBuilder b = new StringBuilder();
			for(int i = 0; i < length; i++) {
				b.append(className[i]).append(".");
			}
			b.setLength(b.length() - 1);
			
			LogLevel logLevel = clazzMap.get(b.toString());
			if (logLevel != null) {
				allLevels.add(logLevel);
			}
			
			length--;
		}
		if (allLevels.isEmpty()) {
			return LogLevel.FATAL;
		}
		List<LogLevel> levelList = new ArrayList<LogLevel>(allLevels);
		Collections.sort(levelList, LogLevel.getComparator());
		return levelList.get(0);
	}


	

}
