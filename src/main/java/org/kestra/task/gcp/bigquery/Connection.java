package org.kestra.task.gcp.bigquery;

import com.google.cloud.bigquery.*;
import org.kestra.core.exceptions.IllegalVariableEvaluationException;
import org.kestra.core.runners.RunContext;
import org.kestra.task.gcp.AbstractConnection;
import org.slf4j.Logger;

import java.io.IOException;

public class Connection extends AbstractConnection {
    public BigQuery of(String projectId, String location) {
        return BigQueryOptions
            .newBuilder()
            .setProjectId(projectId)
            .setLocation(location)
            .build()
            .getService();
    }

    public BigQuery of(String serviceAccount, String projectId, String location) {
        return BigQueryOptions
            .newBuilder()
            .setCredentials(this.credentials(serviceAccount))
            .setProjectId(projectId)
            .setLocation(location)
            .build()
            .getService();
    }

    public static JobId jobId(RunContext runContext) throws IllegalVariableEvaluationException {
        return JobId.of(runContext
            .render("{{flow.namespace}}.{{flow.id}}_{{execution.id}}_{{taskrun.id}}")
            .replace(".", "-")
        );
    }

    public static TableId tableId(String table) {
        String[] split = table.split("\\.");
        if (split.length == 2) {
            return TableId.of(split[0], split[1]);
        } else if (split.length == 3) {
            return TableId.of(split[0], split[1], split[2]);
        } else {
            throw new IllegalArgumentException("Invalid table name '" + table + "'");
        }
    }

    public static void handleErrors(Job queryJob, Logger logger) throws IOException {
        if (queryJob == null) {
            throw new IllegalArgumentException("Job no longer exists");
        } else if (queryJob.getStatus().getError() != null) {
            if (queryJob.getStatus().getExecutionErrors() != null) {
                queryJob
                    .getStatus()
                    .getExecutionErrors()
                    .forEach(bigQueryError -> {
                        logger.error(
                            "Error query with error [\n - {}\n]",
                            bigQueryError.toString()
                        );
                    });
            }

            if (queryJob.getStatus().getError() != null) {
                throw new IOException(queryJob.getStatus().getError().toString());
            }
        }
    }
}
