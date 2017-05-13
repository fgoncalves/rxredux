package com.fred.rxredux;

import io.reactivex.functions.Function3;

/**
 * Middleware contract
 */
public interface Middleware<A extends Action, S extends State>
    extends Function3<Store<S, A>, A, Dispatch<A, S>, S> {
}
