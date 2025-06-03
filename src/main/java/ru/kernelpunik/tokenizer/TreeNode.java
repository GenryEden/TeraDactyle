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

    public TreeNode(T value) {
        this(value, new ArrayList<>());
    }

    static <T> TreeNode<T> empty() {
        return new TreeNode<>(null, new ArrayList<>());
    }
    public void addChild(TreeNode<T> child) {
        children.add(child);
    }
}
