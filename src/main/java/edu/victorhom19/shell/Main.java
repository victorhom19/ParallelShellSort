package edu.victorhom19.shell;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        List<Integer> array_sizes = List.of(100, 1_000);
        List<Integer> thread_num = List.of(2, 4, 8, 12);
        for (Integer array_size : array_sizes) {
            System.out.printf("Array size %d:\n", array_size);
            List<Integer> toSort = randomArr(array_size);
            Sorter sorter = new Sorter(1);
            for (Integer threads : thread_num) {
                sorter = new Sorter(threads);
                sorter.parallelSort(toSort);
                System.out.printf("Parallel with %d threads: %3.4f seconds elapsed\n", threads, sorter.getLastSortTime() / 1_000_000_000);
            }
            sorter.sequentialSort(toSort);
            System.out.printf("Sequential: %3.4f seconds elapsed\n", sorter.getLastSortTime() / 1_000_000_000);
            sorter.close();
            System.exit(0);
        }
    }

    private static List<Integer> randomArr(int size) {
        Random rand = new Random();
        List<Integer> original = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            original.add(rand.nextInt(1_000_000));
        }
        return original;
    }
}

