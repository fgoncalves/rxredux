package com.fred.rxredux;

import rx.functions.Func1;

/**
 * This is the interface for a dispatch function. It should receive an action and do something with
 * it.
 */
public interface Dispatch<A extends Action, S extends State> extends Func1<A, S> {
}
