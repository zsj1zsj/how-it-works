package ru.skuptsov.stream.impl;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import ru.skuptsov.stream.SimpleStream;

public class PerElementTransformStageChainStream {

    static class StreamStage<IN, OUT> implements SimpleStream<OUT> {
        final List<?> list;
        final StreamStage prevStage;
        final Function<Consumer<OUT>, Consumer<IN>> consumerPipelineTransformer;
        final boolean parallel;

        // 这个构造函数实在startStage里调的，因为这个stage没有upstream
        StreamStage(List<?> list, Function<Consumer<OUT>, Consumer<IN>> consumerPipelineTransformer, boolean parallel) {
            this.list = list;
            this.parallel = parallel;
            this.prevStage = null;
            this.consumerPipelineTransformer = consumerPipelineTransformer;
        }

        // 其他的stage就调这个构造函数，需要upstream，也就是上一层的操作器
        StreamStage(List<?> list, StreamStage<?, ?> upStream, Function<Consumer<OUT>, Consumer<IN>> consumerPipelineTransformer, boolean parallel) {
            this.list = list;
            this.prevStage = upStream;
            this.consumerPipelineTransformer = consumerPipelineTransformer;
            this.parallel = parallel;
        }

        abstract static class TransformChain<T, OUT> implements Consumer<T> {
            protected final Consumer<? super OUT> downstream;

            TransformChain(Consumer<? super OUT> downstream) {
                this.downstream = downstream;
            }
        }

        @Override
        public SimpleStream<OUT> filter(Predicate<? super OUT> predicate) {
            return new StreamStage<OUT, OUT>(
                    list,
                    this,
                    new Function<Consumer<OUT>, Consumer<OUT>>() {
                        @Override
                        public Consumer<OUT> apply(Consumer<OUT> outConsumer) {
                            return new TransformChain<OUT, OUT>(outConsumer) {
                                @Override
                                public void accept(OUT out) {
                                    if (predicate.test(out)) {
                                        downstream.accept(out);
                                    }
                                }
                            };
                        }
                    },
                    parallel
            );
        }

        @Override
        public <R> SimpleStream<R> map(Function<? super OUT, ? extends R> mapper) {
            return new StreamStage<OUT, R>(
                    list,
                    this, //这里就是设置它的上层操作器
                    new Function<Consumer<R>, Consumer<OUT>>() {
                        @Override
                        public Consumer<OUT> apply(Consumer<R> outConsumer) {
                            return new TransformChain<OUT, R>(outConsumer) {
                                @Override
                                public void accept(OUT out) {
                                    // mapper就是开发传入的mapper参数,一个function表达式 t->t*2
                                    // 把执行得到的结果传给下一层的操作器
                                    downstream.accept(mapper.apply(out));
                                }
                            };
                        }
                    },
                    parallel
            );
        }


        @Override
        public SimpleStream<OUT> distinct() {
            return new StreamStage<OUT, OUT>(
                    list,
                    this, // 使用当前的 StreamStage 作为上游
                    new Function<Consumer<OUT>, Consumer<OUT>>() {
                        @Override
                        public Consumer<OUT> apply(Consumer<OUT> outConsumer) {
                            return new TransformChain<OUT, OUT>(outConsumer) {
                                // 使用一个 HashSet 来跟踪已经见过的元素
                                private Set<OUT> seen = new HashSet<>();

                                @Override
                                public void accept(OUT out) {
                                    // 只有当 set 中添加成功时（即元素是唯一的），才将其传递给下游
                                    if (seen.add(out)) {
                                        downstream.accept(out);
                                    }
                                }
                            };
                        }
                    },
                    parallel
            );
        }



        @Override
        @SuppressWarnings("unchecked")
        public List<OUT> collectToList() {
            if (parallel) {
                return processParallel();
            } else {
                return processSerial();
            }
        }

        private List<OUT> processParallel() {
            List<OUT> elements = new ArrayList<>();

            int processors = Runtime.getRuntime().availableProcessors();
            List<Future<Collection<?>>> futures = new ArrayList<>();
            List<? extends List<?>> partitionedLists = new Partition<>(list, list.size() / processors);

            for (List<?> subList : partitionedLists) {
                futures.add(CompletableFuture.supplyAsync(
                        () -> {
                            List<Object> subElements = new ArrayList<>();
                            for (Object el : subList) {
                                wrapFunctions(subElements::add).accept(el);
                            }

                            return subElements;
                        }));
            }

            CompletableFuture<Void> all = CompletableFuture.allOf(futures.toArray(new CompletableFuture[1]));
            try {
                all.get();
                for (Future<Collection<?>> future : futures) {
                    elements.addAll((Collection<? extends OUT>) future.get());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return elements;
        }

        Consumer wrapFunctions(Consumer lastConsumer) {
            Consumer consumer = lastConsumer;

            // 由底向上遍历consumer
            for (StreamStage stage = this; stage.prevStage != null; stage = stage.prevStage) {
                // 这里实际就是把当前操作的下级操作downstream置为consumer
                consumer = (Consumer) stage.consumerPipelineTransformer.apply(consumer);
            }
            // 最后返回头
            return consumer;
        }

        private List<OUT> processSerial() {
            List<OUT> elements = new ArrayList<>();

            Consumer<OUT> finalConsumer = elements::add;

            Consumer listElConsumer = wrapFunctions(finalConsumer);

            for (Object el : list) {
                listElConsumer.accept(el);
            }

            return elements;
        }
    }

    public static <T> SimpleStream<T> stream(List<T> list, boolean parallel) {
        return PerElementTransformStageChainStream.startStage(list, parallel);
    }


    // 猜测这个其实就是责任链的header
    private static <T> SimpleStream<T> startStage(List<T> list, boolean parallel) {

        return new StreamStage<T, T>(
                list,
                // function的目的是为了把downstream传入到consumer类中，也就是为当前的操作器设置下级操作器
                new Function<Consumer<T>, Consumer<T>>() {
                    @Override
                    public Consumer<T> apply(Consumer<T> tConsumer) { //这里是function的实现类
                        // 在创建TransformChain的时候传入的tConsumer参数就会把downstream置为该值
                        return new StreamStage.TransformChain<T, T>(tConsumer) { //这里的参数是构造函数的对应参数
                            // downstream是类TransformChain中定义的，执行下层操作,它本身也是一个consumer
                            // TransformChain实现了consumer类
                            @Override
                            public void accept(T t) {
                                downstream.accept(t);
                            }
                        };
                    }
                },
                parallel);
    }
}
