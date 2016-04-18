package com.eyelinecom.whoisd.sads2.telegram.content;

import com.eyelinecom.whoisd.sads2.telegram.content.AttributeReader.AttributeSet;
import com.google.common.base.Predicate;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

public class AttributeUtil {

  public static Predicate<AttributeSet> isBooleanSet(final String name) {
    return new Predicate<AttributeSet>() {
      @Override
      public boolean apply(AttributeSet attrs) { return attrs.getBoolean(name).or(false); }
    };
  }

  /**
   * Filter elements based on their attributes.
   */
  public static List<Element> filter(List<Element> elements, Predicate<AttributeSet> filter) {
    if (isEmpty(elements)) {
      return emptyList();
    }

    elements = new ArrayList<>(elements);

    for (Iterator<Element> iterator = elements.iterator(); iterator.hasNext(); ) {
      final Element element = iterator.next();

      if (!filter.apply(AttributeReader.getAttributes(element))) {
        iterator.remove();
      }
    }

    return elements;
  }

}
