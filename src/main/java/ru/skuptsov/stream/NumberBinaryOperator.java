package ru.skuptsov.stream;

@FunctionalInterface
public interface NumberBinaryOperator {
    Number applyAsNumber(Number left, Number right);
}
