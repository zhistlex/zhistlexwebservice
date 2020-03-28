package org.hucompute.zhistlexws.model.dummy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hucompute.zhistlexws.model.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DummyDocumentCollection implements DocumentCollection {

    private static Logger logger = LogManager.getLogger(DummyDocumentCollection.class);

    protected List<Document> documents;
    protected File directory;

    public DummyDocumentCollection(File pDirectory) throws IOException {
        directory = pDirectory;
        initialize();
    }

    protected void initialize() throws IOException {
        documents = new ArrayList<>();
        for (File lFile:directory.listFiles()) {
            if (lFile.isFile() && lFile.getName().toLowerCase().endsWith(".xml")) {
                logger.info("Loading Document: "+lFile.getAbsolutePath());
                documents.add(new DummyDocument(lFile));
            }
        }
    }

    @Override
    public List<Document> getDocuments() {
        return new ArrayList<>(documents);
    }

    @Override
    public List<Document> getDocuments(String pPPN) {
        List<Document> lResult = new ArrayList<>();
        for (Document lDocument:documents) {
            if (lDocument.getPPN().equals(pPPN)) {
                lResult.add(lDocument);
            }
        }
        return lResult;
    }

    @Override
    public List<Document> getDocuments(PathElement pPathElement) throws RequestException {
        List<Document> lResult = new ArrayList<>(documents);
        if (pPathElement.getMatrixParameters().size() > 0) {
            Iterator<Document> lIterator = lResult.iterator();
            while (lIterator.hasNext()) {
                Document lDocument = lIterator.next();
                for (MatrixParameter lMatrixParameter : pPathElement.getMatrixParameters()) {
                    boolean lPassed = true;
                    List<String> lParamBaseList = new ArrayList<>();
                    switch (lMatrixParameter.getBase()) {
                        case "records": {
                            switch (lMatrixParameter.getKey()) {
                                case "fulltext": {
                                    if (!lDocument.isContainingFullText(lMatrixParameter.getValue())) {
                                        lPassed = false;
                                    }
                                    break;
                                }
                                case "regex": {
                                    if (!lDocument.isMatchFulltextRegEx(lMatrixParameter.getValue())) {
                                        lPassed = false;
                                    }
                                    break;
                                }
                                case "type": {
                                    if (!lDocument.getType().equals(lMatrixParameter.getValue())) {
                                        lPassed = false;
                                    }
                                    break;
                                }
                                case "ppn": {
                                    if (!lDocument.getPPN().equals(lMatrixParameter.getValue())) {
                                        lPassed = false;
                                    }
                                    break;
                                }
                                case "author": {
                                    boolean lHasAtLeastOneMatch = false;
                                    for (String lAuthor:lDocument.getAuthors()) {
                                        if (lAuthor.equals(lMatrixParameter.getValue())) {
                                            lHasAtLeastOneMatch = true;
                                            break;
                                        }
                                    }
                                    if (!lHasAtLeastOneMatch) {
                                        lPassed = false;
                                    }
                                    break;
                                }
                                case "editor": {
                                    boolean lHasAtLeastOneMatch = false;
                                    for (String lEditor:lDocument.getEditors()) {
                                        if (lEditor.equals(lMatrixParameter.getValue())) {
                                            lHasAtLeastOneMatch = true;
                                            break;
                                        }
                                    }
                                    if (!lHasAtLeastOneMatch) {
                                        lPassed = false;
                                    }
                                    break;
                                }
                                case "title": {
                                    if (!lDocument.getTitle().equals(lMatrixParameter.getValue())) {
                                        lPassed = false;
                                    }
                                    break;
                                }
                                case "address": {
                                    if (!lDocument.getAddress().equals(lMatrixParameter.getValue())) {
                                        lPassed = false;
                                    }
                                    break;
                                }
                                case "publisher": {
                                    if (!lDocument.getPublisher().equals(lMatrixParameter.getValue())) {
                                        lPassed = false;
                                    }
                                    break;
                                }
                                case "year": {
                                    if (!Integer.toString(lDocument.getPublicationYear()).equals(lMatrixParameter.getValue())) {
                                        lPassed = false;
                                    }
                                    break;
                                }
                                case "year_min": {
                                    if (lDocument.getPublicationYear() < Integer.parseInt(lMatrixParameter.getValue())) {
                                        lPassed = false;
                                    }
                                    break;
                                }
                                case "year_max": {
                                    if (lDocument.getPublicationYear() > Integer.parseInt(lMatrixParameter.getValue())) {
                                        lPassed = false;
                                    }
                                    break;
                                }
                                case "version": {
                                    if (!lDocument.getVersion().equals(lMatrixParameter.getValue())) {
                                        lPassed = false;
                                    }
                                    break;
                                }
                                default: {
                                    throw new RequestException("Invalid key of Matrix Parameter: "+lMatrixParameter.getKey());
                                }
                            }
                            break;
                        }
                        case "type": {
                            lParamBaseList.add(lDocument.getType());
                            break;
                        }
                        case "ppn": {
                            lParamBaseList.add(lDocument.getPPN());
                            break;
                        }
                        case "author": {
                            lParamBaseList.addAll(lDocument.getAuthors());
                            break;
                        }
                        case "editor": {
                            lParamBaseList.addAll(lDocument.getEditors());
                            break;
                        }
                        case "title": {
                            lParamBaseList.add(lDocument.getTitle());
                            break;
                        }
                        case "address": {
                            lParamBaseList.add(lDocument.getAddress());
                            break;
                        }
                        case "publisher": {
                            lParamBaseList.add(lDocument.getPublisher());
                            break;
                        }
                        case "year": {
                            lParamBaseList.add(Integer.toString(lDocument.getPublicationYear()));
                            break;
                        }
                        default: {
                            throw new RequestException("Invalid base in Matrix Parameter: "+lMatrixParameter.getBase());
                        }
                    }
                    if (lParamBaseList.size() > 0) {
                        boolean lAtLeastOne = false;
                        for (String lString:lParamBaseList) {
                            switch (lMatrixParameter.getKey()) {
                                case "fulltext": {
                                    if (lString.contains(lMatrixParameter.getValue())) {
                                        lAtLeastOne = true;
                                    }
                                    break;
                                }
                                case "regex": {
                                    if (lString.matches(lMatrixParameter.getValue())) {
                                        lAtLeastOne = true;
                                    }
                                    break;
                                }
                                default: {
                                    throw new RequestException("Invalid key in Matrix Parameter: "+lMatrixParameter.getKey());
                                }
                            }
                        }
                        if (!lAtLeastOne) {
                            lPassed = false;
                        }
                    }
                    if (!lPassed) {
                        lIterator.remove();
                        break;
                    }
                }
            }
        }
        return lResult;
    }
}
