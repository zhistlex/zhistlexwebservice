package org.hucompute.zhistlexws;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hucompute.zhistlexws.model.*;
import org.hucompute.zhistlexws.model.dummy.DummyDocumentCollection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static spark.Spark.*;

public class ZHistLexWS {

    private static Logger logger = LogManager.getLogger(ZHistLexWS.class);

    protected DocumentCollection documentCollection;

    public ZHistLexWS() throws IOException {
        initializeDatabase();
        initializeWebService();
    }

    protected void initializeDatabase() throws IOException {
        documentCollection = new DummyDocumentCollection(new File("data/dummy"));
    }

    protected void initializeWebService() {
        port(5080);
        get("/docs/*", (pRequest, pResponse) -> {
            return getDocumentIndexRequest(pRequest, pResponse);
        });
        get("/doc/*", (pRequest, pResponse) -> {
            return getDocumentRequest(pRequest, pResponse);
        });
    }

    protected String processDocsAPIDocs(Request pRequest, Response pResponse, PathList pPathList) throws RequestException {
        return "API";
    }

    protected String processDocAPIDocs(Request pRequest, Response pResponse, PathList pPathList) throws RequestException {
        return "API";
    }

    protected String processTexts(Request pRequest, Response pResponse, PathList pPathList) throws RequestException {
        if ((pPathList.size() != 2) || (!pPathList.get(0).getName().equals("texts"))) throw new RequestException("Invalid Path");
        String lPPN = pPathList.get(1).getName();
        List<Document> lDocuments = documentCollection.getDocuments(lPPN);
        String lResult = null;
        try {
            JSONObject lResultObject = new JSONObject();
            lResultObject.put("query", pPathList.toString());
            lResultObject.put("result_type", "text");
            lResultObject.put("result_count", lDocuments.size());
            JSONArray lArray = new JSONArray();
            for (Document lDocument:lDocuments) {
                lArray.put(lDocument.getTEI());
            }
            lResultObject.put("result_set", lArray);
            lResult = lResultObject.toString(2);
            pResponse.type("application/json");
        }
        catch (JSONException e) {
            throw new RequestException(e);
        }
        return lResult;
    }

