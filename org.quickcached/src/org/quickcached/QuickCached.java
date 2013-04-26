package org.quickcached;

import org.quickserver.net.*;
import org.quickserver.net.server.*;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.Map;
import org.apache.log4j.xml.DOMConfigurator;

public class QuickCached {
	public static final String app_version = "2.0.0";
	
    public static String version = "2.0.0";
    public static boolean DEBUG = false;
    private static final int KEY_MAX_LENGTH = 250;
	private static QuickServer quickcached;

    public static void main(String args[]) throws Exception {
        int i = 0;
        String arg = null;
        String value = null;

		
		if(args.length!=0) {
			while (i < args.length) {
				arg = args[i];
				i++;
				
				if(arg.startsWith("-")==false) continue;
				
				if (arg.equals("-v") || arg.equals("-vv")) {
					SetupLoggingHook.setMakeLogFile(true);

					File log = new File("./log/");

					if(log.canRead()==false) {
						boolean flag = log.mkdirs();
						if(flag==false) {
							System.out.println("Unable to create log folder!");
						}
					}
					DOMConfigurator.configure("conf/log4j_debug.xml");
				} else {
					DOMConfigurator.configure("conf/log4j.xml");
				}

				if (arg.equals("-vv")) {
					DEBUG = true;
				}
			}
		} else {
			DOMConfigurator.configure("conf/log4j.xml");
		}
		
		
        String confFile = "conf" + File.separator + "QuickCached.xml";
        Object config[] = new Object[]{confFile};

        quickcached = new QuickServer();
        quickcached.initService(config);
		
		Map configMap = quickcached.getConfig().getApplicationConfiguration();
		version = (String) configMap.get("MEMCACHED_VERSION_TO_SHOW");
		if(version==null) {
			version = "1.4.6";
		}
		

        //CLI
        //-l <ip_addr>
        //Listen on <ip_addr>; default to INDRR_ANY.
        //This is an important option to consider as there is no other way to secure the installation.
        //Binding to an internal or firewalled network interface is suggested
        //-c <num>
        //Use <num> max simultaneous connections; the default is 1024.
        //-p <num>
        //Listen on TCP port <num>, the default is port 11211.
        //-v
        //Be verbose during the event loop; print out errors and warnings.
        //-vv
        //Be even more verbose; same as -v but also print client commands and responses.

        i = 0;
        while (i < args.length) {
            arg = args[i];
			i++;
			if(i < args.length) value = args[i];

			if(arg.startsWith("-")==false) continue;

            if (arg.equals("-l")) {    
                quickcached.setBindAddr(value);
            } else if (arg.equals("-p")) {
                quickcached.setPort(Integer.parseInt(value));
            } else if (arg.equals("-c")) {
                quickcached.setMaxConnection(Integer.parseInt(value));
            } else if (arg.equals("-h")) {
                printHelp();

                return;
            } else if (arg.equals("-v") || arg.equals("-vv")) {
				//nothing here
			} else {
                //print help - TODO
                System.out.println("Error: Bad argument passed - " + arg);
				printHelp();
                return;
            }
        }

        try {
            if (quickcached != null) {
                quickcached.startServer();
            }
        } catch (AppException e) {
            System.out.println("Error starting server : " + e);
            e.printStackTrace();
        }
    }

	public static String getPID() {
		String pid = ManagementFactory.getRuntimeMXBean().getName();
		int i = pid.indexOf("@");
		pid = pid.substring(0, i);
		return pid;
	}
	
	public static int getPort() {
		return quickcached.getPort();
	}

	public static void printHelp() {
		System.out.println("QuickCached " + version);
		System.out.println("-p <num>      TCP port number to listen on (default: 11211)");
		System.out.println("-l <ip_addr>  interface to listen on (default: INADDR_ANY, all addresses)");
		System.out.println("-c <num>      max simultaneous connections");
		System.out.println("-v            verbose (print errors/warnings while in event loop). Creates logs in log folder");
		System.out.println("-vv           very verbose (also print client commands/reponses). Debut Mode");
		System.out.println("-h            print this help and exit");
	}
}
