package com.github.therycn;

import org.apache.commons.lang3.reflect.FieldUtils;
import software.amazon.awssdk.services.athena.AthenaClient;
import software.amazon.awssdk.services.athena.model.*;
import software.amazon.awssdk.services.athena.paginators.GetQueryResultsIterable;

import java.util.ArrayList;
import java.util.List;

/**
 * Athena Service.
 */
public class AthenaService {

    private static final String DB_NAME = "weather_db";

    private AthenaClient athenaClient;

    private ResultConfiguration resultConfiguration;

    private static final AthenaService instance = new AthenaService();

    private AthenaService() {
        this.athenaClient = AthenaClientFactory.createClient();
        this.resultConfiguration = AthenaClientFactory.createResultConfig();
    }

    public static AthenaService getInstance() {
        return instance;
    }

    public <T> List<T> query(String query, Class<T> tClass) throws InterruptedException, IllegalAccessException, InstantiationException {
        String queryExecutionId = executeQuery(query);
        waitUntilProcessing(queryExecutionId);
        return extract(queryExecutionId, tClass);
    }

    private String executeQuery(String query) {
        // The QueryExecutionContext allows us to set the Database.
        QueryExecutionContext queryExecutionContext = QueryExecutionContext.builder()
                .database(DB_NAME).build();

        // Create the StartQueryExecutionRequest to send to Athena which will start the query.
        StartQueryExecutionRequest startQueryExecutionRequest = StartQueryExecutionRequest.builder()
                .queryString(query)
                .queryExecutionContext(queryExecutionContext)
                .resultConfiguration(resultConfiguration).build();

        StartQueryExecutionResponse startQueryExecutionResponse = athenaClient.startQueryExecution(startQueryExecutionRequest);
        return startQueryExecutionResponse.queryExecutionId();
    }

    public void waitUntilProcessing(String queryExecutionId) throws InterruptedException {
        GetQueryExecutionRequest getQueryExecutionRequest = GetQueryExecutionRequest.builder()
                .queryExecutionId(queryExecutionId).build();

        GetQueryExecutionResponse getQueryExecutionResponse;
        boolean isQueryStillRunning = true;
        while (isQueryStillRunning) {
            getQueryExecutionResponse = athenaClient.getQueryExecution(getQueryExecutionRequest);
            String queryState = getQueryExecutionResponse.queryExecution().status().state().toString();
            if (queryState.equals(QueryExecutionState.FAILED.toString())) {
                throw new RuntimeException("Query Failed to run with Error Message: " + getQueryExecutionResponse
                        .queryExecution().status().stateChangeReason());
            } else if (!queryState.equals(QueryExecutionState.RUNNING.toString())) {
                isQueryStillRunning = false;
            } else {
                Thread.sleep(500l);
            }
        }
    }

    public <T> List<T> extract(String queryExecutionId, Class<T> tClass) throws IllegalAccessException, InstantiationException {
        GetQueryResultsRequest getQueryResultsRequest = GetQueryResultsRequest.builder()
                .queryExecutionId(queryExecutionId).build();
        GetQueryResultsIterable getQueryResultsResults = athenaClient.getQueryResultsPaginator(getQueryResultsRequest);
        List<T> elementList = new ArrayList<>();
        for (GetQueryResultsResponse getQueryResultsResponse : getQueryResultsResults) {
            List<ColumnInfo> columnInfoList = getQueryResultsResponse.resultSet().resultSetMetadata().columnInfo();
            List<Row> rowList = new ArrayList<>(getQueryResultsResponse.resultSet().rows());

            // The first line is the header, remove it
            rowList.remove(0);
            for (Row row : rowList) {
                int i = 0;
                T element = tClass.newInstance();
                for (Datum datum : row.data()) {
                    FieldUtils.writeField(element, columnInfoList.get(i).name(), datum.varCharValue(), true);
                    i++;
                }
                elementList.add(element);
            }
        }

        return elementList;
    }
}
