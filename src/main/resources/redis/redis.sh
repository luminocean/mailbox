#! /usr/bin/env sh

# 说明：
# 1. 首先将redis的可执行文件目录加入classpath中，从而可以直接执行redis-cli和redis-server命令
# 2. 执行./redis.sh start即可启动主从两个redis服务器，以及一个sentinel用于监视运行状态（如果master服务器宕机则立即切换slave服务器顶上）
# 3. 执行./redis.sh会关闭这三个进程
# 4. 所有的中间文件都在$runtime_dir变量指向的目录下

# 当前脚本所在的路径
script_dir="$(dirname $0)"
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
	redis-server ${script_dir}/master.conf

	echo "启动从redis服务器..."
	redis-server ${script_dir}/slave.conf

	echo "启动sentienl监视..."
	redis-server ${script_dir}/sentinel.conf --sentinel
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
