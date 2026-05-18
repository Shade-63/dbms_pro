package com.hospital.bedalloc.util;

import javax.swing.*;
import java.util.concurrent.ExecutionException;

public class SwingWorkerUtil {
    public interface Task<T> {
        T perform() throws Exception;
    }

    public interface Callback<T> {
        void onComplete(T result);
        void onError(Exception e);
    }

    public static <T> void execute(Task<T> task, Callback<T> callback) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return task.perform();
            }

            @Override
            protected void done() {
                try {
                    callback.onComplete(get());
                } catch (InterruptedException | ExecutionException e) {
                    callback.onError(e);
                }
            }
        }.execute();
    }
}
