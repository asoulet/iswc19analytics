# Representativeness of the LOD cloud

## Goal

This implementation measures the representativeness of the Linked Open Data cloud.

## How to

1. Install the database schema by running the [sql script](representativeness/sql/histogram.sql) in the sql directory (for MySQL 8)
2. Configure the application by modifying the property files in [properties/](representativeness/properties)
   - `robusta_file.properties`: thread configuration (`producer_number` and `consumer_number`) / database configuration
   - `file.properties`: database configuration
3. Run the application
   - `TestHistogram`: test the application for one SPARQL endpoint (developpement purpose)
   - `LODHistogramApplication`: run the application with a server and a client (production purpose):
     1. Launch the server: `java -Xss256k -Xms256m -Xmx2048m -jar histogram.jar -a open`
     2. Start the server: `java -Xss256k -Xms256m -Xmx2048m -jar histogram.jar -a start`
     3. Stop the server: `java -Xss256k -Xms256m -Xmx2048m -jar histogram.jar -a stop`
