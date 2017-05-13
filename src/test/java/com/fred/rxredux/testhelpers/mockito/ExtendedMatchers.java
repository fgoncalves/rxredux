package com.fred.rxredux.testhelpers.mockito;

import com.fred.rxredux.Action;
import com.fred.rxredux.Dispatch;
import com.fred.rxredux.State;
import com.fred.rxredux.Store;
import io.reactivex.disposables.Disposable;
import org.mockito.ArgumentMatcher;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;

/**
 * Some common matchers used in several tests
 */
public class ExtendedMatchers {
  /**
   * Match any argument of the type subscription
   */
  public static Disposable anySubscription() {
    return argThat(new InstanceOfDisposableMatcher());
  }

  /**
   * Match any argument of the type {@link State}
   */
  public static State anyState() {
    return argThat(new InstanceOfStateMatcher());
  }

  public static Store anyStore() {
    return any(Store.class);
  }

  public static Action anyAction() {
    return any(Action.class);
  }

  public static Dispatch anyDispatch() {
    return any(Dispatch.class);
  }

  private static class InstanceOfDisposableMatcher implements ArgumentMatcher<Disposable> {
    public boolean matches(Disposable argument) {
      return argument != null && Disposable.class.isAssignableFrom(argument.getClass());
    }
  }

  private static class InstanceOfStateMatcher implements ArgumentMatcher<State> {
    public boolean matches(State argument) {
      return argument != null && State.class.isAssignableFrom(argument.getClass());
    }
  }
}
