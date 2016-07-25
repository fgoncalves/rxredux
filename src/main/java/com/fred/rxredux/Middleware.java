package com.fred.rxredux;

import rx.functions.Func2;

/**
 * Middleware contract
 */
public interface Middleware<S extends State, A extends Action>
    extends Func2<A, Middleware<S, A>, Middleware<S, A>> {
}
