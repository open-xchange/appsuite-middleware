package com.openexchange.ajax.mail.filter;

import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.mail.filter.action.AbstractAction;
import com.openexchange.ajax.mail.filter.action.Keep;
import com.openexchange.ajax.mail.filter.comparison.IsComparison;
import com.openexchange.ajax.mail.filter.test.HeaderTest;
import com.openexchange.configuration.AJAXConfig;


public class AdminListTest extends AbstractMailFilterTest {    

    private AJAXClient userClient;
    private AJAXSession userSession;
    private AJAXClient adminClient;
    private AJAXSession adminSession;
    private Rule rule;

    public AdminListTest(String name) {
        super(name);
    }
    
    public void testUserHasAccessToOtherUsersRules() throws Exception {
        userClient = getClient();
        userSession = userClient.getSession();        

        adminClient = new AJAXClient(User.OXAdmin);
        adminSession = adminClient.getSession();
        
        // Insert new rule as user 1
        rule = new Rule();
        rule.setName("testUserHasAccessToOtherUsersRules");
        rule.setActioncmds(new AbstractAction[] { new Keep() });
        
        IsComparison isComp = new IsComparison();
        rule.setTest(new HeaderTest(isComp, new String[] { "testheader" }, new String[] { "testvalue"} ));
        
        String rid = insertRule(rule, null, userSession);
        
        // Get rules of user with admin session
        final String userImapLogin = AJAXConfig.getProperty(AJAXConfig.Property.IMAP_LOGIN);
        Rule[] rules = listRulesForUser(adminSession, userImapLogin);
        
        boolean foundRule = false;
        for (Rule r : rules) {
            if (r.getName().equals(rule.getName())) {
                foundRule = true;
                break;
            }
        }
        
        deleteRule(rid, null, userSession);
        adminClient.logout();
        
        if (!foundRule) {
            fail("Did not find rule.");
        }                
    }

}
