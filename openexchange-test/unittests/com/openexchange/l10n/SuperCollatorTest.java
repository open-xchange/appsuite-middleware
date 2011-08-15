package com.openexchange.l10n;

import junit.framework.TestCase;

public class SuperCollatorTest extends TestCase {

	public void testMustFindZhCn(){
		SuperCollator collator = SuperCollator.getByJavaLocale("zh_CN");
		assertNotNull(collator);
		assertNotSame(SuperCollator.DEFAULT, collator);
	}
	public void testMustFindZhCn_(){
		SuperCollator collator = SuperCollator.getByJavaLocale("zh_CN_");
		assertNotNull(collator);
		assertNotSame(SuperCollator.DEFAULT, collator);
	}
}
