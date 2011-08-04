#!/usr/bin/ruby
require 'rubygems'
require 'hpricot'
require 'coverage_lib.rb'

usage = <<EOS
Usage:
ruby coverage_target.rb [emma_xml_file] [filter_file]
EOS


unless ARGV.size == 2
  print usage
  exit
end



emma_xml_file = ARGV[0]
filter_file = ARGV[1]

#generate filters
included_modules, excluded_files = extract_filter(filter_file)
included_modules = transform_filter(included_modules)
excluded_files = transform_filter(excluded_files)

#parse xml
time_before_parsing = Time.now
doc = open(emma_xml_file) { |f| Hpricot(f) }
time_after_parsing = Time.now

#find coverage data
all_packages =  doc/"package"
package_coverage = CoverageInformation.new
all_packages.each do |package|
  package_name = package.attributes['name']
  files = package/"srcfile"
  files.each do |srcfile|
    class_name = get_class_name(srcfile)
    if counts?(included_modules, excluded_files, package_name, class_name)
      coverage = CoverageInformation.new(srcfile)
      package_coverage.add(coverage)
      percentage = (coverage.class_coverage.last == 0) ? 0 : (coverage.class_coverage.first * 100.0 / coverage.class_coverage.last).round
      print "%-85s : %4s%\n" % [package_name + "." + class_name , percentage]
    end
  end
end
time_after_analysing = Time.now

#output
print "Overall coverage after applying filters:\n\n" + package_coverage.to_s
time_after_printing = Time.now
print "\n\nTime wasted:  #{(time_after_parsing - time_before_parsing).round}s XML parsing, #{(time_after_analysing - time_after_parsing).round}s analysing\n"