package org.hucompute.zhistlexws;

public class StringUtil {

    public static String encodeXML(String pString) {
        return pString.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

}
