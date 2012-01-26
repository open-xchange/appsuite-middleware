require 'find'

$TESTPATTERN = /addTestSuite\s*\(\s*(.+?)\.class/
$SUITEPATTERN = /addTest\s*\(\s*(.+?)\.suite/


def getFilenameFor(classname)
	packages = classname.split(/\./)
	filename = packages.pop + ".java"
	candidates = Dir["#{$START_DIR}/**/#{filename}"]

	abort("Cannot find #{filename} in or below #{$START_DIR}") if(candidates.size < 1)
	return candidates.first if candidates.size == 1
	return candidates.select {|candidate| getPackageName(candidate) == packages.join(".")}.first
end


def getClassNameFor(test, imports, package)
	return test if test =~ /^com\.openexchange/   			#it might have a full name already
	imports.each { |import| return import if import =~ /#{test}$/ } #it might use a short name and the package name in the imports
	package + "." + test   						#it might be in the same package as the test
end


def getClassNameForFile(filename)
	content = IO.read(filename)
	package = content[/package\s+(.+?);/ , 1]
	name = filename[/\/(\w+?)\.java/,1]
	return package + "." + name
end


def getPackageName(filename)
	content = IO.read(filename)
	content =~ /package\s+(.+?);/
	return $1 
end


def isTest?(filename)
  content = IO.read(filename)
  return false if content =~ /abstract\s+class/
  return true if content =~ /public\s+void\s+test/
  return true if content =~ /\s+@Test/
  false
end


def isTestsuite?(filename)
  return true if (filename =~ /Suite\.java/)
  content = IO.read(filename)
  return true if (content =~ /extends\s+TestSuite/) #classist
  return true if (content =~ /extends\s+\w+Suite/) #classist
  return true if content =~ $TESTPATTERN #ducky
  return true if content =~ $SUITEPATTERN #ducky
  false
end


def getTestsCoveredBy(suitename)
	imports = []
	filename = getFilenameFor(suitename)
	current_package = getPackageName(filename)
	tests = []
	fh = File.open(filename)
	fh.each_line do |line|
		imports.push $1 if line =~ /import\s+(com\.openexchange.+?);/
    		tests.push(getClassNameFor($1, imports, current_package)) if line =~ $TESTPATTERN
	end
	return tests
end


def getSuitesRelatedTo(suitename, alreadyChecked=[])
	imports = []
	filename = getFilenameFor(suitename)
	current_package = getPackageName(filename)
	suites = [suitename]
	fh = File.open(filename)
	fh.each_line do |line|
		imports.push $1 if line =~ /import\s+(com\.openexchange.+?);/
    		suites.push(getClassNameFor($1, imports, current_package)) if line =~ $SUITEPATTERN
	end
	alreadyChecked << suitename
	suites.each do |suite|
		next if alreadyChecked.include?(suite)
		suites = (suites + getSuitesRelatedTo(suite, alreadyChecked)).uniq!
	end
	return suites
end

########
# MAIN #
########
abort("Usage: ruby find_tests_without_suites.rb [start directory] [suite 1]...[suite n]\n\nThis script searches through a given directory (first parameter) and finds all tests that\nare not linked in the test suites it is given (consecutive parameters, notation needs to\ninclude full package name, e.g. com.openexchange.test.UnitTests).") if ARGV.size < 2

$START_DIR = ARGV.shift
startSuites = ARGV.uniq


allTests = Dir["#{$START_DIR}/**/*.java"]
		.select {|filename| isTest?(filename)}
		.collect { |filename| getClassNameForFile(filename)}

coveredTestSuites = startSuites
		.collect{|suite| getSuitesRelatedTo(suite)}
		.flatten
		.uniq

coveredTests = coveredTestSuites
		.collect{|suite| getTestsCoveredBy(suite)}
		.flatten
		.uniq

lonelyTests = (allTests - coveredTests).sort

#lonelyTests.each{|testname| puts testname+"\n"}
lonelyTests.each{|testname| puts "\ttests.addTestSuite(#{testname}.class);\n"}
puts "\n#{allTests.size} tests found, of which #{lonelyTests.size} (#{100*lonelyTests.size/allTests.size}%) are not included in the given suites.\n"
