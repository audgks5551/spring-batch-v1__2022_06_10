package io.spring.batch.helloworld;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.tasklet.CallableTaskletAdapter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Callable;

//@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class CallableTaskletConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    /**
     * 스텝이 실행되는 되는 스레드가 아닌 다른 스레드에서 실행
     */
    @Bean
    public Callable<RepeatStatus> callableObject() {
        return () -> {
            System.out.println("다른 스레드에서 실행되었습니다");
            return RepeatStatus.FINISHED;
        };
    }

    /**
     * tasklet
     */
    @Bean
    public CallableTaskletAdapter tasklet() {
        CallableTaskletAdapter callableTaskletAdapter =
                new CallableTaskletAdapter();

        callableTaskletAdapter.setCallable(callableObject());

        return callableTaskletAdapter;
    }

    /**
     * step에 CallableTaskletAdapter 장착
     */
    @Bean
    public Step callableStep() {
        return this.stepBuilderFactory.get("callableStep")
                .tasklet(tasklet())
                .build();
    }

    /**
     * job
     */
    @Bean
    public Job callableJob() {
        return this.jobBuilderFactory.get("callableJob")
                .start(callableStep())
                .build();
    }
}
