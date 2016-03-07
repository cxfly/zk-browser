package com.github.winse

import org.yaml.snakeyaml.Yaml
import spark.Spark._
import spark._

import scala.beans.BeanProperty

object ZkBrowserServer extends RequestHandlers with ZkClient {

  import com.github.winse.ZkBrowserServer.RouteConversions._

  private val config = new Yaml().loadAs(ZkBrowserServer.getClass.getResourceAsStream("/conf.yaml"), classOf[Config])

  private lazy val templateEngine: TemplateEngine =
    new BeetlTemplateEngine().setDirectoryForTemplateLoading(config.template_dir)

  def main(args: Array[String]) {
    host = config.zk_host
    users = config.users
    zookeeper = createZkClient(host)

    Runtime.getRuntime.addShutdownHook(new Thread(new Runnable() {
      override def run(): Unit = stop()
    }))

    externalStaticFileLocation(config.static_dir)
    port(config.port)

    get("/", (req: Request, res: Response) => res.redirect("/node-zk"))
    get("/node-zk/", (req: Request, res: Response) => modelAndView(Map(), "index.ejs"), templateEngine)
    get("/node-zk/tree", TREE, templateEngine)
    get("/node-zk/get", GET, templateEngine)
    get("/node-zk/children", CHILDREN)

    get(
      "/node-zk/create",
      (req: Request, res: Response) =>
        modelAndView(Map[String, Any]("layout" -> false, "user" -> req.session().attribute("user")), "create.ejs"),
      templateEngine)
    post("/node-zk/create", CREATE)
    post("/node-zk/edit", EDIT)
    post("/node-zk/delete", DELETE)

    post("/node-zk/login", LOGIN)
  }

  def stop() {
    zookeeper.close()
    Spark.stop();
  }

  case class User(name: String, passwd: String)
  class Config {
    @BeanProperty var port: Int = _
    @BeanProperty var zk_host: String = _
    @BeanProperty var template_dir: String = _
    @BeanProperty var static_dir: String = _
    @BeanProperty var users: java.util.Map[String, String] = _
    @BeanProperty var debug: Boolean = _
  }

  object RouteConversions {

    implicit def viewRoute2function(fn: (Request, Response) => ModelAndView): TemplateViewRoute =
      new TemplateViewRoute {
        def handle(request: Request, response: Response): ModelAndView = fn(request, response)
      }

    implicit def routeEmpty2function(fn: (Request, Response) => Unit): Route =
      new Route {
        def handle(request: Request, response: Response): Object = {
          fn(request, response);
          return null
        }
      }

    implicit def route2function(fn: (Request, Response) => Object): Route =
      new Route {
        def handle(request: Request, response: Response): Object = fn(request, response)
      }

  }

}
