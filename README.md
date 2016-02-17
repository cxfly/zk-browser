## node-zk-browser

[node-zk-browser](https://github.com/killme2008/node-zk-browser) 的java1.8版本

##  Build

mvn clean package -Ptar

## Configure

修改conf.yaml配置文件

	port : 3000
	zk_host : localhost:2181
	template_dir : ./views
	static_dir : ./public
	users : { "admin" : "admin" }
	debug : true

## Run

Type command to start app

        ./start.sh

You can visit node-zk now at

        http://localhost:9000

## Lisense

        Apache License Version 2.0

See LICENSE.txt file in the top level folder.
