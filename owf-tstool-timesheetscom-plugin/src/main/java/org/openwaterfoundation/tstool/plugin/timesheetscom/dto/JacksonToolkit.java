// JacksonToolkit - useful tools to use the Jackson library

/* NoticeStart

OWF TSTool TimesheetsCom Plugin
Copyright (C) 2023 Open Water Foundation

OWF TSTool TimesheetsCom Plugin is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

OWF TSTool TimesheetsCom Plugin is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

You should have received a copy of the GNU General Public License
    along with OWF TSTool TimesheetsCom Plugin.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package org.openwaterfoundation.tstool.plugin.timesheetscom.dto;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import RTi.Util.IO.UrlReader;
import RTi.Util.IO.UrlResponse;
import RTi.Util.Message.Message;
import RTi.Util.String.MultiKeyStringDictionary;

/**
 * This toolkit facilitates using Jackson package to translate JSON to/from data access objects.
 */
public class JacksonToolkit {

	/**
	 * Global ObjectMapper as part of the Jackson library used
	 * for serializing and deserializing JSON data to a POJO.
	 */
	private ObjectMapper mapper;

	/**
	 * Jackson Toolkit used for lazy initialization of a singleton class
	 */
	private static JacksonToolkit instance;

	private JacksonToolkit() {
		this.mapper = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	/**
	 * Lazy initialization of a singleton class instance
	 * @return instance of JacksonToolkit class
	 */
	public static JacksonToolkit getInstance() {
		if ( instance == null ) {
			instance = new JacksonToolkit();
		}
		return instance;
	}

	/**
	 * Given a url to Web Services this method retrieves the JSON response from
	 * web services and converts that to a JsonNode from the Jackson Library.
	 * @param url web service URL to query
	 * @param requestProperties header request properties for API authentication
	 * @param elements element names from highest level to specific element
	 * corresponding to the JSON node, typically the name of an array of objects
	 * @return JsonNode of returned value from web services request.
	 * @throws JsonParseException if a JSON parse error
	 * @throws JsonMappingException if a JSON mapping error
	 * @throws MalformedURLException if a bad URL
	 * @throws IOException typically a timeout
	 */
	public JsonNode getJsonNodeFromWebServiceUrl ( boolean debug, String url, MultiKeyStringDictionary requestProperties,
		String [] elements )
		throws JsonParseException, JsonMappingException, MalformedURLException, IOException {
		String requestData = null;
		return getJsonNodeFromWebServiceUrl ( debug, url, requestProperties, requestData, elements );
	}

	/**
	 * Given a url to Web Services this method retrieves the JSON response from
	 * web services and converts that to a JsonNode from the Jackson Library.
	 * @param url web service URL to query
	 * @param requestProperties header request properties for API authentication
	 * @param requestData the request data as text
	 * @param elements element names from highest level to specific element
	 * corresponding to the JSON node, typically the name of an array of objects
	 * @return JsonNode of returned value from web services request.
	 * @throws JsonParseException if a JSON parse error
	 * @throws JsonMappingException if a JSON mapping error
	 * @throws MalformedURLException if a bad URL
	 * @throws IOException typically a timeout
	 */
	public JsonNode getJsonNodeFromWebServiceUrl ( boolean debug, String url, MultiKeyStringDictionary requestProperties,
		String requestData, String [] elements )
		throws JsonParseException, JsonMappingException, MalformedURLException, IOException {
		String routine = getClass().getSimpleName() + ".getJsonNodeFromWebServicesUrl";
		JsonNode results = null;
		URL request = null;
		// Local debugging.
		//debug = false;

		// TODO smalers 219-09-04 this is in HydroBase REST but results in double query of the web service!
		//if ( !httpResponse200(url) ) {
			//Message.printWarning(2, routine, "Error: " + url + " returned a 404 error");
			//return null;
		//}

		//System.out.println(url);

		// Whether the URL was read OK.
		boolean didRead = false;
		int responseCode = -1;
		try {
			//request = new URL(url);
			// Need to call something like HttpURLConnection.setRequestProperty
			UrlReader urlReader = new UrlReader ( url, requestProperties, requestData );
			UrlResponse urlResponse = null;
			try {
				urlResponse = urlReader.read();
				responseCode = urlResponse.getResponseCode();
				if ( responseCode != 200 ) {
					Message.printWarning(2, routine, "HTTP response code = " + responseCode );
					didRead = false;
				}
				else {
					didRead = true;
				}
			}
			catch ( Exception e ) {
				Message.printWarning(2, routine, "Error reading URL \"" + url + "\" (" + e + ").");
				Message.printWarning(2, routine, e);
				didRead = false;
			}
			if ( didRead ) {
				// The following will throw an IOException if there is an error.
				JsonNode rootNode = this.mapper.readTree ( urlResponse.getResponse() );
				if ( rootNode == null ) {
					Message.printDebug(1, routine, "Root JSON node is null." );
				}
				else {
					if ( debug ) {
						// This outputs the entire tree, not pretty print.
						Message.printDebug(1, routine, "Root JSON node contents: " + rootNode );
					}
				}

				// If debugging, print out the result so can see what is returned so that DAO class can be created.
				if ( debug ) {
					logJsonTree(rootNode);
				}
				// Comment when not needed for troubleshooting.
				//logJsonTree(rootNode);

				// Adjust the node to the requested element, typically an array name.
				if ( (elements != null) && (elements.length > 0) ) {
					// Position the node at the requested element.
					results = rootNode; // Default
					for ( String element : elements ) {
						// Get the element of interest:
						// - returns missing node if not found
						// - 'path' only searches for the name in the current level
						results = results.path(element);
						// Returns null if not found.
						//results = rootNode.get(element);
						if ( debug ) {
							Message.printDebug(1, routine, "Results JSON node contents after positioning at \"" + element + "\": " + results );
						}
						if ( results == null ) {
							Message.printDebug(1, routine, "Unable to position JSON node at element \"" + element + "\"." );
							break;
						}
						else {
							if ( results.isArray() ) {
								if ( debug ) {
									Message.printDebug(1, routine, "Results node is an array." );
								}
								// If an array node, missing node will be returned so need to cast to an ArrayNode.
								results = (ArrayNode)results;
							}
							else if ( results.isMissingNode() ) {
								Message.printDebug(1, routine, "Unable to position JSON node (missing node) for element \"" + element + "\"." );
								break;
							}
							else {
								if ( debug ) {
									Message.printDebug(1, routine, "Positioned JSON node at element \"" + element + "\", results=" + results );
								}
								else {
									Message.printDebug(1, routine, "Positioned JSON node at element \"" + element + "\"." );
								}
							}
						}
					}
				}
				else {
					// No element requested so return the root node.
					results = rootNode;
					Message.printStatus(2, routine, "Returning root JSON node." );
				}
			}
		}
		catch ( JsonParseException e ) {
			Message.printWarning(2, routine, "Error parsing JSON response from \"" + request + "\" (" + e + ").");
			throw e;
		}
		catch ( JsonMappingException e ) {
			Message.printWarning(2, routine, "Error mapping JSON response from \"" + request + "\" (" + e + ").");
			throw e;
		}
		catch ( MalformedURLException e ) {
			Message.printWarning(2, routine, "Malformed URL has occured. URL=\"" + url + "\" (" + e + ").");
			throw e;
		}
		catch ( IOException e ) {
			Message.printWarning(2, routine, "IOException (" + e + ").");
			throw e;
		}

		if ( !didRead && (responseCode != 200) ) {
			throw new HttpCodeException("HTTP request had an error (" + responseCode + ").", responseCode );
		}

		return results;
	}

	/**
	 * Checks to see if the request string returns a response 200 or an error 404.
	 * @param urlString String representing the URL request from web services.
	 * @return true if request came back okay, with a response 200, false if response 404.
	 */
	public boolean httpResponse200 ( String urlString ) {
		HttpURLConnection connection;
		try {
			URL url = new URL(urlString);
			connection = (HttpURLConnection)url.openConnection();
			if ( connection.getResponseCode() == 200 ) {
				return true;
			}
			else {
				return false;
			}
		}
		catch (MalformedURLException e) {
			return false;
		}
		catch (IOException e) {
			return false;
		}
	}

	/**
	 * Log the Json tree to the log file.
	 * @param node JsonNode to format and print to the log file
	 */
	private void logJsonTree ( JsonNode node ) {
		String routine = getClass().getSimpleName() + ".logJsonTree";
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			String jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString ( node );
			Message.printStatus ( 2, routine, jsonString );
		}
		catch ( JsonProcessingException e ) {
			// For now ignore.
		}
	}

