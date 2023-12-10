package suic.util;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.*;
import java.util.stream.Collector;

public class CustomCollectors {

    private static final Set<Collector.Characteristics> CH_ID
            = Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.IDENTITY_FINISH));
    private static final Set<Collector.Characteristics> CH_NOID = Collections.emptySet();

    public static <T, R1, R2, R3, R>
    Collector<T, ?, R> teeing3(Collector<? super T, ?, R1> downstream1,
                               Collector<? super T, ?, R2> downstream2,
                               Collector<? super T, ?, R3> downstream3,
                               TriFunction<? super R1, ? super R2, ? super R3, R> merger) {
        return teeing3Impl(downstream1, downstream2, downstream3, merger);
    }

    private static <T, A1, A2, A3, R1, R2, R3, R>
    Collector<T, ?, R> teeing3Impl(Collector<? super T, A1, R1> downstream1,
                                Collector<? super T, A2, R2> downstream2,
                                Collector<? super T, A3, R3> downstream3,
                                TriFunction<? super R1, ? super R2, ? super R3, R> merger) {
        Objects.requireNonNull(downstream1, "downstream1");
        Objects.requireNonNull(downstream2, "downstream2");
        Objects.requireNonNull(downstream3, "downstream3");
        Objects.requireNonNull(merger, "merger");

        Supplier<A1> c1Supplier = Objects.requireNonNull(downstream1.supplier(), "downstream1 supplier");
        Supplier<A2> c2Supplier = Objects.requireNonNull(downstream2.supplier(), "downstream2 supplier");
        Supplier<A3> c3Supplier = Objects.requireNonNull(downstream3.supplier(), "downstream3 supplier");
        BiConsumer<A1, ? super T> c1Accumulator =
                Objects.requireNonNull(downstream1.accumulator(), "downstream1 accumulator");
        BiConsumer<A2, ? super T> c2Accumulator =
                Objects.requireNonNull(downstream2.accumulator(), "downstream2 accumulator");
        BiConsumer<A3, ? super T> c3Accumulator =
                Objects.requireNonNull(downstream3.accumulator(), "downstream3 accumulator");
        BinaryOperator<A1> c1Combiner = Objects.requireNonNull(downstream1.combiner(), "downstream1 combiner");
        BinaryOperator<A2> c2Combiner = Objects.requireNonNull(downstream2.combiner(), "downstream2 combiner");
        BinaryOperator<A3> c3Combiner = Objects.requireNonNull(downstream3.combiner(), "downstream3 combiner");
        Function<A1, R1> c1Finisher = Objects.requireNonNull(downstream1.finisher(), "downstream1 finisher");
        Function<A2, R2> c2Finisher = Objects.requireNonNull(downstream2.finisher(), "downstream2 finisher");
        Function<A3, R3> c3Finisher = Objects.requireNonNull(downstream3.finisher(), "downstream3 finisher");

        Set<Collector.Characteristics> characteristics;
        Set<Collector.Characteristics> c1Characteristics = downstream1.characteristics();
        Set<Collector.Characteristics> c2Characteristics = downstream2.characteristics();
        Set<Collector.Characteristics> c3Characteristics = downstream3.characteristics();
        if (CH_ID.containsAll(c1Characteristics) || CH_ID.containsAll(c2Characteristics) || CH_ID.containsAll(c3Characteristics)) {
            characteristics = CH_NOID;
        } else {
            EnumSet<Collector.Characteristics> c = EnumSet.noneOf(Collector.Characteristics.class);
            c.addAll(c1Characteristics);
            c.retainAll(c2Characteristics);
            c.retainAll(c3Characteristics);
            c.remove(Collector.Characteristics.IDENTITY_FINISH);
            characteristics = Collections.unmodifiableSet(c);
        }

        class TriBox {
            A1 first = c1Supplier.get();
            A2 second = c2Supplier.get();

            A3 third = c3Supplier.get();

            void add(T t) {
                c1Accumulator.accept(first, t);
                c2Accumulator.accept(second, t);
                c3Accumulator.accept(third, t);
            }

            TriBox combine(TriBox other) {
                first = c1Combiner.apply(first, other.first);
                second = c2Combiner.apply(second, other.second);
                third = c3Combiner.apply(third, other.third);
                return this;
            }

            R get() {
                R1 r1 = c1Finisher.apply(first);
                R2 r2 = c2Finisher.apply(second);
                R3 r3 = c3Finisher.apply(third);
                return merger.apply(r1, r2, r3);
            }
        }

        return new CollectorImpl<>(TriBox::new, TriBox::add, TriBox::combine, TriBox::get, characteristics);
    }

    @SuppressWarnings("unchecked")
    private static <I, R> Function<I, R> castingIdentity() {
        return i -> (R) i;
    }

    /**
     * Simple implementation class for {@code Collector}.
     *
     * @param <T> the type of elements to be collected
     * @param <R> the type of the result
     */
    private record CollectorImpl<T, A, R>(Supplier<A> supplier,
                                          BiConsumer<A, T> accumulator,
                                          BinaryOperator<A> combiner,
                                          Function<A, R> finisher,
                                          Set<Characteristics> characteristics
    ) implements Collector<T, A, R> {

        CollectorImpl(Supplier<A> supplier,
                      BiConsumer<A, T> accumulator,
                      BinaryOperator<A> combiner,
                      Set<Characteristics> characteristics) {
            this(supplier, accumulator, combiner, castingIdentity(), characteristics);
        }
    }

}
