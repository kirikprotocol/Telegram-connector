package com.eyelinecom.whoisd.sads2.telegram.api.types;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Function;

import java.io.IOException;

@JsonSerialize(using = TextButton.Serializer.class)
public class TextButton extends KeyboardButton<TextButton> {

  public static final Function<String, TextButton> FROM_STRING =
      new Function<String, TextButton>() {
        @Override public TextButton apply(String value) { return new TextButton(value); }
      };

  public TextButton(String text) {
    setText(text);
  }


  public static class Serializer extends JsonSerializer<TextButton>  {
    @Override
    public void serialize(TextButton value,
                          JsonGenerator jgen,
                          SerializerProvider provider) throws IOException {

      jgen.writeString(value.getText());
    }
  }

}
