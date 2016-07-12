package com.fred.rxredux;

import rx.Observable;

/**
 * Middleware contract
 */
public interface Middleware<S extends State, A extends Action> {
  /**
   * Apply the middleware to the given store
   *
   * @param store The store where there middleware exists
   * @param currentState The current state
   * @param action The action being dispatched
   * @return An observable for the middleware's actions
   */
  Observable<S> apply(Store<S, A> store, S currentState, A action);
}
