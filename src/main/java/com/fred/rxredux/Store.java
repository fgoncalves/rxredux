package com.fred.rxredux;

import io.reactivex.Observer;

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
  void dispatch(A action) throws Exception;

  /**
   * Subscribe to the store's state changes. To unsubscribe simply dispose of the disposable
   * received by the state subscriber.
   *
   * @param stateSubscriber The state subscriber that will be notified once the state changes
   */
  void subscribe(Observer<S> stateSubscriber);

  /**
   * Get the store's current state
   *
   * @return Store's current state
   */
  S state();
}
