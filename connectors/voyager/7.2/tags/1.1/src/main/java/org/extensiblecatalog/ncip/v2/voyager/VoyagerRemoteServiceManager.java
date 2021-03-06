package org.extensiblecatalog.ncip.v2.voyager;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.extensiblecatalog.ncip.v2.common.ConnectorConfigurationFactory;
import org.extensiblecatalog.ncip.v2.service.AuthenticationInput;
import org.extensiblecatalog.ncip.v2.service.RemoteServiceManager;
import org.extensiblecatalog.ncip.v2.service.ToolkitException;
import org.extensiblecatalog.ncip.v2.voyager.util.ILSException;
import org.extensiblecatalog.ncip.v2.voyager.util.LDAPUtils;
import org.extensiblecatalog.ncip.v2.voyager.util.VoyagerConfiguration;
import org.extensiblecatalog.ncip.v2.voyager.util.VoyagerConstants;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;


/**
 * VoyagerRemoteServiceManager is the implementation of the back-end service
 * 
 * @author SharmilaR
 *
 */
public class VoyagerRemoteServiceManager implements  RemoteServiceManager { 
	
	/** Logger */
    static Logger log = Logger.getLogger(VoyagerRemoteServiceManager.class);
	
    private VoyagerConfiguration voyagerConfig;
    {
        try {
            voyagerConfig = (VoyagerConfiguration)ConnectorConfigurationFactory.getConfiguration();
        } catch (ToolkitException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

	HttpClient client = new HttpClient(
    	    new MultiThreadedHttpConnectionManager());

    public VoyagerRemoteServiceManager(Properties properties) {
    }
	
    /**
     * Sets up and returns a connection to the voyager database whose location and
     * driver are defined in the configuration file.
     */
    private Connection openReadDbConnection()
    {
    	Connection conn;
    	log.debug("Entering openReadDbConnection()");
    	try {
    		Class.forName("oracle.jdbc.driver.OracleDriver");
    		String url = (String) voyagerConfig.getProperty(VoyagerConstants.CONFIG_VOYAGER_DB_URL);
    		String username = (String) voyagerConfig.getProperty(VoyagerConstants.CONFIG_VOYAGER_DB_READ_ONLY_USERNAME);
    		String password = (String) voyagerConfig.getProperty(VoyagerConstants.CONFIG_VOYAGER_DB_READ_ONLY_PASSWORD);
    		conn = DriverManager.getConnection(url, username, password);
    		log.debug("Voyager DB read connection = " + conn);
    	} catch (ClassNotFoundException ce) {
    		log.error("An error occurred loading the jdbc driver.", ce);
    		return null;
    	} catch (SQLException se) {
    		log.error("An error occurred while interacting with the Voyager database.  This may have been " +  
    				"caused if the VoyagerDatabaseName was set incorrectly in the NCIP Toolkit configuration file.", se);
    		return null;
    	} 
    	return conn;
    }

    public String getBibIdForItemId(String itemId) {

    	PreparedStatement pstmt = null;
    	ResultSet rs = null;
    	String bibId = "";
    	Connection conn = openReadDbConnection();
    	try {
    		//pstmt = conn.prepareStatement("SELECT bib_id from uiudb.bib_item where uiudb.bib_item.item_id = ?");
    		pstmt = conn.prepareStatement("SELECT bib_id from bib_item where bib_item.item_id = ?");
    		pstmt.setString(1,itemId);
    		rs = pstmt.executeQuery();    
    		while(rs.next()) {
    			bibId = StringUtils.trim(rs.getString(1));
    		}
    	} catch(SQLException e) {
    		log.error("An error occurred while getting the bibliographic Id from the database.", e);
    		return null;
    	} finally {
    		if (rs != null) {
    			try {
    				rs.close();
    			} catch(SQLException e) {
    				log.error("An error occured closing the ResultSet.");                            
    			}
    		}    		
    		if(pstmt != null) {
    			try {
    				pstmt.close();
    			} catch(SQLException e) {
    				log.error("An error occured closing the Statement.");                            
    			}
    		}
    		if(conn != null) {
    			try {
    				conn.close();
    			} catch(SQLException e) {
    				log.error("An error occured closing the Statement.");                            
    			}
    		}
    	}     	
    	log.info("Returning bib Id: " + bibId);
    	return bibId;
    }
    
	/**
	 * Authenticate user
	 * 
	 * @param inputs
	 * @return
	 * @throws ILSException
	 * @throws ILSException
	 */
	public String authenticateUser(List<AuthenticationInput> inputs, String ubPrefix) {
		 
		 String username = null;
		 String password = null;
		 String ldapUsername = null;
		 String ldapPassword = null;
		 String authenticatedUserId = null;
		 
		 for (AuthenticationInput a : inputs) {
			if (a.getAuthenticationInputType().getValue().equalsIgnoreCase("Username")) {
				username = a.getAuthenticationInputData();
			} else if (a.getAuthenticationInputType().getValue().equalsIgnoreCase("Password")) {
				password = a.getAuthenticationInputData();
			} else if (a.getAuthenticationInputType().getValue().equalsIgnoreCase("LDAPUsername")) {
				ldapUsername = a.getAuthenticationInputData();
			} else if (a.getAuthenticationInputType().getValue().equalsIgnoreCase("LDAPPassword")) {
				ldapPassword = a.getAuthenticationInputData();
			}
		 }
		 
		 if (username != null && password != null) {
			 authenticatedUserId = authenticateUser(username, password, ubPrefix);
		 }		 
		 if (ldapUsername != null && ldapPassword != null) {
			 authenticatedUserId = authenticateLDAPUser(ldapUsername, ldapPassword, ubPrefix);
		 }
		 return authenticatedUserId;
	 }
	
	/**
	 *  Authenticates User before processing the request
	 *  
	 * @param username
	 * @param password
	 * @param ubPrefix
	 * @return
	 * @throws ILSException
	 */
    public String authenticateUser(String username, String password, String ubPrefix) {

    	if (log.isDebugEnabled())
    	    log.debug("Entering authenticateUser for user: " + username + ".");
 
    	// The institution ID returned by the web service assuming username was an institution ID
	    String institutionId = getPatronIdFromInstitutionIdAuthData(username, password.toLowerCase(), ubPrefix);

	    if (institutionId != null) {
    		log.info("Found institution ID " + institutionId + " from vxws with institution id");
    		return institutionId;
	    } 
	    // The web service did not authenticate the user, try again assuming barcode data
	    else { 
	    	institutionId = getPatronIdFromBarcodeAuthData(username, password.toLowerCase(), ubPrefix);
	    }
	    
	    return institutionId;
	}
    
    /**
     * Authenticates user against LDAP server
     * 
     * @param username User name of user
     * @param password Password
     * @return
     * @throws ILSException
     */
    public String authenticateLDAPUser(String username, String password, String ubPrefix) {
    	if (log.isDebugEnabled())
    	    log.debug("Entering authenticateUser for LDAP user: " + username + ".");

    	// The connection to the LDAP server
    	DirContext ldapConnection = null;
    	LDAPUtils ldapUtils = new LDAPUtils();

    	try {
    	    // Get a connection to the LDAP server
    	    ldapConnection = ldapUtils.getLDAPConnection(username, password);

    	    // Get the username attribute and start location on the LDAP server
    	    // from the configuration file
    	    String usernameAttribute = (String) voyagerConfig.getProperty(VoyagerConstants.CONFIG_EXTERNAL_LDAP_USERNAME_ATTRIBUTE);
    	    String startLocation = (String) voyagerConfig
    		    .getProperty(VoyagerConstants.CONFIG_EXTERNAL_LDAP_START);

    	    // Get the attributes associated with the user
    	    Attributes attributes = ldapConnection
    		    .getAttributes(usernameAttribute + "=" + username + ", "
    			    + startLocation);

    	    // Get the attribute containing the patron key which Voyager needs
    	    String urid = (String) attributes.get((String) voyagerConfig.getProperty(VoyagerConstants.CONFIG_EXTERNAL_LDAP_UR_ID)).get();
    	    
    	    // Get the attribute containing the last name which Voyager needs
    	    String lastName = (String)  attributes.get((String) voyagerConfig.getProperty(VoyagerConstants.CONFIG_EXTERNAL_LDAP_LAST_NAME)).get();
    	   
    	    log.info("Retrieved urid: " + urid + " and last name: " + lastName);
    	    
    	    // Authenticate against voyager db
    	    return getPatronIdFromInstitutionIdAuthData(urid, lastName, ubPrefix);
    	    
    	} catch (NamingException e) {
    	    log.error("An error occurred while getting the users patron key.", e);
    	} finally {
    	    // If we set up a connection to the LDAP server, close it
    	    if (ldapConnection != null) {
	    		try {
	    		    if (log.isDebugEnabled())
	    			log.debug("Closing the LDAP connection.");
	    		    ldapConnection.close();
	    		} catch (NamingException e) {
	    		    log.warn("An error occurred while closing the connection to the LDAP server defined in the "
	    					    + "configuration file.", e);
	    		}
    	    }
    	}
    	return null;
    }

	/**
	 * Given a user's authentication data, return their patron ID.  If their
	 * authentication information was not valid, return null instead
	 * 
	 * @param username The user's barcode
	 * @param password The user's password
	 * @param ubPrefix The ubPrefix to the web service URL
	 * @return The user's patron ID if the username and password were correct, or null otherwise
	 * @throws ILSException If an error occurred while connecting to the web service
	 */
	public String getPatronIdFromBarcodeAuthData(String username, String password, String ubPrefix)
	{   
	    // Construct the xml to send to the web service
        // Using B for barcode id
        Namespace serNs = Namespace.getNamespace("ser", "http://www.endinfosys.com/Voyager/serviceParameters");
        Document authUserXML = new Document();
        
        Element root = new Element("serviceParameters", serNs);
        authUserXML.addContent(root);
        Element parametersElement = new Element("parameters", serNs);
        root.addContent(parametersElement);
        Element patronElement = new Element("patronIdentifier", serNs);
        patronElement.setAttribute("lastName", password);
        root.addContent(patronElement);
        Element authElement = new Element("authFactor", serNs);
        authElement.setAttribute("type", "B");
        authElement.setText(username);
        patronElement.addContent(authElement);
        
        XMLOutputter xmlOutP = new XMLOutputter();
        
        int statusCode = 0;
        PostMethod postMethod = null;
        // Retrieve the URL of vxws
        String postUrl = (String) voyagerConfig.getProperty(VoyagerConstants.CONFIG_VOYAGER_WEB_SERVICE_URL);
        // And attach the particular web service we are using
		postUrl += ubPrefix + "/AuthenticatePatronService";
		
    	try {
	        synchronized(client) {
	        	postMethod = new PostMethod(postUrl);
	        	//postRenewItem.setRequestEntity(new StringRequestEntity(authenticateUserXml));
	        	postMethod.setRequestEntity(new StringRequestEntity(xmlOutP.outputString(authUserXML)));
	        	statusCode = client.executeMethod(postMethod);	
				log.info("Sent xml request to server.  Received status code " + Integer.toString(statusCode));
	        }
	        
	        if (statusCode == 200) {
	        	InputStream response = postMethod.getResponseBodyAsStream();	        	
	        	SAXBuilder builder = new SAXBuilder();
	        	try {
	        		// Build a JDOM Document from the response and extract the institution ID
		        	Document doc = builder.build(response);
		        	Namespace patNs = Namespace.getNamespace("pat", "http://www.endinfosys.com/Voyager/patronAuthentication");
		        	// If there is a serviceData element then the user was authenticated.  Otherwise an error response was returned
		        	if (doc.getRootElement().getChild("serviceData", serNs).getChild("patronIdentifier", patNs) != null) {
		        		String patronId = doc.getRootElement().getChild("serviceData", serNs).getChild("patronIdentifier", patNs).getAttributeValue("patronId"); 
		        		return patronId;
		        	}
	        		else
		        		return null;
	        	} catch (JDOMException e) { 
	        		log.debug("Error parsing xml response");
	        		return null;
	        	} catch (NullPointerException e) {
	        		log.debug("User not found with barcode date");
	        		return null;
	        	}	        	
	        } else 
				log.error("Cound not contact the vxws service");
	        
    	}  catch (IOException e) {
			log.error("IOException caught while contacting the vxws service.  An internal error occurred in the NCIP Toolkit.", e);
		}	
    	    	
    	return null;
	}

	/**
	 * Given a user's authentication data, return their patron ID.  If their
	 * authentication information was not valid, return null instead
	 * 
	 * @param username The user's username
	 * @param password The user's password
	 * @param ubPrefix The ubPrefix to the web service URL
	 * @return The user's patron ID if the username and password were correct, or null otherwise
	 * @throws ILSException If an error occurred while connecting to the database
	 */
	public String getPatronIdFromInstitutionIdAuthData(String username, String password, String ubPrefix) 
	{   
	    // Construct the xml to send to the web service
        // Using I for institution id
        Namespace serNs = Namespace.getNamespace("ser", "http://www.endinfosys.com/Voyager/serviceParameters");
        Document authUserXML = new Document();
        
        Element root = new Element("serviceParameters", serNs);
        authUserXML.addContent(root);
        Element parametersElement = new Element("parameters", serNs);
        root.addContent(parametersElement);
        Element patronElement = new Element("patronIdentifier", serNs);
        patronElement.setAttribute("lastName", password.toLowerCase());
        root.addContent(patronElement);
        Element authElement = new Element("authFactor", serNs);
        authElement.setAttribute("type", "I");
        authElement.setText(username);
        patronElement.addContent(authElement);
        
        XMLOutputter xmlOutP = new XMLOutputter();
        
        int statusCode = 0;
        PostMethod postRenewItem = null;
        // Retrieve the URL of vxws
        String postUrl = (String) voyagerConfig.getProperty(VoyagerConstants.CONFIG_VOYAGER_WEB_SERVICE_URL);
        // And attach the particular web service we are using
		postUrl += ubPrefix + "/AuthenticatePatronService";
		
		log.debug("Using URL " + postUrl);
		
    	try {
	        synchronized(client) {
	        	postRenewItem = new PostMethod(postUrl);
	        	postRenewItem.setRequestEntity(new StringRequestEntity(xmlOutP.outputString(authUserXML)));
	        	statusCode = client.executeMethod(postRenewItem);	
				log.info("Sent xml request to server.  Received status code " + Integer.toString(statusCode));
	        }
	        
	        if (statusCode == 200) {
	        	InputStream response = postRenewItem.getResponseBodyAsStream();	        	
	        	SAXBuilder builder = new SAXBuilder();
	        	try {
	        		// Build a JDOM Document from the response and extract the institution ID
		        	Document doc = builder.build(response);
		        	Namespace patNs = Namespace.getNamespace("pat", "http://www.endinfosys.com/Voyager/patronAuthentication");
		        	// If there is a serviceData element then the user was authenticated.  Otherwise an error response was returned
		        	if (doc.getRootElement().getChild("serviceData", serNs).getChild("patronIdentifier", patNs) != null) {
			        	String patronId = doc.getRootElement().getChild("serviceData", serNs).getChild("patronIdentifier", patNs).getAttributeValue("patronId"); 
			        	return patronId;
			        }
	        		else
		        		return null;
	        	} catch (JDOMException e) { 
	        		log.debug("Error parsing xml response");
	        		return null;
	        	} catch (NullPointerException npe) {
	        		log.debug("Did not find patron with I data");
	        		return null;
	        	}
	        	
	        } else 
				log.error("Cound not contact the vxws service");
	        
    	}  catch (IOException e) {
			log.error("IOException caught while contacting the vxws service.  An internal error occurred in the NCIP Toolkit.", e);
		}	
    	    	
    	return null;
	}

	/**
	 * Given a partial URL to a vxws service, constructs the entire URL and sends
	 * the given XML to the service, returning the response Document
	 * 
	 * @param serviceUrl partial URL to the particular vxws service
	 * @param inputXML Xml to PUT in the HTTP request
	 * @return XML Document from the vxws response
	 */
	public Document putWebServicesDoc(String serviceUrl, String inputXml) {
		
    	int statusCode;
    	PutMethod putMethod = null;
        // The URL of the web services server + service name
        String baseUrl = (String) voyagerConfig.getProperty(VoyagerConstants.CONFIG_VOYAGER_WEB_SERVICE_URL);
        
        String fullUrl = baseUrl + serviceUrl; 
        
        InputStream response = null;
        
    	try {
	        synchronized(client) {
	        	putMethod = new PutMethod(fullUrl);
	        	putMethod.setRequestEntity(new StringRequestEntity(inputXml));
	        	statusCode = client.executeMethod(putMethod);
	        }
	        
	        if (statusCode == 200) {
	        	response = putMethod.getResponseBodyAsStream();
	        } else {
				log.error("Cound not contact the vxws service.  Recieved HTTP status code: " + statusCode);
	        }
    	}  catch (IOException e) {
			log.error("IOException caught while contacting the vxws service.  An internal error occurred in the NCIP Toolkit.", e);
		}

    	SAXBuilder builder = new SAXBuilder();
    	Document doc = null;
       	try {
    		// Build a JDOM Document from the response 
        	doc = builder.build(response);
       	} catch (JDOMException e) { 
    		log.error("JDOMException parsing xml response");
    	} catch (IOException e) {
    		log.error("IOException parsing xml response");
    	}   
    	
    	return doc;
	}
	
	/**
	 * Given a partial URL to a vxws service, constructs the entire URL
	 * and issues an HTTP GET request to that URL
	 * @param serviceUrl portion of the URL to the particular vxws service
	 * @return XML Document from the vxws response
	 */
	public Document getWebServicesDoc(String serviceUrl) {
		
    	int statusCode;
    	GetMethod getMethod = null;
        // The URL of the web services server + service name
        String baseUrl = (String) voyagerConfig.getProperty(VoyagerConstants.CONFIG_VOYAGER_WEB_SERVICE_URL);
        
        String fullUrl = baseUrl + serviceUrl; 
        
        InputStream response = null;
        
    	try {
	        synchronized(client) {
	        	getMethod = new GetMethod(fullUrl);
	        	statusCode = client.executeMethod(getMethod);
	        }
	        
	        if (statusCode == 200) {
	        	response = getMethod.getResponseBodyAsStream();
	        } else {
				log.error("Cound not contact the vxws service.  Recieved HTTP status code: " + statusCode);
	        }
    	}  catch (IOException e) {
			log.error("IOException caught while contacting the vxws service.  An internal error occurred in the NCIP Toolkit.", e);
		}

    	SAXBuilder builder = new SAXBuilder();
    	Document doc = null;
       	try {
    		// Build a JDOM Document from the response 
        	doc = builder.build(response);
       	} catch (JDOMException e) { 
    		log.error("JDOMException parsing xml response");
    	} catch (IOException e) {
    		log.error("IOException parsing xml response");
    	}   
    	
    	return doc;
	}
	
	/**
	 * Given a partial URL to a vxws service, constructs the entire URL and POSTS
	 * the given XML to the service, returning the response Document
	 * 
	 * @param serviceUrl partial URL to the particular vxws service
	 * @return XML Document from the vxws response
	 */
	public Document postWebServicesDoc(String serviceUrl) {		
    
		int statusCode;
    	PostMethod postMethod = null;
        // The URL of the web services server + service name
        String baseUrl = (String) voyagerConfig.getProperty(VoyagerConstants.CONFIG_VOYAGER_WEB_SERVICE_URL);
        
        String fullUrl = baseUrl + serviceUrl; 
        
        InputStream response = null;
        
    	try {
	        synchronized(client) {
	        	postMethod = new PostMethod(fullUrl);
	        	statusCode = client.executeMethod(postMethod);
	        }
	        
	        if (statusCode == 200) {
	        	response = postMethod.getResponseBodyAsStream();
	        } else {
				log.error("Cound not contact the vxws service with URL: " + serviceUrl + ".  Recieved HTTP status code: " + statusCode);
	        }
    	}  catch (IOException e) {
			log.error("IOException caught while contacting the vxws service.  An internal error occurred in the NCIP Toolkit.", e);
		}

    	SAXBuilder builder = new SAXBuilder();
    	Document doc = null;
       	try {
    		// Build a JDOM Document from the response 
        	doc = builder.build(response);
       	} catch (JDOMException e) { 
    		log.error("JDOMException parsing xml response");
    	} catch (IOException e) {
    		log.error("IOException parsing xml response");
    	}   
    	
    	return doc;
	}
	
	public GregorianCalendar stringDateToGC (String date) {
		String[] dateComponents = date.substring(0, 10).split("-");
		String[] timeComponents = date.substring(11).split(":");
		
		GregorianCalendar gc = new GregorianCalendar(Integer.parseInt(dateComponents[0]),
				Integer.parseInt(dateComponents[1]) - 1, //GC months start at 0 
				Integer.parseInt(dateComponents[2]),
				Integer.parseInt(timeComponents[0]),
				Integer.parseInt(timeComponents[1]));    	
		
		return gc;
	}
}

