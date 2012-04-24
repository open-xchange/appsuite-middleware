package com.openexchange.logging.tracking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.log.LogPropertyName.LogLevel;
import com.openexchange.session.Session;

public class GlobalTrackingConfiguration {
	
	private ConcurrentHashMap<String, ConcurrentHashMap<String, LogLevel>> sessionLevels = new ConcurrentHashMap<String, ConcurrentHashMap<String, LogLevel>>();
	
	private ConcurrentHashMap<Integer, ConcurrentHashMap<String, LogLevel>> cidLevels = new ConcurrentHashMap<Integer, ConcurrentHashMap<String, LogLevel>>();
	
	private ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, ConcurrentHashMap<String, LogLevel>>> userLevels = new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, ConcurrentHashMap<String, LogLevel>>>();
	
	public void setLogLevel(String className, String sessionId, LogLevel level) {
		ConcurrentHashMap<String, LogLevel> clazzMap = sessionLevels.putIfAbsent(sessionId, new ConcurrentHashMap<String, LogLevel>());
		if (level == null) {
			clazzMap.remove(className);
		} else {
			clazzMap.put(className, level);
		}
	}
	
	public void setLogLevel(String className, int cid, int uid, LogLevel level) {
		ConcurrentHashMap<Integer, ConcurrentHashMap<String, LogLevel>> userMap = userLevels.putIfAbsent(cid, new ConcurrentHashMap<Integer, ConcurrentHashMap<String, LogLevel>>());
		ConcurrentHashMap<String, LogLevel> clazzMap = userMap.putIfAbsent(uid, new ConcurrentHashMap<String, LogLevel>());
		if (level == null) {
			clazzMap.remove(className);
		} else {
			clazzMap.put(className, level);
		}
	}
	
	public void setLogLevel(String className, int cid, LogLevel level) {
		ConcurrentHashMap<String, LogLevel> clazzMap = cidLevels.putIfAbsent(cid, new ConcurrentHashMap<String, LogLevel>());
		if (level == null) {
			clazzMap.remove(className);
		} else {
			clazzMap.put(className, level);
		}
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

	public Log getLog(String[] className, Session session) {
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
		ConcurrentHashMap<String, LogLevel> clazzMap = sessionLevels.get(sessionObject.getSessionID());
		LogLevel sessionLevel = getLowestLevel(clazzMap, className);

		clazzMap = cidLevels.get(sessionObject.getContextId());
		LogLevel cidLevel = getLowestLevel(clazzMap, className);

		LogLevel userLevel = null;
		ConcurrentHashMap<Integer,ConcurrentHashMap<String,LogLevel>> map = userLevels.get(sessionObject.getContextId());
		if (map != null) {
			clazzMap = map.get(sessionObject.getUserId());
			userLevel = getLowestLevel(clazzMap, className);
		}
		return getLowestLevel(sessionLevel, cidLevel, userLevel);
	}

	private LogLevel getLowestLevel(LogLevel...logLevel) {
		Arrays.sort(logLevel, LogLevel.getComparator());
		return logLevel[logLevel.length-1];
	}

	private LogLevel getLowestLevel(
			ConcurrentHashMap<String, LogLevel> clazzMap, String[] className) {
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
		return levelList.get(levelList.size()-1);
	}
	


}
