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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import rx.Subscription;
import rx.observers.TestSubscriber;

import static com.fred.rxredux.testhelpers.mockito.ExtendedMatchers.anyAction;
import static com.fred.rxredux.testhelpers.mockito.ExtendedMatchers.anyDispatch;
import static com.fred.rxredux.testhelpers.mockito.ExtendedMatchers.anyState;
import static com.fred.rxredux.testhelpers.mockito.ExtendedMatchers.anyStore;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StoreImplTest {
  @Mock
  Reducer<State, Action<Integer>> rootReducer;
  @Mock
  Middleware<Action<Integer>, State> middleware;

  private TestSubscriber<State> testSubscriber;
  private Subscription testSubscription;
  private Store<State, Action<Integer>> store;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    store =
        new StoreImpl<State, Action<Integer>>(rootReducer, mock(State.class),
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
    when(middleware.call(anyStore(), anyAction(), anyDispatch())).then(new Answer<Integer>() {
      public Integer answer(InvocationOnMock invocationOnMock) throws Throwable {
        // avoid calling the next middleware
        return null;
      }
    });
    store.dispatch(mock(Action.class));

    testSubscriber.assertNoValues();
  }

  @Test
  public void dispatch_shouldForwardActionsToReducerAfterMiddlewareRan() {
    when(middleware.call(anyStore(), anyAction(), anyDispatch())).then(new Answer<Integer>() {
      public Integer answer(InvocationOnMock invocationOnMock) throws Throwable {
        ((Dispatch) invocationOnMock.getArguments()[2]).call(new Action<Integer>(1));
        return null;
      }
    });

    store.dispatch(new Action<Integer>(2));

    verify(rootReducer).call(anyAction(), anyState());
  }

  @Test
  public void dispatch_shouldStillForwardActionsToRootReducerIfThereAreNoMiddlewares() {
    State state = mock(State.class);
    store =
        new StoreImpl<State, Action<Integer>>(rootReducer, state,
            new ImmediateToImmediateScheduler(),
            new ArrayList<Middleware<Action<Integer>, State>>());

    Action<Integer> action = mock(Action.class);
    store.dispatch(action);

    verify(rootReducer).call(action, state);
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
    final Action<Integer> action = mock(Action.class);
    Middleware<Action<Integer>, State> one = new Middleware<Action<Integer>, State>() {
      public State call(Store<State, Action<Integer>> stateActionStore,
          Action<Integer> integerAction,
          Dispatch<Action<Integer>, State> next) {
        action.setType(123);
        next.call(integerAction);
        return null;
      }
    };
    Middleware<Action<Integer>, State> two = new Middleware<Action<Integer>, State>() {
      public State call(Store<State, Action<Integer>> stateActionStore,
          Action<Integer> integerAction,
          Dispatch<Action<Integer>, State> next) {
        action.setType(456);
        next.call(integerAction);
        return null;
      }
    };
    rootReducer = new Reducer<State, Action<Integer>>() {
      public State call(Action<Integer> integerAction, State state) {
        action.setType(789);
        return state;
      }
    };

    store =
        new StoreImpl<State, Action<Integer>>(rootReducer, mock(State.class),
            new ImmediateToImmediateScheduler(), Arrays.asList(one, two));

    store.dispatch(action);

    InOrder inOrder = inOrder(action);

    inOrder.verify(action).setType(123);
    inOrder.verify(action).setType(456);
    inOrder.verify(action).setType(789);
  }
}
