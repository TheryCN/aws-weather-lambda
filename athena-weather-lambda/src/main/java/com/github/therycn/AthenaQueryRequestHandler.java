package com.github.therycn;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.Gson;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.simple.JSONObject;
import software.amazon.awssdk.http.HttpStatusCode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

/**
 * Athena Query Request Handler.
 */
public class AthenaQueryRequestHandler implements RequestStreamHandler {

    private Gson gson = new Gson();

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        LambdaLogger logger = context.getLogger();
        logger.log("Start");
        JSONObject responseJson = null;
        try {
            List<MonthlyWeather> monthlyWeatherList = AthenaService.getInstance()
                    .query("SELECT year, month, COUNT(*) as count FROM weather_logs GROUP BY year, month;", MonthlyWeather.class);

            monthlyWeatherList.forEach(monthlyWeather -> logger.log(monthlyWeather.toString()));
            responseJson = new JSONObject();
            responseJson.put("statusCode", HttpStatusCode.OK);
            responseJson.put("body", gson.toJson(monthlyWeatherList));

            logger.log("End OK");
        } catch (Throwable e) {
            responseJson = new JSONObject();
            responseJson.put("statusCode", HttpStatusCode.INTERNAL_SERVER_ERROR);
            responseJson.put("body", gson.toJson(e));

            logger.log(e.getMessage());
            logger.log(ExceptionUtils.getStackTrace(e));
            logger.log("End Internal Server Error");
        } finally {
            OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
            writer.write(responseJson.toString());
            writer.close();
        }

    }
}