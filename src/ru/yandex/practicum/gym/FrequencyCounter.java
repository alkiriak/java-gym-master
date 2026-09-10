package ru.yandex.practicum.gym;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Структура данных для отслеживания частоты элементов на базе LFU-кэша с бакетами.
 * <p>
 * Внешний двусвязный список корзин ({@link Bucket}), упорядоченный слева направо по возрастанию счетчика:
 * слева фиктивная голова (headBucket, count = 0), справа фиктивный хвост (tailBucket, count = Integer.MAX_VALUE).
 * Переход по next увеличивает частоту, переход по prev уменьшает.
 * <p>
 * Внутренний двусвязный список элементов ({@link Node}) внутри каждой корзины (добавление новых в хвост).
 * <p>
 * Хеш-мапа для прямого доступа к нодам элементов по ключу.
 * <p>
 * Сложность операций:
 * {@link #increment(Object)} - O(1) амортизировано.
 * {@link #getSortedEntries()} - O(K), где K - число уникальных элементов, сортировка не вызывается.
 */
public class FrequencyCounter<T> {

    /**
     * Узел элемента внутри корзины.
     */
    private static class Node<T> {
        final T value;
        Bucket<T> bucket;
        Node<T> prev;
        Node<T> next;

        Node(T value) {
            this.value = value;
        }
    }

    /**
     * Корзина, объединяющая элементы с одинаковой частотой count.
     */
    private static class Bucket<T> {
        final int count;
        Bucket<T> prev;
        Bucket<T> next;

        Node<T> head;
        Node<T> tail;

        Bucket(int count) {
            this.count = count;
        }

        /**
         * Добавляет элемент в конец (хвост) списка корзины за O(1).
         */
        void addNode(Node<T> node) {
            node.bucket = this;
            if (head == null) {
                head = tail = node;
                node.prev = node.next = null;
            } else {
                tail.next = node;
                node.prev = tail;
                node.next = null;
                tail = node;
            }
        }

        /**
         * Удаляет элемент из списка корзины за O(1).
         */
        void removeNode(Node<T> node) {
            if (node.prev != null) {
                node.prev.next = node.next;
            } else {
                head = node.next;
            }

            if (node.next != null) {
                node.next.prev = node.prev;
            } else {
                tail = node.prev;
            }

            node.prev = node.next = null;
        }

        boolean isEmpty() {
            return head == null;
        }
    }

    private final Map<T, Node<T>> nodeMap = new HashMap<>();

    // Фиктивные граничные корзины
    private final Bucket<T> headBucket = new Bucket<>(0);
    private final Bucket<T> tailBucket = new Bucket<>(Integer.MAX_VALUE);

    public FrequencyCounter() {
        headBucket.next = tailBucket;
        tailBucket.prev = headBucket;
    }

    /**
     * Увеличивает счетчик частоты элемента на 1 за время O(1).
     */
    public void increment(T value) {
        Node<T> node = nodeMap.get(value);
        if (node == null) {
            node = new Node<>(value);
            nodeMap.put(value, node);

            // Ищем или создаем корзину count = 1 сразу справа от headBucket
            Bucket<T> targetBucket = headBucket.next;
            if (targetBucket.count != 1) {
                targetBucket = createBucket(1, headBucket, headBucket.next);
            }
            targetBucket.addNode(node);
        } else {
            Bucket<T> currentBucket = node.bucket;
            int newCount = currentBucket.count + 1;
            Bucket<T> targetBucket = currentBucket.next;

            // Если следующая корзина справа имеет больший count, вставляем новую между current и next
            if (targetBucket.count != newCount) {
                targetBucket = createBucket(newCount, currentBucket, currentBucket.next);
            }

            currentBucket.removeNode(node);
            targetBucket.addNode(node);

            // Если в старой корзине не осталось элементов, удаляем её из цепочки
            if (currentBucket.isEmpty()) {
                unlinkBucket(currentBucket);
            }
        }
    }

    /**
     * Возвращает список элементов и их частот, отсортированный по убыванию частоты за O(K).
     */
    public List<Map.Entry<T, Integer>> getSortedEntries() {
        List<Map.Entry<T, Integer>> result = new ArrayList<>(nodeMap.size());
        Bucket<T> curBucket = tailBucket.prev;

        while (curBucket != headBucket) {
            Node<T> curNode = curBucket.head;
            while (curNode != null) {
                Map.Entry<T, Integer> entry = new AbstractMap.SimpleImmutableEntry<>(curNode.value, curBucket.count);
                result.add(entry);
                curNode = curNode.next;
            }
            curBucket = curBucket.prev;
        }

        return Collections.unmodifiableList(result);
    }

    /**
     * Создает новую корзину и встраивает её между корзинами prev и next за O(1).
     */
    private Bucket<T> createBucket(int count, Bucket<T> prev, Bucket<T> next) {
        Bucket<T> newBucket = new Bucket<>(count);
        newBucket.prev = prev;
        newBucket.next = next;
        prev.next = newBucket;
        next.prev = newBucket;
        return newBucket;
    }

    /**
     * Исключает пустую корзину из двусвязного списка за O(1).
     */
    private void unlinkBucket(Bucket<T> bucket) {
        bucket.prev.next = bucket.next;
        bucket.next.prev = bucket.prev;
        bucket.prev = null;
        bucket.next = null;
    }
}
