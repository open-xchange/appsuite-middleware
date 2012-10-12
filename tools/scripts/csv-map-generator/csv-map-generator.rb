# This beautiful tool takes a CSV file that contains the 
# OX test user, analyses the values and creates a mapping 
# from our internal fields to that CSV's fields
#
# Author: tobias.prinz@open-xchange.com
#
require 'csv'
require 'rubygems'
require 'java_native2ascii'
#require 'charlock_holmes'

def p(key, value)
#  print "#{key}=#{JavaNative2Ascii::native2ascii(value)}\n"
  print "#{key}=#{value}\n"
#  print "#{key.upcase}=#{key}\n"
end

filename = ARGV[0]

csv = CSV.read(filename)
ox = csv.last
alien = csv.first

## error handling
#abort "Expected to have ox (#{ox.size}) and alien data (#{alien.size}) to have the same length, what kind of CSV is this?" unless ox.size == alien.size

## actual properties
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
    weird << "[field not mapped] #{alien_name}"
  elsif (ox_name =~ /^[0-9]+$/)
    weird << "[not mappable] #{ox_name}=#{alien_name}"
  else 
    p ox_name, alien_name
  end
end

## encoding
#detection = CharlockHolmes::EncodingDetector.detect(File.read(filename))
#p 'encoding', detection[:encoding] || 'UTF-8'

## comments with problematic fields
weird.each {|problem| print "##{problem}\n"}