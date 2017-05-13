package com.fred.rxredux.testhelpers;

import com.fred.rxredux.transformers.SchedulerTransformer;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

/**
 * Scheduler transformer that puts everything on the immediate scheduler
 */
public class ImmediateToImmediateScheduler implements SchedulerTransformer {
  private final Scheduler subscribeOnScheduler = Schedulers.trampoline();
  private final Scheduler observeOnScheduler = Schedulers.trampoline();

  public <T> ObservableTransformer<T, T> applyObservableSchedulers() {
    return new ObservableTransformer<T, T>() {
      public Observable<T> apply(Observable<T> observable) {
        return observable.subscribeOn(subscribeOnScheduler).observeOn(observeOnScheduler);
      }
    };
  }
}
