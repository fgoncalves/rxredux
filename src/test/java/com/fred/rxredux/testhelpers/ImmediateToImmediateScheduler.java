package com.fred.rxredux.testhelpers;

import com.fred.rxredux.transformers.SchedulerTransformer;
import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;

/**
 * Scheduler transformer that puts everything on the immediate scheduler
 */
public class ImmediateToImmediateScheduler implements SchedulerTransformer {
  private final Scheduler subscribeOnScheduler = Schedulers.immediate();
  private final Scheduler observeOnScheduler = Schedulers.immediate();

  public <T> Observable.Transformer<T, T> applySchedulers() {
    return new Observable.Transformer<T, T>() {
      public Observable<T> call(Observable<T> observable) {
        return observable.subscribeOn(subscribeOnScheduler).observeOn(observeOnScheduler);
      }
    };
  }
}
