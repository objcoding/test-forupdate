package com.objcoding.transaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

@SpringBootApplication
public class TransactionApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(TransactionApplication.class, args);
    }

    @Autowired
    private ForupdateMapper forupdateMapper;

    CountDownLatch countDownLatch = new CountDownLatch(1);
    ReentrantLock reentrantLock = new ReentrantLock();

//    @Override
//    public void run(String... args) throws Exception {
//
//        reentrantLock.lock();
//
//        new Thread(() -> {
//            forupdate();
//        }).start();
//
//        countDownLatch.await();
//        this.forupdateMapper.updateByState("CA");
//        System.out.println("==========update success==========");
//
//        reentrantLock.unlock();
//    }

    @Override
    public void run(String... args) throws Exception {

        AtomicInteger atomicInteger = new AtomicInteger();

        for (int i = 0; i < 10; i++) {
            this.forupdateMapper.findByName("testforupdate");
            System.out.println("========ok:" + atomicInteger.getAndIncrement());
        }

    }

    @Transactional
    public void forupdate() {
        this.forupdateMapper.findByName("testforupdate");
        System.out.println("==========for update==========");
        countDownLatch.countDown();
        reentrantLock.lock();

    }
}
