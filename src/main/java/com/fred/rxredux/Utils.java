package com.fred.rxredux;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple class that has some util methods
 */
public class Utils {
  public interface MapCallbacks<A, B> {
    B map(A object);
  }

  public interface ReduceCallbacks<T> {
    T reduce(T previous, T current);
  }

  public static <A, B> List<B> map(List<A> toMap, MapCallbacks<A, B> mapCallbacks) {
    if (toMap.isEmpty()) return new ArrayList<B>();

    List<B> result = new ArrayList<B>();
    for (A object : toMap) {
      result.add(mapCallbacks.map(object));
    }
    return result;
  }

  public static <T> T reduceRight(List<T> toReduce, ReduceCallbacks<T> reduceCallbacks) {
    if (toReduce.isEmpty()) throw new IllegalArgumentException("Cannot reduce empty list");

    int index = toReduce.size() - 1;
    T result = toReduce.get(index);
    if (toReduce.size() == 1) return result;

    index--;
    for (; index >= 0; index--) {
      result = reduceCallbacks.reduce(result, toReduce.get(index));
    }

    return result;
  }
}
