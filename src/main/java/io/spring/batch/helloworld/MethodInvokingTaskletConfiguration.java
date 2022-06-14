package io.spring.batch.helloworld;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.tasklet.MethodInvokingTaskletAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class MethodInvokingTaskletConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    /**
     * job
     */
    @Bean
    public Job methodInvokingJob() {
        return this.jobBuilderFactory.get("methodInvokingStep")
                .start(methodInvokingStep())
                .build();
    }

    /**
     * step
     */
    @Bean
    public Step methodInvokingStep() {
        return this.stepBuilderFactory.get("methodInvokingStep")
                .tasklet(methodInvokingTasklet(null))
                .build();
    }

    /**
     * tasklet
     *  - 늦은 바인딩 사용
     *  - 매개변수 추가
     */
    @StepScope
    @Bean
    public MethodInvokingTaskletAdapter methodInvokingTasklet(
            @Value("#{jobParameters['message']}") String message) {
        MethodInvokingTaskletAdapter methodInvokingTaskletAdapter =
                new MethodInvokingTaskletAdapter();

        methodInvokingTaskletAdapter.setTargetObject(service());
        methodInvokingTaskletAdapter.setTargetMethod("serviceMethod");
        methodInvokingTaskletAdapter.setArguments(new String[] {message});

        return methodInvokingTaskletAdapter;
    }

    /**
     * service
     */
    private CustomService service() {
        return new CustomService();
    }
}
