#!/bin/bash

#Cron Deployment
#15 * * * * /path/to/run_pipeline.sh
# Set paths
APPLICATION_PATH=/path/to/application.jar
MAIN_CLASS=PipelineMain
INPUT_PATH=/path/to/folder
SUCCESSFUL_PATH=/path/to/successful/folder
UNSUCCESSFUL_PATH=/path/to/unsuccessful/folder
DRIVER_MEMORY=driver_memory
EXECUETOR_MEMORY=executor_memory
EXECUETOR_NUM=executor_num
EXECUETOR_CORES=executor_cores

date=`date '+%Y%m%d%H'`
batchtime=`echo $date`
echo "Started for $batchtime ###############################"
	spark-submit --master yarn --deploy-mode client --class $MAIN_CLASS \
	--driver-memory $DRIVER_MEMORY --executor-memory $EXECUETOR_MEMORY --num-executors $EXECUETOR_NUM --executor-cores $EXECUETOR_CORES \
	$APPLICATION_PATH \
	batchTime=$batchtime input_path=$INPUT_PATH successful_output_path=$SUCCESSFUL_PATH unsuccessful_output_path=$UNSUCCESSFUL_PATH \
	> ~/path/to/log/app.log 2>&1

echo "Finished for $batchtime ###############################"