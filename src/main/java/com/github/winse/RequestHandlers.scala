package com.github.winse

import java.util.{Map => JMap, LinkedHashMap => JLinkedHashMap}

import org.apache.curator.framework.CuratorFramework
import org.apache.zookeeper.CreateMode
import org.apache.zookeeper.data.Stat
import play.api.libs.json.{JsArray, JsObject, Json}
import spark.{ModelAndView, Request, Response}

import scala.collection.JavaConversions._
import scala.collection.immutable.ListMap
import scala.collection.mutable

trait RequestHandlers extends ZkClient {

  var zookeeper: CuratorFramework = _
  var host: String = _
  var users: java.util.Map[String, String] = _

  def TREE =
    (req: Request, res: Response) => {
      val path = requestQueryPath(req)
      modelAndView(Map("layout" -> false, "path" -> path, "host" -> host), "tree.ejs")
    }

  def GET =
    (req: Request, res: Response) => {
      val path = requestQueryPath(req)

      val stat: java.util.Map[_, _] = Option(zookeeper.checkExists().forPath(path)) match {
        case Some(it: Stat) => ListMap(
          "czxid" -> it.getCzxid,
          "mzxid" -> it.getMzxid,
          "dataLength" -> it.getDataLength,
          "numChildren" -> it.getNumChildren,
          "version" -> it.getVersion,
          "cversion" -> it.getCversion,
          "aversion" -> it.getAversion,
          "ctime" -> it.getCtime,
          "mtime" -> it.getMtime,
          "ephemeralOwner" -> it.getEphemeralOwner,
          "pzxid" -> it.getPzxid
        )
        case None => Map[String, Any]()
      }

      val data = Option(zookeeper.getData.forPath(path)) match {
        case Some(it: Array[Byte]) => new String(it)
        case None => ""
      }

      modelAndView(
        Map("layout" -> false, "stat" -> stat, "data" -> data, "path" -> path, "host" -> host, "user" -> req.session().attribute("user")),
        "data.ejs")
    }

  def CHILDREN =
    (req: Request, res: Response) => {
      val path = requestQueryPath(req)

      val children = zookeeper.getChildren().forPath(path);

      res.header("Content-Type", "application/json");
      var result = List[JsObject]();
      for (child <- children) {
        val realPath = if (path == "/") s"/$child" else s"$path/$child"
        result ::= Json.obj(
          "attributes" -> Json.obj("path" -> realPath, "rel" -> "chv"),
          "data" -> Json.obj(
            "title" -> child,
            "icon" -> "ou.png",
            "attributes" -> Json.obj("href" -> ("/zk/get?path=" + realPath))
          ),
          "state" -> "closed"
        )
      }

      JsArray(result)
    }

  // @see http://zookeeper.apache.org/doc/trunk/zookeeperAdmin.html#The+Four+Letter+Words
  // @see org.apache.zookeeper.server.ServerCnxn#cmd2String
  def STAT =
    (req: Request, res: Response) => {
      val hostAndPorts = host.split(",").map(hp => {
        val pair = hp.split(":")
        (pair(0), if(pair.length < 2) 2181 else pair(1).toInt)
      })

      val cmds = Array(
        "ruok",
        "stat",
        "wchs",
        "mntr",
        "conf",
        "dump",
        ""
      )
      val map: JMap[String, JMap[String, String]] = new JLinkedHashMap[String, JMap[String, String]]
      for (
        (host, port) <- hostAndPorts;
        cmd <- cmds if (!cmd.isEmpty)
      ) {
        val response = send4LetterWord(host, port, cmd)
        map.getOrElseUpdate(s"$host:$port", new JLinkedHashMap[String, String]).put(cmd, response)
      }

      modelAndView(Map("stat" -> map), "stat.ejs")
    }

  def CREATE =
    (req: Request, res: Response) => {
      if (req.session().attribute("user") == null) {
        "Please logon..."
      } else {
        val path = req.queryParams("path")
        val data = req.queryParams("data")
        val flag = req.queryParams("flag").toInt

        try {
          zookeeper.create().withMode(CreateMode.fromFlag(flag)).forPath(path, data.getBytes);
          "Create ok."
        } catch {
          case it: Throwable => it.getMessage
        }
      }
    }

  def EDIT =
    (req: Request, res: Response) => {
      if (req.session().attribute("user") == null) {
        "Please logon..."
      } else {
        val path = req.queryParams("path")
        val new_data = req.queryParams("new_data")
        val version = req.queryParams("version").toInt;

        try {
          zookeeper.setData().withVersion(version).forPath(path, new_data.getBytes);
          "Update ok."
        } catch {
          case it: Throwable => it.getMessage
        }
      }
    }

  def DELETE =
    (req: Request, res: Response) => {
      if (req.session().attribute("user") == null) {
        "Please logon..."
      } else {
        val path = req.queryParams("path")
        val version = req.queryParams("version").toInt;
        val recursive = req.queryParams("recursive").toBoolean;

        try {
          val builder = zookeeper.delete()
          if (recursive) builder.deletingChildrenIfNeeded()

          builder.withVersion(version).forPath(path);
          "Delete ok."
        } catch {
          case it: Throwable => it.getMessage
        }
      }
    }

  def LOGIN =
    (req: Request, res: Response) => {
      val name = req.queryParams("user[name]")
      val password = req.queryParams("user[password]")
      if (users(name) == password) {
        req.session().attribute("user", name)
        res.cookie("user", name, 5 * 60 * 1000)
      }
      res.redirect(req.headers("Referer"))
    }

  def modelAndView(map: Map[String, Any], path: String) = {
    val jMap: java.util.Map[String, Any] = map
    new ModelAndView(jMap, path)
  }

  private def requestQueryPath(req: Request): String = Option(req.queryParams("path")) match {
    case Some(it: String) => it
    case None => "/"
  }
}
