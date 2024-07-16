package com.github.eamonnmcmanus.connectionsguessing;

import static com.github.eamonnmcmanus.connectionsguessing.ConnectionsGuessing.Score.MATCH;
import static com.github.eamonnmcmanus.connectionsguessing.ConnectionsGuessing.Score.ONE_AWAY;
import static com.github.eamonnmcmanus.connectionsguessing.ConnectionsGuessing.Score.TWO_AWAY;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.lang.Integer.bitCount;

import com.google.auto.value.AutoBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

/**
 * Exploring whether there is a strategy for guessing the last 2 sets of connections in the NYT
 * Connections game. There are 8 items, which are divided into 2 sets of 4, called "connections".
 * The task is to find what those connections are. You have four guesses. In the real game, you are
 * supposed to look at the items themselves and figure out the connections yourself. But here we are
 * just seeing whether there is a guessing strategy that can always find the connections within the
 * allowed four guesses.
 *
 * <p>After each guess, the game has one of three outcomes: the guess was correct; the guess was off
 * by one (3 of the 4 connection match, but we don't know which 3); the guess was off by more than
 * one. (More than one is really just two, because if three of the items are not in the first
 * connection then they must all be in the second connection, so we're still off by one.)
 *
 * <p>We can represent a possible connection as an 8-bit number that has exactly 4 set bits,
 * representing the items that are present in the first connection. There is some redundancy here:
 * if the items are ABCDEFGH then the first connection being ABCD is equivalent to its being EFGH
 * because you don't have to guess the connections in a particular order. Similarly, <i>guessing</i>
 * ABCD is equivalent to guessing EFGH: if ABCD is correct then EFGH is too; and if ABCD is off by
 * one then EFGH is off by one too, since one item from each guess needs to be swapped with the
 * other guess. Therefore we can consider that item A (bit 0) is in a correct connection and we can
 * also include item A in every guess. So then a full connection is an 8-bit number that has bit 0
 * set and exactly 3 out of the other 7 bits set. There are 7C3 = 35 of those.
 *
 * <p>To search exhaustively, we can consider the cases where the solution is each of the 35
 * connections that contain A. Every division of the 8 items into two connections of 4 must contain
 * a connection that includes A. We can start with a guess of ABCD, which is as good as any other.
 * That gives us a mapping from outcomes to sets of connections, which we enumerate here for
 * clarity:
 *
 * <pre>
 * correct →  {ABCD};
 * one-away → {ABCE,ABCF,ABCG,ABCH,ABDE,ABDF,ABDG,ABDH,ACDE,ACDF,ACDG,ACDH,AEFG,AEFH,AEGH,AFGH};
 * two-away → {ABEF,ACEF,ADEF,ABEG,ACEG,ADEG,ABFG,ACFG,ADFG,ABEH,ACEH,ADEH,ABFH,ACFH,ADFH,ABGH,ACGH,ADGH}
 * </pre>
 *
 * <p>The one-away set here includes connections that have 3 items from ABCD and connections that
 * have only one (A). The latter means that there were 3 items from the other set, so we are still
 * off by one.
 *
 * <p>Now after this first step we have a set of connections that will have been found on this path,
 * {ABCD}, and two possible branches, for the one-away and two-away cases. Each branch has a set of
 * possible connections: 16 connections ({ABCE...AFGH}) for the one-away branch and 18
 * ({ABEF...ADGH}) for the two-away branch. For each branch, we can consider every one of the 35
 * possible guesses and determine the outcomes for each of the possible connections. We <i>don't</i>
 * just consider guesses that are in the currently-possible set, in case the best strategy might be
 * to gather information with guesses that can't succeed. And indeed that does turn out to be the
 * best strategy sometimes.
 *
 * <p>So for example on the one-away branch, when we consider guessing ABCE, we get the following
 * outcomes:
 *
 * <pre>
 * correct →  {ABCE}
 * one-away → {ABCF,ABCG,ABCH,ABDE,ACDE,AFGH}
 * two-away → {ABDF,ABDG,ABDH,ACDF,ACDG,ACDH,AEFG,AEFH,AEGH}
 * </pre>
 *
 * <p>And on the two-away branch, when we consider guessing ABCF, we get these outcomes:
 *
 * <pre>
 * correct →  {}
 * one-away → {ABEF,ACEF,ADEG,ABFG,ACFG,ADEH,ABFH,ACFH,ADGH}
 * two-away → {ADEF,ABEG,ACEG,ADFG,ABEH,ACEH,ADFH,ABGH,ACGH}
 * </pre>
 *
 * <p>By recursively considering every possible guess for both the one-away or two-away sets, we can
 * determine which guess gives us the most solutions within the allowed number of remaining guesses.
 *
 * @param showNonUniverseBetter show cases that produce a better outcome when guessing a connection
 *     that is not in the universe of possible connections than when guessing any connection that is
 * @param onlyGuessUniverse only consider guesses that are in the universe of possible connections
 */
