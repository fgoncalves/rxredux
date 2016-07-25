package com.fred.rxredux;

import rx.functions.Func2;

/**
 * Reducer contract
 *
 * @param <S> State's class
 * @param <A> Action's class
 */
public interface Reducer<S extends State, A extends Action> extends Func2<A, S, S> {
}
