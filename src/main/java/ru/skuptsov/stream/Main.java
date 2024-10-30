package ru.skuptsov.stream;

import com.beust.jcommander.internal.Lists;
import ru.skuptsov.stream.impl.PerElementTransformStageChainStream;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
    public static void main(String[] args) {

        List<Integer> list = IntStream.rangeClosed(1, 4)
                .boxed().collect(Collectors.toList());

//        System.out.println(list);
        System.out.println(PerElementTransformStageChainStream.stream(list, false)
                .reduce(1, (a, b) -> a * b));
    }

}
