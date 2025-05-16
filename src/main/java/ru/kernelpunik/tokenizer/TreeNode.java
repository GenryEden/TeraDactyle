package ru.kernelpunik.tokenizer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Builder
@AllArgsConstructor
@Getter
public class TreeNode<T> {
    private T value;
    private List<TreeNode<T>> children;

    static <T> TreeNode<T> empty() {
        return new TreeNode<>(null, new ArrayList<>());
    }
}
