package ru.skuptsov.stream;

import ru.skuptsov.stream.impl.PerElementTransformStageChainStream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class Main {
    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>(100);
        for (int i = 0; i < 100; i++) {
            list.add(Math.abs(ThreadLocalRandom.current().nextInt()) % 100);
        }

        list = Collections.unmodifiableList(list);
        SimpleStream<Integer> stream = PerElementTransformStageChainStream.stream(list, false);
        // 执行顺序map->filter->map,都是在执行里面的function函数，把设定prevStage
        List<Integer> result = stream.map(t -> t * 2).filter(t -> t % 2 == 0).map(t -> t / 3).collectToList();
        System.out.println(result);
    }

}
