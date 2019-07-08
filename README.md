# Spark Submitter Console

Spark Submitter Console is a tinny but useful web console in which you can 

1. Uploading jar
2. Submitting jar to Yarn cluster
3. Monitoring/Manager your applications:  

![](http://docs.mlsql.tech/upload_images/mm/WX20190708-185353@2x.png)

![](http://docs.mlsql.tech/upload_images/mm/WX20190708-190010@2x.png)


## How to setup

1. Clone and package:

    ```
    mvn clean package -Pshade
    ```

2. find the spark_jobs_2017-08-20.sql in resources directory and import to MySQL.

3. Create /tmp/jdbc.properties like this:
   
   ```
   url=jdbc:mysql://127.0.0.1:3306/spark_jobs?useUnicode=true&characterEncoding=utf8
   userName=xxxx
   password=xxx
   ```
4. Find the jar and start it:

```shell
java -jar .:[your jar path] tech.mlsql.spark.submitter.SparkSubmitterConsoleApp \
  -yarnUrl http://127.0.0.1 \
  -jdbcPath /tmp/jdbc.properties 
```

## Limitation for 0.1.0

1. Only support Yarn Cluster
2. No Auth available. We recommend you to placed it behind Nginx which can provide the auth.
3. No high availability design





 