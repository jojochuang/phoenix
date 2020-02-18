package org.apache.phoenix.trace.util;

//import org.apache.htrace.impl.AlwaysSampler;
//import org.apache.htrace.impl.NeverSampler;

public interface Sampler<T> {

  public static final Sampler<?> ALWAYS = AlwaysSampler.INSTANCE;
  public static final Sampler<?> NEVER = NeverSampler.INSTANCE;

  public boolean next(T info);

}
