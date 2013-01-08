require 'pathname'
require 'cgi'
require 'rubygems'
require 'nokogiri'



def table(node)
	rows = node.xpath('row')

	wiki = "{|\n"
	rows.each do |row|
		wiki += "|-\n"
		row.xpath('entry').each do |entry|
			wiki += "|#{getText(entry)}\n"
		end
	end
	wiki += "|}\n"
	print wiki
end


def title(node, depth)
	title = node.xpath('title').first.content;
	depth.times { title = "=#{title}="}
	print "\n#{title}\n\n"
end


def para(node)
	node.xpath('para').each {|para| print getText(para) + "\n" }

end


def getText(node)
	return node.content.strip || ""
end






input = ARGF.read

input.each_line do |filepath|
	filepath.chomp!
  	next unless filepath =~ /CLT-context\.xml$/
  	doc = Nokogiri::XML(File.new(filepath, 'r'))
  	doc.xpath('//sect1').each do |sect1|
  		title(sect1, 1)
  		para(sect1)

  		subsections = sect1.xpath('sect2');
  		subsections.each do |sect2|
  			title(sect2, 2)
  			para(sect2)
  			sect2.xpath('informaltable/tgroup/tbody').each {|tbody| print table(tbody)}
  		end
  	end
end