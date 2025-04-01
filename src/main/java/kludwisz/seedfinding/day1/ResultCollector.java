package kludwisz.seedfinding.day1;

import java.util.ArrayList;

public class ResultCollector {
    public static final ArrayList<FirstFilter.Result> results = new ArrayList<>();

    public static void addResult(FirstFilter.Result result) {
        synchronized (results) {
            results.add(result);
        }
    }

    public static ArrayList<FirstFilter.Result> getResults() {
        return results;
    }
}
