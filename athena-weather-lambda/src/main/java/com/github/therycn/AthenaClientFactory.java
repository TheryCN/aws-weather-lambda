package com.github.therycn;

import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.athena.AthenaClient;
import software.amazon.awssdk.services.athena.model.ResultConfiguration;

/**
 * Athena Client Factory.
 */
public class AthenaClientFactory {

    public static AthenaClient createClient() {
        return AthenaClient.builder()
                .region(Region.EU_WEST_3)
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .build();
    }

    public static ResultConfiguration createResultConfig() {
        return ResultConfiguration.builder()
                .outputLocation("s3://ty-athena-query-results-bucket/").build();
    }
}
