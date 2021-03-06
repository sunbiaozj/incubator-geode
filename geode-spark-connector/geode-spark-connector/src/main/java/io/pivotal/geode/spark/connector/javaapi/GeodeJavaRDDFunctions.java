/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.pivotal.geode.spark.connector.javaapi;

import io.pivotal.geode.spark.connector.GeodeConnectionConf;
import io.pivotal.geode.spark.connector.GeodeRDDFunctions;
import io.pivotal.geode.spark.connector.internal.rdd.GeodeJoinRDD;
import io.pivotal.geode.spark.connector.internal.rdd.GeodeOuterJoinRDD;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import scala.Option;
import scala.reflect.ClassTag;

import java.util.Properties;

import static io.pivotal.geode.spark.connector.javaapi.JavaAPIHelper.*;

/**
 * A Java API wrapper over {@link org.apache.spark.api.java.JavaRDD} to provide Geode Spark
 * Connector functionality.
 *
 * <p>To obtain an instance of this wrapper, use one of the factory methods in {@link
 * io.pivotal.geode.spark.connector.javaapi.GeodeJavaUtil} class.</p>
 */
public class GeodeJavaRDDFunctions<T> {

  public final GeodeRDDFunctions<T> rddf;

  public GeodeJavaRDDFunctions(JavaRDD<T> rdd) {
    this.rddf = new GeodeRDDFunctions<T>(rdd.rdd());
  }

  /**
   * Save the non-pair RDD to Geode key-value store.
   * @param regionPath the full path of region that the RDD is stored  
   * @param func the PairFunction that converts elements of JavaRDD to key/value pairs
   * @param connConf the GeodeConnectionConf object that provides connection to Geode cluster
   * @param opConf the parameters for this operation
   */  
  public <K, V> void saveToGeode(
    String regionPath, PairFunction<T, K, V> func, GeodeConnectionConf connConf, Properties opConf) {
    rddf.saveToGeode(regionPath, func, connConf, propertiesToScalaMap(opConf));
  }

  /**
   * Save the non-pair RDD to Geode key-value store.
   * @param regionPath the full path of region that the RDD is stored  
   * @param func the PairFunction that converts elements of JavaRDD to key/value pairs
   * @param connConf the GeodeConnectionConf object that provides connection to Geode cluster
   */
  public <K, V> void saveToGeode(
    String regionPath, PairFunction<T, K, V> func, GeodeConnectionConf connConf) {
    rddf.saveToGeode(regionPath, func, connConf, emptyStrStrMap());
  }

  /**
   * Save the non-pair RDD to Geode key-value store.
   * @param regionPath the full path of region that the RDD is stored
   * @param func the PairFunction that converts elements of JavaRDD to key/value pairs
   * @param opConf the parameters for this operation
   */
  public <K, V> void saveToGeode(
    String regionPath, PairFunction<T, K, V> func, Properties opConf) {
    rddf.saveToGeode(regionPath, func, rddf.defaultConnectionConf(), propertiesToScalaMap(opConf));
  }

  /**
   * Save the non-pair RDD to Geode key-value store with default GeodeConnector.
   * @param regionPath the full path of region that the RDD is stored  
   * @param func the PairFunction that converts elements of JavaRDD to key/value pairs
   */
  public <K, V> void saveToGeode(String regionPath, PairFunction<T, K, V> func) {
    rddf.saveToGeode(regionPath, func, rddf.defaultConnectionConf(), emptyStrStrMap());
  }

  /**
   * Return an RDD containing all pairs of elements with matching keys in this
   * RDD&lt;T> and the Geode `Region&lt;K, V>`. The join key from RDD
   * element is generated by `func(T) => K`, and the key from the Geode
   * region is just the key of the key/value pair.
   *
   * Each pair of elements of result RDD will be returned as a (t, v2) tuple,
   * where t is from this RDD and v is from the Geode region.
   *
   * @param regionPath the region path of the Geode region
   * @param func the function that generates region key from RDD element T
   * @param <K> the key type of the Geode region
   * @param <V> the value type of the Geode region
   * @return JavaPairRDD&lt;T, V>
   */
  public <K, V> JavaPairRDD<T, V> joinGeodeRegion(String regionPath, Function<T, K> func) {
    return joinGeodeRegion(regionPath, func, rddf.defaultConnectionConf());
  }

  /**
   * Return an RDD containing all pairs of elements with matching keys in this
   * RDD&lt;T> and the Geode `Region&lt;K, V>`. The join key from RDD
   * element is generated by `func(T) => K`, and the key from the Geode
   * region is just the key of the key/value pair.
   *
   * Each pair of elements of result RDD will be returned as a (t, v2) tuple,
   * where t is from this RDD and v is from the Geode region.
   *
   * @param regionPath the region path of the Geode region
   * @param func the function that generates region key from RDD element T
   * @param connConf the GeodeConnectionConf object that provides connection to Geode cluster
   * @param <K> the key type of the Geode region
   * @param <V> the value type of the Geode region
   * @return JavaPairRDD&lt;T, V>
   */
  public <K, V> JavaPairRDD<T, V> joinGeodeRegion(
    String regionPath, Function<T, K> func, GeodeConnectionConf connConf) {
    GeodeJoinRDD<T, K, V> rdd = rddf.joinGeodeRegion(regionPath, func, connConf);
    ClassTag<T> kt = fakeClassTag();
    ClassTag<V> vt = fakeClassTag();
    return new JavaPairRDD<>(rdd, kt, vt);
  }

  /**
   * Perform a left outer join of this RDD&lt;T> and the Geode `Region&lt;K, V>`.
   * The join key from RDD element is generated by `func(T) => K`, and the
   * key from region is just the key of the key/value pair.
   *
   * For each element (t) in this RDD, the resulting RDD will either contain
   * all pairs (t, Some(v)) for v in the Geode region, or the pair
   * (t, None) if no element in the Geode region have key `func(t)`.
   *
   * @param regionPath the region path of the Geode region
   * @param func the function that generates region key from RDD element T
   * @param <K> the key type of the Geode region
   * @param <V> the value type of the Geode region
   * @return JavaPairRDD&lt;T, Option&lt;V>>
   */
  public <K, V> JavaPairRDD<T, Option<V>> outerJoinGeodeRegion(String regionPath, Function<T, K> func) {
    return outerJoinGeodeRegion(regionPath, func, rddf.defaultConnectionConf());
  }

  /**
   * Perform a left outer join of this RDD&lt;T> and the Geode `Region&lt;K, V>`.
   * The join key from RDD element is generated by `func(T) => K`, and the
   * key from region is just the key of the key/value pair.
   *
   * For each element (t) in this RDD, the resulting RDD will either contain
   * all pairs (t, Some(v)) for v in the Geode region, or the pair
   * (t, None) if no element in the Geode region have key `func(t)`.
   *
   * @param regionPath the region path of the Geode region
   * @param func the function that generates region key from RDD element T
   * @param connConf the GeodeConnectionConf object that provides connection to Geode cluster
   * @param <K> the key type of the Geode region
   * @param <V> the value type of the Geode region
   * @return JavaPairRDD&lt;T, Option&lt;V>>
   */
  public <K, V> JavaPairRDD<T, Option<V>> outerJoinGeodeRegion(
    String regionPath, Function<T, K> func, GeodeConnectionConf connConf) {
    GeodeOuterJoinRDD<T, K, V> rdd = rddf.outerJoinGeodeRegion(regionPath, func, connConf);
    ClassTag<T> kt = fakeClassTag();
    ClassTag<Option<V>> vt = fakeClassTag();
    return new JavaPairRDD<>(rdd, kt, vt);
  }

}
