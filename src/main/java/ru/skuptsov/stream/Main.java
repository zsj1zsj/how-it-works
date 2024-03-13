package ru.skuptsov.stream;

import ru.skuptsov.stream.impl.PerElementTransformStageChainStream;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;

public class Main {
    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>(100);
        list.add(1);
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(3);

        list = Collections.unmodifiableList(list);
        SimpleStream<Integer> stream = PerElementTransformStageChainStream.stream(list, false);
        // 执行顺序map->filter->map,都是在执行里面的function函数，把设定prevStage
        List<Integer> result = stream.distinct().map(t->t*2).collectToList();
        System.out.println(result);

    }

}
