package edu.victorhom19.shell;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Sorter implements Closeable {
    private final ExecutorService executorService;
    private final int poolSize;

    private long startTime;
    private long endTime;

    public Sorter(int poolSize) {
        this.poolSize = poolSize;
        this.executorService = Executors.newFixedThreadPool(poolSize);
    }

    public List<Integer> parallelSort(List<Integer> array) throws ExecutionException, InterruptedException {
        this.startTime = System.nanoTime();
        final AtomicInteger counter = new AtomicInteger();
        int chunkSize = array.size() / poolSize;
        if (array.size() % poolSize != 0) {
            chunkSize++;
        }
        int finalChunkSize = chunkSize;
        List<List<Integer>> partitions = new ArrayList<>(array.stream()
                .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / finalChunkSize))
                .values());
        List<Future<?>> tasks = new ArrayList<>(partitions.size());
        for (int i = 0; i < partitions.size(); i++) {
            int finalI = i;
            Future<?> future = executorService.submit(() -> shellSort(partitions.get(finalI)));
            tasks.add(future);
        }
        for (Future<?> task : tasks) {
            task.get();
        }
        List<Integer> sorted = merge(partitions);
        this.endTime = System.nanoTime();
        return sorted;
    }

    public List<Integer> sequentialSort(List<Integer> array) {
        this.startTime = System.nanoTime();
        List<Integer> sorted = shellSort(array);
        this.endTime = System.nanoTime();
        return sorted;
    }

    public float getLastSortTime() {
        return endTime - startTime;
    }



    public List<Integer> merge(List<List<Integer>> arr) {
        int sz = arr.size();
        if (sz == 1) {
            return arr.get(0);
        }
        if (sz == 2) {
            return smallMerge(arr.get(0), arr.get(1));
        }
        int mid = sz / 2;
        List<Integer> left = merge(arr.subList(0, mid));
        List<Integer> right = merge(arr.subList(mid, sz));
        return smallMerge(left, right);
    }

    private List<Integer> smallMerge(List<Integer> first, List<Integer> second) {
        int resSize = first.size() + second.size();
        List<Integer> res = new ArrayList<>(resSize);
        int p1 = 0;
        int p2 = 0;
        while (p1 + p2 < resSize) {
            if (p1 == first.size()) {
                res.add(second.get(p2++));
                continue;
            }
            if (p2 == second.size()) {
                res.add(first.get(p1++));
                continue;
            }
            if (first.get(p1) < second.get(p2)) {
                res.add(first.get(p1++));
            } else {
                res.add(second.get(p2++));
            }
        }
        return res;
    }

    private static List<Integer> shellSort(List<Integer> data) {
        int n = data.size();
        for (int step = n / 2; step > 0; step /= 2) {
            for (int i = step; i < n; i++) {
                for (int j = i - step; j >= 0 && data.get(j) > data.get(j + step) ; j -= step) {
                    int x = data.get(j);
                    data.set(j, data.get(j + step));
                    data.set(j + step, x);
                }
            }
        }
        return data;
    }

    @Override
    public void close() {
        executorService.shutdown();
    }
}
