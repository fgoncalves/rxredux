package com.fred.rxredux;

import rx.Subscriber;
import rx.Subscription;

/**
 * Store contract
 *
 * @param <S> State's class
 * @param <A> Action's class
 */
public interface Store<S extends State, A extends Action> {
  /**
   * Dispatch an action to this store.
   *
   * @param action The action to dispatch
   */
  void dispatch(A action);

  /**
   * Subscribe to the store's state changes.
   *
   * @param stateSubscriber The state subscriber that will be notified once the state changes
   * @return The subscription so the client can unsubscribe
   */
  Subscription subscribe(Subscriber<S> stateSubscriber);

  /**
   * Get the store's current state
   *
   * @return Store's current state
   */
  S state();
}
