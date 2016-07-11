package com.fred.rxredux;

import rx.Observable;

/**
 * Reducer contract
 */
public interface Reducer<S extends State> {
  /**
   * Apply the action to the given current state and return an observable to the changes.
   *
   * @param action Action to apply to the current state
   * @param currentState Current app state
   * @param <T> Action type's class
   * @return Next state
   */
  <T> Observable<S> reduce(Action<T> action, S currentState);
}
