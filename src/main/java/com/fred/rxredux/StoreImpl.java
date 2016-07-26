package com.fred.rxredux;

import com.fred.rxredux.transformers.IOToIOSchedulerTransformer;
import com.fred.rxredux.transformers.SchedulerTransformer;
import java.util.ArrayList;
import java.util.List;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.functions.Func3;
import rx.subjects.PublishSubject;

/**
 * Store implementation for the default store
 *
 * @param <S> State's class
 * @param <A> Action's class
 */
public class StoreImpl<S extends State, A extends Action> implements Store<S, A> {
  private Middleware<A, S> coreMiddleware = new Middleware<A, S>() {
    public S call(Store<S, A> store, A action, Dispatch<A, S> dispatch) {
      return rootReducer.call(action, store.state());
    }
  };

  private final PublishSubject<S> stateSubject = PublishSubject.create();
  private final Reducer<S, A> rootReducer;
  private final SchedulerTransformer subscriptionSchedulerTransformer;
  private Dispatch<A, S> dispatch = new Dispatch<A, S>() {
    public S call(A a) {
      return coreMiddleware.call(StoreImpl.this, a, null);
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

  public StoreImpl(Reducer<S, A> rootReducer, S initialState,
      SchedulerTransformer subscriptionSchedulerTransformer,
      List<Middleware<A, S>> middlewares) {
    this.rootReducer = rootReducer;
    this.currentState = initialState;
    this.subscriptionSchedulerTransformer = subscriptionSchedulerTransformer;

    List<Middleware<A, S>> copyMiddlewares = new ArrayList<Middleware<A, S>>(middlewares);
    copyMiddlewares.add(coreMiddleware);
    coreMiddleware = Utils.reduceRight(copyMiddlewares,
        new Utils.ReduceCallbacks<Middleware<A, S>>() {
          public Middleware<A, S> reduce(final Middleware<A, S> previous,
              final Middleware<A, S> current) {
            return new Middleware<A, S>() {
              public S call(final Store<S, A> store, A action, final Dispatch<A, S> dispatch) {
                return current.call(store, action, new Dispatch<A, S>() {
                  public S call(A action) {
                    return previous.call(store, action, dispatch);
                  }
                });
              }
            };
          }
        });

    List<Func2<Store<S, A>, A, Func1<Dispatch<A, S>, Void>>> curryedMiddlewares =
        Utils.map(
            middlewares,
            new Utils.MapCallbacks<Middleware<A, S>, Func2<Store<S, A>, A, Func1<Dispatch<A, S>, Void>>>() {
              public Func2<Store<S, A>, A, Func1<Dispatch<A, S>, Void>> map(
                  final Middleware<A, S> middleware) {
                return new Func2<Store<S, A>, A, Func1<Dispatch<A, S>, Void>>() {
                  public Func1<Dispatch<A, S>, Void> call(final Store<S, A> store, final A action) {
                    return new Func1<Dispatch<A, S>, Void>() {
                      public Void call(Dispatch<A, S> dispatch) {
                        middleware.call(store, action, dispatch);
                      }
                    };
                  }
                };
              }
            }
        );

    Func3<Integer, Integer, Integer, Integer> sum =
        new Func3<Integer, Integer, Integer, Integer>() {
          public Integer call(Integer a, Integer b, Integer c) {
            return a + b + c;
          }
        };

    Func2<Integer, Integer, Func1<Integer, Integer>> curryedSum =
        new Func2<Integer, Integer, Func1<Integer, Integer>>() {
          public Func1<Integer, Integer> call(final Integer a, final Integer b) {
            return new Func1<Integer, Integer>() {
              public Integer call(Integer c) {
                return a + b + c;
              }
            };
          }
        };

    curryedSum.call(1, 3).call(4);
  }

  public void dispatch(final A action) {
    currentState = dispatch.call(action);
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
