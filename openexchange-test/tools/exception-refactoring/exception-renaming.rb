# This tool renames all exceptions in the com.openexchange.* namespace
# to OXException. It also removes their imports and adds an import for
# the new class to the top of the imports.

require 'find'

#hack: starting with OXException in the list, because that should be removed from the imports, since it will be added afterwards
full_names = ["com.openexchange.exception.OXException"]
short_names = [] 
excluded_names = [
	"com.openexchange.calendar.recurrence.RecurringException", 
	"com.openexchange.tools.versit.converter.ConverterException",
	"com.openexchange.tools.versit.VersitException",
	"com.openexchange.webdav.protocol.WebdavProtocolException"]
files_to_change = []

# first pass: find all exceptions used
Find.find(ARGV[0]) do |filename|
	next unless File.exists?(filename) && File.file?(filename) && filename.end_with?(".java")
	
	content = "\n" + File.read(filename)
	next unless content =~ /Exception/

 	lines = File.readlines(filename)
	lines.each do |line|
		next if line =~ /import com\.openexchange\.exception\.OXException;/
		next unless line =~ /import (com\.openexchange\..+?Exception);/
		next if (full_names+excluded_names).include? $1
		full_names << $1
		short_name = $1.split(".")[-1]
		short_names <<  short_name unless short_names.include? short_name
	end
	files_to_change << filename
end

#sort by length, longest first, to make sure no subsequences get replaced first:
full_names = full_names.sort_by{|elem| elem.length}.reverse 
short_names = short_names.sort_by{|elem| elem.length}.reverse

# second pass: Replace exceptions with new one
files_to_change.each do |filename|
	content = File.read(filename)
	File.open(filename,"w") do |fh|
		full_names.each {|old| content.gsub! /import\s+#{old}.*?\n/ , "" }
		short_names.each {|old| content.gsub! /([\(\[\{\.\s,])#{old}([\)\]\}\.\s,\{])/ , "\\1OXException\\2"} 
		content.gsub! /\n(package .+?)\n+/ , "\n\\1\n\nimport com.openexchange.exception.OXException;\n" 
		fh.puts(content)
	end
end

puts "Exceptions found:\n#{full_names.join("\n")}\n"
#puts "Classes changed:\n#{classes_changed.sort.join("\n")}\n"
puts "#{full_names.size } exceptions found, #{files_to_change.size} classes changed."