public record ConnectionsGuessing(boolean showNonUniverseBetter, boolean onlyGuessUniverse) {
  public static void main(String[] args) {
    for (int lives = 4; lives <= 6; lives++) {
      var guessing = builder().showNonUniverseBetter(false).onlyGuessUniverse(false).build();
      int best = guessing.searchAbcd(lives);
      System.out.printf(
          "With %d lives, number of connections that can be guessed is %d\n", lives, best);
    }
  }

  @AutoBuilder
  interface Builder {
    Builder showNonUniverseBetter(boolean x);

    Builder onlyGuessUniverse(boolean x);

    ConnectionsGuessing build();
  }

  static Builder builder() {
    return new AutoBuilder_ConnectionsGuessing_Builder()
        .showNonUniverseBetter(false)
        .onlyGuessUniverse(false);
  }

  /**
   * Saves one level of recursion by assuming the first guess is ABCD. Any initial guess is as good
   * as any other so there's no reason to look at them all for the first guess.
   */
  int searchAbcd(int lives) {
    ConnectionSet allAConnections = ConnectionSet.ALL_A_CONNECTIONS;
    var scores = allAConnections.scoresFor(Connection.fromString("ABCD"));
    checkState(!scores.get(MATCH).isEmpty());
    return 1 + search(scores.get(ONE_AWAY), lives - 1) + search(scores.get(TWO_AWAY), lives - 1);
  }

  /**
   * Computes how many values from {@code universe} can be guessed given {@code lives} guesses. If
   * {@code lives} is 1 then (assuming {@code universe} is not empty), obviously 1 value from {@code
   * universe} can be guessed. If {@code lives > 1} then we can consider each possible guess and how
   * it divides {@code universe}. For example, suppose guessing ABFG splits {@code universe} into
   * MATCH={ABFG}, ONE_AWAY={ABCF,ABGH}, TWO_AWAY={ABCD,ABFH}. Then we know that with this guess,
   * the total number of values we can guess is 1 (for ABFG) plus the best result recursively
   * obtained for {ABCF,ABGH} plus the best result recursively obtained for {ABCD,ABFH}.
   *
   * @param universe a non-empty set of {@code Connection} that are the possible solutions at this
   *     stage of the recursion
   * @param lives the number of remaining guesses allowed
   * @return how many values from {@code universe} can be guessed in at most {@code lives} guesses
   */
  int search(ConnectionSet universe, int lives) {
    checkArgument(!universe.isEmpty());
    switch (lives) {
      case 0 -> throw new AssertionError();
      case 1 -> {
        return 1;
      }
    }
    checkArgument(lives > 0);
    int universeSize = universe.size();
    if (universeSize == 1) {
      return 1;
    }
    int best = 0;
    int bestInUniverse = 0;
    Connection bestNotInUniverseGuess = null;
    int bestNotInUniverse = 0;
    ConnectionSet guesses = onlyGuessUniverse ? universe : ConnectionSet.ALL_A_CONNECTIONS;
    for (Connection guess : guesses) {
      int thisBest = bestForGuess(universe, guess, lives);
      if (thisBest >= best) {
        best = thisBest;
        if (universe.contains(guess)) {
          bestInUniverse = thisBest;
        } else {
          bestNotInUniverse = thisBest;
          bestNotInUniverseGuess = guess;
        }
      }
    }
    if (showNonUniverseBetter && bestNotInUniverse > bestInUniverse) {
      System.out.printf(
          "Universe %s lives %d bestNotInUniverse %d for %s -> %s\n",
          universe,
          lives,
          bestNotInUniverse,
          bestNotInUniverseGuess,
          universe.scoresFor(bestNotInUniverseGuess));
      for (Connection guess : universe) {
        int thisBest = bestForGuess(universe, guess, lives);
        var scores = universe.scoresFor(guess);
        System.out.printf(
            "  %s -> %d; split is %s | %s | %s\n",
            guess, thisBest, guess, scores.get(ONE_AWAY), scores.get(TWO_AWAY));
      }
    }
    return best;
  }

  private int bestForGuess(ConnectionSet universe, Connection guess, int lives) {
    var scores = universe.scoresFor(guess);
    var oneAway = scores.get(ONE_AWAY);
    var twoAway = scores.get(TWO_AWAY);
    int universeSize = universe.size();
    if (oneAway.size() == universeSize || twoAway.size() == universeSize) {
      // No new information from the guess: we would recurse with the same or an empty universe.
      return 0;
    }
    int best = scores.get(MATCH).isEmpty() ? 0 : 1;
    for (var newUniverse : List.of(oneAway, twoAway)) {
      if (!newUniverse.isEmpty()) {
        best += search(newUniverse, lives - 1);
      }
    }
    return best;
  }

