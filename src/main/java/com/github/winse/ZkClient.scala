package com.github.winse

import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.retry.ExponentialBackoffRetry

trait ZkClient {

  implicit def runnable2function(fn: () => Unit): Runnable =
    new Runnable {
      override def run = fn()
    }

  def createZkClient(conn: String): CuratorFramework = {
    val retryPolicy = new ExponentialBackoffRetry(1000, 3)
    val client = CuratorFrameworkFactory.newClient(conn, retryPolicy)
    client.start()

    Runtime.getRuntime.addShutdownHook(new Thread(() => client.close()))

    client
  }

  def send4LetterWord(host: String, port: Int, cmd: String): String = {
    return org.apache.zookeeper.client.FourLetterWordMain.send4LetterWord(host, port, cmd)
  }

}
