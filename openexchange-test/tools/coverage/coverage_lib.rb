#!/usr/bin/ruby
require 'rubygems'
require 'hpricot'

class CoverageInformation
  attr_accessor :class_coverage, :method_coverage, :block_coverage, :line_coverage

  
  def initialize(hpricot_element = nil)
    if hpricot_element == nil
      self.class_coverage  = [0,0]
      self.method_coverage = [0,0]
      self.block_coverage  = [0,0]
      self.line_coverage   = [0,0]
    else
      self.class_coverage  = get_coverage(hpricot_element, "class")
      self.method_coverage = get_coverage(hpricot_element, "method")
      self.block_coverage  = get_coverage(hpricot_element, "block")
      self.line_coverage   = get_coverage(hpricot_element, "line")
    end
  end
  
  def to_s
    class_percentage = (class_coverage.last == 0) ? 0 : (class_coverage.first * 100.0 / class_coverage.last).round
    method_percentage = (method_coverage.last == 0) ? 0 : (method_coverage.first * 100.0 / method_coverage.last).round
    block_percentage = (block_coverage.last == 0) ? 0 : (block_coverage.first * 100.0 / block_coverage.last).round
    line_percentage = (line_coverage.last == 0) ? 0 : (line_coverage.first * 100.0 / line_coverage.last).round
    "Class coverage:  #{class_percentage}%:\t#{class_coverage[0]} / #{class_coverage[1]}\n" +
    "Method coverage: #{method_percentage}%:\t#{method_coverage[0]} / #{method_coverage[1]}\n" +
    "Block coverage:  #{block_percentage}%:\t#{block_coverage[0]} / #{block_coverage[1]}\n" +
    "Line coverage:   #{line_percentage}%:\t#{line_coverage[0]} / #{line_coverage[1]}\n"
  end
  
  def add(coverage_information)
    coverage_information.class_coverage.each_with_index{|value, i| self.class_coverage[i] += value }
    coverage_information.method_coverage.each_with_index{|value, i| self.method_coverage[i] += value }
    coverage_information.block_coverage.each_with_index{|value, i| self.block_coverage[i] += value }
    coverage_information.line_coverage.each_with_index{|value, i| self.line_coverage[i] += value }
  end
end


  
def debug(string)
  puts string if false
end

# extract information which files to include and to exclude from file
def extract_filter(filename)
  includes = []
  excludes = []
  File.open(filename) do |file|
    while line = file.gets
      name = line[1..-1].strip
      if line =~ /^\+/
        includes.push name
      elsif line =~ /^-/
        excludes.push name
      elsif line =~ /^#/
        print "Ignoring: #{name}\n"
      else
        print "This should not be in here: #{line}\n"
      end
    end
  end
  return [includes, excludes]
end

# convert EMMA filter information to use it here
def transform_filter(array_of_names)
  array_of_names.collect{ | elem | (elem =~ /\.\*$/) ? elem[0..-3] : elem }
end

# gets class coverage information from an hpricot-element
def get_coverage(elem,type)
  coverage = (elem/"coverage[@type=\"#{type}, %\"]").first
  return [0,0] unless coverage
  value = coverage.attributes['value']
  values = value.scan(/(\d+)%\s+\((\d+)\/(\d+)\)/).first #means: parse "98%   (49/50)" into ["98","49","50"]
  return [0,0] unless values
  values.collect{|elem| elem.to_i}[1..2] #means: parse ["98","49","50"] into [98,49,50], then discard the first value [49,50]
end

def get_name(html_elem)
  (html_elem/'[@type="name", %]').first.attributes['name']
end

# decides whether a file counts for the coverage analysis
def counts?(included, excluded, packagename, classname)
  debug("Counts called for #{packagename} / #{classname}")
  if classname =~ /Test$/ || classname =~ /Suite$/ #exclude tests
    debug("Rejecting #{classname} because its a test\n" )
    return false
  end
  if included.any?{ |name| name =~/^#{packagename}\.?#{classname}$/ }  # include files explicitly allowed
    debug("Accepting #{classname} because its explicitely named\n")
    return true 
  end
  if excluded.any?{ |name| name =~ /^#{packagename}\.?#{classname}$/ }  # exclude files explicitly forbidden
    debug("Rejecting #{classname} because its explicitely excluded\n" )
    return false 
  end
  retVal = false
  if included.any?{ |inclusion| packagename =~ /^#{inclusion}/ }  # include files whose parent package is allowed temporarily, e.g. com.openexchange.servlet
    debug("#{classname} +\n" )
    retVal = true 
  end
  if excluded.any?{ |exclusion| packagename =~ /^#{exclusion}/ }  # exclude files whose parent package is forbidden, e.g. com.openexchange.servlet.osgihelpers
    debug("#{classname} -\n")
    retVal = false
  end
  return retVal
end

def get_class_name(class_elem)
  (class_elem.attributes['name'])[0...-5]
end

def iterate_class_files(doc)
  all_packages =  doc/"package"
  package_coverage = CoverageInformation.new
  all_packages.each do |package|
    package_name = package.attributes['name']
    files = package/"srcfile"
    files.each do |srcfile|
      class_name = (srcfile.attributes['name'])[0...-5]
      yield package, srcfile
    end
  end
end