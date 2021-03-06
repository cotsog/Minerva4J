package be.pielambr.minerva4j.client;

import be.pielambr.minerva4j.beans.Announcement;
import be.pielambr.minerva4j.beans.Course;
import be.pielambr.minerva4j.beans.Document;
import be.pielambr.minerva4j.exceptions.LoginFailedException;
import be.pielambr.minerva4j.parsers.AnnouncementParser;
import be.pielambr.minerva4j.parsers.CourseParser;
import be.pielambr.minerva4j.parsers.DocumentParser;
import be.pielambr.minerva4j.utility.Constants;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import jodd.http.HttpBrowser;
import jodd.http.HttpRequest;
import jodd.jerry.Jerry;
import jodd.lagarto.dom.Node;

import java.util.List;

/**
 * Created by Pieterjan Lambrecht on 15/06/2015.
 */
public class Client {

    private final HttpBrowser _browser;

    private final String _username;
    private final String _password;

    /**
     * Default constructor for the Minerva client
     * @param username Username for the user
     * @param password Password for the user
     */
    public Client(String username, String password) {
        _username = username;
        _password = password;
        _browser = new HttpBrowser();
        _browser.setKeepAlive(true);
    }

    /**
     * Closes open connections
     */
    public void close() {
        _browser.close();
    }

    /**
     * Needs to be called after constructor to login
     * @throws LoginFailedException Is thrown if login fails
     */
    public void connect() throws LoginFailedException {
        HttpRequest login = HttpRequest
                .post(Constants.LOGIN_URL)
                .form("username", _username)
                .form("password", _password);
        _browser.sendRequest(login);
        verifyLogin();
    }

    /**
     * Verifies login for the user after calling connect
     * @return Returns true if the login was correct
     * @throws LoginFailedException Is thrown if the login was incorrect
     */
    public boolean verifyLogin() throws LoginFailedException {
        // Check index page
        HttpRequest index = HttpRequest.get(Constants.INDEX_URL);
        _browser.sendRequest(index);
        // Check to see if we find a course list
        if (_browser.getHttpResponse() != null) {
            Jerry i = Jerry.jerry(_browser.getHttpResponse().body());
            Node node = i.$(Constants.COURSE_LIST).get(0);
            if (node != null) {
                return true;
            }
        }
        throw new LoginFailedException();
    }

    /**
     * Returns a list of announcements
     * @param course The course for which the announcements need to be retrieved
     * @return Returns a list of announcements
     */
    public List<Announcement> getAnnouncements(Course course) {
        return AnnouncementParser.getAnnouncements(_browser, course);
    }

    /**
     * Returns a list of courses
     * @return A list of courses for the current user
     */
    public List<Course> getCourses() {
        return CourseParser.getCourses(_browser);
    }

    /**
     * Returns a list of ducments for a given course
     * @param course The course for which the documents needs to be retrieved
     * @return A list of documents
     */
    public List<Document> getDocuments(Course course) {
        return DocumentParser.getDocuments(_browser, course);
    }

    /**
     * Returns a valid download URL for a given document
     * @param course The course in which the document is uploaded
     * @param document The document
     * @return A valid download URL for the document
     */
    public String getDocumentDownloadURL(Course course, Document document) {
        HttpRequest request = HttpRequest.get(Constants.AJAX_URL + course.getCode() + Constants.DOCUMENTS + document.getId());
        _browser.sendRequest(request);
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(_browser.getHttpResponse().bodyText());
        return element.getAsJsonObject().get("url").getAsString();
    }

}
