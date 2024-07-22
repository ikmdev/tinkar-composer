package dev.ikm.tinkar.composer.template;

@FunctionalInterface
public interface CommentConsumer {
    void accept(Comment comment);
}
