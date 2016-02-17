package com.github.winse

import org.apache.curator.framework.{CuratorFrameworkFactory, CuratorFramework}
import org.apache.curator.retry.ExponentialBackoffRetry

trait ZkClient {

  def createZkClient(conn: String): CuratorFramework = {
    val retryPolicy = new ExponentialBackoffRetry (1000, 3)
    val client = CuratorFrameworkFactory.newClient (conn, retryPolicy)
    client.start()
    client
  }

}
