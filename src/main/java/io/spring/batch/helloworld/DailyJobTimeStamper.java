package io.spring.batch.helloworld;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersIncrementer;

import java.util.Date;

/**
 * 매 실행마다 파라미터에 현재날짜 부여하기
 */
public class DailyJobTimeStamper implements JobParametersIncrementer {
    @Override
    public JobParameters getNext(JobParameters jobParameters) {
        return new JobParametersBuilder(jobParameters)
                .addDate("currentDate", new Date())
                .toJobParameters();
    }
}