    protected String processRecords(Request pRequest, Response pResponse, PathList pPathList) throws RequestException {
        List<Document> lDocuments = documentCollection.getDocuments(pPathList.get(0));
        List<String> lSubList = new ArrayList<>();
        if (pPathList.size() == 2) {
            for (Document lDocument:lDocuments) {
                List<String> lSubListCandidates = new ArrayList<>();
                switch (pPathList.get(1).getName()) {
                    case "authors": {
                        lSubListCandidates.addAll(lDocument.getAuthors());
                        break;
                    }
                    case "titles": {
                        lSubListCandidates.add(lDocument.getTitle());
                        break;
                    }
                    case "ppns": {
                        lSubListCandidates.add(lDocument.getPPN());
                        break;
                    }
                    case "years": {
                        lSubListCandidates.add(Integer.toString(lDocument.getPublicationYear()));
                        break;
                    }
                }
                Iterator<String> lIterator = lSubListCandidates.iterator();
                while (lIterator.hasNext()) {
                    String lValue = lIterator.next();
                    boolean lPassed = true;
                    for (MatrixParameter lMatrixParameter:pPathList.get(1).getMatrixParameters()) {
                        if (!pPathList.get(1).getName().equals(lMatrixParameter.getBase())) {
                            throw new RequestException("Invalid base for Matrix Parameter at Path-Index 1: "+lMatrixParameter.getBase());
                        }
                        switch (lMatrixParameter.getKey()) {
                            case "fulltext": {
                                if (!lValue.contains(lMatrixParameter.getValue())) {
                                    lPassed = false;
                                }
                                break;
                            }
                            case "regex": {
                                if (!lValue.matches(lMatrixParameter.getValue())) {
                                    lPassed = false;
                                }
                                break;
                            }
                            case "year_min": {
                                if (!pPathList.get(1).getName().equals("years")) {
                                    throw new RequestException("Can not use year_min on "+pPathList.get(1).getName());
                                }
                                if (Integer.parseInt(lValue) < Integer.parseInt(lMatrixParameter.getValue())) {
                                    lPassed = false;
                                }
                            }
                            case "year_max": {
                                if (!pPathList.get(1).getName().equals("years")) {
                                    throw new RequestException("Can not use year_max on "+pPathList.get(1).getName());
                                }
                                if (Integer.parseInt(lValue) > Integer.parseInt(lMatrixParameter.getValue())) {
                                    lPassed = false;
                                }
                            }
                            default: {
                                throw new RequestException("Invalid key for Matrix Parameter at Path-Index 1: "+lMatrixParameter.getKey());
                            }
                        }
                        if (!lPassed) break;
                    }
                    if (!lPassed) {
                        lIterator.remove();
                    }
                }
                lSubList.addAll(lSubListCandidates);
            }
            lSubList = new ArrayList<>(new TreeSet<>(lSubList));
        }
        else if (pPathList.size() > 2) {
            throw new RequestException("Invalid PathElement: "+pPathList.get(2));
        }
        JSONObject lResultObject = new JSONObject();
        if (pPathList.size() == 1) {
            Collections.sort(lDocuments);
            try {
                lResultObject.put("query", pPathList.toString());
                lResultObject.put("result_type", "record_list");
                lResultObject.put("result_count", lDocuments.size());
                int lStart = pPathList.getUrlParameters().containsKey("offset") ? Integer.parseInt(pPathList.getUrlParameters().get("offset")) : 0;
                int lEnd = pPathList.getUrlParameters().containsKey("limit") ? lStart + Integer.parseInt(pPathList.getUrlParameters().get("limit")) : lDocuments.size();
                JSONArray lArray = new JSONArray();
                lResultObject.put("result_set", lArray);
                for (int i=lStart; i<lEnd; i++) {
                    lArray.put(lDocuments.get(i).getDublicCore());
                }
            } catch (JSONException e) {
                throw new RequestException(e);
            }
        }
        else {
            Collections.sort(lSubList);
            try {
                lResultObject.put("query", pPathList.toString());
                lResultObject.put("result_type", pPathList.get(1).getName()+"_list");
                lResultObject.put("result_count", lSubList.size());
                int lStart = pPathList.getUrlParameters().containsKey("offset") ? Integer.parseInt(pPathList.getUrlParameters().get("offset")) : 0;
                int lEnd = pPathList.getUrlParameters().containsKey("limit") ? lStart + Integer.parseInt(pPathList.getUrlParameters().get("limit")) : lSubList.size();
                JSONArray lArray = new JSONArray();
                lResultObject.put("result_set", lArray);
                for (int i=lStart; i<lEnd; i++) {
                    lArray.put(lSubList.get(i));
                }
            } catch (JSONException e) {
                throw new RequestException(e);
            }
        }
        String lResult = null;
        try {
            lResult = lResultObject.toString(2);
        }
        catch (JSONException e) {
            throw new RequestException(e);
        }
        pResponse.type("application/json");
        return lResult;
    }

    protected String getDocumentIndexRequest(Request pRequest, Response pResponse) throws RequestException {
        String lResult = null;
        try {
            String lPath = pRequest.raw().getRequestURI();
            lPath = lPath.substring(6)+"?"+pRequest.queryString();
            lPath = lPath.replace("%5E", "^");
            PathList lPathList = new PathList(lPath);
            logger.info(lPathList.toString());
            if (lPathList.size() == 0) {
                throw new RequestException("Query Path empty");
            }
            switch (lPathList.get(0).getName()) {
                case "records": {
                    lResult = processRecords(pRequest, pResponse, lPathList);
                    break;
                }
                case "api-docs": {
                    lResult = processDocsAPIDocs(pRequest, pResponse, lPathList);
                    break;
                }
            }
        }
        catch (RequestException e) {
            logger.error(e.getMessage(), e);
        }
        return lResult;
    }

    protected String getDocumentRequest(Request pRequest, Response pResponse) {
        String lResult = null;
        try {
            String lPath = pRequest.raw().getRequestURI();
            lPath = lPath.substring(5)+"?"+(pRequest.queryString() != null ? pRequest.queryString() : "");
            lPath = lPath.replace("%5E", "^");
            PathList lPathList = new PathList(lPath);
            logger.info(lPathList.toString());
            if (lPathList.size() == 0) {
                throw new RequestException("Query Path empty");
            }
            switch (lPathList.get(0).getName()) {
                case "texts": {
                    lResult = processTexts(pRequest, pResponse, lPathList);
                    break;
                }
                case "api-docs": {
                    lResult = processDocAPIDocs(pRequest, pResponse, lPathList);
                    break;
                }
            }
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return lResult;
    }

    public static void main(String[] args) throws Exception {
        // localhost:5080/docs/records;year=1950;pnn(regex=^12345$)/authors;fulltext=Scheler?foo1=bar;foobar2=lol&foo3=ende
        ZHistLexWS lZHistLexWS = new ZHistLexWS();
    }

}
