package com.fred.rxredux;

import com.fred.rxredux.transformers.IOToIOSchedulerTransformer;
import com.fred.rxredux.transformers.SchedulerTransformer;
import java.util.Arrays;
import java.util.List;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

/**
 * Store implementation for the default store
 */
public class StoreImpl<S extends State> implements Store<S> {
  private final PublishSubject<S> stateSubject = PublishSubject.create();
  private final List<Middleware> middlewares;
  private final Reducer<S> rootReducer;
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
  public static <S extends State> Store<S> create(Reducer<S> rootReducer,
      S initialState, Middleware... middlewares) {
    return new StoreImpl<S>(rootReducer, initialState, new IOToIOSchedulerTransformer(),
        middlewares);
  }

  public StoreImpl(Reducer<S> rootReducer, S initialState, SchedulerTransformer transformer,
      Middleware... middlewares) {
    this.rootReducer = rootReducer;
    this.transformer = transformer;
    this.currentState = initialState;
    this.middlewares = Arrays.asList(middlewares);
  }

  public <T> void dispatch(final Action<T> action) {
    Observable.from(middlewares).flatMap(new Func1<Middleware, Observable<S>>() {
      public Observable<S> call(Middleware middleware) {
        return middleware.apply(currentState, action);
      }
    }).mergeWith(rootReducer.reduce(action, currentState))
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
