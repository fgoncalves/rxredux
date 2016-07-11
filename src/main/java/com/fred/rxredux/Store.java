package com.fred.rxredux;

import rx.Subscriber;
import rx.Subscription;

/**
 * Store contract
 */
public interface Store<S extends State> {
  /**
   * Dispatch an action to this store.
   *
   * @param action The action to dispatch
   * @param <T> The action type's class
   */
  <T> void dispatch(Action<T> action);

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
