package benches;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
@OperationsPerInvocation(CollectionVsStream.N)
@Fork(jvmArgsAppend = "--illegal-access=warn", value = 1)
@Warmup(iterations = 3)
@Measurement(iterations = 8)
public class CollectionVsStream {

  public static final int N = 1000;

  private final static Random random = new Random();
  private final static List<HierarchyElement> hierarchyElements = buildHierarchyElements();

  public static void main(String[] args) throws RunnerException {

    Options opt = new OptionsBuilder()
        .include(CollectionVsStream.class.getSimpleName())
        .forks(1)
        .build();

    new Runner(opt).run();
  }

  private static List<HierarchyElement> buildHierarchyElements() {
    List<HierarchyElement> ret = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      ret.add(randomHierarchyElement());
    }
    return ret;
  }

  private static class HierarchyElement {
    private long id;
    private boolean ancestor;
    HierarchyElement(long id, boolean ancestor) {
      this.id = id;
      this.ancestor = ancestor;
    }

    public boolean isAncestor() {
      return ancestor;
    }

    public long getId() {
      return id;
    }
  }

  private static HierarchyElement randomHierarchyElement() {
    return new HierarchyElement(random.nextLong(), random.nextBoolean());
  }

  @Benchmark
  public void runCollectionBench() {
    // Filter out non ancestor hierarchy elements and collect ancestors ids
    List<Long> ancestorIds = new ArrayList<Long>();
    List<HierarchyElement> ancestorHierarchyElements = new ArrayList<HierarchyElement>();
    for (HierarchyElement hierarchyElement : hierarchyElements) {
      if (hierarchyElement.isAncestor()) {
        ancestorHierarchyElements.add(hierarchyElement);
        ancestorIds.add(hierarchyElement.getId());
      }
    }
    if (fakeCheckOnCollectionToAvoidStrangeOptimizationByCompiler(ancestorHierarchyElements, ancestorIds)) {
    }
  }

  @Benchmark
  public void runStreamBench() {
    List<HierarchyElement> ancestorHierarchyElements = hierarchyElements.stream().filter(h -> h.isAncestor()).collect(Collectors.toList());
    List<Long> ancestorIds = ancestorHierarchyElements.stream().map(h -> h.getId()).collect(Collectors.toList());
    if (fakeCheckOnCollectionToAvoidStrangeOptimizationByCompiler(ancestorHierarchyElements, ancestorIds)) {
    }
  }

  private boolean fakeCheckOnCollectionToAvoidStrangeOptimizationByCompiler(List<HierarchyElement> elts, List<Long> ids) {
    return elts.size() * 12 == ids.size() % 100;
  }

}
