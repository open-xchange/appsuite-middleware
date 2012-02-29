require 'rubygems'
require 'hpricot'
require 'base64'
require 'open-uri'
require 'net/smtp'
require 'net/http'
require 'cgi'
require 'sinatra'

#
# settings
#
PIVOTAL_USERNAME = Base64.decode64("dG9iaWFzLnByaW56QG9wZW4teGNoYW5nZS5jb20=")
PIVOTAL_PASSWORD = Base64.decode64("dHlnZ2Vy")
MAIL_USERNAME = Base64.decode64("dG9iaWFzLnByaW56QG9wZW4teGNoYW5nZS5jb20=")
MAIL_PASSWORD = Base64.decode64("dHlnZ2Vy")

def debug(str)
#  puts str
end

#
# classes
#
class PivotalConnection
  attr_accessor :token
  
  def initialize(username, password)
    open("https://www.pivotaltracker.com/services/v3/tokens/active", :http_basic_authentication=>[username, password]) do |handler|
      content = handler.read
      content =~ /<guid>(.+?)<\/guid>/
      @token = $1
    end
  end
  
  def set_labels(labels, url)
    debug "Setting labels '#{labels} for #{url}"
    uri = URI(url+"?story[labels]=#{CGI::escape(labels)}")
    req = Net::HTTP::Put.new(uri.request_uri)
    req['X-TrackerToken'] = @token
    req['Content-Length'] = 0
    res = Net::HTTP.start(uri.host, uri.port) {|http|
      http.request(req)
    }
  end
  
  def get_story(url)
    uri = URI(url)
    req = Net::HTTP::Get.new(uri.request_uri)
    req['X-TrackerToken'] = @token
    res = Net::HTTP.start(uri.host, uri.port) {|http|
      http.request(req)
    }
    return Hpricot(res.body)
  end
  
end


class MailConnection
  attr_accessor :login, :password
  
  def initialize(login, password)
    @login = login
    @password = password
  end
  
  def send_qa_message(title,link)
    debug "Sending message about '#{title}'"
    recipient = "qa@open-xchange.com"
    message = <<-MESSAGE_END
From: Pivotal Events<#{@login}>
To: QA <#{recipient}>
Subject: [Pivotal Event] Test this: #{CGI::unescapeHTML(title)}
The following user story has been marked as "accepted". The next step is to test it:
#{link}
MESSAGE_END
    
    smtp = Net::SMTP.new('smtp.open-xchange.com', 25)
    smtp.start('open-xchange.com', @login, @password, :plain) { |smtp| smtp.send_message(message, @login, [recipient])}

  end
end


class QualityEventHandler
  
  def is_responsible_for(activity)
    activity.search("//story/current_state").any?{|elem| elem.inner_html == 'accepted'}
  end
  
  def action(activity, piv_con, mail_con)
    stories = activity.search("//story")
    stories.each do |story|
      next unless story.at("current_state") != nil && story.at("current_state").inner_html == 'accepted' # Condition: Must have been changed to "accepted""
      id = story.at("id").inner_html.chomp
      description = activity.at("description").inner_html.chomp
      clickable_url = "https://www.pivotaltracker.com/story/show/#{id}"
      mail_con.send_qa_message(description,clickable_url)

      url = story.at("url").inner_html.chomp
      story = piv_con.get_story(url)
      next unless story.at("labels") == nil || !story.at("labels").inner_html.split(",").include?("qa")  # Condition: Must not yet have the label "qa"
      labels = story.at("labels") == nil ? "qa" : story.at("labels").inner_html + ",qa"
      piv_con.set_labels(labels, url) 
    end
  end
end



#
# main
#
set(:port, 80)
post '/pivotal' do
  piv_con = PivotalConnection.new( PIVOTAL_USERNAME, PIVOTAL_PASSWORD )
  mail_con = MailConnection.new( MAIL_USERNAME, MAIL_PASSWORD )
  handlers = [ QualityEventHandler.new ]

  doc = Hpricot(request.body)
  debug "Got a post request"
  handlers.each { |handler| handler.action(doc, piv_con, mail_con) if handler.is_responsible_for(doc)}
end
