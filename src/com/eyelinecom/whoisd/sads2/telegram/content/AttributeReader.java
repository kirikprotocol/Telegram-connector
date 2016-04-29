package com.eyelinecom.whoisd.sads2.telegram.content;

import com.eyelinecom.whoisd.sads2.exception.ExceptionMapper;
import com.google.common.base.Function;
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
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.trimToNull;

// TODO:  Consider implementing more efficient attribute parsing (no `split` calls).
public class AttributeReader {

  private static final String ATTRIBUTE_NAME = "attributes";

  private static final Cache<Document, AttributeReader> docCache =
      CacheBuilder.newBuilder().expireAfterAccess(5, SECONDS).build();

  private static final Cache<Element, AttributeSetImpl> elemCache =
      CacheBuilder.newBuilder().expireAfterAccess(5, SECONDS).build();

  private final Document doc;

  public static AttributeSet getAttributes(Element element) {
    return ExceptionMapper.proxy(
        forDocument(element.getDocument()).getElementAttributes(element),
        DocumentProcessingException.class,
        AttributeSet.class);
  }

  private static AttributeReader forDocument(final Document doc) {
    try {

      return docCache.get(doc, new Callable<AttributeReader>() {
        @Override public AttributeReader call() { return new AttributeReader(doc); }
      });

    } catch (ExecutionException e) {
      throw new DocumentProcessingException(e);
    }
  }

  private AttributeReader(Document doc) {
    this.doc = checkNotNull(doc);
  }

  private AttributeSetImpl getElementAttributes(final Element elem) {
    checkArgument(elem.getDocument().equals(doc));

    try {
      return elemCache.get(elem, new Callable<AttributeSetImpl>() {
        @Override
        public AttributeSetImpl call() {
          try {
            final AttributeSetImpl impl = new AttributeSetImpl();

            if (elem.getParent() != null) {
              impl.copy(getElementAttributes(elem.getParent()));
            }

            final String value = elem.attributeValue(ATTRIBUTE_NAME);
            return impl.parse(value);

          } catch (Exception e) {
            throw new DocumentProcessingException("Failed processing element attributes:" +
                " path = [" + elem.getUniquePath() + "]," +
                " xml = [" + elem.asXML() + "]", e);
          }
        }
      });

    } catch (ExecutionException e) {
      Throwables.propagateIfInstanceOf(e.getCause(), DocumentProcessingException.class);

      throw new DocumentProcessingException(e);
    }
  }


  //
  //
  //

  @SuppressWarnings("WeakerAccess")
  public interface AttributeSet {
    Optional<Boolean>   getBoolean(String name)   throws DocumentProcessingException;
    Optional<String>    getString(String name)    throws DocumentProcessingException;
    Optional<Integer>   getInteger(String name)   throws DocumentProcessingException;
  }


  //
  //
  //

  private static class AttributeSetImpl implements AttributeSet {

    private final Map<String, String> attributes = new HashMap<>();

    AttributeSetImpl copy(AttributeSetImpl other) {
      attributes.putAll(other.attributes);
      return this;
    }

    public AttributeSetImpl parse(String attributes) {
      if (isNotBlank(attributes)) {
        for (String part : attributes.split(";")) {
          final String[] keyValue = part.split(":");

          final String key = trimToNull(keyValue[0]);
          final String value = trimToNull(keyValue[1]);
          if (key != null && value != null) {
            this.attributes.put(key, value);
          }
        }
      }

      return this;
    }

    @Override
    public Optional<Boolean> getBoolean(String name) {
      final Function<String, Boolean> toBoolean = new Function<String, Boolean>() {
        @Override public Boolean apply(String input) { return Boolean.parseBoolean(input); }
      };

      return getString(name).transform(toBoolean);
    }

    @Override
    public Optional<String> getString(String name) {
      return Optional.fromNullable(attributes.get(name));
    }

    @Override
    public Optional<Integer> getInteger(String name) {
      final Function<String, Integer> toInteger = new Function<String, Integer>() {
        @Override public Integer apply(String input) { return Integer.parseInt(input); }
      };

      return getString(name).transform(toInteger);
    }
  }
}
