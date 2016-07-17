package com.fred.rxredux;

import com.fred.rxredux.testhelpers.ImmediateToImmediateScheduler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.observers.TestSubscriber;

import static com.fred.rxredux.testhelpers.mockito.ExtendedMatchers.anyAction;
import static com.fred.rxredux.testhelpers.mockito.ExtendedMatchers.anyState;
import static com.fred.rxredux.testhelpers.mockito.ExtendedMatchers.anyStore;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StoreImplTest {
  @Mock
  Reducer<State, Action<Integer>> rootReducer;
  @Mock
  Middleware<State, Action<Integer>> middleware;

  private TestSubscriber<State> testSubscriber;
  private Subscription testSubscription;
  private Store<State, Action<Integer>> store;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    store =
        new StoreImpl<State, Action<Integer>>(rootReducer, mock(State.class),
            new ImmediateToImmediateScheduler(),
            new ImmediateToImmediateScheduler(), Collections.singletonList(middleware));
    testSubscriber = new TestSubscriber<State>();
    testSubscription = store.subscribe(testSubscriber);
  }

  @After
  public void tearDown() throws Exception {
    testSubscription.unsubscribe();
  }

  @Test
  public void dispatch_shouldNotForwardEventsIfMiddlewareCompletesTheStream() {
    when(middleware.apply(anyStore(), anyState(), anyAction()))
        .thenReturn(Observable.<State>empty());

    store.dispatch(mock(Action.class));

    testSubscriber.assertNoValues();
  }

  @Test
  public void dispatch_shouldForwardActionsToReducerAfterMiddlewareRan() {
    when(middleware.apply(anyStore(), anyState(), anyAction()))
        .thenReturn(Observable.just(store.state()));

    Action<Integer> action = mock(Action.class);
    store.dispatch(action);

    verify(rootReducer).reduce(action, store.state());
  }

  @Test
  public void dispatch_shouldStillForwardActionsToRootReducerIfThereAreNoMiddlewares() {
    store =
        new StoreImpl<State, Action<Integer>>(rootReducer, mock(State.class),
            new ImmediateToImmediateScheduler(),
            new ImmediateToImmediateScheduler(),
            new ArrayList<Middleware<State, Action<Integer>>>());

    Action<Integer> action = mock(Action.class);
    store.dispatch(action);

    verify(rootReducer).reduce(action, store.state());
  }

  @Test
  public void create_shouldCreateAStoreWithTheGivenInitialState() {
    State initialState = new State();
    Store<State, Action<Integer>> store =
        StoreImpl.create(rootReducer, initialState,
            new ImmediateToImmediateScheduler());

    assertThat(store.state()).isEqualTo(initialState);

    store =
        StoreImpl.create(rootReducer, initialState,
            new ImmediateToImmediateScheduler(),
            Collections.singletonList(middleware));

    assertThat(store.state()).isEqualTo(initialState);
  }

  @Test
  public void dispatch_shouldInvokeMiddlewaresInOrderOfAdditionAndThenTheRootReducer() {
    Action<Integer> action = mock(Action.class);
    Middleware<State, Action<Integer>> one = new Middleware<State, Action<Integer>>() {
      public Observable<State> apply(Store<State, Action<Integer>> store, final State currentState,
          final Action<Integer> action) {
        return Observable.create(new Observable.OnSubscribe<State>() {
          public void call(Subscriber<? super State> subscriber) {
            action.setType(123);
            subscriber.onNext(currentState);
            subscriber.onCompleted();
          }
        });
      }
    };
    Middleware<State, Action<Integer>> two = new Middleware<State, Action<Integer>>() {
      public Observable<State> apply(Store<State, Action<Integer>> store, final State currentState,
          final Action<Integer> action) {
        return Observable.create(new Observable.OnSubscribe<State>() {
          public void call(Subscriber<? super State> subscriber) {
            action.setType(456);
            subscriber.onNext(currentState);
            subscriber.onCompleted();
          }
        });
      }
    };
    rootReducer = new Reducer<State, Action<Integer>>() {
      public Observable<State> reduce(final Action<Integer> action, final State currentState) {
        return Observable.create(new Observable.OnSubscribe<State>() {
          public void call(Subscriber<? super State> subscriber) {
            action.setType(789);
            subscriber.onNext(currentState);
            subscriber.onCompleted();
          }
        });
      }
    };

    store =
        new StoreImpl<State, Action<Integer>>(rootReducer, mock(State.class),
            new ImmediateToImmediateScheduler(),
            new ImmediateToImmediateScheduler(), Arrays.asList(one, two));

    store.dispatch(action);

    InOrder inOrder = inOrder(action);

    inOrder.verify(action).setType(123);
    inOrder.verify(action).setType(456);
    inOrder.verify(action).setType(789);
  }
}
