/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.pulsar.common.util;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This class is aimed at simplifying work with {@code CompletableFuture}.
 */
public class FutureUtil {

    /**
     * Return a future that represents the completion of the futures in the provided list.
     *
     * @param futures
     * @return
     */
    public static <T> CompletableFuture<Void> waitForAll(List<CompletableFuture<T>> futures) {
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    public static <T> CompletableFuture<T> failedFuture(Throwable t) {
        CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(t);
        return future;
    }

    public static Throwable unwrapCompletionException(Throwable t) {
        if (t instanceof CompletionException) {
            return unwrapCompletionException(t.getCause());
        } else {
            return t;
        }
    }

    public static <T> CompletableFuture<T> futureWithDeadline(ScheduledExecutorService executor, Long delay,
            TimeUnit unit, Exception exp) {
        CompletableFuture<T> future = new CompletableFuture<T>();
        executor.schedule(() -> {
            if (!future.isDone()) {
                future.completeExceptionally(exp);
            }
        }, delay, unit);
        return future;
    }

    public static <T> CompletableFuture<T> futureWithDeadline(ScheduledExecutorService executor) {
        return futureWithDeadline(executor, 60000L, TimeUnit.MILLISECONDS,
                new TimeoutException("Future didn't finish within deadline"));
    }

    public static <T> Optional<Throwable> getException(CompletableFuture<T> future) {
        if (future != null && future.isCompletedExceptionally()) {
            try {
                future.get();
            } catch (InterruptedException e) {
                return Optional.ofNullable(e);
            } catch (ExecutionException e) {
                return Optional.ofNullable(e.getCause());
            }
        }
        return Optional.empty();
    }
}
