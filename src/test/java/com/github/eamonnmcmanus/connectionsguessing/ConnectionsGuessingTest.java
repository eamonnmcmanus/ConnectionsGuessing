package com.github.eamonnmcmanus.connectionsguessing;

import static com.github.eamonnmcmanus.connectionsguessing.ConnectionsGuessing.ConnectionSet.ALL_A_CONNECTIONS;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import com.github.eamonnmcmanus.connectionsguessing.ConnectionsGuessing.Connection;
import com.github.eamonnmcmanus.connectionsguessing.ConnectionsGuessing.ConnectionSet;
import com.github.eamonnmcmanus.connectionsguessing.ConnectionsGuessing.Score;
import org.junit.Test;

/**
 * @author emcmanus
 */
public class ConnectionsGuessingTest {
  @Test
  public void connectionString() {
    assertThat(ABCD.toString()).isEqualTo("ABCD");
    assertThat(Connection.fromString("ABCD")).isSameInstanceAs(ABCD);

    assertThat(EFGH.toString()).isEqualTo("EFGH");
    assertThat(Connection.fromString("EFGH")).isSameInstanceAs(EFGH);

    assertThat(ABCD).isNotEqualTo(EFGH);
  }

  @Test
  public void connectionContains() {
    for (int i = 0; i < 4; i++) {
      assertWithMessage("ABCD should contain %s", (char) ('A' + i)).that(ABCD.contains(i)).isTrue();
    }
    for (int i = 5; i < 8; i++) {
      assertWithMessage("ABCD should not contain %s", (char) ('A' + i))
          .that(ABCD.contains(i))
          .isFalse();
    }
  }

  @Test
  public void connectionScore() {
    assertThat(ABCD.scoreFor(ABCD)).isEqualTo(Score.MATCH);
    assertThat(ABCD.scoreFor(EFGH)).isEqualTo(Score.MATCH);
    assertThat(EFGH.scoreFor(ABCD)).isEqualTo(Score.MATCH);

    assertThat(ABCD.scoreFor(ABCE)).isEqualTo(Score.ONE_AWAY);
    assertThat(ABCE.scoreFor(ABCD)).isEqualTo(Score.ONE_AWAY);
    assertThat(ABCD.scoreFor(AFGH)).isEqualTo(Score.ONE_AWAY);

    assertThat(ABCD.scoreFor(ABGH)).isEqualTo(Score.TWO_AWAY);
    assertThat(ABGH.scoreFor(ABCD)).isEqualTo(Score.TWO_AWAY);
  }

  @Test
  public void connectionSetScoreFor() {
    assertThat(ALL_A_CONNECTIONS).hasSize(35);
    var scores = ALL_A_CONNECTIONS.scoresFor(ABCD);
    assertThat(scores)
        .containsExactly(Score.MATCH, ConnectionSet.of(ABCD),
            Score.ONE_AWAY,
                ConnectionSet.of(
                    ABCE, ABCF, ABCG, ABCH, ABDE, ABDF, ABDG, ABDH, ACDE, ACDF, ACDG, ACDH, AEFG,
                    AEFH, AEGH, AFGH),
            Score.TWO_AWAY,
                ConnectionSet.of(
                    ABEF, ACEF, ADEF, ABEG, ACEG, ADEG, ABFG, ACFG, ADFG, ABEH, ACEH, ADEH, ABFH,
                    ACFH, ADFH, ABGH, ACGH, ADGH));
    ConnectionSet oneOffs = scores.get(Score.ONE_AWAY);
    assertThat(oneOffs.scoresFor(ABCE))
        .containsExactly(Score.MATCH, ConnectionSet.of(ABCE),
            Score.ONE_AWAY, ConnectionSet.of(ABCF, ABCG, ABCH, ABDE, ACDE, AFGH),
            Score.TWO_AWAY, ConnectionSet.of(ABDF, ABDG, ABDH, ACDF, ACDG, ACDH, AEFG, AEFH, AEGH));
    ConnectionSet twoOffs = scores.get(Score.TWO_AWAY);
    assertThat(twoOffs.scoresFor(ABCF))
        .containsExactly(Score.MATCH, ConnectionSet.of(),
            Score.ONE_AWAY, ConnectionSet.of(ABEF, ACEF, ADEG, ABFG, ACFG, ADEH, ABFH, ACFH, ADGH),
            Score.TWO_AWAY, ConnectionSet.of(ADEF, ABEG, ACEG, ADFG, ABEH, ACEH, ADFH, ABGH, ACGH));
  }

