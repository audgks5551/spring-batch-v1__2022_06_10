package io.spring.batch.helloworld;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.util.StringUtils;

/**
 * 직접 validator 정의
 */
public class ParameterValidator implements JobParametersValidator {

    @Override
    public void validate(JobParameters jobParameters) throws JobParametersInvalidException {
        String fileName = jobParameters.getString("fileName");

        if (!StringUtils.hasText(fileName)) {
            throw new JobParametersInvalidException("fileName parameter is missing.");
        } else if (!StringUtils.endsWithIgnoreCase(fileName, "csv")) {
            throw new JobParametersInvalidException("fileName parameter does not use the csv file extension");
        }
    }
}
