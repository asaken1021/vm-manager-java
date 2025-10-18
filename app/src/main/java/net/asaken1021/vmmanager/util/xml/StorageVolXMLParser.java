package net.asaken1021.vmmanager.util.xml;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class StorageVolXMLParser {
    private String volDirPath;

    public StorageVolXMLParser(String xmlDesc) throws XMLParserException{
        this.volDirPath = getVolDirPathFromXmlDesc(xmlDesc);
    }

    public String getVolDirPath() {
        return this.volDirPath;
    }

    private String getVolDirPathFromXmlDesc(String xmlDesc) throws XMLParserException {
        try (StringReader reader = new StringReader(xmlDesc)) {
            InputSource source = new InputSource(reader);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            Document document = builder.parse(source);
            XPath xpath = XPathFactory.newInstance().newXPath();

            return xpath.evaluate("//pool[@type='dir']/target/path/text()", document);
        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
            throw new XMLParserException(e);
        }
    }
}
