package com.openexchange.subscribe.crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.facebook.api.FacebookException;
import com.facebook.api.FacebookJaxbRestClient;
import com.facebook.api.ProfileField;
import com.facebook.api.schema.FriendsGetResponse;
import com.facebook.api.schema.Location;
import com.facebook.api.schema.User;
import com.facebook.api.schema.UsersGetInfoResponse;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.openexchange.groupware.container.Contact;
import com.openexchange.subscribe.SubscriptionException;
import com.openexchange.tools.versit.converter.ConverterException;
import com.openexchange.tools.versit.converter.OXContainerConverter;

public class FacebookAPIStep extends AbstractStep implements Step<Contact[], Object>{
	
	private Contact[] contactObjectsArray;
	private static String API_KEY = "d36ebc9e274a89e3bd0c239cea4acb48";
	private static String SECRET = "903e8006dbad9204bb74c26eb3ca2310";
	String url, username, password, actionOfLoginForm, nameOfUserField, nameOfPasswordField, linkAvailableAfterLogin;
	
	public FacebookAPIStep(){
		
	}
	
	public FacebookAPIStep(String description, String url, String username, String password, String actionOfLoginForm, String nameOfUserField, String nameOfPasswordField, String linkAvailableAfterLogin){
		this.description = description;
		this.url = url;
		this.username = username;
		this.password = password;
		this.actionOfLoginForm = actionOfLoginForm;
		this.nameOfUserField = nameOfUserField;
		this.nameOfPasswordField = nameOfPasswordField;
		this.linkAvailableAfterLogin = linkAvailableAfterLogin;
		//System.out.println("***** action in constructor : " + actionOfLoginForm);
	}
	
