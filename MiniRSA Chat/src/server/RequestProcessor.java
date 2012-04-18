package server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Processor for HTTP Requests.
 * 
 * @author Evan Schoenbach
 * @version March 1, 2012
 */
public class RequestProcessor {
        
    private String filePath;
    private BufferedReader in;
    private DataOutputStream out;
    private Socket clientSocket;
    // Document to be send if a directory is requested. Rendered early so can calculate content length for header
    private String directoryOutput;
    private final int BUF_SIZE = 8192;
    private byte[] buffer = new byte[BUF_SIZE];
    private final byte[] EOL = {(byte)'\r', (byte)'\n' };
    
    public void processRequest(Socket socket, File root) {

        clientSocket = socket;
        double httpVersion = 0;
        String methodName = null;
        boolean servlet = false;
        filePath = null; // reset filePath
        directoryOutput = null; // reset
        int statusCode = 500; // default: Unexpected Server Error
        
        /* Get input and output streams/readers */
        try {
            in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            out = new DataOutputStream(clientSocket.getOutputStream());
//            pout = new PrintStream(clientSocket.getOutputStream());
            clientSocket.setSoTimeout(HttpServer.getSocketTimeout());
        } catch (SocketException e1) {
            HttpServer.writeToLog("Error while opening IO streams/readers", e1);
            gracefulExit();
            return;
        } catch (IOException e1) {
            HttpServer.writeToLog("Error while opening IO streams/readers", e1);
            gracefulExit();
            return;
        }
        
        /* Read first line */
        String inputLine = null;
        try {
            inputLine = in.readLine();
            HttpServer.writeToLog("Reading: " + inputLine);
        } catch (IOException e1) {
            HttpServer.writeToLog("Error while reading input line", e1);
            gracefulExit();
            return;
        }
        // System.out.println(inputLine);

        /* Parse first line */
        List<String> inputTokens = new ArrayList<String>();
        if (inputLine != null) {
            for (String string : inputLine.split(" ")) {
                inputTokens.add(string);
            }
            if (inputTokens.size() < 2) {
                HttpServer.writeToLog("No request");
                gracefulExit();
                return;
            }
        } else {
            HttpServer.writeToLog("InputLine empty");
            gracefulExit();
            return;
        }
        
        /* Determine method */
        try {
            methodName = getMethod(inputTokens);
        } catch (Exception e) {
            HttpServer.writeToLog("Unknown Error in method retrieval", e);
            gracefulExit();
            return;
        }
        if (methodName.equals("OTHER"))
            statusCode = 405;

        /* Check for special URLs */
        if (inputLine.startsWith("GET /control")) {
            openControl(inputLine);
            gracefulIOClose();
            return;
        } else if (inputLine.startsWith("GET /shutdown")) {
            shutdownAll(inputLine);
            gracefulIOClose();
            return;
        } else if (inputLine.startsWith("GET /errorlog")) {
            displayErrorFile(inputLine);
            gracefulIOClose();
            return;
        }
        
        /* Determine HTTP version, and verify support */
        try {
            httpVersion = getVersion(inputTokens);
        } catch (Exception e) {
            HttpServer.writeToLog("Unknown Error in version retrieval", e);
            gracefulExit();
            return;
        }
        if (httpVersion == 0.0) {
            HttpServer.writeToLog("Unsupported HTTP version");
            // TODO add 505 error here
            gracefulExit();
            return;
        }
       
        /* Correct poorly written relative URL */
        if (!inputTokens.get(1).startsWith("/") && !inputTokens.get(1).startsWith("h")) {
            inputTokens.set(1, '/' + inputTokens.get(1));
        }
        
        /* Correct spaces */
        if (inputTokens.get(1).indexOf("%20") != -1) {
            String temp = inputTokens.get(1);
            temp = temp.replace("%20", " ");
            inputTokens.set(1, temp);
        }
       
        /* Support absolute URLs */
        if (inputTokens.get(1).startsWith("h")) {
            int colonDoubleSlash = inputTokens.get(1).indexOf("://");
            // Parsing out protocol and host name
            if (colonDoubleSlash != -1) {
                int startOfRelative = inputTokens.get(1).indexOf('/', colonDoubleSlash + 3);
                if (startOfRelative == -1) {
                    HttpServer.writeToLog("Error in parsing URI");
                    gracefulExit();
                    return;
                }
                inputTokens.set(1, inputTokens.get(1).substring(startOfRelative));
                if (inputTokens.get(1).contains(root.toString())) {
                    inputTokens.set(1, inputTokens.get(1).substring(root.toString().length()));
                } else {
                    statusCode = 403;
                }  
            }
        }
        
        /* Check if violates security loophole with "../" or "~", or tries to access "etc/passwd" */
        if (inputLine.contains("etc/passwd")) {
            statusCode = 403;
        } else if (inputLine.contains("..") || inputLine.contains("~")) {
            File testFile = new File(root + inputTokens.get(1));
            String testString = testFile.toString();
            if (testString.contains(root.toString())) {
                inputTokens.set(1, testString.substring(root.toString().length()));
            } else {
                statusCode = 403;
            }  
        }
        
        /* Exit early if attempting to access unauthorized URL */
        if (statusCode == 403) {
            try {
                sendError(statusCode, httpVersion);
            } catch (IOException e) {
                HttpServer.writeToLog("Error while sending 403 error", e);
                gracefulExit();
                return;
            }
            gracefulExit();
            return;
        }

        /* Retrieve file path */
        filePath = root + inputTokens.get(1);
        
        /* Verify file */
        File target = new File(filePath);
        boolean isFile = false;       
        if (target.isDirectory()) {
            statusCode = 200;
        } else if (target.isFile()) {
            isFile = true;  
            statusCode = 200;
        } else {
            HttpServer.writeToLog("Couldn't find file " + target.toString());
            try {
                sendError(404, httpVersion);
            } catch (IOException e) {
                HttpServer.writeToLog("Error sending 404", e);
            }
            gracefulExit();
            return;
        }

        if (statusCode == 200 && !target.exists())
            statusCode = 404;

        /* If HTTP 1.1, check Host and Modified headers */
        if (httpVersion == 1.1) {
            try {
                statusCode = checkForHttpHeaders(target, statusCode);
            } catch (IOException e) {
                HttpServer.writeToLog("Error reading HTTP headers", e);
                gracefulExit();
                return;
            }
        } else if (httpVersion == 1.0) {
            String line = null;
            try {
                /* ignore headers for HTTP/1.0 */
                while ((line = in.readLine()) != null && line.length() > 4) {continue;}
            } catch (IOException e) {
                HttpServer.writeToLog("Error while reading (ignoring) headers", e);
                gracefulExit();
                return;
            }
        } else if (httpVersion == 0.0) {
            HttpServer.writeToLog("Bad HTTP Version imput");
            gracefulExit();
            return;
        }

        /* Determine File extension */
        String ext;
        // System.out.println(filePath);
        if (isFile) {
            int indexOfExtension = filePath.lastIndexOf(".");
            if (indexOfExtension > 0) {
                ext = filePath.substring(indexOfExtension);
            } else {
                ext = ".txt";
            }
        } else {
            ext = ".html";
        }

        /* Send Initial Response Line (Status Line) */
        try {
            sendInitialResponse(statusCode, httpVersion);
        } catch (IOException e) {
            HttpServer.writeToLog("Error while writing initial response", e);
            gracefulExit();
            return;
        }
        if (statusCode != 200 && statusCode != 400 && statusCode != 404 && statusCode != 405) {
            gracefulExit();
            return;
        }
        if (statusCode == 200 || statusCode == 400) { // always send 200 OK so can display error message?
            try {
                sendHeaders(root, target, ext);
            } catch (IOException e) {
                HttpServer.writeToLog("Error while writing headers", e);
                gracefulExit();
                return;
            }
        }

        /* Rest of processing instructions */
        if (methodName.equals("GET")) {
            try {
                if (statusCode == 400) {
                    sendError(statusCode, httpVersion);
                } else if (statusCode == 200) {
                    // System.out.println("sending target");
                    sendTarget(target, isFile, httpVersion);
                } else if (statusCode == 405) {
                    sendError(statusCode, httpVersion);
                } else {
                    HttpServer.writeToLog("Unknown HTTP status code");
                    gracefulExit();
                }
            } catch (IOException e) {
                HttpServer.writeToLog("Error while writing target or error to client", e);
                gracefulExit();
                return;
            }
        }
        gracefulExit();
    }

