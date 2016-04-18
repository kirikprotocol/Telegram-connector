package com.eyelinecom.whoisd.sads2.telegram.content;

import com.eyelinecom.whoisd.sads2.exception.ContentGenerationException;
import com.eyelinecom.whoisd.sads2.exception.DocumentGenerationException;

/**
 * Quite the same as {@linkplain ContentGenerationException} or {@linkplain DocumentGenerationException},
 * but an unchecked one.
 * TODO: consider merging these exceptions and making them unchecked.
 */
public class DocumentProcessingException extends RuntimeException {

  public DocumentProcessingException(Throwable cause) {
    super(cause);
  }

  public DocumentProcessingException(String message) {
    super(message);
  }

  public DocumentProcessingException(String message, Throwable cause) {
    super(message, cause);
  }
}
