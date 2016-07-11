package com.fred.rxredux.transformers;

import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;

/**
 * Scheduler transformer that makes all operations observed and subscribed on the io scheduler -
 * {@link Schedulers#io()}
 */
public class IOToIOSchedulerTransformer implements SchedulerTransformer {
  private final Scheduler subscribeOnScheduler = Schedulers.io();
  private final Scheduler observeOnScheduler = Schedulers.io();

  public <T> Observable.Transformer<T, T> applySchedulers() {
    return new Observable.Transformer<T, T>() {
      public Observable<T> call(Observable<T> observable) {
        return observable.subscribeOn(subscribeOnScheduler).observeOn(observeOnScheduler);
      }
    };
  }
}