	public void execute(WebClient webClient) throws SubscriptionException {
		contactObjectsArray = new Contact[0];
		ArrayList<Contact> contactObjects= new ArrayList<Contact>();
		
		try {
			// Create the client instance
			FacebookJaxbRestClient client = new FacebookJaxbRestClient(API_KEY, SECRET);
			
	
			// first, we need to get an auth-token to log in with
			String token = client.auth_createToken();
	
			// Build the authentication URL for the user to fill out
			String url = "http://www.facebook.com/login.php?api_key=" + API_KEY + "&v=1.0" + "&auth_token=" + token;
			//System.out.println("***** Url for login : " + url);
			//System.out.println("***** action : " + actionOfLoginForm);
			// open browser for user to log in
			LoginPageByFormActionStep step = new LoginPageByFormActionStep(
					description,
					url,
					username,
					password,
					actionOfLoginForm,
					nameOfUserField,
					nameOfPasswordField,
					linkAvailableAfterLogin);
			step.execute(webClient);
			HtmlPage pageAfterLogin = step.getOutput();
			webClient.closeAllWindows();
			
			// fetch session key
			String session = client.auth_getSession(token);
			//System.out.println("***** Session key is " + session);
	
			// keep track of the logged in user id
			Long userId = client.users_getLoggedInUser();
			//System.out.println("***** Fetching friends for user " + userId);
	
			// Get friends list
			client.friends_get();
			FriendsGetResponse response = (FriendsGetResponse) client.getResponsePOJO();
			List<Long> friends = response.getUid();
	
			// Go fetch the information for the user list of user ids			
			client.users_getInfo(friends, EnumSet.of(
					ProfileField.NAME, 
					ProfileField.FIRST_NAME, 
					ProfileField.LAST_NAME,
					ProfileField.BIRTHDAY,
					ProfileField.HOMETOWN_LOCATION,
					ProfileField.PIC));
//			System.out.println("***** response format : " + client.getResponseFormat());
//			System.out.println("***** raw response : " + client.getRawResponse());
//			System.out.println("***** response object type+ " + client.getResponsePOJO());
			UsersGetInfoResponse userResponse = (UsersGetInfoResponse) client.getResponsePOJO();
			List<User> users = userResponse.getUser();
			
			//insert the information for each user into an ox contact
			for (User user : users) {
				Contact contact = new Contact();
				Location location = user.getHometownLocation().getValue();
				//System.out.println("Username : " + user.getName());
				contact.setDisplayName(user.getName());
				//System.out.println("First Name : " + user.getFirstName());
				contact.setGivenName(user.getFirstName());
				//System.out.println("Last Name : " + user.getLastName());
				contact.setSurName(user.getLastName());
				//System.out.println("Birthday : " + user.getBirthday().getValue());
				if (user.getBirthday() != null){
					Calendar calendar = Calendar.getInstance();
					String birthdayString = user.getBirthday().getValue();
					Pattern pattern = Pattern.compile("([a-zA-Z]*)([\\s])([0-9]{1,2})([,]{1}[\\s]{1})([0-9]{4})");
					Matcher matcher = pattern.matcher(birthdayString);
					if (matcher.matches()){
						//only set the contact«s birthday if at least day and month are available
						if (matcher.groupCount()>=3){
							int month = 0;
							int day = 0;
							//set the year to the current year in case it is not available
							int year = calendar.get(Calendar.YEAR);
							//set the day
							day = Integer.valueOf(matcher.group(3));																					
							//set the month
							if (matcher.group(1).equals("January")) month = calendar.JANUARY;
							else if (matcher.group(1).equals("February")) month = calendar.FEBRUARY;
							else if (matcher.group(1).equals("March")) month = calendar.MARCH;
							else if (matcher.group(1).equals("April")) month = calendar.APRIL;
							else if (matcher.group(1).equals("May")) month = calendar.MAY;
							else if (matcher.group(1).equals("June")) month = calendar.JUNE;
							else if (matcher.group(1).equals("July")) month = calendar.JULY;
							else if (matcher.group(1).equals("August")) month = calendar.AUGUST;
							else if (matcher.group(1).equals("September")) month = calendar.SEPTEMBER;
							else if (matcher.group(1).equals("October")) month = calendar.OCTOBER;
							else if (matcher.group(1).equals("November")) month = calendar.NOVEMBER;
							else if (matcher.group(1).equals("December")) month = calendar.DECEMBER;
							
							//set the year
							if (matcher.groupCount()==5) year = Integer.valueOf(matcher.group(5)); 
							
							calendar.set(year, month, day);
							
							contact.setBirthday(calendar.getTime());
						}
					}	
					
				}
				if (location != null){
					System.out.println("Hometown : " + location.getStreet() +", "+ location.getZip() +", "+ location.getCity() +", "+ location.getState() +", "+ location.getCountry());
					if (location.getStreet() != null && !location.getStreet().equals("null")) contact.setStreetHome(location.getStreet());
					if (location.getZip() != null && location.getZip() !=0) contact.setPostalCodeHome(Integer.toString(location.getZip()));
					if (location.getCity() != null && !location.getCity().equals("null")) contact.setCityHome(location.getCity());
					if (location.getState() != null && !location.getState().equals("null")) contact.setStateHome(location.getState());
					if (location.getCountry() != null && !location.getCountry().equals("null")) contact.setCountryHome(location.getCountry());
				}
				//System.out.println("***** Picture url : " + user.getPic().getValue());
				//TODO: download picture
				//add the image from a url to the contact
    			if (user.getPic() != null){
    				OXContainerConverter.loadImageFromURL(contact, user.getPic().getValue());
    			}	
				contactObjects.add(contact);
			}
		} catch (FacebookException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SubscriptionException e) {
			e.printStackTrace();
	} catch (ConverterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		executedSuccessfully = true;
		contactObjectsArray = new Contact[contactObjects.size()];
	    for (int i=0; i<contactObjectsArray.length && i< contactObjects.size(); i++){
	    	contactObjectsArray[i] = contactObjects.get(i);
	    }
	}

	public String inputType() {
		return null;
	}

	public String outputType() {
		return LIST_OF_CONTACT_OBJECTS;
	}

	public Contact[] getOutput() {
		return contactObjectsArray;
	}

	public void setInput(Object input) {
		// TODO Auto-generated method stub
		
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getActionOfLoginForm() {
		return actionOfLoginForm;
	}

	public void setActionOfLoginForm(String actionOfLoginForm) {
		this.actionOfLoginForm = actionOfLoginForm;
	}

	public String getNameOfUserField() {
		return nameOfUserField;
	}

	public void setNameOfUserField(String nameOfUserField) {
		this.nameOfUserField = nameOfUserField;
	}

	public String getNameOfPasswordField() {
		return nameOfPasswordField;
	}

	public void setNameOfPasswordField(String nameOfPasswordField) {
		this.nameOfPasswordField = nameOfPasswordField;
	}

	public String getLinkAvailableAfterLogin() {
		return linkAvailableAfterLogin;
	}

	public void setLinkAvailableAfterLogin(String linkAvailableAfterLogin) {
		this.linkAvailableAfterLogin = linkAvailableAfterLogin;
	}

}
