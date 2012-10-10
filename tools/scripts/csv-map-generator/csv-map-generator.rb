# This beautiful tool takes a CSV file that contains the 
# OX test user, analyses the values and creates a mapping 
# from our internal fields to that CSV's fields
#
# Author: tobias.prinz@open-xchange.com
#
require 'csv'
require 'pp'

def p(key, value)
  print "#{key}=#{value}\n"
end

filename = ARGV[0]

csv = CSV.read(filename)
ox = csv.last
alien = csv.first

print "Expected to have ox (#{ox.size}) and alien data (#{alien.size}) to have the same length" unless ox.size == alien.size

weird = []
ox.each_with_index do |ox_name, index|
  alien_name = alien[index]
  if (ox_name == '2002-02-02')
    p 'anniversary', alien_name
  elsif (ox_name == '2001-01-01')
    p 'birthday', alien_name
  elsif (ox_name =~/email\d@invalid/)
    p ox_name[0,6], alien_name
  elsif (!ox_name)
    weird << "[not mapped] ?=#{alien_name}"
  elsif (ox_name =~ /^[0-9]+$/)
    weird << "[not mappable] #{ox_name}=#{alien_name}"
  else 
    p ox_name, alien_name
  end
end
weird.each {|problem| print "##{problem}\n"}