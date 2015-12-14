//
// Copyright 2015 Amazon.com, Inc. or its affiliates (Amazon). All Rights Reserved.
//
// Code generated by AWS Mobile Hub. Amazon gives unlimited permission to 
// copy, distribute and modify it.
//
// Source code generated from template: aws-my-sample-app-android v0.4
//
package com.amazonaws.mobile;

import com.amazonaws.regions.Regions;

/**
 * This class defines constants for the developer's resource
 * identifiers and API keys. It should be kept private.
 */
public class AWSConfiguration {

    // AWS MobileHub user agent string
    public static final String AWS_MOBILEHUB_USER_AGENT =
        "MobileHub 81e4e503-073a-4644-b615-92828df08eaa aws-my-sample-app-android-v0.4";
    // AMAZON COGNITO
    public static final Regions AMAZON_COGNITO_REGION =
        Regions.US_EAST_1;
    public static final String  AMAZON_COGNITO_IDENTITY_POOL_ID =
        "us-east-1:d0ce233b-0460-439b-8f7c-d3ca0136e220";
    // AMAZON MOBILE ANALYTICS
    public static final String  AMAZON_MOBILE_ANALYTICS_APP_ID =
        "07c0040f2beb48ae8d8a7a8fef71707e";
    // Amazon Mobile Analytics region
    public static final Regions AMAZON_MOBILE_ANALYTICS_REGION = Regions.US_EAST_1;
    public static final String AMAZON_CONTENT_DELIVERY_S3_BUCKET =
        "vomwee-contentdelivery-mobilehub-2051844282";
    public static final String AMAZON_CLOUD_FRONT_DISTRIBUTION_DOMAIN =
        "d1jarvrs4c1a5v.cloudfront.net";
    // S3 BUCKET
    public static final String AMAZON_S3_USER_FILES_BUCKET =
        "vomwee-userfiles-mobilehub-2051844282";
}