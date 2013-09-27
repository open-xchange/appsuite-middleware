package org.quickcached;

import org.quickserver.net.server.*;
import org.quickserver.net.ServerHook;

import java.io.*;

import org.quickserver.util.logging.*;
import java.util.logging.*;

public class SetupLoggingHook implements ServerHook {
	private QuickServer quickserver;

	private static boolean init;
	private static boolean makeLogFile;

	public String info() {
		return "Init Server Hook to setup logging.";
	}

	public void initHook(QuickServer quickserver) {
		this.quickserver = quickserver;
	}

	public boolean handleEvent(int event) {
		if(event==ServerHook.PRE_STARTUP) {
			if(init==false) {
				init = true;
			} else {
				return false;
			}
			
			Logger logger = null;
			FileHandler txtLog = null;


			try	{
				logger = Logger.getLogger("");
				logger.setLevel(Level.FINEST);

				logger = Logger.getLogger("com.whirlycott");
				logger.setLevel(Level.WARNING);

				logger = Logger.getLogger("org.quickcached");
				logger.setLevel(Level.FINEST);

				if(isMakeLogFile()) {
					txtLog = new FileHandler("log/QuickCached_"+quickserver.getPort()+"_%u%g.txt",
						1024*1024, 100, true);
					txtLog.setLevel(Level.FINEST);
					txtLog.setFormatter(new SimpleTextFormatter());
					logger.addHandler(txtLog);
				} else {
					logger.setLevel(Level.WARNING);
				}

				quickserver.setAppLogger(logger); //img

				return true;
			} catch(IOException e){
				System.err.println("Could not create txtLog FileHandler : "+e);
			}
		}

		return false;
	}

	public static boolean isMakeLogFile() {
		return makeLogFile;
	}

	public static void setMakeLogFile(boolean flag) {
		makeLogFile = flag;
	}
}
