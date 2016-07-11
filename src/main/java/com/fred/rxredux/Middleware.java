package com.fred.rxredux;

import rx.Observable;

/**
 * Middleware contract
 */
public interface Middleware {
  <S, T> Observable<S> apply(S currentState, Action<T> action);
}
