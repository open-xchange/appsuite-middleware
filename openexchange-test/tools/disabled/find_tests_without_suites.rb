require 'find'

$list_of_test_suites = []
$used_tests = []
$all_tests = []
$current_directory = ""

$LOG = []
$TESTPATTERN = /addTestSuite\s*\(\s*(.+?)\.class/
$SUITEPATTERN = /addTest\s*\(\s*(.+?)\.suite/

def debug(string)
  puts string
end


def get_file_name(suite)
  $current_directory + suite.gsub(/\./,"/") + ".java"
end

def get_full_name_for_file(filename)
  content = IO.read(filename)
  package = content[/package\s+(.+?);/ , 1]
  name = filename[/\/(\w+?)\.java/,1]
  package + "." + name
end

def get_full_name_of_test(test, imports, package)
  #it might have a full name already
  if test =~ /^com\.openexchange/ 
    return test
  end
  
  #it might use a short name and the package name in the imports
  imports.each do |import|
    if import =~ /#{test}$/ 
      return import
    end
  end

  #it might be in the same package as the test
  package + "." + test
end


def analyze_suite(suite, new_suites)
  current_package = ""
  imports = []
  
  filename = get_file_name(suite)
  fh = File.open(filename)
  fh.each_line do |line|
    imports.push $1 if line =~ /import\s+(com\.openexchange.+?);/
    current_package = $1 if line =~ /package\s+(.+?);/
    
    #these come later in the source code, so they can access the other variables which are already set
    if line =~ $SUITEPATTERN
      suitename = get_full_name_of_test($1, imports, current_package)
      $LOG << "[Suite] Found #{suitename}"
      new_suites.push(suitename) 
    end
    if line =~ $TESTPATTERN
      testname = get_full_name_of_test($1, imports, current_package)
      $LOG << "[Test] Stored #{testname}"
      $used_tests.push(testname) 
    end
  end
end


def is_test(filename)
  return false if filename =~ /Abstract/
  return true if filename =~ /Test\.java/
  
  content = IO.read(filename)
  return false if content =~ /abstract\s+class/
  return true if content =~ /public\s+void\s+test/
  return true if content =~ /@test/
  false
end


def find_all_tests()
  Find.find($current_directory) do |file|
    next unless file =~ /\.java$/
    next unless is_test(file)
    $all_tests << get_full_name_for_file(file)
  end
end


def find_test_suites(suites)
  new_suites = []
  
  suites.each do |suite|
    analyze_suite(suite, new_suites)
  end
  
  new_suites.each do |suite|
    unless $list_of_test_suites.find {|searched_suites| searched_suites == suite}
      $list_of_test_suites << suite
      $LOG << "[Suite] Stored #{suite}"
      find_test_suites(suite)
    end
  end
  
end


def find_unused_tests()
  all = $all_tests.uniq
  used = $used_tests.uniq
  unused_tests =  all - used
  puts "#{all.size} potential tests overall, #{used.size} tests in suites, #{all.size-used.size} probably unused tests:\n" 
  unused_tests.each {|test| puts "#{test}\n" }
end



puts "Unused interface- and unit-tests\n"
puts("=" * 35)

$current_directory = "/Users/development/workspace/openexchange-test/src/"
find_test_suites("com.openexchange.test.InterfaceTests")
find_all_tests()

$current_directory = "/Users/development/workspace/openexchange-test/unittests/"
find_test_suites("com.openexchange.test.UnitTests")
find_test_suites("com.openexchange.test.I18nTests")
find_all_tests()

find_unused_tests()

# puts "\n" + $LOG.join("\n")