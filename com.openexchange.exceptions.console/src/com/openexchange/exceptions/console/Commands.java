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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.List;
import java.util.Set;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import com.openexchange.exceptions.ComponentRegistry;
import com.openexchange.exceptions.ErrorMessage;
import com.openexchange.exceptions.Exceptions;
import com.openexchange.exceptions.StringComponent;
import com.openexchange.groupware.Component;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class Commands implements CommandProvider {

    private final ComponentRegistry components;

    public Commands(final ComponentRegistry components) {
        this.components = components;
    }

    public Object _listComponents(final CommandInterpreter intp) {
        for(final Component component : components.getComponents()) {
            final StringBuilder line = new StringBuilder();
            line.append(component).append(" registered by ").append(components.getExceptionsForComponent(component).getApplicationId());
            line.append("\n");
            intp.print(line);
        }
        return null;
    }

    public Object _listApplications(final CommandInterpreter intp) {
        for(final String applicationId : components.getApplicationIds()) {
            final StringBuilder line = new StringBuilder();
            line.append(applicationId).append(": [");
            for(final Exceptions exceptions : components.getExceptionsForApplication(applicationId)) {
                line.append(exceptions.getComponent()).append(", ");
            }
            line.setLength(line.length()-2);
            line.append("]");
            line.append("\n");
            intp.print(line);
        }
        return null;
    }

    public Object _listErrorMessages(final CommandInterpreter intp) {
        final String componentOrApplicationId = intp.nextArgument();
        if (componentOrApplicationId == null) {
            listAllErrors(intp);
            return null;
        }
        final Exceptions exceptions = components.getExceptionsForComponent(new StringComponent(componentOrApplicationId));
        if (exceptions != null) {
            listErrorMessages(exceptions, intp);
            return null;
        }

        final List<Exceptions<?>> exceptionList = components.getExceptionsForApplication(componentOrApplicationId);
        if(null == exceptionList || exceptionList.isEmpty()) {
            intp.print("Could not find error messages for component or applicationId: "+componentOrApplicationId);
            return null;
        }

        for(final Exceptions e : exceptionList) {
            listErrorMessages(e, intp);
        }
        return null;
    }

    private void listAllErrors(final CommandInterpreter intp) {
        for(final Component component : components.getComponents()) {
            final Exceptions exceptions = components.getExceptionsForComponent(component);
            listErrorMessages(exceptions, intp);
        }
    }

    private void listErrorMessages(final Exceptions exceptions, final CommandInterpreter intp) {
        intp.print(exceptions.getApplicationId()+" "+exceptions.getComponent()+" : \n\t");
        for (final ErrorMessage error : (Set<ErrorMessage>)exceptions.getMessages()) {
            final StringBuilder line = new StringBuilder("\t");
            appendError(line, error);
            line.append("\n\t");
            intp.print(line);
        }
    }

    private void appendError(final StringBuilder line, final ErrorMessage error) {
        line.append(error.getComponent()).append("-").append(error.getDetailNumber()).append(" ").append(error.getMessage()).append(" -- ").append(error.getHelp());
    }

    public Object _dumpErrorsToCSV(final CommandInterpreter intp) {
        final String filename = intp.nextArgument();
        if (filename == null) {
            intp.print("Please provide a filename to dump the codes into");
            return null;
        }
        final File file = new File(filename);
        if (file.exists() && !file.delete()) {
            // File deletion failed
            intp.printStackTrace(new Throwable(MessageFormat.format("File \"{0}\" could not be deleted.", file.getPath())));
            return null;
        }
        try {
            file.createNewFile();
        } catch (final IOException e) {
            intp.printStackTrace(e);
            return null;
        }
        PrintWriter out = null;
        try {
            out = new PrintWriter(new FileWriter(file));
            for(final Component component : components.getComponents()) {
                final Exceptions exceptions = components.getExceptionsForComponent(component);
                intp.print("Dumping component "+exceptions.getComponent()+" to "+file+"\n");
                exportErrorMessages(exceptions, out);
            }
        } catch (final IOException e) {
            intp.printStackTrace(e);
        } finally {
            if (null != out) {
                out.close();
            }
        }

        return null;
    }

    private void exportErrorMessages(final Exceptions exceptions, final PrintWriter out) {
        final Component component = exceptions.getComponent();
        final String componentString = component.getAbbreviation();
        final String applicationId = exceptions.getApplicationId();
        for (final ErrorMessage error : (Set<ErrorMessage>)exceptions.getMessages()) {
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

    public Object _showMessage(final CommandInterpreter intp) {
        final String component = intp.nextArgument();
        if (component == null) {
            intp.print("Please provide a component in the query.");
            return null;
        }
        final String detailNumberS = intp.nextArgument();
        if (detailNumberS == null) {
            intp.print("Please provide a detailNumber in the query.");
            return null;
        }
        final int detailNumber = Integer.parseInt(detailNumberS);
        final Exceptions exceptions = components.getExceptionsForComponent(new StringComponent(component));
        if(exceptions == null) {
            intp.print("Could not find registration for component "+component);
            return null;
        }
        final ErrorMessage errorMessage = exceptions.findMessage(detailNumber);
        if(errorMessage == null) {
            intp.print("Could not find errorMessage "+component+"-"+detailNumber);
            return null;
        }
        final StringBuilder line = new StringBuilder();
        appendError(line, errorMessage);
        intp.print(line);
        return null;
    }


    public String getHelp() {
        final StringBuilder help = new StringBuilder();
        help.append("--- Open-Xchange Component Registy ---\n\t");
        help.append("listComponents - Lists all components registered in the registry\n\t");
        help.append("listApplications - Lists all applicationIds registered in the registry\n\t");
        help.append("listErrorMessages [component | applicationId] - Lists all error messages declared for a component or application. Omit the argument to see all error messges.\n\t");
        help.append("showMessage [component] [detailNumber] - Shows the error message for the given component and detailNumber.\n\t");
        help.append("dumpErrorsToCSV [filename] - Dumps all error messages to a .csv file specified by filename. Overwrites the file.");

        return help.toString();
    }
}
