package com.fred.rxredux.testhelpers.mockito;

import com.fred.rxredux.Action;
import com.fred.rxredux.State;
import org.mockito.ArgumentMatcher;
import rx.Subscription;

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

  /**
   * Match any argument of the type {@link Action}
   */
  public static <T> Action<T> anyAction(T clazz) {
    return argThat(new InstanceOfActionMatcher<T>());
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

  private static class InstanceOfActionMatcher<T> implements ArgumentMatcher<Action<T>> {
    public boolean matches(Action argument) {
      return argument != null && Action.class.isAssignableFrom(argument.getClass());
    }
  }
}