    private int checkForHttpHeaders(File target, int statusCode) throws IOException {

        String line, hostLine = null, modLine = null, unmodLine = null;
        List<String> modTokens = new ArrayList<String>();
        List<String> unmodTokens = new ArrayList<String>();
        int prevHeader = 0; // 0 means unknown, 1 is host, 2 is if-mod, 3 is if-unmod. For multiline headers
        while ((line = in.readLine()) != null && line.length() > 4) {
             System.out.println("Reading line " + line);
            if (line.startsWith(" ")) { // support multiline headers
                switch (prevHeader) {
                case 0: break;
                case 1: hostLine = hostLine + line.trim(); break;
                case 2: modLine = modLine + line.trim(); break;
                case 3: unmodLine = unmodLine + line.trim(); break;
                }
            }
            if (line.toUpperCase().contains("Host: ".toUpperCase())) { // case-insensitive
                hostLine = line;
                prevHeader = 1;
            } else if (line.toUpperCase().contains("If-Modified-Since: ".toUpperCase())) {
                modLine = line;
                prevHeader = 2;
            } else if (line.toUpperCase().contains("If-Unmodified-Since: ".toUpperCase())) {
                unmodLine = line;
                prevHeader = 3;
            } else {
                prevHeader = 0;
            }
        }
        if (hostLine == null) {
            HttpServer.writeToLog("No Host header!");
            return 400;
        }
        if (modLine != null && statusCode == 200) { // corrected since submission
            for (String string : modLine.split(": ")) {
                modTokens.add(string);
            }
            Date ifModSince;
            try {
                ifModSince = convertToDate(modTokens.get(1));
            } catch (ParseException e) {
                HttpServer.writeToLog("Last-Modified header date error", e);
                return 304;
            }
            Date lastMod = new Date(target.lastModified());
            // System.out.println("If-Mod-Since: " + ifModSince);
            // System.out.println("Last modified: " + lastMod);
            if (lastMod.compareTo(ifModSince) <= 0) {
                System.err.println("Not modified since");
                return 304;
            }
        }
        if (unmodLine != null && statusCode == 200) { // corrected since submission
            for (String string : unmodLine.split(": ")) {
                unmodTokens.add(string);
            }
            Date ifUnmodSince;
            try {
                ifUnmodSince = convertToDate(unmodTokens.get(1));
            } catch (ParseException e) {
                HttpServer.writeToLog("Last-Modified header date error", e);
                return 412;
            }
            Date lastUnmod = new Date(target.lastModified());
            if (lastUnmod.compareTo(ifUnmodSince) <= 0) {
                System.err.println("Unmodified since");
                return 412;
            }
        }
        return statusCode;
    }

