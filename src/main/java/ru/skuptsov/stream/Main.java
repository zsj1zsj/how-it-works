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

        List<Person> nameList = Arrays.asList(new Person("Tanaka", 20),
                new Person("Suzuki", 22), new Person("Takahashi", 21));
        Stream<Object> stream = nameList.stream().flatMap(x -> Stream.of(x.name, x.age));
        System.out.println((stream.collect(Collectors.toList())));
    }

    public static class Person {
        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        private int age;
        private String name;

    }

}
