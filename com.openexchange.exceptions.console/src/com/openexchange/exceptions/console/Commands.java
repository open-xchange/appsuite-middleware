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
package com.openexchange.exceptions.console;

import org.eclipse.osgi.framework.console.CommandProvider;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import com.openexchange.exceptions.ComponentRegistry;
import com.openexchange.exceptions.Exceptions;
import com.openexchange.exceptions.StringComponent;
import com.openexchange.exceptions.ErrorMessage;
import com.openexchange.groupware.Component;

import java.util.List;
import java.util.Set;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class Commands implements CommandProvider {

    private ComponentRegistry components;

    public Commands(ComponentRegistry components) {
        this.components = components;
    }

    public Object _listComponents(CommandInterpreter intp) {
        for(Component component : components.getComponents()) {
            StringBuilder line = new StringBuilder();
            line.append(component).append(" registered by ").append(components.getExceptionsForComponent(component).getApplicationId());
            line.append("\n");
            intp.print(line);
        }
        return null;
    }

    public Object _listApplications(CommandInterpreter intp) {
        for(String applicationId : components.getApplicationIds()) {
            StringBuilder line = new StringBuilder();
            line.append(applicationId).append(": [");
            for(Exceptions exceptions : components.getExceptionsForApplication(applicationId)) {
                line.append(exceptions.getComponent()).append(", ");
            }
            line.setLength(line.length()-2);
            line.append("]");
            line.append("\n");
            intp.print(line);
        }
        return null;
    }

    public Object _listErrorMessages(CommandInterpreter intp) {
        String componentOrApplicationId = intp.nextArgument();
        if (componentOrApplicationId == null) {
            listAllErrors(intp);
            return null;
        }
        Exceptions exceptions = components.getExceptionsForComponent(new StringComponent(componentOrApplicationId));
        if (exceptions != null) {
            listErrorMessages(exceptions, intp);
            return null;
        }

        List<Exceptions<?>> exceptionList = components.getExceptionsForApplication(componentOrApplicationId);
        if(null == exceptionList || exceptionList.isEmpty()) {
            intp.print("Could not find error messages for component or applicationId: "+componentOrApplicationId);
            return null;
        }

        for(Exceptions e : exceptionList) {
            listErrorMessages(e, intp);
        }
        return null;
    }

    private void listAllErrors(CommandInterpreter intp) {
        for(Component component : components.getComponents()) {
            Exceptions exceptions = components.getExceptionsForComponent(component);
            listErrorMessages(exceptions, intp);
        }
    }

    private void listErrorMessages(Exceptions exceptions, CommandInterpreter intp) {
        intp.print(exceptions.getApplicationId()+" "+exceptions.getComponent()+" : \n\t");
        for (ErrorMessage error : (Set<ErrorMessage>)exceptions.getMessages()) {
            StringBuilder line = new StringBuilder("\t");
            appendError(line, error);
            line.append("\n\t");
            intp.print(line);
        }
    }

    private void appendError(StringBuilder line, ErrorMessage error) {
        line.append(error.getComponent()).append("-").append(error.getDetailNumber()).append(" ").append(error.getMessage()).append(" -- ").append(error.getHelp());
    }

    public Object _dumpErrorsToCSV(CommandInterpreter intp) {
        String filename = intp.nextArgument();
        if (filename == null) {
            intp.print("Please provide a filename to dump the codes into");
            return null;
        }
        File file = new File(filename);
        if(file.exists()) { file.delete(); }
        try {
            file.createNewFile();
        } catch (IOException e) {
            intp.printStackTrace(e);
            return null;
        }
        PrintWriter out = null;
        try {
            out = new PrintWriter(new FileWriter(file));
            for(Component component : components.getComponents()) {
                Exceptions exceptions = components.getExceptionsForComponent(component);
                intp.print("Dumping component "+exceptions.getComponent()+" to "+file+"\n");
                exportErrorMessages(exceptions, out);
            }
        } catch (IOException e) {
            intp.printStackTrace(e);
        } finally {
            out.close();
        }

        return null;
    }

    private void exportErrorMessages(Exceptions exceptions, PrintWriter out) {
        Component component = exceptions.getComponent();
        String componentString = component.getAbbreviation();
        String applicationId = exceptions.getApplicationId();
        for (ErrorMessage error : (Set<ErrorMessage>)exceptions.getMessages()) {
            out.print(quote(componentString));
            out.print(';');
            out.print(quote(applicationId));
            out.print(';');
            out.print(quote(String.valueOf(error.getCategory().getCode())));
            out.print(';');
            out.print(quote(error.getCategory().name()));
            out.print(';');
            out.print(quote(String.valueOf(error.getDetailNumber())));
            out.print(";");
            out.print(quote(error.getMessage()));
            out.print(";");
            out.print(quote(error.getHelp()));
            out.print(";");
            out.print(quote(""));
            out.println(";");
            out.flush();
        }

    }

    private String quote(final String s) {
		if(s == null) {
			return "";
		}
		return '"'+s.replaceAll("\\\"", "\\\"")+'"';
	}

    public Object _showMessage(CommandInterpreter intp) {
        String component = intp.nextArgument();
        if (component == null) {
            intp.print("Please provide a component in the query.");
            return null;
        }
        String detailNumberS = intp.nextArgument();
        if (detailNumberS == null) {
            intp.print("Please provide a detailNumber in the query.");
            return null;
        }
        int detailNumber = Integer.valueOf(detailNumberS);
        Exceptions exceptions = components.getExceptionsForComponent(new StringComponent(component));
        if(exceptions == null) {
            intp.print("Could not find registration for component "+component);
            return null;
        }
        ErrorMessage errorMessage = exceptions.findMessage(detailNumber);
        if(errorMessage == null) {
            intp.print("Could not find errorMessage "+component+"-"+detailNumber);
            return null;
        }
        StringBuilder line = new StringBuilder();
        appendError(line, errorMessage);
        intp.print(line);
        return null;
    }


    public String getHelp() {
        StringBuilder help = new StringBuilder();
        help.append("--- Open-Xchange Component Registy ---\n\t");
        help.append("listComponents - Lists all components registered in the registry\n\t");
        help.append("listApplications - Lists all applicationIds registered in the registry\n\t");
        help.append("listErrorMessages [component | applicationId] - Lists all error messages declared for a component or application. Omit the argument to see all error messges.\n\t");
        help.append("showMessage [component] [detailNumber] - Shows the error message for the given component and detailNumber.\n\t");
        help.append("dumpErrorsToCSV [filename] - Dumps all error messages to a .csv file specified by filename. Overwrites the file.");

        return help.toString();
    }
}
