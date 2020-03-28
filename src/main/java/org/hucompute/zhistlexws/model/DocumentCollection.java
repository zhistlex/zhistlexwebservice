package org.hucompute.zhistlexws.model;

import java.util.List;

public interface DocumentCollection {

    public List<Document> getDocuments();

    public List<Document> getDocuments(PathElement pPathElement) throws RequestException;

    public List<Document> getDocuments(String pPPN);

}
