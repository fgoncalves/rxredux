package com.fred.rxredux;

import rx.Observable;

/**
 * Reducer contract
 *
 * @param <S> State's class
 * @param <A> Action's class
 */
public interface Reducer<S extends State, A extends Action> {
  /**
   * Apply the action to the given current state and return an observable to the changes.
   *
   * @param action Action to apply to the current state
   * @param currentState Current app state
   * @return Next state
   */
  Observable<S> reduce(A action, S currentState);
}
