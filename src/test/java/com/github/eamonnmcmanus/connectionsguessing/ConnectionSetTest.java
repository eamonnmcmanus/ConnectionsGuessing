/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.github.eamonnmcmanus.connectionsguessing;

import com.github.eamonnmcmanus.connectionsguessing.ConnectionsGuessing.Connection;
import com.github.eamonnmcmanus.connectionsguessing.ConnectionsGuessing.ConnectionSet;
import com.google.common.collect.testing.SampleElements;
import com.google.common.collect.testing.SetTestSuiteBuilder;
import com.google.common.collect.testing.TestSetGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import junit.framework.Test;
import junit.framework.TestCase;

/**
 * @author emcmanus
 */
public class ConnectionSetTest extends TestCase {
  public static Test suite() {
    return SetTestSuiteBuilder.using(new TestItemsSetGenerator())
        .named("ConnectionSetTest")
        .withFeatures(
            CollectionFeature.ALLOWS_NULL_QUERIES,
            CollectionFeature.KNOWN_ORDER,
            CollectionFeature.RESTRICTS_ELEMENTS,
            CollectionFeature.SUPPORTS_ADD,
            CollectionFeature.SUPPORTS_ITERATOR_REMOVE,
            CollectionFeature.SUPPORTS_REMOVE,
            CollectionSize.ANY)
        .createTestSuite();
  }

  private static class TestItemsSetGenerator implements TestSetGenerator<Connection> {
    @Override
    public Set<Connection> create(Object... elements) {
      ConnectionSet set = new ConnectionSet();
      for (var element : elements) {
        set.add((Connection) element);
      }
      return set;
    }

    @Override
    public SampleElements<Connection> samples() {
      List<Connection> connectionList = new ArrayList<>();
      for (int i = 64; i < 256 && connectionList.size() < 5; i++) {
        if (Integer.bitCount(i) == 4) {
          connectionList.add(new Connection(i));
        }
      }
      return new SampleElements<>(
          connectionList.get(0),
          connectionList.get(1),
          connectionList.get(2),
          connectionList.get(3),
          connectionList.get(4));
    }

    @Override
    public Connection[] createArray(int length) {
      return new Connection[length];
    }

    @Override
    public Iterable<Connection> order(List<Connection> list) {
      Collections.sort(list);
      return list;
    }
  }
}
