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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.jsieve.export;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.jsieve.SieveException;
import org.apache.jsieve.parser.generated.Node;
import org.apache.jsieve.parser.generated.ParseException;
import org.apache.jsieve.parser.generated.SieveParser;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.jsieve.commands.ActionCommand;
import com.openexchange.jsieve.commands.ActionCommand.Commands;
import com.openexchange.jsieve.commands.Command;
import com.openexchange.jsieve.commands.IfStructureCommand;
import com.openexchange.jsieve.commands.RequireCommand;
import com.openexchange.jsieve.commands.Rule;
import com.openexchange.jsieve.commands.RuleComment;
import com.openexchange.jsieve.visitors.InternalVisitor;
import com.openexchange.jsieve.visitors.Visitor;
import com.openexchange.jsieve.visitors.Visitor.OwnType;
import com.openexchange.mailfilter.Credentials;
import com.openexchange.mailfilter.exceptions.MailFilterExceptionCode;

/**
 * This class will be used to filter out special things which are not part of
 * the sieve RFC. Those are the rulenames and the flags. Furthermore you can get
 * the uncommented rules through this class.
 *
 * @author d7
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class SieveTextFilter {

    /**
     * This class hold the rules without the require command and the list of the old required extensions
     *
     * @author d7
     *
     */
    public class ClientRulesAndRequire {

        private Map<String, List<Rule>> flaggedRules;

        private final List<Rule> rules;

        private final HashSet<String> require;

        /**
         * Represents a list of client rules
         *
         * @param rules
         * @param require
         */
        public ClientRulesAndRequire(final HashSet<String> require, final List<Rule> rules) {
            this.require = require;
            this.rules = rules;
        }

        /**
         * Represents a flagged list of client rules
         *
         * @param rules
         * @param flagged
         */
        public ClientRulesAndRequire(final HashSet<String> require, final Map<String, List<Rule>> flagged) {
            this.require = require;
            flaggedRules = flagged;
            rules = new ArrayList<Rule>();
            for (List<Rule> list : flaggedRules.values()) {
                rules.addAll(list);
            }
        }

        public final List<Rule> getRules() {
            return rules;
        }

        public final HashSet<String> getRequire() {
            return require;
        }

        public final Map<String, List<Rule>> getFlaggedRules() {
            return flaggedRules;
        }
    }

    public class RuleListAndNextUid {

        private final ArrayList<Rule> rulelist;

        private final int nextuid;

        private final boolean error;

        /**
         * @param rulelist
         * @param nextuid
         */
        public RuleListAndNextUid(final ArrayList<Rule> rulelist, final int nextuid, final boolean error) {
            super();
            this.rulelist = rulelist;
            this.nextuid = nextuid;
            this.error = error;
        }

        /**
         * @return the rulelist
         */
        public final ArrayList<Rule> getRulelist() {
            return rulelist;
        }

        /**
         * @return the uid
         */
        public final int getNextuid() {
            return nextuid;
        }

        public final boolean isError() {
            return error;
        }

    }

    public class NextUidAndError {

        private final int nextuid;

        private final boolean error;

        /**
         * @param nextuid
         * @param error
         */
        public NextUidAndError(final int nextuid, final boolean error) {
            this.nextuid = nextuid;
            this.error = error;
        }

        public final int getNextuid() {
            return nextuid;
        }

        public final boolean isError() {
            return error;
        }

    }

    private static final String COMMENT_TAG = "#<!-->";

    private static final String CRLF = "\r\n";

    private static final String FIRST_LINE = "# Generated by OX Sieve Bundle on ";

    // Set this value to 0 if you don't need the first comment line inside the script
    private static final int FIRST_LINE_OFFSET = 1;

    private static final String FLAG_TAG = "## Flag: ";

    private static final String LEGAL_FLAG_CHARS = "[a-zA-Z1-9]";

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SieveTextFilter.class);

    private static final String RULENAME_TAG = "Rulename: ";

    private static final String SEPARATOR = "|";

    private static final String SEPARATOR_REGEX = "\\" + SEPARATOR;

    private static final String UNIQUE_ID = "UniqueId:";

    private static final String MATCH_STRING = "([^" + SEPARATOR_REGEX + "]*?)";

    private static final Pattern PATTERN = Pattern.compile("^" + FLAG_TAG + MATCH_STRING + SEPARATOR_REGEX + UNIQUE_ID + MATCH_STRING + SEPARATOR_REGEX + RULENAME_TAG + "(.*?)$");

    private static final Pattern REQUIRE_PATTERN = Pattern.compile("(\"[a-z0-9\\.\\-]+\")+");

    // ------------------------------------------------------------------------------------------------------------------------------ //

    private final String username;

    public SieveTextFilter(final Credentials creds) {
        this.username = creds.getRightUsername();
    }

    public SieveTextFilter(final String username) {
        this.username = username;
    }

    public static void main(String[] args) {
        SieveTextFilter stf = new SieveTextFilter("skata");
        String r = "# Generated by OX Sieve Bundle on Tue Mar 01 15:30:09 CET 2016\n" + "require [ \"date\" , \"vacation\" , \"relational\" , \"imap4flags\", \"fileinto\" ] ;\n\n"

            + "## Flag: vacation|UniqueId:3|Rulename: vacation notice" + "if allof ( currentdate :value \"ge\" \"date\" \"2016-03-02\" , currentdate :value \"le\" \"date\" \"2016-03-09\" )" + " {";
        stf.readRequire(r);
    }

    public RuleListAndNextUid readScriptFromString(final String readFileToString) throws ParseException, SieveException, OXException {
        boolean errorsinscript = false;
        // The following line strips off the first line of the script
        // final String first = readFileToString.replaceAll("^.*(\r)?\n", "");
        final String commentedlines = diffremovenotcommentedlines(kickcommentsright(readFileToString), readFileToString);

        final Node uncommented = new SieveParser(new StringReader(readFileToString)).start();
        // final List<OwnType> jjtAccept = (List<OwnType>)
        // uncommented.jjtAccept(new Visitor(), null);
        // log.debug(jjtAccept);
        final Node commented = new SieveParser(new StringReader(commentedlines)).start();
        // log.debug("\n-------------------------");
        // final List<OwnType> jjtAccept2 = (List<OwnType>)
        // commented.jjtAccept(new Visitor(), null);
        // log.debug(jjtAccept2);
        final ArrayList<RuleComment> rulenames = getRulenames(readFileToString);
        final ArrayList<Rule> rules = (ArrayList<Rule>) uncommented.jjtAccept(new InternalVisitor(), Boolean.FALSE);
        final ArrayList<Rule> rules2 = (ArrayList<Rule>) commented.jjtAccept(new InternalVisitor(), Boolean.TRUE);
        // Attention: After merging the manipulation of finalrules also
        // manipulates rules and rules2
        final ArrayList<Rule> finalrules = mergerules(rules, rules2);
        if (addRulenameToFittingCommandAndSetErrors(rulenames, finalrules, readFileToString, commentedlines)) {
            errorsinscript = true;
        }
        final NextUidAndError nextuidanderror = setPosAndMissingErrortextsAndIds(finalrules, readFileToString, commentedlines);
        if (nextuidanderror.isError()) {
            errorsinscript = true;
        }
        return new RuleListAndNextUid(finalrules, nextuidanderror.getNextuid(), errorsinscript);
    }

    /**
     * This method is used to get back the resulting sieve script
     *
     * @param clientrulesandrequire
     * @return
     * @throws SieveException
     * @deprecated Use {@link #writeback(ClientRulesAndRequire, Set)}
     */
    @Deprecated
    public String writeback(final ClientRulesAndRequire clientrulesandrequire) throws SieveException {
        return writeback(clientrulesandrequire, Collections.<String> emptySet());
    }

    /**
     * This method is used to get back the resulting sieve script
     *
     * @param clientrulesandrequire
     * @param capabilities The SIEVE capabilities
     * @return
     * @throws SieveException
     */
    public String writeback(final ClientRulesAndRequire clientrulesandrequire, final Set<String> capabilities) throws SieveException {
        final ArrayList<Rule> finalruleswithrightrequires = addRightRequires(clientrulesandrequire, capabilities);
        addLines(finalruleswithrightrequires);
        final ArrayList<Rule> noncommented = new ArrayList<Rule>();
        final ArrayList<Rule> commented = new ArrayList<Rule>();
        splitRules(finalruleswithrightrequires, noncommented, commented);

        // Convert the rules to jjTree form but only if they are filled
        Node noncommentednode = null;
        if (!noncommented.isEmpty()) {
            noncommentednode = RuleConverter.rulesToNodes(noncommented);
        }
        final Node commentednode = RuleConverter.rulesToNodes(commented);

        // and convert this to a writeable form
        List<OwnType> noncommentedoutput = new ArrayList<OwnType>();
        if (null != noncommentednode) {
            noncommentedoutput = (List<OwnType>) noncommentednode.jjtAccept(new Visitor(), null);
        }
        final List<OwnType> commentedoutput = (List<OwnType>) commentednode.jjtAccept(new Visitor(), null);

        return listToString(interweaving(noncommentedoutput, commentedoutput, clientrulesandrequire.getRules()));
    }

    /**
     * Writes an empty script
     * 
     * @return The empty script
     */
    public String writeEmptyScript() {
        return FIRST_LINE + new Date().toString();
    }

    /**
     * Here we have to strip off the require line because this line should not be edited by the client
     *
     * @param rules list of rules
     * @param flag The flag which should be filtered out
     * @param error If an error in any rules has occurred while reading the sieve script. This is important because if so we must keep the
     *            old requires line
     * @return
     */
    public ClientRulesAndRequire splitClientRulesAndRequire(final List<Rule> rules, final String flag, final boolean error) {
        final List<Rule> listOfRules = new ArrayList<Rule>();
        final HashSet<String> requires = new HashSet<String>();
        final Map<String, List<Rule>> flagged = new HashMap<String, List<Rule>>();
        ClientRulesAndRequire retval = new ClientRulesAndRequire(requires, rules);
        // The flag is checked here because if no flag is given we can omit some checks which increases performance
        if (!Strings.isEmpty(flag)) {
            for (final Rule rule : rules) {
                final RuleComment ruleComment = rule.getRuleComment();
                final RequireCommand requireCommand = rule.getRequireCommand();
                if (null != requireCommand && error) {
                    requires.addAll(requireCommand.getList().get(0));
                } else if (null != ruleComment && null != ruleComment.getFlags() && ruleComment.getFlags().contains(flag)) {
                    listOfRules.add(rule);
                    List<Rule> fl = flagged.get(flag);
                    if (fl == null) {
                        fl = new ArrayList<Rule>();
                    }
                    fl.add(rule);
                    flagged.put(flag, fl);
                }
            }
            if (error) {
                retval = new ClientRulesAndRequire(requires, flagged);
            } else {
                retval = new ClientRulesAndRequire(new HashSet<String>(), flagged);
            }

        } else {
            for (final Rule rule : rules) {
                final RequireCommand requireCommand = rule.getRequireCommand();
                if (null == requireCommand) {
                    listOfRules.add(rule);
                } else if (error) {
                    requires.addAll(requireCommand.getList().get(0));
                }
            }
            if (error) {
                retval = new ClientRulesAndRequire(requires, listOfRules);
            } else {
                retval = new ClientRulesAndRequire(new HashSet<String>(), listOfRules);
            }

        }
        return retval;
    }

    /**
     * This method goes through all Rules and adds a fitting line number
     *
     * @param finalrules
     */
    private void addLines(final ArrayList<Rule> finalrules) {
        // We add one to the first line offset here, so that the first real rule isn't written directly below
        // the require rule
        int linenumber = FIRST_LINE_OFFSET + 1;
        for (int i = 0; i < finalrules.size(); i++) {
            final Rule rule = finalrules.get(i);
            final RuleComment ruleComment = rule.getRuleComment();
            if (null != ruleComment) {
                LOG.debug("Added line number {} to comment {}", linenumber, ruleComment);
                ruleComment.setLine(linenumber++);
            }
            LOG.debug("Added line number {} to rule {}", linenumber, rule);
            rule.setLinenumber(linenumber);
            // Here we add one because a space between two rules looks better
            final ArrayList<Command> commands = rule.getCommands();
            if (null != commands && !commands.isEmpty()) {
                linenumber += countcommandlines(commands) + 1;
            } else {
                final String text = rule.getText();
                if (null != text) {
                    linenumber += countlines(text) + 1;
                }
            }
        }
    }

    private int countlines(final String text) {
        int start = 0;
        int number = 0;
        while (true) {
            final int indexOf = text.indexOf(CRLF, start);
            if (-1 == indexOf) {
                break;
            }
            start = indexOf + CRLF.length();
            number++;
        }
        return number;
    }

    private void addPlainTextToRule(final String wholetext, final String commentedtext, final RuleComment ruleComment, final Rule rightrule) {
        final int linenumber = rightrule.getLinenumber();
        final int endlinenumber = rightrule.getEndlinenumber();
        // Known comments aren't written to the plain text part, otherwise they will be doubled when writing back
        if (rightrule.isCommented()) {
            rightrule.setText(getRightPart(commentedtext, linenumber, endlinenumber));
        } else {
            rightrule.setText(getRightPart(wholetext, linenumber, endlinenumber));
        }
    }

    /**
     * This method removes require commands in a list of rules if there are any
     * and adds the proper require commands according to the actions used in the
     * command within
     *
     * @param clientrulesandrequire
     * @param capabilities The SIEVE capabilities
     * @return
     */
    private ArrayList<Rule> addRightRequires(final ClientRulesAndRequire clientrulesandrequire, final Set<String> capabilities) {
        final ArrayList<Rule> newruleslist = new ArrayList<Rule>();
        final Set<String> requireds = new HashSet<String>();
        for (final Rule rule : clientrulesandrequire.getRules()) {
            if (null == rule.getRequireCommand()) {
                newruleslist.add(rule);
                final ArrayList<Command> commands = rule.getCommands();
                if (!rule.isCommented() && null != commands) {
                    for (final Command command : commands) {
                        final Set<String> required = command.getRequired();
                        required.retainAll(capabilities);
                        requireds.addAll(required);
                    }
                }
            }
        }
        requireds.addAll(clientrulesandrequire.getRequire());
        if (!requireds.isEmpty()) {
            final ArrayList<ArrayList<String>> arrayList = new ArrayList<ArrayList<String>>();
            arrayList.add(new ArrayList<String>(requiredList(requireds)));
            final ArrayList<Command> commandlist = new ArrayList<Command>();
            commandlist.add(new RequireCommand(arrayList));
            newruleslist.add(0, new Rule(commandlist, FIRST_LINE_OFFSET + 1));
        }
        return newruleslist;
    }

    private static Set<String> requiredList(final Set<String> requireds) {
        if (requireds.contains("imapflags") && requireds.contains("imap4flags")) {
            // Prefer "imap4flags" if both supported
            requireds.remove("imapflags");
        }
        return requireds;
    }

    /**
     * This method adds the rulename to the fitting command. It will also take care that only the errortext and the ruletext are left, if an
     * error has occured.
     *
     * @param ruleComments All the comments which are found and must be applied to a rule
     * @param rules The rules
     * @param wholetext The whole text of the script
     * @param commentedtext The commented text of the script
     * @return true if an errortext and plaintext has been added anywhere false if not
     */
    private boolean addRulenameToFittingCommandAndSetErrors(final ArrayList<RuleComment> ruleComments, final ArrayList<Rule> rules, final String wholetext, final String commentedtext) {
        boolean erroradded = false;
        for (final RuleComment ruleComment : ruleComments) {
            int mindiff = Integer.MAX_VALUE;
            Rule rightrule = null;
            for (final Rule rule : rules) {
                final int abs = getPosDif(rule.getLinenumber() - ruleComment.getLine());
                if (abs < mindiff) {
                    mindiff = abs;
                    rightrule = rule;
                }
            }
            if (null != rightrule) {
                final String errortext = ruleComment.getErrortext();
                if (null != errortext) {
                    final String errormsg = rightrule.getErrormsg();
                    rightrule.setErrormsg(null != errormsg ? errormsg + CRLF + errortext : errortext);
                    if (handleRuleError(wholetext, commentedtext, ruleComment, rightrule)) {
                        erroradded = true;
                    }
                } else {
                    rightrule.setRuleComments(ruleComment);
                    if (handleRuleError(wholetext, commentedtext, ruleComment, rightrule)) {
                        erroradded = true;
                    }
                }
            }
        }
        return erroradded;
    }

    /**
     * This methods counts the lines which will be produced by the array of
     * commands given
     *
     * @param commands
     * @return
     */
    private int countcommandlines(final ArrayList<Command> commands) {
        int i = 0;
        for (final Command command : commands) {
            if (command instanceof IfStructureCommand) {
                i += 2;
                final IfStructureCommand ifstructure = (IfStructureCommand) command;
                i += getActionCommandSize(ifstructure.getActionCommands());
                i++;
            } else {
                i++;
            }
        }
        return i;
    }

    /**
     *
     * TODO: decribe what this method does
     *
     * @param test
     * @param first
     * @return
     */
    private String diffremovenotcommentedlines(final String test, final String first) {
        final ArrayList<String> removedcomments = stringToList(test);
        final List<String> orig = stringToList(first);
        for (int i = 0; i < orig.size(); i++) {
            final String removedcommentsline = removedcomments.get(i);
            final String origline = orig.get(i);
            if (removedcommentsline.equals(origline)) {
                removedcomments.remove(i);
                removedcomments.add(i, "");
            }
        }
        return listToString(removedcomments);
    }

    private void fillup(final List<String> retval, final int i) {
        for (int o = 0; o < i; o++) {
            retval.add("");
        }
    }

    private int getActionCommandSize(final List<ActionCommand> actioncommands) {
        int size = 0;
        if (null != actioncommands) {
            for (final ActionCommand actionCommand : actioncommands) {
                if (Commands.VACATION.equals(actionCommand.getCommand()) || Commands.ENOTIFY.equals(actionCommand.getCommand()) || Commands.PGP_ENCRYPT.equals(actionCommand.getCommand())) {
                    // The text arguments for vacation end method for enotify are the last in the list
                    final ArrayList<Object> arguments = actionCommand.getArguments();
                    final int size2 = arguments.size();
                    if (0 < size2) {
                        final ArrayList<String> object = (ArrayList<String>) arguments.get(size2 - 1);
                        size += countlines(object.get(0)) + 1;
                    }
                } else {
                    size++;
                }
            }
        }
        return size;
    }

    private int getPosDif(final int i) {
        return (i > 0) ? i : Integer.MAX_VALUE;
    }

    private String getRightPart(final String commentedtext, final int linenumber, final int endlinenumber) {
        int start = 0;
        int end = 0;
        int number = 1;
        while (number < linenumber) {
            final int indexOf = commentedtext.indexOf(CRLF, start);
            start = indexOf + CRLF.length();
            number++;
        }
        end = start;
        while (number <= endlinenumber) {
            final int indexOf = commentedtext.indexOf(CRLF, end);
            end = indexOf + CRLF.length();
            number++;
        }
        return commentedtext.substring(start, end);
    }

    private ArrayList<RuleComment> getRulenames(final String readFileToString) throws OXException {
        final ArrayList<RuleComment> ruleComments = new ArrayList<RuleComment>();
        final ArrayList<String> stringToList = stringToList(readFileToString);
        for (int i = 0; i < stringToList.size(); i++) {
            final String line = stringToList.get(i);
            final Matcher matcher = PATTERN.matcher(line);
            if (matcher.matches()) {
                final String flags = matcher.group(1);
                if (flags.length() > 0) {
                    String errortext = null;
                    final List<String> flaglist = Arrays.asList(flags.split(","));
                    for (final String flag : flaglist) {
                        final String illegal = flag.replaceAll(LEGAL_FLAG_CHARS, "");
                        if (illegal.length() > 0) {
                            final String error = "Illegal chars inside flags: \"" + illegal + "\"";
                            if (null != errortext) {
                                errortext += CRLF + error;
                            } else {
                                errortext = error;
                            }
                        }
                    }
                    if (null != errortext) {
                        ruleComments.add(new RuleComment(i + 1, errortext));
                    } else {
                        try {
                            final int uniqueid = Integer.parseInt(matcher.group(2));
                            ruleComments.add(new RuleComment(flaglist, uniqueid, matcher.group(3), i + 1));
                        } catch (final NumberFormatException e) {
                            ruleComments.add(new RuleComment(i + 1, "Unique id is no integer"));
                        }
                    }
                } else {
                    try {
                        final int uniqueid = Integer.parseInt(matcher.group(2));
                        ruleComments.add(new RuleComment(uniqueid, matcher.group(3), i + 1));
                    } catch (final NumberFormatException e) {
                        ruleComments.add(new RuleComment(i + 1, "Unique id is no integer"));
                    }
                }
            }
        }
        return ruleComments;
    }

    /**
     * @param wholetext
     * @param commentedtext
     * @param rulename
     * @param rightrule
     * @return true if an errortext and plaintext has been added false otherwise
     */
    private boolean handleRuleError(final String wholetext, final String commentedtext, final RuleComment rulename, final Rule rightrule) {
        final String errormsg = rightrule.getErrormsg();
        if (null != errormsg) {
            rightrule.setCommands(null);
            addPlainTextToRule(wholetext, commentedtext, rulename, rightrule);
            printErrorForUser(MailFilterExceptionCode.SIEVE_ERROR.create(errormsg));
            return true;
        }
        return false;
    }

    private List<String> interweaving(final List<OwnType> noncommentedoutput, final List<OwnType> commentedoutput, final List<Rule> rules) {
        final List<String> retval = new ArrayList<String>();
        retval.add(FIRST_LINE + (new Date()).toString());
        for (final OwnType owntype : noncommentedoutput) {
            final int linenumber = owntype.getLinenumber();
            final String string = owntype.getOutput().toString();
            final int size = retval.size();
            if (linenumber > size + 1) {
                fillup(retval, linenumber - (size + 1));
            }
            retval.addAll(stringToList(string));
        }
        for (final OwnType owntype : commentedoutput) {
            int linenumber = owntype.getLinenumber() - 1;
            final String string = owntype.getOutput().toString();
            final int size = retval.size();
            if (linenumber >= size) {
                fillup(retval, linenumber - (size - 1));
            }
            final List<String> stringToListComment = stringToListComment(string);
            while (null != retval.get(linenumber) && !("".equals(retval.get(linenumber)))) {
                linenumber++;
            }
            removeEmptyLines(retval, linenumber, stringToListComment.size());
            retval.addAll(linenumber, stringToListComment);
        }

        for (final Rule rule : rules) {
            final RuleComment ruleComment = rule.getRuleComment();
            final String text = rule.getText();
            if (null != text) {
                final int line = ruleComment.getLine();
                final int size = retval.size();
                if (line > size) {
                    fillup(retval, line - size);
                }
                final ArrayList<String> stringToList = stringToList(text);
                removeEmptyLines(retval, line, stringToList.size());
                retval.addAll(line, stringToList);
            }
            if (null != ruleComment) {
                final int line = ruleComment.getLine();
                final String rulename2 = ruleComment.getRulename();
                // The nth line is the n-1th position inside the array
                int index = line - 1;
                String sVal = new StringBuilder(128).append(FLAG_TAG).append(listToCommaSeparatedString(ruleComment.getFlags())).append(SEPARATOR).append(UNIQUE_ID).append(ruleComment.getUniqueid()).append(SEPARATOR).append(RULENAME_TAG).append(null == rulename2 ? "" : rulename2).toString();
                if (index < 0 || index > retval.size()) {
                    retval.add(sVal);
                } else {
                    retval.add(index, sVal);
                    searchEmptyLineAndRemove(retval, index);
                }
            }
        }

        return retval;
    }

    private void removeEmptyLines(final List<String> retval, final int line, final int linescount) {
        for (int i = 0; i < linescount; i++) {
            if (retval.size() > line + 1) {
                final String string = retval.get(line);
                if (null != string && "".equals(string)) {
                    retval.remove(line);
                }
            }
        }
    }

    /**
     * This method is used to remove the comments only at those places at which
     * they are used as comments not in multi line texts for example
     *
     * @param readFileToString
     * @return
     */
    private String kickcommentsright(final String readFileToString) {
        final StringBuilder sb = new StringBuilder(readFileToString);
        boolean dontparse = false;
        boolean nextchar = false;
        boolean newline = true;
        boolean comment = false;
        boolean commentremoved = false;
        boolean inQuotes = false;
        for (int i = 0; i < sb.length();) {
            final char c = sb.charAt(i);
            if (c == '\n') {
                newline = true;
                nextchar = false;
                comment = false;
                commentremoved = false;
                i++;
            } else if (dontparse && c == '\\') {
                //Ignore next char
                i += 2;
            } else if (!commentremoved && !comment && c == '"') {
                // The comment flag is important because otherwise
                // you could struggle about '"' in comments
                dontparse = !dontparse;
                inQuotes = !inQuotes;
                nextchar = false;
                i++;
            } else if (!commentremoved && !comment && c == 't') {
                if ("text:".equals(sb.substring(i, i + 5))) {
                    dontparse = true;
                }
                nextchar = false;
                i++;
            } else if (!dontparse && !nextchar && newline && c == '#') {
                // Only delete chars at the beginning of a line
                if ((i == 0 || sb.charAt(i - 1) == '\n') && ((i + COMMENT_TAG.length()) < sb.length()) && COMMENT_TAG.equals(sb.substring(i, i + COMMENT_TAG.length()))) {
                    sb.delete(i, i + COMMENT_TAG.length());
                }
                commentremoved = true;
                nextchar = true;
                newline = false;
            } else if (dontparse && c == '.') {
                if (sb.charAt(i - 1) == '\n' && sb.charAt(i + 1) == '\r' && !inQuotes) {
                    dontparse = false;
                }
                nextchar = false;
                i++;
            } else if (nextchar && c == '#') {
                comment = true;
                i++;
            } else {
                nextchar = false;
                i++;
            }
        }
        return sb.toString();
    }

    private String listToCommaSeparatedString(final List<String> flags) {
        if (null != flags) {
            final StringBuilder sb = new StringBuilder();
            for (final String flag : flags) {
                sb.append(flag);
                sb.append(',');
            }
            return (0 != sb.length()) ? sb.deleteCharAt(sb.length() - 1).toString() : sb.toString();
        } else {
            return "";
        }
    }

    private String listToString(final List<String> list) {
        final StringBuilder sb = new StringBuilder();
        for (final String line : list) {
            sb.append(line);
            sb.append(CRLF);
        }
        return sb.toString();
    }

    /**
     * This method takes two lists of rules and merges them together
     *
     * @param rules
     * @param rules2
     * @return
     */
    private ArrayList<Rule> mergerules(final ArrayList<Rule> rules, final ArrayList<Rule> rules2) {
        final ArrayList<Rule> retval = new ArrayList<Rule>();
        retval.addAll(rules);
        retval.addAll(rules2);
        Collections.sort(retval);
        return retval;
    }

    private void printErrorForUser(final OXException mailfilterException) {
        LOG.error("Error in mailfilter rules of user {}", this.username, mailfilterException);
    }

    /**
     * This method interates over the given list from the given line on and
     * search for the next empty line. This line will then be removed
     *
     * @param retval
     * @param line
     */
    private void searchEmptyLineAndRemove(final List<String> retval, final int line) {
        for (int i = line; i < retval.size(); i++) {
            if (0 == retval.get(i).length()) {
                retval.remove(i);
                return;
            }
        }
    }

    /**
     * @param finalrules
     * @param wholetext
     * @param commentedtext
     * @return The next uid
     */
    private NextUidAndError setPosAndMissingErrortextsAndIds(final ArrayList<Rule> finalrules, final String wholetext, final String commentedtext) {
        final List<Integer> uniqueids = new ArrayList<Integer>();
        int max = -1;
        int i = 0;
        boolean errortext = false;
        for (int o = 0; o < finalrules.size(); o++) {
            final Rule rule = finalrules.get(o);
            if (null == rule.getRequireCommand()) {
                final RuleComment ruleComment = rule.getRuleComment();
                int uniqueid = -1;
                if (null != ruleComment && -1 != (uniqueid = ruleComment.getUniqueid())) {
                    if (uniqueid > max) {
                        max = uniqueid;
                    }
                } else {
                    uniqueids.add(o);
                }
                rule.setPosition(i++);
                if (null != rule.getErrormsg() && null == rule.getText()) {
                    if (handleRuleError(wholetext, commentedtext, null, rule)) {
                        errortext = true;
                    }
                }
            }
        }
        // Now we go through all the rules which have to unique id
        for (final Integer id : uniqueids) {
            final Rule rule = finalrules.get(id);
            final RuleComment ruleComment = rule.getRuleComment();
            if (null != ruleComment) {
                ruleComment.setUniqueid(++max);
            } else {
                rule.setRuleComments(new RuleComment(++max));
            }
        }
        return new NextUidAndError(++max, errortext);
    }

    /**
     * This method splits the given list of rules into two separate lists one
     * for the commented rules and one for the uncommented
     *
     * @param totalrules
     *            A list containing all rules
     * @param noncommented
     *            The list which will contain all non-commented rules
     *            afterwards
     * @param commented
     *            The list which will contain all commented rules afterwards
     */
    private void splitRules(final ArrayList<Rule> totalrules, final ArrayList<Rule> noncommented, final ArrayList<Rule> commented) {
        // First clear the two output lists
        noncommented.clear();
        commented.clear();
        for (final Rule rule : totalrules) {
            if (rule.isCommented()) {
                commented.add(rule);
            } else {
                noncommented.add(rule);
            }
        }
    }

    private ArrayList<String> stringToList(final String string) {
        return new ArrayList<String>(Arrays.asList(string.split("\r?\n")));
    }

    private List<String> stringToListComment(final String string) {
        final ArrayList<String> retval = new ArrayList<String>();
        final String[] split = string.split("\r?\n");
        for (final String line : split) {
            retval.add(COMMENT_TAG + line);
        }
        return retval;
    }

    /**
     * Rewrites the require part of the sieve script, retaining the previous and the current require sets.
     *
     * @param writeback The writeback after the CRUD operation
     * @param oldScript The old script before the CRUD operation
     * @param sieveTextFilter The {@link SieveTextFilter}
     * @return The new script with the rewritten 'require' part
     */
    public String rewriteRequire(String writeback, String oldScript) {
        // Acquire both require lines
        Set<String> require = readRequire(oldScript);
        Set<String> writeBackRequire = readRequire(writeback);

        // Merge require sets
        Set<String> mergeRequire = new HashSet<String>();
        mergeRequire.addAll(require);
        mergeRequire.addAll(writeBackRequire);

        String[] splitWriteBack = writeback.split("\r?\n");
        // Script is empty, just return it
        if (splitWriteBack.length < 2) {
            return writeback;
        }

        // Assemble the merged require line
        String requireLine = assembleRequire(mergeRequire);

        // Inject the new requireLine to the new writeback
        String[] writeBack = injectRequire(splitWriteBack, requireLine);

        // Re-assemble the 'writeback' script from the array
        return assembleWriteBack(writeBack);
    }

    /**
     * Reads the require line from the specified script and returns it as a set with strings with '"' quotes
     *
     * @param sieveScript The sieve script to read the require line from
     * @return The set with the requires, or an empty set if no require line is present.
     */
    private Set<String> readRequire(String sieveScript) {
        String[] split = sieveScript.split("\r?\n");
        // The require part is always located in line 2 of the script, i.e. the 1st element of the array
        if (split.length < 2 || !split[1].startsWith("require")) {
            return Collections.emptySet();
        }

        String require = split[1];
        final Matcher matcher = REQUIRE_PATTERN.matcher(require);
        Set<String> requires = new HashSet<String>();
        while (matcher.find()) {
            requires.add(matcher.group());
        }
        return requires;
    }

    /**
     * Assemble the require line
     *
     * @param require The require set
     * @return The require line as a string, or an empty string if the specified set is empty
     */
    private String assembleRequire(Set<String> require) {
        if (require.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("require");
        builder.append(" [ ");
        for (String req : require) {
            builder.append(req).append(" , ");
        }
        builder.setLength(builder.length() - 2);
        builder.append("];");

        return builder.toString();
    }

    /**
     * Injects the specified require line to the specified split write back string array
     *
     * @param splitWriteBack The split write back string array
     * @param requireLine The require line
     * @return The new writeback
     */
    private String[] injectRequire(String[] splitWriteBack, final String requireLine) {
        // The require part is always located in line 2 of the script, i.e. the 1st element of the array
        String requireSlice = splitWriteBack[1];
        String[] writeBack;
        if (requireSlice.startsWith("require")) {
            // If the require line exists replace
            splitWriteBack[1] = requireLine;
            writeBack = splitWriteBack;
        } else {
            // Or add. Create a new array with size equals to the previous array + 2, to accommodate the require line plus an empty line
            writeBack = new String[splitWriteBack.length + 2];
            // The top comment line
            writeBack[0] = splitWriteBack[0];
            // The require part
            writeBack[1] = requireLine;
            // empty line
            writeBack[2] = "";
            System.arraycopy(splitWriteBack, 1, writeBack, 3, splitWriteBack.length - 1);
        }
        return writeBack;
    }

    /**
     * Re-assemble the 'writeback' script from the array
     *
     * @param writeback
     * @return
     */
    private String assembleWriteBack(String[] writeback) {
        StringBuilder builder = new StringBuilder();
        for (String swb : writeback) {
            builder.append(swb).append(CRLF);
        }
        return builder.toString();
    }
}
