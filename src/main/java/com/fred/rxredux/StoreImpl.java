package com.fred.rxredux;

import com.fred.rxredux.transformers.IOToIOSchedulerTransformer;
import com.fred.rxredux.transformers.SchedulerTransformer;
import java.util.ArrayList;
import java.util.List;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.PublishSubject;

/**
 * Store implementation for the default store
 *
 * @param <S> State's class
 * @param <A> Action's class
 */
public class StoreImpl<S extends State, A extends Action> implements Store<S, A> {
  private Func2<A, S, S> mainDispatcher = new Func2<A, S, S>() {
    public S call(A a, S s) {
      return rootReducer.call(a, s);
    }
  };

  private final PublishSubject<S> stateSubject = PublishSubject.create();
  private final List<Middleware<S, A>> middlewares;
  private final Reducer<S, A> rootReducer;
  private final SchedulerTransformer actionStreamSchedulerTransformer;
  private final SchedulerTransformer subscriptionSchedulerTransformer;
  private S currentState;

  private Func1<A, Func1<A, S>> compose(final List<Func1<A, S>> toCompose) {
    if (toCompose.isEmpty()) {
      throw new IllegalStateException("Compose cannot be called with an empty list");
    }

    if (toCompose.size() == 1) {
      return new Func1<A, Func1<A, S>>() {
        public Func1<A, S> call(A a) {
          return toCompose.get(0);
        }
      };
    }

    Func1<A, Func1<A, S>> result = new Func1<A, Func1<A, S>>() {
      public Func1<A, S> call(A a) {
        return toCompose.get(0);
      }
    };
    for (Func1<A, S> func : toCompose) {
      final Func1<A, Func1<A, S>> finalResult = result;
      result = new Func1<A, Func1<A, S>>() {
        public Func1<A, S> call(A a) {
          return finalResult.call(a);
        }
      };
    }

    return result;
  }

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
      List<Middleware<S, A>> middlewares) {
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
        subscriptionSchedulerTransformer, new ArrayList<Middleware<S, A>>());
  }

  public StoreImpl(Reducer<S, A> rootReducer, S initialState,
      SchedulerTransformer actionStreamSchedulerTransformer,
      SchedulerTransformer subscriptionSchedulerTransformer, List<Middleware<S, A>> middlewares) {
    this.rootReducer = rootReducer;
    this.actionStreamSchedulerTransformer = actionStreamSchedulerTransformer;
    this.currentState = initialState;
    this.subscriptionSchedulerTransformer = subscriptionSchedulerTransformer;
    this.middlewares = middlewares;
  }

  public void dispatch(final A action) {

    Observable.from(middlewares).flatMap(new Func1<Middleware<S, A>, Observable<S>>() {
      public Observable<S> call(Middleware<S, A> middleware) {
        return middleware.apply(StoreImpl.this, currentState, action);
      }
    }).mergeWith(rootReducer.reduce(action, currentState))
        .last()
        .compose(actionStreamSchedulerTransformer.<S>applySchedulers())
        .subscribe(new Subscriber<S>() {
          public void onCompleted() {

          }

          public void onError(Throwable throwable) {
            stateSubject.onError(throwable);
          }

          public void onNext(S s) {
            currentState = s;
            stateSubject.onNext(s);
          }
        });
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
