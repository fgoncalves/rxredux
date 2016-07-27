package com.fred.rxredux;

import com.fred.rxredux.transformers.IOToIOSchedulerTransformer;
import com.fred.rxredux.transformers.SchedulerTransformer;
import java.util.ArrayList;
import java.util.List;
import rx.Subscriber;
import rx.Subscription;
import rx.subjects.PublishSubject;

/**
 * Store implementation for the default store
 *
 * @param <S> State's class
 * @param <A> Action's class
 */
public class StoreImpl<S extends State, A extends Action> implements Store<S, A> {
  private final PublishSubject<S> stateSubject = PublishSubject.create();
  private final Reducer<S, A> rootReducer;
  private final SchedulerTransformer subscriptionSchedulerTransformer;
  private Dispatch<A, S> coreDispatch = new Dispatch<A, S>() {
    public S call(A action) {
      return rootReducer.call(action, state());
    }
  };
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
      List<Middleware<A, S>> middlewares) {
    return new StoreImpl<S, A>(rootReducer, initialState,
        subscriptionSchedulerTransformer, middlewares);
  }

  /**
   * Same as {@link #create(Reducer, State, SchedulerTransformer, List)} but adds no middleware
   */
  public static <S extends State, A extends Action> Store<S, A> create(Reducer<S, A> rootReducer,
      S initialState, SchedulerTransformer subscriptionSchedulerTransformer) {
    return new StoreImpl<S, A>(rootReducer, initialState,
        subscriptionSchedulerTransformer,
        new ArrayList<Middleware<A, S>>());
  }

  public StoreImpl(final Reducer<S, A> rootReducer, S initialState,
      SchedulerTransformer subscriptionSchedulerTransformer,
      List<Middleware<A, S>> middlewares) {
    this.rootReducer = rootReducer;
    this.currentState = initialState;
    this.subscriptionSchedulerTransformer = subscriptionSchedulerTransformer;

    coreDispatch = chainMiddlewares(middlewares);
  }

  private Dispatch<A, S> chainMiddlewares(List<Middleware<A, S>> middlewares) {
    List<Dispatch<A, S>> dispatchers = new ArrayList<Dispatch<A, S>>();
    dispatchers.add(coreDispatch);
    for (int i = middlewares.size() - 1; i >= 0; i--) {
      final Middleware<A, S> middleware = middlewares.get(i);
      final Dispatch<A, S> next = dispatchers.get(0);
      dispatchers.add(0, new Dispatch<A, S>() {
        public S call(A a) {
          return middleware.call(StoreImpl.this, a, next);
        }
      });
    }
    return dispatchers.get(0);
  }

  public void dispatch(final A action) {
    currentState = coreDispatch.call(action);
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
}
