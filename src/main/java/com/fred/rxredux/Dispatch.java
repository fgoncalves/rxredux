package com.fred.rxredux;

import io.reactivex.functions.Function;

/**
 * This is the interface for a dispatch function. It should receive an action and do something with
 * it.
 */
public interface Dispatch<A extends Action, S extends State> extends Function<A, S> {
}
