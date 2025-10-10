package com.aiproductsearch.search_service.util;

import java.util.List;
import java.util.Locale;

public class VectorUtils {

  public static String toVectorLiteral(List<Float> vec) {
    StringBuilder sb = new StringBuilder(12 * vec.size());
    sb.append('[');
    for (int i = 0; i < vec.size(); i++) {
      if (i > 0) sb.append(',');
      sb.append(String.format(Locale.ROOT, "%.6f", vec.get(i)));
    }
    sb.append(']');
    return sb.toString();
  }
}