    private Date convertToDate(String date) throws ParseException {
        char[] dateChars = date.toCharArray();
        SimpleDateFormat format = null;
        if (dateChars[3] == ',') {
            format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
        } else if (dateChars[3] == ' ') {
            format = new SimpleDateFormat("EEEE, dd-MMM-yy HH:mm:ss zzz");
        } else {
            format = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
        }
        Date parsedDate = format.parse(date);
        return parsedDate;
    }

    private void sendTarget(File target, boolean isFile, double httpVersion) throws IOException {
        out.write(EOL); // to complete header
        if (isFile) {
            try {
                sendFile(target);
            } catch (FileNotFoundException e) {
                sendError(404, httpVersion);
                gracefulExit();
                return;
            }
        } else {
            sendDirectory();
        }
        out.flush();
    }

    private void sendError(int errorCode, double httpVersion) throws IOException {
        sendInitialResponse(200, httpVersion);
        sendHeaders();
        out.write(EOL); // to complete header
        switch (errorCode) {
            case 400:  out.writeBytes("<html><title>400 Bad Request</title><p>\n");
                       out.writeBytes("<body><h2>No Host: header received</h2>");
                       out.write(EOL);
                       out.writeBytes("HTTP 1.1 requests must include the Host: header.");
                       break;
            case 403:  out.writeBytes("<html><title>403 Forbidden</title><p>\n");
                       out.writeBytes("<body><h2>403 Error:  Content Forbidden</h2>");
                       break;
            case 404:  out.writeBytes("<html><title>404 Not Found</title><p>\n");
                       out.writeBytes("<body><h2>404 Error:  Client Not Found</h2>");
                       break;
            case 405:  out.writeBytes("<html><title>405 Method Not Allowed</title><p>\n");
                       out.writeBytes("<body><h2>405 Error:  Method Not Allowed</h2>");
                       out.writeBytes("<h4>Allow: GET, HEAD, POST</h4>");
                       break;
            default:   HttpServer.writeToLog("Unknown error code printing");
                       gracefulExit();
        }
        out.writeBytes("</body></html>");
        out.write(EOL);
        out.flush();
    }

