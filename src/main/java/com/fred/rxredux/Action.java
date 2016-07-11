package com.fred.rxredux;

/**
 * Base action class. Every action contains a type.
 *
 * @param <T> Action type's class
 */
public class Action<T> {
  private T type;

  public Action(T type) {
    this.type = type;
  }

  public T getType() {
    return type;
  }

  public void setType(T type) {
    this.type = type;
  }
}
