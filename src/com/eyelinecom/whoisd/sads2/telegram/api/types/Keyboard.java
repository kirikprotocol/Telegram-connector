package com.eyelinecom.whoisd.sads2.telegram.api.types;

import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * @see ReplyKeyboardMarkup
 * @see ReplyKeyboardHide
 */
@XmlSeeAlso({ReplyKeyboardHide.class, ReplyKeyboardMarkup.class})
public abstract class Keyboard<T extends ApiType> extends ApiType<T> {

}
