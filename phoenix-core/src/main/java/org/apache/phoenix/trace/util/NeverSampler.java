package org.apache.phoenix.trace.util;

/**
 * A Sampler that never returns true.
 */
public final class NeverSampler implements Sampler<Object> {

  public static final NeverSampler INSTANCE = new NeverSampler();

  public NeverSampler() {
  }

  @Override
  public boolean next(Object info) {
    return false;
  }

}
