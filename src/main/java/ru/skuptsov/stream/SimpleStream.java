package ru.skuptsov.stream;

import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface SimpleStream<T> {

    SimpleStream<T> filter(Predicate<? super T> predicate);

    <R> SimpleStream<R> map(Function<? super T, ? extends R> mapper);


    //定义并实现distinct
    SimpleStream<T> distinct();

    List<T> collectToList();

    Number sum();

    public Number average();

    T reduce(T identity, BinaryOperator<T> accumulator);

    <R> SimpleStream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper);
}
