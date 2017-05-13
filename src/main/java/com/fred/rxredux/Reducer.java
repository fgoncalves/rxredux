package com.fred.rxredux;

import io.reactivex.functions.BiFunction;

/**
 * Reducer contract
 *
 * @param <S> State's class
 * @param <A> Action's class
 */
public interface Reducer<S extends State, A extends Action> extends BiFunction<A, S, S> {
}
