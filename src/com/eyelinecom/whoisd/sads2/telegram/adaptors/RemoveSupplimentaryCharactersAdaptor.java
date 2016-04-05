package com.eyelinecom.whoisd.sads2.telegram.adaptors;

import com.eyelinecom.whoisd.sads2.adaptor.DocumentAdaptor;
import com.eyelinecom.whoisd.sads2.common.DocumentUtils;
import com.eyelinecom.whoisd.sads2.common.SADSLogger;
import com.eyelinecom.whoisd.sads2.content.ContentResponse;
import com.eyelinecom.whoisd.sads2.content.ContentResponseUtils;
import org.apache.commons.logging.Log;
import org.dom4j.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RemoveSupplimentaryCharactersAdaptor extends DocumentAdaptor {

  @Override
  public Document transform(Document document, ContentResponse content) throws Exception {

    String serviceId = content.getServiceScenario().getId();
    String subscriber = ContentResponseUtils.getAbonent(content);
    Log log = SADSLogger.getLogger(serviceId,subscriber,this.getClass().getSimpleName());

    try {
      String docStr = document.asXML();
      String changedText = removeSupplementaryCharacters(docStr,log);
      if(!docStr.equals(changedText)){
        return DocumentUtils.parseDocument(changedText.getBytes("utf-8"));
      }
    } catch (Exception e) {
      log.error(e);

    }
    return document;
  }

  private String removeSupplementaryCharacters(String str,Log log){
    /*
      The set of characters from U+0000 to U+FFFF is sometimes referred to as the Basic Multilingual Plane (BMP).
      Characters whose code points are greater than U+FFFF are called supplementary characters.
      The Java platform uses the UTF-16 representation in char arrays and in the String and StringBuffer classes.
      In this representation, supplementary characters are represented as a >>>PAIR of char values<<<, the first from the high-surrogates range, (\uD800-\uDBFF), the second from the low-surrogates range (\uDC00-\uDFFF).
       */

    List<Integer>supplementaryCharacterIndexes = new ArrayList<>();
    for(int i=0;i<str.length();i++){
      if(!Character.isBmpCodePoint(str.codePointAt(i))){
        supplementaryCharacterIndexes.add(i);
      }
    }
    if(supplementaryCharacterIndexes.size()>0){
      log.debug("there are " + supplementaryCharacterIndexes.size() + " supplementary characters in response document text");
      //sort in descending order
      Collections.reverse(supplementaryCharacterIndexes);
      StringBuilder builder = new StringBuilder(str);
      for(int i : supplementaryCharacterIndexes){
        //because supplementary characters are represented as a pair of chars
        builder.deleteCharAt(i);
        builder.deleteCharAt(i);
      }
      log.debug("after removing supplementary characters: \n\r");
      log.debug(builder.toString());
      return builder.toString();
    }
    return str;
  }
}
