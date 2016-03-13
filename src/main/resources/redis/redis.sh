#! /usr/bin/env sh

# 使用方法：首先将redis的可执行文件目录加入classpath中
# 然后执行./redis.sh 即可启动主从两个redis服务器，以及一个sentinel用于监视运行状态（如果master服务器宕机则立即切换slave服务器顶上）
# 执行./redis.sh会关闭这三个进程
# 所有的中间文件都在$runtime_dir目录下
# 需要注意的是，默认情况下6379端口是master,7379是slave。如果反过来请将.conf文件最后的一些自动生成的配置删除即可恢复

# 临时文件存放目录
runtime_dir="/tmp/redis"
data_dir="/tmp/redis/data"

# 操作命令，有start和stop两个选项
command=$1

start(){
	echo "准备pid与日志临时目录$runtime_dir..."
	mkdir -p $runtime_dir

	echo "准备数据dump目录$data_dir..."
	mkdir -p "$data_dir"

	echo "启动主redis服务器..."
	redis-server ./master.conf

	echo "启动从redis服务器..."
	redis-server ./slave.conf

	echo "启动sentienl监视..."
	redis-server ./sentinel.conf --sentinel
}

stop(){
	echo "停止所有redis相关服务..."
	# 保存当前目录
	dir=$(pwd)
	cd $runtime_dir

	for pid_file in $(ls -1 *.pid | sort -r); do
		pid=$(cat $pid_file)
		kill $pid
	done

	cd $dir
}

if [ "$command" == "start" ]; then
	start
elif [ "$command" == "stop" ];then
	stop
else
	echo "[usage]:redis.sh start|stop"
fi
