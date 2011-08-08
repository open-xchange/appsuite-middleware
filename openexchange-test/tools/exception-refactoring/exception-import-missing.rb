# This tool adds the import statement for OXExceptions to files
# that lack it.
# The first tool for renaming exceptions to OXException missed 
# adding the import statement to files where the package name
# was in the first line (i.e. they were missing the copyright
# statement). This one corrects that.
#
require 'find'

classes_changed = []
Find.find(ARGV[0]) do |filename|
	next unless File.exists?(filename) && File.file?(filename) && filename.end_with?(".java")
	
	content = "\n"+File.read(filename)
	next unless content =~ /OXException/
	next if content =~ /import com\.openexchange\.exception\.OXException;/
	File.open(filename,"w") do |fh|
		content.gsub! /\n(package .+?)\n+/ , "\n\\1\n\nimport com.openexchange.exception.OXException;\n" 
		fh.puts(content.strip!)
	end
	classes_changed << filename
end

puts "Classes found that missed the import statement:\n" + classes_changed.join("\n")
puts "Classes that missed the import statement: " + classes_changed.size.to_s

