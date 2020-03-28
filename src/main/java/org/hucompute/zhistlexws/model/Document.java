package org.hucompute.zhistlexws.model;

import java.util.List;

public interface Document extends Comparable<Document> {

    public String getTEI();

    public String getType();

    public String getPPN();

    public List<String> getAuthors();

    public List<String> getEditors();

    public String getTitle();

    public String getAddress();

    public String getPublisher();

    public int getPublicationYear();

    public String getVersion();

    public boolean isContainingFullText(String pSubstring);

    public boolean isMatchFulltextRegEx(String pRegex);

    public String getDublicCore();
}