    private void sendFile(File file) throws IOException {
        DataInputStream fileIn = new DataInputStream(
                new FileInputStream(file.getAbsolutePath()));
        int bytesRead;
        while ((bytesRead = fileIn.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        fileIn.close();
    }

    private void sendDirectory() throws IOException {
        out.writeBytes(directoryOutput);
    }

    private void sendInitialResponse(int statusCode, double httpVersion) throws IOException {
        String status = reasonPhrase(statusCode);
        out.writeBytes("HTTP/" + httpVersion + " " + statusCode + " " + status);
        out.write(EOL);
    }

    private void sendHeaders(File root, File target, String extension) throws IOException {

        /* Send Date: Header (required in HTTP/1.1) */
        DateFormat newFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
        newFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String newDate = newFormat.format(new Date());
        out.writeBytes("Date: " + newDate);
        out.write(EOL);
        
        /* Send Server: Header */
        out.writeBytes("Server: Evan's HTTP Server");
        out.write(EOL);
        
        /* Close persistent connection */
        out.writeBytes("Connection: close");
        out.write(EOL);
        
        /* Send file detail headers if sending a file or folder */
        if (target != null) {
            sendFileFolderHeaders(root, target, extension);
        }
    }
    
    private void sendHeaders() throws IOException {
        sendHeaders(null, null, null);
    }
    
    private void sendFileFolderHeaders(File root, File target, String extension) throws IOException {
        String contentType = getContentType(extension);
        out.writeBytes("Content-Type: " + contentType);
        out.write(EOL);
        
        long contentLength;
        if (target.isDirectory()) {
            buildDirectory(target, root);
            contentLength = directoryOutput.length();
        } else {
            contentLength = target.length();
        }
        out.writeBytes("Content-Length: " + contentLength); // this change fixed Broken Pipe issue (length was too small for directories)
        out.write(EOL);
        
        Date lastMod = new Date(target.lastModified());
        DateFormat newFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
        newFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String lastModString = newFormat.format(lastMod);
     
        out.writeBytes("Last-Modified: " + lastModString);
        out.write(EOL);
    }

    private void buildDirectory(File target, File root) {
        StringBuilder output = new StringBuilder("");
        String targetString = target.toString();
        File dir;
        int indexOfLastSlash = targetString.lastIndexOf("/");
        if (targetString.length() - indexOfLastSlash == 1) {
            int indexOfSecondToLastSlash = targetString.substring(0, indexOfLastSlash).lastIndexOf("/");
            dir = new File(targetString.substring(0, indexOfSecondToLastSlash));
        } else
            dir = new File(targetString);

        output.append("<html><title>Directory listing</title><p>\n<body>");
        String[] list = dir.list();
        
        File dirFilePath = null;
        int pathLen = 0;
        String postRootFilePath = null;
        
        for (int i = 0; list != null && i < list.length; i++) {
            dirFilePath = new File(dir, list[i]);

            /* Get file path after root */
            pathLen = root.toString().length();
            postRootFilePath = dirFilePath.toString().substring(pathLen);
            
            if (dirFilePath.isDirectory()) {
                output.append("<a href=\""+postRootFilePath+"/\">"+list[i]+"/</a><br>");
            } else {   
                output.append("<a href=\""+postRootFilePath+"\">"+list[i]+"</a><br");
            }
        }
        output.append("<p><hr><br><i>" + (new Date()) + "</i></body></html>");
        
        directoryOutput = output.toString();
    }
    
    private String getContentType(String extension) {
        String contentType = MapsAndConstants.getContentType(extension);

        if (contentType == null) {
            // return ("text/plain");
            return ("application/octet-stream");
        }
        return contentType;
    }

    private String reasonPhrase(int statusCode) {
        return MapsAndConstants.getReasonPhrase(statusCode);
    }

    private String getMethod(List<String> inputTokens) {
        String method;

        method = inputTokens.get(0);
        if (method.equals("GET")) {
            return "GET";
        } else if (method.equals("HEAD")) {
            return "HEAD";
        } else if (method.equals("POST")) {
            return "POST";
        } else {
            return "OTHER";
        }
    }

    private double getVersion(List<String> inputTokens) {
        String httpLine = inputTokens.get(2);
        String httpVersion = httpLine.substring(5,8);
        double version = Double.parseDouble(httpVersion);

        if (version == 1.1) {
            return 1.1;
        } else if (version == 1.0) {
            return 1.0;
        } else {
            /* we don't support this version */
            return 0.0;
        }
    }

    public String getFilePath() {
        return filePath;
    }

    private void openControl(String inputLine) {
        HttpServer.openControl(clientSocket, inputLine);
    }

    private void shutdownAll(String inputLine) {
        HttpServer.shutdownAll(clientSocket, inputLine);
        try {
            clientSocket.close();
        } catch (IOException e) {
            HttpServer.writeToLog("Error closing client socket during shutdown", e);
        }
    }
    
    private void displayErrorFile(String inputLine) {
        HttpServer.displayErrorLog(clientSocket, inputLine);
    }
    

    private void gracefulExit() {
        gracefulIOClose();
        try {
            clientSocket.close();
        } catch (IOException e) {
            HttpServer.writeToLog("Failed to close client socket", e);
        }
    }
    
    private void gracefulIOClose() {
        try {
            out.close();
        } catch (IOException e) {
            HttpServer.writeToLog("Failed to close output stream", e);
        }
        try {
            in.close();
        } catch (IOException e) {
            HttpServer.writeToLog("Failed to close input reader", e);
        }
    }
}