	/**
	 * Deserializes a JsonNode to a POJO class.
	 * This has the advantage of providing control over the process.
	 * null values are handled by setting to null in the POJO object if values are defined
	 * as classes (Integer, Double, etc.), but will be set to 0 if primitives are used.
	 * Therefore, if zeros are being used where not expected, define as an object and not primitive in the class.
	 * @param node - JsonNode to deserialize to POJO.
	 * @param objClass - The class that the JsonNode is to be deserialized to.
	 * @return the POJO that has been initialized via Jackson deserialization from the JsonNode data.
	 */
	public Object treeToValue(JsonNode node, Class<?> objClass) {
		String routine = getClass().getSimpleName() + ".treeToValue";
		try {
			return this.mapper.treeToValue(node, objClass);
		}
		catch (JsonParseException e ) {
			Message.printWarning(3, routine, "Error converting JSON response to class instance (" + e + ").");
			Message.printWarning(3, routine, e);
			return null;
		}
		catch (JsonMappingException e ) {
			Message.printWarning(3, routine, "Error converting JSON response to class instance (" + e + ").");
			Message.printWarning(3, routine, e);
			return null;
		}
		catch (IOException e) {
			Message.printWarning(3, routine, "IOException (" + e + ").");
			Message.printWarning(3, routine, e);
			return null;
		}
	}
}