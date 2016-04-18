package com.eyelinecom.whoisd.sads2.telegram.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class TypeUtil {

  public static <X> Class<X> getGenericType(Class<?> self, int argNo) {
    Type generic = self.getGenericSuperclass();
    if (generic instanceof Class) {
      generic = ((Class) generic).getGenericSuperclass();
    }

    final ParameterizedType genericSuperclass =
        (ParameterizedType) generic;
    //noinspection unchecked
    return (Class<X>) genericSuperclass.getActualTypeArguments()[argNo];
  }

}
