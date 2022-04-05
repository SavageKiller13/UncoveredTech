package com.thesecretden.uncovered_tech.main.api.utils;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

import java.util.Objects;

public class FastEither<L, R> {
    private final L left;
    private final R right;

    private FastEither(L left, R right) {
        Preconditions.checkState((left != null)^(right != null));
        this.left = left;
        this.right = right;
    }

    public static <L, R> FastEither<L, R> left(L l) {
        return new FastEither<>(l, null);
    }

    public static <L, R> FastEither<L, R> right(R r) {
        return new FastEither<>(null, r);
    }

    public boolean isLeft() {
        return left != null;
    }

    public boolean isRight() {
        return right != null;
    }

    public L leftNonNull() {
        return Preconditions.checkNotNull(left);
    }

    public R rightNonNull() {
        return Preconditions.checkNotNull(right);
    }

    public <T> T map(Function<L, T> left, Function<R, T> right) {
        if (isLeft())
            return left.apply(leftNonNull());
        else
            return right.apply(rightNonNull());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FastEither<?, ?> that = (FastEither<?, ?>) o;
        return Objects.equals(left, that.left) && Objects.equals(right, that.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }

    public L orThrow() {
        return map(l -> l, r -> {
            if (r instanceof Throwable)
                throw new RuntimeException((Throwable) r);
            throw new RuntimeException(r.toString());
        });
    }
}
