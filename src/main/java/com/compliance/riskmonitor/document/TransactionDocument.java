package com.compliance.riskmonitor.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
//@Document(indexName = "transactions")
@Document(indexName = "transactions", writeTypeHint = WriteTypeHint.FALSE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDocument {

    @Id
    private String transactionId;

    @Field(type = FieldType.Keyword)
    private String userId;

    @Field(type = FieldType.Double)
    private BigDecimal amount;

    @Field(type = FieldType.Keyword)
    private String currency;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String merchant;

    // Keyword = exact match, Text = full-text search
    @Field(type = FieldType.Keyword)
    private String location;

//    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    @Field(type = FieldType.Boolean)
    private boolean flagged;

    @Field(type = FieldType.Integer)
    private Integer riskScore;

    @Field(type = FieldType.Keyword)
    private String flagReasons;

    @Field(type = FieldType.Keyword)
    private String riskLevel;

//    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime processedAt;
}