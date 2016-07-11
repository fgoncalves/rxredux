package com.fred.rxredux;

import com.fred.rxredux.testhelpers.ImmediateToImmediateScheduler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import rx.Observable;
import rx.Subscription;
import rx.observers.TestSubscriber;

import static com.fred.rxredux.testhelpers.mockito.ExtendedMatchers.anyAction;
import static com.fred.rxredux.testhelpers.mockito.ExtendedMatchers.anyState;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StoreImplTest {
  @Mock
  Reducer<State> rootReducer;
  @Mock
  Middleware middleware;

  private TestSubscriber<State> testSubscriber;
  private Subscription testSubscription;
  private Store<State> store;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    store =
        new StoreImpl<State>(rootReducer, mock(State.class), new ImmediateToImmediateScheduler(),
            middleware);
    testSubscriber = new TestSubscriber<State>();
    testSubscription = store.subscribe(testSubscriber);
  }

  @After
  public void tearDown() throws Exception {
    testSubscription.unsubscribe();
  }

  @Test
  public void dispatch_shouldNotForwardEventsIfMiddlewareCompletesTheStream() {
    when(middleware.apply(anyState(), anyAction(Object.class)))
        .thenReturn(Observable.<State>empty());

    store.dispatch(mock(Action.class));

    testSubscriber.assertNoValues();
  }

  @Test
  public void dispatch_shouldForwardActionsToReducerAfterMiddlewareRan() {
    when(middleware.apply(anyState(), anyAction(Object.class)))
        .thenReturn(Observable.just(store.state()));

    Action action = mock(Action.class);
    store.dispatch(action);

    verify(rootReducer).reduce(action, store.state());
  }

  @Test
  public void dispatch_shouldStillForwardActionsToRootReducerIfThereAreNoMiddlewares() {
    store =
        new StoreImpl<State>(rootReducer, mock(State.class), new ImmediateToImmediateScheduler());

    Action action = mock(Action.class);
    store.dispatch(action);

    verify(rootReducer).reduce(action, store.state());
  }

  @Test
  public void create_shouldCreateAStoreWithTheGivenInitialState() {
    State initialState = mock(State.class);
    Store<State> store = StoreImpl.create(rootReducer, initialState);

    assertThat(store.state()).isEqualTo(initialState);
  }
}
