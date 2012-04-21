/**
 * 
 */
package server;

import java.util.Hashtable;

/**
 * Mappings and Constants for HttpServer and its sister classes.
 * 
 * @author Evan Schoenbach
 * @version March 1, 2012
 */
public class MapsAndConstants {
    
    private static Hashtable<String, String> extensionTable = fillExtensionHash();
    
    public static String getContentType(String ext) {
        return extensionTable.get(ext);
    }
    
    public static String getReasonPhrase(int statusCode) {
        if (statusCode == 200) {
            return "OK";
        } else if (statusCode == 400) {
            return "Bad Request";
        } else if (statusCode == 404) {
            return "Not Found";
        } else if (statusCode == 304) {
            return "Not Modified";
        } else if (statusCode == 403) {
            return "Forbidden";
        } else if (statusCode == 405) {
            return "Method Not Allowed";
        } else if (statusCode == 412) {
            return "Precondition Failed";
        } else if (statusCode == 500) {
            return "Unexpected Server Error";
        } else {
            return "Unknown status";
        }
    }
    
    /* Common media type extensions derived from Wikipedia
     * (en.wikipedia.org/w/index.php?title=Internet_media_type&oldid=473355510)
     * */
    private static Hashtable<String, String> fillExtensionHash() {

        Hashtable<String, String> extTable = new Hashtable<String, String>();
        
        /* Application types */
        extTable.put(".doc", "application/msword");
        extTable.put(".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        extTable.put(".js", "application/javascript");
        extTable.put(".pdf", "application/pdf");
        extTable.put(".ppt", "application/vnd.ms-powerpoint");
        extTable.put(".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        extTable.put(".ps", "application/postscript");
        extTable.put(".rss", "application/rss+xml");
        extTable.put(".sh", "application/x-shar");
        extTable.put(".swf", "application/x-shockwave-flash");
        extTable.put(".tar", "application/x-tar");
        extTable.put(".tex", "application/x-latex");
        extTable.put(".ttf", "application/x-font-ttf");
        extTable.put(".xls", "application/vnd.ms-excel");
        extTable.put(".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        extTable.put(".xml", "application/xml");
        extTable.put(".zip", "application/zip");

        /* Audio types */
        extTable.put(".mp3", "audio/mpeg");
        extTable.put(".wma", "audio/x-ms-wma");
        extTable.put(".wav", "audio/vnd.wave");

        /* Image types */
        extTable.put(".gif", "image/gif");
        extTable.put(".jpeg", "image/jpeg");
        extTable.put(".jpg", "image/jpeg");
        extTable.put(".png", "image/png");
        //              extTable.put(".png", "application/octet-stream");
        extTable.put(".tiff", "image/tiff");

        /* Text types */
        extTable.put(".c", "text/plain");
        extTable.put(".cc", "text/plain");
        extTable.put(".css", "text/css");
        extTable.put(".csv", "text/csv");
        extTable.put(".c++", "text/plain");
        extTable.put(".h", "text/plain");
        extTable.put(".htm", "text/html");
        extTable.put(".html", "text/html");
        extTable.put(".java", "text/plain");
        extTable.put(".pl", "text/plain");
        extTable.put(".txt", "text/plain");

        /* Video types */
        extTable.put(".mpeg", "video/mpeg");
        extTable.put(".mpg", "video/mpeg");
        extTable.put(".mp4", "video/mp4");
        extTable.put(".mpeg", "video/mpeg");
        extTable.put(".mov", "video/quicktime");
        extTable.put(".qt", "video/quicktime");
        extTable.put(".wmv", "video/x-ms-wmv");
        
        return extTable;
    }
}
