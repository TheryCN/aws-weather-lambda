## AWS WEATHER

![Pipeline](https://github.com/TheryCN/ty-aws-weather/blob/master/ty-aws-weather-pipeline.png?raw=true)

# Kinesis Firehose

S3 Destination :

Prefix : base/year=!{timestamp:yyyy}/month=!{timestamp:MM})/day={timestamp:dd}/hour=!{timestamp:HH}/

Prefix erreur : error/!{firehose:random-string}/!{firehose:error-output-type}/!{timestamp:yyyy/MM/dd}/

# Athena

```sql
CREATE database weather_db;

CREATE EXTERNAL TABLE IF NOT EXISTS weather_logs (
         coord struct<lon:float,
         lat:float>,
         weather struct<title:string,
         description:string,
         temp:float,
         temp_min:float,
         temp_max:float,
         pressure:bigint,
         humidity:bigint,
         wind_speed:float,
         wind_deg:float,
         clouds:int>,
         datetime timestamp
) PARTITIONED BY (
         year int,
         month int,
         day int,
         hour int
)
ROW FORMAT SERDE 'org.openx.data.jsonserde.JsonSerDe'
WITH SERDEPROPERTIES (
  'serialization.format' = '1'
) LOCATION 's3://ty-weather/base/';

MSCK REPAIR TABLE weather_logs;

SELECT year, month, COUNT(*) FROM weather_logs GROUP BY year, month;
```
