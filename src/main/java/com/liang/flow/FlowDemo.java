package com.liang.flow;

import java.time.LocalDateTime;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

public class FlowDemo {

    // 定义一个处理器，只用写订阅者的接口
    static class MyProcessor extends SubmissionPublisher<String> implements Flow.Processor<String, String> {

        private Flow.Subscription subscription; // 保存订阅关系

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            System.out.println("发布订阅绑定成功" + subscription);
            this.subscription = subscription;
            subscription.request(1); // 请求一个数据
        }

        @Override // 当一个元素到达时，执行这个回调
        public void onNext(String item) {
            System.out.println("Processor处理器收到数据：" + item);
            // 对数据进行处理
            String processedItem = item.toUpperCase();
            submit(processedItem); // 将处理后的数据发布给订阅者
            subscription.request(1); // 请求下一个数据
        }

        @Override // 当发生错误时，执行这个回调
        public void onError(Throwable throwable) {
            System.out.println("Processor处理器收到错误：" + throwable);
        }

        @Override // 当订阅完成时，执行这个回调
        public void onComplete() {
            System.out.println("Processor处理器收到完成通知");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // 1.定义一个发布者，发布数据
        SubmissionPublisher<String> publisher = new SubmissionPublisher<>();

        // 2.定义一个处理器，处理数据
        MyProcessor processor = new MyProcessor();
        
        // 3.定义一个订阅者，订阅数据
        Flow.Subscriber<String> subscriber = new Flow.Subscriber<>() {
            private Flow.Subscription subscription;

            @Override // 当订阅成功时，会调用此方法
            public void onSubscribe(Flow.Subscription subscription) {
                System.out.println(Thread.currentThread().getName() + "收到订阅：" + subscription);
                this.subscription = subscription;
                // 从上游请求一个数据
                this.subscription.request(1);
            }

            @Override // 当一个元素到达时，执行这个回调
            public void onNext(String item) {
                System.out.println(LocalDateTime.now() + "订阅者1收到数据：" + item);
                this.subscription.request(1);
            }

            @Override // 当发生错误时，执行这个回调
            public void onError(Throwable throwable) {
                System.out.println("订阅者1收到错误：" + throwable);
            }

            @Override // 当订阅完成时，执行这个回调
            public void onComplete() {
                System.out.println("订阅者1收到完成通知");
            }
        };

        // 4.绑定发布者和订阅者
        publisher.subscribe(processor);
        processor.subscribe(subscriber);

        for (int i = 0; i < 10; i++) {
            // 发布十条数据，发布的数据存到 Object[] 缓冲区
            publisher.submit("data-" + i); // 发布数据
        }

        // 关闭发布者
        publisher.close();

        // 主线程不能立即退出，否则数据处理不完，程序会立即退出
        Thread.sleep(5000);
    }
}
