
package jerry.chadwick.jersey.sample;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import java.util.ArrayList;
import java.lang.NumberFormatException;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.enunciate.jaxrs.TypeHint;
import org.codehaus.jackson.node.ObjectNode;

//public static final MediaType APPLICATION_GEDCOMX_XML_TYPE = new MediaType("application", "x-gedcomx-v1+xml");
// Redirect URI is: http://localhost:8080/FSSampleApp/familysearch/callback

/** Example resource class hosted at the URI path "/myresource"
 */
@Path("/familysearch")
public class FamilySearchResource {

    public static List<String> myList;


    private static FamilySearchResource instance;
    private static String gCode;
    private static String gAccessToken;

    // Main is only used for Java Apprentice class tasks and not part of the service.
    public static void main(String [] args) throws Exception {

        if (instance == null)
            instance = new FamilySearchResource();
        gCode = "";
        gAccessToken = "";
    }

// *******************************

    public static FamilySearchResource getInstance() {

        if (instance == null)
            instance = new FamilySearchResource();

        return instance;
    }

    @GET
    @Path("/callback")
    @Produces(MediaType.APPLICATION_JSON)
    public Response callback(@QueryParam("code") String codeParam) {
        String retVal = "The code returned from Oauth is: " + codeParam + " and the POST is sent to //token endpoint.";

        gCode = codeParam;

        String urlParameters  = "grant_type=authorization_code&client_id=a0T3000000Bg9NjEAJ&code=" + gCode;
        byte[] postData       = urlParameters.getBytes( Charset.forName( "UTF-8" ));
        int    postDataLength = postData.length;
        String request        = "https://sandbox.familysearch.org/cis-web/oauth2/v3/token";


        try {
            URL url = new URL(request);
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setInstanceFollowRedirects(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Accept", MediaType.APPLICATION_JSON);
                urlConnection.setRequestProperty("charset", "utf-8");
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                urlConnection.setUseCaches(false);
                try  {
                    DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
                    wr.write(postData);

                    ObjectMapper mapper = new ObjectMapper();
                    ObjectNode node = (ObjectNode) mapper.readTree(urlConnection.getInputStream());
                    gAccessToken = node.get("access_token").getTextValue();

                    //gAccessToken = retVal;
                    retVal = "Session ID / token is " + gAccessToken;

                }
                catch (java.io.IOException ioException) {
                    // handle ioException...
                    retVal = "Failed opening post outputstream...";
                    return Response.status(Response.Status.BAD_REQUEST).entity(retVal).build();
                }
            }
            catch (java.io.IOException ioException) {
                // handle ioException...
                retVal = "Failed opening http connection...";
                return Response.status(Response.Status.BAD_REQUEST).entity(retVal).build();
            }
        }
        catch (java.net.MalformedURLException urlException) {
            // report bad URL
            retVal = "Mal-formed URL request...";
            return Response.status(Response.Status.BAD_REQUEST).entity(retVal).build();
        }

        return Response.status(Response.Status.OK).entity(retVal).build();
    }


    // get current tree person
    @GET
    @Path("/tree/current-person")
    @Produces("application/x-gedcomx-v1+xml")
    public Response getCurrentTreePerson() {

        String retVal           = new String();
        String request          = "https://integration.familysearch.org/platform/tree/current-person";

        try {

            //======

            URL url = new URL(request);
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoInput(true);
                urlConnection.setRequestMethod("GET"); // default
                urlConnection.setRequestProperty("Accept", "application/x-gedcomx-v1+xml");
                urlConnection.setRequestProperty("Authorization", "Bearer " + gAccessToken);
                urlConnection.setRequestProperty("charset", "utf-8");
                urlConnection.setUseCaches(false);

                int responseCode = 0;

                try  {

                    responseCode = urlConnection.getResponseCode();

                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(urlConnection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    retVal = response.toString();

                }
                catch (java.io.IOException ioException) {
                    // handle ioException...
                    retVal = "Failed opening post outputstream...response code = " + responseCode + " Access token = " + gAccessToken;
                    return Response.status(Response.Status.BAD_REQUEST).entity(retVal).build();
                }
            }
            catch (java.io.IOException ioException) {
                // handle ioException...
                retVal = "Failed opening http connection...";
                return Response.status(Response.Status.BAD_REQUEST).entity(retVal).build();
            }
        }
        catch (java.net.MalformedURLException urlException) {
            // report bad URL
            retVal = "Mal-formed URL request...";
            return Response.status(Response.Status.BAD_REQUEST).entity(retVal).build();
        }

        return Response.status(Response.Status.OK).entity(retVal).build();

    }


    /** Method processing HTTP GET requests, producing "text/plain" MIME media
     * type.
     * @return String that will be send back as a response of type "text/plain".
     */

/*

    @GET
    @Path("{id}")
    @Produces("text/plain")
    public Response getPlainTextParam(@PathParam("id") String id) {
        if (id != null) {
            Integer iIndex = new Integer(0);
            try {
                iIndex = Integer.parseInt(id);
                if (iIndex >= getInstance().myList.size()) {
                    String sErrText = "Path parameter: " + iIndex + " must be less than the length of the list of items, which is " + ((Integer) (getInstance().myList.size()-1)).toString();
                    return Response.status(Response.Status.NOT_FOUND).entity(sErrText).build();
                }
            }

            catch (NumberFormatException exc) {
                String sErrText = "Path parameter must be must be an integer number.";
                return Response.status(Response.Status.BAD_REQUEST).entity(sErrText).build();

            }
            catch (IndexOutOfBoundsException exc) {
                String sErrText = "Index must be between 0 and " + ((Integer) getInstance().myList.size()).toString();
                return Response.status(Response.Status.BAD_REQUEST).entity(sErrText).build();

            }
            String eTag = String.valueOf(Response.status(200).hashCode());
            String retVal = "Item number " + iIndex.toString() + " : " + getInstance().myList.get(iIndex.intValue()).toString();
            return Response.status(Response.Status.OK).tag(eTag).entity(retVal).build();
        }
        else {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid id").build();
        }
    }
*/

