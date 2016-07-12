package com.fred.rxredux;

import com.fred.rxredux.transformers.IOToIOSchedulerTransformer;
import com.fred.rxredux.transformers.SchedulerTransformer;
import java.util.ArrayList;
import java.util.List;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

/**
 * Store implementation for the default store
 *
 * @param <S> State's class
 * @param <T> Action type's class
 */
public class StoreImpl<S extends State, A extends Action> implements Store<S, A> {
  private final PublishSubject<S> stateSubject = PublishSubject.create();
  private final List<Middleware<S, A>> middlewares;
  private final Reducer<S, A> rootReducer;
  private final SchedulerTransformer transformer;
  private S currentState;

  /**
   * Create a store with the default config. This will effectively create a store that will use the
   * {@link IOToIOSchedulerTransformer io to io transformer} to subscribe and observe the events of
   * the middlewares
   *
   * @param <S> State's class
   * @param rootReducer Root reducer
   * @param middlewares Middlewares  @return A Store with the given configuration
   */
  public static <S extends State, A extends Action> Store<S, A> create(Reducer<S, A> rootReducer,
      S initialState, List<Middleware<S, A>> middlewares) {
    return new StoreImpl<S, A>(rootReducer, initialState, new IOToIOSchedulerTransformer(),
        middlewares);
  }

  /**
   * Same as {@link #create(Reducer, State, List)} but adds no middleware
   */
  public static <S extends State, A extends Action> Store<S, A> create(Reducer<S, A> rootReducer,
      S initialState) {
    return new StoreImpl<S, A>(rootReducer, initialState, new IOToIOSchedulerTransformer(),
        new ArrayList<Middleware<S, A>>());
  }

  public StoreImpl(Reducer<S, A> rootReducer, S initialState, SchedulerTransformer transformer,
      List<Middleware<S, A>> middlewares) {
    this.rootReducer = rootReducer;
    this.transformer = transformer;
    this.currentState = initialState;
    this.middlewares = middlewares;
  }

  public void dispatch(final A action) {
    Observable.from(middlewares).flatMap(new Func1<Middleware<S, A>, Observable<S>>() {
      public Observable<S> call(Middleware<S, A> middleware) {
        return middleware.apply(StoreImpl.this, currentState, action);
      }
    }).mergeWith(rootReducer.reduce(action, currentState))
        .last()
        .compose(transformer.<S>applySchedulers())
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
    return stateSubject.subscribe(stateSubscriber);
  }

  public S state() {
    return currentState;
  }
}
