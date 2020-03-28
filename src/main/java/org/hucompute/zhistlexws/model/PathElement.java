package org.hucompute.zhistlexws.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathElement {

    protected String name;
    protected List<MatrixParameter> matrixParameters;

    public PathElement(String pRawText) throws RequestException {
        try {
            Pattern lBasePattern = Pattern.compile("([A-Za-z0-9-_]+)\\(([A-Za-z0-9-_]+)=(.*)\\)");
            Pattern lSimplePattern = Pattern.compile("([A-Za-z0-9-_]+)=(.*)");
            if (pRawText.length() == 0) throw new RequestException("Empty PathElement");
            matrixParameters = new ArrayList<>();
            if (pRawText.contains(";")) {
                name = pRawText.substring(0, pRawText.indexOf(";"));
                String[] lFields = pRawText.substring(pRawText.indexOf(";") + 1).split(";", -1);
                for (String lField : lFields) {
                    Matcher lMatcher = lBasePattern.matcher(lField);
                    if (lMatcher.find()) {
                        matrixParameters.add(new MatrixParameter(lMatcher.group(1), lMatcher.group(2), lMatcher.group(3)));
                    }
                    else {
                        lMatcher = lSimplePattern.matcher(lField);
                        if (lMatcher.find()) {
                            matrixParameters.add(new MatrixParameter(name, lMatcher.group(1), lMatcher.group(2)));
                        }
                        else {
                            throw new RequestException("Invalid Matrix Paremeter Statement");
                        }
                    }
                }
            } else {
                name = pRawText;
            }
        }
        catch (Exception e) {
            throw new RequestException(e);
        }
    }

    public String getName() {
        return name;
    }

    public List<MatrixParameter> getMatrixParameters() {
        return new ArrayList<>(matrixParameters);
    }
}
