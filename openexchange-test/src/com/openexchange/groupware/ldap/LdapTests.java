package com.openexchange.groupware.ldap;

import java.io.File;
import java.io.FileInputStream;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import com.openexchange.server.ComfireConfig;
import com.openexchange.server.DBPool;

import junit.framework.Test;
import junit.framework.TestSuite;

public class LdapTests {
	
	private static boolean init = false;
	
	public static Properties p;
	
	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.out.println("Usage: LdapTest properties");
			System.exit(1);
		}
		
		p = new Properties();
		p.load(new FileInputStream(args[0]));
		
		String type = p.getProperty("implementationType");
		if ("database".equals(type)) {
			initDB();
		} else if ("ldap".equals(type)) {
			initLdap();
		} else {
			System.exit(1);
		}
		
		junit.textui.TestRunner.run(LdapTests.suite());
	}
	
	public static void init() throws Exception {
		if (!init) {
			p = new Properties();
			p.load(new FileInputStream(System.getProperty("openexchange-test.propfile").toString()));
			
			String type = p.getProperty("implementationType");
			
			if ("database".equals(type)) {
				initDB();
			} else if ("ldap".equals(type)) {
				initLdap();
			} else {
				System.exit(1);
			}
			
			init = true;
		}
	}
	
	private static void initLdap() throws NamingException {
		ComfireConfig.properties = new Properties();
		ComfireConfig.properties.put("LDAPPROPERTIES", p.get("ldap.properties.file"));
		ComfireConfig.properties.put("CONFIGPATH", p.get("ldap.config.path"));
		
		String host = (String) p.get("host");
		String baseDN = (String) p.get("baseDN");
		//String port = (String) p.get("port");
		boolean ssl = Boolean.valueOf((String) p.get("ssl")).booleanValue();
		
		Hashtable env = new Hashtable();
		if (ssl) {
			env.put("java.naming.ldap.factory.socket", "com.openexchange.tools.ssl.TrustAllSSLSocketFactory");
		}
		DirContext ctx = new InitialDirContext(env);
		String protocol = ssl ? "ldaps://" : "ldap://";
		Attributes attrs = ctx.getAttributes(protocol+host, new String[]{"supportedSASLMechanisms"});
		System.out.println(attrs);
		
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, protocol+host+"/"+baseDN);
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		LdapContext context = new InitialLdapContext(env, null);
		LdapConnectionPooling.setSingleContext(context);
	}
	
	private static void initDB() {
		ComfireConfig.properties = new Properties();
		ComfireConfig.properties.put("LDAPPROPERTIES", p.get("ldap.properties.file"));
		String path = p.getProperty("ldap.config.path");
		new ComfireConfig().loadServerConf(path + File.separator + "server.conf");
		new DBPool(0, 0);
	}
	
	public static Test suite() {
		TestSuite suite = new TestSuite(
			"Test for com.openexchange.groupware.ldap.test");
		//$JUnit-BEGIN$
		suite.addTestSuite(AuthenticationSupportTest.class);
		suite.addTestSuite(UserGroupHandleTest.class);
		suite.addTestSuite(MailSupportTest.class);
		suite.addTestSuite(ResourcesHandleTest.class);
		//$JUnit-END$
		return suite;
	}
	
}
