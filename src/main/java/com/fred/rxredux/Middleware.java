package com.fred.rxredux;

import rx.functions.Func3;

/**
 * Middleware contract
 */
public interface Middleware<A extends Action, S extends State>
    extends Func3<Store<S, A>, A, Dispatch<A, S>, S> {
}
