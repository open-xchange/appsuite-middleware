#!/usr/bin/ruby
require 'rubygems'
require 'hpricot'
require 'test/unit'
require 'coverage_lib.rb'

class CoverageTest < Test::Unit::TestCase
  def test_coverage_should_be_determined_even_if_a_type_is_missing
    doc = Hpricot('<srcfile name="MailFolderDescription.java"><coverage type="method, %" value="67%   (2/3)"/><coverage type="block, %" value="80%   (4/5)"/><coverage type="line, %" value="86%   (6/7)"/>')
    srcfile = (doc/'srcfile').first
    ci = CoverageInformation.new( srcfile )

    assert_equal([0,0], ci.class_coverage)
    assert_equal([2,3], ci.method_coverage)
    assert_equal([4,5], ci.block_coverage)
    assert_equal([6,7], ci.line_coverage)
  end
  
  def test_coverage_should_be_added_up_correctly_even_if_a_type_is_missing
    doc1 = Hpricot('<srcfile name="MailFolderDescription.java"><coverage type="method, %" value="67%   (2/3)"/><coverage type="block, %" value="80%   (4/5)"/><coverage type="line, %" value="86%   (6/7)"/>')
    doc2 = Hpricot('<srcfile name="MailFolderDescription.java"><coverage type="method, %" value="67%   (2/3)"/><coverage type="block, %" value="80%   (4/5)"/><coverage type="line, %" value="86%   (6/7)"/>')
    srcfile1 = (doc1/'srcfile').first
    srcfile2 = (doc2/'srcfile').first

    ci1 = CoverageInformation.new( srcfile1 )
    ci2 = CoverageInformation.new( srcfile2 )
    ci1.add(ci2)
    
    assert_equal([0,0], ci1.class_coverage)
    assert_equal([4,6], ci1.method_coverage)
    assert_equal([8,10], ci1.block_coverage)
    assert_equal([12,14], ci1.line_coverage)
  end
  
  def test_counts_method_should_include_files_in_included_packages
    included = transform_filter("com.openexchange.servlet.*")
    excluded = []
    classname = "TestServlet"
    package = "com.openexchange.servlet"
    assert( counts?(included, excluded, package, classname) )
  end

  def test_counts_method_should_exclude_files_in_excluded_packages
    included = transform_filter("com.openexchange.servlet.*")
    excluded = transform_filter("com.openexchange.servlet.test.*")
    classname = "TestServlet"
    package = "com.openexchange.servlet.test"
    assert(! counts?(included, excluded, package, classname) )  
  end

  def test_should_iterate_all_files_given
    doc = Hpricot('<package name="com.openexchange.admin"><srcfile name="A.java" /><srcfile name="B.java" /><srcfile name="C.java" /></package>')
    ['A','B','C'].each do |classname1 |
      found = false
      iterate_class_files(doc) do |package_elem, class_elem|
        classname2 = get_class_name( class_elem )
        found = true if(classname2 == classname1)
      end
      assert(found)
    end
  end
  
  def test_should_iterate_and_check_files_given_according_to_filter
    doc = Hpricot('<package name="com.openexchange.admin"><srcfile name="A.java" /><srcfile name="B.java" /><srcfile name="C.java" /></package><package name="com.openexchange.badmin"><srcfile name="D.java" /><srcfile name="E.java" /><srcfile name="F.java" /></package>')
    included = transform_filter("com.openexchange.admin.*")
    excluded = transform_filter("com.openexchange.badmin.*")
    counted = []
    not_counted = []
    iterate_class_files(doc) do |package_elem, class_elem|
      if counts?(included, excluded, get_name(package_elem), get_name(class_elem))
        counted.push( get_class_name(class_elem) )
      else
        not_counted.push( get_class_name(class_elem) )
      end
    end
    assert_equal(['A','B','C'], counted)
    assert_equal(['D','E','F'], not_counted)
  end

end