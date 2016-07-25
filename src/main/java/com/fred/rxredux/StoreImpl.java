package com.fred.rxredux;

import com.fred.rxredux.transformers.IOToIOSchedulerTransformer;
import com.fred.rxredux.transformers.SchedulerTransformer;
import java.util.ArrayList;
import java.util.List;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

/**
 * Store implementation for the default store
 *
 * @param <S> State's class
 * @param <A> Action's class
 */
public class StoreImpl<S extends State, A extends Action> implements Store<S, A> {
  // Curried dispatcher -> calls root reducer
  private Func1<A, Func1<S, S>> mainDispatcher = new Func1<A, Func1<S, S>>() {
    public Func1<S, S> call(final A a) {
      return new Func1<S, S>() {
        public S call(S s) {
          return rootReducer.call(a, s);
        }
      };
    }
  };

  private final PublishSubject<S> stateSubject = PublishSubject.create();
  private final List<Func1<Func1<A, Func1<S, S>>, Func1<A, Func1<S, S>>>> middlewares;
  private final Reducer<S, A> rootReducer;
  private final SchedulerTransformer actionStreamSchedulerTransformer;
  private final SchedulerTransformer subscriptionSchedulerTransformer;
  private S currentState;

  /**
   * Create a store with the default config. This will effectively create a store that will use the
   * {@link IOToIOSchedulerTransformer io to io actionStreamSchedulerTransformer} to subscribe and
   * observe the events of the middlewares
   *
   * @param <S> State's class
   * @param rootReducer Root reducer
   * @param initialState Initial state for the store
   * @param subscriptionSchedulerTransformer Scheduler transformer for the store subscriptions
   * @param middlewares Middlewares  @return A Store with the given configuration
   */
  public static <S extends State, A extends Action> Store<S, A> create(Reducer<S, A> rootReducer,
      S initialState, SchedulerTransformer subscriptionSchedulerTransformer,
      List<Func1<Func1<A, Func1<S, S>>, Func1<A, Func1<S, S>>>> middlewares) {
    return new StoreImpl<S, A>(rootReducer, initialState,
        new IOToIOSchedulerTransformer(),
        subscriptionSchedulerTransformer, middlewares);
  }

  /**
   * Same as {@link #create(Reducer, State, SchedulerTransformer, List)} but adds no middleware
   */
  public static <S extends State, A extends Action> Store<S, A> create(Reducer<S, A> rootReducer,
      S initialState, SchedulerTransformer subscriptionSchedulerTransformer) {
    return new StoreImpl<S, A>(rootReducer, initialState,
        new IOToIOSchedulerTransformer(),
        subscriptionSchedulerTransformer,
        new ArrayList<Func1<Func1<A, Func1<S, S>>, Func1<A, Func1<S, S>>>>());
  }

  public StoreImpl(Reducer<S, A> rootReducer, S initialState,
      SchedulerTransformer actionStreamSchedulerTransformer,
      SchedulerTransformer subscriptionSchedulerTransformer,
      List<Func1<Func1<A, Func1<S, S>>, Func1<A, Func1<S, S>>>> middlewares) {
    this.rootReducer = rootReducer;
    this.actionStreamSchedulerTransformer = actionStreamSchedulerTransformer;
    this.currentState = initialState;
    this.subscriptionSchedulerTransformer = subscriptionSchedulerTransformer;
    this.middlewares = middlewares;
  }

  public void dispatch(final A action) {
    // TODO: encapsulate middleware
    currentState = mainDispatcher.call(action).call(state());
    stateSubject.onNext(currentState);
  }

  public Subscription subscribe(Subscriber<S> stateSubscriber) {
    return stateSubject
        .compose(subscriptionSchedulerTransformer.<S>applySchedulers())
        .subscribe(stateSubscriber);
  }

  public S state() {
    return currentState;
  }

  /**
   * Compose the different middlewares together with the main dispatcher
   *
   * @param toCompose The middlewares to compose
   * @return The composed function
   */
  private Func1<Func1<A, Func1<S, S>>, Func1<A, Func1<S, S>>> compose(
      final List<Func1<Func1<A, Func1<S, S>>, Func1<A, Func1<S, S>>>> toCompose) {
    if (toCompose.isEmpty()) {
      throw new IllegalStateException("Compose cannot be called with an empty list");
    }

    Func1<Func1<A, Func1<S, S>>, Func1<A, Func1<S, S>>> result = toCompose.get(0);

    if (toCompose.size() == 1) return result;

    for (final Func1<Func1<A, Func1<S, S>>, Func1<A, Func1<S, S>>> func : toCompose) {
      final Func1<Func1<A, Func1<S, S>>, Func1<A, Func1<S, S>>> finalResult = result;
      result = new Func1<Func1<A, Func1<S, S>>, Func1<A, Func1<S, S>>>() {
        public Func1<A, Func1<S, S>> call(Func1<A, Func1<S, S>> next) {
          return func.call(finalResult.call(next));
        }
      };
    }

    return result;
  }
}
