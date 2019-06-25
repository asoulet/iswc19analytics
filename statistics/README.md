# Statistics usage on the LOD cloud

## Goal

This implementation measures property usage and class of the Linked Open Data cloud.

## How to

1. Install the database schema by running the [sql script](https://github.com/asoulet/iswc19analytics/blob/master/statistics/sql/statistics.sql) in the sql directory (for MySQL 8)
2. Configure the application by modifying the property files in [properties/](https://github.com/asoulet/iswc19analytics/tree/master/statistics/properties)
   - `robusta_file.properties`: thread configuration (`producer_number` and `consumer_number`) / database configuration
   - `file.properties`: database configuration
3. Run the application
   - `TestStatistics`: test the application for one SPARQL endpoint (developpement purpose)
   - `LODStatisticsApplication`: run the application with a server and a client (production purpose):
     1. Launch the server: `java -Xss256k -Xms256m -Xmx2048m -jar statistics.jar -a open`
     2. Start the server: `java -Xss256k -Xms256m -Xmx2048m -jar statistics.jar -a start`
     3. Stop the server: `java -Xss256k -Xms256m -Xmx2048m -jar statistics.jar -a stop`
