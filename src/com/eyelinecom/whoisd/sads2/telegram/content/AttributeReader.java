package com.eyelinecom.whoisd.sads2.telegram.content;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.dom4j.Document;
import org.dom4j.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class AttributeReader {

  private static final String ATTRIBUTE_NAME = "attributes";

  private static final Cache<Document, AttributeReader> docCache =
      CacheBuilder.newBuilder().weakValues().build();

  private static final Cache<Element, AttributeSet> elemCache =
      CacheBuilder.newBuilder().weakValues().build();

  private final Document doc;

  public static AttributeReader forDocument(final Document doc) {
    try {

      return docCache.get(doc, new Callable<AttributeReader>() {
        @Override public AttributeReader call() { return new AttributeReader(doc); }
      });

    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  private AttributeReader(Document doc) {
    this.doc = checkNotNull(doc);
  }

  public AttributeSet getAttributes(final Element elem) {
    checkArgument(elem.getDocument().equals(doc));

    try {

      return elemCache.get(elem, new Callable<AttributeSet>() {
        @Override public AttributeSet call() { return getAttributes0(elem); }
      });

    } catch (ExecutionException e) {
      Throwables.propagateIfInstanceOf(e.getCause(), DocumentProcessingException.class);

      throw new DocumentProcessingException(e);
    }
  }

  private AttributeSet getAttributes0(Element elem) {
    final String value = elem.attributeValue(ATTRIBUTE_NAME);
    try {
      return new AttributeSetImpl().parse(value);

    } catch (Exception e) {
      throw new DocumentProcessingException("Failed processing element attributes:" +
          " path = [" + elem.getUniquePath() + "]," +
          " xml = [" + elem.asXML() + "]," +
          " attributes = [" + value + "]", e);
    }
  }


  //
  //
  //

  @SuppressWarnings("WeakerAccess")
  public interface AttributeSet {
    Optional<Boolean> getBoolean(String name) throws DocumentProcessingException;
  }


  //
  //
  //

  private static class AttributeSetImpl implements AttributeSet {

    private final Map<String, String> attributes = new HashMap<>();

    public AttributeSetImpl parse(String attributes) {
      if (isNotBlank(attributes)) {
        for (String part : attributes.split(";")) {
          final String[] keyValue = part.split(":");
          this.attributes.put(keyValue[0].trim(), keyValue[1].trim());
        }
      }

      return this;
    }

    @Override
    public Optional<Boolean> getBoolean(String name) {
      final String value = attributes.get(name);
      return Optional.fromNullable(value == null ? null : Boolean.parseBoolean(value));
    }
  }
}