    /*
    // This method is called if XML is request and a param is NOT passed
    @GET
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
    public Response getXMLAllItems(@PathParam("id") String id) {
        String retVal = new String();
        retVal ="<hello> " +
                "<title>Jerry's First REST Application" + "</title>" +
                "<list>";
        Integer iIndex = new Integer(0);

        for (int i = 0; i < getInstance().myResourceObjectList.size(); i++) {
            iIndex = i;
            retVal += "<item itemID=\"" + getInstance().myResourceObjectList.get(i).itemID + "\">localhost:8080/firstRESTApp/myresource/" + iIndex.toString() + "</item>";
        }

        retVal += "</list>" +
                "</hello>";
        String eTag = String.valueOf(Response.status(200).hashCode());
        return Response.status(Response.Status.OK).tag(eTag).entity(retVal).build();

    }

*/

/*

    // This method is called if XML or JSON is requested and an id is passed
    @GET
    @Path("{id}")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_JSON})
    public Response getXMLItem(@PathParam("id") String id) {

        Integer iIndex = new Integer(0);

        try {
            iIndex = Integer.parseInt(id);
            if (iIndex >= getInstance().myList.size()) {
                String sErrText = "Path parameter: " + iIndex + " must be less than the length of the list of items, which is " + ((Integer) (getInstance().myList.size()-1)).toString();
                return Response.status(Response.Status.NOT_FOUND).entity(sErrText).build();
            }
        }

        catch (NumberFormatException exc) {
            String sErrText = "Path parameter must be must be an integer number.";
            return Response.status(Response.Status.BAD_REQUEST).entity(sErrText).build();

        }
        catch (IndexOutOfBoundsException exc) {
            String sErrText = "Index must be between 0 and " + ((Integer) getInstance().myList.size()).toString();
            return Response.status(Response.Status.BAD_REQUEST).entity(sErrText).build();

        }
        String eTag = String.valueOf(Response.status(200).hashCode());
        return Response.status(Response.Status.OK).tag(eTag).entity((getInstance().myResourceObjectList.get(iIndex.intValue()))).build();

    }

*/
/*

    // This method is called if XML or JSON is requested and an id is passed
    @GET
    @Path("{id}/coaches")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_JSON})
    @TypeHint(MyCoachObject.class)
    public Response getXMLCoachsItem(@PathParam("id") String id) {

        Integer iIndex = new Integer(0);

        try {
            iIndex = Integer.parseInt(id);
            if (iIndex >= getInstance().myList.size()) {
                String sErrText = "Path parameter: " + iIndex + " must be less than the length of the list of items, which is " + ((Integer) (getInstance().myList.size()-1)).toString();
                return Response.status(Response.Status.NOT_FOUND).entity(sErrText).build();
            }
        }

        catch (NumberFormatException exc) {
            String sErrText = "Path parameter must be must be an integer number.";
            return Response.status(Response.Status.BAD_REQUEST).entity(sErrText).build();

        }
        catch (IndexOutOfBoundsException exc) {
            String sErrText = "Index must be between 0 and " + ((Integer) getInstance().myList.size()).toString();
            return Response.status(Response.Status.BAD_REQUEST).entity(sErrText).build();

        }
        String eTag = String.valueOf(Response.status(200).hashCode());
        return Response.status(Response.Status.OK).tag(eTag).entity((getInstance().myCoachesObjectList.get(iIndex.intValue()))).build();

    }


*/

    /*
    // This method is called if HTML is request and an id is passed
    @GET
    @Path("{id}")
    @Produces(MediaType.TEXT_HTML)
    public Response getHTMLItem(@PathParam("id")String id) {
        Integer iIndex = new Integer(0);
        try {
            iIndex = Integer.parseInt(id);
            if (iIndex >= getInstance().myList.size()) {
                String sErrText = "Path parameter: " + iIndex + " must be less than the length of the list of items, which is " + ((Integer) (getInstance().myList.size()-1)).toString();
                return Response.status(Response.Status.NOT_FOUND).entity(sErrText).build();
            }
        }

        catch (NumberFormatException exc) {
            String sErrText = "Path parameter must be must be an integer number.";
            return Response.status(Response.Status.BAD_REQUEST).entity(sErrText).build();

        }
        catch (IndexOutOfBoundsException exc) {
            String sErrText = "Index must be between 0 and " + ((Integer) getInstance().myList.size()).toString();
            return Response.status(Response.Status.BAD_REQUEST).entity(sErrText).build();

        }
        String retVal =  "<html> " + "<title>" + "Jerry's First REST Application" + "</title>"
                + "<body><h1>" + getInstance().myList.get(iIndex.intValue()).toString() + "</h1></body>" + "</html> ";
        String eTag = String.valueOf(Response.status(200).hashCode());
        return Response.status(Response.Status.OK).tag(eTag).entity(retVal).build();
    }
*/



}
