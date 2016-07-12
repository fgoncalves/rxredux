package com.fred.rxredux.testhelpers.mockito;

import com.fred.rxredux.Action;
import com.fred.rxredux.State;
import com.fred.rxredux.Store;
import org.mockito.ArgumentMatcher;
import rx.Subscription;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;

/**
 * Some common matchers used in several tests
 */
public class ExtendedMatchers {
  /**
   * Match any argument of the type subscription
   */
  public static Subscription anySubscription() {
    return argThat(new InstanceOfSubscriptionMatcher());
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

  private static class InstanceOfSubscriptionMatcher implements ArgumentMatcher<Subscription> {
    public boolean matches(Subscription argument) {
      return argument != null && Subscription.class.isAssignableFrom(argument.getClass());
    }
  }

  private static class InstanceOfStateMatcher implements ArgumentMatcher<State> {
    public boolean matches(State argument) {
      return argument != null && State.class.isAssignableFrom(argument.getClass());
    }
  }
}
