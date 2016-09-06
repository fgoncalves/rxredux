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

Redux middlewares can be implemented by extending the interface middleware. This is simply a Rx Func3
which receives a store, an action and the next dispatch function in the middleware chain.

If the chain should not proceed, then the next dispatch function should not be called. Otherwise,
you should just call the next dispatch function and return the state.

## More info


## License

    Copyright 2013 Frederico Gonçalves

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
