///*
// *
// *    OPEN-XCHANGE legal information
// *
// *    All intellectual property rights in the Software are protected by
// *    international copyright laws.
// *
// *
// *    In some countries OX, OX Open-Xchange, open xchange and OXtender
// *    as well as the corresponding Logos OX Open-Xchange and OX are registered
// *    trademarks of the Open-Xchange, Inc. group of companies.
// *    The use of the Logos is not covered by the GNU General Public License.
// *    Instead, you are allowed to use these Logos according to the terms and
// *    conditions of the Creative Commons License, Version 2.5, Attribution,
// *    Non-commercial, ShareAlike, and the interpretation of the term
// *    Non-commercial applicable to the aforementioned license is published
// *    on the web site http://www.open-xchange.com/EN/legal/index.html.
// *
// *    Please make sure that third-party modules and libraries are used
// *    according to their respective licenses.
// *
// *    Any modifications to this package must retain all copyright notices
// *    of the original copyright holder(s) for the original code used.
// *
// *    After any such modifications, the original and derivative code shall remain
// *    under the copyright of the copyright holder(s) and/or original author(s)per
// *    the Attribution and Assignment Agreement that can be located at
// *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
// *    given Attribution for the derivative code and a license granting use.
// *
// *     Copyright (C) 2004-2006 Open-Xchange, Inc.
// *     Mail: info@open-xchange.com
// *
// *
// *     This program is free software; you can redistribute it and/or modify it
// *     under the terms of the GNU General Public License, Version 2 as published
// *     by the Free Software Foundation.
// *
// *     This program is distributed in the hope that it will be useful, but
// *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
// *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
// *     for more details.
// *
// *     You should have received a copy of the GNU General Public License along
// *     with this program; if not, write to the Free Software Foundation, Inc., 59
// *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
// *
// */
//package com.openexchange.admin.adminConsole;
//
//import java.rmi.NotBoundException;
//import java.rmi.RemoteException;
//import java.rmi.registry.LocateRegistry;
//import java.rmi.registry.Registry;
//import java.util.Hashtable;
//import java.util.concurrent.ExecutionException;
//
//import com.openexchange.admin.dataSource.I_AdminJobExecutor;
//
///**
// * @author choeger
// *
// */
//public class JobController {
//
//	private static I_AdminJobExecutor aje = null;
//	
//	// The command to be parsed, all mentioned without the leading "--"
//	// -----------------------------------------------------------------
//	// This command prints the usage of this small tool
//	private static final String HELP_COMMAND="help";
//	// This command lists all the jobs currently queued
//	private static final String LIST_COMMAND = "list";
//	// This command removed all the jobs in the queue which are finished
//	private static final String FLUSH_COMMAND = "flush";
//	// This command show the result of a single id
//	private static final String GETRESULTID_COMMAND = "getresult";
//	
//	
//	public JobController() {
//		try {
//		    Registry registry = LocateRegistry.getRegistry("localhost");
//		    aje = (I_AdminJobExecutor)registry.lookup(I_AdminJobExecutor.RMI_NAME);
//		    //aje = (I_AdminJobExecutor)Naming.lookup(I_AdminJobExecutor.RMI_NAME);
//		} catch (RemoteException e) {
//			e.printStackTrace();
//		} catch (NotBoundException e) {
//			e.printStackTrace();
//		}
//	}
//
//	private String listJobs() throws RemoteException {
//		return aje.getJobList();
//	}
//	
//	private String flushJobs() throws RemoteException {
//		aje.flush();
//		return "Job queue flushed";
//	}
//	
//	private String getResult(final int j_id) throws RemoteException, InterruptedException, ExecutionException {
//		return aje.getResult(j_id).toString();
//	}
//	
//	private void printUsage() {
//		System.out.println(
//				"The following options are available for this tool:\n"+
//				"	--" + HELP_COMMAND + "			the command to print this text\n"+
//				"	--" + LIST_COMMAND + "			shows a list of jobs which are currently in the\n"+
//									"				queue\n"+
//				"	--" + FLUSH_COMMAND + "			delete all jobs from the queue which are finished\n"+
//				"	--" + GETRESULTID_COMMAND + "=<id>	gets the result of the specified id");
//	}
//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		JobController jobc = new JobController();
//		boolean standard = false;
//
//		if (AdminConsoleTools.inputcontains(args, HELP_COMMAND)) {
//			jobc.printUsage();
//		} else if (AdminConsoleTools.inputcontains(args, LIST_COMMAND)) {
//			standard = true;
//		} else if (AdminConsoleTools.inputcontains(args, FLUSH_COMMAND)) {
//			try {
//				System.out.println(jobc.flushJobs());
//			} catch (RemoteException e) {
//				e.printStackTrace();
//			}
//		} else if (AdminConsoleTools.inputcontains(args, GETRESULTID_COMMAND)) {
//			Hashtable<String, Comparable> id = AdminConsoleTools.parseInput(args, GETRESULTID_COMMAND);
//			if (id.get(GETRESULTID_COMMAND) != null) {
//				try {
//					int j_id = Integer.parseInt(id.get(GETRESULTID_COMMAND).toString());
//					System.out.println(jobc.getResult(j_id));
//				} catch (RemoteException e) {
//					e.printStackTrace();
//				} catch (NumberFormatException e) {
//					System.out.println("The char after " + GETRESULTID_COMMAND + " is not a valid number.");
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				} catch (ExecutionException e) {
//					e.printStackTrace();
//				}
//				
//			} else {
//				System.out.println("No ID number given.");
//			}
//		} else {
//			standard = true;
//		}
//		
//		if (standard) {
//			// The default case
//			try {
//				System.out.println(jobc.listJobs());
//			} catch (RemoteException e) {
//				e.printStackTrace();
//			}
//		}
//	
//	}
//
//}
