package kludwisz.seedfinding.day1;

import java.util.ArrayList;

public class ResultCollector {
    public static final ArrayList<FirstFilter.Result> results = new ArrayList<>();
    public static final ArrayList<FirstFilter.Result2> results2 = new ArrayList<>();

    public static void addResult(FirstFilter.Result result) {
        synchronized (results) {
            results.add(result);
        }
    }

    public static ArrayList<FirstFilter.Result> getResults() {
        return results;
    }

    public static void addResult2(FirstFilter.Result2 result) {
        synchronized (results2) {
            results2.add(result);
        }
    }

    public static ArrayList<FirstFilter.Result2> getResults2() {
        return results2;
    }
}
