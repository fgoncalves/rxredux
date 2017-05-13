package com.fred.rxredux.transformers;

import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.Scheduler;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;

/**
 * Scheduler transformer that makes all operations observed and subscribed on the io scheduler -
 * {@link Schedulers#io()}
 */
public class IOToIOSchedulerTransformer implements SchedulerTransformer {
  private final Scheduler subscribeOnScheduler = Schedulers.io();
  private final Scheduler observeOnScheduler = Schedulers.io();

  public <T> ObservableTransformer<T, T> applyObservableSchedulers() {
    return new ObservableTransformer<T, T>() {
      public ObservableSource<T> apply(@NonNull io.reactivex.Observable<T> observable) {
        return observable.subscribeOn(subscribeOnScheduler).observeOn(observeOnScheduler);
      }
    };
  }
}
