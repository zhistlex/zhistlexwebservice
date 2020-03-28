package org.hucompute.zhistlexws.model;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class PathList extends ArrayList<PathElement> {

    protected TreeMap<String, String> urlParameters = new TreeMap<>();

    public PathList(String pRawString) throws RequestException {
        String lLine = pRawString;
        if (lLine.contains("?")) {
            for (String lTuple:lLine.substring(lLine.indexOf("?")+1).split("[;&]", -1)) {
                if (lTuple.contains("=")) {
                    urlParameters.put(lTuple.substring(0, lTuple.indexOf("=")), lTuple.substring(lTuple.indexOf("=")+1));
                }
            }
            lLine = lLine.substring(0, lLine.indexOf("?"));
        }
        String[] lFields = lLine.split("/", -1);
        for (String lField:lFields) {
            if (lField.length() > 0) {
                add(new PathElement(lField));
            }
        }
    }

    public String toString() {
        StringBuilder lResult = new StringBuilder();
        for (PathElement lPathElement:this) {
            lResult.append("/"+lPathElement.getName());
            for (MatrixParameter lMatrixParameter:lPathElement.getMatrixParameters()) {
                lResult.append(";");
                if (lMatrixParameter.getBase().equals(lPathElement.getName())) {
                    lResult.append(lMatrixParameter.getKey()+"="+lMatrixParameter.getValue());
                }
                else {
                    lResult.append(lMatrixParameter.getBase()+"("+lMatrixParameter.getKey()+"="+lMatrixParameter.getValue()+")");
                }
            }
        }
        if (urlParameters.size() > 0) {
            lResult.append("?");
            for (String lKey:urlParameters.keySet()) {
                if (!(lResult.charAt(lResult.length()-1) == '?')) {
                    lResult.append("&");
                }
                lResult.append(lKey+"="+urlParameters.get(lKey));
            }
        }
        return lResult.toString();
    }

    public TreeMap<String, String> getUrlParameters() {
        return new TreeMap<>(urlParameters);
    }
}