  private static final Connection ABCD = Connection.fromString("ABCD");
  private static final Connection ABCE = Connection.fromString("ABCE");
  private static final Connection ABDE = Connection.fromString("ABDE");
  private static final Connection ACDE = Connection.fromString("ACDE");
  private static final Connection BCDE = Connection.fromString("BCDE");
  private static final Connection ABCF = Connection.fromString("ABCF");
  private static final Connection ABDF = Connection.fromString("ABDF");
  private static final Connection ACDF = Connection.fromString("ACDF");
  private static final Connection BCDF = Connection.fromString("BCDF");
  private static final Connection ABEF = Connection.fromString("ABEF");
  private static final Connection ACEF = Connection.fromString("ACEF");
  private static final Connection BCEF = Connection.fromString("BCEF");
  private static final Connection ADEF = Connection.fromString("ADEF");
  private static final Connection BDEF = Connection.fromString("BDEF");
  private static final Connection CDEF = Connection.fromString("CDEF");
  private static final Connection ABCG = Connection.fromString("ABCG");
  private static final Connection ABDG = Connection.fromString("ABDG");
  private static final Connection ACDG = Connection.fromString("ACDG");
  private static final Connection BCDG = Connection.fromString("BCDG");
  private static final Connection ABEG = Connection.fromString("ABEG");
  private static final Connection ACEG = Connection.fromString("ACEG");
  private static final Connection BCEG = Connection.fromString("BCEG");
  private static final Connection ADEG = Connection.fromString("ADEG");
  private static final Connection BDEG = Connection.fromString("BDEG");
  private static final Connection CDEG = Connection.fromString("CDEG");
  private static final Connection ABFG = Connection.fromString("ABFG");
  private static final Connection ACFG = Connection.fromString("ACFG");
  private static final Connection BCFG = Connection.fromString("BCFG");
  private static final Connection ADFG = Connection.fromString("ADFG");
  private static final Connection BDFG = Connection.fromString("BDFG");
  private static final Connection CDFG = Connection.fromString("CDFG");
  private static final Connection AEFG = Connection.fromString("AEFG");
  private static final Connection BEFG = Connection.fromString("BEFG");
  private static final Connection CEFG = Connection.fromString("CEFG");
  private static final Connection DEFG = Connection.fromString("DEFG");
  private static final Connection ABCH = Connection.fromString("ABCH");
  private static final Connection ABDH = Connection.fromString("ABDH");
  private static final Connection ACDH = Connection.fromString("ACDH");
  private static final Connection BCDH = Connection.fromString("BCDH");
  private static final Connection ABEH = Connection.fromString("ABEH");
  private static final Connection ACEH = Connection.fromString("ACEH");
  private static final Connection BCEH = Connection.fromString("BCEH");
  private static final Connection ADEH = Connection.fromString("ADEH");
  private static final Connection BDEH = Connection.fromString("BDEH");
  private static final Connection CDEH = Connection.fromString("CDEH");
  private static final Connection ABFH = Connection.fromString("ABFH");
  private static final Connection ACFH = Connection.fromString("ACFH");
  private static final Connection BCFH = Connection.fromString("BCFH");
  private static final Connection ADFH = Connection.fromString("ADFH");
  private static final Connection BDFH = Connection.fromString("BDFH");
  private static final Connection CDFH = Connection.fromString("CDFH");
  private static final Connection AEFH = Connection.fromString("AEFH");
  private static final Connection BEFH = Connection.fromString("BEFH");
  private static final Connection CEFH = Connection.fromString("CEFH");
  private static final Connection DEFH = Connection.fromString("DEFH");
  private static final Connection ABGH = Connection.fromString("ABGH");
  private static final Connection ACGH = Connection.fromString("ACGH");
  private static final Connection BCGH = Connection.fromString("BCGH");
  private static final Connection ADGH = Connection.fromString("ADGH");
  private static final Connection BDGH = Connection.fromString("BDGH");
  private static final Connection CDGH = Connection.fromString("CDGH");
  private static final Connection AEGH = Connection.fromString("AEGH");
  private static final Connection BEGH = Connection.fromString("BEGH");
  private static final Connection CEGH = Connection.fromString("CEGH");
  private static final Connection DEGH = Connection.fromString("DEGH");
  private static final Connection AFGH = Connection.fromString("AFGH");
  private static final Connection BFGH = Connection.fromString("BFGH");
  private static final Connection CFGH = Connection.fromString("CFGH");
  private static final Connection DFGH = Connection.fromString("DFGH");
  private static final Connection EFGH = Connection.fromString("EFGH");
}
