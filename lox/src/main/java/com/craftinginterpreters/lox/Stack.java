package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.List;

class Stack<E> {

  private final List<E> delegate = new ArrayList<>();

  void push(E element) {
    delegate.add(element);
  }

  void pop() {
    delegate.remove(indexOfLast());
  }

  E peek() {
    return delegate.get(indexOfLast());
  }

  E get(int index) {
    return delegate.get(index);
  }

  int size() {
    return delegate.size();
  }

  boolean isEmpty() {
    return delegate.isEmpty();
  }

  private int indexOfLast() {
    return size() - 1;
  }
}
