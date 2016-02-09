package com.eyelinecom.whoisd.sads2.telegram.api;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.mapped.Configuration;
import org.codehaus.jettison.mapped.MappedNamespaceConvention;
import org.codehaus.jettison.mapped.MappedXMLStreamReader;
import org.codehaus.jettison.mapped.MappedXMLStreamWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;

public class MarshalUtils {

  public static <T> T unmarshal(JSONObject obj, Class<T> clazz)
      throws JAXBException, JSONException, XMLStreamException {

    final JAXBContext jc = JAXBContext.newInstance(clazz);
    final MappedNamespaceConvention con = new MappedNamespaceConvention(new Configuration());

    final XMLStreamReader xmlStreamReader = new MappedXMLStreamReader(obj, con);

    //noinspection unchecked
    return (T) jc.createUnmarshaller().unmarshal(xmlStreamReader);
  }

  public static <T> String marshal(T obj, Class<T> clazz) throws JAXBException {
    final JAXBContext jc = JAXBContext.newInstance(clazz);
    final MappedNamespaceConvention con = new MappedNamespaceConvention(new Configuration());

    final StringWriter writer = new StringWriter();

    final XMLStreamWriter xmlStreamWriter = new MappedXMLStreamWriter(con, writer);
    jc.createMarshaller().marshal(obj, xmlStreamWriter);

    return writer.toString();
  }

}
