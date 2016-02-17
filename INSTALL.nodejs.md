@2016-2-16 16:36:37

## patch

$ git diff
diff --git a/package.json b/package.json
index c736a84..8c8a9c2 100644
--- a/package.json
+++ b/package.json
@@ -4,7 +4,7 @@
   "dependencies": {
     "ejs": ">= 0.7.2",
     "express": "3.x",
-    "zookeeper":">=3.4.1-4",
+    "zookeeper":"3.4.7-2",
     "express-namespace":">=0.1.1"
   }
 }

## build

yum install -y npm
# npm install -g npm
npm install -g cnpm --registry=https://registry.npm.taobao.org

cd /home/hadoop/node-zk-browser/
cnpm install

./start.sh 

less logs/node-zk-browser.log 