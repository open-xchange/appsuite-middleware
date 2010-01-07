require 'find'

def is_ox_java_file?(filename)
  filename =~ /(com\/openexchange\/.*)\.java/
end

def make_classname(filename)
  filename =~ /(com\/openexchange\/.*)\.java/
  reduced = Regexp.last_match(1)
  reduced.gsub(/\//,".")
end

def find_disabled_test(filename)
  fh = File.open(filename)
  classname = make_classname(filename)
  fh.each_line do |line|
    if line =~ /void\s+(\w+?[tT]est[^\(]+)\(\)/
      method = Regexp.last_match(1)
      print "#{classname}##{method}\n" unless line =~ /void\s+[tT]est/
    end
  end
end

Find.find(File.dirname(__FILE__)+"/../../src") do |file|
  if is_ox_java_file?(file)
    find_disabled_test(file)
  end
end
