package org.apache.phoenix.trace.util;

//import org.apache.htrace.HTraceConfiguration;
//import org.apache.htrace.Sampler;

/**
 * A Sampler that always returns true.
 */
public final class AlwaysSampler implements Sampler<Object> {

  public static final AlwaysSampler
      INSTANCE = new AlwaysSampler();

  public AlwaysSampler() {
  }

  @Override
  public boolean next(Object info) {
    return true;
  }
}
