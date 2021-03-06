package org.kestra.task.gcp.bigquery;

import com.google.cloud.bigquery.Acl;
import com.google.cloud.bigquery.DatasetInfo;
import com.google.cloud.bigquery.EncryptionConfiguration;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.kestra.core.models.annotations.InputProperty;
import org.kestra.core.models.annotations.OutputProperty;
import org.kestra.core.models.tasks.RunnableTask;
import org.kestra.core.models.tasks.Task;
import org.kestra.core.runners.RunContext;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
abstract public class AbstractDataset extends AbstractBigquery implements RunnableTask<AbstractDataset.Output> {
    @NotNull
    @InputProperty(
        description = "The dataset's user-defined id",
        dynamic = true
    )
    protected String name;

    @InputProperty(
        description = "The dataset's access control configuration"
    )
    protected List<Acl> acl;

    @InputProperty(
        description = "The default lifetime of all tables in the dataset, in milliseconds",
        body = "The minimum value is\n" +
            " 3600000 milliseconds (one hour). Once this property is set, all newly-created tables in the\n" +
            " dataset will have an expirationTime property set to the creation time plus the value in this\n" +
            " property, and changing the value will only affect new tables, not existing ones. When the\n" +
            " expirationTime for a given table is reached, that table will be deleted automatically. If a\n" +
            " table's expirationTime is modified or removed before the table expires, or if you provide an\n" +
            " explicit expirationTime when creating a table, that value takes precedence over the default\n" +
            " expiration time indicated by this property. This property is experimental and might be\n" +
            " subject to change or removed."
    )
    protected Long defaultTableLifetime;

    @InputProperty(
        description = "Description",
        body = "A user-friendly description for the dataset.",
        dynamic = true
    )
    protected String description;

    @InputProperty(
        description = "A user-friendly name for the dataset",
        dynamic = true
    )
    protected String friendlyName;

    @InputProperty(
        description = "The geographic location where the dataset should reside",
        body = "This property is experimental\n" +
            " and might be subject to change or removed.\n" +
            " \n" +
            " See <a href=\"https://cloud.google.com/bigquery/docs/reference/v2/datasets#location\">Dataset\n" +
            "      Location</a>",
        dynamic = true
    )
    protected String location;

    @InputProperty(
        description = "The default encryption key for all tables in the dataset",
        body = "Once this property is set, all\n" +
            " newly-created partitioned tables in the dataset will have encryption key set to this value,\n" +
            " unless table creation request (or query) overrides the key."
    )
    protected EncryptionConfiguration defaultEncryptionConfiguration;

    @InputProperty(
        description = "[Optional] The default partition expiration time for all partitioned tables in the dataset, in milliseconds",
        body = " Once this property is set, all newly-created partitioned tables in the\n" +
            " dataset will has an expirationMs property in the timePartitioning settings set to this value.\n" +
            " Changing the value only affect new tables, not existing ones. The storage in a partition will\n" +
            " have an expiration time of its partition time plus this value. Setting this property\n" +
            " overrides the use of defaultTableExpirationMs for partitioned tables: only one of\n" +
            " defaultTableExpirationMs and defaultPartitionExpirationMs will be used for any new\n" +
            " partitioned table. If you provide an explicit timePartitioning.expirationMs when creating or\n" +
            " updating a partitioned table, that value takes precedence over the default partition\n" +
            " expiration time indicated by this property. The value may be null."
    )
    protected Long defaultPartitionExpirationMs;

    @InputProperty(
        description = "The dataset's labels"
    )
    protected Map<String, String> labels;

    protected DatasetInfo datasetInfo(RunContext runContext) throws Exception {
        DatasetInfo.Builder builder = DatasetInfo.newBuilder(runContext.render(this.name));

        if (this.acl != null) {
            builder.setAcl(this.acl);
        }

        if (this.defaultTableLifetime != null) {
            builder.setDefaultTableLifetime(this.defaultTableLifetime);
        }

        if (this.description != null) {
            builder.setDescription(runContext.render(this.description));
        }

        if (this.friendlyName != null) {
            builder.setFriendlyName(runContext.render(this.friendlyName));
        }

        if (this.location != null) {
            builder.setLocation(runContext.render(this.location));
        }

        if (this.defaultEncryptionConfiguration != null) {
            builder.setDefaultEncryptionConfiguration(this.defaultEncryptionConfiguration);
        }

        if (this.defaultPartitionExpirationMs != null) {
            builder.setDefaultPartitionExpirationMs(this.defaultPartitionExpirationMs);
        }

        if (this.labels != null) {
            builder.setLabels(this.labels);
        }

        return builder.build();
    }

    @Builder
    @Getter
    public static class Output implements org.kestra.core.models.tasks.Output {

        @NotNull
        @OutputProperty(
            description = "The dataset's user-defined id"
        )
        private String dataset;

        @NotNull
        @OutputProperty(
            description = "The GCP project id"
        )
        private String project;

        @NotNull
        @OutputProperty(
            description = "A user-friendly name for the dataset"
        )
        private String friendlyName;

        @NotNull
        @OutputProperty(
            description = "A user-friendly description for the dataset"
        )
        private String description;

        @NotNull
        @OutputProperty(
            description = "The geographic location where the dataset should reside",
            body = "This property is experimental\n" +
                " and might be subject to change or removed.\n" +
                " \n" +
                " See <a href=\"https://cloud.google.com/bigquery/docs/reference/v2/datasets#location\">Dataset\n" +
                "      Location</a>"
        )
        private String location;

        public static Output of(DatasetInfo dataset) {
            return Output.builder()
                .dataset(dataset.getDatasetId().getDataset())
                .project(dataset.getDatasetId().getProject())
                .friendlyName(dataset.getFriendlyName())
                .description(dataset.getDescription())
                .location(dataset.getLocation())
                .build();
        }
    }
}
