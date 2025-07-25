package ru.skuptsov.stream;

import ru.skuptsov.stream.impl.PerElementTransformStageChainStream;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class Main {
    public static void main(String[] args) {

        List<Integer> list = IntStream.rangeClosed(1, 5)
                .boxed().collect(toList());

//        System.out.println(list);
//        System.out.println(PerElementTransformStageChainStream.stream(list, false)
//                .reduce(1, (a, b) -> a * b));

        System.out.println("sum:" + PerElementTransformStageChainStream.stream(list, false)
                .sum());

        System.out.println("average: " + PerElementTransformStageChainStream.stream(list, false)
                .average());

        System.out.println(PerElementTransformStageChainStream.stream(IntStream.range(1, 4)
                .mapToDouble(x -> x).boxed().collect(toList())).average());


        List<List<Integer>> nestedList = Arrays.asList(
                Arrays.asList(1, 2, 3),
                Arrays.asList(4, 5, 6),
                Arrays.asList(7, 8, 9)
        );

//        List<String> nameList = Arrays.asList("Tanaka", "Suzuki", "Takahashi");
//        Stream<Object> stream = nameList.stream().flatMap(x -> Stream.of(x, x.length()));
//        System.out.println((stream.collect(Collectors.toList())));

        // 示例 1: 展平 List<List<Integer>>
        List<Integer> resultList = PerElementTransformStageChainStream.stream(nestedList)
                // 这里应该将当前的 innerList (类型是 List<Integer>) 转换为 SimpleStream
                .flatMap(innerList -> PerElementTransformStageChainStream.stream((List<Integer>) innerList))
                .collectToList();



        System.out.println(resultList);

    }

}
