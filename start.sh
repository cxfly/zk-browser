#!/bin/sh

bin=`dirname $0`
bin=`cd "$bin"; pwd`

export APP_HOME=$bin
export APP_OPTS="-Dproc_zk-browser -server -Xms128M -Xmx1024M"

export LOGFILE=$APP_HOME/logs/zk-browser.log

export APP_CLASSPATH=$APP_HOME/conf
for f in $APP_HOME/lib/core/*.jar ; do
  export APP_CLASSPATH+=:$f
done

for f in $APP_HOME/lib/common/*.jar ; do
  export APP_CLASSPATH+=:$f
done

nohup ${JAVA_HOME}/bin/java $APP_OPTS -cp $APP_CLASSPATH com.github.winse.ZkBrowserServer >>$LOGFILE 2>&1 &
