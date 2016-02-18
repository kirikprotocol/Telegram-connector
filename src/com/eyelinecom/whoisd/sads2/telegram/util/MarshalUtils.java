package com.eyelinecom.whoisd.sads2.telegram.util;

import org.apache.commons.collections.IteratorUtils;
import org.codehaus.jettison.json.JSONArray;
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
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang.StringUtils.uncapitalize;

public class MarshalUtils {

  public static JSONObject parse(String json) throws JSONException {
    return new JSONObject(json);
  }

  public static <T> T unmarshal(JSONObject obj, Class<T> clazz)
      throws JAXBException, JSONException, XMLStreamException {

    final JSONObject wrapper = new JSONObject();
    wrapper.put(getRootElementName(clazz), obj);

    final JAXBContext jc = JAXBContext.newInstance(clazz);
    final MappedNamespaceConvention con = new MappedNamespaceConvention(new Configuration());

    final XMLStreamReader xmlStreamReader = new MappedXMLStreamReader(wrapper, con);

    //noinspection unchecked
    return (T) jc.createUnmarshaller().unmarshal(xmlStreamReader);
  }

  public static <T> String marshal(T obj, Class<T> clazz) throws JAXBException, JSONException {
    final JAXBContext jc = JAXBContext.newInstance(clazz);
    final MappedNamespaceConvention con = new MappedNamespaceConvention(new Configuration());

    final StringWriter writer = new StringWriter();

    final XMLStreamWriter xmlStreamWriter = new MappedXMLStreamWriter(con, writer) {

      @Override
      public void writeAttribute(String prefix,
                                 String ns,
                                 String local,
                                 String value) throws XMLStreamException {
        // Omit type information for abstract property types.
        if (con.isElement(prefix, ns, local)) {
          super.writeAttribute(prefix, ns, local, value);
        }
      }
    };

    jc.createMarshaller().marshal(obj, xmlStreamWriter);

    // Support for anonymous two-dimensional arrays.
    final JSONObject root = new JSONObject(writer.toString());
    unwrapArrays2d("item", root);
    unwrapArrays1d("item", root);

    // Unwrap root element.
    return root.get(getRootElementName(clazz)).toString();
  }

  private static <T> String getRootElementName(Class<T> clazz) {
    return uncapitalize(clazz.getSimpleName());
  }

  private static JSONArray coerceArray(String itemName,
                                       JSONArray arr) throws JSONException {
    final List<JSONArray> nested = new ArrayList<>();

    for (int i = 0; i < arr.length(); i++) {
      final JSONObject obj = arr.optJSONObject(i);
      if (obj == null) { return null; }

      final JSONArray array = obj.optJSONArray(itemName);
      if (array == null) { return null; }

      nested.add(array);
    }

    return new JSONArray() {{ for (JSONArray innerArray : nested) put(innerArray); }};
  }

  private static void unwrapArrays2d(String itemName,
                                     JSONObject parent) throws JSONException {
    for (Object keyItem : IteratorUtils.toList(parent.keys())) {
      final String key = (String) keyItem;

      if (parent.optJSONObject(key) != null) {
        unwrapArrays2d(itemName, parent.getJSONObject(key));

      } else if (parent.optJSONArray(key) != null) {
        final JSONArray coerced = coerceArray(itemName, parent.getJSONArray(key));
        if (coerced != null) {
          parent.put(key, coerced);
        }
      }

    }
  }

  private static void unwrapArrays1d(String itemName,
                                     JSONObject parent) throws JSONException {
    for (Object keyItem : IteratorUtils.toList(parent.keys())) {
      final String key = (String) keyItem;

      final JSONObject obj = parent.optJSONObject(key);
      if (obj != null) {
        if (IteratorUtils.toList(obj.keys()).size() == 1 &&
            obj.keys().next().equals(itemName)) {

          final JSONArray coerced = new JSONArray();

          if (obj.optJSONArray(itemName) != null) {
            coerced.put(obj.getJSONArray(itemName));
            parent.put(key, coerced);

          } else {
            // Single item.
            final JSONArray wrapper = new JSONArray();
            wrapper.put(obj.get(itemName));

            coerced.put(wrapper);
            parent.put(key, coerced);
          }

        } else {
          unwrapArrays1d(itemName, obj);
        }
      }
    }
  }

}
