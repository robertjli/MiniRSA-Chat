package client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * @author Evan Schoenbach
 * @version March 8, 2012
 *
 */
public class BasicHttpClient {

    private Socket clientSocket = null;
    private OutputStream out = null;
    private DataOutputStream dataOut = null;
    private InputStream inputStream = null;
    private String domain;

    public BasicHttpClient(String domain) {    

        this.domain = domain;

        /* Open Socket and I/O streams */
        try {
            clientSocket = new Socket(domain, 80);
            out = clientSocket.getOutputStream();
            dataOut = new DataOutputStream(out);
            inputStream = clientSocket.getInputStream();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(-1);
        } catch (IOException e1) {
            e1.printStackTrace();
            System.exit(-1);
        }
    }

    public File retrieveFile(String path) throws IOException {
        /* Send request for the url */
        dataOut.writeBytes("GET " + path + " HTTP/1.1");
        dataOut.writeBytes("\r\n");
        dataOut.writeBytes("Host: " + domain);
        dataOut.writeBytes("\r\n");
        dataOut.writeBytes("Connection: Close");
        dataOut.writeBytes("\r\n");
        dataOut.writeBytes("\r\n");
        dataOut.flush();

        /* Write retrieved file to local temp document */    // maybe write to ArrayList<String> instead of File?
        File document = new File("temp.html");
        BufferedReader in = new BufferedReader(
                new InputStreamReader(inputStream));
        PrintWriter docWriter = null;
        try {
            docWriter = new PrintWriter(new FileWriter(document));
        } catch (IOException e) {
            System.err.println("XML/HTML file creation failed");
            return null;
        }
        String line;

        /* Skip over initial response line and headers */
        while ((line = in.readLine()) != null && line.length() > 4) {
            continue;
        }
        /* Copy HTML/XML body to file */
        while ((line = in.readLine()) != null) {
            docWriter.println(line);
        }
        docWriter.flush();
        docWriter.close();        
        in.close();
        closeSocket();
        
        return document;
    }

    public ArrayList<String> getRobotsTxt() throws IOException {

        ArrayList<String> robotsTxtLines = new ArrayList<String>();

        /* Send request for the robots.txt */
        dataOut.writeBytes("GET /robots.txt HTTP/1.1");
        dataOut.writeBytes("\r\n");
        dataOut.writeBytes("Host: " + domain);
        dataOut.writeBytes("\r\n");
        dataOut.writeBytes("Connection: Close");
        dataOut.writeBytes("\r\n");
        dataOut.writeBytes("\r\n");
        dataOut.flush();

        BufferedReader in = new BufferedReader(
                    new InputStreamReader(inputStream));
        String line;

        /* Skip over initial response line and headers */
        while ((line = in.readLine()) != null && line.length() > 4) {
            continue;
        }
        /* Copy text body to array of Strings */
        while ((line = in.readLine()) != null) {
            robotsTxtLines.add(line);
        }
        
        in.close();
        closeSocket();
        
        return robotsTxtLines;
    }

    public ArrayList<Object> getHead(String path, String lastMod) throws IOException {

        /* Send request for the robots.txt */
        dataOut.writeBytes("HEAD " + path + " HTTP/1.1");
        dataOut.writeBytes("\r\n");
        dataOut.writeBytes("Host: " + domain);
        dataOut.writeBytes("\r\n");
        dataOut.writeBytes("If-Modified-Since: " + lastMod);
        dataOut.writeBytes("\r\n");
        dataOut.writeBytes("Connection: Close");
        dataOut.writeBytes("\r\n");
        dataOut.writeBytes("\r\n");
        dataOut.flush();
        
        BufferedReader in = new BufferedReader(
                    new InputStreamReader(inputStream));
        String line = null, type = null, typeLine = null, lengthLine = null, initialLine = null;
        int length = 0, statusCode = 0;
        boolean gotInitialLine = false; // since there's no easy way to spot initialline except the first HTTP instance
        int prevHeader = 0; // 0 means unknown, 1 is type, 2 is length, 3 is initial. For multiline headers
        while ((line = in.readLine()) != null && line.length() > 4) {
            // System.out.println("Reading line " + line);
            if (line.startsWith(" ")) { // support multiline headers
                switch (prevHeader) {
                    case 0: break;
                    case 1: typeLine = typeLine + line.trim(); break;
                    case 2: lengthLine = lengthLine + line.trim(); break;
                    case 3: initialLine = initialLine + line.trim(); break;
                }
            }
            if (line.toUpperCase().contains("Content-Type:".toUpperCase())) { // case-insensitive
                typeLine = line;
                prevHeader = 1;
            } else if (line.toUpperCase().contains("Content-Length:".toUpperCase())) {
                lengthLine = line;
                prevHeader = 2;
            } else if (line.toUpperCase().contains("HTTP/") && !line.toUpperCase().contains("INVALID")) {
                if (!gotInitialLine) {
                    initialLine = line;
                    prevHeader = 3;
                    gotInitialLine = true;
                } else {
                    prevHeader = 0;
                }
            } else {
                prevHeader = 0;
            }
        }
        if (typeLine != null) {
            type = typeLine.split(":")[1];
            type = type.split(";")[0].trim(); // to avoid charset parameter
            
        }

        if (lengthLine != null) {
            length = Integer.parseInt(lengthLine.split(":")[1].trim());
        }

        if (initialLine != null) {
            try {
                statusCode = Integer.parseInt(initialLine.split(" ")[1]);
            } catch (NumberFormatException e) {
                XPathCrawler.writeToLog("Couldn't parse HTTP statuscode", e);
                e.printStackTrace();
            }
        }

        ArrayList<Object> headRequest = new ArrayList<Object>();
        headRequest.add(type);
        headRequest.add(Integer.valueOf(length));
        headRequest.add(Integer.valueOf(statusCode));
               
        in.close();
        closeSocket();
        
        return headRequest;
    }
    
    public String getRawFile(String path) throws IOException {

        /* Send request for the robots.txt */
        dataOut.writeBytes("GET " + path + " HTTP/1.1");
        dataOut.writeBytes("\r\n");
        dataOut.writeBytes("Host: " + domain);
        dataOut.writeBytes("\r\n");
        dataOut.writeBytes("Connection: Close");
        dataOut.writeBytes("\r\n");
        dataOut.writeBytes("\r\n");
        dataOut.flush();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(inputStream));
        String line;

        /* Skip over initial response line and headers */
        while ((line = in.readLine()) != null && line.length() > 4) {
            continue;
        }
        /* Copy text body to a string */
        StringWriter sw = new StringWriter();
        while ((line = in.readLine()) != null) {
            sw.write(line, 0, line.length());
        }
        
        String string = sw.toString();        
        sw.close();
        in.close();
        closeSocket();
        
        return string;
    }

//    private Date convertToDate(String date) throws ParseException {
//        char[] dateChars = date.toCharArray();
//        SimpleDateFormat format = null;
//        if (dateChars[3] == ',') {
//            format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
//        } else if (dateChars[3] == ' ') {
//            format = new SimpleDateFormat("EEEE, dd-MMM-yy HH:mm:ss zzz");
//        } else {
//            format = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
//        }
//        Date parsedDate = format.parse(date);
//        return parsedDate;
//    }

    public void closeSocket() throws IOException {
        inputStream.close();
        out.close();
        clientSocket.close();
    }

    public Socket getSocket() {
        return clientSocket;
    }

    public OutputStream getWriter() {
        return out;
    }

    public InputStream getInputStream() {
        return inputStream;
    }






}