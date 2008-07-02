package com.openexchange.mailfilter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.jsieve.NumberArgument;
import org.apache.jsieve.SieveException;
import org.apache.jsieve.TagArgument;
import org.apache.jsieve.parser.generated.ParseException;
import org.apache.jsieve.parser.generated.Token;
import org.junit.Test;

import com.openexchange.jsieve.SieveTextFilter;
import com.openexchange.jsieve.SieveTextFilter.ClientRulesAndRequire;
import com.openexchange.jsieve.SieveTextFilter.RuleListAndNextUid;
import com.openexchange.jsieve.commands.ActionCommand;
import com.openexchange.jsieve.commands.Command;
import com.openexchange.jsieve.commands.ElsifCommand;
import com.openexchange.jsieve.commands.RuleComment;
import com.openexchange.jsieve.commands.IfCommand;
import com.openexchange.jsieve.commands.Rule;
import com.openexchange.jsieve.commands.TestCommand;
import com.openexchange.mailfilter.ajax.exceptions.OXMailfilterException;

public class SieveParserTest {

    @Test
    public void parsertest() throws ParseException, IOException, SieveException, OXMailfilterException {
        final SieveTextFilter sieveTextFilter = new SieveTextFilter("test");
        final File file = new File("/home/d7/oxfilterscript.script");
        // final FileInputStream fileInputStream = new
        // FileInputStream(file);
        final String readFileToString = FileUtils.readFileToString(file, "UTF-8");

        final RuleListAndNextUid finalrules = sieveTextFilter.readScriptFromString(readFileToString);
        final ClientRulesAndRequire clientrulesandrequire = sieveTextFilter.splitClientRulesAndRequire(finalrules.getRulelist(), null, finalrules.isError());
        final ArrayList<Rule> rulelist = clientrulesandrequire.getRules();
        searchVacationRule(rulelist);
        sieveTextFilter.writeback(clientrulesandrequire);

        // writeback(createownrules());
    }

    private void searchVacationRule(final ArrayList<Rule> finalrules) {
        for (final Rule rule : finalrules) {
            final Command command = rule.getCommands().get(0);
            if (command instanceof ActionCommand) {
                final ActionCommand actioncommand = (ActionCommand) command;
                System.out.println(actioncommand.getArgumentToTag(":days"));
            }
        }
    }

    /**
     * A method to produce a set of rules like they are produced by a programmer
     * later on
     * 
     * @return
     * @throws SieveException
     */
    private ArrayList<Rule> createownrules() throws SieveException {
        final ArrayList<Rule> retval = new ArrayList<Rule>();
        final ArrayList<Command> commands = new ArrayList<Command>();
        final ArrayList<Command> commands2 = new ArrayList<Command>();

        final List<String> asList = Arrays.asList(new String[] { "From" });
        final List<String> asList2 = Arrays.asList(new String[] { "xyz@\\bla.de\"", "xyz2@bla.de" });
        final List<Object> asList3 = new ArrayList<Object>();
        asList3.add(createTagArg(":regex"));
        asList3.add(asList);
        asList3.add(asList2);
        final ActionCommand fileinto = new ActionCommand(ActionCommand.Commands.FILEINTO, createArrayArray("INBOX"));
        final ActionCommand stop = new ActionCommand(ActionCommand.Commands.STOP, new ArrayList<Object>());
        commands.add(createIfCommand(asList3, Arrays.asList(new ActionCommand[] { fileinto, stop })));
        commands.add(createElseIfCommand(asList3, Arrays.asList(new ActionCommand[] { fileinto, stop })));

        commands2.add(createVacationCommand());
        retval.add(new Rule(new RuleComment("testrule"), commands, false));
        retval.add(new Rule(new RuleComment("vacationrule"), commands2, false));
        return retval;
    }

    private ActionCommand createVacationCommand() throws SieveException {
        final ArrayList<Object> arrayList = new ArrayList<Object>();
        arrayList.add(createTagArg(":days"));
        arrayList.add(createNumberArg("1"));
        arrayList.add(createTagArg(":addresses"));
        arrayList.add(Arrays.asList(new String[] { "xyz@bla.de", "abc@bla.de" }));
        arrayList.add(Arrays.asList(new String[] { "Dies ist ein Test" }));
        return new ActionCommand(ActionCommand.Commands.VACATION, arrayList);
    }

    private TagArgument createTagArg(final String string) {
        final Token token = new Token();
        token.image = string;
        final TagArgument tagArgument = new TagArgument(token);
        return tagArgument;
    }

    private NumberArgument createNumberArg(final String string) {
        final Token token = new Token();
        token.image = string;
        final NumberArgument numberArgument = new NumberArgument(token);
        return numberArgument;
    }

    private ArrayList<Object> createArrayArray(String string) {
        final ArrayList<Object> retval = new ArrayList<Object>();
        final ArrayList<String> strings = new ArrayList<String>();
        strings.add(string);
        retval.add(strings);
        return retval;
    }

    private IfCommand createIfCommand(final List<Object> args, final List<ActionCommand> actioncommands) throws SieveException {
        return new IfCommand(new TestCommand(TestCommand.Commands.HEADER, args, new ArrayList<TestCommand>()), actioncommands);
    }

    private ElsifCommand createElseIfCommand(final List<Object> args, final List<ActionCommand> actioncommands) throws SieveException {
        return new ElsifCommand(new TestCommand(TestCommand.Commands.HEADER, args, new ArrayList<TestCommand>()), actioncommands);
    }

    private void output(final ArrayList<Rule> rules, final ArrayList<Rule> rules2) {
        for (final Rule rule : rules) {
            println(rule);
            println(rule.getLinenumber());
        }
        println("----------------");
        for (final Rule rule : rules2) {
            rule.setCommented(true);
            println(rule.toString());
            println(rule.getLinenumber());
        }
    }

    private void output(List<String> interweaving) {
        for (final String line : interweaving) {
            println(line);
        }
    }

    /**
     * A simple method to disable output at one place
     * 
     * @param line
     */
    private void println(Object line) {
        System.out.println(line);
    }

    // private ArrayList<Integer> nodeiteration(final Node node, final
    // ArrayList<Integer> list) {
    // final ArrayList<Integer> retval = new ArrayList<Integer>();
    // for (int i = 0; i < node.jjtGetNumChildren(); i++) {
    // retval.addAll(printNodes(node.jjtGetChild(i), list));
    // }
    // return retval;
    // }
}