  enum Score {
    MATCH,
    ONE_AWAY,
    TWO_AWAY
  }

  /** A putative connection of 4 items. This is a set of 4 items out of the possible 8. */
  record Connection(int bits) implements Comparable<Connection> {
    Connection {
      checkArgument(bitCount(bits) == 4);
    }

    static final ImmutableList<Connection> ALL_CONNECTIONS =
        IntStream.range(0, 256)
            .filter(i -> bitCount(i) == 4)
            .boxed()
            .sorted(Comparator.comparingInt(Integer::reverse).reversed())
            // ABCD < ABCE < ACEF < BCDE, with A being bit 0
            .map(Connection::new)
            .collect(toImmutableList());

    static final ImmutableList<Connection> ALL_A_CONNECTIONS =
        ALL_CONNECTIONS.stream()
            .filter(connection -> connection.contains(0))
            .collect(toImmutableList());

    private static final ImmutableMap<Connection, Integer> CONNECTION_TO_INDEX =
        IntStream.range(0, ALL_CONNECTIONS.size())
            .mapToObj(Integer::valueOf)
            .collect(toImmutableMap(ALL_CONNECTIONS::get, i -> i));

    int index() {
      return CONNECTION_TO_INDEX.get(this);
    }

    boolean contains(int item) {
      checkArgument(0 <= item && item < 8, "Item out of range: %s", item);
      return (bits & (1 << item)) != 0;
    }

    Score scoreFor(Connection guess) {
      return switch (bitCount(bits & guess.bits)) {
        case 0, 4 -> MATCH;
        case 1, 3 -> ONE_AWAY;
        case 2 -> TWO_AWAY;
        default -> throw new AssertionError(bits & guess.bits);
      };
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < 8; i++) {
        if ((bits & (1 << i)) != 0) {
          sb.append((char) (i + 'A'));
        }
      }
      return sb.toString();
    }

    static Connection fromString(String s) {
      checkArgument(s.length() == 4, "Wrong length %s", s);
      int bits =
          s.chars()
              .filter(c -> 'A' <= c && c <= 'H')
              .map(c -> 1 << (c - 'A'))
              .reduce(0, (a, b) -> a | b);
      return ALL_CONNECTIONS.get(new Connection(bits).index());
    }

    @Override
    public int compareTo(Connection o) {
      return Integer.compare(index(), o.index());
    }
  }

  static class ConnectionSet extends AbstractSet<Connection> {
    static final ConnectionSet ALL_A_CONNECTIONS =
        new ConnectionSet(Connection.ALL_A_CONNECTIONS).freeze();

    private final BitSet connectionBits = new BitSet(Connection.ALL_CONNECTIONS.size());
    private boolean frozen = false;

    ConnectionSet() {}

    ConnectionSet(Collection<Connection> values) {
      addAll(values);
    }

    static ConnectionSet of(Connection... values) {
      return new ConnectionSet(Arrays.asList(values));
    }

    @CanIgnoreReturnValue
    ConnectionSet freeze() {
      frozen = true;
      return this;
    }

    @Override
    public Iterator<Connection> iterator() {
      return new Iterator<>() {
        private int currentIndex = -1;

        @Override
        public boolean hasNext() {
          return connectionBits.nextSetBit(currentIndex + 1) >= 0;
        }

        @Override
        public Connection next() {
          int nextIndex = connectionBits.nextSetBit(currentIndex + 1);
          if (nextIndex < 0) {
            throw new NoSuchElementException();
          }
          currentIndex = nextIndex;
          return Connection.ALL_CONNECTIONS.get(currentIndex);
        }

        @Override
        public void remove() {
          checkState(!frozen);
          checkState(currentIndex >= 0 && connectionBits.get(currentIndex));
          connectionBits.clear(currentIndex);
        }
      };
    }

    @Override
    public int size() {
      return connectionBits.cardinality();
    }

    @Override
    public boolean add(Connection connection) {
      checkState(!frozen);
      if (connectionBits.get(connection.index())) {
        return false;
      }
      connectionBits.set(connection.index());
      return true;
    }

    @Override
    public boolean contains(Object o) {
      return o instanceof Connection connection && connectionBits.get(connection.index());
    }

    @Override
    public boolean remove(Object o) {
      checkState(!frozen);
      if (o instanceof Connection connection && connectionBits.get(connection.index())) {
        connectionBits.clear(connection.index());
        return true;
      }
      return false;
    }

    ImmutableMap<Score, ConnectionSet> scoresFor(Connection guess) {
      Map<Score, ConnectionSet> map = new EnumMap<>(Score.class);
      for (Score score : Score.values()) {
        map.put(score, new ConnectionSet());
      }
      for (Connection connection : this) {
        map.get(connection.scoreFor(guess)).add(connection);
      }
      map.values().forEach(ConnectionSet::freeze);
      return Maps.immutableEnumMap(map);
    }
  }
}
