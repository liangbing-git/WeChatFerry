package com.wechat.ferry.utils;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPoolUtils {

    /**
     * 创建自定义线程池
     *
     * @param threadNamePrefix 线程名前缀
     * @param corePoolSize     核心线程数
     * @param maxPoolSize      最大线程数
     * @param queueCapacity    队列容量
     * @param keepAliveTime    线程空闲时间（单位：秒）
     * @return 自定义线程池
     */
    public static ThreadPoolExecutor createThreadPool(String threadNamePrefix,
                                                      int corePoolSize,
                                                      int maxPoolSize,
                                                      int queueCapacity,
                                                      long keepAliveTime) {
        // 自定义线程工厂，用于设置线程名称
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, threadNamePrefix + "-thread-" + threadNumber.getAndIncrement());
                thread.setDaemon(false); // 设置为非守护线程
                return thread;
            }
        };

        // 创建阻塞队列
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(queueCapacity);

        // 创建线程池
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                corePoolSize,         // 核心线程数
                maxPoolSize,          // 最大线程数
                keepAliveTime,        // 线程空闲时间
                TimeUnit.SECONDS,     // 时间单位
                workQueue,            // 任务队列
                threadFactory,        // 线程工厂
                new ThreadPoolExecutor.AbortPolicy() // 拒绝策略
        );

        return executor;
    }

    /**
     * 关闭线程池
     *
     * @param executor 线程池
     */
    public static void shutdownThreadPool(ThreadPoolExecutor executor) {
        if (executor != null) {
            executor.shutdown(); // 停止接受新任务
            try {
                // 等待现有任务完成
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow(); // 强制终止任务
                }
            } catch (InterruptedException e) {
                executor.shutdownNow(); // 强制终止任务
                Thread.currentThread().interrupt(); // 恢复中断状态
            }
        }
    }
}