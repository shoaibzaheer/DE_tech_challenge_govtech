# DE_tech_challenge Pipeline
## Pipeline Deployment Steps
1. Compile and Package
   %mvn clean package
2. Copy jar to deployment folder
3. Copy run_pipeline.sh to deployment folder
4. Change paths and parameters in the run_pipeline.sh script
5. open crontab in edit mode using following command and put cron string as below:
   %crontab -l
6. 15 * * * * /path/to/shell/script.sh