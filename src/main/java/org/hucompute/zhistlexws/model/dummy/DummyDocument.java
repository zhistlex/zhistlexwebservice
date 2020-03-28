package org.hucompute.zhistlexws.model.dummy;

import org.hucompute.zhistlexws.StringUtil;
import org.hucompute.zhistlexws.model.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;

import javax.security.auth.callback.CallbackHandler;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class DummyDocument implements Document {

    protected File xmlFile;
    protected org.jdom2.Document document;
    protected String rawXML = "<TEI/>";
    protected String type = "script";
    protected String ppn = "";
    protected List<String> authors = new ArrayList<>();
    protected List<String> editors = new ArrayList<>();
    protected String title = "";
    protected String address = "";
    protected int publicationYear = 0;
    protected String version = "1.0";
    protected String publisher = "";

    public DummyDocument(File pXMLFile) throws IOException {
        xmlFile = pXMLFile;
        initialize();
    }

    protected void initialize() throws IOException {
        BufferedReader lReader = new BufferedReader(new InputStreamReader(new FileInputStream(xmlFile), Charset.forName("UTF-8")));
        StringBuilder lContent = new StringBuilder();
        char[] lBuffer = new char[65535];
        int lRead = 0;
        while ((lRead = lReader.read(lBuffer)) > 0) {
            lContent.append(lBuffer, 0, lRead);
        }
        lReader.close();
        rawXML = lContent.toString();
        try {
            document = new SAXBuilder().build(xmlFile);
            for (Element lElement:document.getDescendants(new ElementFilter("idno"))) {
                if ((lElement.getAttributeValue("type") != null) && (lElement.getAttributeValue("type").equals("ppn_gvk"))) {
                    ppn = lElement.getText();
                }
            }
            for (Element lElement:document.getDescendants(new ElementFilter("author"))) {
                authors.add(lElement.getText());
            }
            Element lSourceDescElement = null;
            for (Element lElement:document.getDescendants(new ElementFilter("sourceDesc"))) {
                lSourceDescElement = lElement;
                break;
            }
            for (Element lElement:lSourceDescElement.getDescendants(new ElementFilter("title"))) {
                title = lElement.getText();
            }
            String lForename = "";
            String lSurname = "";
            for (Element lElement:lSourceDescElement.getDescendants(new ElementFilter("forename"))) {
                lForename = lElement.getText();
            }
            for (Element lElement:lSourceDescElement.getDescendants(new ElementFilter("surname"))) {
                lSurname = lElement.getText();
            }
            String lEditor = (lForename+" "+lSurname).trim();
            if (lEditor.length() > 0) {
                editors.add(lEditor);
            }
            for (Element lElement:lSourceDescElement.getDescendants(new ElementFilter("pubPlace"))) {
                address = lElement.getText();
            }
            for (Element lElement:lSourceDescElement.getDescendants(new ElementFilter("date"))) {
                String lText = lElement.getText();
                if (lText.contains("=")) lText = lText.substring(0, lText.indexOf("=")).trim();
                publicationYear = Integer.parseInt(lText);
            }
            for (Element lElement:lSourceDescElement.getDescendants(new ElementFilter("publisher"))) {
                publisher = lElement.getText();
            }
        }
        catch (JDOMException e) {
            throw new IOException(e);
        }
    }

    @Override
    public String getTEI() {
        return rawXML;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getPPN() {
        return ppn;
    }

    @Override
    public List<String> getAuthors() {
        return authors;
    }

    @Override
    public List<String> getEditors() {
        return editors;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public String getPublisher() {
        return null;
    }

    @Override
    public int getPublicationYear() {
        return publicationYear;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public boolean isContainingFullText(String pSubstring) {
        return rawXML.contains(pSubstring);
    }

    @Override
    public boolean isMatchFulltextRegEx(String pRegex) {
        return rawXML.matches(pRegex);
    }

    @Override
    public int compareTo(Document o) {
        return ppn.compareTo(o.getPPN());
    }

    @Override
    public String getDublicCore() {
        StringBuilder lResult = new StringBuilder();
        lResult.append("<oai_dc:dc \n" +
                "     xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\" \n" +
                "     xmlns:dc=\"http://purl.org/dc/elements/1.1/\" \n" +
                "     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
                "     xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/oai_dc/ \n" +
                "     http://www.openarchives.org/OAI/2.0/oai_dc.xsd\">\n");
        if ((getTitle() != null) && (getTitle().length() > 0)) lResult.append("<dc:title>"+ StringUtil.encodeXML(getTitle())+"</dc:title>\n");
        for (String lAuthor:getAuthors()) {
            lResult.append("<dc:creator>"+ StringUtil.encodeXML(lAuthor)+"</dc:creator>\n");
        }
        for (String lEditor:getEditors()) {
            lResult.append("<dc:contributor>"+ StringUtil.encodeXML(lEditor)+"</dc:contributor>\n");
        }
        if ((getType() != null) && (getType().length() > 0)) lResult.append("<dc:type>"+ StringUtil.encodeXML(getType())+"</dc:type>\n");
        if (getPublicationYear() != 0) lResult.append("<dc:date>"+ getPublicationYear()+"</dc:date>\n");
        if ((getPublisher() != null) && (getPublisher().length() > 0)) lResult.append("<dc:publisher>"+ StringUtil.encodeXML(getPublisher())+"</dc:publisher>\n");
        if ((getPPN() != null) && (getPPN().length() > 0)) lResult.append("<dc:identifier>"+ StringUtil.encodeXML(getPPN())+"</dc:identifier>\n");
        lResult.append("</oai_dc:dc>");
        return lResult.toString();
    }
}
