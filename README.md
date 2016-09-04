# RxRedux

A simple implementation of the redux architecture in Java taking advantage of
[RxJava](https://github.com/ReactiveX/RxJava).

The library itself does not guarantee all the redux core concepts. Specifically immutability
of the state is quite complex to guarantee since there's no built in support for it in the
language. Moreover, the state classes are defined by whoever wants to use the library.
Therefore, it's up to the developer to decide if he/she wants to ensure immutability.

## Actions

All actions must inherit from the Action class. At this point, each action requires a type.
You can add more fields in your subclasses if you need to pass additional data to the reducers.

## State

The state class is actually the simplest on as it contains no implementation. You just need to
inherit from this class and you should make your subclasses immutable.

## Reducers

Reducers should implement the Reducer interface. This is just an extension of RxJava's Func2 class.
Reducers receive the action and the current state and should output the new state.

You can have more than one reducer, but it's up to you to call it in the proper occasions.

## Store

There should only be one store in your application, but again this is not guaranteed by this library.

You should use the create methods in order to create a store, but you're not required to do this.
A store will be created for a given action and state class which must be the same as the ones
used in your root reducer.

Use the store's dispatch method to dispatch the actions.

## Middleware


## More info
